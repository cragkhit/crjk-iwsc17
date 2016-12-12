package org.jfree.data.time;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class TimePeriodValue implements Cloneable, Serializable {
    private static final long serialVersionUID = 3390443360845711275L;
    private TimePeriod period;
    private Number value;
    public TimePeriodValue ( final TimePeriod period, final Number value ) {
        ParamChecks.nullNotPermitted ( period, "period" );
        this.period = period;
        this.value = value;
    }
    public TimePeriodValue ( final TimePeriod period, final double value ) {
        this ( period, new Double ( value ) );
    }
    public TimePeriod getPeriod() {
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
        if ( ! ( obj instanceof TimePeriodValue ) ) {
            return false;
        }
        final TimePeriodValue timePeriodValue = ( TimePeriodValue ) obj;
        Label_0054: {
            if ( this.period != null ) {
                if ( this.period.equals ( timePeriodValue.period ) ) {
                    break Label_0054;
                }
            } else if ( timePeriodValue.period == null ) {
                break Label_0054;
            }
            return false;
        }
        if ( this.value != null ) {
            if ( this.value.equals ( timePeriodValue.value ) ) {
                return true;
            }
        } else if ( timePeriodValue.value == null ) {
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int result = ( this.period != null ) ? this.period.hashCode() : 0;
        result = 29 * result + ( ( this.value != null ) ? this.value.hashCode() : 0 );
        return result;
    }
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        } catch ( CloneNotSupportedException e ) {
            throw new RuntimeException ( e );
        }
        return clone;
    }
    @Override
    public String toString() {
        return "TimePeriodValue[" + this.getPeriod() + "," + this.getValue() + "]";
    }
}
