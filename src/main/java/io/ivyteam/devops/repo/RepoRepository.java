package io.ivyteam.devops.repo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.ivyteam.devops.db.Database;

public class RepoRepository {

  public static final RepoRepository INSTANCE = new RepoRepository();

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
            var settingsLog = result.getString("settingsLog");

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

            var branches = new ArrayList<Branch>();
            try (var stmtB = connection.prepareStatement("SELECT * FROM branch WHERE repository = ?")) {
              stmtB.setString(1, name);
              try (var resultB = stmtB.executeQuery()) {
                while (resultB.next()) {
                  var bName = resultB.getString("name");
                  var lastCommitAuthor = resultB.getString("lastCommitAuthor");
                  branches.add(new Branch(name, bName, lastCommitAuthor));
                }
              }
            }

            var repo = new Repo(name, archived, openPullRequests, license, settingsLog, prs, branches);
            repos.add(repo);
          }
          return repos;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(Repo repo) {
    try (var connection = Database.connection()) {
      try (var stmt = connection.prepareStatement(
          "INSERT INTO repository (name, archived, openPullRequests, license, settingsLog) VALUES (?, ?, ?, ?, ?)")) {
        stmt.setString(1, repo.name());
        stmt.setInt(2, repo.archived() ? 1 : 0);
        stmt.setInt(3, repo.openPullRequests());
        stmt.setInt(4, repo.license() ? 1 : 0);
        stmt.setString(5, repo.settingsLog());
        stmt.execute();

        for (var pr : repo.prs()) {
          try (var s = connection
              .prepareStatement("INSERT INTO pull_request (repository, id, title, user) VALUES (?, ?, ?, ?)")) {
            s.setString(1, repo.name());
            s.setLong(2, pr.id());
            s.setString(3, pr.title());
            s.setString(4, pr.user());
            s.execute();
          }
        }

        for (var branch : repo.branches()) {
          try (var s = connection
              .prepareStatement("INSERT INTO branch (repository, name, lastCommitAuthor) VALUES (?, ?, ?)")) {
            s.setString(1, repo.name());
            s.setString(2, branch.name());
            s.setString(3, branch.lastCommitAuthor());
            s.execute();
          }
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
