package io.ivyteam.devops.branches;

import java.util.Comparator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

    private static final String LOCAL_STORAGE_KEY = "excludedBranchPrefixes";

    public BranchesView() {
        var repos = RepoRepository.INSTANCE.all();
        var branches = repos.stream().flatMap(r -> r.branches().stream())
                .sorted(Comparator.comparing(Branch::lastCommitAuthor).thenComparing(Branch::repository))
                .toList();

        var grid = new Grid<Branch>();
        grid.setHeightFull();
        var dataView = grid.setItems(branches);
        grid
                .addColumn(Branch::lastCommitAuthor)
                .setHeader("Author")
                .setWidth("10%")
                .setSortable(true);
        grid
                .addColumn(Branch::repository)
                .setHeader("Repo")
                .setWidth("20%")
                .setSortable(true);
        grid
                .addColumn(new ComponentRenderer<Anchor, Branch>(b -> {
                    return new Anchor(b.ghLink(), b.name());
                }))
                .setHeader("Name")
                .setWidth("40%");

        var inputLayout = userInput(dataView);
        var layout = new VerticalLayout();
        layout.setHeightFull();
        layout.add(inputLayout);
        layout.add(grid);
        setContent(layout);
    }

    private Component userInput(GridListDataView<Branch> dataView) {
        var search = new TextField();
        search.setWidth("30%");
        search.setPlaceholder("Search");
        search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.addValueChangeListener(e -> dataView.refreshAll());

        var excludedBranchPrefixes = new TextField();
        excludedBranchPrefixes.setWidth("60%");
        excludedBranchPrefixes.setHelperText("Branch prefixes to be excluded, e.g. maste,release");
        excludedBranchPrefixes.setValueChangeMode(ValueChangeMode.EAGER);
        excludedBranchPrefixes.addValueChangeListener(e -> {
            dataView.refreshAll();
            WebStorage.setItem(LOCAL_STORAGE_KEY, e.getValue());
        });
        WebStorage.getItem(LOCAL_STORAGE_KEY, value -> {
            if (value == null) {
                excludedBranchPrefixes.setValue("master,release/,stale/,dependabot/,gh-pages,dev10.0,dev11.1");
            }
            excludedBranchPrefixes.setValue(value);
        });
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
}
