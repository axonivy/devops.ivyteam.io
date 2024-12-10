package io.ivyteam.devops.securityscanner;

public record SecurityScanner(
    String repo,
    String scantype,
    int critical,
    int high,
    int medium,
    int low) {

  private final static String URL_PREFIX = "https://github.com/";

  public String link_dependabot() {
    return URL_PREFIX + repo + ScanTypeEnum.DEPENDABOT.getUrlSuffix();
  }

  public String link_secretScan() {
    return URL_PREFIX + repo + ScanTypeEnum.SECRET_SCANNING.getUrlSuffix();
  }

  public String link_codeScan() {
    return URL_PREFIX + repo + ScanTypeEnum.CODE_SCANNING.getUrlSuffix();
  }
}
