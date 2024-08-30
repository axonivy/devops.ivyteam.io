package io.ivyteam.devops.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestSettings {

  @Test
  void gitHubAppId() {
    var settings = new Settings();
    settings.gitHubAppId("abc");
    assertThat(settings.gitHubAppId()).isEqualTo("abc");
  }
}
