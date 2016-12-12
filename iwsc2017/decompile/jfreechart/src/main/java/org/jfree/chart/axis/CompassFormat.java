package org.jfree.chart.axis;
import java.text.ParsePosition;
import java.text.FieldPosition;
import org.jfree.chart.util.ParamChecks;
import java.text.NumberFormat;
public class CompassFormat extends NumberFormat {
    public final String[] directions;
    public CompassFormat() {
        this ( "N", "E", "S", "W" );
    }
    public CompassFormat ( final String n, final String e, final String s, final String w ) {
        this ( new String[] { n, n + n + e, n + e, e + n + e, e, e + s + e, s + e, s + s + e, s, s + s + w, s + w, w + s + w, w, w + n + w, n + w, n + n + w } );
    }
    public CompassFormat ( final String[] directions ) {
        ParamChecks.nullNotPermitted ( directions, "directions" );
        if ( directions.length != 16 ) {
            throw new IllegalArgumentException ( "The 'directions' array must contain exactly 16 elements" );
        }
        this.directions = directions;
    }
    public String getDirectionCode ( double direction ) {
        direction %= 360.0;
        if ( direction < 0.0 ) {
            direction += 360.0;
        }
        final int index = ( ( int ) Math.floor ( direction / 11.25 ) + 1 ) / 2;
        return this.directions[index];
    }
    @Override
    public StringBuffer format ( final double number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        return toAppendTo.append ( this.getDirectionCode ( number ) );
    }
    @Override
    public StringBuffer format ( final long number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        return toAppendTo.append ( this.getDirectionCode ( number ) );
    }
    @Override
    public Number parse ( final String source, final ParsePosition parsePosition ) {
        return null;
    }
}
