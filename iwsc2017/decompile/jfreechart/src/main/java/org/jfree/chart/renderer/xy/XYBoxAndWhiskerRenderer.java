package org.jfree.chart.renderer.xy;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import java.util.Iterator;
import org.jfree.chart.renderer.OutlierList;
import java.util.List;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierListCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Stroke;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.entity.EntityCollection;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.BoxAndWhiskerXYToolTipGenerator;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYBoxAndWhiskerRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -8020170108532232324L;
    private double boxWidth;
    private transient Paint boxPaint;
    private boolean fillBox;
    private transient Paint artifactPaint;
    public XYBoxAndWhiskerRenderer() {
        this ( -1.0 );
    }
    public XYBoxAndWhiskerRenderer ( final double boxWidth ) {
        this.artifactPaint = Color.black;
        this.boxWidth = boxWidth;
        this.boxPaint = Color.green;
        this.fillBox = true;
        this.setBaseToolTipGenerator ( new BoxAndWhiskerXYToolTipGenerator() );
    }
    public double getBoxWidth() {
        return this.boxWidth;
    }
    public void setBoxWidth ( final double width ) {
        if ( width != this.boxWidth ) {
            this.boxWidth = width;
            this.fireChangeEvent();
        }
    }
    public Paint getBoxPaint() {
        return this.boxPaint;
    }
    public void setBoxPaint ( final Paint paint ) {
        this.boxPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getFillBox() {
        return this.fillBox;
    }
    public void setFillBox ( final boolean flag ) {
        this.fillBox = flag;
        this.fireChangeEvent();
    }
    public Paint getArtifactPaint() {
        return this.artifactPaint;
    }
    public void setArtifactPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.artifactPaint = paint;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    protected Paint lookupBoxPaint ( final int series, final int item ) {
        final Paint p = this.getBoxPaint();
        if ( p != null ) {
            return p;
        }
        return this.getItemPaint ( series, item );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            this.drawHorizontalItem ( g2, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            this.drawVerticalItem ( g2, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass );
        }
    }
    public void drawHorizontalItem ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final BoxAndWhiskerXYDataset boxAndWhiskerData = ( BoxAndWhiskerXYDataset ) dataset;
        final Number x = boxAndWhiskerData.getX ( series, item );
        final Number yMax = boxAndWhiskerData.getMaxRegularValue ( series, item );
        final Number yMin = boxAndWhiskerData.getMinRegularValue ( series, item );
        final Number yMedian = boxAndWhiskerData.getMedianValue ( series, item );
        final Number yAverage = boxAndWhiskerData.getMeanValue ( series, item );
        final Number yQ1Median = boxAndWhiskerData.getQ1Value ( series, item );
        final Number yQ3Median = boxAndWhiskerData.getQ3Value ( series, item );
        final double xx = domainAxis.valueToJava2D ( x.doubleValue(), dataArea, plot.getDomainAxisEdge() );
        final RectangleEdge location = plot.getRangeAxisEdge();
        final double yyMax = rangeAxis.valueToJava2D ( yMax.doubleValue(), dataArea, location );
        final double yyMin = rangeAxis.valueToJava2D ( yMin.doubleValue(), dataArea, location );
        final double yyMedian = rangeAxis.valueToJava2D ( yMedian.doubleValue(), dataArea, location );
        double yyAverage = 0.0;
        if ( yAverage != null ) {
            yyAverage = rangeAxis.valueToJava2D ( yAverage.doubleValue(), dataArea, location );
        }
        final double yyQ1Median = rangeAxis.valueToJava2D ( yQ1Median.doubleValue(), dataArea, location );
        final double yyQ3Median = rangeAxis.valueToJava2D ( yQ3Median.doubleValue(), dataArea, location );
        double width;
        double exactBoxWidth = width = this.getBoxWidth();
        final double dataAreaX = dataArea.getHeight();
        final double maxBoxPercent = 0.1;
        final double maxBoxWidth = dataAreaX * maxBoxPercent;
        if ( exactBoxWidth <= 0.0 ) {
            final int itemCount = boxAndWhiskerData.getItemCount ( series );
            exactBoxWidth = dataAreaX / itemCount * 4.5 / 7.0;
            if ( exactBoxWidth < 3.0 ) {
                width = 3.0;
            } else if ( exactBoxWidth > maxBoxWidth ) {
                width = maxBoxWidth;
            } else {
                width = exactBoxWidth;
            }
        }
        g2.setPaint ( this.getItemPaint ( series, item ) );
        final Stroke s = this.getItemStroke ( series, item );
        g2.setStroke ( s );
        g2.draw ( new Line2D.Double ( yyMax, xx, yyQ3Median, xx ) );
        g2.draw ( new Line2D.Double ( yyMax, xx - width / 2.0, yyMax, xx + width / 2.0 ) );
        g2.draw ( new Line2D.Double ( yyMin, xx, yyQ1Median, xx ) );
        g2.draw ( new Line2D.Double ( yyMin, xx - width / 2.0, yyMin, xx + width / 2.0 ) );
        Shape box;
        if ( yyQ1Median < yyQ3Median ) {
            box = new Rectangle2D.Double ( yyQ1Median, xx - width / 2.0, yyQ3Median - yyQ1Median, width );
        } else {
            box = new Rectangle2D.Double ( yyQ3Median, xx - width / 2.0, yyQ1Median - yyQ3Median, width );
        }
        if ( this.fillBox ) {
            g2.setPaint ( this.lookupBoxPaint ( series, item ) );
            g2.fill ( box );
        }
        g2.setStroke ( this.getItemOutlineStroke ( series, item ) );
        g2.setPaint ( this.getItemOutlinePaint ( series, item ) );
        g2.draw ( box );
        g2.setPaint ( this.getArtifactPaint() );
        g2.draw ( new Line2D.Double ( yyMedian, xx - width / 2.0, yyMedian, xx + width / 2.0 ) );
        if ( yAverage != null ) {
            final double aRadius = width / 4.0;
            if ( yyAverage > dataArea.getMinX() - aRadius && yyAverage < dataArea.getMaxX() + aRadius ) {
                final Ellipse2D.Double avgEllipse = new Ellipse2D.Double ( yyAverage - aRadius, xx - aRadius, aRadius * 2.0, aRadius * 2.0 );
                g2.fill ( avgEllipse );
                g2.draw ( avgEllipse );
            }
        }
        if ( entities != null && box.intersects ( dataArea ) ) {
            this.addEntity ( entities, box, dataset, series, item, yyAverage, xx );
        }
    }
    public void drawVerticalItem ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final BoxAndWhiskerXYDataset boxAndWhiskerData = ( BoxAndWhiskerXYDataset ) dataset;
        final Number x = boxAndWhiskerData.getX ( series, item );
        final Number yMax = boxAndWhiskerData.getMaxRegularValue ( series, item );
        final Number yMin = boxAndWhiskerData.getMinRegularValue ( series, item );
        final Number yMedian = boxAndWhiskerData.getMedianValue ( series, item );
        final Number yAverage = boxAndWhiskerData.getMeanValue ( series, item );
        final Number yQ1Median = boxAndWhiskerData.getQ1Value ( series, item );
        final Number yQ3Median = boxAndWhiskerData.getQ3Value ( series, item );
        List yOutliers = boxAndWhiskerData.getOutliers ( series, item );
        if ( yOutliers == null ) {
            yOutliers = Collections.EMPTY_LIST;
        }
        final double xx = domainAxis.valueToJava2D ( x.doubleValue(), dataArea, plot.getDomainAxisEdge() );
        final RectangleEdge location = plot.getRangeAxisEdge();
        final double yyMax = rangeAxis.valueToJava2D ( yMax.doubleValue(), dataArea, location );
        final double yyMin = rangeAxis.valueToJava2D ( yMin.doubleValue(), dataArea, location );
        final double yyMedian = rangeAxis.valueToJava2D ( yMedian.doubleValue(), dataArea, location );
        double yyAverage = 0.0;
        if ( yAverage != null ) {
            yyAverage = rangeAxis.valueToJava2D ( yAverage.doubleValue(), dataArea, location );
        }
        final double yyQ1Median = rangeAxis.valueToJava2D ( yQ1Median.doubleValue(), dataArea, location );
        final double yyQ3Median = rangeAxis.valueToJava2D ( yQ3Median.doubleValue(), dataArea, location );
        double width;
        double exactBoxWidth = width = this.getBoxWidth();
        final double dataAreaX = dataArea.getMaxX() - dataArea.getMinX();
        final double maxBoxPercent = 0.1;
        final double maxBoxWidth = dataAreaX * maxBoxPercent;
        if ( exactBoxWidth <= 0.0 ) {
            final int itemCount = boxAndWhiskerData.getItemCount ( series );
            exactBoxWidth = dataAreaX / itemCount * 4.5 / 7.0;
            if ( exactBoxWidth < 3.0 ) {
                width = 3.0;
            } else if ( exactBoxWidth > maxBoxWidth ) {
                width = maxBoxWidth;
            } else {
                width = exactBoxWidth;
            }
        }
        g2.setPaint ( this.getItemPaint ( series, item ) );
        final Stroke s = this.getItemStroke ( series, item );
        g2.setStroke ( s );
        g2.draw ( new Line2D.Double ( xx, yyMax, xx, yyQ3Median ) );
        g2.draw ( new Line2D.Double ( xx - width / 2.0, yyMax, xx + width / 2.0, yyMax ) );
        g2.draw ( new Line2D.Double ( xx, yyMin, xx, yyQ1Median ) );
        g2.draw ( new Line2D.Double ( xx - width / 2.0, yyMin, xx + width / 2.0, yyMin ) );
        Shape box;
        if ( yyQ1Median > yyQ3Median ) {
            box = new Rectangle2D.Double ( xx - width / 2.0, yyQ3Median, width, yyQ1Median - yyQ3Median );
        } else {
            box = new Rectangle2D.Double ( xx - width / 2.0, yyQ1Median, width, yyQ3Median - yyQ1Median );
        }
        if ( this.fillBox ) {
            g2.setPaint ( this.lookupBoxPaint ( series, item ) );
            g2.fill ( box );
        }
        g2.setStroke ( this.getItemOutlineStroke ( series, item ) );
        g2.setPaint ( this.getItemOutlinePaint ( series, item ) );
        g2.draw ( box );
        g2.setPaint ( this.getArtifactPaint() );
        g2.draw ( new Line2D.Double ( xx - width / 2.0, yyMedian, xx + width / 2.0, yyMedian ) );
        double aRadius = 0.0;
        final double oRadius = width / 3.0;
        if ( yAverage != null ) {
            aRadius = width / 4.0;
            if ( yyAverage > dataArea.getMinY() - aRadius && yyAverage < dataArea.getMaxY() + aRadius ) {
                final Ellipse2D.Double avgEllipse = new Ellipse2D.Double ( xx - aRadius, yyAverage - aRadius, aRadius * 2.0, aRadius * 2.0 );
                g2.fill ( avgEllipse );
                g2.draw ( avgEllipse );
            }
        }
        final List outliers = new ArrayList();
        final OutlierListCollection outlierListCollection = new OutlierListCollection();
        for ( int i = 0; i < yOutliers.size(); ++i ) {
            final double outlier = yOutliers.get ( i ).doubleValue();
            if ( outlier > boxAndWhiskerData.getMaxOutlier ( series, item ).doubleValue() ) {
                outlierListCollection.setHighFarOut ( true );
            } else if ( outlier < boxAndWhiskerData.getMinOutlier ( series, item ).doubleValue() ) {
                outlierListCollection.setLowFarOut ( true );
            } else if ( outlier > boxAndWhiskerData.getMaxRegularValue ( series, item ).doubleValue() ) {
                final double yyOutlier = rangeAxis.valueToJava2D ( outlier, dataArea, location );
                outliers.add ( new Outlier ( xx, yyOutlier, oRadius ) );
            } else if ( outlier < boxAndWhiskerData.getMinRegularValue ( series, item ).doubleValue() ) {
                final double yyOutlier = rangeAxis.valueToJava2D ( outlier, dataArea, location );
                outliers.add ( new Outlier ( xx, yyOutlier, oRadius ) );
            }
            Collections.sort ( ( List<Comparable> ) outliers );
        }
        for ( final Outlier outlier2 : outliers ) {
            outlierListCollection.add ( outlier2 );
        }
        final double maxAxisValue = rangeAxis.valueToJava2D ( rangeAxis.getUpperBound(), dataArea, location ) + aRadius;
        final double minAxisValue = rangeAxis.valueToJava2D ( rangeAxis.getLowerBound(), dataArea, location ) - aRadius;
        for ( final OutlierList list : outlierListCollection ) {
            final Outlier outlier3 = list.getAveragedOutlier();
            final Point2D point = outlier3.getPoint();
            if ( list.isMultiple() ) {
                this.drawMultipleEllipse ( point, width, oRadius, g2 );
            } else {
                this.drawEllipse ( point, oRadius, g2 );
            }
        }
        if ( outlierListCollection.isHighFarOut() ) {
            this.drawHighFarOut ( aRadius, g2, xx, maxAxisValue );
        }
        if ( outlierListCollection.isLowFarOut() ) {
            this.drawLowFarOut ( aRadius, g2, xx, minAxisValue );
        }
        if ( entities != null && box.intersects ( dataArea ) ) {
            this.addEntity ( entities, box, dataset, series, item, xx, yyAverage );
        }
    }
    protected void drawEllipse ( final Point2D point, final double oRadius, final Graphics2D g2 ) {
        final Ellipse2D.Double dot = new Ellipse2D.Double ( point.getX() + oRadius / 2.0, point.getY(), oRadius, oRadius );
        g2.draw ( dot );
    }
    protected void drawMultipleEllipse ( final Point2D point, final double boxWidth, final double oRadius, final Graphics2D g2 ) {
        final Ellipse2D.Double dot1 = new Ellipse2D.Double ( point.getX() - boxWidth / 2.0 + oRadius, point.getY(), oRadius, oRadius );
        final Ellipse2D.Double dot2 = new Ellipse2D.Double ( point.getX() + boxWidth / 2.0, point.getY(), oRadius, oRadius );
        g2.draw ( dot1 );
        g2.draw ( dot2 );
    }
    protected void drawHighFarOut ( final double aRadius, final Graphics2D g2, final double xx, final double m ) {
        final double side = aRadius * 2.0;
        g2.draw ( new Line2D.Double ( xx - side, m + side, xx + side, m + side ) );
        g2.draw ( new Line2D.Double ( xx - side, m + side, xx, m ) );
        g2.draw ( new Line2D.Double ( xx + side, m + side, xx, m ) );
    }
    protected void drawLowFarOut ( final double aRadius, final Graphics2D g2, final double xx, final double m ) {
        final double side = aRadius * 2.0;
        g2.draw ( new Line2D.Double ( xx - side, m - side, xx + side, m - side ) );
        g2.draw ( new Line2D.Double ( xx - side, m - side, xx, m ) );
        g2.draw ( new Line2D.Double ( xx + side, m - side, xx, m ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYBoxAndWhiskerRenderer ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final XYBoxAndWhiskerRenderer that = ( XYBoxAndWhiskerRenderer ) obj;
        return this.boxWidth == that.getBoxWidth() && PaintUtilities.equal ( this.boxPaint, that.boxPaint ) && PaintUtilities.equal ( this.artifactPaint, that.artifactPaint ) && this.fillBox == that.fillBox;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.boxPaint, stream );
        SerialUtilities.writePaint ( this.artifactPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.boxPaint = SerialUtilities.readPaint ( stream );
        this.artifactPaint = SerialUtilities.readPaint ( stream );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
