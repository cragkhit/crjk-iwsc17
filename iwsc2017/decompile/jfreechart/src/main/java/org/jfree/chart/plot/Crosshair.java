package org.jfree.chart.plot;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import java.beans.PropertyChangeListener;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.awt.Font;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.ui.RectangleAnchor;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class Crosshair implements Cloneable, PublicCloneable, Serializable {
    private boolean visible;
    private double value;
    private transient Paint paint;
    private transient Stroke stroke;
    private boolean labelVisible;
    private RectangleAnchor labelAnchor;
    private CrosshairLabelGenerator labelGenerator;
    private double labelXOffset;
    private double labelYOffset;
    private Font labelFont;
    private transient Paint labelPaint;
    private transient Paint labelBackgroundPaint;
    private boolean labelOutlineVisible;
    private transient Stroke labelOutlineStroke;
    private transient Paint labelOutlinePaint;
    private transient PropertyChangeSupport pcs;
    public Crosshair() {
        this ( 0.0 );
    }
    public Crosshair ( final double value ) {
        this ( value, Color.black, new BasicStroke ( 1.0f ) );
    }
    public Crosshair ( final double value, final Paint paint, final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.visible = true;
        this.value = value;
        this.paint = paint;
        this.stroke = stroke;
        this.labelVisible = false;
        this.labelGenerator = new StandardCrosshairLabelGenerator();
        this.labelAnchor = RectangleAnchor.BOTTOM_LEFT;
        this.labelXOffset = 3.0;
        this.labelYOffset = 3.0;
        this.labelFont = new Font ( "Tahoma", 0, 12 );
        this.labelPaint = Color.black;
        this.labelBackgroundPaint = new Color ( 0, 0, 255, 63 );
        this.labelOutlineVisible = true;
        this.labelOutlinePaint = Color.black;
        this.labelOutlineStroke = new BasicStroke ( 0.5f );
        this.pcs = new PropertyChangeSupport ( this );
    }
    public boolean isVisible() {
        return this.visible;
    }
    public void setVisible ( final boolean visible ) {
        final boolean old = this.visible;
        this.visible = visible;
        this.pcs.firePropertyChange ( "visible", old, visible );
    }
    public double getValue() {
        return this.value;
    }
    public void setValue ( final double value ) {
        final Double oldValue = new Double ( this.value );
        this.value = value;
        this.pcs.firePropertyChange ( "value", oldValue, new Double ( value ) );
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        final Paint old = this.paint;
        this.paint = paint;
        this.pcs.firePropertyChange ( "paint", old, paint );
    }
    public Stroke getStroke() {
        return this.stroke;
    }
    public void setStroke ( final Stroke stroke ) {
        final Stroke old = this.stroke;
        this.stroke = stroke;
        this.pcs.firePropertyChange ( "stroke", old, stroke );
    }
    public boolean isLabelVisible() {
        return this.labelVisible;
    }
    public void setLabelVisible ( final boolean visible ) {
        final boolean old = this.labelVisible;
        this.labelVisible = visible;
        this.pcs.firePropertyChange ( "labelVisible", old, visible );
    }
    public CrosshairLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }
    public void setLabelGenerator ( final CrosshairLabelGenerator generator ) {
        ParamChecks.nullNotPermitted ( generator, "generator" );
        final CrosshairLabelGenerator old = this.labelGenerator;
        this.labelGenerator = generator;
        this.pcs.firePropertyChange ( "labelGenerator", old, generator );
    }
    public RectangleAnchor getLabelAnchor() {
        return this.labelAnchor;
    }
    public void setLabelAnchor ( final RectangleAnchor anchor ) {
        final RectangleAnchor old = this.labelAnchor;
        this.labelAnchor = anchor;
        this.pcs.firePropertyChange ( "labelAnchor", old, anchor );
    }
    public double getLabelXOffset() {
        return this.labelXOffset;
    }
    public void setLabelXOffset ( final double offset ) {
        final Double old = new Double ( this.labelXOffset );
        this.labelXOffset = offset;
        this.pcs.firePropertyChange ( "labelXOffset", old, new Double ( offset ) );
    }
    public double getLabelYOffset() {
        return this.labelYOffset;
    }
    public void setLabelYOffset ( final double offset ) {
        final Double old = new Double ( this.labelYOffset );
        this.labelYOffset = offset;
        this.pcs.firePropertyChange ( "labelYOffset", old, new Double ( offset ) );
    }
    public Font getLabelFont() {
        return this.labelFont;
    }
    public void setLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        final Font old = this.labelFont;
        this.labelFont = font;
        this.pcs.firePropertyChange ( "labelFont", old, font );
    }
    public Paint getLabelPaint() {
        return this.labelPaint;
    }
    public void setLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        final Paint old = this.labelPaint;
        this.labelPaint = paint;
        this.pcs.firePropertyChange ( "labelPaint", old, paint );
    }
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }
    public void setLabelBackgroundPaint ( final Paint paint ) {
        final Paint old = this.labelBackgroundPaint;
        this.labelBackgroundPaint = paint;
        this.pcs.firePropertyChange ( "labelBackgroundPaint", old, paint );
    }
    public boolean isLabelOutlineVisible() {
        return this.labelOutlineVisible;
    }
    public void setLabelOutlineVisible ( final boolean visible ) {
        final boolean old = this.labelOutlineVisible;
        this.labelOutlineVisible = visible;
        this.pcs.firePropertyChange ( "labelOutlineVisible", old, visible );
    }
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }
    public void setLabelOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        final Paint old = this.labelOutlinePaint;
        this.labelOutlinePaint = paint;
        this.pcs.firePropertyChange ( "labelOutlinePaint", old, paint );
    }
    public Stroke getLabelOutlineStroke() {
        return this.labelOutlineStroke;
    }
    public void setLabelOutlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        final Stroke old = this.labelOutlineStroke;
        this.labelOutlineStroke = stroke;
        this.pcs.firePropertyChange ( "labelOutlineStroke", old, stroke );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Crosshair ) ) {
            return false;
        }
        final Crosshair that = ( Crosshair ) obj;
        return this.visible == that.visible && this.value == that.value && PaintUtilities.equal ( this.paint, that.paint ) && this.stroke.equals ( that.stroke ) && this.labelVisible == that.labelVisible && this.labelGenerator.equals ( that.labelGenerator ) && this.labelAnchor.equals ( ( Object ) that.labelAnchor ) && this.labelXOffset == that.labelXOffset && this.labelYOffset == that.labelYOffset && this.labelFont.equals ( that.labelFont ) && PaintUtilities.equal ( this.labelPaint, that.labelPaint ) && PaintUtilities.equal ( this.labelBackgroundPaint, that.labelBackgroundPaint ) && this.labelOutlineVisible == that.labelOutlineVisible && PaintUtilities.equal ( this.labelOutlinePaint, that.labelOutlinePaint ) && this.labelOutlineStroke.equals ( that.labelOutlineStroke );
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = HashUtilities.hashCode ( hash, this.visible );
        hash = HashUtilities.hashCode ( hash, this.value );
        hash = HashUtilities.hashCode ( hash, this.paint );
        hash = HashUtilities.hashCode ( hash, this.stroke );
        hash = HashUtilities.hashCode ( hash, this.labelVisible );
        hash = HashUtilities.hashCode ( hash, this.labelAnchor );
        hash = HashUtilities.hashCode ( hash, this.labelGenerator );
        hash = HashUtilities.hashCode ( hash, this.labelXOffset );
        hash = HashUtilities.hashCode ( hash, this.labelYOffset );
        hash = HashUtilities.hashCode ( hash, this.labelFont );
        hash = HashUtilities.hashCode ( hash, this.labelPaint );
        hash = HashUtilities.hashCode ( hash, this.labelBackgroundPaint );
        hash = HashUtilities.hashCode ( hash, this.labelOutlineVisible );
        hash = HashUtilities.hashCode ( hash, this.labelOutlineStroke );
        hash = HashUtilities.hashCode ( hash, this.labelOutlinePaint );
        return hash;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public void addPropertyChangeListener ( final PropertyChangeListener l ) {
        this.pcs.addPropertyChangeListener ( l );
    }
    public void removePropertyChangeListener ( final PropertyChangeListener l ) {
        this.pcs.removePropertyChangeListener ( l );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writeStroke ( this.stroke, stream );
        SerialUtilities.writePaint ( this.labelPaint, stream );
        SerialUtilities.writePaint ( this.labelBackgroundPaint, stream );
        SerialUtilities.writeStroke ( this.labelOutlineStroke, stream );
        SerialUtilities.writePaint ( this.labelOutlinePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.stroke = SerialUtilities.readStroke ( stream );
        this.labelPaint = SerialUtilities.readPaint ( stream );
        this.labelBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.labelOutlineStroke = SerialUtilities.readStroke ( stream );
        this.labelOutlinePaint = SerialUtilities.readPaint ( stream );
        this.pcs = new PropertyChangeSupport ( this );
    }
}
