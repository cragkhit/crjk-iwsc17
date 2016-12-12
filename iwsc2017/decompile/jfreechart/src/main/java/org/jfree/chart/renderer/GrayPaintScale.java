package org.jfree.chart.renderer;
import org.jfree.chart.HashUtilities;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class GrayPaintScale implements PaintScale, PublicCloneable, Serializable {
    private double lowerBound;
    private double upperBound;
    private int alpha;
    public GrayPaintScale() {
        this ( 0.0, 1.0 );
    }
    public GrayPaintScale ( final double lowerBound, final double upperBound ) {
        this ( lowerBound, upperBound, 255 );
    }
    public GrayPaintScale ( final double lowerBound, final double upperBound, final int alpha ) {
        if ( lowerBound >= upperBound ) {
            throw new IllegalArgumentException ( "Requires lowerBound < upperBound." );
        }
        if ( alpha < 0 || alpha > 255 ) {
            throw new IllegalArgumentException ( "Requires alpha in the range 0 to 255." );
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.alpha = alpha;
    }
    @Override
    public double getLowerBound() {
        return this.lowerBound;
    }
    @Override
    public double getUpperBound() {
        return this.upperBound;
    }
    public int getAlpha() {
        return this.alpha;
    }
    @Override
    public Paint getPaint ( final double value ) {
        double v = Math.max ( value, this.lowerBound );
        v = Math.min ( v, this.upperBound );
        final int g = ( int ) ( ( v - this.lowerBound ) / ( this.upperBound - this.lowerBound ) * 255.0 );
        return new Color ( g, g, g, this.alpha );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof GrayPaintScale ) ) {
            return false;
        }
        final GrayPaintScale that = ( GrayPaintScale ) obj;
        return this.lowerBound == that.lowerBound && this.upperBound == that.upperBound && this.alpha == that.alpha;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = HashUtilities.hashCode ( hash, this.lowerBound );
        hash = HashUtilities.hashCode ( hash, this.upperBound );
        hash = 43 * hash + this.alpha;
        return hash;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
