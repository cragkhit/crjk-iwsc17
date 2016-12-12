package org.jfree.chart.axis;
import java.util.Date;
import java.io.Serializable;
public class Segment implements Comparable, Cloneable, Serializable {
    protected long segmentNumber;
    protected long segmentStart;
    protected long segmentEnd;
    protected long millisecond;
    protected Segment() {
    }
    protected Segment ( final long millisecond ) {
        this.segmentNumber = this.calculateSegmentNumber ( millisecond );
        this.segmentStart = SegmentedTimeline.access$000 ( SegmentedTimeline.this ) + this.segmentNumber * SegmentedTimeline.access$100 ( SegmentedTimeline.this );
        this.segmentEnd = this.segmentStart + SegmentedTimeline.access$100 ( SegmentedTimeline.this ) - 1L;
        this.millisecond = millisecond;
    }
    public long calculateSegmentNumber ( final long millis ) {
        if ( millis >= SegmentedTimeline.access$000 ( SegmentedTimeline.this ) ) {
            return ( millis - SegmentedTimeline.access$000 ( SegmentedTimeline.this ) ) / SegmentedTimeline.access$100 ( SegmentedTimeline.this );
        }
        return ( millis - SegmentedTimeline.access$000 ( SegmentedTimeline.this ) ) / SegmentedTimeline.access$100 ( SegmentedTimeline.this ) - 1L;
    }
    public long getSegmentNumber() {
        return this.segmentNumber;
    }
    public long getSegmentCount() {
        return 1L;
    }
    public long getSegmentStart() {
        return this.segmentStart;
    }
    public long getSegmentEnd() {
        return this.segmentEnd;
    }
    public long getMillisecond() {
        return this.millisecond;
    }
    public Date getDate() {
        return SegmentedTimeline.this.getDate ( this.millisecond );
    }
    public boolean contains ( final long millis ) {
        return this.segmentStart <= millis && millis <= this.segmentEnd;
    }
    public boolean contains ( final long from, final long to ) {
        return this.segmentStart <= from && to <= this.segmentEnd;
    }
    public boolean contains ( final Segment segment ) {
        return this.contains ( segment.getSegmentStart(), segment.getSegmentEnd() );
    }
    public boolean contained ( final long from, final long to ) {
        return from <= this.segmentStart && this.segmentEnd <= to;
    }
    public Segment intersect ( final long from, final long to ) {
        if ( from <= this.segmentStart && this.segmentEnd <= to ) {
            return this;
        }
        return null;
    }
    public boolean before ( final Segment other ) {
        return this.segmentEnd < other.getSegmentStart();
    }
    public boolean after ( final Segment other ) {
        return this.segmentStart > other.getSegmentEnd();
    }
    @Override
    public boolean equals ( final Object object ) {
        if ( object instanceof Segment ) {
            final Segment other = ( Segment ) object;
            return this.segmentNumber == other.getSegmentNumber() && this.segmentStart == other.getSegmentStart() && this.segmentEnd == other.getSegmentEnd() && this.millisecond == other.getMillisecond();
        }
        return false;
    }
    public Segment copy() {
        try {
            return ( Segment ) this.clone();
        } catch ( CloneNotSupportedException e ) {
            return null;
        }
    }
    @Override
    public int compareTo ( final Object object ) {
        final Segment other = ( Segment ) object;
        if ( this.before ( other ) ) {
            return -1;
        }
        if ( this.after ( other ) ) {
            return 1;
        }
        return 0;
    }
    public boolean inIncludeSegments() {
        return this.getSegmentNumberRelativeToGroup() < SegmentedTimeline.access$200 ( SegmentedTimeline.this ) && !this.inExceptionSegments();
    }
    public boolean inExcludeSegments() {
        return this.getSegmentNumberRelativeToGroup() >= SegmentedTimeline.access$200 ( SegmentedTimeline.this );
    }
    private long getSegmentNumberRelativeToGroup() {
        long p = this.segmentNumber % SegmentedTimeline.access$300 ( SegmentedTimeline.this );
        if ( p < 0L ) {
            p += SegmentedTimeline.access$300 ( SegmentedTimeline.this );
        }
        return p;
    }
    public boolean inExceptionSegments() {
        return SegmentedTimeline.access$400 ( SegmentedTimeline.this, this ) >= 0;
    }
    public void inc ( final long n ) {
        this.segmentNumber += n;
        final long m = n * SegmentedTimeline.access$100 ( SegmentedTimeline.this );
        this.segmentStart += m;
        this.segmentEnd += m;
        this.millisecond += m;
    }
    public void inc() {
        this.inc ( 1L );
    }
    public void dec ( final long n ) {
        this.segmentNumber -= n;
        final long m = n * SegmentedTimeline.access$100 ( SegmentedTimeline.this );
        this.segmentStart -= m;
        this.segmentEnd -= m;
        this.millisecond -= m;
    }
    public void dec() {
        this.dec ( 1L );
    }
    public void moveIndexToStart() {
        this.millisecond = this.segmentStart;
    }
    public void moveIndexToEnd() {
        this.millisecond = this.segmentEnd;
    }
}
