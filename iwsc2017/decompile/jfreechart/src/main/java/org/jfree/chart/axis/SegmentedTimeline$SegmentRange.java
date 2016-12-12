package org.jfree.chart.axis;
protected class SegmentRange extends Segment {
    private long segmentCount;
    public SegmentRange ( final long fromMillisecond, final long toMillisecond ) {
        final Segment start = SegmentedTimeline.this.getSegment ( fromMillisecond );
        final Segment end = SegmentedTimeline.this.getSegment ( toMillisecond );
        this.millisecond = fromMillisecond;
        this.segmentNumber = this.calculateSegmentNumber ( fromMillisecond );
        this.segmentStart = start.segmentStart;
        this.segmentEnd = end.segmentEnd;
        this.segmentCount = end.getSegmentNumber() - start.getSegmentNumber() + 1L;
    }
    @Override
    public long getSegmentCount() {
        return this.segmentCount;
    }
    @Override
    public Segment intersect ( final long from, final long to ) {
        final long start = Math.max ( from, this.segmentStart );
        final long end = Math.min ( to, this.segmentEnd );
        if ( start <= end ) {
            return new SegmentRange ( start, end );
        }
        return null;
    }
    @Override
    public boolean inIncludeSegments() {
        final Segment segment = SegmentedTimeline.this.getSegment ( this.segmentStart );
        while ( segment.getSegmentStart() < this.segmentEnd ) {
            if ( !segment.inIncludeSegments() ) {
                return false;
            }
            segment.inc();
        }
        return true;
    }
    @Override
    public boolean inExcludeSegments() {
        final Segment segment = SegmentedTimeline.this.getSegment ( this.segmentStart );
        while ( segment.getSegmentStart() < this.segmentEnd ) {
            if ( !segment.inExceptionSegments() ) {
                return false;
            }
            segment.inc();
        }
        return true;
    }
    @Override
    public void inc ( final long n ) {
        throw new IllegalArgumentException ( "Not implemented in SegmentRange" );
    }
}
