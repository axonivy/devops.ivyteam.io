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
            var settingsLog = result.getString("settingsLog");

            var repo = new Repo(name, archived, privateRepo, license, securityMd, codeOfConduct, settingsLog);
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
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("DELETE FROM repository WHERE name = ?")) {
        stmt.setString(1, repo.name());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO repository (name, archived, private, license, securityMd, codeOfConduct, settingsLog) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, repo.name());
        stmt.setInt(2, repo.archived() ? 1 : 0);
        stmt.setInt(3, repo.privateRepo() ? 1 : 0);
        stmt.setString(4, repo.license());
        stmt.setString(5, repo.securityMd());
        stmt.setString(6, repo.codeOfConduct());
        stmt.setString(7, repo.settingsLog());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
