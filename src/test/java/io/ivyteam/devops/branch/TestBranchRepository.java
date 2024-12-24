package io.ivyteam.devops.branch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.user.UserRepository;

class TestBranchRepository {

  private static final Branch BRANCH = Branch.create()
      .lastCommitAuthor("alex")
      .protectedBranch(false)
      .name("test-alex")
      .repository("axonivy/test")
      .authoredDate(new java.util.Date())
      .build();

  @TempDir
  Path tempDir;

  UserRepository users;

  BranchRepository branches;

  RepoRepository repos;

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    users = new UserRepository(db);
    branches = new BranchRepository(db, users);
    repos = new RepoRepository(db);
  }

  @Test
  void create() {
    repos.create(Repo.create().name("axonivy/test").build());
    branches.create(BRANCH);
    assertThat(branches.all()).containsExactly(BRANCH);
  }

  @Test
  void delete() {
    repos.create(Repo.create().name("axonivy/test").build());
    branches.create(BRANCH);
    branches.delete("axonivy/test", "test-alex");
    assertThat(branches.all()).isEmpty();
  }

  @Test
  void findByUser_nonExisting() {
    assertThat(branches.findByUser("alex")).isEmpty();
  }

  @Test
  void findByUser() {
    repos.create(Repo.create().name("axonivy/test").build());
    branches.create(BRANCH);
    assertThat(branches.findByUser("alex")).containsExactly(BRANCH);
  }

  @Test
  void findByRepo_nonExisting() {
    assertThat(branches.findByRepo("axonivy/test")).isEmpty();
  }

  @Test
  void findByRepo() {
    repos.create(Repo.create().name("axonivy/test").build());
    branches.create(BRANCH);
    assertThat(branches.findByRepo("axonivy/test")).containsExactly(BRANCH);
  }

  @Test
  void all_empty() {
    assertThat(branches.all()).isEmpty();
  }
}
