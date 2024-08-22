package io.ivyteam.devops.repo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.ivyteam.devops.db.Database;

public class RepoRepository {

  public static final RepoRepository INSTANCE = new RepoRepository();

  public List<Repo> all() {
    try (var connection = Database.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM repository ORDER BY name")) {
        try (var result = stmt.executeQuery()) {
          var repos = new ArrayList<Repo>();
          while (result.next()) {
            var name = result.getString("name");
            var archived = result.getInt("archived") == 1;
            var privateRepo = result.getInt("private") == 1;
            var openPullRequests = result.getInt("openPullRequests");
            var license = result.getString("license");
            var securityMd = result.getString("securityMd");
            var codeOfConduct = result.getString("codeOfConduct");
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

            var repo = new Repo(name, archived, privateRepo, openPullRequests, license, securityMd, codeOfConduct,
                settingsLog, prs, branches);
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
      try (var stmt = connection.prepareStatement("DELETE FROM repository WHERE name = ?")) {
        stmt.setString(1, repo.name());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO repository (name, archived, private, openPullRequests, license, securityMd, codeOfConduct, settingsLog) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, repo.name());
        stmt.setInt(2, repo.archived() ? 1 : 0);
        stmt.setInt(3, repo.privateRepo() ? 1 : 0);
        stmt.setInt(4, repo.openPullRequests());
        stmt.setString(5, repo.license());
        stmt.setString(6, repo.securityMd());
        stmt.setString(7, repo.codeOfConduct());
        stmt.setString(8, repo.settingsLog());
        stmt.execute();

        for (var pr : repo.prs()) {
          createPr(connection, pr);
        }

        for (var branch : repo.branches()) {
          createBranch(connection, branch);
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void createPr(Connection connection, PullRequest pr) throws SQLException {
    try (var s = connection
        .prepareStatement("INSERT INTO pull_request (repository, id, title, user) VALUES (?, ?, ?, ?)")) {
      s.setString(1, pr.repository());
      s.setLong(2, pr.id());
      s.setString(3, pr.title());
      s.setString(4, pr.user());
      s.execute();
    }
  }

  public void createPr(PullRequest pr) {
    try (var connection = Database.connection()) {
      createPr(connection, pr);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void deletePr(PullRequest pr) {
    try (var connection = Database.connection()) {
      try (var s = connection.prepareStatement("DELETE FROM pull_request WHERE repository = ? AND id = ?")) {
        s.setString(1, pr.repository());
        s.setLong(2, pr.id());
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void createBranch(Connection connection, Branch branch) throws SQLException {
    try (var s = connection
        .prepareStatement("INSERT INTO branch (repository, name, lastCommitAuthor) VALUES (?, ?, ?)")) {
      s.setString(1, branch.repository());
      s.setString(2, branch.name());
      s.setString(3, branch.lastCommitAuthor());
      s.execute();
    }
  }

  public void createBranch(Branch branch) {
    try (var connection = Database.connection()) {
      createBranch(connection, branch);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void deleteBranch(Branch branch) {
    try (var connection = Database.connection()) {
      try (var s = connection.prepareStatement("DELETE FROM branch WHERE repository = ? AND name = ?")) {
        s.setString(1, branch.repository());
        s.setString(2, branch.name());
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
