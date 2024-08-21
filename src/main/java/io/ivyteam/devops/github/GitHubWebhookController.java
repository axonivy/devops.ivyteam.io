package io.ivyteam.devops.github;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.PullRequest;
import io.ivyteam.devops.repo.RepoRepository;

@RestController
@RequestMapping("/github-webhook/")
@Conditional(WebhookCondition.class)
public class GitHubWebhookController {

    @GetMapping(produces = "text/plain")
    String get() {
        return "OK";
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=create")
    public ResponseEntity<Branch> createBranch(@RequestBody BranchBean bean) {
        if ("branch".equals(bean.ref_type)) {
            var branch = bean.toBranch();
            RepoRepository.INSTANCE.createBranch(branch);
            return ResponseEntity.ok().body(branch);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=delete")
    public ResponseEntity<Branch> deleteBranch(@RequestBody BranchBean bean) {
        if ("branch".equals(bean.ref_type)) {
            var branch = bean.toBranch();
            RepoRepository.INSTANCE.deleteBranch(branch);
            return ResponseEntity.ok().body(branch);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "X-GitHub-Event=pull_request")
    public ResponseEntity<PullRequest> createPR(@RequestBody PullRequestBean bean) {
        var pr = bean.toPullRequest();
        if ("opened".equals(bean.action)) {
            RepoRepository.INSTANCE.createPr(pr);
            return ResponseEntity.ok().body(pr);
        }
        if ("closed".equals(bean.action)) {
            RepoRepository.INSTANCE.deletePr(pr);
            return ResponseEntity.ok().body(pr);
        }
        return ResponseEntity.notFound().build();
    }

    record BranchBean(String ref, String ref_type, Repository repository, User sender) {
        Branch toBranch() {
            return new Branch(this.repository.full_name, this.ref, this.sender.login);
        }
    }

    record Repository(String full_name) {
    }

    record User(String login) {

    }

    record PullRequestBean(String action, PrDetail pull_request, Repository repository) {
        PullRequest toPullRequest() {
            return new PullRequest(this.repository.full_name, this.pull_request.number, this.pull_request.title,
                    this.pull_request.user.login);
        }
    }

    record PrDetail(long number, String title, User user) {

    }
}
