package org.jfree.chart.labels;
import org.jfree.data.xy.XYDataset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardXYItemLabelGenerator extends AbstractXYItemLabelGenerator implements XYItemLabelGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7807668053171837925L;
    public static final String DEFAULT_ITEM_LABEL_FORMAT = "{2}";
    public StandardXYItemLabelGenerator() {
        this ( "{2}", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    public StandardXYItemLabelGenerator ( final String formatString ) {
        this ( formatString, NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    public StandardXYItemLabelGenerator ( final String formatString, final NumberFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYItemLabelGenerator ( final String formatString, final DateFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYItemLabelGenerator ( final String formatString, final NumberFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYItemLabelGenerator ( final String formatString, final DateFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    @Override
    public String generateLabel ( final XYDataset dataset, final int series, final int item ) {
        return this.generateLabelString ( dataset, series, item );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof StandardXYItemLabelGenerator && super.equals ( obj ) );
    }
}
