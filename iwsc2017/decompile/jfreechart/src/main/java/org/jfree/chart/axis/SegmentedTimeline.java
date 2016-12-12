package org.jfree.chart.axis;
import java.util.Iterator;
import java.util.Date;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.TimeZone;
import java.io.Serializable;
public class SegmentedTimeline implements Timeline, Cloneable, Serializable {
    private static final long serialVersionUID = 1093779862539903110L;
    public static final long DAY_SEGMENT_SIZE = 86400000L;
    public static final long HOUR_SEGMENT_SIZE = 3600000L;
    public static final long FIFTEEN_MINUTE_SEGMENT_SIZE = 900000L;
    public static final long MINUTE_SEGMENT_SIZE = 60000L;
    public static long FIRST_MONDAY_AFTER_1900;
    public static TimeZone NO_DST_TIME_ZONE;
    public static TimeZone DEFAULT_TIME_ZONE;
    private Calendar workingCalendarNoDST;
    private Calendar workingCalendar;
    private long segmentSize;
    private int segmentsIncluded;
    private int segmentsExcluded;
    private int groupSegmentCount;
    private long startTime;
    private long segmentsIncludedSize;
    private long segmentsExcludedSize;
    private long segmentsGroupSize;
    private List exceptionSegments;
    private SegmentedTimeline baseTimeline;
    private boolean adjustForDaylightSaving;
    public SegmentedTimeline ( final long segmentSize, final int segmentsIncluded, final int segmentsExcluded ) {
        this.workingCalendar = Calendar.getInstance();
        this.exceptionSegments = new ArrayList();
        this.adjustForDaylightSaving = false;
        this.segmentSize = segmentSize;
        this.segmentsIncluded = segmentsIncluded;
        this.segmentsExcluded = segmentsExcluded;
        this.groupSegmentCount = this.segmentsIncluded + this.segmentsExcluded;
        this.segmentsIncludedSize = this.segmentsIncluded * this.segmentSize;
        this.segmentsExcludedSize = this.segmentsExcluded * this.segmentSize;
        this.segmentsGroupSize = this.segmentsIncludedSize + this.segmentsExcludedSize;
        final int offset = TimeZone.getDefault().getRawOffset();
        final TimeZone z = new SimpleTimeZone ( offset, "UTC-" + offset );
        this.workingCalendarNoDST = new GregorianCalendar ( z, Locale.getDefault() );
    }
    public static long firstMondayAfter1900() {
        final int offset = TimeZone.getDefault().getRawOffset();
        final TimeZone z = new SimpleTimeZone ( offset, "UTC-" + offset );
        final Calendar cal = new GregorianCalendar ( z );
        cal.set ( 1900, 0, 1, 0, 0, 0 );
        cal.set ( 14, 0 );
        while ( cal.get ( 7 ) != 2 ) {
            cal.add ( 5, 1 );
        }
        return cal.getTime().getTime();
    }
    public static SegmentedTimeline newMondayThroughFridayTimeline() {
        final SegmentedTimeline timeline = new SegmentedTimeline ( 86400000L, 5, 2 );
        timeline.setStartTime ( firstMondayAfter1900() );
        return timeline;
    }
    public static SegmentedTimeline newFifteenMinuteTimeline() {
        final SegmentedTimeline timeline = new SegmentedTimeline ( 900000L, 28, 68 );
        timeline.setStartTime ( firstMondayAfter1900() + 36L * timeline.getSegmentSize() );
        timeline.setBaseTimeline ( newMondayThroughFridayTimeline() );
        return timeline;
    }
    public boolean getAdjustForDaylightSaving() {
        return this.adjustForDaylightSaving;
    }
    public void setAdjustForDaylightSaving ( final boolean adjust ) {
        this.adjustForDaylightSaving = adjust;
    }
    public void setStartTime ( final long millisecond ) {
        this.startTime = millisecond;
    }
    public long getStartTime() {
        return this.startTime;
    }
    public int getSegmentsExcluded() {
        return this.segmentsExcluded;
    }
    public long getSegmentsExcludedSize() {
        return this.segmentsExcludedSize;
    }
    public int getGroupSegmentCount() {
        return this.groupSegmentCount;
    }
    public long getSegmentsGroupSize() {
        return this.segmentsGroupSize;
    }
    public int getSegmentsIncluded() {
        return this.segmentsIncluded;
    }
    public long getSegmentsIncludedSize() {
        return this.segmentsIncludedSize;
    }
    public long getSegmentSize() {
        return this.segmentSize;
    }
    public List getExceptionSegments() {
        return Collections.unmodifiableList ( ( List<?> ) this.exceptionSegments );
    }
    public void setExceptionSegments ( final List exceptionSegments ) {
        this.exceptionSegments = exceptionSegments;
    }
    public SegmentedTimeline getBaseTimeline() {
        return this.baseTimeline;
    }
    public void setBaseTimeline ( final SegmentedTimeline baseTimeline ) {
        if ( baseTimeline != null ) {
            if ( baseTimeline.getSegmentSize() < this.segmentSize ) {
                throw new IllegalArgumentException ( "baseTimeline.getSegmentSize() is smaller than segmentSize" );
            }
            if ( baseTimeline.getStartTime() > this.startTime ) {
                throw new IllegalArgumentException ( "baseTimeline.getStartTime() is after startTime" );
            }
            if ( baseTimeline.getSegmentSize() % this.segmentSize != 0L ) {
                throw new IllegalArgumentException ( "baseTimeline.getSegmentSize() is not multiple of segmentSize" );
            }
            if ( ( this.startTime - baseTimeline.getStartTime() ) % this.segmentSize != 0L ) {
                throw new IllegalArgumentException ( "baseTimeline is not aligned" );
            }
        }
        this.baseTimeline = baseTimeline;
    }
    @Override
    public long toTimelineValue ( long millisecond ) {
        final long rawMilliseconds = millisecond - this.startTime;
        final long groupMilliseconds = rawMilliseconds % this.segmentsGroupSize;
        final long groupIndex = rawMilliseconds / this.segmentsGroupSize;
        long result;
        if ( groupMilliseconds >= this.segmentsIncludedSize ) {
            result = this.toTimelineValue ( this.startTime + this.segmentsGroupSize * ( groupIndex + 1L ) );
        } else {
            Segment segment = this.getSegment ( millisecond );
            if ( segment.inExceptionSegments() ) {
                int p;
                while ( ( p = this.binarySearchExceptionSegments ( segment ) ) >= 0 ) {
                    segment = this.getSegment ( millisecond = this.exceptionSegments.get ( p ).getSegmentEnd() + 1L );
                }
                result = this.toTimelineValue ( millisecond );
            } else {
                final long shiftedSegmentedValue = millisecond - this.startTime;
                final long x = shiftedSegmentedValue % this.segmentsGroupSize;
                final long y = shiftedSegmentedValue / this.segmentsGroupSize;
                final long wholeExceptionsBeforeDomainValue = this.getExceptionSegmentCount ( this.startTime, millisecond - 1L );
                if ( x < this.segmentsIncludedSize ) {
                    result = this.segmentsIncludedSize * y + x - wholeExceptionsBeforeDomainValue * this.segmentSize;
                } else {
                    result = this.segmentsIncludedSize * ( y + 1L ) - wholeExceptionsBeforeDomainValue * this.segmentSize;
                }
            }
        }
        return result;
    }
    @Override
    public long toTimelineValue ( final Date date ) {
        return this.toTimelineValue ( this.getTime ( date ) );
    }
    @Override
    public long toMillisecond ( final long timelineValue ) {
        final Segment result = new Segment ( this.startTime + timelineValue + timelineValue / this.segmentsIncludedSize * this.segmentsExcludedSize );
        for ( long lastIndex = this.startTime; lastIndex <= result.segmentStart; ++lastIndex ) {
            long exceptionSegmentCount;
            while ( ( exceptionSegmentCount = this.getExceptionSegmentCount ( lastIndex, result.millisecond / this.segmentSize * this.segmentSize - 1L ) ) > 0L ) {
                lastIndex = result.segmentStart;
                for ( int i = 0; i < exceptionSegmentCount; ++i ) {
                    do {
                        result.inc();
                    } while ( result.inExcludeSegments() );
                }
            }
            lastIndex = result.segmentStart;
            while ( result.inExceptionSegments() || result.inExcludeSegments() ) {
                result.inc();
                lastIndex += this.segmentSize;
            }
        }
        return this.getTimeFromLong ( result.millisecond );
    }
    public long getTimeFromLong ( final long date ) {
        long result = date;
        if ( this.adjustForDaylightSaving ) {
            this.workingCalendarNoDST.setTime ( new Date ( date ) );
            this.workingCalendar.set ( this.workingCalendarNoDST.get ( 1 ), this.workingCalendarNoDST.get ( 2 ), this.workingCalendarNoDST.get ( 5 ), this.workingCalendarNoDST.get ( 11 ), this.workingCalendarNoDST.get ( 12 ), this.workingCalendarNoDST.get ( 13 ) );
            this.workingCalendar.set ( 14, this.workingCalendarNoDST.get ( 14 ) );
            result = this.workingCalendar.getTime().getTime();
        }
        return result;
    }
    @Override
    public boolean containsDomainValue ( final long millisecond ) {
        final Segment segment = this.getSegment ( millisecond );
        return segment.inIncludeSegments();
    }
    @Override
    public boolean containsDomainValue ( final Date date ) {
        return this.containsDomainValue ( this.getTime ( date ) );
    }
    @Override
    public boolean containsDomainRange ( final long domainValueStart, final long domainValueEnd ) {
        if ( domainValueEnd < domainValueStart ) {
            throw new IllegalArgumentException ( "domainValueEnd (" + domainValueEnd + ") < domainValueStart (" + domainValueStart + ")" );
        }
        final Segment segment = this.getSegment ( domainValueStart );
        boolean contains = true;
        do {
            contains = segment.inIncludeSegments();
            if ( segment.contains ( domainValueEnd ) ) {
                break;
            }
            segment.inc();
        } while ( contains );
        return contains;
    }
    @Override
    public boolean containsDomainRange ( final Date dateDomainValueStart, final Date dateDomainValueEnd ) {
        return this.containsDomainRange ( this.getTime ( dateDomainValueStart ), this.getTime ( dateDomainValueEnd ) );
    }
    public void addException ( final long millisecond ) {
        this.addException ( new Segment ( millisecond ) );
    }
    public void addException ( final long fromDomainValue, final long toDomainValue ) {
        this.addException ( new SegmentRange ( fromDomainValue, toDomainValue ) );
    }
    public void addException ( final Date exceptionDate ) {
        this.addException ( this.getTime ( exceptionDate ) );
    }
    public void addExceptions ( final List exceptionList ) {
        final Iterator iter = exceptionList.iterator();
        while ( iter.hasNext() ) {
            this.addException ( iter.next() );
        }
    }
    private void addException ( final Segment segment ) {
        if ( segment.inIncludeSegments() ) {
            final int p = this.binarySearchExceptionSegments ( segment );
            this.exceptionSegments.add ( - ( p + 1 ), segment );
        }
    }
    public void addBaseTimelineException ( final long domainValue ) {
        final Segment baseSegment = this.baseTimeline.getSegment ( domainValue );
        if ( baseSegment.inIncludeSegments() ) {
            final Segment segment = this.getSegment ( baseSegment.getSegmentStart() );
            while ( segment.getSegmentStart() <= baseSegment.getSegmentEnd() ) {
                if ( segment.inIncludeSegments() ) {
                    final long fromDomainValue = segment.getSegmentStart();
                    long toDomainValue;
                    do {
                        toDomainValue = segment.getSegmentEnd();
                        segment.inc();
                    } while ( segment.inIncludeSegments() );
                    this.addException ( fromDomainValue, toDomainValue );
                } else {
                    segment.inc();
                }
            }
        }
    }
    public void addBaseTimelineException ( final Date date ) {
        this.addBaseTimelineException ( this.getTime ( date ) );
    }
    public void addBaseTimelineExclusions ( final long fromBaseDomainValue, final long toBaseDomainValue ) {
        final Segment baseSegment = this.baseTimeline.getSegment ( fromBaseDomainValue );
        while ( baseSegment.getSegmentStart() <= toBaseDomainValue && !baseSegment.inExcludeSegments() ) {
            baseSegment.inc();
        }
        while ( baseSegment.getSegmentStart() <= toBaseDomainValue ) {
            final long baseExclusionRangeEnd = baseSegment.getSegmentStart() + this.baseTimeline.getSegmentsExcluded() * this.baseTimeline.getSegmentSize() - 1L;
            final Segment segment = this.getSegment ( baseSegment.getSegmentStart() );
            while ( segment.getSegmentStart() <= baseExclusionRangeEnd ) {
                if ( segment.inIncludeSegments() ) {
                    final long fromDomainValue = segment.getSegmentStart();
                    long toDomainValue;
                    do {
                        toDomainValue = segment.getSegmentEnd();
                        segment.inc();
                    } while ( segment.inIncludeSegments() );
                    this.addException ( new BaseTimelineSegmentRange ( fromDomainValue, toDomainValue ) );
                } else {
                    segment.inc();
                }
            }
            baseSegment.inc ( this.baseTimeline.getGroupSegmentCount() );
        }
    }
    public long getExceptionSegmentCount ( final long fromMillisecond, final long toMillisecond ) {
        if ( toMillisecond < fromMillisecond ) {
            return 0L;
        }
        int n = 0;
        for ( final Segment segment : this.exceptionSegments ) {
            final Segment intersection = segment.intersect ( fromMillisecond, toMillisecond );
            if ( intersection != null ) {
                n += ( int ) intersection.getSegmentCount();
            }
        }
        return n;
    }
    public Segment getSegment ( final long millisecond ) {
        return new Segment ( millisecond );
    }
    public Segment getSegment ( final Date date ) {
        return this.getSegment ( this.getTime ( date ) );
    }
    private boolean equals ( final Object o, final Object p ) {
        return o == p || ( o != null && o.equals ( p ) );
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( o instanceof SegmentedTimeline ) {
            final SegmentedTimeline other = ( SegmentedTimeline ) o;
            final boolean b0 = this.segmentSize == other.getSegmentSize();
            final boolean b = this.segmentsIncluded == other.getSegmentsIncluded();
            final boolean b2 = this.segmentsExcluded == other.getSegmentsExcluded();
            final boolean b3 = this.startTime == other.getStartTime();
            final boolean b4 = this.equals ( this.exceptionSegments, other.getExceptionSegments() );
            return b0 && b && b2 && b3 && b4;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int result = 19;
        result = 37 * result + ( int ) ( this.segmentSize ^ this.segmentSize >>> 32 );
        result = 37 * result + ( int ) ( this.startTime ^ this.startTime >>> 32 );
        return result;
    }
    private int binarySearchExceptionSegments ( final Segment segment ) {
        int low = 0;
        int high = this.exceptionSegments.size() - 1;
        while ( low <= high ) {
            final int mid = ( low + high ) / 2;
            final Segment midSegment = this.exceptionSegments.get ( mid );
            if ( segment.contains ( midSegment ) || midSegment.contains ( segment ) ) {
                return mid;
            }
            if ( midSegment.before ( segment ) ) {
                low = mid + 1;
            } else {
                if ( !midSegment.after ( segment ) ) {
                    throw new IllegalStateException ( "Invalid condition." );
                }
                high = mid - 1;
            }
        }
        return - ( low + 1 );
    }
    public long getTime ( final Date date ) {
        long result = date.getTime();
        if ( this.adjustForDaylightSaving ) {
            this.workingCalendar.setTime ( date );
            this.workingCalendarNoDST.set ( this.workingCalendar.get ( 1 ), this.workingCalendar.get ( 2 ), this.workingCalendar.get ( 5 ), this.workingCalendar.get ( 11 ), this.workingCalendar.get ( 12 ), this.workingCalendar.get ( 13 ) );
            this.workingCalendarNoDST.set ( 14, this.workingCalendar.get ( 14 ) );
            final Date revisedDate = this.workingCalendarNoDST.getTime();
            result = revisedDate.getTime();
        }
        return result;
    }
    public Date getDate ( final long value ) {
        this.workingCalendarNoDST.setTime ( new Date ( value ) );
        return this.workingCalendarNoDST.getTime();
    }
    public Object clone() throws CloneNotSupportedException {
        final SegmentedTimeline clone = ( SegmentedTimeline ) super.clone();
        return clone;
    }
    static {
        SegmentedTimeline.DEFAULT_TIME_ZONE = TimeZone.getDefault();
        final int offset = TimeZone.getDefault().getRawOffset();
        SegmentedTimeline.NO_DST_TIME_ZONE = new SimpleTimeZone ( offset, "UTC-" + offset );
        final Calendar cal = new GregorianCalendar ( SegmentedTimeline.NO_DST_TIME_ZONE );
        cal.set ( 1900, 0, 1, 0, 0, 0 );
        cal.set ( 14, 0 );
        while ( cal.get ( 7 ) != 2 ) {
            cal.add ( 5, 1 );
        }
        SegmentedTimeline.FIRST_MONDAY_AFTER_1900 = cal.getTime().getTime();
    }
    public class Segment implements Comparable, Cloneable, Serializable {
        protected long segmentNumber;
        protected long segmentStart;
        protected long segmentEnd;
        protected long millisecond;
        protected Segment() {
        }
        protected Segment ( final long millisecond ) {
            this.segmentNumber = this.calculateSegmentNumber ( millisecond );
            this.segmentStart = SegmentedTimeline.this.startTime + this.segmentNumber * SegmentedTimeline.this.segmentSize;
            this.segmentEnd = this.segmentStart + SegmentedTimeline.this.segmentSize - 1L;
            this.millisecond = millisecond;
        }
        public long calculateSegmentNumber ( final long millis ) {
            if ( millis >= SegmentedTimeline.this.startTime ) {
                return ( millis - SegmentedTimeline.this.startTime ) / SegmentedTimeline.this.segmentSize;
            }
            return ( millis - SegmentedTimeline.this.startTime ) / SegmentedTimeline.this.segmentSize - 1L;
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
            return this.getSegmentNumberRelativeToGroup() < SegmentedTimeline.this.segmentsIncluded && !this.inExceptionSegments();
        }
        public boolean inExcludeSegments() {
            return this.getSegmentNumberRelativeToGroup() >= SegmentedTimeline.this.segmentsIncluded;
        }
        private long getSegmentNumberRelativeToGroup() {
            long p = this.segmentNumber % SegmentedTimeline.this.groupSegmentCount;
            if ( p < 0L ) {
                p += SegmentedTimeline.this.groupSegmentCount;
            }
            return p;
        }
        public boolean inExceptionSegments() {
            return SegmentedTimeline.this.binarySearchExceptionSegments ( this ) >= 0;
        }
        public void inc ( final long n ) {
            this.segmentNumber += n;
            final long m = n * SegmentedTimeline.this.segmentSize;
            this.segmentStart += m;
            this.segmentEnd += m;
            this.millisecond += m;
        }
        public void inc() {
            this.inc ( 1L );
        }
        public void dec ( final long n ) {
            this.segmentNumber -= n;
            final long m = n * SegmentedTimeline.this.segmentSize;
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
    protected class BaseTimelineSegmentRange extends SegmentRange {
        public BaseTimelineSegmentRange ( final long fromDomainValue, final long toDomainValue ) {
            super ( fromDomainValue, toDomainValue );
        }
    }
}
