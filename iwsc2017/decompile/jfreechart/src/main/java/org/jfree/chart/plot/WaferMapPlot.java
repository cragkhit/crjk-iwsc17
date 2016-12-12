package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.awt.BasicStroke;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.LegendItemCollection;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.Color;
import java.awt.Shape;
import org.jfree.ui.RectangleInsets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.data.general.WaferMapDataset;
import java.util.ResourceBundle;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.chart.event.RendererChangeListener;
public class WaferMapPlot extends Plot implements RendererChangeListener, Cloneable, Serializable {
    private static final long serialVersionUID = 4668320403707308155L;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_GRIDLINE_PAINT;
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;
    public static final Stroke DEFAULT_CROSSHAIR_STROKE;
    public static final Paint DEFAULT_CROSSHAIR_PAINT;
    protected static ResourceBundle localizationResources;
    private PlotOrientation orientation;
    private WaferMapDataset dataset;
    private WaferMapRenderer renderer;
    public WaferMapPlot() {
        this ( null );
    }
    public WaferMapPlot ( final WaferMapDataset dataset ) {
        this ( dataset, null );
    }
    public WaferMapPlot ( final WaferMapDataset dataset, final WaferMapRenderer renderer ) {
        this.orientation = PlotOrientation.VERTICAL;
        this.dataset = dataset;
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        if ( ( this.renderer = renderer ) != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
    }
    @Override
    public String getPlotType() {
        return "WMAP_Plot";
    }
    public WaferMapDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final WaferMapDataset dataset ) {
        if ( this.dataset != null ) {
            this.dataset.removeChangeListener ( this );
        }
        if ( ( this.dataset = dataset ) != null ) {
            this.setDatasetGroup ( dataset.getGroup() );
            dataset.addChangeListener ( this );
        }
        this.datasetChanged ( new DatasetChangeEvent ( this, dataset ) );
    }
    public void setRenderer ( final WaferMapRenderer renderer ) {
        if ( this.renderer != null ) {
            this.renderer.removeChangeListener ( this );
        }
        if ( ( this.renderer = renderer ) != null ) {
            renderer.setPlot ( this );
        }
        this.fireChangeEvent();
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState state, final PlotRenderingInfo info ) {
        final boolean b1 = area.getWidth() <= 10.0;
        final boolean b2 = area.getHeight() <= 10.0;
        if ( b1 || b2 ) {
            return;
        }
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        this.drawChipGrid ( g2, area );
        this.drawWaferEdge ( g2, area );
    }
    protected void drawChipGrid ( final Graphics2D g2, final Rectangle2D plotArea ) {
        final Shape savedClip = g2.getClip();
        g2.setClip ( this.getWaferEdge ( plotArea ) );
        final Rectangle2D chip = new Rectangle2D.Double();
        int xchips = 35;
        int ychips = 20;
        double space = 1.0;
        if ( this.dataset != null ) {
            xchips = this.dataset.getMaxChipX() + 2;
            ychips = this.dataset.getMaxChipY() + 2;
            space = this.dataset.getChipSpace();
        }
        double startX = plotArea.getX();
        double startY = plotArea.getY();
        double chipWidth = 1.0;
        double chipHeight = 1.0;
        if ( plotArea.getWidth() != plotArea.getHeight() ) {
            double major;
            double minor;
            if ( plotArea.getWidth() > plotArea.getHeight() ) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            } else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            if ( plotArea.getWidth() == minor ) {
                startY += ( major - minor ) / 2.0;
                chipWidth = ( plotArea.getWidth() - ( space * xchips - 1.0 ) ) / xchips;
                chipHeight = ( plotArea.getWidth() - ( space * ychips - 1.0 ) ) / ychips;
            } else {
                startX += ( major - minor ) / 2.0;
                chipWidth = ( plotArea.getHeight() - ( space * xchips - 1.0 ) ) / xchips;
                chipHeight = ( plotArea.getHeight() - ( space * ychips - 1.0 ) ) / ychips;
            }
        }
        for ( int x = 1; x <= xchips; ++x ) {
            final double upperLeftX = startX - chipWidth + chipWidth * x + space * ( x - 1 );
            for ( int y = 1; y <= ychips; ++y ) {
                final double upperLeftY = startY - chipHeight + chipHeight * y + space * ( y - 1 );
                chip.setFrame ( upperLeftX, upperLeftY, chipWidth, chipHeight );
                g2.setColor ( Color.white );
                if ( this.dataset.getChipValue ( x - 1, ychips - y - 1 ) != null ) {
                    g2.setPaint ( this.renderer.getChipColor ( this.dataset.getChipValue ( x - 1, ychips - y - 1 ) ) );
                }
                g2.fill ( chip );
                g2.setColor ( Color.lightGray );
                g2.draw ( chip );
            }
        }
        g2.setClip ( savedClip );
    }
    protected Ellipse2D getWaferEdge ( final Rectangle2D plotArea ) {
        final Ellipse2D edge = new Ellipse2D.Double();
        double diameter = plotArea.getWidth();
        double upperLeftX = plotArea.getX();
        double upperLeftY = plotArea.getY();
        if ( plotArea.getWidth() != plotArea.getHeight() ) {
            double major;
            double minor;
            if ( plotArea.getWidth() > plotArea.getHeight() ) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            } else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            diameter = minor;
            if ( plotArea.getWidth() == minor ) {
                upperLeftY = plotArea.getY() + ( major - minor ) / 2.0;
            } else {
                upperLeftX = plotArea.getX() + ( major - minor ) / 2.0;
            }
        }
        edge.setFrame ( upperLeftX, upperLeftY, diameter, diameter );
        return edge;
    }
    protected void drawWaferEdge ( final Graphics2D g2, final Rectangle2D plotArea ) {
        final Ellipse2D waferEdge = this.getWaferEdge ( plotArea );
        g2.setColor ( Color.black );
        g2.draw ( waferEdge );
        final Rectangle2D waferFrame = waferEdge.getFrame();
        final double notchDiameter = waferFrame.getWidth() * 0.04;
        Arc2D notch;
        if ( this.orientation == PlotOrientation.HORIZONTAL ) {
            final Rectangle2D notchFrame = new Rectangle2D.Double ( waferFrame.getX() + waferFrame.getWidth() - notchDiameter / 2.0, waferFrame.getY() + waferFrame.getHeight() / 2.0 - notchDiameter / 2.0, notchDiameter, notchDiameter );
            notch = new Arc2D.Double ( notchFrame, 90.0, 180.0, 0 );
        } else {
            final Rectangle2D notchFrame = new Rectangle2D.Double ( waferFrame.getX() + waferFrame.getWidth() / 2.0 - notchDiameter / 2.0, waferFrame.getY() + waferFrame.getHeight() - notchDiameter / 2.0, notchDiameter, notchDiameter );
            notch = new Arc2D.Double ( notchFrame, 0.0, 180.0, 0 );
        }
        g2.setColor ( Color.white );
        g2.fill ( notch );
        g2.setColor ( Color.black );
        g2.draw ( notch );
    }
    @Override
    public LegendItemCollection getLegendItems() {
        return this.renderer.getLegendCollection();
    }
    @Override
    public void rendererChanged ( final RendererChangeEvent event ) {
        this.fireChangeEvent();
    }
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f );
        DEFAULT_GRIDLINE_PAINT = Color.lightGray;
        DEFAULT_CROSSHAIR_STROKE = WaferMapPlot.DEFAULT_GRIDLINE_STROKE;
        DEFAULT_CROSSHAIR_PAINT = Color.blue;
        WaferMapPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
