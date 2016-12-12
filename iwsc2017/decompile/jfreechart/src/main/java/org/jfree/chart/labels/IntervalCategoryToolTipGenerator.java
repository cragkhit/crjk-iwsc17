package org.jfree.chart.labels;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
public class IntervalCategoryToolTipGenerator extends StandardCategoryToolTipGenerator {
    private static final long serialVersionUID = -3853824986520333437L;
    public static final String DEFAULT_TOOL_TIP_FORMAT_STRING = "({0}, {1}) = {3} - {4}";
    public IntervalCategoryToolTipGenerator() {
        super ( "({0}, {1}) = {3} - {4}", NumberFormat.getInstance() );
    }
    public IntervalCategoryToolTipGenerator ( final String labelFormat, final NumberFormat formatter ) {
        super ( labelFormat, formatter );
    }
    public IntervalCategoryToolTipGenerator ( final String labelFormat, final DateFormat formatter ) {
        super ( labelFormat, formatter );
    }
    @Override
    protected Object[] createItemArray ( final CategoryDataset dataset, final int row, final int column ) {
        final Object[] result = { dataset.getRowKey ( row ).toString(), dataset.getColumnKey ( column ).toString(), null, null, null };
        final Number value = dataset.getValue ( row, column );
        if ( this.getNumberFormat() != null ) {
            result[2] = this.getNumberFormat().format ( value );
        } else if ( this.getDateFormat() != null ) {
            result[2] = this.getDateFormat().format ( value );
        }
        if ( dataset instanceof IntervalCategoryDataset ) {
            final IntervalCategoryDataset icd = ( IntervalCategoryDataset ) dataset;
            final Number start = icd.getStartValue ( row, column );
            final Number end = icd.getEndValue ( row, column );
            if ( this.getNumberFormat() != null ) {
                result[3] = this.getNumberFormat().format ( start );
                result[4] = this.getNumberFormat().format ( end );
            } else if ( this.getDateFormat() != null ) {
                result[3] = this.getDateFormat().format ( start );
                result[4] = this.getDateFormat().format ( end );
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof IntervalCategoryToolTipGenerator && super.equals ( obj ) );
    }
}
