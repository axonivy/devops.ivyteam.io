package io.ivyteam.devops.users;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Streams;

import io.ivyteam.devops.repo.RepoRepository;

@Repository
public class UserRepository {

  public List<User> all() {
    var repos = RepoRepository.INSTANCE.all();

    var stream1 = repos.stream()
        .flatMap(repo -> repo.branches().stream())
        .map(branch -> branch.lastCommitAuthor());
    var stream2 = repos.stream()
        .flatMap(repo -> repo.prs().stream())
        .map(branch -> branch.user());

    return Streams.concat(stream1, stream2)
        .distinct()
        .map(name -> new User(name))
        .toList();
  }
}
