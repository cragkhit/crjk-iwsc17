

package org.jfree.chart.axis;

import java.util.Date;

import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;


public class DateTick extends ValueTick {


    private Date date;


    public DateTick ( Date date, String label,
                      TextAnchor textAnchor, TextAnchor rotationAnchor,
                      double angle ) {
        this ( TickType.MAJOR, date, label, textAnchor, rotationAnchor, angle );
    }


    public DateTick ( TickType tickType, Date date, String label,
                      TextAnchor textAnchor, TextAnchor rotationAnchor,
                      double angle ) {
        super ( tickType, date.getTime(), label, textAnchor, rotationAnchor,
                angle );
        ParamChecks.nullNotPermitted ( tickType, "tickType" );
        this.date = date;
    }


    public Date getDate() {
        return this.date;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DateTick ) ) {
            return false;
        }
        DateTick that = ( DateTick ) obj;
        if ( !ObjectUtilities.equal ( this.date, that.date ) ) {
            return false;
        }
        return super.equals ( obj );
    }


    @Override
    public int hashCode() {
        return this.date.hashCode();
    }

}
