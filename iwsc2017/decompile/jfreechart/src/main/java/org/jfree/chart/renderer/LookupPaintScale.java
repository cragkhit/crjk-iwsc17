package org.jfree.chart.renderer;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import java.util.Collection;
import org.jfree.util.PaintUtilities;
import java.util.Collections;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.util.List;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class LookupPaintScale implements PaintScale, PublicCloneable, Serializable {
    static final long serialVersionUID = -5239384246251042006L;
    private double lowerBound;
    private double upperBound;
    private transient Paint defaultPaint;
    private List lookupTable;
    public LookupPaintScale() {
        this ( 0.0, 1.0, Color.lightGray );
    }
    public LookupPaintScale ( final double lowerBound, final double upperBound, final Paint defaultPaint ) {
        if ( lowerBound >= upperBound ) {
            throw new IllegalArgumentException ( "Requires lowerBound < upperBound." );
        }
        ParamChecks.nullNotPermitted ( defaultPaint, "defaultPaint" );
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.defaultPaint = defaultPaint;
        this.lookupTable = new ArrayList();
    }
    public Paint getDefaultPaint() {
        return this.defaultPaint;
    }
    @Override
    public double getLowerBound() {
        return this.lowerBound;
    }
    @Override
    public double getUpperBound() {
        return this.upperBound;
    }
    public void add ( final Number value, final Paint paint ) {
        this.add ( value.doubleValue(), paint );
    }
    public void add ( final double value, final Paint paint ) {
        final PaintItem item = new PaintItem ( value, paint );
        final int index = Collections.binarySearch ( this.lookupTable, item );
        if ( index >= 0 ) {
            this.lookupTable.set ( index, item );
        } else {
            this.lookupTable.add ( - ( index + 1 ), item );
        }
    }
    @Override
    public Paint getPaint ( final double value ) {
        if ( value < this.lowerBound ) {
            return this.defaultPaint;
        }
        if ( value > this.upperBound ) {
            return this.defaultPaint;
        }
        final int count = this.lookupTable.size();
        if ( count == 0 ) {
            return this.defaultPaint;
        }
        PaintItem item = this.lookupTable.get ( 0 );
        if ( value < item.value ) {
            return this.defaultPaint;
        }
        int low = 0;
        int high = this.lookupTable.size() - 1;
        while ( high - low > 1 ) {
            final int current = ( low + high ) / 2;
            item = this.lookupTable.get ( current );
            if ( value >= item.value ) {
                low = current;
            } else {
                high = current;
            }
        }
        if ( high > low ) {
            item = this.lookupTable.get ( high );
            if ( value < item.value ) {
                item = this.lookupTable.get ( low );
            }
        }
        return ( item != null ) ? item.paint : this.defaultPaint;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LookupPaintScale ) ) {
            return false;
        }
        final LookupPaintScale that = ( LookupPaintScale ) obj;
        return this.lowerBound == that.lowerBound && this.upperBound == that.upperBound && PaintUtilities.equal ( this.defaultPaint, that.defaultPaint ) && this.lookupTable.equals ( that.lookupTable );
    }
    public Object clone() throws CloneNotSupportedException {
        final LookupPaintScale clone = ( LookupPaintScale ) super.clone();
        clone.lookupTable = new ArrayList ( this.lookupTable );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.defaultPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.defaultPaint = SerialUtilities.readPaint ( stream );
    }
    static class PaintItem implements Comparable, Serializable {
        static final long serialVersionUID = 698920578512361570L;
        double value;
        transient Paint paint;
        public PaintItem ( final double value, final Paint paint ) {
            this.value = value;
            this.paint = paint;
        }
        @Override
        public int compareTo ( final Object obj ) {
            final PaintItem that = ( PaintItem ) obj;
            final double d1 = this.value;
            final double d2 = that.value;
            if ( d1 > d2 ) {
                return 1;
            }
            if ( d1 < d2 ) {
                return -1;
            }
            return 0;
        }
        @Override
        public boolean equals ( final Object obj ) {
            if ( obj == this ) {
                return true;
            }
            if ( ! ( obj instanceof PaintItem ) ) {
                return false;
            }
            final PaintItem that = ( PaintItem ) obj;
            return this.value == that.value && PaintUtilities.equal ( this.paint, that.paint );
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
}
