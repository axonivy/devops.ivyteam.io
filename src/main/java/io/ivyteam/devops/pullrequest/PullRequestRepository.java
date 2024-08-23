package io.ivyteam.devops.pullrequest;

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

@Repository
public class PullRequestRepository {

  @Autowired
  private Database db;

  public List<PullRequest> all() {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM pull_request ORDER BY title")) {
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<PullRequest> findByUser(String userName) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM pull_request WHERE user = ? ORDER BY title")) {
        stmt.setString(1, userName);
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<PullRequest> findByRepository(String repo) {
    try (var connection = db.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM pull_request WHERE repository = ? ORDER BY title")) {
        stmt.setString(1, repo);
        return query(stmt);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void create(PullRequest pr) {
    try (var connection = db.connection()) {
      try (var s = connection
          .prepareStatement("INSERT INTO pull_request (repository, id, title, user) VALUES (?, ?, ?, ?)")) {
        s.setString(1, pr.repository());
        s.setLong(2, pr.id());
        s.setString(3, pr.title());
        s.setString(4, pr.user());
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void delete(PullRequest pr) {
    try (var connection = db.connection()) {
      try (var s = connection.prepareStatement("DELETE FROM pull_request WHERE repository = ? AND id = ?")) {
        s.setString(1, pr.repository());
        s.setLong(2, pr.id());
        s.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<PullRequest> query(PreparedStatement stmt) throws SQLException {
    try (var result = stmt.executeQuery()) {
      var prs = new ArrayList<PullRequest>();
      while (result.next()) {
        var pr = toPr(result);
        prs.add(pr);
      }
      return prs;
    }
  }

  private PullRequest toPr(ResultSet result) throws SQLException {
    var repository = result.getString("repository");
    var id = result.getLong("id");
    var title = result.getString("title");
    var user = result.getString("user");
    return new PullRequest(repository, id, title, user);
  }

  public Map<String, Long> countByRepo() {
    try (var connection = db.connection()) {
      try (var s = connection.prepareStatement("SELECT repository, COUNT(*) FROM pull_request GROUP BY repository")) {
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
