package org.apache.el.parser;
private static class NestedState {
    private int nestingCount;
    private boolean hasFormalParameters;
    private NestedState() {
        this.nestingCount = 0;
        this.hasFormalParameters = false;
    }
    private void incrementNestingCount() {
        ++this.nestingCount;
    }
    private int getNestingCount() {
        return this.nestingCount;
    }
    private void setHasFormalParameters() {
        this.hasFormalParameters = true;
    }
    private boolean getHasFormalParameters() {
        return this.hasFormalParameters;
    }
}
