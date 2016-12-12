package org.junit.runner.manipulation;
import java.util.Iterator;
import org.junit.runner.Description;
public abstract class Filter {
    public static final Filter ALL;
    public static Filter matchMethodDescription ( final Description desiredDescription ) {
        return new Filter() {
            public boolean shouldRun ( final Description description ) {
                if ( description.isTest() ) {
                    return desiredDescription.equals ( description );
                }
                for ( final Description each : description.getChildren() ) {
                    if ( this.shouldRun ( each ) ) {
                        return true;
                    }
                }
                return false;
            }
            public String describe() {
                return String.format ( "Method %s", desiredDescription.getDisplayName() );
            }
        };
    }
    public abstract boolean shouldRun ( final Description p0 );
    public abstract String describe();
    public void apply ( final Object child ) throws NoTestsRemainException {
        if ( ! ( child instanceof Filterable ) ) {
            return;
        }
        final Filterable filterable = ( Filterable ) child;
        filterable.filter ( this );
    }
    public Filter intersect ( final Filter second ) {
        if ( second == this || second == Filter.ALL ) {
            return this;
        }
        return new Filter() {
            public boolean shouldRun ( final Description description ) {
                return Filter.this.shouldRun ( description ) && second.shouldRun ( description );
            }
            public String describe() {
                return Filter.this.describe() + " and " + second.describe();
            }
        };
    }
    static {
        ALL = new Filter() {
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
        };
    }
}
