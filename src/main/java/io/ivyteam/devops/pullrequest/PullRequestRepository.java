package io.ivyteam.devops.pullrequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.PullRequest;

@Repository
public class PullRequestRepository {

  public List<PullRequest> findByUser(String userName) {
    try (var connection = Database.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM pull_request WHERE user = ? ORDER BY title")) {
        stmt.setString(1, userName);
        try (var result = stmt.executeQuery()) {
          var prs = new ArrayList<PullRequest>();
          while (result.next()) {
            var pr = toPr(result);
            prs.add(pr);
          }
          return prs;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<PullRequest> findByRepository(String repo) {
    try (var connection = Database.connection()) {
      try (var stmt = connection.prepareStatement("SELECT * FROM pull_request WHERE repository = ? ORDER BY title")) {
        stmt.setString(1, repo);
        try (var result = stmt.executeQuery()) {
          var prs = new ArrayList<PullRequest>();
          while (result.next()) {
            var pr = toPr(result);
            prs.add(pr);
          }
          return prs;
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private PullRequest toPr(ResultSet result) throws SQLException {
    var repository = result.getString("repository");
    var id = result.getLong("id");
    var title = result.getString("title");
    var user = result.getString("user");
    var pr = new PullRequest(repository, id, title, user);
    return pr;
  }
}
