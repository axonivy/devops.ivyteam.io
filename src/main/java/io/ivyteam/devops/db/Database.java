package io.ivyteam.devops.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

  private static final String VERSION = "3";
  private static final Path PATH = Path.of("data", "githubV" + VERSION + ".db");

  private static boolean exists() {
    return Files.exists(PATH);
  }

  public static void create() {
    try (var c = dbConnection()) {
      try (var stmt = c.createStatement()) {
        try (var in = Database.class.getResourceAsStream("schema.sql")) {
          var sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
          stmt.executeUpdate(sql);
        }
      }
    } catch (SQLException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Connection connection() {
    if (!exists()) {
      create();
    }
    return dbConnection();
  }

  private static Connection dbConnection() {
    try {
      Files.createDirectories(PATH.getParent());
      Class.forName("org.sqlite.JDBC");
      var connection = DriverManager.getConnection("jdbc:sqlite:" + PATH);
      try (var stmt = connection.createStatement()) {
        stmt.execute("PRAGMA foreign_keys = ON");
      }
      return connection;

    } catch (ClassNotFoundException | SQLException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
