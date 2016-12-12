package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import java.awt.Shape;
import org.jfree.chart.util.LineUtilities;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYLineAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -80535465244091334L;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private transient Stroke stroke;
    private transient Paint paint;
    public XYLineAnnotation ( final double x1, final double y1, final double x2, final double y2 ) {
        this ( x1, y1, x2, y2, new BasicStroke ( 1.0f ), Color.black );
    }
    public XYLineAnnotation ( final double x1, final double y1, final double x2, final double y2, final Stroke stroke, final Paint paint ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.stroke = stroke;
        this.paint = paint;
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        float j2DX1 = 0.0f;
        float j2DX2 = 0.0f;
        float j2DY1 = 0.0f;
        float j2DY2 = 0.0f;
        if ( orientation == PlotOrientation.VERTICAL ) {
            j2DX1 = ( float ) domainAxis.valueToJava2D ( this.x1, dataArea, domainEdge );
            j2DY1 = ( float ) rangeAxis.valueToJava2D ( this.y1, dataArea, rangeEdge );
            j2DX2 = ( float ) domainAxis.valueToJava2D ( this.x2, dataArea, domainEdge );
            j2DY2 = ( float ) rangeAxis.valueToJava2D ( this.y2, dataArea, rangeEdge );
        } else if ( orientation == PlotOrientation.HORIZONTAL ) {
            j2DY1 = ( float ) domainAxis.valueToJava2D ( this.x1, dataArea, domainEdge );
            j2DX1 = ( float ) rangeAxis.valueToJava2D ( this.y1, dataArea, rangeEdge );
            j2DY2 = ( float ) domainAxis.valueToJava2D ( this.x2, dataArea, domainEdge );
            j2DX2 = ( float ) rangeAxis.valueToJava2D ( this.y2, dataArea, rangeEdge );
        }
        g2.setPaint ( this.paint );
        g2.setStroke ( this.stroke );
        final Line2D line = new Line2D.Float ( j2DX1, j2DY1, j2DX2, j2DY2 );
        final boolean visible = LineUtilities.clipLine ( line, dataArea );
        if ( visible ) {
            g2.draw ( line );
        }
        final String toolTip = this.getToolTipText();
        final String url = this.getURL();
        if ( toolTip != null || url != null ) {
            this.addEntity ( info, ShapeUtilities.createLineRegion ( line, 1.0f ), rendererIndex, toolTip, url );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof XYLineAnnotation ) ) {
            return false;
        }
        final XYLineAnnotation that = ( XYLineAnnotation ) obj;
        return this.x1 == that.x1 && this.y1 == that.y1 && this.x2 == that.x2 && this.y2 == that.y2 && PaintUtilities.equal ( this.paint, that.paint ) && ObjectUtilities.equal ( ( Object ) this.stroke, ( Object ) that.stroke );
    }
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits ( this.x1 );
        int result = ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.x2 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y1 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y2 );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writeStroke ( this.stroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.stroke = SerialUtilities.readStroke ( stream );
    }
}
