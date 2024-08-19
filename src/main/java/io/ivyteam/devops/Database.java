package io.ivyteam.devops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {

  private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

  private static final String NAME = "github.db";

  public static boolean exists() {
    var path = Path.of(NAME);
    return Files.exists(path);
  }

  public static void create() {
    LOGGER.info("Create database");
    try (var c = connection()) {
      try (var stmt = c.createStatement()) {
        var sql = """
             CREATE TABLE repository
             (
               name VARCHAR(200) PRIMARY KEY NOT NULL,
               archived INTEGER NOT NULL,
               openPullRequests INTEGER NOT NULL,
               license INTEGER NOT NULL
             )
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
    if (exists()) {
      try {
        LOGGER.info("Delete database");
        Files.delete(Path.of(NAME));
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  public static Connection connection() {
    try {
      Class.forName("org.sqlite.JDBC");
      return DriverManager.getConnection("jdbc:sqlite:" + NAME);
    } catch (ClassNotFoundException | SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
