package io.ivyteam.devops.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequest;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.securityscanner.ScanTypeEnum;
import io.ivyteam.devops.securityscanner.SecurityScannerApiHelper;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository;
import io.ivyteam.devops.user.UserRepository;
import io.ivyteam.devops.user.UserUpdate;

@Service
public class GitHubSynchronizer {

  @Autowired
  GitHubProvider gitHub;

  @Autowired
  private RepoRepository repos;

  @Autowired
  private PullRequestRepository prs;

  @Autowired
  private BranchRepository branches;

  @Autowired
  private UserRepository users;

  @Autowired
  private SecurityScannerRepository securityScanners;

  private boolean isRunning = false;

  private Progress progress;

  private final List<Consumer<Progress>> progressListener = new CopyOnWriteArrayList<>();

  public void addListener(Consumer<Progress> progress) {
    progressListener.add(progress);
  }

  public void removeListener(Consumer<Progress> progress) {
    progressListener.remove(progress);
  }

  private void notify(String msg, double work) {
    this.progress = new Progress(msg, work);
    var remove = new ArrayList<Consumer<Progress>>();
    for (var listener : progressListener) {
      try {
        listener.accept(progress);
      } catch (Exception ex) {
        remove.add(listener);
      }
    }
    progressListener.removeAll(remove);
  }

  public Progress getProgress() {
    return progress;
  }

  public synchronized void run() {
    isRunning = true;
    try {
      var org = gitHub.org();

      notify("Loading repositories from GitHub Organization " + org, 0);

      var repos = reposFor(org);
      double counter = 0;
      double allRepos = repos.size();
      for (var repo : repos) {
        counter++;

        var text = "Indexing repository " + repo.getFullName() + " (" + (int) counter
            + "/" + (int) allRepos + ")";
        var percent = (counter * 100 / allRepos) / 100;
        notify(text, percent);

        repo = gitHub.get().getRepository(repo.getFullName());
        synch(repo);
        var helper = new SecurityScannerApiHelper(securityScanners, repo, gitHub.token());
        if (repo.isVulnerabilityAlertsEnabled()) {
          helper.synch(ScanTypeEnum.DEPENDABOT.getValue());
        }
        if (!repo.isPrivate()) {
          helper.synch(ScanTypeEnum.CODE_SCANNING.getValue());
          helper.synch(ScanTypeEnum.SECRET_SCANNING.getValue());
        }

      }
      notify("Indexing finished", 1);

      for (var r : this.repos.all()) {
        notify("Update repository settings of " + r.name(), 1);
        var configurator = new GitHubRepoConfigurator(gitHub, branches, r);
        var changed = configurator.run();
        if (changed) {
          synch(r);
        }
      }

      for (var u : users.all()) {
        try {
          var user = gitHub.get().getUser(u.name());
          if (user != null) {
            var update = UserUpdate.create(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .build();
            users.update(update);
          }
        } catch (Exception ex) {
          System.out.println("Could not find user " + u.name());
        }
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      isRunning = false;
      progressListener.clear();
    }
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void synch(Repo repository) {
    try {
      var repo = gitHub.get().getRepository(repository.name());
      synch(repo);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void synch(GHRepository repo) throws IOException {
    var name = repo.getFullName();
    var archived = repo.isArchived();
    var privateRepo = repo.isPrivate();
    var licence = license(repo, "LICENSE");
    var securityMd = license(repo, "SECURITY.md");
    var codeOfConduct = license(repo, "CODE_OF_CONDUCT.md");
    var gitHubPrs = repo.getPullRequests(GHIssueState.OPEN);

    boolean deleteBranchOnMerge = repo.isDeleteBranchOnMerge();
    boolean projects = repo.hasProjects();
    boolean issues = repo.hasIssues();
    boolean wiki = repo.hasWiki();
    boolean hooks = !repo.getHooks().isEmpty();
    boolean fork = repo.isFork();
    boolean isVulnAlertOn = repo.isVulnerabilityAlertsEnabled();

    var ghRepo = repo;

    var rr = new Repo(name, archived, privateRepo, deleteBranchOnMerge, projects, issues, wiki, hooks, fork,
        isVulnAlertOn, licence, securityMd, codeOfConduct);
    repos.create(rr);

    gitHubPrs.stream()
        .map(this::toPullRequest)
        .forEach(prs::create);

    repo.getBranches().values().stream()
        .map(b -> toBranch(b, ghRepo))
        .forEach(branches::create);
  }

  private String license(GHRepository repo, String file) throws IOException {
    try {
      try (var in = repo.getFileContent(file).read()) {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (GHFileNotFoundException e) {
      return null;
    }
  }

  private PullRequest toPullRequest(GHPullRequest pr) {
    try {
      var title = pr.getTitle();
      var user = pr.getUser().getLogin();
      var id = pr.getNumber();
      var p = new PullRequest(pr.getRepository().getFullName(), id, title, user, pr.getHead().getRef());
      return p;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Branch toBranch(GHBranch branch, GHRepository repo) {
    try {
      var lastCommit = repo.getCommit(branch.getSHA1());
      var author = lastCommit.getAuthor();
      var lastCommitAuthor = "[unknown]";
      if (author != null) {
        lastCommitAuthor = author.getLogin();
      }
      return new Branch(repo.getFullName(), branch.getName(), lastCommitAuthor, branch.isProtected(),
          lastCommit.getAuthoredDate());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<GHRepository> reposFor(String orgName) {
    try {
      var org = gitHub.get().getOrganization(orgName);
      return List.copyOf(org.getRepositories().values()).stream()
          // .limit(10)
          .toList();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public record Progress(String message, double percent) {

  }
}
