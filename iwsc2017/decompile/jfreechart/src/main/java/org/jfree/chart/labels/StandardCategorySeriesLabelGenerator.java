package org.jfree.chart.labels;
import org.jfree.chart.HashUtilities;
import java.text.MessageFormat;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardCategorySeriesLabelGenerator implements CategorySeriesLabelGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 4630760091523940820L;
    public static final String DEFAULT_LABEL_FORMAT = "{0}";
    private String formatPattern;
    public StandardCategorySeriesLabelGenerator() {
        this ( "{0}" );
    }
    public StandardCategorySeriesLabelGenerator ( final String format ) {
        ParamChecks.nullNotPermitted ( format, "format" );
        this.formatPattern = format;
    }
    @Override
    public String generateLabel ( final CategoryDataset dataset, final int series ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final String label = MessageFormat.format ( this.formatPattern, this.createItemArray ( dataset, series ) );
        return label;
    }
    protected Object[] createItemArray ( final CategoryDataset dataset, final int series ) {
        final Object[] result = { dataset.getRowKey ( series ).toString() };
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardCategorySeriesLabelGenerator ) ) {
            return false;
        }
        final StandardCategorySeriesLabelGenerator that = ( StandardCategorySeriesLabelGenerator ) obj;
        return this.formatPattern.equals ( that.formatPattern );
    }
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode ( result, this.formatPattern );
        return result;
    }
}
