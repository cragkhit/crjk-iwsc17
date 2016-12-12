package org.jfree.chart.title;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import org.jfree.chart.block.RectangleConstraint;
import java.awt.Graphics2D;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.BlockContainer;
import java.awt.Paint;
import java.io.Serializable;
public class CompositeTitle extends Title implements Cloneable, Serializable {
    private static final long serialVersionUID = -6770854036232562290L;
    private transient Paint backgroundPaint;
    private BlockContainer container;
    public CompositeTitle() {
        this ( new BlockContainer ( new BorderArrangement() ) );
    }
    public CompositeTitle ( final BlockContainer container ) {
        ParamChecks.nullNotPermitted ( container, "container" );
        this.container = container;
        this.backgroundPaint = null;
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        this.backgroundPaint = paint;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public BlockContainer getContainer() {
        return this.container;
    }
    public void setTitleContainer ( final BlockContainer container ) {
        ParamChecks.nullNotPermitted ( container, "container" );
        this.container = container;
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint contentConstraint = this.toContentConstraint ( constraint );
        final Size2D contentSize = this.container.arrange ( g2, contentConstraint );
        return new Size2D ( this.calculateTotalWidth ( contentSize.getWidth() ), this.calculateTotalHeight ( contentSize.getHeight() ) );
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null );
    }
    @Override
    public Object draw ( final Graphics2D g2, Rectangle2D area, final Object params ) {
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        area = this.trimBorder ( area );
        if ( this.backgroundPaint != null ) {
            g2.setPaint ( this.backgroundPaint );
            g2.fill ( area );
        }
        area = this.trimPadding ( area );
        return this.container.draw ( g2, area, params );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CompositeTitle ) ) {
            return false;
        }
        final CompositeTitle that = ( CompositeTitle ) obj;
        return this.container.equals ( that.container ) && PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
    }
}
