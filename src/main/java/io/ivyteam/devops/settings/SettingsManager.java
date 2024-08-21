package io.ivyteam.devops.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {

  private static final Path PATH = Path.of("data", "devops.properties");

  public static Settings get() {
    var settings = new Settings();
    if (!Files.exists(PATH)) {
      return settings;
    }

    var properties = new java.util.Properties();
    try (var in = Files.newInputStream(PATH)) {
      properties.load(in);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    settings.gitHubOrg(properties.getProperty("github.org", ""));
    settings.gitHubToken(properties.getProperty("github.token", ""));
    return settings;
  }

  public static void save(Settings settings) {
    var properties = new java.util.Properties();
    properties.setProperty("github.org", settings.gitHubOrg());
    properties.setProperty("github.token", settings.gitHubToken());
    try (var out = Files.newOutputStream(PATH, java.nio.file.StandardOpenOption.CREATE)) {
      properties.store(out, null);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
