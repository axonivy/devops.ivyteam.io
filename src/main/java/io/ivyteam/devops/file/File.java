package io.ivyteam.devops.file;

public record File(
    String repository,
    String path,
    String content) {

  public static final String LICENSE = "LICENSE";
  public static final String RENOVATE_JSON = "renovate.json";
  public static final String GITHUB_RENOVATE_JSON = ".github/renovate.json";
  public static final String SECURITY_MD = "SECURITY.md";
  public static final String CODE_OF_CONDUCT_MD = "CODE_OF_CONDUCT.md";

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String repository;
    private String path;
    private String content;

    public Builder repository(String repository) {
      this.repository = repository;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public File build() {
      return new File(repository, path, content);
    }
  }
}
