package io.ivyteam.devops.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.file.File;
import io.ivyteam.devops.file.FileRepository;
import io.ivyteam.devops.pullrequest.PullRequest;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.securityscanner.ScanType;
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
  private FileRepository files;

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

  public void synch(GHRepository ghRepo) throws IOException {
    var repo = Repo.create()
        .name(ghRepo.getFullName())
        .archived(ghRepo.isArchived())
        .privateRepo(ghRepo.isPrivate())
        .deleteBranchOnMerge(ghRepo.isDeleteBranchOnMerge())
        .projects(ghRepo.hasProjects())
        .issues(ghRepo.hasIssues())
        .wiki(ghRepo.hasWiki())
        .hooks(!ghRepo.getHooks().isEmpty())
        .fork(ghRepo.isFork())
        .isVulnAlertOn(ghRepo.isVulnerabilityAlertsEnabled())
        .build();
    repos.create(repo);

    for (var f : List.of(File.LICENSE, File.SECURITY_MD, File.CODE_OF_CONDUCT_MD, File.RENOVATE_JSON,
        File.GITHUB_RENOVATE_JSON)) {
      var content = readFile(ghRepo, f);
      if (content != null && !content.isEmpty()) {
        var file = File.create()
            .repository(repo.name())
            .path(f)
            .content(content)
            .build();
        files.create(file);
      }
    }

    ghRepo.getPullRequests(GHIssueState.OPEN).stream()
        .map(this::toPullRequest)
        .forEach(prs::create);

    ghRepo.getBranches().values().stream()
        .map(b -> toBranch(b, ghRepo))
        .forEach(branches::create);

    syncSecurityScanner(ghRepo);
  }

  private String readFile(GHRepository repo, String file) throws IOException {
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
      return PullRequest.create()
          .repository(pr.getRepository().getFullName())
          .id(pr.getNumber())
          .title(pr.getTitle())
          .user(pr.getUser().getLogin())
          .branchName(pr.getHead().getRef())
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Branch toBranch(GHBranch branch, GHRepository repo) {
    try {
      var lastCommit = repo.getCommit(branch.getSHA1());
      return Branch.create()
          .repository(repo.getFullName())
          .name(branch.getName())
          .lastCommitAuthor(toLastCommitAuthor(lastCommit))
          .protectedBranch(branch.isProtected())
          .authoredDate(lastCommit.getAuthoredDate())
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String toLastCommitAuthor(GHCommit commit) throws IOException {
    var author = commit.getAuthor();
    if (author != null) {
      return author.getLogin();
    }
    return "[unknown]";
  }

  private List<GHRepository> reposFor(String orgName) {
    try {
      var org = gitHub.get().getOrganization(orgName);
      return List.copyOf(org.getRepositories().values()).stream()
          // .filter(r -> r.getFullName().contains("dev-workflow-ui"))
          // .limit(50)
          .toList();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void syncSecurityScanner(GHRepository repo) throws IOException {
    var helper = new SecurityScannerApiHelper(securityScanners, repo, gitHub.token());
    if (repo.isVulnerabilityAlertsEnabled()) {
      helper.synch(ScanType.DEPENDABOT);
    }
    if (!repo.isPrivate()) {
      helper.synch(ScanType.CODE_SCANNING);
      helper.synch(ScanType.SECRET_SCANNING);
    }
  }

  public record Progress(String message, double percent) {

  }
}
