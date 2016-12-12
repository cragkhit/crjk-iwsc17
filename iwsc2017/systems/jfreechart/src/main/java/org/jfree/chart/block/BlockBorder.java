

package org.jfree.chart.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;

import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.PaintUtilities;


public class BlockBorder implements BlockFrame, Serializable {


    private static final long serialVersionUID = 4961579220410228283L;


    public static final BlockBorder NONE = new BlockBorder (
        RectangleInsets.ZERO_INSETS, Color.white );


    private RectangleInsets insets;


    private transient Paint paint;


    public BlockBorder() {
        this ( Color.black );
    }


    public BlockBorder ( Paint paint ) {
        this ( new RectangleInsets ( 1, 1, 1, 1 ), paint );
    }


    public BlockBorder ( double top, double left, double bottom, double right ) {
        this ( new RectangleInsets ( top, left, bottom, right ), Color.black );
    }


    public BlockBorder ( double top, double left, double bottom, double right,
                         Paint paint ) {
        this ( new RectangleInsets ( top, left, bottom, right ), paint );
    }


    public BlockBorder ( RectangleInsets insets, Paint paint ) {
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
    public void draw ( Graphics2D g2, Rectangle2D area ) {
        double t = this.insets.calculateTopInset ( area.getHeight() );
        double b = this.insets.calculateBottomInset ( area.getHeight() );
        double l = this.insets.calculateLeftInset ( area.getWidth() );
        double r = this.insets.calculateRightInset ( area.getWidth() );
        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        g2.setPaint ( this.paint );
        Rectangle2D rect = new Rectangle2D.Double();
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
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BlockBorder ) ) {
            return false;
        }
        BlockBorder that = ( BlockBorder ) obj;
        if ( !this.insets.equals ( that.insets ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.paint, that.paint ) ) {
            return false;
        }
        return true;
    }


    private void writeObject ( ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }


    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }

}
