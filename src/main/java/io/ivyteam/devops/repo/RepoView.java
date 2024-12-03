package io.ivyteam.devops.repo;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.router.internal.HasUrlParameterFormat;

import io.ivyteam.devops.branch.BranchGrid;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.github.GitHubProvider;
import io.ivyteam.devops.github.GitHubRepoConfigurator;
import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("/repository")
public class RepoView extends View implements HasUrlParameter<String> {

  @Autowired
  private GitHubProvider gitHub;

  @Autowired
  private RepoRepository repos;

  @Autowired
  private BranchRepository branches;

  @Autowired
  private PullRequestRepository pullRequests;

  @Autowired
  private GitHubSynchronizer synchronizer;

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    var repo = repos.all().stream()
        .filter(r -> r.name().equals(parameter))
        .findAny()
        .orElseThrow();

    title.setText(repo.name());

    var tabSheet = new TabSheet();

    var tabDetail = new Tab("Details");
    tabSheet.add(tabDetail, createDetail(repo));

    var repoPrs = pullRequests.findByRepository(repo.name());
    var prCounter = new Span("Pull Requests (" + repoPrs.size() + ")");
    var tabPrs = new Tab(prCounter);
    var gridPrs = PullRequestGrid.create(repoPrs);
    tabSheet.add(tabPrs, gridPrs);

    var repoBranches = branches.findByRepo(repo.name());
    var branchCounter = new Span("Branches (" + repoBranches.size() + ")");
    var tabBranches = new Tab(branchCounter);
    var routeParameters = new RouteParameters(HasUrlParameterFormat.PARAMETER_NAME, parameter);
    var gridBranches = new BranchGrid(repoBranches, pullRequests, RepoView.class, routeParameters).create();
    tabSheet.add(tabBranches, gridBranches);

    var tabJobs = new Tab("Jobs");
    var jobs = createJobs(repo);
    tabSheet.add(tabJobs, jobs);

    tabSheet.setSizeFull();
    setContent(tabSheet);
  }

  private Component createDetail(Repo repo) {
    var formLayout = new FormLayout();
    formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));

    var txtName = new TextField("Name");
    txtName.setValue(repo.name());
    txtName.setReadOnly(true);
    formLayout.add(txtName);

    var chkArchived = new Checkbox("Archived");
    chkArchived.setValue(repo.archived());
    chkArchived.setReadOnly(true);
    formLayout.add(chkArchived);

    var chkPrivate = new Checkbox("Private");
    chkPrivate.setValue(repo.privateRepo());
    chkPrivate.setReadOnly(true);
    formLayout.add(chkPrivate);

    var chkFork = new Checkbox("Fork");
    chkFork.setValue(repo.fork());
    chkFork.setReadOnly(true);
    formLayout.add(chkFork);

    var chkIsVulnAlertOn = new Checkbox("Enable vulnerability alerts");
    chkIsVulnAlertOn.setValue(repo.isVulnAlertOn());
    chkIsVulnAlertOn.setReadOnly(true);
    formLayout.add(chkIsVulnAlertOn);

    var chkDeleteBranchOnMerge = new Checkbox("Delete branch on merge");
    chkDeleteBranchOnMerge.setValue(repo.deleteBranchOnMerge());
    chkDeleteBranchOnMerge.setReadOnly(true);
    formLayout.add(chkDeleteBranchOnMerge);

    var chkProjects = new Checkbox("Projects");
    chkProjects.setValue(repo.projects());
    chkProjects.setReadOnly(true);
    formLayout.add(chkProjects);

    var chkIssues = new Checkbox("Issues");
    chkIssues.setValue(repo.issues());
    chkIssues.setReadOnly(true);
    formLayout.add(chkIssues);

    var chkWiki = new Checkbox("Wiki");
    chkWiki.setValue(repo.wiki());
    chkWiki.setReadOnly(true);
    formLayout.add(chkWiki);

    var chkHooks = new Checkbox("Hooks");
    chkHooks.setValue(repo.hooks());
    chkHooks.setReadOnly(true);
    formLayout.add(chkHooks);

    var tabSheet = new TabSheet();

    var tabLicense = new Tab("LICENSE");
    tabSheet.add(tabLicense, createTextArea(repo.license()));

    var tabSecurityMd = new Tab("SECURITY.md");
    tabSheet.add(tabSecurityMd, createTextArea(repo.securityMd()));

    var tabCodeOfConduct = new Tab("CODE_OF_CONDUCT.md");
    tabSheet.add(tabCodeOfConduct, createTextArea(repo.codeOfConduct()));

    tabSheet.setSizeFull();
    formLayout.add(tabSheet);

    return formLayout;
  }

  private Component createJobs(Repo repo) {
    var formLayout = new FormLayout();
    formLayout.setResponsiveSteps(new ResponsiveStep("0", 2));

    var btnSynch = new Button("Reindex");
    btnSynch.addClickListener(event -> synch(repo));
    btnSynch.setMaxWidth("150px");
    formLayout.add(btnSynch);

    var btnUpdateSettings = new Button("Update Settings");
    btnUpdateSettings.addClickListener(event -> updateSettings(repo));
    btnUpdateSettings.setMaxWidth("150px");
    formLayout.add(btnUpdateSettings);

    return formLayout;
  }

  private TextArea createTextArea(String value) {
    var txt = new TextArea();
    txt.setValue(value == null ? "" : value);
    txt.setReadOnly(true);
    txt.getStyle().set("width", "100%");
    txt.setHeight("600px");
    return txt;
  }

  private void synch(Repo repo) {
    try {
      var ghRepo = gitHub.get().getRepository(repo.name());
      synchronizer.synch(ghRepo);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateSettings(Repo repo) {
    new GitHubRepoConfigurator(gitHub, branches, repo).run();
    synch(repo);
  }
}
