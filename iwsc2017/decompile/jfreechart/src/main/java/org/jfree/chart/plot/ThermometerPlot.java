package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.LegendItemCollection;
import java.awt.FontMetrics;
import org.jfree.data.Range;
import java.awt.geom.Line2D;
import org.jfree.ui.RectangleEdge;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.util.UnitType;
import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.data.general.DefaultValueDataset;
import java.util.ResourceBundle;
import java.text.NumberFormat;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.ValueDataset;
import java.io.Serializable;
public class ThermometerPlot extends Plot implements ValueAxisPlot, Zoomable, Cloneable, Serializable {
    private static final long serialVersionUID = 4087093313147984390L;
    public static final int UNITS_NONE = 0;
    public static final int UNITS_FAHRENHEIT = 1;
    public static final int UNITS_CELCIUS = 2;
    public static final int UNITS_KELVIN = 3;
    public static final int NONE = 0;
    public static final int RIGHT = 1;
    public static final int LEFT = 2;
    public static final int BULB = 3;
    public static final int NORMAL = 0;
    public static final int WARNING = 1;
    public static final int CRITICAL = 2;
    protected static final int BULB_RADIUS = 40;
    protected static final int BULB_DIAMETER = 80;
    protected static final int COLUMN_RADIUS = 20;
    protected static final int COLUMN_DIAMETER = 40;
    protected static final int GAP_RADIUS = 5;
    protected static final int GAP_DIAMETER = 10;
    protected static final int AXIS_GAP = 10;
    protected static final String[] UNITS;
    protected static final int RANGE_LOW = 0;
    protected static final int RANGE_HIGH = 1;
    protected static final int DISPLAY_LOW = 2;
    protected static final int DISPLAY_HIGH = 3;
    protected static final double DEFAULT_LOWER_BOUND = 0.0;
    protected static final double DEFAULT_UPPER_BOUND = 100.0;
    protected static final int DEFAULT_BULB_RADIUS = 40;
    protected static final int DEFAULT_COLUMN_RADIUS = 20;
    protected static final int DEFAULT_GAP = 5;
    private ValueDataset dataset;
    private ValueAxis rangeAxis;
    private double lowerBound;
    private double upperBound;
    private int bulbRadius;
    private int columnRadius;
    private int gap;
    private RectangleInsets padding;
    private transient Stroke thermometerStroke;
    private transient Paint thermometerPaint;
    private int units;
    private int valueLocation;
    private int axisLocation;
    private Font valueFont;
    private transient Paint valuePaint;
    private NumberFormat valueFormat;
    private transient Paint mercuryPaint;
    private boolean showValueLines;
    private int subrange;
    private double[][] subrangeInfo;
    private boolean followDataInSubranges;
    private boolean useSubrangePaint;
    private transient Paint[] subrangePaint;
    private boolean subrangeIndicatorsVisible;
    private transient Stroke subrangeIndicatorStroke;
    private transient Stroke rangeIndicatorStroke;
    protected static ResourceBundle localizationResources;
    public ThermometerPlot() {
        this ( new DefaultValueDataset() );
    }
    public ThermometerPlot ( final ValueDataset dataset ) {
        this.lowerBound = 0.0;
        this.upperBound = 100.0;
        this.bulbRadius = 40;
        this.columnRadius = 20;
        this.gap = 5;
        this.thermometerStroke = new BasicStroke ( 1.0f );
        this.thermometerPaint = Color.black;
        this.units = 2;
        this.valueLocation = 3;
        this.axisLocation = 2;
        this.valueFont = new Font ( "SansSerif", 1, 16 );
        this.valuePaint = Color.white;
        this.valueFormat = new DecimalFormat();
        this.mercuryPaint = Color.lightGray;
        this.showValueLines = false;
        this.subrange = -1;
        this.subrangeInfo = new double[][] { { 0.0, 50.0, 0.0, 50.0 }, { 50.0, 75.0, 50.0, 75.0 }, { 75.0, 100.0, 75.0, 100.0 } };
        this.followDataInSubranges = false;
        this.useSubrangePaint = true;
        this.subrangePaint = new Paint[] { Color.green, Color.orange, Color.red };
        this.subrangeIndicatorsVisible = true;
        this.subrangeIndicatorStroke = new BasicStroke ( 2.0f );
        this.rangeIndicatorStroke = new BasicStroke ( 3.0f );
        this.padding = new RectangleInsets ( UnitType.RELATIVE, 0.05, 0.05, 0.05, 0.05 );
        this.dataset = dataset;
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        final NumberAxis axis = new NumberAxis ( null );
        axis.setStandardTickUnits ( NumberAxis.createIntegerTickUnits() );
        axis.setAxisLineVisible ( false );
        axis.setPlot ( this );
        axis.addChangeListener ( this );
        this.rangeAxis = axis;
        this.setAxisRange();
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
    public ValueAxis getRangeAxis() {
        return this.rangeAxis;
    }
    public void setRangeAxis ( final ValueAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        this.rangeAxis.removeChangeListener ( this );
        axis.setPlot ( this );
        axis.addChangeListener ( this );
        this.rangeAxis = axis;
        this.fireChangeEvent();
    }
    public double getLowerBound() {
        return this.lowerBound;
    }
    public void setLowerBound ( final double lower ) {
        this.lowerBound = lower;
        this.setAxisRange();
    }
    public double getUpperBound() {
        return this.upperBound;
    }
    public void setUpperBound ( final double upper ) {
        this.upperBound = upper;
        this.setAxisRange();
    }
    public void setRange ( final double lower, final double upper ) {
        this.lowerBound = lower;
        this.upperBound = upper;
        this.setAxisRange();
    }
    public RectangleInsets getPadding() {
        return this.padding;
    }
    public void setPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.padding = padding;
        this.fireChangeEvent();
    }
    public Stroke getThermometerStroke() {
        return this.thermometerStroke;
    }
    public void setThermometerStroke ( final Stroke s ) {
        if ( s != null ) {
            this.thermometerStroke = s;
            this.fireChangeEvent();
        }
    }
    public Paint getThermometerPaint() {
        return this.thermometerPaint;
    }
    public void setThermometerPaint ( final Paint paint ) {
        if ( paint != null ) {
            this.thermometerPaint = paint;
            this.fireChangeEvent();
        }
    }
    public int getUnits() {
        return this.units;
    }
    public void setUnits ( final int u ) {
        if ( u >= 0 && u < ThermometerPlot.UNITS.length && this.units != u ) {
            this.units = u;
            this.fireChangeEvent();
        }
    }
    public void setUnits ( String u ) {
        if ( u == null ) {
            return;
        }
        u = u.toUpperCase().trim();
        for ( int i = 0; i < ThermometerPlot.UNITS.length; ++i ) {
            if ( u.equals ( ThermometerPlot.UNITS[i].toUpperCase().trim() ) ) {
                this.setUnits ( i );
                i = ThermometerPlot.UNITS.length;
            }
        }
    }
    public int getValueLocation() {
        return this.valueLocation;
    }
    public void setValueLocation ( final int location ) {
        if ( location >= 0 && location < 4 ) {
            this.valueLocation = location;
            this.fireChangeEvent();
            return;
        }
        throw new IllegalArgumentException ( "Location not recognised." );
    }
    public int getAxisLocation() {
        return this.axisLocation;
    }
    public void setAxisLocation ( final int location ) {
        if ( location >= 0 && location < 3 ) {
            this.axisLocation = location;
            this.fireChangeEvent();
            return;
        }
        throw new IllegalArgumentException ( "Location not recognised." );
    }
    public Font getValueFont() {
        return this.valueFont;
    }
    public void setValueFont ( final Font f ) {
        ParamChecks.nullNotPermitted ( f, "f" );
        if ( !this.valueFont.equals ( f ) ) {
            this.valueFont = f;
            this.fireChangeEvent();
        }
    }
    public Paint getValuePaint() {
        return this.valuePaint;
    }
    public void setValuePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        if ( !this.valuePaint.equals ( paint ) ) {
            this.valuePaint = paint;
            this.fireChangeEvent();
        }
    }
    public void setValueFormat ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.valueFormat = formatter;
        this.fireChangeEvent();
    }
    public Paint getMercuryPaint() {
        return this.mercuryPaint;
    }
    public void setMercuryPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.mercuryPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getShowValueLines() {
        return this.showValueLines;
    }
    public void setShowValueLines ( final boolean b ) {
        this.showValueLines = b;
        this.fireChangeEvent();
    }
    public void setSubrangeInfo ( final int range, final double low, final double hi ) {
        this.setSubrangeInfo ( range, low, hi, low, hi );
    }
    public void setSubrangeInfo ( final int range, final double rangeLow, final double rangeHigh, final double displayLow, final double displayHigh ) {
        if ( range >= 0 && range < 3 ) {
            this.setSubrange ( range, rangeLow, rangeHigh );
            this.setDisplayRange ( range, displayLow, displayHigh );
            this.setAxisRange();
            this.fireChangeEvent();
        }
    }
    public void setSubrange ( final int range, final double low, final double high ) {
        if ( range >= 0 && range < 3 ) {
            this.subrangeInfo[range][1] = high;
            this.subrangeInfo[range][0] = low;
        }
    }
    public void setDisplayRange ( final int range, final double low, final double high ) {
        if ( range >= 0 && range < this.subrangeInfo.length && isValidNumber ( high ) && isValidNumber ( low ) ) {
            if ( high > low ) {
                this.subrangeInfo[range][3] = high;
                this.subrangeInfo[range][2] = low;
            } else {
                this.subrangeInfo[range][3] = low;
                this.subrangeInfo[range][2] = high;
            }
        }
    }
    public Paint getSubrangePaint ( final int range ) {
        if ( range >= 0 && range < this.subrangePaint.length ) {
            return this.subrangePaint[range];
        }
        return this.mercuryPaint;
    }
    public void setSubrangePaint ( final int range, final Paint paint ) {
        if ( range >= 0 && range < this.subrangePaint.length && paint != null ) {
            this.subrangePaint[range] = paint;
            this.fireChangeEvent();
        }
    }
    public boolean getFollowDataInSubranges() {
        return this.followDataInSubranges;
    }
    public void setFollowDataInSubranges ( final boolean flag ) {
        this.followDataInSubranges = flag;
        this.fireChangeEvent();
    }
    public boolean getUseSubrangePaint() {
        return this.useSubrangePaint;
    }
    public void setUseSubrangePaint ( final boolean flag ) {
        this.useSubrangePaint = flag;
        this.fireChangeEvent();
    }
    public int getBulbRadius() {
        return this.bulbRadius;
    }
    public void setBulbRadius ( final int r ) {
        this.bulbRadius = r;
        this.fireChangeEvent();
    }
    public int getBulbDiameter() {
        return this.getBulbRadius() * 2;
    }
    public int getColumnRadius() {
        return this.columnRadius;
    }
    public void setColumnRadius ( final int r ) {
        this.columnRadius = r;
        this.fireChangeEvent();
    }
    public int getColumnDiameter() {
        return this.getColumnRadius() * 2;
    }
    public int getGap() {
        return this.gap;
    }
    public void setGap ( final int gap ) {
        this.gap = gap;
        this.fireChangeEvent();
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final RoundRectangle2D outerStem = new RoundRectangle2D.Double();
        final RoundRectangle2D innerStem = new RoundRectangle2D.Double();
        final RoundRectangle2D mercuryStem = new RoundRectangle2D.Double();
        final Ellipse2D outerBulb = new Ellipse2D.Double();
        final Ellipse2D innerBulb = new Ellipse2D.Double();
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        this.drawBackground ( g2, area );
        final Rectangle2D interior = ( Rectangle2D ) area.clone();
        this.padding.trim ( interior );
        final int midX = ( int ) ( interior.getX() + interior.getWidth() / 2.0 );
        final int midY = ( int ) ( interior.getY() + interior.getHeight() / 2.0 );
        final int stemTop = ( int ) ( interior.getMinY() + this.getBulbRadius() );
        final int stemBottom = ( int ) ( interior.getMaxY() - this.getBulbDiameter() );
        final Rectangle2D dataArea = new Rectangle2D.Double ( midX - this.getColumnRadius(), stemTop, this.getColumnRadius(), stemBottom - stemTop );
        outerBulb.setFrame ( midX - this.getBulbRadius(), stemBottom, this.getBulbDiameter(), this.getBulbDiameter() );
        outerStem.setRoundRect ( midX - this.getColumnRadius(), interior.getMinY(), this.getColumnDiameter(), stemBottom + this.getBulbDiameter() - stemTop, this.getColumnDiameter(), this.getColumnDiameter() );
        final Area outerThermometer = new Area ( outerBulb );
        Area tempArea = new Area ( outerStem );
        outerThermometer.add ( tempArea );
        innerBulb.setFrame ( midX - this.getBulbRadius() + this.getGap(), stemBottom + this.getGap(), this.getBulbDiameter() - this.getGap() * 2, this.getBulbDiameter() - this.getGap() * 2 );
        innerStem.setRoundRect ( midX - this.getColumnRadius() + this.getGap(), interior.getMinY() + this.getGap(), this.getColumnDiameter() - this.getGap() * 2, stemBottom + this.getBulbDiameter() - this.getGap() * 2 - stemTop, this.getColumnDiameter() - this.getGap() * 2, this.getColumnDiameter() - this.getGap() * 2 );
        final Area innerThermometer = new Area ( innerBulb );
        tempArea = new Area ( innerStem );
        innerThermometer.add ( tempArea );
        if ( this.dataset != null && this.dataset.getValue() != null ) {
            final double current = this.dataset.getValue().doubleValue();
            final double ds = this.rangeAxis.valueToJava2D ( current, dataArea, RectangleEdge.LEFT );
            int i = this.getColumnDiameter() - this.getGap() * 2;
            final int j = this.getColumnRadius() - this.getGap();
            int l = i / 2;
            int k = ( int ) Math.round ( ds );
            if ( k < this.getGap() + interior.getMinY() ) {
                k = ( int ) ( this.getGap() + interior.getMinY() );
                l = this.getBulbRadius();
            }
            final Area mercury = new Area ( innerBulb );
            if ( k < stemBottom + this.getBulbRadius() ) {
                mercuryStem.setRoundRect ( midX - j, k, i, stemBottom + this.getBulbRadius() - k, l, l );
                tempArea = new Area ( mercuryStem );
                mercury.add ( tempArea );
            }
            g2.setPaint ( this.getCurrentPaint() );
            g2.fill ( mercury );
            if ( this.subrangeIndicatorsVisible ) {
                g2.setStroke ( this.subrangeIndicatorStroke );
                final Range range = this.rangeAxis.getRange();
                double value = this.subrangeInfo[0][0];
                if ( range.contains ( value ) ) {
                    final double x = midX + this.getColumnRadius() + 2;
                    final double y = this.rangeAxis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
                    final Line2D line = new Line2D.Double ( x, y, x + 10.0, y );
                    g2.setPaint ( this.subrangePaint[0] );
                    g2.draw ( line );
                }
                value = this.subrangeInfo[1][0];
                if ( range.contains ( value ) ) {
                    final double x = midX + this.getColumnRadius() + 2;
                    final double y = this.rangeAxis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
                    final Line2D line = new Line2D.Double ( x, y, x + 10.0, y );
                    g2.setPaint ( this.subrangePaint[1] );
                    g2.draw ( line );
                }
                value = this.subrangeInfo[2][0];
                if ( range.contains ( value ) ) {
                    final double x = midX + this.getColumnRadius() + 2;
                    final double y = this.rangeAxis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
                    final Line2D line = new Line2D.Double ( x, y, x + 10.0, y );
                    g2.setPaint ( this.subrangePaint[2] );
                    g2.draw ( line );
                }
            }
            if ( this.rangeAxis != null && this.axisLocation != 0 ) {
                int drawWidth = 10;
                if ( this.showValueLines ) {
                    drawWidth += this.getColumnDiameter();
                }
                switch ( this.axisLocation ) {
                case 1: {
                    final double cursor = midX + this.getColumnRadius();
                    final Rectangle2D drawArea = new Rectangle2D.Double ( cursor, stemTop, drawWidth, stemBottom - stemTop + 1 );
                    this.rangeAxis.draw ( g2, cursor, area, drawArea, RectangleEdge.RIGHT, null );
                    break;
                }
                default: {
                    final double cursor = midX - this.getColumnRadius();
                    final Rectangle2D drawArea = new Rectangle2D.Double ( cursor, stemTop, drawWidth, stemBottom - stemTop + 1 );
                    this.rangeAxis.draw ( g2, cursor, area, drawArea, RectangleEdge.LEFT, null );
                    break;
                }
                }
            }
            g2.setFont ( this.valueFont );
            g2.setPaint ( this.valuePaint );
            final FontMetrics metrics = g2.getFontMetrics();
            switch ( this.valueLocation ) {
            case 1: {
                g2.drawString ( this.valueFormat.format ( current ), midX + this.getColumnRadius() + this.getGap(), midY );
                break;
            }
            case 2: {
                final String valueString = this.valueFormat.format ( current );
                final int stringWidth = metrics.stringWidth ( valueString );
                g2.drawString ( valueString, midX - this.getColumnRadius() - this.getGap() - stringWidth, midY );
                break;
            }
            case 3: {
                final String temp = this.valueFormat.format ( current );
                i = metrics.stringWidth ( temp ) / 2;
                g2.drawString ( temp, midX - i, stemBottom + this.getBulbRadius() + this.getGap() );
                break;
            }
            }
        }
        g2.setPaint ( this.thermometerPaint );
        g2.setFont ( this.valueFont );
        final FontMetrics metrics = g2.getFontMetrics();
        final int tickX1 = midX - this.getColumnRadius() - this.getGap() * 2 - metrics.stringWidth ( ThermometerPlot.UNITS[this.units] );
        if ( tickX1 > area.getMinX() ) {
            g2.drawString ( ThermometerPlot.UNITS[this.units], tickX1, ( int ) ( area.getMinY() + 20.0 ) );
        }
        g2.setStroke ( this.thermometerStroke );
        g2.draw ( outerThermometer );
        g2.draw ( innerThermometer );
        this.drawOutline ( g2, area );
    }
    @Override
    public void zoom ( final double percent ) {
    }
    @Override
    public String getPlotType() {
        return ThermometerPlot.localizationResources.getString ( "Thermometer_Plot" );
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        if ( this.dataset != null ) {
            final Number vn = this.dataset.getValue();
            if ( vn != null ) {
                final double value = vn.doubleValue();
                if ( this.inSubrange ( 0, value ) ) {
                    this.subrange = 0;
                } else if ( this.inSubrange ( 1, value ) ) {
                    this.subrange = 1;
                } else if ( this.inSubrange ( 2, value ) ) {
                    this.subrange = 2;
                } else {
                    this.subrange = -1;
                }
                this.setAxisRange();
            }
        }
        super.datasetChanged ( event );
    }
    public Number getMinimumVerticalDataValue() {
        return new Double ( this.lowerBound );
    }
    public Number getMaximumVerticalDataValue() {
        return new Double ( this.upperBound );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        return new Range ( this.lowerBound, this.upperBound );
    }
    protected void setAxisRange() {
        if ( this.subrange >= 0 && this.followDataInSubranges ) {
            this.rangeAxis.setRange ( new Range ( this.subrangeInfo[this.subrange][2], this.subrangeInfo[this.subrange][3] ) );
        } else {
            this.rangeAxis.setRange ( this.lowerBound, this.upperBound );
        }
    }
    @Override
    public LegendItemCollection getLegendItems() {
        return null;
    }
    @Override
    public PlotOrientation getOrientation() {
        return PlotOrientation.VERTICAL;
    }
    protected static boolean isValidNumber ( final double d ) {
        return !Double.isNaN ( d ) && !Double.isInfinite ( d );
    }
    private boolean inSubrange ( final int subrange, final double value ) {
        return value > this.subrangeInfo[subrange][0] && value <= this.subrangeInfo[subrange][1];
    }
    private Paint getCurrentPaint() {
        Paint result = this.mercuryPaint;
        if ( this.useSubrangePaint ) {
            final double value = this.dataset.getValue().doubleValue();
            if ( this.inSubrange ( 0, value ) ) {
                result = this.subrangePaint[0];
            } else if ( this.inSubrange ( 1, value ) ) {
                result = this.subrangePaint[1];
            } else if ( this.inSubrange ( 2, value ) ) {
                result = this.subrangePaint[2];
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ThermometerPlot ) ) {
            return false;
        }
        final ThermometerPlot that = ( ThermometerPlot ) obj;
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeAxis, ( Object ) that.rangeAxis ) ) {
            return false;
        }
        if ( this.axisLocation != that.axisLocation ) {
            return false;
        }
        if ( this.lowerBound != that.lowerBound ) {
            return false;
        }
        if ( this.upperBound != that.upperBound ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.padding, ( Object ) that.padding ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.thermometerStroke, ( Object ) that.thermometerStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.thermometerPaint, that.thermometerPaint ) ) {
            return false;
        }
        if ( this.units != that.units ) {
            return false;
        }
        if ( this.valueLocation != that.valueLocation ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.valueFont, ( Object ) that.valueFont ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.valuePaint, that.valuePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.valueFormat, ( Object ) that.valueFormat ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.mercuryPaint, that.mercuryPaint ) ) {
            return false;
        }
        if ( this.showValueLines != that.showValueLines ) {
            return false;
        }
        if ( this.subrange != that.subrange ) {
            return false;
        }
        if ( this.followDataInSubranges != that.followDataInSubranges ) {
            return false;
        }
        if ( !equal ( this.subrangeInfo, that.subrangeInfo ) ) {
            return false;
        }
        if ( this.useSubrangePaint != that.useSubrangePaint ) {
            return false;
        }
        if ( this.bulbRadius != that.bulbRadius ) {
            return false;
        }
        if ( this.columnRadius != that.columnRadius ) {
            return false;
        }
        if ( this.gap != that.gap ) {
            return false;
        }
        for ( int i = 0; i < this.subrangePaint.length; ++i ) {
            if ( !PaintUtilities.equal ( this.subrangePaint[i], that.subrangePaint[i] ) ) {
                return false;
            }
        }
        return true;
    }
    private static boolean equal ( final double[][] array1, final double[][] array2 ) {
        if ( array1 == null ) {
            return array2 == null;
        }
        if ( array2 == null ) {
            return false;
        }
        if ( array1.length != array2.length ) {
            return false;
        }
        for ( int i = 0; i < array1.length; ++i ) {
            if ( !Arrays.equals ( array1[i], array2[i] ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ThermometerPlot clone = ( ThermometerPlot ) super.clone();
        if ( clone.dataset != null ) {
            clone.dataset.addChangeListener ( clone );
        }
        clone.rangeAxis = ( ValueAxis ) ObjectUtilities.clone ( ( Object ) this.rangeAxis );
        if ( clone.rangeAxis != null ) {
            clone.rangeAxis.setPlot ( clone );
            clone.rangeAxis.addChangeListener ( clone );
        }
        clone.valueFormat = ( NumberFormat ) this.valueFormat.clone();
        clone.subrangePaint = this.subrangePaint.clone();
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.thermometerStroke, stream );
        SerialUtilities.writePaint ( this.thermometerPaint, stream );
        SerialUtilities.writePaint ( this.valuePaint, stream );
        SerialUtilities.writePaint ( this.mercuryPaint, stream );
        SerialUtilities.writeStroke ( this.subrangeIndicatorStroke, stream );
        SerialUtilities.writeStroke ( this.rangeIndicatorStroke, stream );
        for ( int i = 0; i < 3; ++i ) {
            SerialUtilities.writePaint ( this.subrangePaint[i], stream );
        }
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.thermometerStroke = SerialUtilities.readStroke ( stream );
        this.thermometerPaint = SerialUtilities.readPaint ( stream );
        this.valuePaint = SerialUtilities.readPaint ( stream );
        this.mercuryPaint = SerialUtilities.readPaint ( stream );
        this.subrangeIndicatorStroke = SerialUtilities.readStroke ( stream );
        this.rangeIndicatorStroke = SerialUtilities.readStroke ( stream );
        this.subrangePaint = new Paint[3];
        for ( int i = 0; i < 3; ++i ) {
            this.subrangePaint[i] = SerialUtilities.readPaint ( stream );
        }
        if ( this.rangeAxis != null ) {
            this.rangeAxis.addChangeListener ( this );
        }
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor ) {
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
        this.rangeAxis.resizeRange ( factor );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor ) {
        final double anchorY = this.getRangeAxis().java2DToValue ( source.getY(), state.getDataArea(), RectangleEdge.LEFT );
        this.rangeAxis.resizeRange ( factor, anchorY );
    }
    @Override
    public void zoomDomainAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
        this.rangeAxis.zoomRange ( lowerPercent, upperPercent );
    }
    @Override
    public boolean isDomainZoomable() {
        return false;
    }
    @Override
    public boolean isRangeZoomable() {
        return true;
    }
    static {
        UNITS = new String[] { "", "°F", "°C", "°K" };
        ThermometerPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
