package org.jfree.chart.plot;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.ChartColor;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ShapeUtilities;
import java.util.Arrays;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultDrawingSupplier implements DrawingSupplier, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -7339847061039422538L;
    public static final Paint[] DEFAULT_PAINT_SEQUENCE;
    public static final Paint[] DEFAULT_OUTLINE_PAINT_SEQUENCE;
    public static final Paint[] DEFAULT_FILL_PAINT_SEQUENCE;
    public static final Stroke[] DEFAULT_STROKE_SEQUENCE;
    public static final Stroke[] DEFAULT_OUTLINE_STROKE_SEQUENCE;
    public static final Shape[] DEFAULT_SHAPE_SEQUENCE;
    private transient Paint[] paintSequence;
    private int paintIndex;
    private transient Paint[] outlinePaintSequence;
    private int outlinePaintIndex;
    private transient Paint[] fillPaintSequence;
    private int fillPaintIndex;
    private transient Stroke[] strokeSequence;
    private int strokeIndex;
    private transient Stroke[] outlineStrokeSequence;
    private int outlineStrokeIndex;
    private transient Shape[] shapeSequence;
    private int shapeIndex;
    public DefaultDrawingSupplier() {
        this ( DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE );
    }
    public DefaultDrawingSupplier ( final Paint[] paintSequence, final Paint[] outlinePaintSequence, final Stroke[] strokeSequence, final Stroke[] outlineStrokeSequence, final Shape[] shapeSequence ) {
        this.paintSequence = paintSequence;
        this.fillPaintSequence = DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE;
        this.outlinePaintSequence = outlinePaintSequence;
        this.strokeSequence = strokeSequence;
        this.outlineStrokeSequence = outlineStrokeSequence;
        this.shapeSequence = shapeSequence;
    }
    public DefaultDrawingSupplier ( final Paint[] paintSequence, final Paint[] fillPaintSequence, final Paint[] outlinePaintSequence, final Stroke[] strokeSequence, final Stroke[] outlineStrokeSequence, final Shape[] shapeSequence ) {
        this.paintSequence = paintSequence;
        this.fillPaintSequence = fillPaintSequence;
        this.outlinePaintSequence = outlinePaintSequence;
        this.strokeSequence = strokeSequence;
        this.outlineStrokeSequence = outlineStrokeSequence;
        this.shapeSequence = shapeSequence;
    }
    @Override
    public Paint getNextPaint() {
        final Paint result = this.paintSequence[this.paintIndex % this.paintSequence.length];
        ++this.paintIndex;
        return result;
    }
    @Override
    public Paint getNextOutlinePaint() {
        final Paint result = this.outlinePaintSequence[this.outlinePaintIndex % this.outlinePaintSequence.length];
        ++this.outlinePaintIndex;
        return result;
    }
    @Override
    public Paint getNextFillPaint() {
        final Paint result = this.fillPaintSequence[this.fillPaintIndex % this.fillPaintSequence.length];
        ++this.fillPaintIndex;
        return result;
    }
    @Override
    public Stroke getNextStroke() {
        final Stroke result = this.strokeSequence[this.strokeIndex % this.strokeSequence.length];
        ++this.strokeIndex;
        return result;
    }
    @Override
    public Stroke getNextOutlineStroke() {
        final Stroke result = this.outlineStrokeSequence[this.outlineStrokeIndex % this.outlineStrokeSequence.length];
        ++this.outlineStrokeIndex;
        return result;
    }
    @Override
    public Shape getNextShape() {
        final Shape result = this.shapeSequence[this.shapeIndex % this.shapeSequence.length];
        ++this.shapeIndex;
        return result;
    }
    public static Shape[] createStandardSeriesShapes() {
        final Shape[] result = new Shape[10];
        final double size = 6.0;
        final double delta = size / 2.0;
        result[0] = new Rectangle2D.Double ( -delta, -delta, size, size );
        result[1] = new Ellipse2D.Double ( -delta, -delta, size, size );
        int[] xpoints = intArray ( 0.0, delta, -delta );
        int[] ypoints = intArray ( -delta, delta, delta );
        result[2] = new Polygon ( xpoints, ypoints, 3 );
        xpoints = intArray ( 0.0, delta, 0.0, -delta );
        ypoints = intArray ( -delta, 0.0, delta, 0.0 );
        result[3] = new Polygon ( xpoints, ypoints, 4 );
        result[4] = new Rectangle2D.Double ( -delta, -delta / 2.0, size, size / 2.0 );
        xpoints = intArray ( -delta, delta, 0.0 );
        ypoints = intArray ( -delta, -delta, delta );
        result[5] = new Polygon ( xpoints, ypoints, 3 );
        result[6] = new Ellipse2D.Double ( -delta, -delta / 2.0, size, size / 2.0 );
        xpoints = intArray ( -delta, delta, -delta );
        ypoints = intArray ( -delta, 0.0, delta );
        result[7] = new Polygon ( xpoints, ypoints, 3 );
        result[8] = new Rectangle2D.Double ( -delta / 2.0, -delta, size / 2.0, size );
        xpoints = intArray ( -delta, delta, delta );
        ypoints = intArray ( 0.0, -delta, delta );
        result[9] = new Polygon ( xpoints, ypoints, 3 );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultDrawingSupplier ) ) {
            return false;
        }
        final DefaultDrawingSupplier that = ( DefaultDrawingSupplier ) obj;
        return Arrays.equals ( this.paintSequence, that.paintSequence ) && this.paintIndex == that.paintIndex && Arrays.equals ( this.outlinePaintSequence, that.outlinePaintSequence ) && this.outlinePaintIndex == that.outlinePaintIndex && Arrays.equals ( this.strokeSequence, that.strokeSequence ) && this.strokeIndex == that.strokeIndex && Arrays.equals ( this.outlineStrokeSequence, that.outlineStrokeSequence ) && this.outlineStrokeIndex == that.outlineStrokeIndex && this.equalShapes ( this.shapeSequence, that.shapeSequence ) && this.shapeIndex == that.shapeIndex;
    }
    private boolean equalShapes ( final Shape[] s1, final Shape[] s2 ) {
        if ( s1 == null ) {
            return s2 == null;
        }
        if ( s2 == null ) {
            return false;
        }
        if ( s1.length != s2.length ) {
            return false;
        }
        for ( int i = 0; i < s1.length; ++i ) {
            if ( !ShapeUtilities.equal ( s1[i], s2[i] ) ) {
                return false;
            }
        }
        return true;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        final int paintCount = this.paintSequence.length;
        stream.writeInt ( paintCount );
        for ( int i = 0; i < paintCount; ++i ) {
            SerialUtilities.writePaint ( this.paintSequence[i], stream );
        }
        final int outlinePaintCount = this.outlinePaintSequence.length;
        stream.writeInt ( outlinePaintCount );
        for ( int j = 0; j < outlinePaintCount; ++j ) {
            SerialUtilities.writePaint ( this.outlinePaintSequence[j], stream );
        }
        final int strokeCount = this.strokeSequence.length;
        stream.writeInt ( strokeCount );
        for ( int k = 0; k < strokeCount; ++k ) {
            SerialUtilities.writeStroke ( this.strokeSequence[k], stream );
        }
        final int outlineStrokeCount = this.outlineStrokeSequence.length;
        stream.writeInt ( outlineStrokeCount );
        for ( int l = 0; l < outlineStrokeCount; ++l ) {
            SerialUtilities.writeStroke ( this.outlineStrokeSequence[l], stream );
        }
        final int shapeCount = this.shapeSequence.length;
        stream.writeInt ( shapeCount );
        for ( int m = 0; m < shapeCount; ++m ) {
            SerialUtilities.writeShape ( this.shapeSequence[m], stream );
        }
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        final int paintCount = stream.readInt();
        this.paintSequence = new Paint[paintCount];
        for ( int i = 0; i < paintCount; ++i ) {
            this.paintSequence[i] = SerialUtilities.readPaint ( stream );
        }
        final int outlinePaintCount = stream.readInt();
        this.outlinePaintSequence = new Paint[outlinePaintCount];
        for ( int j = 0; j < outlinePaintCount; ++j ) {
            this.outlinePaintSequence[j] = SerialUtilities.readPaint ( stream );
        }
        final int strokeCount = stream.readInt();
        this.strokeSequence = new Stroke[strokeCount];
        for ( int k = 0; k < strokeCount; ++k ) {
            this.strokeSequence[k] = SerialUtilities.readStroke ( stream );
        }
        final int outlineStrokeCount = stream.readInt();
        this.outlineStrokeSequence = new Stroke[outlineStrokeCount];
        for ( int l = 0; l < outlineStrokeCount; ++l ) {
            this.outlineStrokeSequence[l] = SerialUtilities.readStroke ( stream );
        }
        final int shapeCount = stream.readInt();
        this.shapeSequence = new Shape[shapeCount];
        for ( int m = 0; m < shapeCount; ++m ) {
            this.shapeSequence[m] = SerialUtilities.readShape ( stream );
        }
    }
    private static int[] intArray ( final double a, final double b, final double c ) {
        return new int[] { ( int ) a, ( int ) b, ( int ) c };
    }
    private static int[] intArray ( final double a, final double b, final double c, final double d ) {
        return new int[] { ( int ) a, ( int ) b, ( int ) c, ( int ) d };
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultDrawingSupplier clone = ( DefaultDrawingSupplier ) super.clone();
        return clone;
    }
    static {
        DEFAULT_PAINT_SEQUENCE = ChartColor.createDefaultPaintArray();
        DEFAULT_OUTLINE_PAINT_SEQUENCE = new Paint[] { Color.lightGray };
        DEFAULT_FILL_PAINT_SEQUENCE = new Paint[] { Color.white };
        DEFAULT_STROKE_SEQUENCE = new Stroke[] { new BasicStroke ( 1.0f, 2, 2 ) };
        DEFAULT_OUTLINE_STROKE_SEQUENCE = new Stroke[] { new BasicStroke ( 1.0f, 2, 2 ) };
        DEFAULT_SHAPE_SEQUENCE = createStandardSeriesShapes();
    }
}
