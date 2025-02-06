package io.ivyteam.devops.user;

import java.util.HashMap;
import java.util.Map;

public class UserCache {

  private final Map<String, User> users = new HashMap<>();

  public UserCache(java.util.List<User> all) {
    for (var user : all) {
      users.put(user.name(), user);
    }
  }

  public User get(String name) {
    return users.get(name);
  }
}
