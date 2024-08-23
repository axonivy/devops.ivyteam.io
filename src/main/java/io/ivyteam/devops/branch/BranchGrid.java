package io.ivyteam.devops.branch;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;

import io.ivyteam.devops.settings.SettingsManager;
import io.ivyteam.devops.user.User;

public class BranchGrid {

  private static final String PREFIXES_KEY = "filter";
  private static final String SEARCH_KEY = "search";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

  private final List<Branch> branches;
  private final Class<? extends Component> navigationTarget;
  private final RouteParameters routeParameters;
  private String searchValue = "";
  private String excludedPrefixes = "";

  public BranchGrid(List<Branch> branches, Class<? extends Component> navigationTarget,
      RouteParameters routeParameters) {
    this.branches = branches;
    this.navigationTarget = navigationTarget;
    this.routeParameters = routeParameters;
  }

  public Component create() {
    var grid = new Grid<>(branches);
    grid.setSizeFull();

    grid.addColumn(
        new ComponentRenderer<>(
            branch -> new Anchor(new User(branch.lastCommitAuthor()).link(), branch.lastCommitAuthor())))
        .setHeader("Author")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::lastCommitAuthor));

    grid.addColumn(new ComponentRenderer<>(branch -> new Anchor(branch.repoLink(), branch.repository())))
        .setHeader("Repository")
        .setWidth("20%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::repository));

    grid
        .addColumn(new ComponentRenderer<>(branch -> new Anchor(branch.ghLink(), branch.name())))
        .setHeader("Name")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::name))
        .setWidth("40%");

    grid
        .addColumn(branch -> DATE_FORMAT.format(branch.authoredDate()))
        .setHeader("Updated")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::authoredDate));

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
    });

    return layout;
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
    var baseUrl = RouteConfiguration.forSessionScope().getUrl(navigationTarget, routeParameters);
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
}
