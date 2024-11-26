package io.ivyteam.devops.user;

public record User(String name, String avatarUrl) {

  public String link() {
    return "/users/" + name;
  }
}
