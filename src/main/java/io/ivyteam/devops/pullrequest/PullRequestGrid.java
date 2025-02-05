package io.ivyteam.devops.pullrequest;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;

import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.ReposView;
import io.ivyteam.devops.user.User;
import io.ivyteam.devops.user.UserView;

public class PullRequestGrid {

  public static Component create(List<PullRequest> prs, Consumer<GridListDataView<?>> update) {
    var grid = new Grid<>(prs);
    grid.setSizeFull();

    grid
        .addColumn(pr -> pr.id())
        .setHeader("ID")
        .setWidth("5%")
        .setSortable(true);

    grid
        .addComponentColumn(PullRequestGrid::prLink)
        .setHeader("Title")
        .setWidth("40%")
        .setSortable(true)
        .setComparator(Comparator.comparing(PullRequest::title));

    grid
        .addComponentColumn(p -> ReposView.repoLink(Repo.create().name(p.repository()).build()))
        .setHeader("Repo")
        .setWidth("30%")
        .setSortable(true)
        .setComparator(Comparator.comparing(pr -> pr.repository()));

    grid
        .addComponentColumn(pr -> UserView.userLink(new User(pr.user(), "")))
        .setHeader("User")
        .setWidth("20%")
        .setSortable(true)
        .setComparator(Comparator.comparing(PullRequest::user));

    var searchFilter = searchFilter(grid.getListDataView(), update);
    var layout = new VerticalLayout();
    layout.setSizeFull();
    layout.add(searchFilter);
    layout.add(grid);
    update.accept(grid.getListDataView());
    return layout;
  }

  private static Component searchFilter(GridListDataView<PullRequest> dataView, Consumer<GridListDataView<?>> update) {
    var search = new TextField();
    search.setWidth("100%");
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.setValueChangeMode(ValueChangeMode.EAGER);
    search.addValueChangeListener(e -> dataView.refreshAll());
    search.addValueChangeListener(e -> update.accept(dataView));
    dataView.addFilter(new SearchFilter(search));

    var inputLayout = new HorizontalLayout();
    inputLayout.setWidthFull();
    inputLayout.add(search);
    return inputLayout;
  }

  private static Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "0.25em");
    return icon;
  }

  private static class SearchFilter implements SerializablePredicate<PullRequest> {

    private final TextField search;

    SearchFilter(TextField search) {
      this.search = search;
    }

    @Override
    public boolean test(PullRequest pr) {
      var searchValue = search.getValue().trim().toLowerCase();
      if (searchValue.isEmpty()) {
        return true;
      }
      var matchesName = pr.title().toLowerCase().contains(searchValue);
      var matchesRepo = pr.repository().toLowerCase().contains(searchValue);
      var matchesUser = pr.user().toLowerCase().contains(searchValue);
      return matchesName || matchesRepo || matchesUser;
    }
  }

  public static Component prLink(PullRequest pr) {
    var icon = createIcon(VaadinIcon.EXTERNAL_LINK);
    var layout = new HorizontalLayout();
    layout.add(pr.title());
    layout.add(new Anchor(pr.ghLink(), icon));
    layout.setSpacing(false);
    layout.setAlignItems(Alignment.CENTER);
    return layout;
  }
}
