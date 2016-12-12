package org.junit.runner.manipulation;
import org.junit.runner.Description;
static final class Filter$1 extends Filter {
    public boolean shouldRun ( final Description description ) {
        return true;
    }
    public String describe() {
        return "all tests";
    }
    public void apply ( final Object child ) throws NoTestsRemainException {
    }
    public Filter intersect ( final Filter second ) {
        return second;
    }
}
