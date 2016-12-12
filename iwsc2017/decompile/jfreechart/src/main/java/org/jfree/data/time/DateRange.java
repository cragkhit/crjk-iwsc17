package org.jfree.data.time;
import java.text.DateFormat;
import java.util.Date;
import java.io.Serializable;
import org.jfree.data.Range;
public class DateRange extends Range implements Serializable {
    private static final long serialVersionUID = -4705682568375418157L;
    private long lowerDate;
    private long upperDate;
    public DateRange() {
        this ( new Date ( 0L ), new Date ( 1L ) );
    }
    public DateRange ( final Date lower, final Date upper ) {
        super ( lower.getTime(), upper.getTime() );
        this.lowerDate = lower.getTime();
        this.upperDate = upper.getTime();
    }
    public DateRange ( final double lower, final double upper ) {
        super ( lower, upper );
        this.lowerDate = ( long ) lower;
        this.upperDate = ( long ) upper;
    }
    public DateRange ( final Range other ) {
        this ( other.getLowerBound(), other.getUpperBound() );
    }
    public Date getLowerDate() {
        return new Date ( this.lowerDate );
    }
    public long getLowerMillis() {
        return this.lowerDate;
    }
    public Date getUpperDate() {
        return new Date ( this.upperDate );
    }
    public long getUpperMillis() {
        return this.upperDate;
    }
    @Override
    public String toString() {
        final DateFormat df = DateFormat.getDateTimeInstance();
        return "[" + df.format ( this.getLowerDate() ) + " --> " + df.format ( this.getUpperDate() ) + "]";
    }
}
