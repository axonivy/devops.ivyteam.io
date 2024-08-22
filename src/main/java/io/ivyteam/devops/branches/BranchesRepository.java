package io.ivyteam.devops.branches;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.RepoRepository;

@Repository
public class BranchesRepository {

  public List<Branch> findByUser(String user) {
    return RepoRepository.INSTANCE.all().stream()
        .flatMap(repo -> repo.branches().stream())
        .filter(branch -> branch.lastCommitAuthor().equals(user))
        .toList();
  }
}
