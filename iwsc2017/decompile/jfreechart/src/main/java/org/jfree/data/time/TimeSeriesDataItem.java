package org.jfree.data.time;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class TimeSeriesDataItem implements Cloneable, Comparable, Serializable {
    private static final long serialVersionUID = -2235346966016401302L;
    private RegularTimePeriod period;
    private Number value;
    public TimeSeriesDataItem ( final RegularTimePeriod period, final Number value ) {
        ParamChecks.nullNotPermitted ( period, "period" );
        this.period = period;
        this.value = value;
    }
    public TimeSeriesDataItem ( final RegularTimePeriod period, final double value ) {
        this ( period, new Double ( value ) );
    }
    public RegularTimePeriod getPeriod() {
        return this.period;
    }
    public Number getValue() {
        return this.value;
    }
    public void setValue ( final Number value ) {
        this.value = value;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof TimeSeriesDataItem ) ) {
            return false;
        }
        final TimeSeriesDataItem that = ( TimeSeriesDataItem ) obj;
        return ObjectUtilities.equal ( ( Object ) this.period, ( Object ) that.period ) && ObjectUtilities.equal ( ( Object ) this.value, ( Object ) that.value );
    }
    @Override
    public int hashCode() {
        int result = ( this.period != null ) ? this.period.hashCode() : 0;
        result = 29 * result + ( ( this.value != null ) ? this.value.hashCode() : 0 );
        return result;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        int result;
        if ( o1 instanceof TimeSeriesDataItem ) {
            final TimeSeriesDataItem datapair = ( TimeSeriesDataItem ) o1;
            result = this.getPeriod().compareTo ( datapair.getPeriod() );
        } else {
            result = 1;
        }
        return result;
    }
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        } catch ( CloneNotSupportedException e ) {
            e.printStackTrace();
        }
        return clone;
    }
}
