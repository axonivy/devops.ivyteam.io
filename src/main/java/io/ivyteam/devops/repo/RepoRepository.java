package io.ivyteam.devops.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;

@Repository
public class RepoRepository {

  @Autowired
  private Database db;

  public RepoRepository(Database db) {
    this.db = db;
  }

  public List<Repo> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM repository ORDER BY name")) {
        try (var result = stmt.executeQuery()) {
          var repos = new ArrayList<Repo>();
          while (result.next()) {
            var repo = toRepo(result);
            repos.add(repo);
          }
          return repos;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Repo toRepo(ResultSet result) throws SQLException {
    return Repo.create()
        .name(result.getString("name"))
        .archived(result.getInt("archived") == 1)
        .privateRepo(result.getInt("private") == 1)
        .deleteBranchOnMerge(result.getInt("deleteBranchOnMerge") == 1)
        .projects(result.getInt("projects") == 1)
        .issues(result.getInt("issues") == 1)
        .wiki(result.getInt("wiki") == 1)
        .hooks(result.getInt("hooks") == 1)
        .fork(result.getInt("fork") == 1)
        .isVulnAlertOn(result.getInt("isVulnAlertOn") == 1)
        .autolinks(result.getString("autolinks"))
        .build();
  }

  public boolean exist(String name) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT COUNT(*) FROM repository WHERE name = ?")) {
        stmt.setString(1, name);
        try (var result = stmt.executeQuery()) {
          result.next();
          return result.getInt(1) > 0;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(Repo repo) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("DELETE FROM repository WHERE name = ?")) {
        stmt.setString(1, repo.name());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO repository (name, archived, private, deleteBranchOnMerge, projects, issues, wiki, hooks, fork, isVulnAlertOn, autolinks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, repo.name());
        stmt.setInt(2, repo.archived() ? 1 : 0);
        stmt.setInt(3, repo.privateRepo() ? 1 : 0);
        stmt.setInt(4, repo.deleteBranchOnMerge() ? 1 : 0);
        stmt.setInt(5, repo.projects() ? 1 : 0);
        stmt.setInt(6, repo.issues() ? 1 : 0);
        stmt.setInt(7, repo.wiki() ? 1 : 0);
        stmt.setInt(8, repo.hooks() ? 1 : 0);
        stmt.setInt(9, repo.fork() ? 1 : 0);
        stmt.setInt(10, repo.isVulnAlertOn() ? 1 : 0);
        stmt.setString(11, repo.autolinks());

        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
