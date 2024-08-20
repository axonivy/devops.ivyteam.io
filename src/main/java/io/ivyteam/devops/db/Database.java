package io.ivyteam.devops.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

  private static final String NAME = "github.db";

  public static boolean exists() {
    var path = Path.of(NAME);
    return Files.exists(path);
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
             DROP TABLE IF EXISTS pull_request;
             DROP TABLE IF EXISTS repository;             
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
      Class.forName("org.sqlite.JDBC");
      return DriverManager.getConnection("jdbc:sqlite:" + NAME);
    } catch (ClassNotFoundException | SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
