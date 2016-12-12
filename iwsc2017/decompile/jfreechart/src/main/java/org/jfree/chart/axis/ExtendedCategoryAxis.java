package org.jfree.chart.axis;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.text.TextFragment;
import org.jfree.text.TextLine;
import org.jfree.text.TextBlock;
import java.awt.Graphics2D;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.util.HashMap;
import java.awt.Paint;
import java.awt.Font;
import java.util.Map;
public class ExtendedCategoryAxis extends CategoryAxis {
    static final long serialVersionUID = -3004429093959826567L;
    private Map sublabels;
    private Font sublabelFont;
    private transient Paint sublabelPaint;
    public ExtendedCategoryAxis ( final String label ) {
        super ( label );
        this.sublabels = new HashMap();
        this.sublabelFont = new Font ( "SansSerif", 0, 10 );
        this.sublabelPaint = Color.black;
    }
    public Font getSubLabelFont() {
        return this.sublabelFont;
    }
    public void setSubLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.sublabelFont = font;
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    public Paint getSubLabelPaint() {
        return this.sublabelPaint;
    }
    public void setSubLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.sublabelPaint = paint;
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    public void addSubLabel ( final Comparable category, final String label ) {
        this.sublabels.put ( category, label );
    }
    @Override
    protected TextBlock createLabel ( final Comparable category, final float width, final RectangleEdge edge, final Graphics2D g2 ) {
        final TextBlock label = super.createLabel ( category, width, edge, g2 );
        final String s = this.sublabels.get ( category );
        if ( s != null ) {
            if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
                final TextLine line = new TextLine ( s, this.sublabelFont, this.sublabelPaint );
                label.addLine ( line );
            } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                final TextLine line = label.getLastLine();
                if ( line != null ) {
                    line.addFragment ( new TextFragment ( "  " + s, this.sublabelFont, this.sublabelPaint ) );
                }
            }
        }
        return label;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ExtendedCategoryAxis ) ) {
            return false;
        }
        final ExtendedCategoryAxis that = ( ExtendedCategoryAxis ) obj;
        return this.sublabelFont.equals ( that.sublabelFont ) && PaintUtilities.equal ( this.sublabelPaint, that.sublabelPaint ) && this.sublabels.equals ( that.sublabels ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ExtendedCategoryAxis clone = ( ExtendedCategoryAxis ) super.clone();
        clone.sublabels = new HashMap ( this.sublabels );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.sublabelPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.sublabelPaint = SerialUtilities.readPaint ( stream );
    }
}
