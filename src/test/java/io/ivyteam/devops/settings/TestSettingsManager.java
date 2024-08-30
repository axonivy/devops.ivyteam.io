package io.ivyteam.devops.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestSettingsManager {

  @TempDir
  java.nio.file.Path tempDir;

  @Test
  void get() {
    var manager = new SettingsManager(tempDir.resolve("abc.properties"));
    var settings = manager.get();
    assertThat(settings.gitHubOrg()).isEmpty();
  }

  @Test
  void set() {
    var manager = new SettingsManager(tempDir.resolve("abc.properties"));
    var settings = manager.get();
    settings.gitHubOrg("myorg");
    manager.save(settings);

    settings = manager.get();
    assertThat(settings.gitHubOrg()).isEqualTo("myorg");
  }
}
