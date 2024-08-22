package io.ivyteam.devops.branches;

import java.util.Comparator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View implements BeforeEnterObserver {

  private static final String LOCAL_STORAGE_KEY = "excludedBranchPrefixes";
  private static final String SEARCH_KEY = "search";
  private String searchValue = "";

  public BranchesView() {
    var repos = RepoRepository.INSTANCE.all();
    var branches = repos.stream().flatMap(r -> r.branches().stream())
        .sorted(Comparator.comparing(Branch::lastCommitAuthor).thenComparing(Branch::repository))
        .toList();

    var grid = BranchesGrid.create(branches);

    WebStorage.getItem(LOCAL_STORAGE_KEY, excludedPrefixes -> {
      if (excludedPrefixes == null) {
        excludedPrefixes = "master,release/,stale/,dependabot/,gh-pages,dev10.0,dev11.1";
      }
      var inputLayout = userInput(excludedPrefixes, grid.getListDataView());
      var layout = new VerticalLayout();
      layout.setHeightFull();
      layout.add(inputLayout);
      layout.add(grid);
      setContent(layout);
    });
  }

  private Component userInput(String excludedPrefixes, GridListDataView<Branch> dataView) {
    var search = new TextField();
    search.setWidth("30%");
    search.setPlaceholder("Search");
    search.setValue(searchValue);
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.setValueChangeMode(ValueChangeMode.EAGER);
    search.addValueChangeListener(e -> {
      dataView.refreshAll();
      var baseUrl = RouteConfiguration.forSessionScope().getUrl(getClass());
      var queryParams = e.getValue().isEmpty() ? QueryParameters.empty() : QueryParameters.of(SEARCH_KEY, e.getValue());
      var updatedLocation = new Location(baseUrl, queryParams);
      UI.getCurrent().getPage().getHistory().replaceState(null, updatedLocation);
    });

    var excludedBranchPrefixes = new TextField();
    excludedBranchPrefixes.setWidth("70%");
    excludedBranchPrefixes.setHelperText("Branch prefixes to be excluded, e.g. master,release/,stale/");
    excludedBranchPrefixes.setValueChangeMode(ValueChangeMode.EAGER);
    excludedBranchPrefixes.addValueChangeListener(e -> {
      dataView.refreshAll();
      WebStorage.setItem(LOCAL_STORAGE_KEY, e.getValue());
    });
    excludedBranchPrefixes.setValue(excludedPrefixes);
    dataView.addFilter(new SearchFilter(search));
    dataView.addFilter(new ExcludedBranchesFilter(excludedBranchPrefixes));
    var inputLayout = new HorizontalLayout();
    inputLayout.setWidthFull();
    inputLayout.add(search);
    inputLayout.add(excludedBranchPrefixes);
    return inputLayout;
  }

  @Override
  public String title() {
    return "Branches";
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    searchValue = event.getLocation().getQueryParameters().getSingleParameter(SEARCH_KEY).orElse("");
  }
}
