package org.jfree.chart.labels;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import java.util.Date;
import org.jfree.data.xy.XYDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
public class BoxAndWhiskerXYToolTipGenerator extends StandardXYToolTipGenerator implements XYToolTipGenerator, Cloneable, Serializable {
    private static final long serialVersionUID = -2648775791161459710L;
    public static final String DEFAULT_TOOL_TIP_FORMAT = "X: {1} Mean: {2} Median: {3} Min: {4} Max: {5} Q1: {6} Q3: {7} ";
    public BoxAndWhiskerXYToolTipGenerator() {
        super ( "X: {1} Mean: {2} Median: {3} Min: {4} Max: {5} Q1: {6} Q3: {7} ", NumberFormat.getInstance(), NumberFormat.getInstance() );
    }
    public BoxAndWhiskerXYToolTipGenerator ( final String toolTipFormat, final DateFormat dateFormat, final NumberFormat numberFormat ) {
        super ( toolTipFormat, dateFormat, numberFormat );
    }
    @Override
    protected Object[] createItemArray ( final XYDataset dataset, final int series, final int item ) {
        final Object[] result = new Object[8];
        result[0] = dataset.getSeriesKey ( series ).toString();
        final Number x = dataset.getX ( series, item );
        if ( this.getXDateFormat() != null ) {
            result[1] = this.getXDateFormat().format ( new Date ( x.longValue() ) );
        } else {
            result[1] = this.getXFormat().format ( x );
        }
        final NumberFormat formatter = this.getYFormat();
        if ( dataset instanceof BoxAndWhiskerXYDataset ) {
            final BoxAndWhiskerXYDataset d = ( BoxAndWhiskerXYDataset ) dataset;
            result[2] = formatter.format ( d.getMeanValue ( series, item ) );
            result[3] = formatter.format ( d.getMedianValue ( series, item ) );
            result[4] = formatter.format ( d.getMinRegularValue ( series, item ) );
            result[5] = formatter.format ( d.getMaxRegularValue ( series, item ) );
            result[6] = formatter.format ( d.getQ1Value ( series, item ) );
            result[7] = formatter.format ( d.getQ3Value ( series, item ) );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof BoxAndWhiskerXYToolTipGenerator && super.equals ( obj ) );
    }
}
