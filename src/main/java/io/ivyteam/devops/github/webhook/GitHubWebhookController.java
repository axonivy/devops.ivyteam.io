package io.ivyteam.devops.github.webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequest;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.settings.SettingsManager;

@RestController
@RequestMapping("/github-webhook/")
public class GitHubWebhookController {

  @Autowired
  BranchRepository branches;

  @Autowired
  PullRequestRepository prs;

  @GetMapping(produces = "text/plain")
  String get() {
    return "OK";
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=create")
  public ResponseEntity<Branch> createBranch(@RequestBody BranchBean bean) {
    validateBean(bean);
    if ("branch".equals(bean.ref_type)) {
      var branch = bean.toBranch();
      branches.create(branch);
      return ResponseEntity.ok().body(branch);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=delete")
  public ResponseEntity<Branch> deleteBranch(@RequestBody BranchBean bean) {
    validateBean(bean);
    if ("branch".equals(bean.ref_type)) {
      var branch = bean.toBranch();
      branches.delete(branch);
      return ResponseEntity.ok().body(branch);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=pull_request")
  public ResponseEntity<PullRequest> createPR(@RequestBody PullRequestBean bean) {
    validateBean(bean.organization);
    var pr = bean.toPullRequest();
    if ("opened".equals(bean.action)) {
      prs.create(pr);
      return ResponseEntity.ok().body(pr);
    }
    if ("closed".equals(bean.action)) {
      prs.delete(pr);
      return ResponseEntity.ok().body(pr);
    }
    return ResponseEntity.notFound().build();
  }

  private void validateBean(Object bean) {
    switch (bean) {
      case BranchBean b -> validateOrg(b.organization);
      case PullRequestBean pr -> validateOrg(pr.organization);
      default -> throw new RuntimeException("Invalid request body provided: " + bean);
    }
  }

  private void validateOrg(Organization org) {
    if (org == null || org.login == null || !org.login.equals(SettingsManager.INSTANCE.get().gitHubOrg())) {
      throw new RuntimeException("Invalid organization provided: " + org);
    }
  }

  record BranchBean(
      String ref,
      String ref_type,
      Repository repository,
      User sender,
      Organization organization) {

    Branch toBranch() {
      return new Branch(this.repository.full_name, this.ref, this.sender.login);
    }
  }

  record PullRequestBean(
      String action,
      PrDetail pull_request,
      Repository repository,
      Organization organization) {

    PullRequest toPullRequest() {
      return new PullRequest(this.repository.full_name, this.pull_request.number, this.pull_request.title,
          this.pull_request.user.login);
    }
  }

  record Organization(String login) {
  }

  record Repository(String full_name) {
  }

  record User(String login) {

  }

  record PrDetail(long number, String title, User user) {

  }
}
