package io.ivyteam.devops.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

  private static final Path PATH = Path.of("data", "github.db");

  public static boolean exists() {
    return Files.exists(PATH);
  }

  public static void create() {
    try (var c = connection()) {
      try (var stmt = c.createStatement()) {
        var sql = """
             CREATE TABLE repository
             (
               name VARCHAR(200) PRIMARY KEY NOT NULL,
               archived INTEGER NOT NULL,
               openPullRequests INTEGER NOT NULL,
               license INTEGER NOT NULL,
               settingsLog TEXT NULL
             );

             CREATE TABLE pull_request
             (
               repository VARCHAR(200) NOT NULL,
               id INTEGER NOT NULL,
               title VARCHAR(400) NOT NULL,
               user VARCHAR(200) NOT NULL,

               PRIMARY KEY(repository, id),
               FOREIGN KEY(repository) REFERENCES repository(name)
             );
            """;
        stmt.executeUpdate(sql);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void delete() {
    if (!exists()) {
      return;
    }
    try (var c = connection()) {
      try (var stmt = c.createStatement()) {
        var sql = """
            PRAGMA writable_schema = 1;
            DELETE FROM sqlite_master;
            PRAGMA writable_schema = 0;
            VACUUM;
            PRAGMA integrity_check;
            """;
        stmt.execute(sql);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }    
  }

  public static Connection connection() {
    try {
      Files.createDirectories(PATH.getParent());
      Class.forName("org.sqlite.JDBC");
      return DriverManager.getConnection("jdbc:sqlite:" + PATH);
    } catch (ClassNotFoundException | SQLException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
