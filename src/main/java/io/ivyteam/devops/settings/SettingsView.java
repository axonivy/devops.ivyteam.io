package io.ivyteam.devops.settings;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
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

  private static Thread synchronizer;

  public SettingsView() {
    this.settings = SettingsManager.get();

    var div = new VerticalLayout();

    reindexButton = new Button("Reindex");
    var ui = UI.getCurrent();
    reindexButton.addClickListener(clickEvent -> {
      if (synchronizer != null) {
        return;
      }
      reindexButton.setEnabled(false);
      synchronizer = new Thread(() -> new GitHubSynchronizer(progress -> updateUi(ui, progress)).run());
      synchronizer.start();
    });

    githubOrganization = new TextField("GitHub Organization");
    githubOrganization.setValue(settings.gitHubOrg());
    githubOrganization.addValueChangeListener(event -> saveSettings());
    githubToken = new PasswordField("GitHub Token");
    githubToken.setValue(settings.gitHubToken());
    githubToken.addValueChangeListener(event -> saveSettings());

    var formLayout = new FormLayout();
    formLayout.add(githubOrganization, githubToken);
    formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
    div.setMaxWidth("50%");
    div.add(formLayout);

    progressBarLabelText = new NativeLabel();
    progressBarLabelText.setVisible(false);
    progressBar = new ProgressBar();
    progressBar.setVisible(false);

    Dialog dialog = new Dialog();
    dialog.setWidth("400px");

    dialog.setHeaderTitle("Reindex Github Repositories");

    VerticalLayout dialogLayout = new VerticalLayout();
    dialog.add(dialogLayout);
    dialogLayout.add(progressBarLabelText, progressBar);

    Button cancelButton = new Button("Cancel", e -> dialog.close());
    dialog.getFooter().add(cancelButton);
    dialog.getFooter().add(reindexButton);

    Button button = new Button("Reindex", e -> dialog.open());

    div.add(dialog, button);

    setContent(div);
  }

  private void saveSettings() {
    settings.gitHubOrg(githubOrganization.getValue());
    settings.gitHubToken(githubToken.getValue());
    SettingsManager.save(settings);
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
        reindexButton.setEnabled(true);
      }
    });
  }
}
