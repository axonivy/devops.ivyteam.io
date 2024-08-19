package io.ivyteam.devops;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepoRepository {

  public static final RepoRepository INSTANCE = new RepoRepository();

  public record Repo(String name, boolean archived, int openPullRequests, boolean license) {

  }

  public List<Repo> all() {
    try (var connection = Database.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM repository")) {
        try (var result = stmt.executeQuery()) {
          var repos = new ArrayList<Repo>();
          while (result.next()) {
            var name = result.getString("name");
            var archived = result.getInt("archived") == 1;
            var openPullRequests = result.getInt("openPullRequests");
            var license = result.getInt("license") == 1;
            var repo = new Repo(name, archived, openPullRequests, license);
            repos.add(repo);
          }
          return repos;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
