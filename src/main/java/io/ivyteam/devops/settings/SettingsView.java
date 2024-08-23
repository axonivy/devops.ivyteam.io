package io.ivyteam.devops.settings;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.github.GitHubSynchronizer.Progress;
import io.ivyteam.devops.view.View;

@Route("/settings")
public class SettingsView extends View {

  private final Settings settings;

  private final Button reindexButton;
  private final NativeLabel progressBarLabelText;
  private final ProgressBar progressBar;

  private final TextField githubOrganization;
  private final PasswordField githubToken;
  private final TextField excludedBranchPrefixes;

  private static Thread synchronizer;

  public SettingsView(GitHubSynchronizer syncher) {
    this.settings = SettingsManager.INSTANCE.get();

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

    githubOrganization = new TextField("GitHub Organization");
    githubOrganization.setValue(settings.gitHubOrg());
    githubOrganization.addValueChangeListener(event -> saveSettings());
    githubToken = new PasswordField("GitHub Token");
    githubToken.setValue(settings.gitHubToken());
    githubToken.addValueChangeListener(event -> saveSettings());
    excludedBranchPrefixes = new TextField("Excluded branch prefixes");
    excludedBranchPrefixes.setValue(settings.excludedBranchPrefixes());
    excludedBranchPrefixes.addValueChangeListener(event -> saveSettings());

    var formLayout = new FormLayout();
    formLayout.add(githubOrganization, githubToken, excludedBranchPrefixes);
    formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
    div.setMaxWidth("50%");
    div.add(formLayout);

    progressBarLabelText = new NativeLabel();
    progressBarLabelText.setVisible(syncher.isRunning());
    progressBar = new ProgressBar();
    progressBar.setVisible(syncher.isRunning());

    div.add(reindexButton, progressBarLabelText, progressBar);

    setContent(div);
  }

  private void saveSettings() {
    settings.gitHubOrg(githubOrganization.getValue());
    settings.gitHubToken(githubToken.getValue());
    settings.excludedBranchPrefixes(excludedBranchPrefixes.getValue());
    SettingsManager.INSTANCE.save(settings);
  }

  @Override
  public String title() {
    return "Settings";
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
