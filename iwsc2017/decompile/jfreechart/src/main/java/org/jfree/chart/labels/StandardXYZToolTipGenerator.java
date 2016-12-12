package org.jfree.chart.labels;
import org.jfree.util.ObjectUtilities;
import java.text.MessageFormat;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.chart.util.ParamChecks;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
public class StandardXYZToolTipGenerator extends StandardXYToolTipGenerator implements XYZToolTipGenerator, Serializable {
    private static final long serialVersionUID = -2961577421889473503L;
    public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1}, {2}, {3})";
    private NumberFormat zFormat;
    private DateFormat zDateFormat;
    public StandardXYZToolTipGenerator() {
        this ( "{0}: ({1}, {2}, {3})", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    public StandardXYZToolTipGenerator ( final String formatString, final NumberFormat xFormat, final NumberFormat yFormat, final NumberFormat zFormat ) {
        super ( formatString, xFormat, yFormat );
        ParamChecks.nullNotPermitted ( zFormat, "zFormat" );
        this.zFormat = zFormat;
    }
    public StandardXYZToolTipGenerator ( final String formatString, final DateFormat xFormat, final DateFormat yFormat, final DateFormat zFormat ) {
        super ( formatString, xFormat, yFormat );
        ParamChecks.nullNotPermitted ( zFormat, "zFormat" );
        this.zDateFormat = zFormat;
    }
    public NumberFormat getZFormat() {
        return this.zFormat;
    }
    public DateFormat getZDateFormat() {
        return this.zDateFormat;
    }
    @Override
    public String generateToolTip ( final XYZDataset dataset, final int series, final int item ) {
        return this.generateLabelString ( dataset, series, item );
    }
    @Override
    public String generateLabelString ( final XYDataset dataset, final int series, final int item ) {
        final Object[] items = this.createItemArray ( ( XYZDataset ) dataset, series, item );
        final String result = MessageFormat.format ( this.getFormatString(), items );
        return result;
    }
    protected Object[] createItemArray ( final XYZDataset dataset, final int series, final int item ) {
        final Object[] result = new Object[4];
        result[0] = dataset.getSeriesKey ( series ).toString();
        final Number x = dataset.getX ( series, item );
        final DateFormat xf = this.getXDateFormat();
        if ( xf != null ) {
            result[1] = xf.format ( x );
        } else {
            result[1] = this.getXFormat().format ( x );
        }
        final Number y = dataset.getY ( series, item );
        final DateFormat yf = this.getYDateFormat();
        if ( yf != null ) {
            result[2] = yf.format ( y );
        } else {
            result[2] = this.getYFormat().format ( y );
        }
        final Number z = dataset.getZ ( series, item );
        if ( this.zDateFormat != null ) {
            result[3] = this.zDateFormat.format ( z );
        } else {
            result[3] = this.zFormat.format ( z );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardXYZToolTipGenerator ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final StandardXYZToolTipGenerator that = ( StandardXYZToolTipGenerator ) obj;
        return ObjectUtilities.equal ( ( Object ) this.zFormat, ( Object ) that.zFormat ) && ObjectUtilities.equal ( ( Object ) this.zDateFormat, ( Object ) that.zDateFormat );
    }
}
