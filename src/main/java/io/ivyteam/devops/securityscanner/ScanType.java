package io.ivyteam.devops.securityscanner;

public enum ScanType {

  DEPENDABOT("dependabot", "/security/dependabot/"),
  SECRET_SCANNING("secret-scanning", "/security/secret-scanning/"),
  CODE_SCANNING("code-scanning", "/security/code-scanning/");

  private final String value;
  private final String urlSuffix;

  ScanType(String value, String urlSuffix) {
    this.value = value;
    this.urlSuffix = urlSuffix;
  }

  public String getValue() {
    return value;
  }

  public String getUrlSuffix() {
    return urlSuffix;
  }

  public static ScanType fromValue(String value) {
    for (ScanType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return null;
  }
}
