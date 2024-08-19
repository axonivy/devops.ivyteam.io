package io.ivyteam.devops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;

public class GitHubSynchronizer {

  public void run() {
    if (Database.exists()) {
      return;
    }
    Database.create();

    try (var connection = Database.connection()) {
      for (var repo : reposFor("axonivy")) {
        try (var stmt = connection.prepareStatement(
            "INSERT INTO repository (name, archived, openPullRequests, license) VALUES (?, ?, ?, ?)")) {
          stmt.setString(1, repo.getFullName());
          stmt.setInt(2, repo.isArchived() ? 1 : 0);
          stmt.setInt(3, repo.getPullRequests(GHIssueState.OPEN).size());
          int licence;
          try {
            licence = repo.getFileContent("LICENSE") == null ? 0 : 1;
          } catch (GHFileNotFoundException ex) {
            licence = 0;
          }
          stmt.setInt(4, licence);
          stmt.execute();
        }
      }

    } catch (IOException | SQLException e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  private static List<GHRepository> reposFor(String orgName) {
    try {
      var org = GitHubProvider.get().getOrganization(orgName);
      return List.copyOf(org.getRepositories().values()).stream()
          .toList();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
