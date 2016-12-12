package org.junit.runner.manipulation;
import org.junit.runner.Description;
class Filter$3 extends Filter {
    public boolean shouldRun ( final Description description ) {
        return Filter.this.shouldRun ( description ) && Filter.this.shouldRun ( description );
    }
    public String describe() {
        return Filter.this.describe() + " and " + Filter.this.describe();
    }
}
