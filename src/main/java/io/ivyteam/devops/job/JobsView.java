package io.ivyteam.devops.job;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.github.GitHubSynchronizer.Progress;
import io.ivyteam.devops.view.View;

@Route("/jobs")
public class JobsView extends View {

  private final Button reindexButton;
  private final NativeLabel progressBarLabelText;
  private final ProgressBar progressBar;
  private static Thread synchronizer;

  public JobsView(GitHubSynchronizer syncher) {
    title.setText("Jobs");
    var div = new VerticalLayout();

    reindexButton = new Button("Reindex");
    var ui = UI.getCurrent();

    reindexButton.setVisible(!syncher.isRunning());

    if (syncher.isRunning()) {
      updateUi(ui, syncher.getProgress());
      syncher.addListener(progress -> updateUi(ui, progress));
    }

    reindexButton.addClickListener(clickEvent -> {
      reindexButton.setVisible(false);
      syncher.addListener(progress -> updateUi(ui, progress));
      synchronizer = new Thread(() -> syncher.run());
      synchronizer.start();
    });

    progressBarLabelText = new NativeLabel();
    progressBarLabelText.setVisible(syncher.isRunning());
    progressBar = new ProgressBar();
    progressBar.setVisible(syncher.isRunning());

    div.add(reindexButton, progressBarLabelText, progressBar);

    setContent(div);
  }

  private void updateUi(UI ui, Progress progress) {
    ui.access(() -> {
      progressBarLabelText.setVisible(true);
      progressBar.setVisible(true);
      progressBarLabelText.setText(progress.message());
      progressBar.setValue(progress.percent());

      if (progress.percent() == 1) {
        reindexButton.setVisible(true);
        progressBar.setVisible(false);
        progressBarLabelText.setVisible(false);
      }
    });
  }
}
