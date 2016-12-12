package org.jfree.chart.labels;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import java.util.Date;
import java.text.MessageFormat;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.util.ParamChecks;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
public class AbstractXYItemLabelGenerator implements Cloneable, Serializable {
    private static final long serialVersionUID = 5869744396278660636L;
    private String formatString;
    private NumberFormat xFormat;
    private DateFormat xDateFormat;
    private NumberFormat yFormat;
    private DateFormat yDateFormat;
    private String nullYString;
    protected AbstractXYItemLabelGenerator() {
        this ( "{2}", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    protected AbstractXYItemLabelGenerator ( final String formatString, final NumberFormat xFormat, final NumberFormat yFormat ) {
        this.nullYString = "null";
        ParamChecks.nullNotPermitted ( formatString, "formatString" );
        ParamChecks.nullNotPermitted ( xFormat, "xFormat" );
        ParamChecks.nullNotPermitted ( yFormat, "yFormat" );
        this.formatString = formatString;
        this.xFormat = xFormat;
        this.yFormat = yFormat;
    }
    protected AbstractXYItemLabelGenerator ( final String formatString, final DateFormat xFormat, final NumberFormat yFormat ) {
        this ( formatString, NumberFormat.getInstance(), yFormat );
        this.xDateFormat = xFormat;
    }
    protected AbstractXYItemLabelGenerator ( final String formatString, final NumberFormat xFormat, final DateFormat yFormat ) {
        this ( formatString, xFormat, NumberFormat.getInstance() );
        this.yDateFormat = yFormat;
    }
    protected AbstractXYItemLabelGenerator ( final String formatString, final DateFormat xFormat, final DateFormat yFormat ) {
        this ( formatString, NumberFormat.getInstance(), NumberFormat.getInstance() );
        this.xDateFormat = xFormat;
        this.yDateFormat = yFormat;
    }
    public String getFormatString() {
        return this.formatString;
    }
    public NumberFormat getXFormat() {
        return this.xFormat;
    }
    public DateFormat getXDateFormat() {
        return this.xDateFormat;
    }
    public NumberFormat getYFormat() {
        return this.yFormat;
    }
    public DateFormat getYDateFormat() {
        return this.yDateFormat;
    }
    public String generateLabelString ( final XYDataset dataset, final int series, final int item ) {
        final Object[] items = this.createItemArray ( dataset, series, item );
        final String result = MessageFormat.format ( this.formatString, items );
        return result;
    }
    public String getNullYString() {
        return this.nullYString;
    }
    protected Object[] createItemArray ( final XYDataset dataset, final int series, final int item ) {
        final Object[] result = { dataset.getSeriesKey ( series ).toString(), null, null };
        final double x = dataset.getXValue ( series, item );
        if ( this.xDateFormat != null ) {
            result[1] = this.xDateFormat.format ( new Date ( ( long ) x ) );
        } else {
            result[1] = this.xFormat.format ( x );
        }
        final double y = dataset.getYValue ( series, item );
        if ( Double.isNaN ( y ) && dataset.getY ( series, item ) == null ) {
            result[2] = this.nullYString;
        } else if ( this.yDateFormat != null ) {
            result[2] = this.yDateFormat.format ( new Date ( ( long ) y ) );
        } else {
            result[2] = this.yFormat.format ( y );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractXYItemLabelGenerator ) ) {
            return false;
        }
        final AbstractXYItemLabelGenerator that = ( AbstractXYItemLabelGenerator ) obj;
        return this.formatString.equals ( that.formatString ) && ObjectUtilities.equal ( ( Object ) this.xFormat, ( Object ) that.xFormat ) && ObjectUtilities.equal ( ( Object ) this.xDateFormat, ( Object ) that.xDateFormat ) && ObjectUtilities.equal ( ( Object ) this.yFormat, ( Object ) that.yFormat ) && ObjectUtilities.equal ( ( Object ) this.yDateFormat, ( Object ) that.yDateFormat ) && this.nullYString.equals ( that.nullYString );
    }
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode ( result, this.formatString );
        result = HashUtilities.hashCode ( result, this.xFormat );
        result = HashUtilities.hashCode ( result, this.xDateFormat );
        result = HashUtilities.hashCode ( result, this.yFormat );
        result = HashUtilities.hashCode ( result, this.yDateFormat );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractXYItemLabelGenerator clone = ( AbstractXYItemLabelGenerator ) super.clone();
        if ( this.xFormat != null ) {
            clone.xFormat = ( NumberFormat ) this.xFormat.clone();
        }
        if ( this.yFormat != null ) {
            clone.yFormat = ( NumberFormat ) this.yFormat.clone();
        }
        if ( this.xDateFormat != null ) {
            clone.xDateFormat = ( DateFormat ) this.xDateFormat.clone();
        }
        if ( this.yDateFormat != null ) {
            clone.yDateFormat = ( DateFormat ) this.yDateFormat.clone();
        }
        return clone;
    }
}
