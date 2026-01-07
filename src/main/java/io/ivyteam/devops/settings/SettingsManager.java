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

    settings.gitHubClientId(properties.getProperty(Settings.GITHUB_CLIENT_ID, ""));
    settings.gitHubClientSecret(properties.getProperty(Settings.GITHUB_CLIENT_SECRET, ""));
    settings.gitHubAppId(properties.getProperty(Settings.GITHUB_APP_ID, ""));
    settings.gitHubAppInstallationId(properties.getProperty(Settings.GITHUB_APP_INSTALLATION_ID, ""));
    settings.excludedBranchPrefixes(properties.getProperty(Settings.EXCLUDED_BRANCH_PREFIXES,
        "master,release/,stale/,dependabot/,gh-pages,dev/"));
    settings.branchProtectionPrefixes(properties.getProperty(Settings.BRANCH_PROTECTION_PREFIXES,
        "master,release/"));
    settings.autolinkUrl(properties.getProperty(Settings.AUTOLINK_URL, ""));
    settings.autolinkPrefix(properties.getProperty(Settings.AUTOLINK_PREFIX, ""));
    return settings;
  }

  public void save(Settings settings) {
    var properties = new java.util.Properties();
    properties.setProperty(Settings.GITHUB_CLIENT_ID, settings.gitHubClientId());
    properties.setProperty(Settings.GITHUB_CLIENT_SECRET, settings.gitHubClientSecret());
    properties.setProperty(Settings.GITHUB_APP_ID, settings.gitHubAppId());
    properties.setProperty(Settings.GITHUB_APP_INSTALLATION_ID, settings.gitHubAppInstallationId());
    properties.setProperty(Settings.EXCLUDED_BRANCH_PREFIXES, settings.excludedBranchPrefixes());
    properties.setProperty(Settings.BRANCH_PROTECTION_PREFIXES, settings.branchProtectionPrefixes());
    properties.setProperty(Settings.AUTOLINK_URL, settings.autolinkUrl());
    properties.setProperty(Settings.AUTOLINK_PREFIX, settings.autolinkPrefix());

    try (var out = Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE)) {
      properties.store(out, null);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
