package io.ivyteam.devops.branches;

import java.util.Comparator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import io.ivyteam.devops.repo.Branch;
import io.ivyteam.devops.repo.RepoRepository;
import io.ivyteam.devops.view.View;

@Route("branches")
public class BranchesView extends View {

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

        var searchField = new TextField();
        searchField.setWidth("100%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(branch -> {
            var searchTerm = searchField.getValue().trim().toLowerCase();

            if (searchTerm.isEmpty())
                return true;

            var matchesName = branch.name().toLowerCase().contains(searchTerm);
            var matchesRepo = branch.repository().toLowerCase().contains(searchTerm);
            var matchesAuthor = branch.lastCommitAuthor().toLowerCase().contains(searchTerm);

            return matchesName || matchesRepo || matchesAuthor;
        });

        var layout = new VerticalLayout();
        layout.setHeightFull();
        layout.add(searchField);
        layout.add(searchField);
        layout.add(grid);
        setContent(layout);
    }

    @Override
    public String title() {
        return "Branches";
    }
}
