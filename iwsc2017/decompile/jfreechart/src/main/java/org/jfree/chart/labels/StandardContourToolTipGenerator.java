package org.jfree.chart.labels;
import java.text.FieldPosition;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.jfree.data.contour.ContourDataset;
import java.text.DecimalFormat;
import java.io.Serializable;
public class StandardContourToolTipGenerator implements ContourToolTipGenerator, Serializable {
    private static final long serialVersionUID = -1881659351247502711L;
    private DecimalFormat valueForm;
    public StandardContourToolTipGenerator() {
        this.valueForm = new DecimalFormat ( "##.###" );
    }
    @Override
    public String generateToolTip ( final ContourDataset data, final int item ) {
        final double x = data.getXValue ( 0, item );
        final double y = data.getYValue ( 0, item );
        final double z = data.getZValue ( 0, item );
        String xString;
        if ( data.isDateAxis ( 0 ) ) {
            final SimpleDateFormat formatter = new SimpleDateFormat ( "MM/dd/yyyy hh:mm:ss" );
            StringBuffer strbuf = new StringBuffer();
            strbuf = formatter.format ( new Date ( ( long ) x ), strbuf, new FieldPosition ( 0 ) );
            xString = strbuf.toString();
        } else {
            xString = this.valueForm.format ( x );
        }
        if ( !Double.isNaN ( z ) ) {
            return "X: " + xString + ", Y: " + this.valueForm.format ( y ) + ", Z: " + this.valueForm.format ( z );
        }
        return "X: " + xString + ", Y: " + this.valueForm.format ( y ) + ", Z: no data";
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardContourToolTipGenerator ) ) {
            return false;
        }
        final StandardContourToolTipGenerator that = ( StandardContourToolTipGenerator ) obj;
        return this.valueForm != null && this.valueForm.equals ( that.valueForm );
    }
}
