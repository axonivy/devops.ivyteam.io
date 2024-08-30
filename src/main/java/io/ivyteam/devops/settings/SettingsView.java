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

  private final PasswordField githubClientId;
  private final PasswordField githubClientSecret;
  private final TextField gitHubAppId;
  private final TextField gitHubAppInstallationId;
  private final TextField excludedBranchPrefixes;
  private final TextField branchProtectionPrefixes;

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

    githubClientId = new PasswordField("GitHub Client ID");
    githubClientId.setValue(settings.gitHubClientId());
    githubClientId.addValueChangeListener(event -> saveSettings());

    githubClientSecret = new PasswordField("GitHub Client Secret");
    githubClientSecret.setValue(settings.gitHubClientSecret());
    githubClientSecret.addValueChangeListener(event -> saveSettings());

    gitHubAppId = new TextField("GitHub Application ID");
    gitHubAppId.setValue(settings.gitHubAppId());
    gitHubAppId.addValueChangeListener(event -> saveSettings());

    gitHubAppInstallationId = new TextField("GitHub Application Installation ID");
    gitHubAppInstallationId.setValue(settings.gitHubAppInstallationId());
    gitHubAppInstallationId.addValueChangeListener(event -> saveSettings());

    excludedBranchPrefixes = new TextField("Excluded branch prefixes");
    excludedBranchPrefixes.setValue(settings.excludedBranchPrefixes());
    excludedBranchPrefixes.addValueChangeListener(event -> saveSettings());

    branchProtectionPrefixes = new TextField("Branch protection prefixes");
    branchProtectionPrefixes.setValue(settings.branchProtectionPrefixes());
    branchProtectionPrefixes.addValueChangeListener(event -> saveSettings());

    var formLayout = new FormLayout();
    formLayout.add(githubClientId, githubClientSecret, gitHubAppId, gitHubAppInstallationId,
        excludedBranchPrefixes,
        branchProtectionPrefixes);
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
    settings.gitHubClientId(githubClientId.getValue());
    settings.gitHubClientSecret(githubClientSecret.getValue());
    settings.gitHubAppId(gitHubAppId.getValue());
    settings.gitHubAppInstallationId(gitHubAppInstallationId.getValue());
    settings.excludedBranchPrefixes(excludedBranchPrefixes.getValue());
    settings.branchProtectionPrefixes(branchProtectionPrefixes.getValue());
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
