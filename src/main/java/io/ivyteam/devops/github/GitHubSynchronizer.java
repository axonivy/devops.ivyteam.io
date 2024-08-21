package io.ivyteam.devops.github;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.PullRequest;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubSynchronizer {

  public static final GitHubSynchronizer INSTANCE = new GitHubSynchronizer();

  private boolean isRunning = false;

  private final List<Consumer<Progress>> progressListener = new CopyOnWriteArrayList<>();

  public void addListener(Consumer<Progress> progress) {
    progressListener.add(progress);
  }

  public void removeListener(Consumer<Progress> progress) {
    progressListener.remove(progress);
  }

  private void notify(String msg, double work) {
    progressListener.forEach(listener -> listener.accept(new Progress(msg, work)));
  }

  public synchronized void run() {
    isRunning = true;
    try (var connection = Database.connection()) {
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
    boolean licence = hasLicence(repo);
    var gitHubPrs = repo.getPullRequests(GHIssueState.OPEN);
    var openPullRequests = gitHubPrs.size();

    var prs = gitHubPrs.stream()
        .map(this::toPullRequest)
        .toList();

    var ghRepo = repo;
    var branches = repo.getBranches().values().stream()
        .map(b -> toBranch(b, ghRepo)).toList();

    var rr = new Repo(name, archived, openPullRequests, licence, settingsLog, prs, branches);
    repository.create(rr);
  }

  private boolean hasLicence(GHRepository repo) throws IOException {
    boolean licence;
    try {
      licence = repo.getFileContent("LICENSE") != null;
    } catch (GHFileNotFoundException e) {
      licence = false;
    }
    return licence;
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
