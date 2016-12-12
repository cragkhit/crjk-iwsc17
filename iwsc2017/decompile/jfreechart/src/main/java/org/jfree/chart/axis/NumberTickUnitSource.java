package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.Serializable;
public class NumberTickUnitSource implements TickUnitSource, Serializable {
    private boolean integers;
    private int power;
    private int factor;
    private NumberFormat formatter;
    private DecimalFormat dfNeg4;
    private DecimalFormat dfNeg3;
    private DecimalFormat dfNeg2;
    private DecimalFormat dfNeg1;
    private DecimalFormat df0;
    private DecimalFormat df;
    public NumberTickUnitSource() {
        this ( false );
    }
    public NumberTickUnitSource ( final boolean integers ) {
        this ( integers, null );
    }
    public NumberTickUnitSource ( final boolean integers, final NumberFormat formatter ) {
        this.power = 0;
        this.factor = 1;
        this.dfNeg4 = new DecimalFormat ( "0.0000" );
        this.dfNeg3 = new DecimalFormat ( "0.000" );
        this.dfNeg2 = new DecimalFormat ( "0.00" );
        this.dfNeg1 = new DecimalFormat ( "0.0" );
        this.df0 = new DecimalFormat ( "#,##0" );
        this.df = new DecimalFormat ( "#.######E0" );
        this.integers = integers;
        this.formatter = formatter;
        this.power = 0;
        this.factor = 1;
    }
    @Override
    public TickUnit getLargerTickUnit ( final TickUnit unit ) {
        TickUnit t = this.getCeilingTickUnit ( unit );
        if ( t.equals ( unit ) ) {
            this.next();
            t = new NumberTickUnit ( this.getTickSize(), this.getTickLabelFormat(), this.getMinorTickCount() );
        }
        return t;
    }
    @Override
    public TickUnit getCeilingTickUnit ( final TickUnit unit ) {
        return this.getCeilingTickUnit ( unit.getSize() );
    }
    @Override
    public TickUnit getCeilingTickUnit ( final double size ) {
        if ( Double.isInfinite ( size ) ) {
            throw new IllegalArgumentException ( "Must be finite." );
        }
        this.power = ( int ) Math.ceil ( Math.log10 ( size ) );
        if ( this.integers ) {
            this.power = Math.max ( this.power, 0 );
        }
        this.factor = 1;
        for ( boolean done = false; !done; done = true ) {
            done = !this.previous();
            if ( this.getTickSize() < size ) {
                this.next();
            }
        }
        return new NumberTickUnit ( this.getTickSize(), this.getTickLabelFormat(), this.getMinorTickCount() );
    }
    private boolean next() {
        if ( this.factor == 1 ) {
            this.factor = 2;
            return true;
        }
        if ( this.factor == 2 ) {
            this.factor = 5;
            return true;
        }
        if ( this.factor != 5 ) {
            throw new IllegalStateException ( "We should never get here." );
        }
        if ( this.power == 300 ) {
            return false;
        }
        ++this.power;
        this.factor = 1;
        return true;
    }
    private boolean previous() {
        if ( this.factor == 1 ) {
            if ( ( this.integers && this.power == 0 ) || this.power == -300 ) {
                return false;
            }
            this.factor = 5;
            --this.power;
            return true;
        } else {
            if ( this.factor == 2 ) {
                this.factor = 1;
                return true;
            }
            if ( this.factor == 5 ) {
                this.factor = 2;
                return true;
            }
            throw new IllegalStateException ( "We should never get here." );
        }
    }
    private double getTickSize() {
        return this.factor * Math.pow ( 10.0, this.power );
    }
    private NumberFormat getTickLabelFormat() {
        if ( this.formatter != null ) {
            return this.formatter;
        }
        if ( this.power == -4 ) {
            return this.dfNeg4;
        }
        if ( this.power == -3 ) {
            return this.dfNeg3;
        }
        if ( this.power == -2 ) {
            return this.dfNeg2;
        }
        if ( this.power == -1 ) {
            return this.dfNeg1;
        }
        if ( this.power >= 0 && this.power <= 6 ) {
            return this.df0;
        }
        return this.df;
    }
    private int getMinorTickCount() {
        if ( this.factor == 1 ) {
            return 10;
        }
        if ( this.factor == 5 ) {
            return 5;
        }
        return 0;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof NumberTickUnitSource ) ) {
            return false;
        }
        final NumberTickUnitSource that = ( NumberTickUnitSource ) obj;
        return this.integers == that.integers && ObjectUtilities.equal ( ( Object ) this.formatter, ( Object ) that.formatter ) && this.power == that.power && this.factor == that.factor;
    }
}
