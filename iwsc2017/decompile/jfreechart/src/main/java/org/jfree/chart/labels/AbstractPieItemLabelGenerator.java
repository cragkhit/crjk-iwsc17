package org.jfree.chart.labels;
import org.jfree.chart.HashUtilities;
import java.text.MessageFormat;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.chart.util.ParamChecks;
import java.text.NumberFormat;
import java.io.Serializable;
public class AbstractPieItemLabelGenerator implements Serializable {
    private static final long serialVersionUID = 7347703325267846275L;
    private String labelFormat;
    private NumberFormat numberFormat;
    private NumberFormat percentFormat;
    protected AbstractPieItemLabelGenerator ( final String labelFormat, final NumberFormat numberFormat, final NumberFormat percentFormat ) {
        ParamChecks.nullNotPermitted ( labelFormat, "labelFormat" );
        ParamChecks.nullNotPermitted ( numberFormat, "numberFormat" );
        ParamChecks.nullNotPermitted ( percentFormat, "percentFormat" );
        this.labelFormat = labelFormat;
        this.numberFormat = numberFormat;
        this.percentFormat = percentFormat;
    }
    public String getLabelFormat() {
        return this.labelFormat;
    }
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }
    public NumberFormat getPercentFormat() {
        return this.percentFormat;
    }
    protected Object[] createItemArray ( final PieDataset dataset, final Comparable key ) {
        final Object[] result = new Object[4];
        final double total = DatasetUtilities.calculatePieDatasetTotal ( dataset );
        result[0] = key.toString();
        final Number value = dataset.getValue ( key );
        if ( value != null ) {
            result[1] = this.numberFormat.format ( value );
        } else {
            result[1] = "null";
        }
        double percent = 0.0;
        if ( value != null ) {
            final double v = value.doubleValue();
            if ( v > 0.0 ) {
                percent = v / total;
            }
        }
        result[2] = this.percentFormat.format ( percent );
        result[3] = this.numberFormat.format ( total );
        return result;
    }
    protected String generateSectionLabel ( final PieDataset dataset, final Comparable key ) {
        String result = null;
        if ( dataset != null ) {
            final Object[] items = this.createItemArray ( dataset, key );
            result = MessageFormat.format ( this.labelFormat, items );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractPieItemLabelGenerator ) ) {
            return false;
        }
        final AbstractPieItemLabelGenerator that = ( AbstractPieItemLabelGenerator ) obj;
        return this.labelFormat.equals ( that.labelFormat ) && this.numberFormat.equals ( that.numberFormat ) && this.percentFormat.equals ( that.percentFormat );
    }
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode ( result, this.labelFormat );
        result = HashUtilities.hashCode ( result, this.numberFormat );
        result = HashUtilities.hashCode ( result, this.percentFormat );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractPieItemLabelGenerator clone = ( AbstractPieItemLabelGenerator ) super.clone();
        if ( this.numberFormat != null ) {
            clone.numberFormat = ( NumberFormat ) this.numberFormat.clone();
        }
        if ( this.percentFormat != null ) {
            clone.percentFormat = ( NumberFormat ) this.percentFormat.clone();
        }
        return clone;
    }
}
