package io.ivyteam.devops.repo;

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
            var name = result.getString("name");
            var archived = result.getInt("archived") == 1;
            var privateRepo = result.getInt("private") == 1;
            var license = result.getString("license");
            var securityMd = result.getString("securityMd");
            var codeOfConduct = result.getString("codeOfConduct");
            var deleteBranchOnMerge = result.getInt("deleteBranchOnMerge") == 1;
            var projects = result.getInt("projects") == 1;
            var issues = result.getInt("issues") == 1;
            var wiki = result.getInt("wiki") == 1;
            var hooks = result.getInt("hooks") == 1;
            var fork = result.getInt("fork") == 1;
            var isVulnAlertOn = result.getInt("isVulnAlertOn") == 1;

            var repo = new Repo(name, archived, privateRepo, deleteBranchOnMerge, projects, issues, wiki, hooks, fork,
                isVulnAlertOn, license, securityMd, codeOfConduct);
            repos.add(repo);
          }
          return repos;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
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
          "INSERT INTO repository (name, archived, private, deleteBranchOnMerge, projects, issues, wiki, hooks, fork, isVulnAlertOn, license, securityMd, codeOfConduct) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
        stmt.setString(11, repo.license());
        stmt.setString(12, repo.securityMd());
        stmt.setString(13, repo.codeOfConduct());

        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
