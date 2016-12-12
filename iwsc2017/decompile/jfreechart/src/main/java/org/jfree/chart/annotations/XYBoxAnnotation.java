package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.RectangleEdge;
import java.awt.Shape;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYBoxAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 6764703772526757457L;
    private double x0;
    private double y0;
    private double x1;
    private double y1;
    private transient Stroke stroke;
    private transient Paint outlinePaint;
    private transient Paint fillPaint;
    public XYBoxAnnotation ( final double x0, final double y0, final double x1, final double y1 ) {
        this ( x0, y0, x1, y1, new BasicStroke ( 1.0f ), Color.black );
    }
    public XYBoxAnnotation ( final double x0, final double y0, final double x1, final double y1, final Stroke stroke, final Paint outlinePaint ) {
        this ( x0, y0, x1, y1, stroke, outlinePaint, null );
    }
    public XYBoxAnnotation ( final double x0, final double y0, final double x1, final double y1, final Stroke stroke, final Paint outlinePaint, final Paint fillPaint ) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.stroke = stroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        final double transX0 = domainAxis.valueToJava2D ( this.x0, dataArea, domainEdge );
        final double transY0 = rangeAxis.valueToJava2D ( this.y0, dataArea, rangeEdge );
        final double transX = domainAxis.valueToJava2D ( this.x1, dataArea, domainEdge );
        final double transY = rangeAxis.valueToJava2D ( this.y1, dataArea, rangeEdge );
        Rectangle2D box = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            box = new Rectangle2D.Double ( transY0, transX, transY - transY0, transX0 - transX );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            box = new Rectangle2D.Double ( transX0, transY, transX - transX0, transY0 - transY );
        }
        if ( this.fillPaint != null ) {
            g2.setPaint ( this.fillPaint );
            g2.fill ( box );
        }
        if ( this.stroke != null && this.outlinePaint != null ) {
            g2.setPaint ( this.outlinePaint );
            g2.setStroke ( this.stroke );
            g2.draw ( box );
        }
        this.addEntity ( info, box, rendererIndex, this.getToolTipText(), this.getURL() );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof XYBoxAnnotation ) ) {
            return false;
        }
        final XYBoxAnnotation that = ( XYBoxAnnotation ) obj;
        return this.x0 == that.x0 && this.y0 == that.y0 && this.x1 == that.x1 && this.y1 == that.y1 && ObjectUtilities.equal ( ( Object ) this.stroke, ( Object ) that.stroke ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && PaintUtilities.equal ( this.fillPaint, that.fillPaint );
    }
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits ( this.x0 );
        int result = ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.x1 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y0 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y1 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.stroke, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writePaint ( this.fillPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.stroke = SerialUtilities.readStroke ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.fillPaint = SerialUtilities.readPaint ( stream );
    }
}
