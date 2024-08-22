package io.ivyteam.devops.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.pullrequest.PullRequest;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubSynchronizer {

  public static final GitHubSynchronizer INSTANCE = new GitHubSynchronizer();

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
    try (var connection = new Database().connection()) {
      var org = SettingsManager.INSTANCE.get().gitHubOrg();

      notify("Loading repositories from GitHub Organization " + org, 0);
      var repos = reposFor(org);
      double counter = 0;
      double allRepos = repos.size();
      for (var repo : repos) {
        counter++;

        var text = "Indexing repository " + repo.getFullName() + " (" + (int) counter + "/" + (int) allRepos + ")";
        var percent = (counter * 100 / allRepos) / 100;
        notify(text, percent);

        synch(repo);
      }
      notify("Indexing finished", 1);
    } catch (IOException | SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      isRunning = false;
      progressListener.clear();
    }
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void synch(GHRepository repo) throws IOException {
    var org = GitHubProvider.get().getOrganization(repo.getOwnerName());
    repo = org.getRepository(repo.getName()); // reload to load all settings

    var settingsLog = new GitHubRepoConfigurator(repo, true).run().stream().collect(Collectors.joining("\n"));

    var repository = new RepoRepository();

    var name = repo.getFullName();
    var archived = repo.isArchived();
    var privateRepo = repo.isPrivate();
    var licence = license(repo, "LICENSE");
    var securityMd = license(repo, "SECURITY.md");
    var codeOfConduct = license(repo, "CODE_OF_CONDUCT.md");
    var gitHubPrs = repo.getPullRequests(GHIssueState.OPEN);

    var prs = gitHubPrs.stream()
        .map(this::toPullRequest)
        .toList();

    var ghRepo = repo;
    var branches = repo.getBranches().values().stream()
        .map(b -> toBranch(b, ghRepo)).toList();

    var rr = new Repo(name, archived, privateRepo, licence, securityMd, codeOfConduct, settingsLog);
    repository.create(rr);

    for (var pr : prs) {
      new PullRequestRepository().create(pr);
    }

    for (var branch : branches) {
      new BranchRepository().create(branch);
    }
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
      var p = new PullRequest(pr.getRepository().getFullName(), id, title, user);
      return p;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Branch toBranch(GHBranch branch, GHRepository repo) {
    try {
      var lastCommit = repo.getCommit(branch.getSHA1());
      var author = lastCommit.getAuthor();
      var lastCommitAuthor = "?";
      if (author != null) {
        lastCommitAuthor = author.getLogin();
      }
      return new Branch(repo.getFullName(), branch.getName(), lastCommitAuthor);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<GHRepository> reposFor(String orgName) {
    try {
      var org = GitHubProvider.get().getOrganization(orgName);
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
