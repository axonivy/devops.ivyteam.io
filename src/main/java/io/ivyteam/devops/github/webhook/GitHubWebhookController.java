package io.ivyteam.devops.github.webhook;

import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import io.ivyteam.devops.branch.Branch;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequest;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;

@RestController
@RequestMapping(GitHubWebhookController.PATH)
@AnonymousAllowed
public class GitHubWebhookController {

  public static final String PATH = "/github-webhook/";

  @Autowired
  RepoRepository repos;

  @Autowired
  BranchRepository branches;

  @Autowired
  PullRequestRepository prs;

  @GetMapping(produces = "text/plain")
  String get() {
    return "OK";
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=push")
  public ResponseEntity<String> push(@RequestBody PushBean bean) {
    if (bean.deleted) {
      return ResponseEntity.ok().body("DELETED");
    }
    var branch = bean.toBranch();
    if (!repos.exist(bean.repository.full_name)) {
      repos.create(Repo.create().name(bean.repository.full_name).build());
    }
    branches.create(branch);
    return ResponseEntity.ok().body("CREATED");
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=delete")
  public ResponseEntity<BranchBean> deleteBranch(@RequestBody BranchBean bean) {
    if ("branch".equals(bean.ref_type)) {
      branches.delete(bean.repo(), bean.name());
      return ResponseEntity.ok().body(bean);
    }
    throw new RuntimeException("ref type not supported: " + bean.ref_type);
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=pull_request")
  public ResponseEntity<PullRequest> pr(@RequestBody PullRequestBean bean) {
    var pr = bean.toPullRequest();
    if ("opened".equals(bean.action)) {
      prs.create(pr);
      return ResponseEntity.ok().body(pr);
    }
    if ("closed".equals(bean.action)) {
      prs.delete(pr);
      return ResponseEntity.ok().body(pr);
    }
    return ResponseEntity.noContent().build();
  }

  private static Date tsToDate(String timestamp) {
    var instant = ZonedDateTime.parse(timestamp).toInstant();
    return Date.from(instant);
  }

  record PushBean(
      String ref,
      Repository repository,
      Commit head_commit,
      boolean deleted) {

    Branch toBranch() {
      return Branch.create()
          .repository(this.repository.full_name)
          .name(ref.replace("refs/heads/", ""))
          .lastCommitAuthor(this.head_commit.author.username)
          .protectedBranch(false)
          .authoredDate(tsToDate(this.head_commit.timestamp))
          .build();
    }
  }

  record BranchBean(
      String ref,
      String ref_type,
      Repository repository,
      User sender,
      String updated_at) {

    String repo() {
      return repository.full_name;
    }

    String name() {
      return ref;
    }
  }

  record PullRequestBean(
      String action,
      PrDetail pull_request,
      Repository repository) {

    PullRequest toPullRequest() {
      return PullRequest.create()
          .repository(this.repository.full_name)
          .id(this.pull_request.number)
          .title(this.pull_request.title)
          .user(this.pull_request.user.login)
          .branchName(this.pull_request.head.ref)
          .build();
    }
  }

  record Commit(String timestamp, Author author) {
  }

  record Author(String username) {
  }

  record Repository(String full_name) {
  }

  record User(String login) {
  }

  record PrDetail(long number, String title, User user, Head head) {
  }

  record Head(String ref) {
  }
}
