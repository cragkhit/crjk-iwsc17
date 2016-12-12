package org.jfree.chart.block;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class EmptyBlock extends AbstractBlock implements Block, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -4083197869412648579L;
    public EmptyBlock ( final double width, final double height ) {
        this.setWidth ( width );
        this.setHeight ( height );
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D base = new Size2D ( this.calculateTotalWidth ( this.getWidth() ), this.calculateTotalHeight ( this.getHeight() ) );
        return constraint.calculateConstrainedSize ( base );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null );
    }
    @Override
    public Object draw ( final Graphics2D g2, Rectangle2D area, final Object params ) {
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        return null;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
