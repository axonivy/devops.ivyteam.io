package io.ivyteam.devops;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepoRepository {

  public static final RepoRepository INSTANCE = new RepoRepository();

  public record Repo(String name, boolean archived, int openPullRequests, boolean license, List<PullRequest> prs) {

    public String link() {
      return "/repository/" + name;
    }
  }

  public record PullRequest(String repository, long id, String title, String user) {

    public String ghLink() {
      return "https://github.com/" + repository + "/pull/" + id;
    }
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

            var prs = new ArrayList<PullRequest>();
            try (var stmtPr = connection.prepareStatement("SELECT * FROM pull_request WHERE repository = ?")) {              
              stmtPr.setString(1, name);
              try (var resultPr = stmtPr.executeQuery()) {
                while (resultPr.next()) {                  
                  var prName = resultPr.getString("title");
                  var prUser = resultPr.getString("user");
                  var prId = resultPr.getLong("id");
                  prs.add(new PullRequest(name, prId, prName, prUser));
                }
              }
            }

            var repo = new Repo(name, archived, openPullRequests, license, prs);
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
