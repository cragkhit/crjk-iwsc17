package org.jfree.chart.axis;
import java.util.Date;
public interface Timeline {
    long toTimelineValue ( long p0 );
    long toTimelineValue ( Date p0 );
    long toMillisecond ( long p0 );
    boolean containsDomainValue ( long p0 );
    boolean containsDomainValue ( Date p0 );
    boolean containsDomainRange ( long p0, long p1 );
    boolean containsDomainRange ( Date p0, Date p1 );
}
