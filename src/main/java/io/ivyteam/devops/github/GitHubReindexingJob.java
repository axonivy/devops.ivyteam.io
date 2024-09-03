package io.ivyteam.devops.github;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.ivyteam.devops.github.GitHubSynchronizer.Progress;

@Component
public class GitHubReindexingJob {

  private final static Logger LOGGER = LoggerFactory.getLogger(GitHubReindexingJob.class);

  @Autowired
  private GitHubSynchronizer synchronizer;

  @Scheduled(cron = "0 11 0 * * ?")
  public void reindex() {
    Consumer<Progress> listener = this::log;
    try {
      synchronizer.addListener(listener);
      synchronizer.run();
    } finally {
      synchronizer.removeListener(listener);
    }
  }

  private void log(Progress progress) {
    var percent = ((int) (progress.percent() * 100));
    LOGGER.info(percent + "%: " + progress.message());
  }
}
