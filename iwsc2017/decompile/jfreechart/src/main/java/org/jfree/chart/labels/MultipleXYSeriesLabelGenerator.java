package org.jfree.chart.labels;
import org.jfree.chart.HashUtilities;
import java.util.Iterator;
import java.util.Set;
import java.text.MessageFormat;
import org.jfree.data.xy.XYDataset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.jfree.chart.util.ParamChecks;
import java.util.Map;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class MultipleXYSeriesLabelGenerator implements XYSeriesLabelGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 138976236941898560L;
    public static final String DEFAULT_LABEL_FORMAT = "{0}";
    private String formatPattern;
    private String additionalFormatPattern;
    private Map seriesLabelLists;
    public MultipleXYSeriesLabelGenerator() {
        this ( "{0}" );
    }
    public MultipleXYSeriesLabelGenerator ( final String format ) {
        ParamChecks.nullNotPermitted ( format, "format" );
        this.formatPattern = format;
        this.additionalFormatPattern = "\n{0}";
        this.seriesLabelLists = new HashMap();
    }
    public void addSeriesLabel ( final int series, final String label ) {
        final Integer key = new Integer ( series );
        List labelList = this.seriesLabelLists.get ( key );
        if ( labelList == null ) {
            labelList = new ArrayList();
            this.seriesLabelLists.put ( key, labelList );
        }
        labelList.add ( label );
    }
    public void clearSeriesLabels ( final int series ) {
        final Integer key = new Integer ( series );
        this.seriesLabelLists.put ( key, null );
    }
    @Override
    public String generateLabel ( final XYDataset dataset, final int series ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final StringBuilder label = new StringBuilder();
        label.append ( MessageFormat.format ( this.formatPattern, this.createItemArray ( dataset, series ) ) );
        final Integer key = new Integer ( series );
        final List extraLabels = this.seriesLabelLists.get ( key );
        if ( extraLabels != null ) {
            final Object[] temp = { null };
            for ( int i = 0; i < extraLabels.size(); ++i ) {
                temp[0] = extraLabels.get ( i );
                final String labelAddition = MessageFormat.format ( this.additionalFormatPattern, temp );
                label.append ( labelAddition );
            }
        }
        return label.toString();
    }
    protected Object[] createItemArray ( final XYDataset dataset, final int series ) {
        final Object[] result = { dataset.getSeriesKey ( series ).toString() };
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final MultipleXYSeriesLabelGenerator clone = ( MultipleXYSeriesLabelGenerator ) super.clone();
        clone.seriesLabelLists = new HashMap();
        final Set keys = this.seriesLabelLists.keySet();
        for ( final Object key : keys ) {
            Object toAdd;
            final Object entry = toAdd = this.seriesLabelLists.get ( key );
            if ( entry instanceof PublicCloneable ) {
                final PublicCloneable pc = ( PublicCloneable ) entry;
                toAdd = pc.clone();
            }
            clone.seriesLabelLists.put ( key, toAdd );
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof MultipleXYSeriesLabelGenerator ) ) {
            return false;
        }
        final MultipleXYSeriesLabelGenerator that = ( MultipleXYSeriesLabelGenerator ) obj;
        return this.formatPattern.equals ( that.formatPattern ) && this.additionalFormatPattern.equals ( that.additionalFormatPattern ) && this.seriesLabelLists.equals ( that.seriesLabelLists );
    }
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode ( result, this.formatPattern );
        result = HashUtilities.hashCode ( result, this.additionalFormatPattern );
        result = HashUtilities.hashCode ( result, this.seriesLabelLists );
        return result;
    }
}
