package io.ivyteam.devops.branch;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.user.User;
import io.ivyteam.devops.user.UserRepository;

@Repository
public class BranchRepository {

  @Autowired
  private Database db;

  @Autowired
  private UserRepository users;

  public BranchRepository(Database db, UserRepository users) {
    this.db = db;
    this.users = users;
  }

  public List<Branch> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM branch ORDER BY lastCommitAuthor, repository")) {
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<Branch> findByUser(String user) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM branch WHERE lastCommitAuthor = ?")) {
        stmt.setString(1, user);
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<Branch> findByRepo(String repo) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM branch WHERE repository = ?")) {
        stmt.setString(1, repo);
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(Branch branch) {
    var user = new User(branch.lastCommitAuthor(), "");
    if (!users.exists(user)) {
      users.create(user);
    }

    try (var connection = db.connection()) {
      try (var s = connection
          .prepareStatement(
              "INSERT OR REPLACE INTO branch (repository, name, lastCommitAuthor, protected, authoredDate) VALUES (?, ?, ?, ?, ?)")) {
        s.setString(1, branch.repository());
        s.setString(2, branch.name());
        s.setString(3, branch.lastCommitAuthor());
        s.setInt(4, branch.protectedBranch() ? 1 : 0);
        s.setDate(5, new Date(branch.authoredDate().getTime()));
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void delete(String repo, String name) {
    try (var connection = db.connection()) {
      try (var s = connection.prepareStatement("DELETE FROM branch WHERE repository = ? AND name = ?")) {
        s.setString(1, repo);
        s.setString(2, name);
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<Branch> query(PreparedStatement stmt) throws SQLException {
    try (var result = stmt.executeQuery()) {
      var branches = new ArrayList<Branch>();
      while (result.next()) {
        var branch = toBranch(result);
        branches.add(branch);
      }
      return branches;
    }
  }

  private Branch toBranch(ResultSet result) throws SQLException {
    var repository = result.getString("repository");
    var name = result.getString("name");
    var lastCommitAuthor = result.getString("lastCommitAuthor");
    var protectedBranch = result.getInt("protected") == 1;
    var authoredDate = result.getDate("authoredDate");
    return new Branch(repository, name, lastCommitAuthor, protectedBranch, authoredDate);
  }

  public Map<String, Long> countByRepo() {
    try (var connection = db.connection()) {
      try (var s = connection.prepareStatement("SELECT repository, COUNT(*) FROM branch GROUP BY repository")) {
        try (var result = s.executeQuery()) {
          var count = new HashMap<String, Long>();
          while (result.next()) {
            var repo = result.getString("repository");
            var counter = result.getLong("COUNT(*)");
            count.put(repo, counter);
          }
          return count;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
}
