package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.TextAnchor;
import java.util.Date;
public class DateTick extends ValueTick {
    private Date date;
    public DateTick ( final Date date, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        this ( TickType.MAJOR, date, label, textAnchor, rotationAnchor, angle );
    }
    public DateTick ( final TickType tickType, final Date date, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( tickType, date.getTime(), label, textAnchor, rotationAnchor, angle );
        ParamChecks.nullNotPermitted ( tickType, "tickType" );
        this.date = date;
    }
    public Date getDate() {
        return this.date;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DateTick ) ) {
            return false;
        }
        final DateTick that = ( DateTick ) obj;
        return ObjectUtilities.equal ( ( Object ) this.date, ( Object ) that.date ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        return this.date.hashCode();
    }
}
