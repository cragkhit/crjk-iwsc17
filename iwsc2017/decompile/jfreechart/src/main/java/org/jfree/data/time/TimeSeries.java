package org.jfree.data.time;
import java.util.Iterator;
import org.jfree.util.ObjectUtilities;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.jfree.data.general.SeriesException;
import java.util.Collection;
import java.util.Calendar;
import org.jfree.chart.util.ParamChecks;
import java.util.TimeZone;
import org.jfree.data.Range;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.data.general.Series;
public class TimeSeries extends Series implements Cloneable, Serializable {
    private static final long serialVersionUID = -5032960206869675528L;
    protected static final String DEFAULT_DOMAIN_DESCRIPTION = "Time";
    protected static final String DEFAULT_RANGE_DESCRIPTION = "Value";
    private String domain;
    private String range;
    protected Class timePeriodClass;
    protected List data;
    private int maximumItemCount;
    private long maximumItemAge;
    private double minY;
    private double maxY;
    public TimeSeries ( final Comparable name ) {
        this ( name, "Time", "Value" );
    }
    public TimeSeries ( final Comparable name, final String domain, final String range ) {
        super ( name );
        this.domain = domain;
        this.range = range;
        this.timePeriodClass = null;
        this.data = new ArrayList();
        this.maximumItemCount = Integer.MAX_VALUE;
        this.maximumItemAge = Long.MAX_VALUE;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
    }
    public String getDomainDescription() {
        return this.domain;
    }
    public void setDomainDescription ( final String description ) {
        final String old = this.domain;
        this.firePropertyChange ( "Domain", old, this.domain = description );
    }
    public String getRangeDescription() {
        return this.range;
    }
    public void setRangeDescription ( final String description ) {
        final String old = this.range;
        this.firePropertyChange ( "Range", old, this.range = description );
    }
    @Override
    public int getItemCount() {
        return this.data.size();
    }
    public List getItems() {
        return Collections.unmodifiableList ( ( List<?> ) this.data );
    }
    public int getMaximumItemCount() {
        return this.maximumItemCount;
    }
    public void setMaximumItemCount ( final int maximum ) {
        if ( maximum < 0 ) {
            throw new IllegalArgumentException ( "Negative 'maximum' argument." );
        }
        this.maximumItemCount = maximum;
        final int count = this.data.size();
        if ( count > maximum ) {
            this.delete ( 0, count - maximum - 1 );
        }
    }
    public long getMaximumItemAge() {
        return this.maximumItemAge;
    }
    public void setMaximumItemAge ( final long periods ) {
        if ( periods < 0L ) {
            throw new IllegalArgumentException ( "Negative 'periods' argument." );
        }
        this.maximumItemAge = periods;
        this.removeAgedItems ( true );
    }
    public Range findValueRange() {
        if ( this.data.isEmpty() ) {
            return null;
        }
        return new Range ( this.minY, this.maxY );
    }
    public Range findValueRange ( final Range xRange, final TimeZone timeZone ) {
        return this.findValueRange ( xRange, TimePeriodAnchor.MIDDLE, timeZone );
    }
    public Range findValueRange ( final Range xRange, final TimePeriodAnchor xAnchor, final TimeZone zone ) {
        ParamChecks.nullNotPermitted ( xRange, "xRange" );
        ParamChecks.nullNotPermitted ( xAnchor, "xAnchor" );
        ParamChecks.nullNotPermitted ( zone, "zone" );
        if ( this.data.isEmpty() ) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance ( zone );
        double lowY = Double.POSITIVE_INFINITY;
        double highY = Double.NEGATIVE_INFINITY;
        for ( int i = 0; i < this.data.size(); ++i ) {
            final TimeSeriesDataItem item = this.data.get ( i );
            final long millis = item.getPeriod().getMillisecond ( xAnchor, calendar );
            if ( xRange.contains ( millis ) ) {
                final Number n = item.getValue();
                if ( n != null ) {
                    final double v = n.doubleValue();
                    lowY = this.minIgnoreNaN ( lowY, v );
                    highY = this.maxIgnoreNaN ( highY, v );
                }
            }
        }
        if ( !Double.isInfinite ( lowY ) || !Double.isInfinite ( highY ) ) {
            return new Range ( lowY, highY );
        }
        if ( lowY < highY ) {
            return new Range ( lowY, highY );
        }
        return new Range ( Double.NaN, Double.NaN );
    }
    public double getMinY() {
        return this.minY;
    }
    public double getMaxY() {
        return this.maxY;
    }
    public Class getTimePeriodClass() {
        return this.timePeriodClass;
    }
    public TimeSeriesDataItem getDataItem ( final int index ) {
        final TimeSeriesDataItem item = this.data.get ( index );
        return ( TimeSeriesDataItem ) item.clone();
    }
    public TimeSeriesDataItem getDataItem ( final RegularTimePeriod period ) {
        final int index = this.getIndex ( period );
        if ( index >= 0 ) {
            return this.getDataItem ( index );
        }
        return null;
    }
    TimeSeriesDataItem getRawDataItem ( final int index ) {
        return this.data.get ( index );
    }
    TimeSeriesDataItem getRawDataItem ( final RegularTimePeriod period ) {
        final int index = this.getIndex ( period );
        if ( index >= 0 ) {
            return this.data.get ( index );
        }
        return null;
    }
    public RegularTimePeriod getTimePeriod ( final int index ) {
        return this.getRawDataItem ( index ).getPeriod();
    }
    public RegularTimePeriod getNextTimePeriod() {
        final RegularTimePeriod last = this.getTimePeriod ( this.getItemCount() - 1 );
        return last.next();
    }
    public Collection getTimePeriods() {
        final Collection result = new ArrayList();
        for ( int i = 0; i < this.getItemCount(); ++i ) {
            result.add ( this.getTimePeriod ( i ) );
        }
        return result;
    }
    public Collection getTimePeriodsUniqueToOtherSeries ( final TimeSeries series ) {
        final Collection result = new ArrayList();
        for ( int i = 0; i < series.getItemCount(); ++i ) {
            final RegularTimePeriod period = series.getTimePeriod ( i );
            final int index = this.getIndex ( period );
            if ( index < 0 ) {
                result.add ( period );
            }
        }
        return result;
    }
    public int getIndex ( final RegularTimePeriod period ) {
        ParamChecks.nullNotPermitted ( period, "period" );
        final TimeSeriesDataItem dummy = new TimeSeriesDataItem ( period, -2.147483648E9 );
        return Collections.binarySearch ( this.data, dummy );
    }
    public Number getValue ( final int index ) {
        return this.getRawDataItem ( index ).getValue();
    }
    public Number getValue ( final RegularTimePeriod period ) {
        final int index = this.getIndex ( period );
        if ( index >= 0 ) {
            return this.getValue ( index );
        }
        return null;
    }
    public void add ( final TimeSeriesDataItem item ) {
        this.add ( item, true );
    }
    public void add ( TimeSeriesDataItem item, final boolean notify ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        item = ( TimeSeriesDataItem ) item.clone();
        final Class c = item.getPeriod().getClass();
        if ( this.timePeriodClass == null ) {
            this.timePeriodClass = c;
        } else if ( !this.timePeriodClass.equals ( c ) ) {
            final StringBuilder b = new StringBuilder();
            b.append ( "You are trying to add data where the time period class " );
            b.append ( "is " );
            b.append ( item.getPeriod().getClass().getName() );
            b.append ( ", but the TimeSeries is expecting an instance of " );
            b.append ( this.timePeriodClass.getName() );
            b.append ( "." );
            throw new SeriesException ( b.toString() );
        }
        boolean added = false;
        final int count = this.getItemCount();
        if ( count == 0 ) {
            this.data.add ( item );
            added = true;
        } else {
            final RegularTimePeriod last = this.getTimePeriod ( this.getItemCount() - 1 );
            if ( item.getPeriod().compareTo ( last ) > 0 ) {
                this.data.add ( item );
                added = true;
            } else {
                final int index = Collections.binarySearch ( this.data, item );
                if ( index >= 0 ) {
                    final StringBuilder b2 = new StringBuilder();
                    b2.append ( "You are attempting to add an observation for " );
                    b2.append ( "the time period " );
                    b2.append ( item.getPeriod().toString() );
                    b2.append ( " but the series already contains an observation" );
                    b2.append ( " for that time period. Duplicates are not " );
                    b2.append ( "permitted.  Try using the addOrUpdate() method." );
                    throw new SeriesException ( b2.toString() );
                }
                this.data.add ( -index - 1, item );
                added = true;
            }
        }
        if ( added ) {
            this.updateBoundsForAddedItem ( item );
            if ( this.getItemCount() > this.maximumItemCount ) {
                final TimeSeriesDataItem d = this.data.remove ( 0 );
                this.updateBoundsForRemovedItem ( d );
            }
            this.removeAgedItems ( false );
            if ( notify ) {
                this.fireSeriesChanged();
            }
        }
    }
    public void add ( final RegularTimePeriod period, final double value ) {
        this.add ( period, value, true );
    }
    public void add ( final RegularTimePeriod period, final double value, final boolean notify ) {
        final TimeSeriesDataItem item = new TimeSeriesDataItem ( period, value );
        this.add ( item, notify );
    }
    public void add ( final RegularTimePeriod period, final Number value ) {
        this.add ( period, value, true );
    }
    public void add ( final RegularTimePeriod period, final Number value, final boolean notify ) {
        final TimeSeriesDataItem item = new TimeSeriesDataItem ( period, value );
        this.add ( item, notify );
    }
    public void update ( final RegularTimePeriod period, final double value ) {
        this.update ( period, new Double ( value ) );
    }
    public void update ( final RegularTimePeriod period, final Number value ) {
        final TimeSeriesDataItem temp = new TimeSeriesDataItem ( period, value );
        final int index = Collections.binarySearch ( this.data, temp );
        if ( index < 0 ) {
            throw new SeriesException ( "There is no existing value for the specified 'period'." );
        }
        this.update ( index, value );
    }
    public void update ( final int index, final Number value ) {
        final TimeSeriesDataItem item = this.data.get ( index );
        boolean iterate = false;
        final Number oldYN = item.getValue();
        if ( oldYN != null ) {
            final double oldY = oldYN.doubleValue();
            if ( !Double.isNaN ( oldY ) ) {
                iterate = ( oldY <= this.minY || oldY >= this.maxY );
            }
        }
        item.setValue ( value );
        if ( iterate ) {
            this.updateMinMaxYByIteration();
        } else if ( value != null ) {
            final double yy = value.doubleValue();
            this.minY = this.minIgnoreNaN ( this.minY, yy );
            this.maxY = this.maxIgnoreNaN ( this.maxY, yy );
        }
        this.fireSeriesChanged();
    }
    public TimeSeries addAndOrUpdate ( final TimeSeries series ) {
        final TimeSeries overwritten = new TimeSeries ( "Overwritten values from: " + this.getKey() );
        for ( int i = 0; i < series.getItemCount(); ++i ) {
            final TimeSeriesDataItem item = series.getRawDataItem ( i );
            final TimeSeriesDataItem oldItem = this.addOrUpdate ( item.getPeriod(), item.getValue() );
            if ( oldItem != null ) {
                overwritten.add ( oldItem );
            }
        }
        return overwritten;
    }
    public TimeSeriesDataItem addOrUpdate ( final RegularTimePeriod period, final double value ) {
        return this.addOrUpdate ( period, new Double ( value ) );
    }
    public TimeSeriesDataItem addOrUpdate ( final RegularTimePeriod period, final Number value ) {
        return this.addOrUpdate ( new TimeSeriesDataItem ( period, value ) );
    }
    public TimeSeriesDataItem addOrUpdate ( TimeSeriesDataItem item ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        final Class periodClass = item.getPeriod().getClass();
        if ( this.timePeriodClass == null ) {
            this.timePeriodClass = periodClass;
        } else if ( !this.timePeriodClass.equals ( periodClass ) ) {
            final String msg = "You are trying to add data where the time period class is " + periodClass.getName() + ", but the TimeSeries is expecting an instance of " + this.timePeriodClass.getName() + ".";
            throw new SeriesException ( msg );
        }
        TimeSeriesDataItem overwritten = null;
        final int index = Collections.binarySearch ( this.data, item );
        if ( index >= 0 ) {
            final TimeSeriesDataItem existing = this.data.get ( index );
            overwritten = ( TimeSeriesDataItem ) existing.clone();
            boolean iterate = false;
            final Number oldYN = existing.getValue();
            final double oldY = ( oldYN != null ) ? oldYN.doubleValue() : Double.NaN;
            if ( !Double.isNaN ( oldY ) ) {
                iterate = ( oldY <= this.minY || oldY >= this.maxY );
            }
            existing.setValue ( item.getValue() );
            if ( iterate ) {
                this.updateMinMaxYByIteration();
            } else if ( item.getValue() != null ) {
                final double yy = item.getValue().doubleValue();
                this.minY = this.minIgnoreNaN ( this.minY, yy );
                this.maxY = this.maxIgnoreNaN ( this.maxY, yy );
            }
        } else {
            item = ( TimeSeriesDataItem ) item.clone();
            this.data.add ( -index - 1, item );
            this.updateBoundsForAddedItem ( item );
            if ( this.getItemCount() > this.maximumItemCount ) {
                final TimeSeriesDataItem d = this.data.remove ( 0 );
                this.updateBoundsForRemovedItem ( d );
            }
        }
        this.removeAgedItems ( false );
        this.fireSeriesChanged();
        return overwritten;
    }
    public void removeAgedItems ( final boolean notify ) {
        if ( this.getItemCount() > 1 ) {
            final long latest = this.getTimePeriod ( this.getItemCount() - 1 ).getSerialIndex();
            boolean removed = false;
            while ( latest - this.getTimePeriod ( 0 ).getSerialIndex() > this.maximumItemAge ) {
                this.data.remove ( 0 );
                removed = true;
            }
            if ( removed ) {
                this.updateMinMaxYByIteration();
                if ( notify ) {
                    this.fireSeriesChanged();
                }
            }
        }
    }
    public void removeAgedItems ( final long latest, final boolean notify ) {
        if ( this.data.isEmpty() ) {
            return;
        }
        long index = Long.MAX_VALUE;
        try {
            final Method m = RegularTimePeriod.class.getDeclaredMethod ( "createInstance", Class.class, Date.class, TimeZone.class );
            final RegularTimePeriod newest = ( RegularTimePeriod ) m.invoke ( this.timePeriodClass, this.timePeriodClass, new Date ( latest ), TimeZone.getDefault() );
            index = newest.getSerialIndex();
        } catch ( NoSuchMethodException e ) {
            throw new RuntimeException ( e );
        } catch ( IllegalAccessException e2 ) {
            throw new RuntimeException ( e2 );
        } catch ( InvocationTargetException e3 ) {
            throw new RuntimeException ( e3 );
        }
        boolean removed = false;
        while ( this.getItemCount() > 0 && index - this.getTimePeriod ( 0 ).getSerialIndex() > this.maximumItemAge ) {
            this.data.remove ( 0 );
            removed = true;
        }
        if ( removed ) {
            this.updateMinMaxYByIteration();
            if ( notify ) {
                this.fireSeriesChanged();
            }
        }
    }
    public void clear() {
        if ( this.data.size() > 0 ) {
            this.data.clear();
            this.timePeriodClass = null;
            this.minY = Double.NaN;
            this.maxY = Double.NaN;
            this.fireSeriesChanged();
        }
    }
    public void delete ( final RegularTimePeriod period ) {
        final int index = this.getIndex ( period );
        if ( index >= 0 ) {
            final TimeSeriesDataItem item = this.data.remove ( index );
            this.updateBoundsForRemovedItem ( item );
            if ( this.data.isEmpty() ) {
                this.timePeriodClass = null;
            }
            this.fireSeriesChanged();
        }
    }
    public void delete ( final int start, final int end ) {
        this.delete ( start, end, true );
    }
    public void delete ( final int start, final int end, final boolean notify ) {
        if ( end < start ) {
            throw new IllegalArgumentException ( "Requires start <= end." );
        }
        for ( int i = 0; i <= end - start; ++i ) {
            this.data.remove ( start );
        }
        this.updateMinMaxYByIteration();
        if ( this.data.isEmpty() ) {
            this.timePeriodClass = null;
        }
        if ( notify ) {
            this.fireSeriesChanged();
        }
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final TimeSeries clone = ( TimeSeries ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        return clone;
    }
    public TimeSeries createCopy ( final int start, final int end ) throws CloneNotSupportedException {
        if ( start < 0 ) {
            throw new IllegalArgumentException ( "Requires start >= 0." );
        }
        if ( end < start ) {
            throw new IllegalArgumentException ( "Requires start <= end." );
        }
        final TimeSeries copy = ( TimeSeries ) super.clone();
        copy.minY = Double.NaN;
        copy.maxY = Double.NaN;
        copy.data = new ArrayList();
        if ( this.data.size() > 0 ) {
            for ( int index = start; index <= end; ++index ) {
                final TimeSeriesDataItem item = this.data.get ( index );
                final TimeSeriesDataItem clone = ( TimeSeriesDataItem ) item.clone();
                try {
                    copy.add ( clone );
                } catch ( SeriesException e ) {
                    throw new RuntimeException ( e );
                }
            }
        }
        return copy;
    }
    public TimeSeries createCopy ( final RegularTimePeriod start, final RegularTimePeriod end ) throws CloneNotSupportedException {
        ParamChecks.nullNotPermitted ( start, "start" );
        ParamChecks.nullNotPermitted ( end, "end" );
        if ( start.compareTo ( end ) > 0 ) {
            throw new IllegalArgumentException ( "Requires start on or before end." );
        }
        boolean emptyRange = false;
        int startIndex = this.getIndex ( start );
        if ( startIndex < 0 ) {
            startIndex = - ( startIndex + 1 );
            if ( startIndex == this.data.size() ) {
                emptyRange = true;
            }
        }
        int endIndex = this.getIndex ( end );
        if ( endIndex < 0 ) {
            endIndex = - ( endIndex + 1 );
            --endIndex;
        }
        if ( endIndex < 0 || endIndex < startIndex ) {
            emptyRange = true;
        }
        if ( emptyRange ) {
            final TimeSeries copy = ( TimeSeries ) super.clone();
            copy.data = new ArrayList();
            return copy;
        }
        return this.createCopy ( startIndex, endIndex );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TimeSeries ) ) {
            return false;
        }
        final TimeSeries that = ( TimeSeries ) obj;
        if ( !ObjectUtilities.equal ( ( Object ) this.getDomainDescription(), ( Object ) that.getDomainDescription() ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.getRangeDescription(), ( Object ) that.getRangeDescription() ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.timePeriodClass, ( Object ) that.timePeriodClass ) ) {
            return false;
        }
        if ( this.getMaximumItemAge() != that.getMaximumItemAge() ) {
            return false;
        }
        if ( this.getMaximumItemCount() != that.getMaximumItemCount() ) {
            return false;
        }
        final int count = this.getItemCount();
        return count == that.getItemCount() && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + ( ( this.domain != null ) ? this.domain.hashCode() : 0 );
        result = 29 * result + ( ( this.range != null ) ? this.range.hashCode() : 0 );
        result = 29 * result + ( ( this.timePeriodClass != null ) ? this.timePeriodClass.hashCode() : 0 );
        final int count = this.getItemCount();
        if ( count > 0 ) {
            final TimeSeriesDataItem item = this.getRawDataItem ( 0 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 1 ) {
            final TimeSeriesDataItem item = this.getRawDataItem ( count - 1 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 2 ) {
            final TimeSeriesDataItem item = this.getRawDataItem ( count / 2 );
            result = 29 * result + item.hashCode();
        }
        result = 29 * result + this.maximumItemCount;
        result = 29 * result + ( int ) this.maximumItemAge;
        return result;
    }
    private void updateBoundsForAddedItem ( final TimeSeriesDataItem item ) {
        final Number yN = item.getValue();
        if ( item.getValue() != null ) {
            final double y = yN.doubleValue();
            this.minY = this.minIgnoreNaN ( this.minY, y );
            this.maxY = this.maxIgnoreNaN ( this.maxY, y );
        }
    }
    private void updateBoundsForRemovedItem ( final TimeSeriesDataItem item ) {
        final Number yN = item.getValue();
        if ( yN != null ) {
            final double y = yN.doubleValue();
            if ( !Double.isNaN ( y ) && ( y <= this.minY || y >= this.maxY ) ) {
                this.updateMinMaxYByIteration();
            }
        }
    }
    private void updateMinMaxYByIteration() {
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
        for ( final TimeSeriesDataItem item : this.data ) {
            this.updateBoundsForAddedItem ( item );
        }
    }
    private double minIgnoreNaN ( final double a, final double b ) {
        if ( Double.isNaN ( a ) ) {
            return b;
        }
        if ( Double.isNaN ( b ) ) {
            return a;
        }
        return Math.min ( a, b );
    }
    private double maxIgnoreNaN ( final double a, final double b ) {
        if ( Double.isNaN ( a ) ) {
            return b;
        }
        if ( Double.isNaN ( b ) ) {
            return a;
        }
        return Math.max ( a, b );
    }
    public TimeSeries ( final Comparable name, final Class timePeriodClass ) {
        this ( name, "Time", "Value", timePeriodClass );
    }
    public TimeSeries ( final Comparable name, final String domain, final String range, final Class timePeriodClass ) {
        super ( name );
        this.domain = domain;
        this.range = range;
        this.timePeriodClass = timePeriodClass;
        this.data = new ArrayList();
        this.maximumItemCount = Integer.MAX_VALUE;
        this.maximumItemAge = Long.MAX_VALUE;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
    }
}
