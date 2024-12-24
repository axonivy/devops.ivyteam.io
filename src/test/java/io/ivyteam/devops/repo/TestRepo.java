package io.ivyteam.devops.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestRepo {

  private static final Repo REPO = Repo.create()
      .name("dummy/abc")
      .codeOfConduct("code of conduct")
      .deleteBranchOnMerge(true)
      .fork(false)
      .isVulnAlertOn(true)
      .license("lic")
      .issues(false)
      .privateRepo(false)
      .archived(false)
      .securityMd("security md")
      .build();

  @Test
  void name() {
    assertThat(REPO.name()).isEqualTo("dummy/abc");
  }

  @Test
  void codeOfConduct() {
    assertThat(REPO.codeOfConduct()).isEqualTo("code of conduct");
  }

  @Test
  void deleteBranchOnMerge() {
    assertThat(REPO.deleteBranchOnMerge()).isTrue();
  }

  @Test
  void fork() {
    assertThat(REPO.fork()).isFalse();
  }

  @Test
  void isVulnAlertOn() {
    assertThat(REPO.isVulnAlertOn()).isTrue();
  }

  @Test
  void license() {
    assertThat(REPO.license()).isEqualTo("lic");
  }

  @Test
  void issues() {
    assertThat(REPO.issues()).isFalse();
  }

  @Test
  void privateRepo() {
    assertThat(REPO.privateRepo()).isFalse();
  }

  @Test
  void archived() {
    assertThat(REPO.archived()).isFalse();
  }

  @Test
  void securityMd() {
    assertThat(REPO.securityMd()).isEqualTo("security md");
  }
}
