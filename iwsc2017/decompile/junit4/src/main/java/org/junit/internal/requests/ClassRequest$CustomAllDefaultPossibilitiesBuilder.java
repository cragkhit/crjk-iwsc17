package org.junit.internal.requests;
import org.junit.runners.model.RunnerBuilder;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
private class CustomAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {
    protected RunnerBuilder suiteMethodBuilder() {
        return new CustomSuiteMethodBuilder();
    }
}
