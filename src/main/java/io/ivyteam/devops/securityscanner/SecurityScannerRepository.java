package io.ivyteam.devops.securityscanner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;

@Repository
public class SecurityScannerRepository {

  @Autowired
  private Database db;

  public List<SecurityScanner> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM securityscanner ORDER BY repository, scantype")) {
        try (var result = stmt.executeQuery()) {
          return getSecurityScanner(stmt.executeQuery());
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public SecurityScanner getByRepoAndScantype(String repoName, String scantype) {
    if (repoName == null || scantype == null) {
      return null;
    }
    try (var connection = db.connection()) {
      try (var stmt = connection
          .prepareStatement("SELECT * FROM securityscanner where repository = ? and scantype = ?")) {
        stmt.setString(1, repoName);
        stmt.setString(2, scantype);
        var securityScanners = getSecurityScanner(stmt.executeQuery());
        if (!securityScanners.isEmpty()) {
          return securityScanners.getFirst();
        }
        return null;
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(SecurityScanner securityScanner) {
    try (var connection = db.connection()) {
      try (
          var stmt = connection.prepareStatement("DELETE FROM securityscanner WHERE repository = ? and scantype = ?")) {
        stmt.setString(1, securityScanner.repo());
        stmt.setString(2, securityScanner.scantype());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO securityscanner (repository, scantype, critical, high, medium, low) VALUES (?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, securityScanner.repo());
        stmt.setString(2, securityScanner.scantype());
        stmt.setInt(3, securityScanner.critical());
        stmt.setInt(4, securityScanner.high());
        stmt.setInt(5, securityScanner.medium());
        stmt.setInt(6, securityScanner.low());

        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<SecurityScanner> getSecurityScanner(ResultSet result) {
    var securityScanners = new ArrayList<SecurityScanner>();
    try {
      while (result.next()) {
        var repository = result.getString("repository");
        var scantype = result.getString("scantype");
        var critical = result.getInt("critical");
        var high = result.getInt("high");
        var medium = result.getInt("medium");
        var low = result.getInt("low");

        var securityScanner = new SecurityScanner(repository, scantype, critical, high, medium, low);
        securityScanners.add(securityScanner);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return securityScanners;
  }
}
