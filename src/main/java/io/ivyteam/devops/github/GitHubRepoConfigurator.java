package io.ivyteam.devops.github;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.settings.SettingsManager;

public class GitHubRepoConfigurator {

  private final static Logger LOGGER = LoggerFactory.getLogger(GitHubRepoConfigurator.class);

  private final GitHubProvider gitHub;
  private final BranchRepository branches;
  private final Repo repo;

  public GitHubRepoConfigurator(GitHubProvider gitHub, BranchRepository branches, Repo repo) {
    this.gitHub = gitHub;
    this.branches = branches;
    this.repo = repo;
  }

  public boolean run() {
    try {
      LOGGER.info("Update settings of " + repo.name());
      var changed = false;
      var ghRepo = gitHub.get().getRepository(repo.name());
      if (ghRepo.isPrivate()) {
        LOGGER.info("Skip " + repo.name() + " because it is private");
        return changed;
      }
      if (ghRepo.isArchived()) {
        LOGGER.info("Skip " + repo.name() + " because it is archived");
        return changed;
      }
      var brs = branches.findByRepo(repo.name());
      if (!repo.deleteBranchOnMerge()) {
        LOGGER.info("Set delete branch on merge to true");
        ghRepo.deleteBranchOnMerge(true);
        changed = true;
      }
      if (repo.projects()) {
        LOGGER.info("Disable projects");
        ghRepo.enableProjects(false);
        changed = true;
      }
      if (!repo.issues()) {
        LOGGER.info("Enable issues");
        ghRepo.enableIssueTracker(true);
        changed = true;
      }
      if (repo.wiki()) {
        LOGGER.info("Disable wiki");
        ghRepo.enableWiki(false);
        changed = true;
      }
      if (repo.hooks()) {
        for (var hook : ghRepo.getHooks()) {
          LOGGER.info("Delete hook");
          hook.delete();
          changed = true;
        }
      }
      for (var br : brs) {
        if (needsProtection(br)) {
          if (!br.protectedBranch()) {
            var ghBranch = ghRepo.getBranch(br.name());
            LOGGER.info("Enable protection");
            ghBranch.enableProtection()
                .requiredReviewers(0)
                .includeAdmins()
                .enable();
            changed = true;
          }
        } else {
          if (br.protectedBranch()) {
            var ghBranch = ghRepo.getBranch(br.name());
            LOGGER.info("Disable protection");
            ghBranch.disableProtection();
            changed = true;
          }
        }
      }
      return changed;
    } catch (IOException ex) {
      throw new RuntimeException("Could not update repository " + repo.name(), ex);
    }
  }

  private boolean needsProtection(Branch branch) {
    for (var prefix : SettingsManager.INSTANCE.get().branchProtectionPrefixes().split(",")) {
      LOGGER.info("Check if " + branch.name() + " starts with " + prefix);
      if (branch.name().startsWith(prefix)) {
        return false;
      }
    }
    return false;
  }
}
