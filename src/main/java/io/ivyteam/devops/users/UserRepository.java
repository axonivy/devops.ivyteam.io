package io.ivyteam.devops.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Streams;

import io.ivyteam.devops.branches.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestRepository;

@Repository
public class UserRepository {

  @Autowired
  private BranchRepository branches;

  @Autowired
  private PullRequestRepository pullRequests;

  public List<User> all() {
    var stream1 = branches.all().stream().map(branch -> branch.lastCommitAuthor());
    var stream2 = pullRequests.all().stream().map(branch -> branch.user());
    return Streams.concat(stream1, stream2)
        .distinct()
        .map(name -> new User(name))
        .toList();
  }
}
