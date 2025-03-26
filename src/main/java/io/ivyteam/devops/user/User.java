package io.ivyteam.devops.user;

public record User(
    String login,
    String name,
    String email,
    String company,
    String location,
    String bio,
    String avatarUrl) {

  public String link() {
    return "/users/" + login;
  }

  public String ghLink() {
    return "https://github.com/" + login;
  }

  public static Builder create() {
    return new Builder();
  }

  public static class Builder {

    private String login;
    private String name;
    private String email;
    private String company;
    private String location;
    private String bio;
    private String avatarUrl;

    public Builder login(String login) {
      this.login = login;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder company(String company) {
      this.company = company;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder bio(String bio) {
      this.bio = bio;
      return this;
    }

    public Builder avatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
      return this;
    }

    public User build() {
      return new User(login, name, email, company, location, bio, avatarUrl);
    }
  }
}
