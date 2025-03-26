package io.ivyteam.devops.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;

@Repository
public class UserRepository {

  @Autowired
  private Database db;

  public UserRepository(Database db) {
    this.db = db;
  }

  public List<User> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM user ORDER BY login")) {
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(User user) {
    try (var connection = db.connection()) {
      try (var stmt = connection
          .prepareStatement(
              "INSERT INTO user (login, name, email, company, location, bio, avatarUrl) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
        stmt.setString(1, user.login());
        stmt.setString(2, user.name());
        stmt.setString(3, user.email());
        stmt.setString(4, user.company());
        stmt.setString(5, user.location());
        stmt.setString(6, user.bio());
        stmt.setString(7, user.avatarUrl());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void update(User user) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement(
          "UPDATE user SET name = (?), email = (?), company = (?), location = (?), bio = (?), avatarUrl = (?) WHERE login = (?)")) {
        stmt.setString(1, user.name());
        stmt.setString(2, user.email());
        stmt.setString(3, user.company());
        stmt.setString(4, user.location());
        stmt.setString(5, user.bio());
        stmt.setString(6, user.avatarUrl());
        stmt.setString(7, user.login());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public boolean exists(User user) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM user WHERE login = ?")) {
        stmt.setString(1, user.login());
        try (var result = stmt.executeQuery()) {
          return result.next();
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<User> query(PreparedStatement stmt) throws SQLException {
    try (var result = stmt.executeQuery()) {
      var users = new ArrayList<User>();
      while (result.next()) {
        var user = toUser(result);
        users.add(user);
      }
      return users;
    }
  }

  private User toUser(ResultSet result) throws SQLException {
    return User.create()
        .login(result.getString("login"))
        .name(result.getString("name"))
        .email(result.getString("email"))
        .company(result.getString("company"))
        .location(result.getString("location"))
        .bio(result.getString("bio"))
        .avatarUrl(result.getString("avatarUrl"))
        .build();
  }
}
