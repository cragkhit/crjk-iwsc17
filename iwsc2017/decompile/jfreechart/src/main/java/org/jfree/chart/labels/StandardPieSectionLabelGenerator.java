package org.jfree.chart.labels;
import org.jfree.data.general.PieDataset;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.Map;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardPieSectionLabelGenerator extends AbstractPieItemLabelGenerator implements PieSectionLabelGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3064190563760203668L;
    public static final String DEFAULT_SECTION_LABEL_FORMAT = "{0}";
    private Map attributedLabels;
    public StandardPieSectionLabelGenerator() {
        this ( "{0}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance() );
    }
    public StandardPieSectionLabelGenerator ( final Locale locale ) {
        this ( "{0}", locale );
    }
    public StandardPieSectionLabelGenerator ( final String labelFormat ) {
        this ( labelFormat, NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance() );
    }
    public StandardPieSectionLabelGenerator ( final String labelFormat, final Locale locale ) {
        this ( labelFormat, NumberFormat.getNumberInstance ( locale ), NumberFormat.getPercentInstance ( locale ) );
    }
    public StandardPieSectionLabelGenerator ( final String labelFormat, final NumberFormat numberFormat, final NumberFormat percentFormat ) {
        super ( labelFormat, numberFormat, percentFormat );
        this.attributedLabels = new HashMap();
    }
    public AttributedString getAttributedLabel ( final int section ) {
        return this.attributedLabels.get ( section );
    }
    public void setAttributedLabel ( final int section, final AttributedString label ) {
        this.attributedLabels.put ( section, label );
    }
    @Override
    public String generateSectionLabel ( final PieDataset dataset, final Comparable key ) {
        return super.generateSectionLabel ( dataset, key );
    }
    @Override
    public AttributedString generateAttributedSectionLabel ( final PieDataset dataset, final Comparable key ) {
        return this.getAttributedLabel ( dataset.getIndex ( key ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardPieSectionLabelGenerator ) ) {
            return false;
        }
        final StandardPieSectionLabelGenerator that = ( StandardPieSectionLabelGenerator ) obj;
        return this.attributedLabels.equals ( that.attributedLabels ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final StandardPieSectionLabelGenerator clone = ( StandardPieSectionLabelGenerator ) super.clone();
        ( clone.attributedLabels = new HashMap() ).putAll ( this.attributedLabels );
        return clone;
    }
}
