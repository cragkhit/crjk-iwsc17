package org.jfree.chart.plot;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.ui.LengthAdjustmentType;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.ui.GradientPaintTransformer;
import java.io.Serializable;
public class IntervalMarker extends Marker implements Cloneable, Serializable {
    private static final long serialVersionUID = -1762344775267627916L;
    private double startValue;
    private double endValue;
    private GradientPaintTransformer gradientPaintTransformer;
    public IntervalMarker ( final double start, final double end ) {
        this ( start, end, Color.gray, new BasicStroke ( 0.5f ), Color.gray, new BasicStroke ( 0.5f ), 0.8f );
    }
    public IntervalMarker ( final double start, final double end, final Paint paint ) {
        this ( start, end, paint, new BasicStroke ( 0.5f ), null, null, 0.8f );
    }
    public IntervalMarker ( final double start, final double end, final Paint paint, final Stroke stroke, final Paint outlinePaint, final Stroke outlineStroke, final float alpha ) {
        super ( paint, stroke, outlinePaint, outlineStroke, alpha );
        this.startValue = start;
        this.endValue = end;
        this.gradientPaintTransformer = null;
        this.setLabelOffsetType ( LengthAdjustmentType.CONTRACT );
    }
    public double getStartValue() {
        return this.startValue;
    }
    public void setStartValue ( final double value ) {
        this.startValue = value;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    public double getEndValue() {
        return this.endValue;
    }
    public void setEndValue ( final double value ) {
        this.endValue = value;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }
    public void setGradientPaintTransformer ( final GradientPaintTransformer transformer ) {
        this.gradientPaintTransformer = transformer;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof IntervalMarker ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final IntervalMarker that = ( IntervalMarker ) obj;
        return this.startValue == that.startValue && this.endValue == that.endValue && ObjectUtilities.equal ( ( Object ) this.gradientPaintTransformer, ( Object ) that.gradientPaintTransformer );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
