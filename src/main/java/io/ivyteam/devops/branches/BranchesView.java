package io.ivyteam.devops.branches;

import java.util.Comparator;
import java.util.HashMap;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.settings.SettingsManager;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

  private static final String PREFIXES_KEY = "filter";
  private static final String SEARCH_KEY = "search";
  private String searchValue = "";
  private String excludedPrefixes = "";

  public BranchesView() {
    var repos = RepoRepository.INSTANCE.all();
    var branches = repos.stream().flatMap(r -> r.branches().stream())
        .sorted(Comparator.comparing(Branch::lastCommitAuthor).thenComparing(Branch::repository))
        .toList();

    var grid = BranchesGrid.create(branches);
    var layout = new VerticalLayout();
    layout.setHeightFull();

    UI.getCurrent().getPage().fetchCurrentURL(url -> {
      var queryParams = QueryParameters.fromString(url.getQuery());
      searchValue = queryParams.getSingleParameter(SEARCH_KEY).orElse("");
      excludedPrefixes = queryParams.getSingleParameter(PREFIXES_KEY)
          .orElseGet(() -> SettingsManager.INSTANCE.get().excludedBranchPrefixes());

      var inputLayout = userInput(grid.getListDataView());
      layout.add(inputLayout);
      layout.add(grid);
      setContent(layout);
    });
  }

  private Component userInput(GridListDataView<Branch> dataView) {
    var search = new TextField();
    search.setWidth("30%");
    search.setPlaceholder("Search");
    search.setValue(searchValue);
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.setValueChangeMode(ValueChangeMode.EAGER);
    search.addValueChangeListener(e -> {
      searchValue = e.getValue();
      updateQueryParams();
      dataView.refreshAll();
    });

    var excludedBranchPrefixes = new TextField();
    excludedBranchPrefixes.setWidth("70%");
    excludedBranchPrefixes.setHelperText("Branch prefixes to be excluded, e.g. master,release/,stale/");
    excludedBranchPrefixes.setValueChangeMode(ValueChangeMode.EAGER);
    excludedBranchPrefixes.addValueChangeListener(e -> {
      excludedPrefixes = e.getValue();
      updateQueryParams();
      dataView.refreshAll();
    });
    excludedBranchPrefixes.setValue(excludedPrefixes);
    dataView.addFilter(new SearchFilter(search));
    dataView.addFilter(new ExcludedBranchesFilter(excludedBranchPrefixes));
    var inputLayout = new HorizontalLayout();
    inputLayout.add(search, excludedBranchPrefixes);
    inputLayout.setWidthFull();
    return inputLayout;
  }

  private void updateQueryParams() {
    var baseUrl = RouteConfiguration.forSessionScope().getUrl(getClass());
    var params = new HashMap<String, String>();
    if (!searchValue.isEmpty()) {
      params.put(SEARCH_KEY, searchValue);
    }
    if (!excludedPrefixes.isEmpty()) {
      params.put(PREFIXES_KEY, excludedPrefixes);
    }
    var queryParams = params.isEmpty() ? QueryParameters.empty() : QueryParameters.simple(params);
    var updatedLocation = new Location(baseUrl, queryParams);
    UI.getCurrent().getPage().getHistory().replaceState(null, updatedLocation);
  }

  @Override
  public String title() {
    return "Branches";
  }
}
