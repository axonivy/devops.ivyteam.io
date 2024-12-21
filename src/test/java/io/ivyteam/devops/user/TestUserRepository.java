package io.ivyteam.devops.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;

class TestUserRepository {

  private static final User ALEX = new User("alex", "https://example.com/alex.png");
  private static final User LOUIS = new User("louis", "https://example.com/louis.png");

  @TempDir
  Path tempDir;

  UserRepository users;

  @BeforeEach
  void beforeEach() {
    var db = new Database(tempDir.resolve("test.db"));
    users = new UserRepository(db);
  }

  @Test
  void create() {
    users.create(ALEX);
    assertThat(users.all()).containsExactly(ALEX);
  }

  @Test
  void exists() {
    users.create(ALEX);
    assertThat(users.exists(ALEX)).isTrue();
  }

  @Test
  void doesNotExists() {
    assertThat(users.exists(LOUIS)).isFalse();
  }

  @Test
  void all_ordered() {
    users.create(LOUIS);
    users.create(ALEX);
    assertThat(users.all()).containsExactly(ALEX, LOUIS);
  }

  @Test
  void all_empty() {
    assertThat(users.all()).isEmpty();
  }
}
