package io.ivyteam.devops.branch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;

class TestBranch {

  private static final Date NOW = new Date();

  private static final Branch BRANCH = Branch.create()
      .lastCommitAuthor("alex")
      .protectedBranch(false)
      .name("test-alex")
      .repository("axonivy/test")
      .authoredDate(NOW)
      .build();

  @Test
  void name() {
    assertThat(BRANCH.name()).isEqualTo("test-alex");
  }

  @Test
  void repository() {
    assertThat(BRANCH.repository()).isEqualTo("axonivy/test");
  }

  @Test
  void lastCommitAuthor() {
    assertThat(BRANCH.lastCommitAuthor()).isEqualTo("alex");
  }

  @Test
  void authoredDate() {
    assertThat(BRANCH.authoredDate()).isEqualTo(NOW);
  }

  @Test
  void protectedBranch() {
    assertThat(BRANCH.protectedBranch()).isFalse();
  }

  @Test
  void repoLink() {
    assertThat(BRANCH.repoLink()).isEqualTo("/repository/axonivy/test");
  }

  @Test
  void ghRepoLink() {
    assertThat(BRANCH.ghRepoLink()).isEqualTo("https://github.com/axonivy/test");
  }

  @Test
  void ghLink() {
    assertThat(BRANCH.ghLink()).isEqualTo("https://github.com/axonivy/test/tree/test-alex");
  }
}
