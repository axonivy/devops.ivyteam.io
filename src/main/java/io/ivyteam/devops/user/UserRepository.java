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

  public List<User> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM user ORDER BY name")) {
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(User user) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("INSERT INTO user (name) VALUES (?)")) {
        stmt.setString(1, user.name());
        stmt.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public boolean exists(User user) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM user WHERE name = ?")) {
        stmt.setString(1, user.name());
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
    var name = result.getString("name");
    return new User(name);
  }
}
