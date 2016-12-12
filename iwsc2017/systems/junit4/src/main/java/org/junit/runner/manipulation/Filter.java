package org.junit.runner.manipulation;
import org.junit.runner.Description;
import org.junit.runner.Request;
public abstract class Filter {
    public static final Filter ALL = new Filter() {
        @Override
        public boolean shouldRun ( Description description ) {
            return true;
        }
        @Override
        public String describe() {
            return "all tests";
        }
        @Override
        public void apply ( Object child ) throws NoTestsRemainException {
        }
        @Override
        public Filter intersect ( Filter second ) {
            return second;
        }
    };
    public static Filter matchMethodDescription ( final Description desiredDescription ) {
        return new Filter() {
            @Override
            public boolean shouldRun ( Description description ) {
                if ( description.isTest() ) {
                    return desiredDescription.equals ( description );
                }
                for ( Description each : description.getChildren() ) {
                    if ( shouldRun ( each ) ) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public String describe() {
                return String.format ( "Method %s", desiredDescription.getDisplayName() );
            }
        };
    }
    public abstract boolean shouldRun ( Description description );
    public abstract String describe();
    public void apply ( Object child ) throws NoTestsRemainException {
        if ( ! ( child instanceof Filterable ) ) {
            return;
        }
        Filterable filterable = ( Filterable ) child;
        filterable.filter ( this );
    }
    public Filter intersect ( final Filter second ) {
        if ( second == this || second == ALL ) {
            return this;
        }
        final Filter first = this;
        return new Filter() {
            @Override
            public boolean shouldRun ( Description description ) {
                return first.shouldRun ( description )
                       && second.shouldRun ( description );
            }
            @Override
            public String describe() {
                return first.describe() + " and " + second.describe();
            }
        };
    }
}
