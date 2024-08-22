package io.ivyteam.devops.branches;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Branch;

@Repository
public class BranchRepository {

  @Autowired
  private Database db;

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
    try (var connection = db.connection()) {
      try (var s = connection
          .prepareStatement("INSERT INTO branch (repository, name, lastCommitAuthor) VALUES (?, ?, ?)")) {
        s.setString(1, branch.repository());
        s.setString(2, branch.name());
        s.setString(3, branch.lastCommitAuthor());
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void delete(Branch branch) {
    try (var connection = db.connection()) {
      try (var s = connection.prepareStatement("DELETE FROM branch WHERE repository = ? AND name = ?")) {
        s.setString(1, branch.repository());
        s.setString(2, branch.name());
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
    return new Branch(repository, name, lastCommitAuthor);
  }
}
