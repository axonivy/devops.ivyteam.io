package io.ivyteam.devops.github;

import java.io.IOException;

import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubRepoConfigurator {

  private final Repo repo;

  @Autowired
  private BranchRepository branches;

  public GitHubRepoConfigurator(Repo repo) {
    this.repo = repo;
  }

  public void run() {
    try {
      GHRepository ghRepo = GitHubProvider.get().getRepository(repo.name());
      var brs = branches.findByRepo(repo.name());
      if (!repo.deleteBranchOnMerge()) {
        ghRepo.deleteBranchOnMerge(true);
      }
      if (repo.projects()) {
        ghRepo.enableProjects(false);
      }
      if (!repo.issues()) {
        ghRepo.enableIssueTracker(true);
      }
      if (repo.wiki()) {
        ghRepo.enableWiki(false);
      }
      if (repo.hooks()) {
        for (var hook : ghRepo.getHooks()) {
          hook.delete();
        }
      }
      for (var br : brs) {
        if (needsProtection(br)) {
          if (!br.protectedBranch()) {
            var ghBranch = ghRepo.getBranch(br.name());
            ghBranch.enableProtection()
                .requiredReviewers(0)
                .includeAdmins()
                .enable();
          }
        } else {
          if (br.protectedBranch()) {
            var ghBranch = ghRepo.getBranch(br.name());
            ghBranch.disableProtection();
          }
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private boolean needsProtection(Branch branch) {
    for (var prefix : SettingsManager.INSTANCE.get().branchProtectionPrefixes().split(",")) {
      if (branch.name().startsWith(prefix)) {
        return false;
      }
    }
    return false;
  }
}
