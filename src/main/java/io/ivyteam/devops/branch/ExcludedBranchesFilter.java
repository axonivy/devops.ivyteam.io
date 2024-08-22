package io.ivyteam.devops.branch;

import java.util.Arrays;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

class ExcludedBranchesFilter implements SerializablePredicate<Branch> {

  private final TextField excludedBranchPrefixes;

  ExcludedBranchesFilter(TextField excludedBranchPrefixes) {
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
