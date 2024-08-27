package io.ivyteam.devops.github;

import java.io.IOException;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubRepoConfigurator {

  private final BranchRepository branches;
  private final Repo repo;

  public GitHubRepoConfigurator(BranchRepository branches, Repo repo) {
    this.branches = branches;
    this.repo = repo;
  }

  public boolean run() {
    try {
      var changed = false;
      var ghRepo = GitHubProvider.get().getRepository(repo.name());
      if (ghRepo.isPrivate()) {
        return changed;
      }
      var brs = branches.findByRepo(repo.name());
      if (!repo.deleteBranchOnMerge()) {
        ghRepo.deleteBranchOnMerge(true);
        changed = true;
      }
      if (repo.projects()) {
        ghRepo.enableProjects(false);
        changed = true;
      }
      if (!repo.issues()) {
        ghRepo.enableIssueTracker(true);
        changed = true;
      }
      if (repo.wiki()) {
        ghRepo.enableWiki(false);
        changed = true;
      }
      if (repo.hooks()) {
        for (var hook : ghRepo.getHooks()) {
          hook.delete();
          changed = true;
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
            changed = true;
          }
        } else {
          if (br.protectedBranch()) {
            var ghBranch = ghRepo.getBranch(br.name());
            ghBranch.disableProtection();
            changed = true;
          }
        }
      }
      return changed;
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
