package io.ivyteam.devops.securityscanner;

public record SecurityScanner(
    String repo,
    ScanType scantype,
    int critical,
    int high,
    int medium,
    int low) {

  private final static String URL_PREFIX = "https://github.com/";

  public String link() {
    return switch (scantype) {
      case DEPENDABOT -> URL_PREFIX + repo + ScanType.DEPENDABOT.getUrlSuffix();
      case SECRET_SCANNING -> URL_PREFIX + repo + ScanType.SECRET_SCANNING.getUrlSuffix();
      case CODE_SCANNING -> URL_PREFIX + repo + ScanType.CODE_SCANNING.getUrlSuffix();
    };
  }

  public int sort() {
    if (critical != 0) {
      return (int) (critical + Math.pow(10, 9));
    }
    if (high != 0) {
      return (int) (high + Math.pow(10, 6));
    }
    if (medium != 0) {
      return (int) (medium + Math.pow(10, 3));
    }
    return low;
  }

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String repo;
    private ScanType scantype;
    private int critical;
    private int high;
    private int medium;
    private int low;

    public Builder repo(String repo) {
      this.repo = repo;
      return this;
    }

    public Builder scantype(ScanType scantype) {
      this.scantype = scantype;
      return this;
    }

    public Builder critical(int critical) {
      this.critical = critical;
      return this;
    }

    public Builder high(int high) {
      this.high = high;
      return this;
    }

    public Builder medium(int medium) {
      this.medium = medium;
      return this;
    }

    public Builder low(int low) {
      this.low = low;
      return this;
    }

    public SecurityScanner build() {
      return new SecurityScanner(repo, scantype, critical, high, medium, low);
    }
  }
}
