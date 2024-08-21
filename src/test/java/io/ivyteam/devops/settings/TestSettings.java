package io.ivyteam.devops.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestSettings {

  @Test
  void gitHubOrg() {
    var settings = new Settings();
    settings.gitHubOrg("abc");
    assertThat(settings.gitHubOrg()).isEqualTo("abc");
  }

  @Test
  void gitHubToken() {
    var settings = new Settings();
    settings.gitHubToken("tok");
    assertThat(settings.gitHubToken()).isEqualTo("tok");
  }
}
