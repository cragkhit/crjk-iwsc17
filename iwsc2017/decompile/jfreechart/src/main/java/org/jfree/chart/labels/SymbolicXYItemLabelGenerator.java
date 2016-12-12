package org.jfree.chart.labels;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XisSymbolic;
import org.jfree.data.xy.YisSymbolic;
import org.jfree.data.xy.XYDataset;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class SymbolicXYItemLabelGenerator implements XYItemLabelGenerator, XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3963400354475494395L;
    @Override
    public String generateToolTip ( final XYDataset data, final int series, final int item ) {
        String yStr;
        if ( data instanceof YisSymbolic ) {
            yStr = ( ( YisSymbolic ) data ).getYSymbolicValue ( series, item );
        } else {
            final double y = data.getYValue ( series, item );
            yStr = Double.toString ( round ( y, 2 ) );
        }
        String xStr;
        if ( data instanceof XisSymbolic ) {
            xStr = ( ( XisSymbolic ) data ).getXSymbolicValue ( series, item );
        } else if ( data instanceof TimeSeriesCollection ) {
            final RegularTimePeriod p = ( ( TimeSeriesCollection ) data ).getSeries ( series ).getTimePeriod ( item );
            xStr = p.toString();
        } else {
            final double x = data.getXValue ( series, item );
            xStr = Double.toString ( round ( x, 2 ) );
        }
        return "X: " + xStr + ", Y: " + yStr;
    }
    @Override
    public String generateLabel ( final XYDataset dataset, final int series, final int category ) {
        return null;
    }
    private static double round ( final double value, final int nb ) {
        if ( nb <= 0 ) {
            return Math.floor ( value + 0.5 );
        }
        final double p = Math.pow ( 10.0, nb );
        final double tempval = Math.floor ( value * p + 0.5 );
        return tempval / p;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || obj instanceof SymbolicXYItemLabelGenerator;
    }
    @Override
    public int hashCode() {
        final int result = 127;
        return result;
    }
}
