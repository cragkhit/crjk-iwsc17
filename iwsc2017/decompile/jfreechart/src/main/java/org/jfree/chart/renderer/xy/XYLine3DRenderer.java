package org.jfree.chart.renderer.xy;
import java.awt.Color;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.chart.Effect3D;
public class XYLine3DRenderer extends XYLineAndShapeRenderer implements Effect3D, Serializable {
    private static final long serialVersionUID = 588933208243446087L;
    public static final double DEFAULT_X_OFFSET = 12.0;
    public static final double DEFAULT_Y_OFFSET = 8.0;
    public static final Paint DEFAULT_WALL_PAINT;
    private double xOffset;
    private double yOffset;
    private transient Paint wallPaint;
    public XYLine3DRenderer() {
        this.wallPaint = XYLine3DRenderer.DEFAULT_WALL_PAINT;
        this.xOffset = 12.0;
        this.yOffset = 8.0;
    }
    @Override
    public double getXOffset() {
        return this.xOffset;
    }
    @Override
    public double getYOffset() {
        return this.yOffset;
    }
    public void setXOffset ( final double xOffset ) {
        this.xOffset = xOffset;
        this.fireChangeEvent();
    }
    public void setYOffset ( final double yOffset ) {
        this.yOffset = yOffset;
        this.fireChangeEvent();
    }
    public Paint getWallPaint() {
        return this.wallPaint;
    }
    public void setWallPaint ( final Paint paint ) {
        this.wallPaint = paint;
        this.fireChangeEvent();
    }
    @Override
    public int getPassCount() {
        return 3;
    }
    @Override
    protected boolean isLinePass ( final int pass ) {
        return pass == 0 || pass == 1;
    }
    @Override
    protected boolean isItemPass ( final int pass ) {
        return pass == 2;
    }
    protected boolean isShadowPass ( final int pass ) {
        return pass == 0;
    }
    @Override
    protected void drawFirstPassShape ( final Graphics2D g2, final int pass, final int series, final int item, final Shape shape ) {
        if ( this.isShadowPass ( pass ) ) {
            if ( this.getWallPaint() != null ) {
                g2.setStroke ( this.getItemStroke ( series, item ) );
                g2.setPaint ( this.getWallPaint() );
                g2.translate ( this.getXOffset(), this.getYOffset() );
                g2.draw ( shape );
                g2.translate ( -this.getXOffset(), -this.getYOffset() );
            }
        } else {
            super.drawFirstPassShape ( g2, pass, series, item, shape );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYLine3DRenderer ) ) {
            return false;
        }
        final XYLine3DRenderer that = ( XYLine3DRenderer ) obj;
        return this.xOffset == that.xOffset && this.yOffset == that.yOffset && PaintUtilities.equal ( this.wallPaint, that.wallPaint ) && super.equals ( obj );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.wallPaint = SerialUtilities.readPaint ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.wallPaint, stream );
    }
    static {
        DEFAULT_WALL_PAINT = new Color ( 221, 221, 221 );
    }
}
