package org.jfree.chart.labels;
import org.jfree.chart.HashUtilities;
import java.util.Date;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class HighLowItemLabelGenerator implements XYItemLabelGenerator, XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 5617111754832211830L;
    private DateFormat dateFormatter;
    private NumberFormat numberFormatter;
    public HighLowItemLabelGenerator() {
        this ( DateFormat.getInstance(), NumberFormat.getInstance() );
    }
    public HighLowItemLabelGenerator ( final DateFormat dateFormatter, final NumberFormat numberFormatter ) {
        if ( dateFormatter == null ) {
            throw new IllegalArgumentException ( "Null 'dateFormatter' argument." );
        }
        if ( numberFormatter == null ) {
            throw new IllegalArgumentException ( "Null 'numberFormatter' argument." );
        }
        this.dateFormatter = dateFormatter;
        this.numberFormatter = numberFormatter;
    }
    @Override
    public String generateToolTip ( final XYDataset dataset, final int series, final int item ) {
        if ( ! ( dataset instanceof OHLCDataset ) ) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        final OHLCDataset d = ( OHLCDataset ) dataset;
        final Number high = d.getHigh ( series, item );
        final Number low = d.getLow ( series, item );
        final Number open = d.getOpen ( series, item );
        final Number close = d.getClose ( series, item );
        final Number x = d.getX ( series, item );
        sb.append ( d.getSeriesKey ( series ).toString() );
        if ( x != null ) {
            final Date date = new Date ( x.longValue() );
            sb.append ( "--> Date=" ).append ( this.dateFormatter.format ( date ) );
            if ( high != null ) {
                sb.append ( " High=" );
                sb.append ( this.numberFormatter.format ( high.doubleValue() ) );
            }
            if ( low != null ) {
                sb.append ( " Low=" );
                sb.append ( this.numberFormatter.format ( low.doubleValue() ) );
            }
            if ( open != null ) {
                sb.append ( " Open=" );
                sb.append ( this.numberFormatter.format ( open.doubleValue() ) );
            }
            if ( close != null ) {
                sb.append ( " Close=" );
                sb.append ( this.numberFormatter.format ( close.doubleValue() ) );
            }
        }
        return sb.toString();
    }
    @Override
    public String generateLabel ( final XYDataset dataset, final int series, final int category ) {
        return null;
    }
    public Object clone() throws CloneNotSupportedException {
        final HighLowItemLabelGenerator clone = ( HighLowItemLabelGenerator ) super.clone();
        if ( this.dateFormatter != null ) {
            clone.dateFormatter = ( DateFormat ) this.dateFormatter.clone();
        }
        if ( this.numberFormatter != null ) {
            clone.numberFormatter = ( NumberFormat ) this.numberFormatter.clone();
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof HighLowItemLabelGenerator ) ) {
            return false;
        }
        final HighLowItemLabelGenerator generator = ( HighLowItemLabelGenerator ) obj;
        return this.dateFormatter.equals ( generator.dateFormatter ) && this.numberFormatter.equals ( generator.numberFormatter );
    }
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtilities.hashCode ( result, this.dateFormatter );
        result = HashUtilities.hashCode ( result, this.numberFormatter );
        return result;
    }
}
