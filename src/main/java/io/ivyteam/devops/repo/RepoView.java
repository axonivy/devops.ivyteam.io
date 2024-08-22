package io.ivyteam.devops.repo;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.router.WildcardParameter;

import io.ivyteam.devops.branch.BranchGrid;
import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("/repository")
public class RepoView extends View implements HasUrlParameter<String> {

  @Autowired
  private RepoRepository repos;

  @Autowired
  private BranchRepository branches;

  @Autowired
  private PullRequestRepository pullRequests;

  @Override
  public String title() {
    return "Repository";
  }

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    var repo = repos.all().stream()
        .filter(r -> r.name().equals(parameter))
        .findAny()
        .orElseThrow();

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
    var gridBranches = BranchGrid.create(repoBranches);
    tabSheet.add(tabBranches, gridBranches);

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

  private TextArea createTextArea(String value) {
    var txt = new TextArea();
    txt.setValue(value == null ? "" : value);
    txt.setReadOnly(true);
    txt.getStyle().set("width", "100%");
    txt.setHeight("800px");
    return txt;
  }
}
