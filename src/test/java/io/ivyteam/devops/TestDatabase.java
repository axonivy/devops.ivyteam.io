package io.ivyteam.devops;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TestDatabase {

  @Test
  void exists() {
    Assertions.assertThat(Database.exists()).isFalse();
  }
}
