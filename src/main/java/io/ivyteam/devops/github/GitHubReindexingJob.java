package io.ivyteam.devops.github;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.ivyteam.devops.github.GitHubSynchronizer.Progress;
import io.ivyteam.devops.repo.RepoRepository;

@Component
public class GitHubReindexingJob {

  private final static Logger LOGGER = LoggerFactory.getLogger(GitHubReindexingJob.class);

  @Autowired
  private GitHubSynchronizer synchronizer;

  @Autowired
  private RepoRepository repos;

  @Scheduled(cron = "0 11 0 * * ?")
  public void reindex() {
    Consumer<Progress> listener = progress -> LOGGER.info(progress.percent() * 100 + "%: " + progress.message());
    try {
      synchronizer.addListener(listener);
      synchronizer.run();
    } finally {
      synchronizer.removeListener(listener);
    }
    for (var repo : repos.all()) {
      var changed = new GitHubRepoConfigurator(repo).run();
      if (changed) {
        new GitHubSynchronizer().synch(repo);
      }
    }
  }
}
