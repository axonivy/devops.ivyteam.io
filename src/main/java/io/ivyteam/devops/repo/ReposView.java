package io.ivyteam.devops.repo;

import java.io.IOException;
import java.util.Comparator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.github.GitHubProvider;
import io.ivyteam.devops.github.GitHubRepoConfigurator;
import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.view.View;

@Route("")
public class ReposView extends View {

  private final Grid<Repo> grid;

  private GitHubSynchronizer synchronizer;

  public ReposView(RepoRepository repos, PullRequestRepository prs, BranchRepository branches,
      GitHubSynchronizer synchronizer) {
    var repositories = repos.all();
    grid = new Grid<>(repositories);
    title.setText("Repositories (" + repositories.size() + ")");

    grid.addColumn(new ComponentRenderer<>(p -> new Anchor(p.link(), p.name())))
        .setHeader("Name")
        .setWidth("40%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Repo::name));

    var prCount = prs.countByRepo();
    grid
        .addComponentColumn(repo -> {
          var counter = new Span(String.valueOf(prCount.getOrDefault(repo.name(), 0L)));
          counter.getElement().getThemeList().add("badge pill small contrast");
          counter.getStyle().set("margin-inline-start", "var(--lumo-space-s)");
          return counter;
        })
        .setHeader("PRs")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(repo -> prCount.getOrDefault(repo.name(), 0L)));

    var branchCount = branches.countByRepo();
    grid
        .addComponentColumn(repo -> {
          var counter = new Span(String.valueOf(branchCount.getOrDefault(repo.name(), 0L)));
          counter.getElement().getThemeList().add("badge pill small contrast");
          counter.getStyle().set("margin-inline-start", "var(--lumo-space-s)");
          return counter;
        })
        .setHeader("Branches")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(repo -> branchCount.getOrDefault(repo.name(), 0L)));

    grid
        .addComponentColumn(repo -> {
          if (repo.archived() || repo.privateRepo()) {
            return null;
          }
          if (repo.license() != null) {
            var icon = createIcon(VaadinIcon.CHECK);
            icon.getElement().getThemeList().add("badge success");
            return icon;
          }
          var icon = createIcon(VaadinIcon.HAMMER);
          icon.getElement().getThemeList().add("badge error");
          return icon;
        })
        .setHeader("License")
        .setWidth("10%")
        .setSortable(true);

    grid.addComponentColumn(repo -> {
      if (repo.archived()) {
        return null;
      }

      if (repo.settingsLog().isEmpty()) {
        var confirmed = createIcon(VaadinIcon.CHECK);
        confirmed.getElement().getThemeList().add("badge success");
        return confirmed;
      }
      var play = createIcon(VaadinIcon.PLAY);
      play.getElement().getThemeList().add("badge");
      play.setTooltipText(repo.settingsLog());
      play.addClickListener(event -> updateSettings(repo));
      play.setColor("blue");
      return play;
    })
        .setHeader("Settings")
        .setWidth("10%");

    grid.addComponentColumn(repo -> {
      var play = createIcon(VaadinIcon.PLAY);
      play.getElement().getThemeList().add("badge");
      play.setTooltipText(repo.settingsLog());
      play.addClickListener(event -> synch(repo));
      play.setColor("blue");
      return play;
    })
        .setHeader("Synch")
        .setWidth("10%");

    grid
        .addComponentColumn(repo -> {
          var layout = new HorizontalLayout();
          if (repo.archived()) {
            var icon = createIcon(VaadinIcon.ARCHIVE);
            layout.add(icon);
          }
          if (repo.privateRepo()) {
            var icon = createIcon(VaadinIcon.LOCK);
            layout.add(icon);
          }
          return layout;
        })
        .setWidth("10%");

    grid.setHeightFull();

    var inputLayout = userInput(grid.getListDataView());
    var layout = new VerticalLayout();
    layout.setHeightFull();
    layout.add(inputLayout);
    layout.add(grid);
    setContent(layout);
  }

  private Component userInput(GridListDataView<Repo> dataView) {
    var search = new TextField();
    search.setWidth("80%");
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.setValueChangeMode(ValueChangeMode.EAGER);
    search.addValueChangeListener(e -> dataView.refreshAll());
    dataView.addFilter(new SearchFilter(search));

    var checkboxArchived = new Checkbox();
    checkboxArchived.setWidthFull();
    checkboxArchived.getStyle().set("max-width", "20%");
    checkboxArchived.setLabel("Include archived");
    checkboxArchived.addValueChangeListener(e -> dataView.refreshAll());
    dataView.addFilter(new RepoFilter(checkboxArchived));

    var inputLayout = new HorizontalLayout();
    inputLayout.setWidthFull();
    inputLayout.add(search);
    inputLayout.add(checkboxArchived);
    return inputLayout;
  }

  private Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "var(--lumo-space-xs");
    return icon;
  }

  private void updateSettings(Repo repo) {
    try {
      var ghRepo = GitHubProvider.get().getRepository(repo.name());
      new GitHubRepoConfigurator(ghRepo, false).run();
      synch(repo);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void synch(Repo repo) {
    try {
      var ghRepo = GitHubProvider.get().getRepository(repo.name());
      synchronizer.synch(ghRepo);
      grid.getDataProvider().refreshAll();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class RepoFilter implements SerializablePredicate<Repo> {

    private final Checkbox archived;

    public RepoFilter(Checkbox archived) {
      this.archived = archived;
    }

    @Override
    public boolean test(Repo repo) {
      if (archived.getValue()) {
        return true;
      }
      return !repo.archived();
    }
  }

  public class SearchFilter implements SerializablePredicate<Repo> {

    private final TextField search;

    public SearchFilter(TextField search) {
      this.search = search;
    }

    @Override
    public boolean test(Repo repo) {
      var searchValue = search.getValue().trim().toLowerCase();
      if (searchValue.isEmpty()) {
        return true;
      }
      var matchesName = repo.name().toLowerCase().contains(searchValue);
      return matchesName;
    }
  }
}