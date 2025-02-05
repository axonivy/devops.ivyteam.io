package io.ivyteam.devops.branch;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;

import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.ReposView;
import io.ivyteam.devops.settings.SettingsManager;
import io.ivyteam.devops.user.User;
import io.ivyteam.devops.user.UserView;

public class BranchGrid {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final String PREFIXES_KEY = "filter";
  private static final String SEARCH_KEY = "search";

  private final List<Branch> branches;
  private final Class<? extends Component> navigationTarget;
  private final RouteParameters routeParameters;
  private final Consumer<GridListDataView<?>> update;
  private String searchValue = "";
  private String excludedPrefixes = "";
  private PullRequestRepository prRepo;

  public BranchGrid(List<Branch> branches, PullRequestRepository prRepo, Class<? extends Component> navigationTarget,
      RouteParameters routeParameters, Consumer<GridListDataView<?>> update) {
    this.branches = branches;
    this.prRepo = prRepo;
    this.navigationTarget = navigationTarget;
    this.routeParameters = routeParameters;
    this.update = update;
  }

  public Component create() {
    var prs = prRepo.all().stream()
        .collect(Collectors.toMap(key -> key.repository() + key.branchName(), value -> value));
    var grid = new Grid<Branch>(branches);
    grid.setSizeFull();

    grid.addComponentColumn(branch -> UserView.userLink(new User(branch.lastCommitAuthor(), "")))
        .setHeader("Author")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::lastCommitAuthor));

    grid.addComponentColumn(branch -> ReposView.repoLink(Repo.create().name(branch.repository()).build()))
        .setHeader("Repository")
        .setWidth("20%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::repository));

    grid.addComponentColumn(branch -> new Anchor(branch.ghLink(), branch.name()))
        .setHeader("Branch")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::name))
        .setWidth("30%");

    grid
        .addComponentColumn(branch -> {
          var pr = prs.get(branch.repository() + branch.name());
          if (pr == null) {
            return null;
          }
          return new Anchor(pr.ghLink(), "#" + pr.id());
        })
        .setHeader("PR")
        .setWidth("5%");

    grid
        .addColumn(branch -> DATE_FORMAT.format(branch.authoredDate()))
        .setHeader("Updated")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Branch::authoredDate));

    grid
        .addComponentColumn(branch -> {
          if (branch.protectedBranch()) {
            var icon = createIcon(VaadinIcon.LOCK);
            icon.setTooltipText("Protected");
            return icon;
          }
          return null;
        })
        .setWidth("5%");

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
    update.accept(grid.getListDataView());
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
    search.addValueChangeListener(e -> update.accept(dataView));

    var excludedBranchPrefixes = new TextField();
    excludedBranchPrefixes.setWidth("70%");
    excludedBranchPrefixes.setHelperText("Branch prefixes to be excluded, e.g. master,release/,stale/");
    excludedBranchPrefixes.setValueChangeMode(ValueChangeMode.EAGER);
    excludedBranchPrefixes.addValueChangeListener(e -> {
      excludedPrefixes = e.getValue();
      updateQueryParams();
      dataView.refreshAll();
    });
    excludedBranchPrefixes.addValueChangeListener(e -> update.accept(dataView));
    excludedBranchPrefixes.setValue(excludedPrefixes);
    dataView.addFilter(new SearchFilter(search));
    dataView.addFilter(new ExcludedBranchesFilter(excludedBranchPrefixes));
    var inputLayout = new HorizontalLayout();
    inputLayout.add(search, excludedBranchPrefixes);
    inputLayout.setWidthFull();
    return inputLayout;
  }

  private Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "var(--lumo-space-xs");
    return icon;
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
