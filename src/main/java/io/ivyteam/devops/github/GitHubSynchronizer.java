package io.ivyteam.devops.github;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
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

public class GitHubSynchronizer {

  private final Consumer<Progress> progress;

  public GitHubSynchronizer(Consumer<Progress> progress) {
    this.progress = progress;
  }

  public void run() {
    progress.accept(new Progress("Delete Database", 0));
    Database.delete();
    progress.accept(new Progress("Create Database", 0));
    Database.create();

    try (var connection = Database.connection()) {
      progress.accept(new Progress("Loading repositories from GitHub Organization axonivy", 0));
      var repos = reposFor("axonivy");
      double counter = 0;
      double allRepos = repos.size();
      for (var repo : repos) {
        counter++;

        var text = "Indexing repository " + repo.getFullName() + " (" + (int) counter + "/" + (int) allRepos + ")";
        var percent = (counter * 100 / allRepos) / 100;
        progress.accept(new Progress(text, percent));

        var settingsLog = new GitHubRepoConfigurator(repo, true).analyze().stream().collect(Collectors.joining("\n"));

        var repository = new RepoRepository();

        var name = repo.getFullName();
        var archived = repo.isArchived();
        boolean licence = hasLicence(repo);
        var gitHubPrs = repo.getPullRequests(GHIssueState.OPEN);
        var openPullRequests = gitHubPrs.size();

        var prs = gitHubPrs.stream()
            .map(this::toPullRequest)
            .toList();

        var branches = repo.getBranches().values().stream()
            .map(b -> toBranch(b, repo)).toList();

        var rr = new Repo(name, archived, openPullRequests, licence, settingsLog, prs, branches);
        repository.create(rr);
      }
    } catch (IOException | SQLException ex) {
      throw new RuntimeException(ex);
    }

    progress.accept(new Progress("Indexing finished", 1));
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
      return List.copyOf(org.getRepositories().values())
          .stream()
          .map(r -> {
            try {
              return org.getRepository(r.getName());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          })
          .toList();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public record Progress(String message, double percent) {

  }
}
