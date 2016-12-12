package org.jfree.chart.axis;
import java.util.Date;
import java.io.Serializable;
private static class DefaultTimeline implements Timeline, Serializable {
    @Override
    public long toTimelineValue ( final long millisecond ) {
        return millisecond;
    }
    @Override
    public long toTimelineValue ( final Date date ) {
        return date.getTime();
    }
    @Override
    public long toMillisecond ( final long value ) {
        return value;
    }
    @Override
    public boolean containsDomainValue ( final long millisecond ) {
        return true;
    }
    @Override
    public boolean containsDomainValue ( final Date date ) {
        return true;
    }
    @Override
    public boolean containsDomainRange ( final long from, final long to ) {
        return true;
    }
    @Override
    public boolean containsDomainRange ( final Date from, final Date to ) {
        return true;
    }
    @Override
    public boolean equals ( final Object object ) {
        return object != null && ( object == this || object instanceof DefaultTimeline );
    }
}
