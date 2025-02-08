package io.ivyteam.devops.repo;

import java.util.Comparator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.repo.check.RepoCheck;
import io.ivyteam.devops.repo.check.RepoCheck.Result;
import io.ivyteam.devops.securityscanner.ScanType;
import io.ivyteam.devops.securityscanner.SecurityScanner;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository.Key;
import io.ivyteam.devops.view.View;

@Route("")
public class ReposView extends View {

  private final Grid<Repo> grid;

  public ReposView(
      RepoRepository repos,
      PullRequestRepository prs,
      BranchRepository branches,
      SecurityScannerRepository securityscanners) {

    var repositories = repos.all();
    grid = new Grid<>(repositories);

    grid.addComponentColumn(ReposView::repoLink)
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
        .setWidth("5%")
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
        .setWidth("5%")
        .setSortable(true)
        .setComparator(Comparator.comparing(repo -> branchCount.getOrDefault(repo.name(), 0L)));

    grid
        .addComponentColumn(repo -> {
          if (RepoCheck.run(repo).stream().allMatch(Result::success)) {
            var icon = createIcon(VaadinIcon.CHECK);
            icon.getElement().getThemeList().add("badge success");
            return icon;
          }
          return null;
        })
        .setHeader("Checks")
        .setWidth("5%");

    grid.addComponentColumn(repo -> {
      var layout = new HorizontalLayout();
      if (repo.archived()) {
        var icon = createIcon(VaadinIcon.ARCHIVE);
        icon.setTooltipText("Archived");
        layout.add(icon);
      }
      return layout;
    }).setWidth("75px");

    grid.addComponentColumn(repo -> {
      var layout = new HorizontalLayout();
      if (repo.privateRepo()) {
        var icon = createIcon(VaadinIcon.LOCK);
        icon.setTooltipText("Private");
        layout.add(icon);
      }
      return layout;
    }).setWidth("75px");

    var scanners = securityscanners.all();

    grid.addComponentColumn(repo -> {
      var layout = new HorizontalLayout();
      var scanner = scanners.get(new Key(repo.name(), ScanType.DEPENDABOT));
      if (scanner != null) {
        layout.add(toSecurityScannerLink(scanner));
      }
      return layout;
    })
        .setHeader("Dependabot")
        .setWidth("100px")
        .setSortable(true)
        .setComparator(
            Comparator.comparing(repo -> getSortingNr(scanners.get(new Key(repo.name(), ScanType.DEPENDABOT)))));

    grid.addComponentColumn(repo -> {
      var layout = new HorizontalLayout();
      var scanner = scanners.get(new Key(repo.name(), ScanType.CODE_SCANNING));
      if (scanner != null) {
        layout.add(toSecurityScannerLink(scanner));
      }
      return layout;
    })
        .setHeader("CodeScan")
        .setWidth("100px")
        .setSortable(true)
        .setComparator(
            Comparator.comparing(repo -> getSortingNr(scanners.get(new Key(repo.name(), ScanType.CODE_SCANNING)))));

    grid.addComponentColumn(repo -> {
      var layout = new HorizontalLayout();
      var scanner = scanners.get(new Key(repo.name(), ScanType.SECRET_SCANNING));
      if (scanner != null) {
        layout.add(toSecurityScannerLink(scanner));
      }
      return layout;
    })
        .setHeader("SecretScan")
        .setWidth("100px")
        .setSortable(true)
        .setComparator(
            Comparator.comparing(repo -> getSortingNr(scanners.get(new Key(repo.name(), ScanType.SECRET_SCANNING)))));

    grid.setHeightFull();

    var inputLayout = userInput(grid.getListDataView());
    var layout = new VerticalLayout();
    layout.setHeightFull();
    layout.add(inputLayout);
    layout.add(grid);

    updateTitle(grid.getListDataView());

    setContent(layout);
  }

  private Component userInput(GridListDataView<Repo> dataView) {
    var search = new TextField();
    search.setWidth("80%");
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    search.setValueChangeMode(ValueChangeMode.EAGER);
    search.addValueChangeListener(e -> dataView.refreshAll());
    search.addValueChangeListener(e -> updateTitle(dataView));
    dataView.addFilter(new SearchFilter(search));

    var checkboxArchived = new Checkbox();
    checkboxArchived.setWidthFull();
    checkboxArchived.getStyle().set("max-width", "20%");
    checkboxArchived.setLabel("Include archived");
    checkboxArchived.addValueChangeListener(e -> dataView.refreshAll());
    checkboxArchived.addValueChangeListener(e -> updateTitle(dataView));
    dataView.addFilter(new RepoFilter(checkboxArchived));

    var layout = new HorizontalLayout();
    layout.setWidthFull();
    layout.setAlignItems(Alignment.CENTER);
    layout.add(search);
    layout.add(checkboxArchived);
    return layout;
  }

  private static Icon createIcon(VaadinIcon vaadinIcon) {
    var icon = vaadinIcon.create();
    icon.getStyle().set("padding", "0.25em");
    icon.getStyle().set("margin-bottom", "2px");
    icon.setSize("var(--lumo-icon-size-s)");
    return icon;
  }

  private void updateTitle(GridListDataView<Repo> dataView) {
    title.setText("Repositories (" + dataView.getItemCount() + ")");
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

  private int getSortingNr(SecurityScanner s) {
    if (s == null) {
      return -1;
    }
    return s.sort();
  }

  private Anchor toSecurityScannerLink(SecurityScanner ss) {
    int summary = ss.critical() + ss.high() + ss.medium() + ss.low();
    var text = ss.scantype().getValue() + "->  C: " + ss.critical() + " | H: " + ss.high() + " | M: " + ss.medium()
        + " | L: "
        + ss.low();

    var icon = VaadinIcon.QUESTION_CIRCLE.create();
    icon.setSize("14px");
    icon.setTooltipText(text);
    icon.getStyle().set("margin-left", "4px");

    var a = new Anchor(ss.link(), String.valueOf(summary), AnchorTarget.BLANK);
    a.add(icon);

    if (ss.critical() + ss.high() > 0) {
      a.getElement().getThemeList().add("badge pill small error");
    } else if (ss.low() + ss.medium() > 0) {
      a.getElement().getThemeList().add("badge pill small contrast");
    } else {
      a.getElement().getThemeList().add("badge pill small success");
      icon.setIcon(VaadinIcon.CHECK);
    }
    return a;
  }

  public static Component repoLink(Repo repo) {
    var icon = createIcon(VaadinIcon.EXTERNAL_LINK);
    var layout = new HorizontalLayout();
    layout.add(new Anchor(repo.link(), repo.name()));
    layout.add(new Anchor(repo.gitHubLink(), icon));
    layout.setSpacing(false);
    layout.setAlignItems(Alignment.CENTER);
    return layout;
  }
}
