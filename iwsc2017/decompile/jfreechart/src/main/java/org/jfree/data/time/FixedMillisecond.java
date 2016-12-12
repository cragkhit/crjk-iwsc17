package org.jfree.data.time;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
public class FixedMillisecond extends RegularTimePeriod implements Serializable {
    private static final long serialVersionUID = 7867521484545646931L;
    private final long time;
    public FixedMillisecond() {
        this ( System.currentTimeMillis() );
    }
    public FixedMillisecond ( final long millisecond ) {
        this.time = millisecond;
    }
    public FixedMillisecond ( final Date time ) {
        this ( time.getTime() );
    }
    public Date getTime() {
        return new Date ( this.time );
    }
    @Override
    public void peg ( final Calendar calendar ) {
    }
    @Override
    public RegularTimePeriod previous() {
        RegularTimePeriod result = null;
        final long t = this.time;
        if ( t != Long.MIN_VALUE ) {
            result = new FixedMillisecond ( t - 1L );
        }
        return result;
    }
    @Override
    public RegularTimePeriod next() {
        RegularTimePeriod result = null;
        final long t = this.time;
        if ( t != Long.MAX_VALUE ) {
            result = new FixedMillisecond ( t + 1L );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object object ) {
        if ( object instanceof FixedMillisecond ) {
            final FixedMillisecond m = ( FixedMillisecond ) object;
            return this.time == m.getFirstMillisecond();
        }
        return false;
    }
    @Override
    public int hashCode() {
        return ( int ) this.time;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        int result;
        if ( o1 instanceof FixedMillisecond ) {
            final FixedMillisecond t1 = ( FixedMillisecond ) o1;
            final long difference = this.time - t1.time;
            if ( difference > 0L ) {
                result = 1;
            } else if ( difference < 0L ) {
                result = -1;
            } else {
                result = 0;
            }
        } else if ( o1 instanceof RegularTimePeriod ) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }
    @Override
    public long getFirstMillisecond() {
        return this.time;
    }
    @Override
    public long getFirstMillisecond ( final Calendar calendar ) {
        return this.time;
    }
    @Override
    public long getLastMillisecond() {
        return this.time;
    }
    @Override
    public long getLastMillisecond ( final Calendar calendar ) {
        return this.time;
    }
    @Override
    public long getMiddleMillisecond() {
        return this.time;
    }
    @Override
    public long getMiddleMillisecond ( final Calendar calendar ) {
        return this.time;
    }
    @Override
    public long getSerialIndex() {
        return this.time;
    }
}
