package org.jfree.chart.axis;
import org.jfree.chart.util.ParamChecks;
import java.text.NumberFormat;
import java.io.Serializable;
public class NumberTickUnit extends TickUnit implements Serializable {
    private static final long serialVersionUID = 3849459506627654442L;
    private NumberFormat formatter;
    public NumberTickUnit ( final double size ) {
        this ( size, NumberFormat.getNumberInstance() );
    }
    public NumberTickUnit ( final double size, final NumberFormat formatter ) {
        super ( size );
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.formatter = formatter;
    }
    public NumberTickUnit ( final double size, final NumberFormat formatter, final int minorTickCount ) {
        super ( size, minorTickCount );
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.formatter = formatter;
    }
    @Override
    public String valueToString ( final double value ) {
        return this.formatter.format ( value );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof NumberTickUnit ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final NumberTickUnit that = ( NumberTickUnit ) obj;
        return this.formatter.equals ( that.formatter );
    }
    @Override
    public String toString() {
        return "[size=" + this.valueToString ( this.getSize() ) + "]";
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + ( ( this.formatter != null ) ? this.formatter.hashCode() : 0 );
        return result;
    }
}
