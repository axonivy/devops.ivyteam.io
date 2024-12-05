package io.ivyteam.devops.dependabot;

public record Dependabot(
    String repo,
    int critical,
    int high,
    int medium,
    int low) {

  public String link() {
    String prefix = "https://github.com/";
    String suffix = "/security/dependabot/";
    return prefix + repo + suffix;
  }

}
