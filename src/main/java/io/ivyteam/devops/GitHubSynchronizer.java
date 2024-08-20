package io.ivyteam.devops;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;

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

        var settingsLog = new RepoConfigurator(repo, true).analyze().stream().collect(Collectors.joining("\n"));

        try (var stmt = connection.prepareStatement(
            "INSERT INTO repository (name, archived, openPullRequests, license, settingsLog) VALUES (?, ?, ?, ?, ?)")) {
          stmt.setString(1, repo.getFullName());
          stmt.setInt(2, repo.isArchived() ? 1 : 0);
          
          var prs = repo.getPullRequests(GHIssueState.OPEN);
          stmt.setInt(3, prs.size());

          for (var pr : prs) {            
            var title = pr.getTitle();
            var user = pr.getUser().getLogin();
            var id = pr.getNumber();

            try (var s = connection.prepareStatement("INSERT INTO pull_request (repository, id, title, user) VALUES (?, ?, ?, ?)")) {              
              s.setString(1, repo.getFullName());
              s.setLong(2, id);
              s.setString(3, title);
              s.setString(4, user);
              s.execute();
            }            
          }

          int licence;
          try {
            licence = repo.getFileContent("LICENSE") == null ? 0 : 1;
          } catch (GHFileNotFoundException e) {
            licence = 0;
          }
          stmt.setInt(4, licence);
          stmt.setString(5, settingsLog);
          stmt.execute();
        }
      }
    } catch (IOException | SQLException ex) {
      throw new RuntimeException(ex);
    }

    progress.accept(new Progress("Indexing finished", 1));
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
