package org.jfree.chart.labels;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import java.text.NumberFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class BoxAndWhiskerToolTipGenerator extends StandardCategoryToolTipGenerator implements CategoryToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -6076837753823076334L;
    public static final String DEFAULT_TOOL_TIP_FORMAT = "X: {1} Mean: {2} Median: {3} Min: {4} Max: {5} Q1: {6} Q3: {7} ";
    public BoxAndWhiskerToolTipGenerator() {
        super ( "X: {1} Mean: {2} Median: {3} Min: {4} Max: {5} Q1: {6} Q3: {7} ", NumberFormat.getInstance() );
    }
    public BoxAndWhiskerToolTipGenerator ( final String format, final NumberFormat formatter ) {
        super ( format, formatter );
    }
    protected Object[] createItemArray ( final CategoryDataset dataset, final int series, final int item ) {
        final Object[] result = new Object[8];
        result[0] = dataset.getRowKey ( series );
        final Number y = dataset.getValue ( series, item );
        final NumberFormat formatter = this.getNumberFormat();
        result[1] = formatter.format ( y );
        if ( dataset instanceof BoxAndWhiskerCategoryDataset ) {
            final BoxAndWhiskerCategoryDataset d = ( BoxAndWhiskerCategoryDataset ) dataset;
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
        return obj == this || ( obj instanceof BoxAndWhiskerToolTipGenerator && super.equals ( obj ) );
    }
}
