package io.ivyteam.devops.settings;

public class Settings {

  public static final String GITHUB_ORG = "github.org";
  public static final String GITHUB_CLIENT_ID = "github.client.id";
  public static final String GITHUB_CLIENT_SECRET = "github.client.secret";
  public static final String GITHUB_APP_ID = "github.app.id";
  public static final String GITHUB_APP_INSTALLATION_ID = "github.app.installation.id";
  public static final String EXCLUDED_BRANCH_PREFIXES = "excluded.branch.prefixes";
  public static final String BRANCH_PROTECTION_PREFIXES = "branch.protection.prefixes";

  private String gitHubOrg = "";
  private String gitHubClientId = "";
  private String gitHubClientSecret = "";
  private String gitHubAppId = "";
  private String gitHubAppInstallationId = "";
  private String excludedBranchPrefixes = "";
  private String branchProtectionPrefixes = "";

  public String gitHubOrg() {
    return gitHubOrg;
  }

  public void gitHubOrg(String gitHubOrg) {
    this.gitHubOrg = gitHubOrg;
  }

  public String gitHubClientId() {
    return gitHubClientId;
  }

  public void gitHubClientId(String gitHubClientId) {
    this.gitHubClientId = gitHubClientId;
  }

  public String gitHubClientSecret() {
    return gitHubClientSecret;
  }

  public void gitHubClientSecret(String gitHubClientSecret) {
    this.gitHubClientSecret = gitHubClientSecret;
  }

  public String gitHubAppId() {
    return gitHubAppId;
  }

  public void gitHubAppId(String gitHubAppId) {
    this.gitHubAppId = gitHubAppId;
  }

  public String gitHubAppInstallationId() {
    return gitHubAppInstallationId;
  }

  public void gitHubAppInstallationId(String gitHubAppInstallationId) {
    this.gitHubAppInstallationId = gitHubAppInstallationId;
  }

  public String excludedBranchPrefixes() {
    return excludedBranchPrefixes;
  }

  public void excludedBranchPrefixes(String excludedBranchPrefixes) {
    this.excludedBranchPrefixes = excludedBranchPrefixes;
  }

  public String branchProtectionPrefixes() {
    return branchProtectionPrefixes;
  }

  public void branchProtectionPrefixes(String branchProtectionPrefixes) {
    this.branchProtectionPrefixes = branchProtectionPrefixes;
  }
}
