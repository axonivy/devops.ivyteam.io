package io.ivyteam.devops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;

public class RepoConfigurator {

  private final GHRepository repo;
  private final boolean dry;
  private final List<String> log = new ArrayList<String>();

  public RepoConfigurator(GHRepository repo, boolean dry) {
    this.repo = repo;
    this.dry = dry;
  }

  public List<String> analyze() {
    try {
      deleteHeadBranchOnMerge();
      disableProjects();
      enableIssues();
      disableWiki();
      deleteHooks();
      protectBranches();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return log;
  }

  public void synch() {

  }

  RepoConfigurator deleteHeadBranchOnMerge() throws IOException {
    if (!repo.isDeleteBranchOnMerge()) {
      log("delete banch on merge");
      if (!dry) {
        repo.deleteBranchOnMerge(true);
      }
    }
    return this;
  }

  RepoConfigurator protectBranches() throws IOException {
    for (var branch : repo.getBranches().values()) {
      if (!branch.getName().equals("master") && !branch.getName().startsWith("release/")) {
        continue;
      }

      if (!isProtected(branch)) {
        log("protect " + branch.getName() + " branch");
        if (!dry) {
          branch.enableProtection()
              .requiredReviewers(0)
              .includeAdmins()
              .enable();
        }
      }
    }
    return this;
  }

  RepoConfigurator deleteHooks() throws IOException {
    for (var hook : repo.getHooks()) {
      log("delete hook " + hook.getUrl());
      if (!dry) {
        hook.delete();
      }
    }
    return this;
  }

  RepoConfigurator disableWiki() throws IOException {
    if (repo.hasWiki()) {
      log("disable wiki");
      if (!dry) {
        repo.enableWiki(false);
      }
    }
    return this;
  }

  RepoConfigurator disableIssues() throws IOException {
    if (repo.hasIssues()) {
      log("disable issues");
      if (!dry) {
        repo.enableIssueTracker(false);
      }
    }
    return this;
  }

  RepoConfigurator enableIssues() throws IOException {
    if (!repo.hasIssues()) {
      log("enable issues");
      if (!dry) {
        repo.enableIssueTracker(true);
      }
    }
    return this;
  }

  RepoConfigurator disableProjects() throws IOException {
    if (repo.hasProjects()) {
      log("disable projects");
      if (!dry) {
        repo.enableProjects(false);
      }
    }
    return this;
  }

  private boolean isProtected(GHBranch branch) throws IOException {
    if (branch.isProtected()) {
      var protection = branch.getProtection();
      var reviews = protection.getRequiredReviews();
      if (reviews == null) {
        log("protection of branch " + branch.getName() + " allows to merge without PR");
        if (!dry) {
          branch.disableProtection();
        }
        return false;
      }
      var reviewers = reviews.getRequiredReviewers();
      if (reviewers != 0) {
        log("protection of branch " + branch.getName() + " has not the correct amount of reviewers " + reviewers);
        if (!dry) {
          branch.disableProtection();
        }
        return false;
      }
      if (!protection.getEnforceAdmins().isEnabled()) {
        log("protection of branch " + branch.getName() + " does not enforce admins");
        if (!dry) {
          branch.disableProtection();
        }
        return false;
      }
      return true;
    }
    return false;
  }

  private void log(String log) {
    this.log.add(log);
  }
}
