package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardDialRange extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = 345515648249364904L;
    private int scaleIndex;
    private double lowerBound;
    private double upperBound;
    private transient Paint paint;
    private double innerRadius;
    private double outerRadius;
    public StandardDialRange() {
        this ( 0.0, 100.0, Color.white );
    }
    public StandardDialRange ( final double lower, final double upper, final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.scaleIndex = 0;
        this.lowerBound = lower;
        this.upperBound = upper;
        this.innerRadius = 0.48;
        this.outerRadius = 0.52;
        this.paint = paint;
    }
    public int getScaleIndex() {
        return this.scaleIndex;
    }
    public void setScaleIndex ( final int index ) {
        this.scaleIndex = index;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getLowerBound() {
        return this.lowerBound;
    }
    public void setLowerBound ( final double bound ) {
        if ( bound >= this.upperBound ) {
            throw new IllegalArgumentException ( "Lower bound must be less than upper bound." );
        }
        this.lowerBound = bound;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getUpperBound() {
        return this.upperBound;
    }
    public void setUpperBound ( final double bound ) {
        if ( bound <= this.lowerBound ) {
            throw new IllegalArgumentException ( "Lower bound must be less than upper bound." );
        }
        this.upperBound = bound;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public void setBounds ( final double lower, final double upper ) {
        if ( lower >= upper ) {
            throw new IllegalArgumentException ( "Lower must be less than upper." );
        }
        this.lowerBound = lower;
        this.upperBound = upper;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getInnerRadius() {
        return this.innerRadius;
    }
    public void setInnerRadius ( final double radius ) {
        this.innerRadius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getOuterRadius() {
        return this.outerRadius;
    }
    public void setOuterRadius ( final double radius ) {
        this.outerRadius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean isClippedToWindow() {
        return true;
    }
    @Override
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        final Rectangle2D arcRectInner = DialPlot.rectangleByRadius ( frame, this.innerRadius, this.innerRadius );
        final Rectangle2D arcRectOuter = DialPlot.rectangleByRadius ( frame, this.outerRadius, this.outerRadius );
        final DialScale scale = plot.getScale ( this.scaleIndex );
        if ( scale == null ) {
            throw new RuntimeException ( "No scale for scaleIndex = " + this.scaleIndex );
        }
        final double angleMin = scale.valueToAngle ( this.lowerBound );
        final double angleMax = scale.valueToAngle ( this.upperBound );
        final Arc2D arcInner = new Arc2D.Double ( arcRectInner, angleMin, angleMax - angleMin, 0 );
        final Arc2D arcOuter = new Arc2D.Double ( arcRectOuter, angleMax, angleMin - angleMax, 0 );
        g2.setPaint ( this.paint );
        g2.setStroke ( new BasicStroke ( 2.0f ) );
        g2.draw ( arcInner );
        g2.draw ( arcOuter );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardDialRange ) ) {
            return false;
        }
        final StandardDialRange that = ( StandardDialRange ) obj;
        return this.scaleIndex == that.scaleIndex && this.lowerBound == that.lowerBound && this.upperBound == that.upperBound && PaintUtilities.equal ( this.paint, that.paint ) && this.innerRadius == that.innerRadius && this.outerRadius == that.outerRadius && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits ( this.lowerBound );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.upperBound );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.innerRadius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.outerRadius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
}
