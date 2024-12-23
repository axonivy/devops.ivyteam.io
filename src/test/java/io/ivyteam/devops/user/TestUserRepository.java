package io.ivyteam.devops.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.ivyteam.devops.db.Database;

class TestUserRepository {

  private static final User ALEX = User.create()
      .name("alex")
      .avatarUrl("https://example.com/alex.png")
      .build();

  private static final User LOUIS = User.create()
      .name("louis")
      .avatarUrl("https://example.com/louis.png")
      .build();

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

  @Test
  void update() {
    users.create(ALEX);
    var update = UserUpdate.create("alex")
        .avatarUrl("https://example.com/suter.png")
        .build();
    users.update(update);
    var suter = User.create()
        .name("alex")
        .avatarUrl("https://example.com/suter.png")
        .build();
    assertThat(users.all()).containsExactly(suter);
  }

  @Test
  void update_nonExisting() {
    var update = UserUpdate.create("lukas")
        .avatarUrl("https://example.com/alex.png")
        .build();
    users.update(update);
    assertThat(users.all()).isEmpty();
  }
}
