package io.ivyteam.devops.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

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

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    var users = new UserRepository(db);
    repos = new RepoRepository(db);
    prs = new PullRequestRepository(db, users);
  }

  @Test
  void all_empty() {
    assertThat(prs.all()).isEmpty();
  }

  @Test
  void create() {
    repos.create(Repo.create().name("axonivy/test").build());
    var pr = PullRequest
        .create()
        .title("PR-1")
        .user("alex")
        .repository("axonivy/test")
        .id(1)
        .branchName("master")
        .build();

    prs.create(pr);
    assertThat(prs.all()).containsExactly(pr);
  }
}
