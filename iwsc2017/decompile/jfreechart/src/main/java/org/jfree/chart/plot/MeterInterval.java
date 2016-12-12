package org.jfree.chart.plot;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.data.Range;
import java.io.Serializable;
public class MeterInterval implements Serializable {
    private static final long serialVersionUID = 1530982090622488257L;
    private String label;
    private Range range;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    private transient Paint backgroundPaint;
    public MeterInterval ( final String label, final Range range ) {
        this ( label, range, Color.yellow, new BasicStroke ( 2.0f ), null );
    }
    public MeterInterval ( final String label, final Range range, final Paint outlinePaint, final Stroke outlineStroke, final Paint backgroundPaint ) {
        ParamChecks.nullNotPermitted ( label, "label" );
        ParamChecks.nullNotPermitted ( range, "range" );
        this.label = label;
        this.range = range;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.backgroundPaint = backgroundPaint;
    }
    public String getLabel() {
        return this.label;
    }
    public Range getRange() {
        return this.range;
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof MeterInterval ) ) {
            return false;
        }
        final MeterInterval that = ( MeterInterval ) obj;
        return this.label.equals ( that.label ) && this.range.equals ( that.range ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && ObjectUtilities.equal ( ( Object ) this.outlineStroke, ( Object ) that.outlineStroke ) && PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
    }
}
