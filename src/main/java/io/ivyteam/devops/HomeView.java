package io.ivyteam.devops;

import java.io.IOException;
import java.util.Comparator;

import org.apache.commons.lang3.function.BooleanConsumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.github.GitHubProvider;
import io.ivyteam.devops.github.GitHubRepoConfigurator;
import io.ivyteam.devops.github.GitHubSynchronizer;
import io.ivyteam.devops.repo.Repo;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("")
public class HomeView extends View {

  private final Grid<Repo> grid;

  public HomeView() {
    var repos = RepoRepository.INSTANCE.all();

    grid = new Grid<>();
    var dataView = grid.setItems(repos);

    grid.addColumn(LitRenderer.<Repo>of("""
           <a href="${item.link}">${item.name}</a>
        """)
        .withProperty("link", p -> p.link())
        .withProperty("name", p -> p.name()))
        .setHeader("Name")
        .setWidth("40%")
        .setSortable(true);

    var archivedColumn = grid
        .addColumn(Repo::archived)
        .setHeader("Archived")
        .setWidth("10%")
        .setSortable(true);

    grid
        .addComponentColumn(repo -> {
          var counter = new Span(String.valueOf(repo.openPullRequests()));
          counter.getElement().getThemeList().add("badge pill small contrast");
          counter.getStyle().set("margin-inline-start", "var(--lumo-space-s)");
          return counter;
        })
        .setHeader("Open PRs")
        .setWidth("10%")
        .setSortable(true)
        .setComparator(Comparator.comparing(Repo::openPullRequests));

    grid
        .addComponentColumn(repo -> {
          if (repo.archived()) {
            return null;
          }
          if (repo.license()) {
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
        .setWidth("20%");

    var repoFilter = new RepoFilter(dataView);
    grid.getHeaderRows().clear();
    var headerRow = grid.appendHeaderRow();
    headerRow.getCell(archivedColumn).setComponent(createFilterHeader(repoFilter::archived));

    grid.setHeightFull();
    setContent(grid);
  }

  private static Component createFilterHeader(BooleanConsumer filterChangeConsumer) {
    var textField = new Checkbox();
    textField.setWidthFull();
    textField.getStyle().set("max-width", "100%");
    textField.addValueChangeListener(
        e -> filterChangeConsumer.accept(e.getValue()));
    return textField;
  }

  @Override
  public String title() {
    return "Dashboard";
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
      new GitHubSynchronizer(null).synch(ghRepo);
      grid.getDataProvider().refreshAll();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class RepoFilter {

    private final GridListDataView<Repo> dataView;
    private boolean archived = false;

    public RepoFilter(GridListDataView<Repo> dataView) {
      this.dataView = dataView;
      this.dataView.addFilter(this::test);
    }

    public void archived(boolean archived) {
      this.archived = archived;
      this.dataView.refreshAll();
    }

    public boolean test(Repo repo) {
      return repo.archived() == archived;
    }
  }
}
