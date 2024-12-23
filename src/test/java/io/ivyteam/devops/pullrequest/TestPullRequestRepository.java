package io.ivyteam.devops.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.user.UserRepository;

class TestPullRequestRepository {

  @TempDir
  Path tempDir;

  RepoRepository repos;

  PullRequestRepository prs;

  PullRequest PR = PullRequest
      .create()
      .title("PR-1")
      .user("alex")
      .repository("axonivy/test")
      .id(1)
      .branchName("master")
      .build();

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    var users = new UserRepository(db);
    repos = new RepoRepository(db);
    repos.create(Repo.create().name("axonivy/test").build());
    prs = new PullRequestRepository(db, users);
  }

  @Test
  void all_empty() {
    assertThat(prs.all()).isEmpty();
  }

  @Test
  void findByUser_empty() {
    assertThat(prs.findByUser("suter")).isEmpty();
  }

  @Test
  void findByUser() {
    prs.create(PR);
    assertThat(prs.findByUser("alex")).containsExactly(PR);
  }

  @Test
  void findByRepository_empty() {
    assertThat(prs.findByRepository("gotham")).isEmpty();
  }

  @Test
  void findByRepository() {
    prs.create(PR);
    assertThat(prs.findByRepository("axonivy/test")).containsExactly(PR);
  }

  @Test
  void delete() {
    prs.create(PR);
    prs.delete(PR);
    assertThat(prs.all()).isEmpty();
  }

  @Test
  void countByRepo() {
    prs.create(PR);
    assertThat(prs.countByRepo()).containsExactly(Map.entry("axonivy/test", 1L));
  }

  @Test
  void create() {
    prs.create(PR);
    assertThat(prs.all()).containsExactly(PR);
  }
}
