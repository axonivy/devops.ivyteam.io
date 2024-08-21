package io.ivyteam.devops.settings;

public class Settings {

  public static final String GITHUB_ORG = "github.org";
  public static final String GITHUB_TOKEN = "github.token";

  private String gitHubOrg = "";
  private String gitHubToken = "";

  public String gitHubOrg() {
    return gitHubOrg;
  }

  public void gitHubOrg(String gitHubOrg) {
    this.gitHubOrg = gitHubOrg;
  }

  public String gitHubToken() {
    return gitHubToken;
  }

  public void gitHubToken(String gitHubToken) {
    this.gitHubToken = gitHubToken;
  }
}
