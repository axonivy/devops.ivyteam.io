package io.ivyteam.devops.github;

import java.io.IOException;

import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;

import io.ivyteam.devops.security.JwtToken;
import io.ivyteam.devops.settings.SettingsManager;

@Service
public class GitHubProvider {

  private static final long TOKEN_LIFETIME = 60000;

  private GHAppInstallation installation;
  private long tokenCreated;
  private String token;

  public GitHub get() {
    createToken();
    try {
      return new GitHubBuilder()
          .withAppInstallationToken(token)
          .build();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public String org() {
    createToken();
    return installation.getAccount().getLogin();
  }

  private void createToken() {
    if (installation == null || token == null || TOKEN_LIFETIME < tokenCreated) {
      this.installation = installation();
      this.token = token();
    }
  }

  private GHAppInstallation installation() {
    try {
      var settings = SettingsManager.INSTANCE.get();
      tokenCreated = System.currentTimeMillis();
      var jwtToken = JwtToken.createJWT(settings.gitHubAppId(), 60000);
      var gitHubApp = new GitHubBuilder().withJwtToken(jwtToken).build();
      return gitHubApp.getApp().getInstallationById(Long.valueOf(settings.gitHubAppInstallationId()));
    } catch (Exception ex) {
      throw new RuntimeException("Could not load GitHub Application Installation", ex);
    }
  }

  private String token() {
    try {
      return installation
          .createToken()
          .create()
          .getToken();
    } catch (IOException ex) {
      throw new RuntimeException("Could not load generate token", ex);
    }
  }
}
