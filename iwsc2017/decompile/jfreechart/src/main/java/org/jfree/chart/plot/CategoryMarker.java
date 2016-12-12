package org.jfree.chart.plot;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.LengthAdjustmentType;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.Serializable;
public class CategoryMarker extends Marker implements Cloneable, Serializable {
    private Comparable key;
    private boolean drawAsLine;
    public CategoryMarker ( final Comparable key ) {
        this ( key, Color.gray, new BasicStroke ( 1.0f ) );
    }
    public CategoryMarker ( final Comparable key, final Paint paint, final Stroke stroke ) {
        this ( key, paint, stroke, paint, stroke, 1.0f );
    }
    public CategoryMarker ( final Comparable key, final Paint paint, final Stroke stroke, final Paint outlinePaint, final Stroke outlineStroke, final float alpha ) {
        super ( paint, stroke, outlinePaint, outlineStroke, alpha );
        this.drawAsLine = false;
        this.key = key;
        this.setLabelOffsetType ( LengthAdjustmentType.EXPAND );
    }
    public Comparable getKey() {
        return this.key;
    }
    public void setKey ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    public boolean getDrawAsLine() {
        return this.drawAsLine;
    }
    public void setDrawAsLine ( final boolean drawAsLine ) {
        this.drawAsLine = drawAsLine;
        this.notifyListeners ( new MarkerChangeEvent ( this ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( ! ( obj instanceof CategoryMarker ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final CategoryMarker that = ( CategoryMarker ) obj;
        return this.key.equals ( that.key ) && this.drawAsLine == that.drawAsLine;
    }
}
