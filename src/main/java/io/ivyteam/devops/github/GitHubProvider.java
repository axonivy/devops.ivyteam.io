package io.ivyteam.devops.github;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import io.ivyteam.devops.security.JwtToken;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubProvider {

  public static GitHub get() {
    try {
      var settings = SettingsManager.INSTANCE.get();
      var jwtToken = JwtToken.createJWT(settings.gitHubAppId(), 600000);
      var gitHubApp = new GitHubBuilder().withJwtToken(jwtToken).build();
      var appInstallation = gitHubApp.getApp().getInstallationById(Long.valueOf(settings.gitHubAppInstallationId()));

      var token = appInstallation
          .createToken()
          .create()
          .getToken();

      return new GitHubBuilder()
          .withAppInstallationToken(token)
          .build();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
