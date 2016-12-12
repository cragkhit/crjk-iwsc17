package org.jfree.chart.block;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.Paint;
import org.jfree.ui.RectangleInsets;
import java.io.Serializable;
public class BlockBorder implements BlockFrame, Serializable {
    private static final long serialVersionUID = 4961579220410228283L;
    public static final BlockBorder NONE;
    private RectangleInsets insets;
    private transient Paint paint;
    public BlockBorder() {
        this ( Color.black );
    }
    public BlockBorder ( final Paint paint ) {
        this ( new RectangleInsets ( 1.0, 1.0, 1.0, 1.0 ), paint );
    }
    public BlockBorder ( final double top, final double left, final double bottom, final double right ) {
        this ( new RectangleInsets ( top, left, bottom, right ), Color.black );
    }
    public BlockBorder ( final double top, final double left, final double bottom, final double right, final Paint paint ) {
        this ( new RectangleInsets ( top, left, bottom, right ), paint );
    }
    public BlockBorder ( final RectangleInsets insets, final Paint paint ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.insets = insets;
        this.paint = paint;
    }
    @Override
    public RectangleInsets getInsets() {
        return this.insets;
    }
    public Paint getPaint() {
        return this.paint;
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        final double t = this.insets.calculateTopInset ( area.getHeight() );
        final double b = this.insets.calculateBottomInset ( area.getHeight() );
        final double l = this.insets.calculateLeftInset ( area.getWidth() );
        final double r = this.insets.calculateRightInset ( area.getWidth() );
        final double x = area.getX();
        final double y = area.getY();
        final double w = area.getWidth();
        final double h = area.getHeight();
        g2.setPaint ( this.paint );
        final Rectangle2D rect = new Rectangle2D.Double();
        if ( t > 0.0 ) {
            rect.setRect ( x, y, w, t );
            g2.fill ( rect );
        }
        if ( b > 0.0 ) {
            rect.setRect ( x, y + h - b, w, b );
            g2.fill ( rect );
        }
        if ( l > 0.0 ) {
            rect.setRect ( x, y, l, h );
            g2.fill ( rect );
        }
        if ( r > 0.0 ) {
            rect.setRect ( x + w - r, y, r, h );
            g2.fill ( rect );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BlockBorder ) ) {
            return false;
        }
        final BlockBorder that = ( BlockBorder ) obj;
        return this.insets.equals ( ( Object ) that.insets ) && PaintUtilities.equal ( this.paint, that.paint );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
    static {
        NONE = new BlockBorder ( RectangleInsets.ZERO_INSETS, Color.white );
    }
}
