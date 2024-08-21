package io.ivyteam.devops.branches;

import java.util.Arrays;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

import io.ivyteam.devops.repo.Branch;

public class ExcludedBranchesFilter implements SerializablePredicate<Branch> {

    private final TextField excludedBranchPrefixes;

    public ExcludedBranchesFilter(TextField excludedBranchPrefixes) {
        this.excludedBranchPrefixes = excludedBranchPrefixes;
    }

    @Override
    public boolean test(Branch branch) {
        var excludedBranches = excludedBranchPrefixes.getValue().trim().toLowerCase();
        if (excludedBranches.isEmpty()) {
            return true;
        }
        return !Arrays.stream(excludedBranches.split(","))
                .anyMatch(excludedBranche -> branch.name().startsWith(excludedBranche));
    }
}
