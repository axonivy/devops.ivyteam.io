package io.ivyteam.devops.securityscanner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;

@Repository
public class SecurityScannerRepository {

  @Autowired
  private Database db;

  public SecurityScannerRepository(Database db) {
    this.db = db;
  }

  public java.util.Map<Key, SecurityScanner> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection
          .prepareStatement("SELECT * FROM securityscanner")) {
        try (var result = stmt.executeQuery()) {
          var scanners = new HashMap<Key, SecurityScanner>();
          while (result.next()) {
            var scanner = toSecurityScanner(result);
            var key = new Key(scanner.repo(), scanner.scantype());
            scanners.put(key, scanner);
          }
          return scanners;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public record Key(String repo, ScanType scantype) {
  }

  public void create(SecurityScanner securityScanner) {
    try (var connection = db.connection()) {
      try (
          var stmt = connection.prepareStatement("DELETE FROM securityscanner WHERE repository = ? and scantype = ?")) {
        stmt.setString(1, securityScanner.repo());
        stmt.setString(2, securityScanner.scantype().getValue());
        stmt.execute();
      }

      try (var stmt = connection.prepareStatement(
          "INSERT INTO securityscanner (repository, scantype, msg, critical, high, medium, low) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, securityScanner.repo());
        stmt.setString(2, securityScanner.scantype().getValue());
        stmt.setString(3, securityScanner.msg());
        stmt.setInt(4, securityScanner.critical());
        stmt.setInt(5, securityScanner.high());
        stmt.setInt(6, securityScanner.medium());
        stmt.setInt(7, securityScanner.low());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private SecurityScanner toSecurityScanner(ResultSet result) throws SQLException {
    return SecurityScanner.create()
        .repo(result.getString("repository"))
        .scantype(ScanType.fromValue(result.getString("scantype")))
        .msg(result.getString("msg"))
        .critical(result.getInt("critical"))
        .high(result.getInt("high"))
        .medium(result.getInt("medium"))
        .low(result.getInt("low"))
        .build();
  }
}
