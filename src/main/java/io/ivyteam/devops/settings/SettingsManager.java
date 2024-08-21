package io.ivyteam.devops.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {

  public static final SettingsManager INSTANCE = new SettingsManager();

  private final Path path;

  private SettingsManager() {
    this(Path.of("data", "devops.properties"));
  }

  SettingsManager(Path path) {
    this.path = path;
  }

  public Settings get() {
    var settings = new Settings();
    if (!Files.exists(path)) {
      return settings;
    }

    var properties = new java.util.Properties();
    try (var in = Files.newInputStream(path)) {
      properties.load(in);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    settings.gitHubOrg(properties.getProperty(Settings.GITHUB_ORG, ""));
    settings.gitHubToken(properties.getProperty(Settings.GITHUB_TOKEN, ""));
    return settings;
  }

  public void save(Settings settings) {
    var properties = new java.util.Properties();
    properties.setProperty(Settings.GITHUB_ORG, settings.gitHubOrg());
    properties.setProperty(Settings.GITHUB_TOKEN, settings.gitHubToken());
    try (var out = Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE)) {
      properties.store(out, null);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
