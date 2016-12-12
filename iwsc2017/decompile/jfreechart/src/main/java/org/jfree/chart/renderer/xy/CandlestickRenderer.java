package org.jfree.chart.renderer.xy;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.Stroke;
import org.jfree.chart.entity.EntityCollection;
import java.awt.geom.Line2D;
import java.awt.Shape;
import java.awt.Composite;
import java.awt.AlphaComposite;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CandlestickRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 50390395841817121L;
    public static final int WIDTHMETHOD_AVERAGE = 0;
    public static final int WIDTHMETHOD_SMALLEST = 1;
    public static final int WIDTHMETHOD_INTERVALDATA = 2;
    private int autoWidthMethod;
    private double autoWidthFactor;
    private double autoWidthGap;
    private double candleWidth;
    private double maxCandleWidthInMilliseconds;
    private double maxCandleWidth;
    private transient Paint upPaint;
    private transient Paint downPaint;
    private boolean drawVolume;
    private transient Paint volumePaint;
    private transient double maxVolume;
    private boolean useOutlinePaint;
    public CandlestickRenderer() {
        this ( -1.0 );
    }
    public CandlestickRenderer ( final double candleWidth ) {
        this ( candleWidth, true, new HighLowItemLabelGenerator() );
    }
    public CandlestickRenderer ( final double candleWidth, final boolean drawVolume, final XYToolTipGenerator toolTipGenerator ) {
        this.autoWidthMethod = 0;
        this.autoWidthFactor = 0.6428571428571429;
        this.autoWidthGap = 0.0;
        this.maxCandleWidthInMilliseconds = 7.2E7;
        this.setBaseToolTipGenerator ( toolTipGenerator );
        this.candleWidth = candleWidth;
        this.drawVolume = drawVolume;
        this.volumePaint = Color.gray;
        this.upPaint = Color.green;
        this.downPaint = Color.red;
        this.useOutlinePaint = false;
    }
    public double getCandleWidth() {
        return this.candleWidth;
    }
    public void setCandleWidth ( final double width ) {
        if ( width != this.candleWidth ) {
            this.candleWidth = width;
            this.fireChangeEvent();
        }
    }
    public double getMaxCandleWidthInMilliseconds() {
        return this.maxCandleWidthInMilliseconds;
    }
    public void setMaxCandleWidthInMilliseconds ( final double millis ) {
        this.maxCandleWidthInMilliseconds = millis;
        this.fireChangeEvent();
    }
    public int getAutoWidthMethod() {
        return this.autoWidthMethod;
    }
    public void setAutoWidthMethod ( final int autoWidthMethod ) {
        if ( this.autoWidthMethod != autoWidthMethod ) {
            this.autoWidthMethod = autoWidthMethod;
            this.fireChangeEvent();
        }
    }
    public double getAutoWidthFactor() {
        return this.autoWidthFactor;
    }
    public void setAutoWidthFactor ( final double autoWidthFactor ) {
        if ( this.autoWidthFactor != autoWidthFactor ) {
            this.autoWidthFactor = autoWidthFactor;
            this.fireChangeEvent();
        }
    }
    public double getAutoWidthGap() {
        return this.autoWidthGap;
    }
    public void setAutoWidthGap ( final double autoWidthGap ) {
        if ( this.autoWidthGap != autoWidthGap ) {
            this.autoWidthGap = autoWidthGap;
            this.fireChangeEvent();
        }
    }
    public Paint getUpPaint() {
        return this.upPaint;
    }
    public void setUpPaint ( final Paint paint ) {
        this.upPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getDownPaint() {
        return this.downPaint;
    }
    public void setDownPaint ( final Paint paint ) {
        this.downPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getDrawVolume() {
        return this.drawVolume;
    }
    public void setDrawVolume ( final boolean flag ) {
        if ( this.drawVolume != flag ) {
            this.drawVolume = flag;
            this.fireChangeEvent();
        }
    }
    public Paint getVolumePaint() {
        return this.volumePaint;
    }
    public void setVolumePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.volumePaint = paint;
        this.fireChangeEvent();
    }
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }
    public void setUseOutlinePaint ( final boolean use ) {
        if ( this.useOutlinePaint != use ) {
            this.useOutlinePaint = use;
            this.fireChangeEvent();
        }
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset dataset, final PlotRenderingInfo info ) {
        final ValueAxis axis = plot.getDomainAxis();
        final double x1 = axis.getLowerBound();
        final double x2 = x1 + this.maxCandleWidthInMilliseconds;
        final RectangleEdge edge = plot.getDomainAxisEdge();
        final double xx1 = axis.valueToJava2D ( x1, dataArea, edge );
        final double xx2 = axis.valueToJava2D ( x2, dataArea, edge );
        this.maxCandleWidth = Math.abs ( xx2 - xx1 );
        if ( this.drawVolume ) {
            final OHLCDataset highLowDataset = ( OHLCDataset ) dataset;
            this.maxVolume = 0.0;
            for ( int series = 0; series < highLowDataset.getSeriesCount(); ++series ) {
                for ( int item = 0; item < highLowDataset.getItemCount ( series ); ++item ) {
                    final double volume = highLowDataset.getVolumeValue ( series, item );
                    if ( volume > this.maxVolume ) {
                        this.maxVolume = volume;
                    }
                }
            }
        }
        return new XYItemRendererState ( info );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final PlotOrientation orientation = plot.getOrientation();
        boolean horiz;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            horiz = true;
        } else {
            if ( orientation != PlotOrientation.VERTICAL ) {
                return;
            }
            horiz = false;
        }
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final OHLCDataset highLowData = ( OHLCDataset ) dataset;
        final double x = highLowData.getXValue ( series, item );
        final double yHigh = highLowData.getHighValue ( series, item );
        final double yLow = highLowData.getLowValue ( series, item );
        final double yOpen = highLowData.getOpenValue ( series, item );
        final double yClose = highLowData.getCloseValue ( series, item );
        final RectangleEdge domainEdge = plot.getDomainAxisEdge();
        final double xx = domainAxis.valueToJava2D ( x, dataArea, domainEdge );
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final double yyHigh = rangeAxis.valueToJava2D ( yHigh, dataArea, edge );
        final double yyLow = rangeAxis.valueToJava2D ( yLow, dataArea, edge );
        final double yyOpen = rangeAxis.valueToJava2D ( yOpen, dataArea, edge );
        final double yyClose = rangeAxis.valueToJava2D ( yClose, dataArea, edge );
        double volumeWidth;
        double stickWidth;
        if ( this.candleWidth > 0.0 ) {
            volumeWidth = this.candleWidth;
            stickWidth = this.candleWidth;
        } else {
            double xxWidth = 0.0;
            switch ( this.autoWidthMethod ) {
            case 0: {
                final int itemCount = highLowData.getItemCount ( series );
                if ( horiz ) {
                    xxWidth = dataArea.getHeight() / itemCount;
                    break;
                }
                xxWidth = dataArea.getWidth() / itemCount;
                break;
            }
            case 1: {
                final int itemCount = highLowData.getItemCount ( series );
                double lastPos = -1.0;
                xxWidth = dataArea.getWidth();
                for ( int i = 0; i < itemCount; ++i ) {
                    final double pos = domainAxis.valueToJava2D ( highLowData.getXValue ( series, i ), dataArea, domainEdge );
                    if ( lastPos != -1.0 ) {
                        xxWidth = Math.min ( xxWidth, Math.abs ( pos - lastPos ) );
                    }
                    lastPos = pos;
                }
                break;
            }
            case 2: {
                final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
                final double startPos = domainAxis.valueToJava2D ( intervalXYData.getStartXValue ( series, item ), dataArea, plot.getDomainAxisEdge() );
                final double endPos = domainAxis.valueToJava2D ( intervalXYData.getEndXValue ( series, item ), dataArea, plot.getDomainAxisEdge() );
                xxWidth = Math.abs ( endPos - startPos );
                break;
            }
            }
            xxWidth -= 2.0 * this.autoWidthGap;
            xxWidth *= this.autoWidthFactor;
            xxWidth = Math.min ( xxWidth, this.maxCandleWidth );
            volumeWidth = Math.max ( Math.min ( 1.0, this.maxCandleWidth ), xxWidth );
            stickWidth = Math.max ( Math.min ( 3.0, this.maxCandleWidth ), xxWidth );
        }
        final Paint p = this.getItemPaint ( series, item );
        Paint outlinePaint = null;
        if ( this.useOutlinePaint ) {
            outlinePaint = this.getItemOutlinePaint ( series, item );
        }
        final Stroke s = this.getItemStroke ( series, item );
        g2.setStroke ( s );
        if ( this.drawVolume ) {
            final int volume = ( int ) highLowData.getVolumeValue ( series, item );
            final double volumeHeight = volume / this.maxVolume;
            double min;
            double max;
            if ( horiz ) {
                min = dataArea.getMinX();
                max = dataArea.getMaxX();
            } else {
                min = dataArea.getMinY();
                max = dataArea.getMaxY();
            }
            final double zzVolume = volumeHeight * ( max - min );
            g2.setPaint ( this.getVolumePaint() );
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, 0.3f ) );
            if ( horiz ) {
                g2.fill ( new Rectangle2D.Double ( min, xx - volumeWidth / 2.0, zzVolume, volumeWidth ) );
            } else {
                g2.fill ( new Rectangle2D.Double ( xx - volumeWidth / 2.0, max - zzVolume, volumeWidth, zzVolume ) );
            }
            g2.setComposite ( originalComposite );
        }
        if ( this.useOutlinePaint ) {
            g2.setPaint ( outlinePaint );
        } else {
            g2.setPaint ( p );
        }
        final double yyMaxOpenClose = Math.max ( yyOpen, yyClose );
        final double yyMinOpenClose = Math.min ( yyOpen, yyClose );
        final double maxOpenClose = Math.max ( yOpen, yClose );
        final double minOpenClose = Math.min ( yOpen, yClose );
        if ( yHigh > maxOpenClose ) {
            if ( horiz ) {
                g2.draw ( new Line2D.Double ( yyHigh, xx, yyMaxOpenClose, xx ) );
            } else {
                g2.draw ( new Line2D.Double ( xx, yyHigh, xx, yyMaxOpenClose ) );
            }
        }
        if ( yLow < minOpenClose ) {
            if ( horiz ) {
                g2.draw ( new Line2D.Double ( yyLow, xx, yyMinOpenClose, xx ) );
            } else {
                g2.draw ( new Line2D.Double ( xx, yyLow, xx, yyMinOpenClose ) );
            }
        }
        final double length = Math.abs ( yyHigh - yyLow );
        final double base = Math.min ( yyHigh, yyLow );
        Rectangle2D body;
        Rectangle2D hotspot;
        if ( horiz ) {
            body = new Rectangle2D.Double ( yyMinOpenClose, xx - stickWidth / 2.0, yyMaxOpenClose - yyMinOpenClose, stickWidth );
            hotspot = new Rectangle2D.Double ( base, xx - stickWidth / 2.0, length, stickWidth );
        } else {
            body = new Rectangle2D.Double ( xx - stickWidth / 2.0, yyMinOpenClose, stickWidth, yyMaxOpenClose - yyMinOpenClose );
            hotspot = new Rectangle2D.Double ( xx - stickWidth / 2.0, base, stickWidth, length );
        }
        if ( yClose > yOpen ) {
            if ( this.upPaint != null ) {
                g2.setPaint ( this.upPaint );
            } else {
                g2.setPaint ( p );
            }
            g2.fill ( body );
        } else {
            if ( this.downPaint != null ) {
                g2.setPaint ( this.downPaint );
            } else {
                g2.setPaint ( p );
            }
            g2.fill ( body );
        }
        if ( this.useOutlinePaint ) {
            g2.setPaint ( outlinePaint );
        } else {
            g2.setPaint ( p );
        }
        g2.draw ( body );
        if ( entities != null ) {
            this.addEntity ( entities, hotspot, dataset, series, item, 0.0, 0.0 );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CandlestickRenderer ) ) {
            return false;
        }
        final CandlestickRenderer that = ( CandlestickRenderer ) obj;
        return this.candleWidth == that.candleWidth && PaintUtilities.equal ( this.upPaint, that.upPaint ) && PaintUtilities.equal ( this.downPaint, that.downPaint ) && this.drawVolume == that.drawVolume && this.maxCandleWidthInMilliseconds == that.maxCandleWidthInMilliseconds && this.autoWidthMethod == that.autoWidthMethod && this.autoWidthFactor == that.autoWidthFactor && this.autoWidthGap == that.autoWidthGap && this.useOutlinePaint == that.useOutlinePaint && PaintUtilities.equal ( this.volumePaint, that.volumePaint ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.upPaint, stream );
        SerialUtilities.writePaint ( this.downPaint, stream );
        SerialUtilities.writePaint ( this.volumePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.upPaint = SerialUtilities.readPaint ( stream );
        this.downPaint = SerialUtilities.readPaint ( stream );
        this.volumePaint = SerialUtilities.readPaint ( stream );
    }
    public boolean drawVolume() {
        return this.drawVolume;
    }
}
