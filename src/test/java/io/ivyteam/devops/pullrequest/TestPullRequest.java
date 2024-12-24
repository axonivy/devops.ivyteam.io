package io.ivyteam.devops.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestPullRequest {

  private static final PullRequest PR = PullRequest.create()
      .title("PR-1")
      .user("alex")
      .repository("axonivy/test")
      .id(1)
      .branchName("master")
      .build();

  @Test
  void title() {
    assertThat(PR.title()).isEqualTo("PR-1");
  }

  @Test
  void user() {
    assertThat(PR.user()).isEqualTo("alex");
  }

  @Test
  void repository() {
    assertThat(PR.repository()).isEqualTo("axonivy/test");
  }

  @Test
  void id() {
    assertThat(PR.id()).isEqualTo(1);
  }

  @Test
  void branchName() {
    assertThat(PR.branchName()).isEqualTo("master");
  }
}
