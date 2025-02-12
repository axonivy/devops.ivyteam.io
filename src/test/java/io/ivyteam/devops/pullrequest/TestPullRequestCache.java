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

class TestPullRequestCache {

  @TempDir
  Path tempDir;

  RepoRepository repos;

  PullRequestRepository prs;

  PullRequestCache cache;

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
    prs.create(PR);
    cache = new PullRequestCache(prs.all());
  }

  @Test
  void nonExisting() {
    assertThat(cache.get("non-existing", "non-existing")).isEmpty();
  }

  @Test
  void existing() {
    assertThat(cache.get("axonivy/test", "master")).containsExactly(PR);
  }

  @Test
  void filterRepository() {
    repos.create(Repo.create().name("axonivy/test2").build());
    var pr1 = PullRequest
        .create()
        .title("PR-2")
        .user("alex")
        .repository("axonivy/test2")
        .id(2)
        .branchName("master")
        .build();
    prs.create(pr1);
    cache = new PullRequestCache(prs.all());
    assertThat(cache.get("axonivy/test", "master")).containsExactly(PR);
  }

  @Test
  void multiplePrsOnSameBranch() {
    var pr1 = PullRequest
        .create()
        .title("PR-2")
        .user("alex")
        .repository("axonivy/test")
        .id(2)
        .branchName("release/12.0")
        .build();
    prs.create(pr1);
    var pr2 = PullRequest
        .create()
        .title("PR-3")
        .user("alex")
        .repository("axonivy/test")
        .id(3)
        .branchName("release/12.0")
        .build();
    prs.create(pr2);
    cache = new PullRequestCache(prs.all());
    assertThat(cache.get("axonivy/test", "release/12.0")).containsExactly(pr1, pr2);
  }
}
