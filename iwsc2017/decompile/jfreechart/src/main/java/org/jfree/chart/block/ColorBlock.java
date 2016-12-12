package org.jfree.chart.block;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Paint;
public class ColorBlock extends AbstractBlock implements Block {
    static final long serialVersionUID = 3383866145634010865L;
    private transient Paint paint;
    public ColorBlock ( final Paint paint, final double width, final double height ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.setWidth ( width );
        this.setHeight ( height );
    }
    public Paint getPaint() {
        return this.paint;
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        return new Size2D ( this.calculateTotalWidth ( this.getWidth() ), this.calculateTotalHeight ( this.getHeight() ) );
    }
    public void draw ( final Graphics2D g2, Rectangle2D area ) {
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        area = this.trimBorder ( area );
        area = this.trimPadding ( area );
        g2.setPaint ( this.paint );
        g2.fill ( area );
    }
    @Override
    public Object draw ( final Graphics2D g2, final Rectangle2D area, final Object params ) {
        this.draw ( g2, area );
        return null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ColorBlock ) ) {
            return false;
        }
        final ColorBlock that = ( ColorBlock ) obj;
        return PaintUtilities.equal ( this.paint, that.paint ) && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
}
