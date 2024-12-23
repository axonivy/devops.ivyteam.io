package io.ivyteam.devops.user;

public record User(String name, String avatarUrl) {

  public String link() {
    return "/users/" + name;
  }

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String name;
    private String avatarUrl;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder avatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
      return this;
    }

    public User build() {
      return new User(name, avatarUrl);
    }
  }
}
