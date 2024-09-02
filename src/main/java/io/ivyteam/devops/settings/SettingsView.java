package io.ivyteam.devops.settings;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.view.View;

@Route("/settings")
public class SettingsView extends View {

  private final Settings settings;

  private final PasswordField githubClientId;
  private final PasswordField githubClientSecret;
  private final TextField gitHubAppId;
  private final TextField gitHubAppInstallationId;
  private final TextField excludedBranchPrefixes;
  private final TextField branchProtectionPrefixes;

  public SettingsView() {
    this.settings = SettingsManager.INSTANCE.get();

    var div = new VerticalLayout();

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
}
