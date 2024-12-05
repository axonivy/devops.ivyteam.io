package io.ivyteam.devops.dependabot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;

@Repository
public class DependabotRepository {

  @Autowired
  private Database db;

  public List<Dependabot> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM dependabot ORDER BY repository")) {
        try (var result = stmt.executeQuery()) {
          return getDependabots(stmt.executeQuery());
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public Dependabot getByRepo(String repoName) {
    if (repoName == null) {
      return null;
    }
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM dependabot where repository = ?")) {
        stmt.setString(1, repoName);
        var dependabots = getDependabots(stmt.executeQuery());
        if (!dependabots.isEmpty()) {
          return dependabots.getFirst();
        }
        return null;
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(Dependabot dependabot) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("DELETE FROM dependabot WHERE repository = ?")) {
        stmt.setString(1, dependabot.repo());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO dependabot (repository, critical, high, medium, low) VALUES (?, ?, ?, ?, ?)")) {
        stmt.setString(1, dependabot.repo());
        stmt.setInt(2, dependabot.critical());
        stmt.setInt(3, dependabot.high());
        stmt.setInt(4, dependabot.medium());
        stmt.setInt(5, dependabot.low());

        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<Dependabot> getDependabots(ResultSet result) {
    var dependabots = new ArrayList<Dependabot>();
    try {
      while (result.next()) {
        var repository = result.getString("repository");
        var critical = result.getInt("critical");
        var vulnAlertsHigh = result.getInt("high");
        var medium = result.getInt("medium");
        var low = result.getInt("low");

        var dependabot = new Dependabot(repository, critical, vulnAlertsHigh, medium, low);
        dependabots.add(dependabot);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return dependabots;
  }
}
