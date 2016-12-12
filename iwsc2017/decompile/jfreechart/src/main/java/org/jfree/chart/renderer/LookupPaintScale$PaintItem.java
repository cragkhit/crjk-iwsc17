package org.jfree.chart.renderer;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Paint;
import java.io.Serializable;
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
