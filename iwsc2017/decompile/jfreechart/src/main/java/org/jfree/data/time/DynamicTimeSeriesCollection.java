package org.jfree.data.time;
import org.jfree.data.general.SeriesChangeEvent;
import java.util.TimeZone;
import org.jfree.data.Range;
import java.util.Calendar;
import org.jfree.data.RangeInfo;
import org.jfree.data.DomainInfo;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class DynamicTimeSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, DomainInfo, RangeInfo {
    public static final int START = 0;
    public static final int MIDDLE = 1;
    public static final int END = 2;
    private int maximumItemCount;
    protected int historyCount;
    private Comparable[] seriesKeys;
    private Class timePeriodClass;
    protected RegularTimePeriod[] pointsInTime;
    private int seriesCount;
    protected ValueSequence[] valueHistory;
    protected Calendar workingCalendar;
    private int position;
    private boolean domainIsPointsInTime;
    private int oldestAt;
    private int newestAt;
    private long deltaTime;
    private Long domainStart;
    private Long domainEnd;
    private Range domainRange;
    private Float minValue;
    private Float maxValue;
    private Range valueRange;
    public DynamicTimeSeriesCollection ( final int nSeries, final int nMoments ) {
        this ( nSeries, nMoments, new Millisecond(), TimeZone.getDefault() );
        this.newestAt = nMoments - 1;
    }
    public DynamicTimeSeriesCollection ( final int nSeries, final int nMoments, final TimeZone zone ) {
        this ( nSeries, nMoments, new Millisecond(), zone );
        this.newestAt = nMoments - 1;
    }
    public DynamicTimeSeriesCollection ( final int nSeries, final int nMoments, final RegularTimePeriod timeSample ) {
        this ( nSeries, nMoments, timeSample, TimeZone.getDefault() );
    }
    public DynamicTimeSeriesCollection ( final int nSeries, final int nMoments, final RegularTimePeriod timeSample, final TimeZone zone ) {
        this.maximumItemCount = 2000;
        this.timePeriodClass = Minute.class;
        this.minValue = new Float ( 0.0f );
        this.maxValue = null;
        this.maximumItemCount = nMoments;
        this.historyCount = nMoments;
        this.seriesKeys = new Comparable[nSeries];
        for ( int i = 0; i < nSeries; ++i ) {
            this.seriesKeys[i] = "";
        }
        this.newestAt = nMoments - 1;
        this.valueHistory = new ValueSequence[nSeries];
        this.timePeriodClass = timeSample.getClass();
        if ( this.timePeriodClass == Millisecond.class ) {
            this.pointsInTime = new Millisecond[nMoments];
        } else if ( this.timePeriodClass == Second.class ) {
            this.pointsInTime = new Second[nMoments];
        } else if ( this.timePeriodClass == Minute.class ) {
            this.pointsInTime = new Minute[nMoments];
        } else if ( this.timePeriodClass == Hour.class ) {
            this.pointsInTime = new Hour[nMoments];
        }
        this.workingCalendar = Calendar.getInstance ( zone );
        this.position = 0;
        this.domainIsPointsInTime = true;
    }
    public synchronized long setTimeBase ( final RegularTimePeriod start ) {
        if ( this.pointsInTime[0] == null ) {
            this.pointsInTime[0] = start;
            for ( int i = 1; i < this.historyCount; ++i ) {
                this.pointsInTime[i] = this.pointsInTime[i - 1].next();
            }
        }
        final long oldestL = this.pointsInTime[0].getFirstMillisecond ( this.workingCalendar );
        final long nextL = this.pointsInTime[1].getFirstMillisecond ( this.workingCalendar );
        this.deltaTime = nextL - oldestL;
        this.oldestAt = 0;
        this.newestAt = this.historyCount - 1;
        this.findDomainLimits();
        return this.deltaTime;
    }
    protected void findDomainLimits() {
        final long startL = this.getOldestTime().getFirstMillisecond ( this.workingCalendar );
        long endL;
        if ( this.domainIsPointsInTime ) {
            endL = this.getNewestTime().getFirstMillisecond ( this.workingCalendar );
        } else {
            endL = this.getNewestTime().getLastMillisecond ( this.workingCalendar );
        }
        this.domainStart = new Long ( startL );
        this.domainEnd = new Long ( endL );
        this.domainRange = new Range ( startL, endL );
    }
    public int getPosition() {
        return this.position;
    }
    public void setPosition ( final int position ) {
        this.position = position;
    }
    public void addSeries ( final float[] values, final int seriesNumber, final Comparable seriesKey ) {
        this.invalidateRangeInfo();
        if ( values == null ) {
            throw new IllegalArgumentException ( "TimeSeriesDataset.addSeries(): cannot add null array of values." );
        }
        if ( seriesNumber >= this.valueHistory.length ) {
            throw new IllegalArgumentException ( "TimeSeriesDataset.addSeries(): cannot add more series than specified in c'tor" );
        }
        if ( this.valueHistory[seriesNumber] == null ) {
            this.valueHistory[seriesNumber] = new ValueSequence ( this.historyCount );
            ++this.seriesCount;
        }
        final int srcLength = values.length;
        int copyLength = this.historyCount;
        boolean fillNeeded = false;
        if ( srcLength < this.historyCount ) {
            fillNeeded = true;
            copyLength = srcLength;
        }
        for ( int i = 0; i < copyLength; ++i ) {
            this.valueHistory[seriesNumber].enterData ( i, values[i] );
        }
        if ( fillNeeded ) {
            for ( int i = copyLength; i < this.historyCount; ++i ) {
                this.valueHistory[seriesNumber].enterData ( i, 0.0f );
            }
        }
        if ( seriesKey != null ) {
            this.seriesKeys[seriesNumber] = seriesKey;
        }
        this.fireSeriesChanged();
    }
    public void setSeriesKey ( final int seriesNumber, final Comparable key ) {
        this.seriesKeys[seriesNumber] = key;
    }
    public void addValue ( final int seriesNumber, final int index, final float value ) {
        this.invalidateRangeInfo();
        if ( seriesNumber >= this.valueHistory.length ) {
            throw new IllegalArgumentException ( "TimeSeriesDataset.addValue(): series #" + seriesNumber + "unspecified in c'tor" );
        }
        if ( this.valueHistory[seriesNumber] == null ) {
            this.valueHistory[seriesNumber] = new ValueSequence ( this.historyCount );
            ++this.seriesCount;
        }
        this.valueHistory[seriesNumber].enterData ( index, value );
        this.fireSeriesChanged();
    }
    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }
    @Override
    public int getItemCount ( final int series ) {
        return this.historyCount;
    }
    protected int translateGet ( final int toFetch ) {
        if ( this.oldestAt == 0 ) {
            return toFetch;
        }
        int newIndex = toFetch + this.oldestAt;
        if ( newIndex >= this.historyCount ) {
            newIndex -= this.historyCount;
        }
        return newIndex;
    }
    public int offsetFromNewest ( final int delta ) {
        return this.wrapOffset ( this.newestAt + delta );
    }
    public int offsetFromOldest ( final int delta ) {
        return this.wrapOffset ( this.oldestAt + delta );
    }
    protected int wrapOffset ( final int protoIndex ) {
        int tmp = protoIndex;
        if ( tmp >= this.historyCount ) {
            tmp -= this.historyCount;
        } else if ( tmp < 0 ) {
            tmp += this.historyCount;
        }
        return tmp;
    }
    public synchronized RegularTimePeriod advanceTime() {
        final RegularTimePeriod nextInstant = this.pointsInTime[this.newestAt].next();
        this.newestAt = this.oldestAt;
        boolean extremaChanged = false;
        float oldMax = 0.0f;
        if ( this.maxValue != null ) {
            oldMax = this.maxValue;
        }
        for ( int s = 0; s < this.getSeriesCount(); ++s ) {
            if ( this.valueHistory[s].getData ( this.oldestAt ) == oldMax ) {
                extremaChanged = true;
            }
            if ( extremaChanged ) {
                break;
            }
        }
        if ( extremaChanged ) {
            this.invalidateRangeInfo();
        }
        final float wiper = 0.0f;
        for ( int s2 = 0; s2 < this.getSeriesCount(); ++s2 ) {
            this.valueHistory[s2].enterData ( this.newestAt, wiper );
        }
        this.pointsInTime[this.newestAt] = nextInstant;
        ++this.oldestAt;
        if ( this.oldestAt >= this.historyCount ) {
            this.oldestAt = 0;
        }
        final long startL = this.domainStart;
        this.domainStart = new Long ( startL + this.deltaTime );
        final long endL = this.domainEnd;
        this.domainEnd = new Long ( endL + this.deltaTime );
        this.domainRange = new Range ( startL, endL );
        this.fireSeriesChanged();
        return nextInstant;
    }
    public void invalidateRangeInfo() {
        this.maxValue = null;
        this.valueRange = null;
    }
    protected double findMaxValue() {
        double max = 0.0;
        for ( int s = 0; s < this.getSeriesCount(); ++s ) {
            for ( int i = 0; i < this.historyCount; ++i ) {
                final double tmp = this.getYValue ( s, i );
                if ( tmp > max ) {
                    max = tmp;
                }
            }
        }
        return max;
    }
    public int getOldestIndex() {
        return this.oldestAt;
    }
    public int getNewestIndex() {
        return this.newestAt;
    }
    public void appendData ( final float[] newData ) {
        final int nDataPoints = newData.length;
        if ( nDataPoints > this.valueHistory.length ) {
            throw new IllegalArgumentException ( "More data than series to put them in" );
        }
        for ( int s = 0; s < nDataPoints; ++s ) {
            if ( this.valueHistory[s] == null ) {
                this.valueHistory[s] = new ValueSequence ( this.historyCount );
            }
            this.valueHistory[s].enterData ( this.newestAt, newData[s] );
        }
        this.fireSeriesChanged();
    }
    public void appendData ( final float[] newData, int insertionIndex, final int refresh ) {
        final int nDataPoints = newData.length;
        if ( nDataPoints > this.valueHistory.length ) {
            throw new IllegalArgumentException ( "More data than series to put them in" );
        }
        for ( int s = 0; s < nDataPoints; ++s ) {
            if ( this.valueHistory[s] == null ) {
                this.valueHistory[s] = new ValueSequence ( this.historyCount );
            }
            this.valueHistory[s].enterData ( insertionIndex, newData[s] );
        }
        if ( refresh > 0 && ++insertionIndex % refresh == 0 ) {
            this.fireSeriesChanged();
        }
    }
    public RegularTimePeriod getNewestTime() {
        return this.pointsInTime[this.newestAt];
    }
    public RegularTimePeriod getOldestTime() {
        return this.pointsInTime[this.oldestAt];
    }
    @Override
    public Number getX ( final int series, final int item ) {
        final RegularTimePeriod tp = this.pointsInTime[this.translateGet ( item )];
        return new Long ( this.getX ( tp ) );
    }
    @Override
    public double getYValue ( final int series, final int item ) {
        final ValueSequence values = this.valueHistory[series];
        return values.getData ( this.translateGet ( item ) );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        return new Float ( this.getYValue ( series, item ) );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        final RegularTimePeriod tp = this.pointsInTime[this.translateGet ( item )];
        return new Long ( tp.getFirstMillisecond ( this.workingCalendar ) );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        final RegularTimePeriod tp = this.pointsInTime[this.translateGet ( item )];
        return new Long ( tp.getLastMillisecond ( this.workingCalendar ) );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        return this.seriesKeys[series];
    }
    protected void fireSeriesChanged() {
        this.seriesChanged ( new SeriesChangeEvent ( this ) );
    }
    @Override
    public double getDomainLowerBound ( final boolean includeInterval ) {
        return this.domainStart;
    }
    @Override
    public double getDomainUpperBound ( final boolean includeInterval ) {
        return this.domainEnd;
    }
    @Override
    public Range getDomainBounds ( final boolean includeInterval ) {
        if ( this.domainRange == null ) {
            this.findDomainLimits();
        }
        return this.domainRange;
    }
    private long getX ( final RegularTimePeriod period ) {
        switch ( this.position ) {
        case 0: {
            return period.getFirstMillisecond ( this.workingCalendar );
        }
        case 1: {
            return period.getMiddleMillisecond ( this.workingCalendar );
        }
        case 2: {
            return period.getLastMillisecond ( this.workingCalendar );
        }
        default: {
            return period.getMiddleMillisecond ( this.workingCalendar );
        }
        }
    }
    @Override
    public double getRangeLowerBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        if ( this.minValue != null ) {
            result = this.minValue;
        }
        return result;
    }
    @Override
    public double getRangeUpperBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        if ( this.maxValue != null ) {
            result = this.maxValue;
        }
        return result;
    }
    @Override
    public Range getRangeBounds ( final boolean includeInterval ) {
        if ( this.valueRange == null ) {
            final double max = this.getRangeUpperBound ( includeInterval );
            this.valueRange = new Range ( 0.0, max );
        }
        return this.valueRange;
    }
    protected class ValueSequence {
        float[] dataPoints;
        public ValueSequence ( final DynamicTimeSeriesCollection this$0 ) {
            this ( this$0, this$0.maximumItemCount );
        }
        public ValueSequence ( final int length ) {
            this.dataPoints = new float[length];
            for ( int i = 0; i < length; ++i ) {
                this.dataPoints[i] = 0.0f;
            }
        }
        public void enterData ( final int index, final float value ) {
            this.dataPoints[index] = value;
        }
        public float getData ( final int index ) {
            return this.dataPoints[index];
        }
    }
}
