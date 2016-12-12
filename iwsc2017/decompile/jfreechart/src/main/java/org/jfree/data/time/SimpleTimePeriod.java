package org.jfree.data.time;
import java.util.Date;
import java.io.Serializable;
public class SimpleTimePeriod implements TimePeriod, Comparable, Serializable {
    private static final long serialVersionUID = 8684672361131829554L;
    private long start;
    private long end;
    public SimpleTimePeriod ( final long start, final long end ) {
        if ( start > end ) {
            throw new IllegalArgumentException ( "Requires start <= end." );
        }
        this.start = start;
        this.end = end;
    }
    public SimpleTimePeriod ( final Date start, final Date end ) {
        this ( start.getTime(), end.getTime() );
    }
    @Override
    public Date getStart() {
        return new Date ( this.start );
    }
    public long getStartMillis() {
        return this.start;
    }
    @Override
    public Date getEnd() {
        return new Date ( this.end );
    }
    public long getEndMillis() {
        return this.end;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TimePeriod ) ) {
            return false;
        }
        final TimePeriod that = ( TimePeriod ) obj;
        return this.getStart().equals ( that.getStart() ) && this.getEnd().equals ( that.getEnd() );
    }
    @Override
    public int compareTo ( final Object obj ) {
        final TimePeriod that = ( TimePeriod ) obj;
        final long t0 = this.getStart().getTime();
        final long t = this.getEnd().getTime();
        final long m0 = t0 + ( t - t0 ) / 2L;
        final long t2 = that.getStart().getTime();
        final long t3 = that.getEnd().getTime();
        final long m = t2 + ( t3 - t2 ) / 2L;
        if ( m0 < m ) {
            return -1;
        }
        if ( m0 > m ) {
            return 1;
        }
        if ( t0 < t2 ) {
            return -1;
        }
        if ( t0 > t2 ) {
            return 1;
        }
        if ( t < t3 ) {
            return -1;
        }
        if ( t > t3 ) {
            return 1;
        }
        return 0;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ( int ) this.start;
        result = 37 * result + ( int ) this.end;
        return result;
    }
}
