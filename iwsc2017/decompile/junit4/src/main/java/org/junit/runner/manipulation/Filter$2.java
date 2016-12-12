package org.junit.runner.manipulation;
import java.util.Iterator;
import org.junit.runner.Description;
static final class Filter$2 extends Filter {
    final   Description val$desiredDescription;
    public boolean shouldRun ( final Description description ) {
        if ( description.isTest() ) {
            return this.val$desiredDescription.equals ( description );
        }
        for ( final Description each : description.getChildren() ) {
            if ( this.shouldRun ( each ) ) {
                return true;
            }
        }
        return false;
    }
    public String describe() {
        return String.format ( "Method %s", this.val$desiredDescription.getDisplayName() );
    }
}
