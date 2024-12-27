package io.ivyteam.devops.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;

class TestRepoRepository {

  private static final Repo REPO = Repo.create()
      .name("axonivy/test")
      .build();

  @TempDir
  Path tempDir;

  RepoRepository repos;

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    repos = new RepoRepository(db);
  }

  @Test
  void create() {
    repos.create(REPO);
    assertThat(repos.all()).containsExactly(REPO);
  }

  @Test
  void exist() {
    repos.create(REPO);
    assertThat(repos.exist("axonivy/test")).isTrue();
  }

  @Test
  void doesNotExist() {
    assertThat(repos.exist("axonivy/test")).isFalse();
  }
}
