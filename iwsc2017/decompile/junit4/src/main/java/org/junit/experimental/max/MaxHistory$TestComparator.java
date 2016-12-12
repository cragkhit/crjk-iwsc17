package org.junit.experimental.max;
import org.junit.runner.Description;
import java.util.Comparator;
private class TestComparator implements Comparator<Description> {
    public int compare ( final Description o1, final Description o2 ) {
        if ( MaxHistory.this.isNewTest ( o1 ) ) {
            return -1;
        }
        if ( MaxHistory.this.isNewTest ( o2 ) ) {
            return 1;
        }
        final int result = this.getFailure ( o2 ).compareTo ( this.getFailure ( o1 ) );
        return ( result != 0 ) ? result : MaxHistory.this.getTestDuration ( o1 ).compareTo ( MaxHistory.this.getTestDuration ( o2 ) );
    }
    private Long getFailure ( final Description key ) {
        final Long result = MaxHistory.this.getFailureTimestamp ( key );
        if ( result == null ) {
            return 0L;
        }
        return result;
    }
}
