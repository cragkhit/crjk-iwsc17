package org.jfree.chart.labels;
import java.util.Date;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class IntervalXYToolTipGenerator extends AbstractXYItemLabelGenerator implements XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1} - {2}), ({5} - {6})";
    public IntervalXYToolTipGenerator() {
        this ( "{0}: ({1} - {2}), ({5} - {6})", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    public IntervalXYToolTipGenerator ( final String formatString, final NumberFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public IntervalXYToolTipGenerator ( final String formatString, final DateFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public IntervalXYToolTipGenerator ( final String formatString, final NumberFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public IntervalXYToolTipGenerator ( final String formatString, final DateFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    @Override
    protected Object[] createItemArray ( final XYDataset dataset, final int series, final int item ) {
        IntervalXYDataset intervalDataset = null;
        if ( dataset instanceof IntervalXYDataset ) {
            intervalDataset = ( IntervalXYDataset ) dataset;
        }
        final Object[] result = new Object[7];
        result[0] = dataset.getSeriesKey ( series ).toString();
        double xs;
        double xe;
        final double x = xe = ( xs = dataset.getXValue ( series, item ) );
        double ys;
        double ye;
        final double y = ye = ( ys = dataset.getYValue ( series, item ) );
        if ( intervalDataset != null ) {
            xs = intervalDataset.getStartXValue ( series, item );
            xe = intervalDataset.getEndXValue ( series, item );
            ys = intervalDataset.getStartYValue ( series, item );
            ye = intervalDataset.getEndYValue ( series, item );
        }
        final DateFormat xdf = this.getXDateFormat();
        if ( xdf != null ) {
            result[1] = xdf.format ( new Date ( ( long ) x ) );
            result[2] = xdf.format ( new Date ( ( long ) xs ) );
            result[3] = xdf.format ( new Date ( ( long ) xe ) );
        } else {
            final NumberFormat xnf = this.getXFormat();
            result[1] = xnf.format ( x );
            result[2] = xnf.format ( xs );
            result[3] = xnf.format ( xe );
        }
        final NumberFormat ynf = this.getYFormat();
        final DateFormat ydf = this.getYDateFormat();
        if ( Double.isNaN ( y ) && dataset.getY ( series, item ) == null ) {
            result[4] = this.getNullYString();
        } else if ( ydf != null ) {
            result[4] = ydf.format ( new Date ( ( long ) y ) );
        } else {
            result[4] = ynf.format ( y );
        }
        if ( Double.isNaN ( ys ) && intervalDataset != null && intervalDataset.getStartY ( series, item ) == null ) {
            result[5] = this.getNullYString();
        } else if ( ydf != null ) {
            result[5] = ydf.format ( new Date ( ( long ) ys ) );
        } else {
            result[5] = ynf.format ( ys );
        }
        if ( Double.isNaN ( ye ) && intervalDataset != null && intervalDataset.getEndY ( series, item ) == null ) {
            result[6] = this.getNullYString();
        } else if ( ydf != null ) {
            result[6] = ydf.format ( new Date ( ( long ) ye ) );
        } else {
            result[6] = ynf.format ( ye );
        }
        return result;
    }
    @Override
    public String generateToolTip ( final XYDataset dataset, final int series, final int item ) {
        return this.generateLabelString ( dataset, series, item );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof IntervalXYToolTipGenerator && super.equals ( obj ) );
    }
}
