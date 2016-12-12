package org.jfree.chart.plot;
import org.jfree.chart.event.MarkerChangeEvent;
import java.awt.Stroke;
import java.awt.Paint;
public class ValueMarker extends Marker {
    private double value;
    public ValueMarker ( final double value ) {
        this.value = value;
    }
    public ValueMarker ( final double value, final Paint paint, final Stroke stroke ) {
        this ( value, paint, stroke, paint, stroke, 1.0f );
    }
    public ValueMarker ( final double value, final Paint paint, final Stroke stroke, final Paint outlinePaint, final Stroke outlineStroke, final float alpha ) {
        super ( paint, stroke, outlinePaint, outlineStroke, alpha );
        this.value = value;
    }
    public double getValue() {
        return this.value;
    }
    public void setValue ( final double value ) {
        this.value = value;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof ValueMarker ) ) {
            return false;
        }
        final ValueMarker that = ( ValueMarker ) obj;
        return this.value == that.value;
    }
}
