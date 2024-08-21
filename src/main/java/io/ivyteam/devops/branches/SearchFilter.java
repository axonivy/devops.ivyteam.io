package io.ivyteam.devops.branches;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

import io.ivyteam.devops.repo.Branch;

public class SearchFilter implements SerializablePredicate<Branch> {

    private final TextField search;

    public SearchFilter(TextField search) {
        this.search = search;
    }

    @Override
    public boolean test(Branch branch) {
        var searchValue = search.getValue().trim().toLowerCase();
        if (searchValue.isEmpty()) {
            return true;
        }
        var matchesName = branch.name().toLowerCase().contains(searchValue);
        var matchesRepo = branch.repository().toLowerCase().contains(searchValue);
        var matchesAuthor = branch.lastCommitAuthor().toLowerCase().contains(searchValue);
        return matchesName || matchesRepo || matchesAuthor;
    }
}
