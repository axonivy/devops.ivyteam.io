package io.ivyteam.devops.repo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;

import io.ivyteam.devops.branches.BranchesGrid;
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

    var tabPrs = new Tab("Pull Requests");
    var gridPrs = createGridPullRequests(repo);
    tabSheet.add(tabPrs, gridPrs);

    var tabBranches = new Tab("Branches");
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
    txt.setValue(value);
    txt.setReadOnly(true);
    txt.getStyle().set("width", "100%");
    txt.setHeight("800px");
    return txt;
  }

  private Grid<PullRequest> createGridPullRequests(Repo repo) {
    var grid = new Grid<PullRequest>();
    grid.setItems(repo.prs());
    grid.addColumn(LitRenderer.<PullRequest>of("""
           <a href="${item.link}">${item.title}</a>
        """)
        .withProperty("link", pr -> pr.ghLink())
        .withProperty("title", pr -> pr.title()))
        .setHeader("Title")
        .setWidth("70%")
        .setSortable(true);

    grid
        .addColumn(PullRequest::user)
        .setHeader("User")
        .setWidth("30%")
        .setSortable(true);
    grid.setSizeFull();
    return grid;
  }
}
