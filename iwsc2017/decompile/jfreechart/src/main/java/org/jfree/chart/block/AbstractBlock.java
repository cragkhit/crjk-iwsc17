package org.jfree.chart.block;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PublicCloneable;
import java.awt.Shape;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.Range;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.RectangleInsets;
import java.io.Serializable;
public class AbstractBlock implements Cloneable, Serializable {
    private static final long serialVersionUID = 7689852412141274563L;
    private String id;
    private RectangleInsets margin;
    private BlockFrame frame;
    private RectangleInsets padding;
    private double width;
    private double height;
    private transient Rectangle2D bounds;
    protected AbstractBlock() {
        this.id = null;
        this.width = 0.0;
        this.height = 0.0;
        this.bounds = new Rectangle2D.Float();
        this.margin = RectangleInsets.ZERO_INSETS;
        this.frame = BlockBorder.NONE;
        this.padding = RectangleInsets.ZERO_INSETS;
    }
    public String getID() {
        return this.id;
    }
    public void setID ( final String id ) {
        this.id = id;
    }
    public double getWidth() {
        return this.width;
    }
    public void setWidth ( final double width ) {
        this.width = width;
    }
    public double getHeight() {
        return this.height;
    }
    public void setHeight ( final double height ) {
        this.height = height;
    }
    public RectangleInsets getMargin() {
        return this.margin;
    }
    public void setMargin ( final RectangleInsets margin ) {
        ParamChecks.nullNotPermitted ( margin, "margin" );
        this.margin = margin;
    }
    public void setMargin ( final double top, final double left, final double bottom, final double right ) {
        this.setMargin ( new RectangleInsets ( top, left, bottom, right ) );
    }
    public BlockBorder getBorder() {
        if ( this.frame instanceof BlockBorder ) {
            return ( BlockBorder ) this.frame;
        }
        return null;
    }
    public void setBorder ( final BlockBorder border ) {
        this.setFrame ( border );
    }
    public void setBorder ( final double top, final double left, final double bottom, final double right ) {
        this.setFrame ( new BlockBorder ( top, left, bottom, right ) );
    }
    public BlockFrame getFrame() {
        return this.frame;
    }
    public void setFrame ( final BlockFrame frame ) {
        ParamChecks.nullNotPermitted ( frame, "frame" );
        this.frame = frame;
    }
    public RectangleInsets getPadding() {
        return this.padding;
    }
    public void setPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.padding = padding;
    }
    public void setPadding ( final double top, final double left, final double bottom, final double right ) {
        this.setPadding ( new RectangleInsets ( top, left, bottom, right ) );
    }
    public double getContentXOffset() {
        return this.margin.getLeft() + this.frame.getInsets().getLeft() + this.padding.getLeft();
    }
    public double getContentYOffset() {
        return this.margin.getTop() + this.frame.getInsets().getTop() + this.padding.getTop();
    }
    public Size2D arrange ( final Graphics2D g2 ) {
        return this.arrange ( g2, RectangleConstraint.NONE );
    }
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D base = new Size2D ( this.getWidth(), this.getHeight() );
        return constraint.calculateConstrainedSize ( base );
    }
    public Rectangle2D getBounds() {
        return this.bounds;
    }
    public void setBounds ( final Rectangle2D bounds ) {
        ParamChecks.nullNotPermitted ( bounds, "bounds" );
        this.bounds = bounds;
    }
    protected double trimToContentWidth ( final double fixedWidth ) {
        double result = this.margin.trimWidth ( fixedWidth );
        result = this.frame.getInsets().trimWidth ( result );
        result = this.padding.trimWidth ( result );
        return Math.max ( result, 0.0 );
    }
    protected double trimToContentHeight ( final double fixedHeight ) {
        double result = this.margin.trimHeight ( fixedHeight );
        result = this.frame.getInsets().trimHeight ( result );
        result = this.padding.trimHeight ( result );
        return Math.max ( result, 0.0 );
    }
    protected RectangleConstraint toContentConstraint ( final RectangleConstraint c ) {
        ParamChecks.nullNotPermitted ( c, "c" );
        if ( c.equals ( RectangleConstraint.NONE ) ) {
            return c;
        }
        final double w = c.getWidth();
        final Range wr = c.getWidthRange();
        final double h = c.getHeight();
        final Range hr = c.getHeightRange();
        final double ww = this.trimToContentWidth ( w );
        final double hh = this.trimToContentHeight ( h );
        final Range wwr = this.trimToContentWidth ( wr );
        final Range hhr = this.trimToContentHeight ( hr );
        return new RectangleConstraint ( ww, wwr, c.getWidthConstraintType(), hh, hhr, c.getHeightConstraintType() );
    }
    private Range trimToContentWidth ( final Range r ) {
        if ( r == null ) {
            return null;
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if ( r.getLowerBound() > 0.0 ) {
            lowerBound = this.trimToContentWidth ( r.getLowerBound() );
        }
        if ( r.getUpperBound() < Double.POSITIVE_INFINITY ) {
            upperBound = this.trimToContentWidth ( r.getUpperBound() );
        }
        return new Range ( lowerBound, upperBound );
    }
    private Range trimToContentHeight ( final Range r ) {
        if ( r == null ) {
            return null;
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if ( r.getLowerBound() > 0.0 ) {
            lowerBound = this.trimToContentHeight ( r.getLowerBound() );
        }
        if ( r.getUpperBound() < Double.POSITIVE_INFINITY ) {
            upperBound = this.trimToContentHeight ( r.getUpperBound() );
        }
        return new Range ( lowerBound, upperBound );
    }
    protected double calculateTotalWidth ( final double contentWidth ) {
        double result = this.padding.extendWidth ( contentWidth );
        result = this.frame.getInsets().extendWidth ( result );
        result = this.margin.extendWidth ( result );
        return result;
    }
    protected double calculateTotalHeight ( final double contentHeight ) {
        double result = this.padding.extendHeight ( contentHeight );
        result = this.frame.getInsets().extendHeight ( result );
        result = this.margin.extendHeight ( result );
        return result;
    }
    protected Rectangle2D trimMargin ( final Rectangle2D area ) {
        this.margin.trim ( area );
        return area;
    }
    protected Rectangle2D trimBorder ( final Rectangle2D area ) {
        this.frame.getInsets().trim ( area );
        return area;
    }
    protected Rectangle2D trimPadding ( final Rectangle2D area ) {
        this.padding.trim ( area );
        return area;
    }
    protected void drawBorder ( final Graphics2D g2, final Rectangle2D area ) {
        this.frame.draw ( g2, area );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractBlock ) ) {
            return false;
        }
        final AbstractBlock that = ( AbstractBlock ) obj;
        return ObjectUtilities.equal ( ( Object ) this.id, ( Object ) that.id ) && this.frame.equals ( that.frame ) && this.bounds.equals ( that.bounds ) && this.margin.equals ( ( Object ) that.margin ) && this.padding.equals ( ( Object ) that.padding ) && this.height == that.height && this.width == that.width;
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractBlock clone = ( AbstractBlock ) super.clone();
        clone.bounds = ( Rectangle2D ) ShapeUtilities.clone ( ( Shape ) this.bounds );
        if ( this.frame instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.frame;
            clone.frame = ( BlockFrame ) pc.clone();
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( ( Shape ) this.bounds, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.bounds = ( Rectangle2D ) SerialUtilities.readShape ( stream );
    }
}
