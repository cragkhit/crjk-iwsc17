package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.util.Collection;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.TextAnchor;
import java.awt.FontMetrics;
import org.jfree.text.TextUtilities;
import java.awt.geom.Line2D;
import java.awt.geom.Arc2D;
import org.jfree.ui.RectangleInsets;
import java.awt.geom.Ellipse2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.awt.Shape;
import org.jfree.chart.LegendItem;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItemCollection;
import java.util.Collections;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.awt.Color;
import java.util.List;
import java.util.ResourceBundle;
import java.text.NumberFormat;
import org.jfree.data.Range;
import org.jfree.data.general.ValueDataset;
import java.awt.Font;
import java.awt.Paint;
import java.io.Serializable;
public class MeterPlot extends Plot implements Serializable, Cloneable {
    private static final long serialVersionUID = 2987472457734470962L;
    static final Paint DEFAULT_DIAL_BACKGROUND_PAINT;
    static final Paint DEFAULT_NEEDLE_PAINT;
    static final Font DEFAULT_VALUE_FONT;
    static final Paint DEFAULT_VALUE_PAINT;
    public static final int DEFAULT_METER_ANGLE = 270;
    public static final float DEFAULT_BORDER_SIZE = 3.0f;
    public static final float DEFAULT_CIRCLE_SIZE = 10.0f;
    public static final Font DEFAULT_LABEL_FONT;
    private ValueDataset dataset;
    private DialShape shape;
    private int meterAngle;
    private Range range;
    private double tickSize;
    private transient Paint tickPaint;
    private String units;
    private Font valueFont;
    private transient Paint valuePaint;
    private boolean drawBorder;
    private transient Paint dialOutlinePaint;
    private transient Paint dialBackgroundPaint;
    private transient Paint needlePaint;
    private boolean tickLabelsVisible;
    private Font tickLabelFont;
    private transient Paint tickLabelPaint;
    private NumberFormat tickLabelFormat;
    protected static ResourceBundle localizationResources;
    private List intervals;
    public MeterPlot() {
        this ( null );
    }
    public MeterPlot ( final ValueDataset dataset ) {
        this.shape = DialShape.CIRCLE;
        this.meterAngle = 270;
        this.range = new Range ( 0.0, 100.0 );
        this.tickSize = 10.0;
        this.tickPaint = Color.white;
        this.units = "Units";
        this.needlePaint = MeterPlot.DEFAULT_NEEDLE_PAINT;
        this.tickLabelsVisible = true;
        this.tickLabelFont = MeterPlot.DEFAULT_LABEL_FONT;
        this.tickLabelPaint = Color.black;
        this.tickLabelFormat = NumberFormat.getInstance();
        this.valueFont = MeterPlot.DEFAULT_VALUE_FONT;
        this.valuePaint = MeterPlot.DEFAULT_VALUE_PAINT;
        this.dialBackgroundPaint = MeterPlot.DEFAULT_DIAL_BACKGROUND_PAINT;
        this.intervals = new ArrayList();
        this.setDataset ( dataset );
    }
    public DialShape getDialShape() {
        return this.shape;
    }
    public void setDialShape ( final DialShape shape ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        this.shape = shape;
        this.fireChangeEvent();
    }
    public int getMeterAngle() {
        return this.meterAngle;
    }
    public void setMeterAngle ( final int angle ) {
        if ( angle < 1 || angle > 360 ) {
            throw new IllegalArgumentException ( "Invalid 'angle' (" + angle + ")" );
        }
        this.meterAngle = angle;
        this.fireChangeEvent();
    }
    public Range getRange() {
        return this.range;
    }
    public void setRange ( final Range range ) {
        ParamChecks.nullNotPermitted ( range, "range" );
        if ( range.getLength() <= 0.0 ) {
            throw new IllegalArgumentException ( "Range length must be positive." );
        }
        this.range = range;
        this.fireChangeEvent();
    }
    public double getTickSize() {
        return this.tickSize;
    }
    public void setTickSize ( final double size ) {
        if ( size <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'size' > 0." );
        }
        this.tickSize = size;
        this.fireChangeEvent();
    }
    public Paint getTickPaint() {
        return this.tickPaint;
    }
    public void setTickPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickPaint = paint;
        this.fireChangeEvent();
    }
    public String getUnits() {
        return this.units;
    }
    public void setUnits ( final String units ) {
        this.units = units;
        this.fireChangeEvent();
    }
    public Paint getNeedlePaint() {
        return this.needlePaint;
    }
    public void setNeedlePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.needlePaint = paint;
        this.fireChangeEvent();
    }
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }
    public void setTickLabelsVisible ( final boolean visible ) {
        if ( this.tickLabelsVisible != visible ) {
            this.tickLabelsVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }
    public void setTickLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        if ( !this.tickLabelFont.equals ( font ) ) {
            this.tickLabelFont = font;
            this.fireChangeEvent();
        }
    }
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }
    public void setTickLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        if ( !this.tickLabelPaint.equals ( paint ) ) {
            this.tickLabelPaint = paint;
            this.fireChangeEvent();
        }
    }
    public NumberFormat getTickLabelFormat() {
        return this.tickLabelFormat;
    }
    public void setTickLabelFormat ( final NumberFormat format ) {
        ParamChecks.nullNotPermitted ( format, "format" );
        this.tickLabelFormat = format;
        this.fireChangeEvent();
    }
    public Font getValueFont() {
        return this.valueFont;
    }
    public void setValueFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.valueFont = font;
        this.fireChangeEvent();
    }
    public Paint getValuePaint() {
        return this.valuePaint;
    }
    public void setValuePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.valuePaint = paint;
        this.fireChangeEvent();
    }
    public Paint getDialBackgroundPaint() {
        return this.dialBackgroundPaint;
    }
    public void setDialBackgroundPaint ( final Paint paint ) {
        this.dialBackgroundPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getDrawBorder() {
        return this.drawBorder;
    }
    public void setDrawBorder ( final boolean draw ) {
        this.drawBorder = draw;
        this.fireChangeEvent();
    }
    public Paint getDialOutlinePaint() {
        return this.dialOutlinePaint;
    }
    public void setDialOutlinePaint ( final Paint paint ) {
        this.dialOutlinePaint = paint;
        this.fireChangeEvent();
    }
    public ValueDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final ValueDataset dataset ) {
        final ValueDataset existing = this.dataset;
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( ( this.dataset = dataset ) != null ) {
            this.setDatasetGroup ( dataset.getGroup() );
            dataset.addChangeListener ( this );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        this.datasetChanged ( event );
    }
    public List getIntervals() {
        return Collections.unmodifiableList ( ( List<?> ) this.intervals );
    }
    public void addInterval ( final MeterInterval interval ) {
        ParamChecks.nullNotPermitted ( interval, "interval" );
        this.intervals.add ( interval );
        this.fireChangeEvent();
    }
    public void clearIntervals() {
        this.intervals.clear();
        this.fireChangeEvent();
    }
    @Override
    public LegendItemCollection getLegendItems() {
        final LegendItemCollection result = new LegendItemCollection();
        for ( final MeterInterval mi : this.intervals ) {
            Paint color = mi.getBackgroundPaint();
            if ( color == null ) {
                color = mi.getOutlinePaint();
            }
            final LegendItem item = new LegendItem ( mi.getLabel(), mi.getLabel(), null, null, new Rectangle2D.Double ( -4.0, -4.0, 8.0, 8.0 ), color );
            item.setDataset ( this.getDataset() );
            result.add ( item );
        }
        return result;
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        area.setRect ( area.getX() + 4.0, area.getY() + 4.0, area.getWidth() - 8.0, area.getHeight() - 8.0 );
        if ( this.drawBorder ) {
            this.drawBackground ( g2, area );
        }
        final double gapHorizontal = 6.0;
        final double gapVertical = 6.0;
        double meterX = area.getX() + gapHorizontal / 2.0;
        double meterY = area.getY() + gapVertical / 2.0;
        double meterW = area.getWidth() - gapHorizontal;
        double meterH = area.getHeight() - gapVertical + ( ( this.meterAngle <= 180 && this.shape != DialShape.CIRCLE ) ? ( area.getHeight() / 1.25 ) : 0.0 );
        final double min = Math.min ( meterW, meterH ) / 2.0;
        meterX = ( meterX + meterX + meterW ) / 2.0 - min;
        meterY = ( meterY + meterY + meterH ) / 2.0 - min;
        meterW = 2.0 * min;
        meterH = 2.0 * min;
        final Rectangle2D meterArea = new Rectangle2D.Double ( meterX, meterY, meterW, meterH );
        final Rectangle2D.Double originalArea = new Rectangle2D.Double ( meterArea.getX() - 4.0, meterArea.getY() - 4.0, meterArea.getWidth() + 8.0, meterArea.getHeight() + 8.0 );
        final double meterMiddleX = meterArea.getCenterX();
        final double meterMiddleY = meterArea.getCenterY();
        final ValueDataset data = this.getDataset();
        if ( data != null ) {
            final double dataMin = this.range.getLowerBound();
            final double dataMax = this.range.getUpperBound();
            final Shape savedClip = g2.getClip();
            g2.clip ( originalArea );
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
            if ( this.dialBackgroundPaint != null ) {
                this.fillArc ( g2, originalArea, dataMin, dataMax, this.dialBackgroundPaint, true );
            }
            this.drawTicks ( g2, meterArea, dataMin, dataMax );
            this.drawArcForInterval ( g2, meterArea, new MeterInterval ( "", this.range, this.dialOutlinePaint, new BasicStroke ( 1.0f ), null ) );
            for ( final MeterInterval interval : this.intervals ) {
                this.drawArcForInterval ( g2, meterArea, interval );
            }
            final Number n = data.getValue();
            if ( n != null ) {
                final double value = n.doubleValue();
                this.drawValueLabel ( g2, meterArea );
                if ( this.range.contains ( value ) ) {
                    g2.setPaint ( this.needlePaint );
                    g2.setStroke ( new BasicStroke ( 2.0f ) );
                    final double radius = meterArea.getWidth() / 2.0 + 3.0 + 15.0;
                    final double valueAngle = this.valueToAngle ( value );
                    final double valueP1 = meterMiddleX + radius * Math.cos ( 3.141592653589793 * ( valueAngle / 180.0 ) );
                    final double valueP2 = meterMiddleY - radius * Math.sin ( 3.141592653589793 * ( valueAngle / 180.0 ) );
                    final Polygon arrow = new Polygon();
                    if ( ( valueAngle > 135.0 && valueAngle < 225.0 ) || ( valueAngle < 45.0 && valueAngle > -45.0 ) ) {
                        final double valueP3 = meterMiddleY - 2.5;
                        final double valueP4 = meterMiddleY + 2.5;
                        arrow.addPoint ( ( int ) meterMiddleX, ( int ) valueP3 );
                        arrow.addPoint ( ( int ) meterMiddleX, ( int ) valueP4 );
                    } else {
                        arrow.addPoint ( ( int ) ( meterMiddleX - 2.5 ), ( int ) meterMiddleY );
                        arrow.addPoint ( ( int ) ( meterMiddleX + 2.5 ), ( int ) meterMiddleY );
                    }
                    arrow.addPoint ( ( int ) valueP1, ( int ) valueP2 );
                    g2.fill ( arrow );
                    final Ellipse2D circle = new Ellipse2D.Double ( meterMiddleX - 5.0, meterMiddleY - 5.0, 10.0, 10.0 );
                    g2.fill ( circle );
                }
            }
            g2.setClip ( savedClip );
            g2.setComposite ( originalComposite );
        }
        if ( this.drawBorder ) {
            this.drawOutline ( g2, area );
        }
    }
    protected void drawArcForInterval ( final Graphics2D g2, final Rectangle2D meterArea, final MeterInterval interval ) {
        final double minValue = interval.getRange().getLowerBound();
        final double maxValue = interval.getRange().getUpperBound();
        final Paint outlinePaint = interval.getOutlinePaint();
        final Stroke outlineStroke = interval.getOutlineStroke();
        final Paint backgroundPaint = interval.getBackgroundPaint();
        if ( backgroundPaint != null ) {
            this.fillArc ( g2, meterArea, minValue, maxValue, backgroundPaint, false );
        }
        if ( outlinePaint != null ) {
            if ( outlineStroke != null ) {
                this.drawArc ( g2, meterArea, minValue, maxValue, outlinePaint, outlineStroke );
            }
            this.drawTick ( g2, meterArea, minValue, true );
            this.drawTick ( g2, meterArea, maxValue, true );
        }
    }
    protected void drawArc ( final Graphics2D g2, final Rectangle2D area, final double minValue, final double maxValue, final Paint paint, final Stroke stroke ) {
        final double startAngle = this.valueToAngle ( maxValue );
        final double endAngle = this.valueToAngle ( minValue );
        final double extent = endAngle - startAngle;
        final double x = area.getX();
        final double y = area.getY();
        final double w = area.getWidth();
        final double h = area.getHeight();
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        if ( paint != null && stroke != null ) {
            final Arc2D.Double arc = new Arc2D.Double ( x, y, w, h, startAngle, extent, 0 );
            g2.setPaint ( paint );
            g2.setStroke ( stroke );
            g2.draw ( arc );
        }
    }
    protected void fillArc ( final Graphics2D g2, final Rectangle2D area, final double minValue, final double maxValue, final Paint paint, final boolean dial ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        final double startAngle = this.valueToAngle ( maxValue );
        final double endAngle = this.valueToAngle ( minValue );
        double extent = endAngle - startAngle;
        final double x = area.getX();
        final double y = area.getY();
        final double w = area.getWidth();
        final double h = area.getHeight();
        int joinType = 0;
        if ( this.shape == DialShape.PIE ) {
            joinType = 2;
        } else if ( this.shape == DialShape.CHORD ) {
            if ( dial && this.meterAngle > 180 ) {
                joinType = 1;
            } else {
                joinType = 2;
            }
        } else {
            if ( this.shape != DialShape.CIRCLE ) {
                throw new IllegalStateException ( "DialShape not recognised." );
            }
            joinType = 2;
            if ( dial ) {
                extent = 360.0;
            }
        }
        g2.setPaint ( paint );
        final Arc2D.Double arc = new Arc2D.Double ( x, y, w, h, startAngle, extent, joinType );
        g2.fill ( arc );
    }
    public double valueToAngle ( double value ) {
        value -= this.range.getLowerBound();
        final double baseAngle = 180 + ( this.meterAngle - 180 ) / 2;
        return baseAngle - value / this.range.getLength() * this.meterAngle;
    }
    protected void drawTicks ( final Graphics2D g2, final Rectangle2D meterArea, final double minValue, final double maxValue ) {
        for ( double v = minValue; v <= maxValue; v += this.tickSize ) {
            this.drawTick ( g2, meterArea, v );
        }
    }
    protected void drawTick ( final Graphics2D g2, final Rectangle2D meterArea, final double value ) {
        this.drawTick ( g2, meterArea, value, false );
    }
    protected void drawTick ( final Graphics2D g2, final Rectangle2D meterArea, final double value, final boolean label ) {
        final double valueAngle = this.valueToAngle ( value );
        final double meterMiddleX = meterArea.getCenterX();
        final double meterMiddleY = meterArea.getCenterY();
        g2.setPaint ( this.tickPaint );
        g2.setStroke ( new BasicStroke ( 2.0f ) );
        final double radius = meterArea.getWidth() / 2.0 + 3.0;
        final double radius2 = radius - 15.0;
        final double valueP1X = meterMiddleX + radius * Math.cos ( 3.141592653589793 * ( valueAngle / 180.0 ) );
        final double valueP1Y = meterMiddleY - radius * Math.sin ( 3.141592653589793 * ( valueAngle / 180.0 ) );
        final double valueP2X = meterMiddleX + radius2 * Math.cos ( 3.141592653589793 * ( valueAngle / 180.0 ) );
        final double valueP2Y = meterMiddleY - radius2 * Math.sin ( 3.141592653589793 * ( valueAngle / 180.0 ) );
        final Line2D.Double line = new Line2D.Double ( valueP1X, valueP1Y, valueP2X, valueP2Y );
        g2.draw ( line );
        if ( this.tickLabelsVisible && label ) {
            final String tickLabel = this.tickLabelFormat.format ( value );
            g2.setFont ( this.tickLabelFont );
            g2.setPaint ( this.tickLabelPaint );
            final FontMetrics fm = g2.getFontMetrics();
            final Rectangle2D tickLabelBounds = TextUtilities.getTextBounds ( tickLabel, g2, fm );
            double x = valueP2X;
            double y = valueP2Y;
            if ( valueAngle == 90.0 || valueAngle == 270.0 ) {
                x -= tickLabelBounds.getWidth() / 2.0;
            } else if ( valueAngle < 90.0 || valueAngle > 270.0 ) {
                x -= tickLabelBounds.getWidth();
            }
            if ( ( valueAngle > 135.0 && valueAngle < 225.0 ) || valueAngle > 315.0 || valueAngle < 45.0 ) {
                y -= tickLabelBounds.getHeight() / 2.0;
            } else {
                y += tickLabelBounds.getHeight() / 2.0;
            }
            g2.drawString ( tickLabel, ( float ) x, ( float ) y );
        }
    }
    protected void drawValueLabel ( final Graphics2D g2, final Rectangle2D area ) {
        g2.setFont ( this.valueFont );
        g2.setPaint ( this.valuePaint );
        String valueStr = "No value";
        if ( this.dataset != null ) {
            final Number n = this.dataset.getValue();
            if ( n != null ) {
                valueStr = this.tickLabelFormat.format ( n.doubleValue() ) + " " + this.units;
            }
        }
        final float x = ( float ) area.getCenterX();
        final float y = ( float ) area.getCenterY() + 10.0f;
        TextUtilities.drawAlignedString ( valueStr, g2, x, y, TextAnchor.TOP_CENTER );
    }
    @Override
    public String getPlotType() {
        return MeterPlot.localizationResources.getString ( "Meter_Plot" );
    }
    @Override
    public void zoom ( final double percent ) {
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof MeterPlot ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final MeterPlot that = ( MeterPlot ) obj;
        return ObjectUtilities.equal ( ( Object ) this.units, ( Object ) that.units ) && ObjectUtilities.equal ( ( Object ) this.range, ( Object ) that.range ) && ObjectUtilities.equal ( ( Object ) this.intervals, ( Object ) that.intervals ) && PaintUtilities.equal ( this.dialOutlinePaint, that.dialOutlinePaint ) && this.shape == that.shape && PaintUtilities.equal ( this.dialBackgroundPaint, that.dialBackgroundPaint ) && PaintUtilities.equal ( this.needlePaint, that.needlePaint ) && ObjectUtilities.equal ( ( Object ) this.valueFont, ( Object ) that.valueFont ) && PaintUtilities.equal ( this.valuePaint, that.valuePaint ) && PaintUtilities.equal ( this.tickPaint, that.tickPaint ) && this.tickSize == that.tickSize && this.tickLabelsVisible == that.tickLabelsVisible && ObjectUtilities.equal ( ( Object ) this.tickLabelFont, ( Object ) that.tickLabelFont ) && PaintUtilities.equal ( this.tickLabelPaint, that.tickLabelPaint ) && ObjectUtilities.equal ( ( Object ) this.tickLabelFormat, ( Object ) that.tickLabelFormat ) && this.drawBorder == that.drawBorder && this.meterAngle == that.meterAngle;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.dialBackgroundPaint, stream );
        SerialUtilities.writePaint ( this.dialOutlinePaint, stream );
        SerialUtilities.writePaint ( this.needlePaint, stream );
        SerialUtilities.writePaint ( this.valuePaint, stream );
        SerialUtilities.writePaint ( this.tickPaint, stream );
        SerialUtilities.writePaint ( this.tickLabelPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dialBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.dialOutlinePaint = SerialUtilities.readPaint ( stream );
        this.needlePaint = SerialUtilities.readPaint ( stream );
        this.valuePaint = SerialUtilities.readPaint ( stream );
        this.tickPaint = SerialUtilities.readPaint ( stream );
        this.tickLabelPaint = SerialUtilities.readPaint ( stream );
        if ( this.dataset != null ) {
            this.dataset.addChangeListener ( this );
        }
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final MeterPlot clone = ( MeterPlot ) super.clone();
        clone.tickLabelFormat = ( NumberFormat ) this.tickLabelFormat.clone();
        clone.intervals = new ArrayList ( this.intervals );
        if ( clone.dataset != null ) {
            clone.dataset.addChangeListener ( clone );
        }
        return clone;
    }
    static {
        DEFAULT_DIAL_BACKGROUND_PAINT = Color.black;
        DEFAULT_NEEDLE_PAINT = Color.green;
        DEFAULT_VALUE_FONT = new Font ( "SansSerif", 1, 12 );
        DEFAULT_VALUE_PAINT = Color.yellow;
        DEFAULT_LABEL_FONT = new Font ( "SansSerif", 1, 10 );
        MeterPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
