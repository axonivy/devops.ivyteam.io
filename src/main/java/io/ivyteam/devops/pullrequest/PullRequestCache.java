package io.ivyteam.devops.pullrequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PullRequestCache {

  private final Map<CacheKey, List<PullRequest>> users = new HashMap<>();

  public PullRequestCache(List<PullRequest> all) {
    for (var pr : all) {
      var key = new CacheKey(pr.repository(), pr.branchName());
      var ps = users.computeIfAbsent(key, k -> new ArrayList<>());
      ps.add(pr);
    }
  }

  public List<PullRequest> get(String name, String branch) {
    return users.getOrDefault(new CacheKey(name, branch), List.of());
  }

  private record CacheKey(String repo, String branch) {
  };
}
