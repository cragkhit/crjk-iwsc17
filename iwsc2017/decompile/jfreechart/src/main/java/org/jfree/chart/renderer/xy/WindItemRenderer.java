package org.jfree.chart.renderer.xy;
import org.jfree.ui.RectangleEdge;
import java.awt.Stroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.data.xy.WindDataset;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class WindItemRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 8078914101916976844L;
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D plotArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final WindDataset windData = ( WindDataset ) dataset;
        final Paint seriesPaint = this.getItemPaint ( series, item );
        final Stroke seriesStroke = this.getItemStroke ( series, item );
        g2.setPaint ( seriesPaint );
        g2.setStroke ( seriesStroke );
        final Number x = windData.getX ( series, item );
        final Number windDir = windData.getWindDirection ( series, item );
        final Number wforce = windData.getWindForce ( series, item );
        final double windForce = wforce.doubleValue();
        final double wdirt = Math.toRadians ( windDir.doubleValue() * -30.0 - 90.0 );
        final RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        final double ax1 = domainAxis.valueToJava2D ( x.doubleValue(), plotArea, domainAxisLocation );
        final double ay1 = rangeAxis.valueToJava2D ( 0.0, plotArea, rangeAxisLocation );
        final double rax2 = x.doubleValue() + windForce * Math.cos ( wdirt ) * 8000000.0;
        final double ray2 = windForce * Math.sin ( wdirt );
        final double ax2 = domainAxis.valueToJava2D ( rax2, plotArea, domainAxisLocation );
        final double ay2 = rangeAxis.valueToJava2D ( ray2, plotArea, rangeAxisLocation );
        final int diri = windDir.intValue();
        final int forcei = wforce.intValue();
        final String dirforce = diri + "-" + forcei;
        Line2D line = new Line2D.Double ( ax1, ay1, ax2, ay2 );
        g2.draw ( line );
        g2.setPaint ( Color.blue );
        g2.setFont ( new Font ( "Dialog", 1, 9 ) );
        g2.drawString ( dirforce, ( float ) ax1, ( float ) ay1 );
        g2.setPaint ( seriesPaint );
        g2.setStroke ( seriesStroke );
        final double aldir = Math.toRadians ( windDir.doubleValue() * -30.0 - 90.0 - 5.0 );
        final double ralx2 = wforce.doubleValue() * Math.cos ( aldir ) * 8000000.0 * 0.8 + x.doubleValue();
        final double raly2 = wforce.doubleValue() * Math.sin ( aldir ) * 0.8;
        final double alx2 = domainAxis.valueToJava2D ( ralx2, plotArea, domainAxisLocation );
        final double aly2 = rangeAxis.valueToJava2D ( raly2, plotArea, rangeAxisLocation );
        line = new Line2D.Double ( alx2, aly2, ax2, ay2 );
        g2.draw ( line );
        final double ardir = Math.toRadians ( windDir.doubleValue() * -30.0 - 90.0 + 5.0 );
        final double rarx2 = wforce.doubleValue() * Math.cos ( ardir ) * 8000000.0 * 0.8 + x.doubleValue();
        final double rary2 = wforce.doubleValue() * Math.sin ( ardir ) * 0.8;
        final double arx2 = domainAxis.valueToJava2D ( rarx2, plotArea, domainAxisLocation );
        final double ary2 = rangeAxis.valueToJava2D ( rary2, plotArea, rangeAxisLocation );
        line = new Line2D.Double ( arx2, ary2, ax2, ay2 );
        g2.draw ( line );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
