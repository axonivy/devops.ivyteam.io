package io.ivyteam.devops.repo;

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

import io.ivyteam.devops.branches.BranchesGrid;
import io.ivyteam.devops.pullrequest.PullRequestGrid;
import io.ivyteam.devops.view.View;

@Route("/repository")
public class RepoView extends View implements HasUrlParameter<String> {

  @Override
  public String title() {
    return "Repository";
  }

  @Override
  public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
    var repo = RepoRepository.INSTANCE.all().stream()
        .filter(r -> r.name().equals(parameter))
        .findAny()
        .orElseThrow();

    var tabSheet = new TabSheet();

    var tabDetail = new Tab("Details");
    tabSheet.add(tabDetail, createDetail(repo));

    var prCounter = new Span("Pull Requests (" + repo.prs().size() + ")");
    var tabPrs = new Tab(prCounter);
    var gridPrs = PullRequestGrid.create(repo.prs());
    tabSheet.add(tabPrs, gridPrs);

    var branchCounter = new Span("Branches (" + repo.branches().size() + ")");
    var tabBranches = new Tab(branchCounter);
    var gridBranches = BranchesGrid.create(repo.branches());
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
