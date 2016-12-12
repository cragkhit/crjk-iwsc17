package org.jfree.data.time;
import org.jfree.data.general.SeriesException;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.data.general.Series;
public class TimePeriodValues extends Series implements Serializable {
    static final long serialVersionUID = -2210593619794989709L;
    protected static final String DEFAULT_DOMAIN_DESCRIPTION = "Time";
    protected static final String DEFAULT_RANGE_DESCRIPTION = "Value";
    private String domain;
    private String range;
    private List data;
    private int minStartIndex;
    private int maxStartIndex;
    private int minMiddleIndex;
    private int maxMiddleIndex;
    private int minEndIndex;
    private int maxEndIndex;
    public TimePeriodValues ( final String name ) {
        this ( name, "Time", "Value" );
    }
    public TimePeriodValues ( final String name, final String domain, final String range ) {
        super ( name );
        this.minStartIndex = -1;
        this.maxStartIndex = -1;
        this.minMiddleIndex = -1;
        this.maxMiddleIndex = -1;
        this.minEndIndex = -1;
        this.maxEndIndex = -1;
        this.domain = domain;
        this.range = range;
        this.data = new ArrayList();
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
    public TimePeriodValue getDataItem ( final int index ) {
        return this.data.get ( index );
    }
    public TimePeriod getTimePeriod ( final int index ) {
        return this.getDataItem ( index ).getPeriod();
    }
    public Number getValue ( final int index ) {
        return this.getDataItem ( index ).getValue();
    }
    public void add ( final TimePeriodValue item ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        this.data.add ( item );
        this.updateBounds ( item.getPeriod(), this.data.size() - 1 );
        this.fireSeriesChanged();
    }
    private void updateBounds ( final TimePeriod period, final int index ) {
        final long start = period.getStart().getTime();
        final long end = period.getEnd().getTime();
        final long middle = start + ( end - start ) / 2L;
        if ( this.minStartIndex >= 0 ) {
            final long minStart = this.getDataItem ( this.minStartIndex ).getPeriod().getStart().getTime();
            if ( start < minStart ) {
                this.minStartIndex = index;
            }
        } else {
            this.minStartIndex = index;
        }
        if ( this.maxStartIndex >= 0 ) {
            final long maxStart = this.getDataItem ( this.maxStartIndex ).getPeriod().getStart().getTime();
            if ( start > maxStart ) {
                this.maxStartIndex = index;
            }
        } else {
            this.maxStartIndex = index;
        }
        if ( this.minMiddleIndex >= 0 ) {
            final long s = this.getDataItem ( this.minMiddleIndex ).getPeriod().getStart().getTime();
            final long e = this.getDataItem ( this.minMiddleIndex ).getPeriod().getEnd().getTime();
            final long minMiddle = s + ( e - s ) / 2L;
            if ( middle < minMiddle ) {
                this.minMiddleIndex = index;
            }
        } else {
            this.minMiddleIndex = index;
        }
        if ( this.maxMiddleIndex >= 0 ) {
            final long s = this.getDataItem ( this.maxMiddleIndex ).getPeriod().getStart().getTime();
            final long e = this.getDataItem ( this.maxMiddleIndex ).getPeriod().getEnd().getTime();
            final long maxMiddle = s + ( e - s ) / 2L;
            if ( middle > maxMiddle ) {
                this.maxMiddleIndex = index;
            }
        } else {
            this.maxMiddleIndex = index;
        }
        if ( this.minEndIndex >= 0 ) {
            final long minEnd = this.getDataItem ( this.minEndIndex ).getPeriod().getEnd().getTime();
            if ( end < minEnd ) {
                this.minEndIndex = index;
            }
        } else {
            this.minEndIndex = index;
        }
        if ( this.maxEndIndex >= 0 ) {
            final long maxEnd = this.getDataItem ( this.maxEndIndex ).getPeriod().getEnd().getTime();
            if ( end > maxEnd ) {
                this.maxEndIndex = index;
            }
        } else {
            this.maxEndIndex = index;
        }
    }
    private void recalculateBounds() {
        this.minStartIndex = -1;
        this.minMiddleIndex = -1;
        this.minEndIndex = -1;
        this.maxStartIndex = -1;
        this.maxMiddleIndex = -1;
        this.maxEndIndex = -1;
        for ( int i = 0; i < this.data.size(); ++i ) {
            final TimePeriodValue tpv = this.data.get ( i );
            this.updateBounds ( tpv.getPeriod(), i );
        }
    }
    public void add ( final TimePeriod period, final double value ) {
        final TimePeriodValue item = new TimePeriodValue ( period, value );
        this.add ( item );
    }
    public void add ( final TimePeriod period, final Number value ) {
        final TimePeriodValue item = new TimePeriodValue ( period, value );
        this.add ( item );
    }
    public void update ( final int index, final Number value ) {
        final TimePeriodValue item = this.getDataItem ( index );
        item.setValue ( value );
        this.fireSeriesChanged();
    }
    public void delete ( final int start, final int end ) {
        for ( int i = 0; i <= end - start; ++i ) {
            this.data.remove ( start );
        }
        this.recalculateBounds();
        this.fireSeriesChanged();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TimePeriodValues ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final TimePeriodValues that = ( TimePeriodValues ) obj;
        if ( !ObjectUtilities.equal ( ( Object ) this.getDomainDescription(), ( Object ) that.getDomainDescription() ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.getRangeDescription(), ( Object ) that.getRangeDescription() ) ) {
            return false;
        }
        final int count = this.getItemCount();
        if ( count != that.getItemCount() ) {
            return false;
        }
        for ( int i = 0; i < count; ++i ) {
            if ( !this.getDataItem ( i ).equals ( that.getDataItem ( i ) ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        int result = ( this.domain != null ) ? this.domain.hashCode() : 0;
        result = 29 * result + ( ( this.range != null ) ? this.range.hashCode() : 0 );
        result = 29 * result + this.data.hashCode();
        result = 29 * result + this.minStartIndex;
        result = 29 * result + this.maxStartIndex;
        result = 29 * result + this.minMiddleIndex;
        result = 29 * result + this.maxMiddleIndex;
        result = 29 * result + this.minEndIndex;
        result = 29 * result + this.maxEndIndex;
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final Object clone = this.createCopy ( 0, this.getItemCount() - 1 );
        return clone;
    }
    public TimePeriodValues createCopy ( final int start, final int end ) throws CloneNotSupportedException {
        final TimePeriodValues copy = ( TimePeriodValues ) super.clone();
        copy.data = new ArrayList();
        if ( this.data.size() > 0 ) {
            for ( int index = start; index <= end; ++index ) {
                final TimePeriodValue item = this.data.get ( index );
                final TimePeriodValue clone = ( TimePeriodValue ) item.clone();
                try {
                    copy.add ( clone );
                } catch ( SeriesException e ) {
                    System.err.println ( "Failed to add cloned item." );
                }
            }
        }
        return copy;
    }
    public int getMinStartIndex() {
        return this.minStartIndex;
    }
    public int getMaxStartIndex() {
        return this.maxStartIndex;
    }
    public int getMinMiddleIndex() {
        return this.minMiddleIndex;
    }
    public int getMaxMiddleIndex() {
        return this.maxMiddleIndex;
    }
    public int getMinEndIndex() {
        return this.minEndIndex;
    }
    public int getMaxEndIndex() {
        return this.maxEndIndex;
    }
}
