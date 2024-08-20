package io.ivyteam.devops.github;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class GitHubProvider {

  public static GitHub get() {
    var file = System.getProperty("GITHUB.TOKEN.FILE", "github.token");
    var path = Path.of(file);
    try {
      var token = Files.readString(path).strip();
      return new GitHubBuilder()
              .withOAuthToken(token)
              .build();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
