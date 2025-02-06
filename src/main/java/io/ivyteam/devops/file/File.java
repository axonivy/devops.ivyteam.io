package io.ivyteam.devops.file;

public record File(
    String repository,
    String path,
    String content) {

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
