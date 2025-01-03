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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.branch.BranchRepository;
import io.ivyteam.devops.pullrequest.PullRequestRepository;
import io.ivyteam.devops.securityscanner.ScanType;
import io.ivyteam.devops.securityscanner.SecurityScanner;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository;
import io.ivyteam.devops.securityscanner.SecurityScannerRepository.Key;
import io.ivyteam.devops.view.View;

@Route("")
public class ReposView extends View {
  private final Grid<Repo> grid;

  public ReposView(RepoRepository repos, PullRequestRepository prs, BranchRepository branches,
      SecurityScannerRepository securityscanners) {

    var repositories = repos.all();
    grid = new Grid<>(repositories);
    title.setText("Repositories (" + repositories.size() + ")");

    grid.addComponentColumn(p -> new Anchor(p.link(), p.name()))
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
          if (repo.license() != null) {
            var icon = createIcon(VaadinIcon.CHECK);
            icon.getElement().getThemeList().add("badge success");
            return icon;
          }
          return null;
        })
        .setHeader("License")
        .setWidth("10%")
        .setSortable(true);

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
}
