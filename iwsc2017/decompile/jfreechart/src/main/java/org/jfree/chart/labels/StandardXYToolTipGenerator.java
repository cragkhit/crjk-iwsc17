package org.jfree.chart.labels;
import org.jfree.data.xy.XYDataset;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardXYToolTipGenerator extends AbstractXYItemLabelGenerator implements XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3564164459039540784L;
    public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1}, {2})";
    public static StandardXYToolTipGenerator getTimeSeriesInstance() {
        return new StandardXYToolTipGenerator ( "{0}: ({1}, {2})", DateFormat.getInstance(), NumberFormat.getInstance() );
    }
    public StandardXYToolTipGenerator() {
        this ( "{0}: ({1}, {2})", NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance() );
    }
    public StandardXYToolTipGenerator ( final String formatString, final NumberFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYToolTipGenerator ( final String formatString, final DateFormat xFormat, final NumberFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYToolTipGenerator ( final String formatString, final NumberFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    public StandardXYToolTipGenerator ( final String formatString, final DateFormat xFormat, final DateFormat yFormat ) {
        super ( formatString, xFormat, yFormat );
    }
    @Override
    public String generateToolTip ( final XYDataset dataset, final int series, final int item ) {
        return this.generateLabelString ( dataset, series, item );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof StandardXYToolTipGenerator && super.equals ( obj ) );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
