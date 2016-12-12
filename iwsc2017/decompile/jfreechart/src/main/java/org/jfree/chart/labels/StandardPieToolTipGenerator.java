package org.jfree.chart.labels;
import org.jfree.data.general.PieDataset;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardPieToolTipGenerator extends AbstractPieItemLabelGenerator implements PieToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 2995304200445733779L;
    public static final String DEFAULT_TOOLTIP_FORMAT = "{0}: ({1}, {2})";
    public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0} = {1}";
    public StandardPieToolTipGenerator() {
        this ( "{0}: ({1}, {2})" );
    }
    public StandardPieToolTipGenerator ( final Locale locale ) {
        this ( "{0}: ({1}, {2})", locale );
    }
    public StandardPieToolTipGenerator ( final String labelFormat ) {
        this ( labelFormat, Locale.getDefault() );
    }
    public StandardPieToolTipGenerator ( final String labelFormat, final Locale locale ) {
        this ( labelFormat, NumberFormat.getNumberInstance ( locale ), NumberFormat.getPercentInstance ( locale ) );
    }
    public StandardPieToolTipGenerator ( final String labelFormat, final NumberFormat numberFormat, final NumberFormat percentFormat ) {
        super ( labelFormat, numberFormat, percentFormat );
    }
    @Override
    public String generateToolTip ( final PieDataset dataset, final Comparable key ) {
        return this.generateSectionLabel ( dataset, key );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
