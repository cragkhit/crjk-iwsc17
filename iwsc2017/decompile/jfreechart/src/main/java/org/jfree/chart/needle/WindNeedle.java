package org.jfree.chart.needle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class WindNeedle extends ArrowNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = -2861061368907167834L;
    public WindNeedle() {
        super ( false );
    }
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        super.drawNeedle ( g2, plotArea, rotate, angle );
        if ( rotate != null && plotArea != null ) {
            final int spacing = this.getSize() * 3;
            final Rectangle2D newArea = new Rectangle2D.Double();
            newArea.setRect ( plotArea.getMinX() - spacing, plotArea.getMinY(), plotArea.getWidth(), plotArea.getHeight() );
            super.drawNeedle ( g2, newArea, rotate, angle );
            newArea.setRect ( plotArea.getMinX() + spacing, plotArea.getMinY(), plotArea.getWidth(), plotArea.getHeight() );
            super.drawNeedle ( g2, newArea, rotate, angle );
        }
    }
    @Override
    public boolean equals ( final Object object ) {
        return object != null && ( object == this || ( super.equals ( object ) && object instanceof WindNeedle ) );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
