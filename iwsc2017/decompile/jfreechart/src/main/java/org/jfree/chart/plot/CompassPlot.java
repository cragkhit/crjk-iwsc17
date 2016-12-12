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
import org.jfree.ui.RectangleInsets;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import org.jfree.chart.needle.MiddlePinNeedle;
import org.jfree.chart.needle.WindNeedle;
import org.jfree.chart.needle.ShipNeedle;
import java.awt.BasicStroke;
import org.jfree.chart.needle.PointerNeedle;
import org.jfree.chart.needle.PlumNeedle;
import org.jfree.chart.needle.PinNeedle;
import org.jfree.chart.needle.LongNeedle;
import org.jfree.chart.needle.LineNeedle;
import org.jfree.chart.needle.ArrowNeedle;
import java.awt.Stroke;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.DatasetChangeListener;
import java.awt.Color;
import org.jfree.data.general.DefaultValueDataset;
import java.util.ResourceBundle;
import org.jfree.chart.needle.MeterNeedle;
import org.jfree.data.general.ValueDataset;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
public class CompassPlot extends Plot implements Cloneable, Serializable {
    private static final long serialVersionUID = 6924382802125527395L;
    public static final Font DEFAULT_LABEL_FONT;
    public static final int NO_LABELS = 0;
    public static final int VALUE_LABELS = 1;
    private int labelType;
    private Font labelFont;
    private boolean drawBorder;
    private transient Paint roseHighlightPaint;
    private transient Paint rosePaint;
    private transient Paint roseCenterPaint;
    private Font compassFont;
    private transient Ellipse2D circle1;
    private transient Ellipse2D circle2;
    private transient Area a1;
    private transient Area a2;
    private transient Rectangle2D rect1;
    private ValueDataset[] datasets;
    private MeterNeedle[] seriesNeedle;
    protected static ResourceBundle localizationResources;
    protected double revolutionDistance;
    public CompassPlot() {
        this ( new DefaultValueDataset() );
    }
    public CompassPlot ( final ValueDataset dataset ) {
        this.drawBorder = false;
        this.roseHighlightPaint = Color.black;
        this.rosePaint = Color.yellow;
        this.roseCenterPaint = Color.white;
        this.compassFont = new Font ( "Arial", 0, 10 );
        this.datasets = new ValueDataset[1];
        this.seriesNeedle = new MeterNeedle[1];
        this.revolutionDistance = 360.0;
        if ( dataset != null ) {
            ( this.datasets[0] = dataset ).addChangeListener ( this );
        }
        this.circle1 = new Ellipse2D.Double();
        this.circle2 = new Ellipse2D.Double();
        this.rect1 = new Rectangle2D.Double();
        this.setSeriesNeedle ( 0 );
    }
    public int getLabelType() {
        return this.labelType;
    }
    public void setLabelType ( final int type ) {
        if ( type != 0 && type != 1 ) {
            throw new IllegalArgumentException ( "MeterPlot.setLabelType(int): unrecognised type." );
        }
        if ( this.labelType != type ) {
            this.labelType = type;
            this.fireChangeEvent();
        }
    }
    public Font getLabelFont() {
        return this.labelFont;
    }
    public void setLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.labelFont = font;
        this.fireChangeEvent();
    }
    public Paint getRosePaint() {
        return this.rosePaint;
    }
    public void setRosePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rosePaint = paint;
        this.fireChangeEvent();
    }
    public Paint getRoseCenterPaint() {
        return this.roseCenterPaint;
    }
    public void setRoseCenterPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.roseCenterPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getRoseHighlightPaint() {
        return this.roseHighlightPaint;
    }
    public void setRoseHighlightPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.roseHighlightPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getDrawBorder() {
        return this.drawBorder;
    }
    public void setDrawBorder ( final boolean status ) {
        this.drawBorder = status;
        this.fireChangeEvent();
    }
    public void setSeriesPaint ( final int series, final Paint paint ) {
        if ( series >= 0 && series < this.seriesNeedle.length ) {
            this.seriesNeedle[series].setFillPaint ( paint );
        }
    }
    public void setSeriesOutlinePaint ( final int series, final Paint p ) {
        if ( series >= 0 && series < this.seriesNeedle.length ) {
            this.seriesNeedle[series].setOutlinePaint ( p );
        }
    }
    public void setSeriesOutlineStroke ( final int series, final Stroke stroke ) {
        if ( series >= 0 && series < this.seriesNeedle.length ) {
            this.seriesNeedle[series].setOutlineStroke ( stroke );
        }
    }
    public void setSeriesNeedle ( final int type ) {
        this.setSeriesNeedle ( 0, type );
    }
    public void setSeriesNeedle ( final int index, final int type ) {
        switch ( type ) {
        case 0: {
            this.setSeriesNeedle ( index, new ArrowNeedle ( true ) );
            this.setSeriesPaint ( index, Color.red );
            this.seriesNeedle[index].setHighlightPaint ( Color.white );
            break;
        }
        case 1: {
            this.setSeriesNeedle ( index, new LineNeedle() );
            break;
        }
        case 2: {
            final MeterNeedle longNeedle = new LongNeedle();
            longNeedle.setRotateY ( 0.5 );
            this.setSeriesNeedle ( index, longNeedle );
            break;
        }
        case 3: {
            this.setSeriesNeedle ( index, new PinNeedle() );
            break;
        }
        case 4: {
            this.setSeriesNeedle ( index, new PlumNeedle() );
            break;
        }
        case 5: {
            this.setSeriesNeedle ( index, new PointerNeedle() );
            break;
        }
        case 6: {
            this.setSeriesPaint ( index, null );
            this.setSeriesOutlineStroke ( index, new BasicStroke ( 3.0f ) );
            this.setSeriesNeedle ( index, new ShipNeedle() );
            break;
        }
        case 7: {
            this.setSeriesPaint ( index, Color.blue );
            this.setSeriesNeedle ( index, new WindNeedle() );
            break;
        }
        case 8: {
            this.setSeriesNeedle ( index, new ArrowNeedle ( true ) );
            break;
        }
        case 9: {
            this.setSeriesNeedle ( index, new MiddlePinNeedle() );
            break;
        }
        default: {
            throw new IllegalArgumentException ( "Unrecognised type." );
        }
        }
    }
    public void setSeriesNeedle ( final int index, final MeterNeedle needle ) {
        if ( needle != null && index < this.seriesNeedle.length ) {
            this.seriesNeedle[index] = needle;
        }
        this.fireChangeEvent();
    }
    public ValueDataset[] getDatasets() {
        return this.datasets;
    }
    public void addDataset ( final ValueDataset dataset ) {
        this.addDataset ( dataset, null );
    }
    public void addDataset ( final ValueDataset dataset, final MeterNeedle needle ) {
        if ( dataset != null ) {
            int i = this.datasets.length + 1;
            final ValueDataset[] t = new ValueDataset[i];
            final MeterNeedle[] p = new MeterNeedle[i];
            for ( i -= 2; i >= 0; --i ) {
                t[i] = this.datasets[i];
                p[i] = this.seriesNeedle[i];
            }
            i = this.datasets.length;
            t[i] = dataset;
            p[i] = ( ( needle != null ) ? needle : p[i - 1] );
            final ValueDataset[] a = this.datasets;
            final MeterNeedle[] b = this.seriesNeedle;
            this.datasets = t;
            this.seriesNeedle = p;
            --i;
            while ( i >= 0 ) {
                a[i] = null;
                b[i] = null;
                --i;
            }
            dataset.addChangeListener ( this );
        }
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        if ( this.drawBorder ) {
            this.drawBackground ( g2, area );
        }
        int midX = ( int ) ( area.getWidth() / 2.0 );
        int midY = ( int ) ( area.getHeight() / 2.0 );
        int radius;
        if ( midY < ( radius = midX ) ) {
            radius = midY;
        }
        --radius;
        final int diameter = 2 * radius;
        midX += ( int ) area.getMinX();
        midY += ( int ) area.getMinY();
        this.circle1.setFrame ( midX - radius, midY - radius, diameter, diameter );
        this.circle2.setFrame ( midX - radius + 15, midY - radius + 15, diameter - 30, diameter - 30 );
        g2.setPaint ( this.rosePaint );
        this.a1 = new Area ( this.circle1 );
        this.a2 = new Area ( this.circle2 );
        this.a1.subtract ( this.a2 );
        g2.fill ( this.a1 );
        g2.setPaint ( this.roseCenterPaint );
        int x1 = diameter - 30;
        g2.fillOval ( midX - radius + 15, midY - radius + 15, x1, x1 );
        g2.setPaint ( this.roseHighlightPaint );
        g2.drawOval ( midX - radius, midY - radius, diameter, diameter );
        x1 = diameter - 20;
        g2.drawOval ( midX - radius + 10, midY - radius + 10, x1, x1 );
        x1 = diameter - 30;
        g2.drawOval ( midX - radius + 15, midY - radius + 15, x1, x1 );
        x1 = diameter - 80;
        g2.drawOval ( midX - radius + 40, midY - radius + 40, x1, x1 );
        int outerRadius = radius - 20;
        int innerRadius = radius - 32;
        for ( int w = 0; w < 360; w += 15 ) {
            final double a = Math.toRadians ( w );
            x1 = midX - ( int ) ( Math.sin ( a ) * innerRadius );
            final int x2 = midX - ( int ) ( Math.sin ( a ) * outerRadius );
            final int y1 = midY - ( int ) ( Math.cos ( a ) * innerRadius );
            final int y2 = midY - ( int ) ( Math.cos ( a ) * outerRadius );
            g2.drawLine ( x1, y1, x2, y2 );
        }
        g2.setPaint ( this.roseHighlightPaint );
        innerRadius = radius - 26;
        outerRadius = 7;
        for ( int w = 45; w < 360; w += 90 ) {
            final double a = Math.toRadians ( w );
            x1 = midX - ( int ) ( Math.sin ( a ) * innerRadius );
            final int y1 = midY - ( int ) ( Math.cos ( a ) * innerRadius );
            g2.fillOval ( x1 - outerRadius, y1 - outerRadius, 2 * outerRadius, 2 * outerRadius );
        }
        for ( int w = 0; w < 360; w += 90 ) {
            final double a = Math.toRadians ( w );
            x1 = midX - ( int ) ( Math.sin ( a ) * innerRadius );
            final int y1 = midY - ( int ) ( Math.cos ( a ) * innerRadius );
            final Polygon p = new Polygon();
            p.addPoint ( x1 - outerRadius, y1 );
            p.addPoint ( x1, y1 + outerRadius );
            p.addPoint ( x1 + outerRadius, y1 );
            p.addPoint ( x1, y1 - outerRadius );
            g2.fillPolygon ( p );
        }
        innerRadius = radius - 42;
        final Font f = this.getCompassFont ( radius );
        g2.setFont ( f );
        g2.drawString ( CompassPlot.localizationResources.getString ( "N" ), midX - 5, midY - innerRadius + f.getSize() );
        g2.drawString ( CompassPlot.localizationResources.getString ( "S" ), midX - 5, midY + innerRadius - 5 );
        g2.drawString ( CompassPlot.localizationResources.getString ( "W" ), midX - innerRadius + 5, midY + 5 );
        g2.drawString ( CompassPlot.localizationResources.getString ( "E" ), midX + innerRadius - f.getSize(), midY + 5 );
        final int y1 = radius / 2;
        x1 = radius / 6;
        final Rectangle2D needleArea = new Rectangle2D.Double ( midX - x1, midY - y1, 2 * x1, 2 * y1 );
        final int x3 = this.seriesNeedle.length;
        for ( int i = this.datasets.length - 1; i >= 0; --i ) {
            final ValueDataset data = this.datasets[i];
            if ( data != null && data.getValue() != null ) {
                double value = data.getValue().doubleValue() % this.revolutionDistance;
                value = value / this.revolutionDistance * 360.0;
                final int current = i % x3;
                this.seriesNeedle[current].draw ( g2, needleArea, value );
            }
        }
        if ( this.drawBorder ) {
            this.drawOutline ( g2, area );
        }
    }
    @Override
    public String getPlotType() {
        return CompassPlot.localizationResources.getString ( "Compass_Plot" );
    }
    @Override
    public LegendItemCollection getLegendItems() {
        return null;
    }
    @Override
    public void zoom ( final double percent ) {
    }
    protected Font getCompassFont ( final int radius ) {
        float fontSize = radius / 10.0f;
        if ( fontSize < 8.0f ) {
            fontSize = 8.0f;
        }
        final Font newFont = this.compassFont.deriveFont ( fontSize );
        return newFont;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CompassPlot ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final CompassPlot that = ( CompassPlot ) obj;
        return this.labelType == that.labelType && ObjectUtilities.equal ( ( Object ) this.labelFont, ( Object ) that.labelFont ) && this.drawBorder == that.drawBorder && PaintUtilities.equal ( this.roseHighlightPaint, that.roseHighlightPaint ) && PaintUtilities.equal ( this.rosePaint, that.rosePaint ) && PaintUtilities.equal ( this.roseCenterPaint, that.roseCenterPaint ) && ObjectUtilities.equal ( ( Object ) this.compassFont, ( Object ) that.compassFont ) && Arrays.equals ( this.seriesNeedle, that.seriesNeedle ) && this.getRevolutionDistance() == that.getRevolutionDistance();
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CompassPlot clone = ( CompassPlot ) super.clone();
        if ( this.circle1 != null ) {
            clone.circle1 = ( Ellipse2D ) this.circle1.clone();
        }
        if ( this.circle2 != null ) {
            clone.circle2 = ( Ellipse2D ) this.circle2.clone();
        }
        if ( this.a1 != null ) {
            clone.a1 = ( Area ) this.a1.clone();
        }
        if ( this.a2 != null ) {
            clone.a2 = ( Area ) this.a2.clone();
        }
        if ( this.rect1 != null ) {
            clone.rect1 = ( Rectangle2D ) this.rect1.clone();
        }
        clone.datasets = this.datasets.clone();
        clone.seriesNeedle = this.seriesNeedle.clone();
        for ( int i = 0; i < this.datasets.length; ++i ) {
            if ( clone.datasets[i] != null ) {
                clone.datasets[i].addChangeListener ( clone );
            }
        }
        return clone;
    }
    public void setRevolutionDistance ( final double size ) {
        if ( size > 0.0 ) {
            this.revolutionDistance = size;
        }
    }
    public double getRevolutionDistance() {
        return this.revolutionDistance;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.rosePaint, stream );
        SerialUtilities.writePaint ( this.roseCenterPaint, stream );
        SerialUtilities.writePaint ( this.roseHighlightPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.rosePaint = SerialUtilities.readPaint ( stream );
        this.roseCenterPaint = SerialUtilities.readPaint ( stream );
        this.roseHighlightPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_LABEL_FONT = new Font ( "SansSerif", 1, 10 );
        CompassPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
