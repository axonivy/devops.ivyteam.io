package io.ivyteam.devops.user;

public record UserUpdate(String name, String avatarUrl) {

  public static Builder create(String name) {
    return new Builder(name);
  }

  public static class Builder {

    private String name;
    private String avatarUrl;

    private Builder(String name) {
      this.name = name;
    }

    public Builder avatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
      return this;
    }

    public UserUpdate build() {
      return new UserUpdate(name, avatarUrl);
    }
  }
}
