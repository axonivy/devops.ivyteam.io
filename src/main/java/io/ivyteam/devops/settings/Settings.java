package io.ivyteam.devops.settings;

public class Settings {

  public static final String GITHUB_ORG = "github.org";
  public static final String GITHUB_TOKEN = "github.token";
  public static final String EXCLUDED_BRANCH_PREFIXES = "excluded.branch.prefixes";

  private String gitHubOrg = "";
  private String gitHubToken = "";
  private String excludedBranchPrefixes = "";

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

  public String excludedBranchPrefixes() {
    return excludedBranchPrefixes;
  }

  public void excludedBranchPrefixes(String excludedBranchPrefixes) {
    this.excludedBranchPrefixes = excludedBranchPrefixes;
  }

}
