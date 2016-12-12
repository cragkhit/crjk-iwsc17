package org.jfree.chart.axis;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.data.Range;
public class ModuloAxis extends NumberAxis {
    private Range fixedRange;
    private double displayStart;
    private double displayEnd;
    public ModuloAxis ( final String label, final Range fixedRange ) {
        super ( label );
        this.fixedRange = fixedRange;
        this.displayStart = 270.0;
        this.displayEnd = 90.0;
    }
    public double getDisplayStart() {
        return this.displayStart;
    }
    public double getDisplayEnd() {
        return this.displayEnd;
    }
    public void setDisplayRange ( final double start, final double end ) {
        this.displayStart = this.mapValueToFixedRange ( start );
        this.displayEnd = this.mapValueToFixedRange ( end );
        if ( this.displayStart < this.displayEnd ) {
            this.setRange ( this.displayStart, this.displayEnd );
        } else {
            this.setRange ( this.displayStart, this.fixedRange.getUpperBound() + ( this.displayEnd - this.fixedRange.getLowerBound() ) );
        }
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    @Override
    protected void autoAdjustRange() {
        this.setRange ( this.fixedRange, false, false );
    }
    @Override
    public double valueToJava2D ( final double value, final Rectangle2D area, final RectangleEdge edge ) {
        final double v = this.mapValueToFixedRange ( value );
        double result;
        if ( this.displayStart < this.displayEnd ) {
            result = this.trans ( v, area, edge );
        } else {
            final double cutoff = ( this.displayStart + this.displayEnd ) / 2.0;
            final double length1 = this.fixedRange.getUpperBound() - this.displayStart;
            final double length2 = this.displayEnd - this.fixedRange.getLowerBound();
            if ( v > cutoff ) {
                result = this.transStart ( v, area, edge, length1, length2 );
            } else {
                result = this.transEnd ( v, area, edge, length1, length2 );
            }
        }
        return result;
    }
    private double trans ( final double value, final Rectangle2D area, final RectangleEdge edge ) {
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getX() + area.getWidth();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            min = area.getMaxY();
            max = area.getMaxY() - area.getHeight();
        }
        if ( this.isInverted() ) {
            return max - ( value - this.displayStart ) / ( this.displayEnd - this.displayStart ) * ( max - min );
        }
        return min + ( value - this.displayStart ) / ( this.displayEnd - this.displayStart ) * ( max - min );
    }
    private double transStart ( final double value, final Rectangle2D area, final RectangleEdge edge, final double length1, final double length2 ) {
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getX() + area.getWidth() * length1 / ( length1 + length2 );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            min = area.getMaxY();
            max = area.getMaxY() - area.getHeight() * length1 / ( length1 + length2 );
        }
        if ( this.isInverted() ) {
            return max - ( value - this.displayStart ) / ( this.fixedRange.getUpperBound() - this.displayStart ) * ( max - min );
        }
        return min + ( value - this.displayStart ) / ( this.fixedRange.getUpperBound() - this.displayStart ) * ( max - min );
    }
    private double transEnd ( final double value, final Rectangle2D area, final RectangleEdge edge, final double length1, final double length2 ) {
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            max = area.getMaxX();
            min = area.getMaxX() - area.getWidth() * length2 / ( length1 + length2 );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            max = area.getMinY();
            min = area.getMinY() + area.getHeight() * length2 / ( length1 + length2 );
        }
        if ( this.isInverted() ) {
            return max - ( value - this.fixedRange.getLowerBound() ) / ( this.displayEnd - this.fixedRange.getLowerBound() ) * ( max - min );
        }
        return min + ( value - this.fixedRange.getLowerBound() ) / ( this.displayEnd - this.fixedRange.getLowerBound() ) * ( max - min );
    }
    private double mapValueToFixedRange ( final double value ) {
        final double lower = this.fixedRange.getLowerBound();
        final double length = this.fixedRange.getLength();
        if ( value < lower ) {
            return lower + length + ( value - lower ) % length;
        }
        return lower + ( value - lower ) % length;
    }
    @Override
    public double java2DToValue ( final double java2DValue, final Rectangle2D area, final RectangleEdge edge ) {
        double result = 0.0;
        if ( this.displayStart < this.displayEnd ) {
            result = super.java2DToValue ( java2DValue, area, edge );
        }
        return result;
    }
    private double getDisplayLength() {
        if ( this.displayStart < this.displayEnd ) {
            return this.displayEnd - this.displayStart;
        }
        return this.fixedRange.getUpperBound() - this.displayStart + ( this.displayEnd - this.fixedRange.getLowerBound() );
    }
    private double getDisplayCentralValue() {
        return this.mapValueToFixedRange ( this.displayStart + this.getDisplayLength() / 2.0 );
    }
    @Override
    public void resizeRange ( final double percent ) {
        this.resizeRange ( percent, this.getDisplayCentralValue() );
    }
    @Override
    public void resizeRange ( final double percent, final double anchorValue ) {
        if ( percent > 0.0 ) {
            final double halfLength = this.getDisplayLength() * percent / 2.0;
            this.setDisplayRange ( anchorValue - halfLength, anchorValue + halfLength );
        } else {
            this.setAutoRange ( true );
        }
    }
    @Override
    public double lengthToJava2D ( final double length, final Rectangle2D area, final RectangleEdge edge ) {
        double axisLength = 0.0;
        if ( this.displayEnd > this.displayStart ) {
            axisLength = this.displayEnd - this.displayStart;
        } else {
            axisLength = this.fixedRange.getUpperBound() - this.displayStart + ( this.displayEnd - this.fixedRange.getLowerBound() );
        }
        double areaLength;
        if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            areaLength = area.getHeight();
        } else {
            areaLength = area.getWidth();
        }
        return length / axisLength * areaLength;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ModuloAxis ) ) {
            return false;
        }
        final ModuloAxis that = ( ModuloAxis ) obj;
        return this.displayStart == that.displayStart && this.displayEnd == that.displayEnd && this.fixedRange.equals ( that.fixedRange ) && super.equals ( obj );
    }
}
