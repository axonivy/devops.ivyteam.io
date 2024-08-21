package io.ivyteam.devops.github;

import java.io.IOException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import io.ivyteam.devops.settings.SettingsManager;

public class GitHubProvider {

  public static GitHub get() {
    try {
      var token = SettingsManager.get().gitHubToken();
      return new GitHubBuilder()
          .withOAuthToken(token)
          .build();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
