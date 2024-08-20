package io.ivyteam.devops;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.ivyteam.devops.db.Database;

class TestDatabase {

  @Test
  void exists() {
    Assertions.assertThat(Database.exists()).isFalse();
  }
}
