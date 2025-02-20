package io.ivyteam.devops.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

@Service
public class Database {

  private static final String VERSION = "19";
  private final Path path;

  public Database() {
    this(Path.of("data", "devopsV" + VERSION + ".db"));
  }

  public Database(Path path) {
    this.path = path;
  }

  public Connection connection() {
    if (!exists()) {
      create();
    }
    return dbConnection();
  }

  private Connection dbConnection() {
    try {
      Files.createDirectories(path.getParent());
      Class.forName("org.sqlite.JDBC");
      var connection = DriverManager.getConnection("jdbc:sqlite:" + path);
      try (var stmt = connection.createStatement()) {
        stmt.execute("PRAGMA foreign_keys = ON");
      }
      return connection;

    } catch (ClassNotFoundException | SQLException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private boolean exists() {
    return Files.exists(path);
  }

  private void create() {
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
}
