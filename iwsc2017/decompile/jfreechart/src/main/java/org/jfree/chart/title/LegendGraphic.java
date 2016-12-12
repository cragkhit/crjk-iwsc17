package org.jfree.chart.title;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import java.awt.GradientPaint;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.ui.Size2D;
import org.jfree.chart.block.RectangleConstraint;
import java.awt.Graphics2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import java.awt.Stroke;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Paint;
import org.jfree.ui.RectangleAnchor;
import java.awt.Shape;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.AbstractBlock;
public class LegendGraphic extends AbstractBlock implements Block, PublicCloneable {
    static final long serialVersionUID = -1338791523854985009L;
    private boolean shapeVisible;
    private transient Shape shape;
    private RectangleAnchor shapeLocation;
    private RectangleAnchor shapeAnchor;
    private boolean shapeFilled;
    private transient Paint fillPaint;
    private GradientPaintTransformer fillPaintTransformer;
    private boolean shapeOutlineVisible;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    private boolean lineVisible;
    private transient Shape line;
    private transient Stroke lineStroke;
    private transient Paint linePaint;
    public LegendGraphic ( final Shape shape, final Paint fillPaint ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        ParamChecks.nullNotPermitted ( fillPaint, "fillPaint" );
        this.shapeVisible = true;
        this.shape = shape;
        this.shapeAnchor = RectangleAnchor.CENTER;
        this.shapeLocation = RectangleAnchor.CENTER;
        this.shapeFilled = true;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
        this.setPadding ( 2.0, 2.0, 2.0, 2.0 );
    }
    public boolean isShapeVisible() {
        return this.shapeVisible;
    }
    public void setShapeVisible ( final boolean visible ) {
        this.shapeVisible = visible;
    }
    public Shape getShape() {
        return this.shape;
    }
    public void setShape ( final Shape shape ) {
        this.shape = shape;
    }
    public boolean isShapeFilled() {
        return this.shapeFilled;
    }
    public void setShapeFilled ( final boolean filled ) {
        this.shapeFilled = filled;
    }
    public Paint getFillPaint() {
        return this.fillPaint;
    }
    public void setFillPaint ( final Paint paint ) {
        this.fillPaint = paint;
    }
    public GradientPaintTransformer getFillPaintTransformer() {
        return this.fillPaintTransformer;
    }
    public void setFillPaintTransformer ( final GradientPaintTransformer transformer ) {
        ParamChecks.nullNotPermitted ( transformer, "transformer" );
        this.fillPaintTransformer = transformer;
    }
    public boolean isShapeOutlineVisible() {
        return this.shapeOutlineVisible;
    }
    public void setShapeOutlineVisible ( final boolean visible ) {
        this.shapeOutlineVisible = visible;
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint paint ) {
        this.outlinePaint = paint;
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    public void setOutlineStroke ( final Stroke stroke ) {
        this.outlineStroke = stroke;
    }
    public RectangleAnchor getShapeAnchor() {
        return this.shapeAnchor;
    }
    public void setShapeAnchor ( final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.shapeAnchor = anchor;
    }
    public RectangleAnchor getShapeLocation() {
        return this.shapeLocation;
    }
    public void setShapeLocation ( final RectangleAnchor location ) {
        ParamChecks.nullNotPermitted ( location, "location" );
        this.shapeLocation = location;
    }
    public boolean isLineVisible() {
        return this.lineVisible;
    }
    public void setLineVisible ( final boolean visible ) {
        this.lineVisible = visible;
    }
    public Shape getLine() {
        return this.line;
    }
    public void setLine ( final Shape line ) {
        this.line = line;
    }
    public Paint getLinePaint() {
        return this.linePaint;
    }
    public void setLinePaint ( final Paint paint ) {
        this.linePaint = paint;
    }
    public Stroke getLineStroke() {
        return this.lineStroke;
    }
    public void setLineStroke ( final Stroke stroke ) {
        this.lineStroke = stroke;
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint contentConstraint = this.toContentConstraint ( constraint );
        final LengthConstraintType w = contentConstraint.getWidthConstraintType();
        final LengthConstraintType h = contentConstraint.getHeightConstraintType();
        Size2D contentSize = null;
        if ( w == LengthConstraintType.NONE ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeNN ( g2 );
            } else {
                if ( h == LengthConstraintType.RANGE ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
                if ( h == LengthConstraintType.FIXED ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
            }
        } else if ( w == LengthConstraintType.RANGE ) {
            if ( h == LengthConstraintType.NONE ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
            if ( h == LengthConstraintType.RANGE ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
            if ( h == LengthConstraintType.FIXED ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
        } else if ( w == LengthConstraintType.FIXED ) {
            if ( h == LengthConstraintType.NONE ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
            if ( h == LengthConstraintType.RANGE ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
            if ( h == LengthConstraintType.FIXED ) {
                contentSize = new Size2D ( contentConstraint.getWidth(), contentConstraint.getHeight() );
            }
        }
        assert contentSize != null;
        return new Size2D ( this.calculateTotalWidth ( contentSize.getWidth() ), this.calculateTotalHeight ( contentSize.getHeight() ) );
    }
    protected Size2D arrangeNN ( final Graphics2D g2 ) {
        Rectangle2D contentSize = new Rectangle2D.Double();
        if ( this.line != null ) {
            contentSize.setRect ( this.line.getBounds2D() );
        }
        if ( this.shape != null ) {
            contentSize = contentSize.createUnion ( this.shape.getBounds2D() );
        }
        return new Size2D ( contentSize.getWidth(), contentSize.getHeight() );
    }
    public void draw ( final Graphics2D g2, Rectangle2D area ) {
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        area = this.trimBorder ( area );
        area = this.trimPadding ( area );
        if ( this.lineVisible ) {
            final Point2D location = RectangleAnchor.coordinates ( area, this.shapeLocation );
            final Shape aLine = ShapeUtilities.createTranslatedShape ( this.getLine(), this.shapeAnchor, location.getX(), location.getY() );
            g2.setPaint ( this.linePaint );
            g2.setStroke ( this.lineStroke );
            g2.draw ( aLine );
        }
        if ( this.shapeVisible ) {
            final Point2D location = RectangleAnchor.coordinates ( area, this.shapeLocation );
            final Shape s = ShapeUtilities.createTranslatedShape ( this.shape, this.shapeAnchor, location.getX(), location.getY() );
            if ( this.shapeFilled ) {
                Paint p = this.fillPaint;
                if ( p instanceof GradientPaint ) {
                    final GradientPaint gp = ( GradientPaint ) this.fillPaint;
                    p = this.fillPaintTransformer.transform ( gp, s );
                }
                g2.setPaint ( p );
                g2.fill ( s );
            }
            if ( this.shapeOutlineVisible ) {
                g2.setPaint ( this.outlinePaint );
                g2.setStroke ( this.outlineStroke );
                g2.draw ( s );
            }
        }
    }
    @Override
    public Object draw ( final Graphics2D g2, final Rectangle2D area, final Object params ) {
        this.draw ( g2, area );
        return null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof LegendGraphic ) ) {
            return false;
        }
        final LegendGraphic that = ( LegendGraphic ) obj;
        return this.shapeVisible == that.shapeVisible && ShapeUtilities.equal ( this.shape, that.shape ) && this.shapeFilled == that.shapeFilled && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && ObjectUtilities.equal ( ( Object ) this.fillPaintTransformer, ( Object ) that.fillPaintTransformer ) && this.shapeOutlineVisible == that.shapeOutlineVisible && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && ObjectUtilities.equal ( ( Object ) this.outlineStroke, ( Object ) that.outlineStroke ) && this.shapeAnchor == that.shapeAnchor && this.shapeLocation == that.shapeLocation && this.lineVisible == that.lineVisible && ShapeUtilities.equal ( this.line, that.line ) && PaintUtilities.equal ( this.linePaint, that.linePaint ) && ObjectUtilities.equal ( ( Object ) this.lineStroke, ( Object ) that.lineStroke ) && super.equals ( obj );
    }
    public int hashCode() {
        int result = 193;
        result = 37 * result + ObjectUtilities.hashCode ( ( Object ) this.fillPaint );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final LegendGraphic clone = ( LegendGraphic ) super.clone();
        clone.shape = ShapeUtilities.clone ( this.shape );
        clone.line = ShapeUtilities.clone ( this.line );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.shape, stream );
        SerialUtilities.writePaint ( this.fillPaint, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
        SerialUtilities.writeShape ( this.line, stream );
        SerialUtilities.writePaint ( this.linePaint, stream );
        SerialUtilities.writeStroke ( this.lineStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.shape = SerialUtilities.readShape ( stream );
        this.fillPaint = SerialUtilities.readPaint ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
        this.line = SerialUtilities.readShape ( stream );
        this.linePaint = SerialUtilities.readPaint ( stream );
        this.lineStroke = SerialUtilities.readStroke ( stream );
    }
}
