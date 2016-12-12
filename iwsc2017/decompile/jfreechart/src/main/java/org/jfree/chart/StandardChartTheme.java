package org.jfree.chart;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.block.LabelBlock;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.Block;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.util.DefaultShadowGenerator;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.Color;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.DrawingSupplier;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardChartTheme implements ChartTheme, Cloneable, PublicCloneable, Serializable {
    private String name;
    private Font extraLargeFont;
    private Font largeFont;
    private Font regularFont;
    private Font smallFont;
    private transient Paint titlePaint;
    private transient Paint subtitlePaint;
    private transient Paint chartBackgroundPaint;
    private transient Paint legendBackgroundPaint;
    private transient Paint legendItemPaint;
    private DrawingSupplier drawingSupplier;
    private transient Paint plotBackgroundPaint;
    private transient Paint plotOutlinePaint;
    private PieLabelLinkStyle labelLinkStyle;
    private transient Paint labelLinkPaint;
    private transient Paint domainGridlinePaint;
    private transient Paint rangeGridlinePaint;
    private transient Paint baselinePaint;
    private transient Paint crosshairPaint;
    private RectangleInsets axisOffset;
    private transient Paint axisLabelPaint;
    private transient Paint tickLabelPaint;
    private transient Paint itemLabelPaint;
    private boolean shadowVisible;
    private transient Paint shadowPaint;
    private BarPainter barPainter;
    private XYBarPainter xyBarPainter;
    private transient Paint thermometerPaint;
    private transient Paint wallPaint;
    private transient Paint errorIndicatorPaint;
    private transient Paint gridBandPaint;
    private transient Paint gridBandAlternatePaint;
    private ShadowGenerator shadowGenerator;
    public static ChartTheme createJFreeTheme() {
        return new StandardChartTheme ( "JFree" );
    }
    public static ChartTheme createDarknessTheme() {
        final StandardChartTheme theme = new StandardChartTheme ( "Darkness" );
        theme.titlePaint = Color.white;
        theme.subtitlePaint = Color.white;
        theme.legendBackgroundPaint = Color.black;
        theme.legendItemPaint = Color.white;
        theme.chartBackgroundPaint = Color.black;
        theme.plotBackgroundPaint = Color.black;
        theme.plotOutlinePaint = Color.yellow;
        theme.baselinePaint = Color.white;
        theme.crosshairPaint = Color.red;
        theme.labelLinkPaint = Color.lightGray;
        theme.tickLabelPaint = Color.white;
        theme.axisLabelPaint = Color.white;
        theme.shadowPaint = Color.darkGray;
        theme.itemLabelPaint = Color.white;
        theme.drawingSupplier = new DefaultDrawingSupplier ( new Paint[] { Color.decode ( "0xFFFF00" ), Color.decode ( "0x0036CC" ), Color.decode ( "0xFF0000" ), Color.decode ( "0xFFFF7F" ), Color.decode ( "0x6681CC" ), Color.decode ( "0xFF7F7F" ), Color.decode ( "0xFFFFBF" ), Color.decode ( "0x99A6CC" ), Color.decode ( "0xFFBFBF" ), Color.decode ( "0xA9A938" ), Color.decode ( "0x2D4587" ) }, new Paint[] { Color.decode ( "0xFFFF00" ), Color.decode ( "0x0036CC" ) }, new Stroke[] { new BasicStroke ( 2.0f ) }, new Stroke[] { new BasicStroke ( 0.5f ) }, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE );
        theme.wallPaint = Color.darkGray;
        theme.errorIndicatorPaint = Color.lightGray;
        theme.gridBandPaint = new Color ( 255, 255, 255, 20 );
        theme.gridBandAlternatePaint = new Color ( 255, 255, 255, 40 );
        theme.shadowGenerator = null;
        return theme;
    }
    public static ChartTheme createLegacyTheme() {
        final StandardChartTheme theme = new StandardChartTheme ( "Legacy" ) {
            @Override
            public void apply ( final JFreeChart chart ) {
            }
        };
        return theme;
    }
    public StandardChartTheme ( final String name ) {
        this ( name, false );
    }
    public StandardChartTheme ( final String name, final boolean shadow ) {
        this.gridBandPaint = SymbolAxis.DEFAULT_GRID_BAND_PAINT;
        this.gridBandAlternatePaint = SymbolAxis.DEFAULT_GRID_BAND_ALTERNATE_PAINT;
        ParamChecks.nullNotPermitted ( name, "name" );
        this.name = name;
        this.extraLargeFont = new Font ( "Tahoma", 1, 20 );
        this.largeFont = new Font ( "Tahoma", 1, 14 );
        this.regularFont = new Font ( "Tahoma", 0, 12 );
        this.smallFont = new Font ( "Tahoma", 0, 10 );
        this.titlePaint = Color.black;
        this.subtitlePaint = Color.black;
        this.legendBackgroundPaint = Color.white;
        this.legendItemPaint = Color.darkGray;
        this.chartBackgroundPaint = Color.white;
        this.drawingSupplier = new DefaultDrawingSupplier();
        this.plotBackgroundPaint = Color.lightGray;
        this.plotOutlinePaint = Color.black;
        this.labelLinkPaint = Color.black;
        this.labelLinkStyle = PieLabelLinkStyle.CUBIC_CURVE;
        this.axisOffset = new RectangleInsets ( 4.0, 4.0, 4.0, 4.0 );
        this.domainGridlinePaint = Color.white;
        this.rangeGridlinePaint = Color.white;
        this.baselinePaint = Color.black;
        this.crosshairPaint = Color.blue;
        this.axisLabelPaint = Color.darkGray;
        this.tickLabelPaint = Color.darkGray;
        this.barPainter = new GradientBarPainter();
        this.xyBarPainter = new GradientXYBarPainter();
        this.shadowVisible = false;
        this.shadowPaint = Color.gray;
        this.itemLabelPaint = Color.black;
        this.thermometerPaint = Color.white;
        this.wallPaint = BarRenderer3D.DEFAULT_WALL_PAINT;
        this.errorIndicatorPaint = Color.black;
        this.shadowGenerator = ( shadow ? new DefaultShadowGenerator() : null );
    }
    public Font getExtraLargeFont() {
        return this.extraLargeFont;
    }
    public void setExtraLargeFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.extraLargeFont = font;
    }
    public Font getLargeFont() {
        return this.largeFont;
    }
    public void setLargeFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.largeFont = font;
    }
    public Font getRegularFont() {
        return this.regularFont;
    }
    public void setRegularFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.regularFont = font;
    }
    public Font getSmallFont() {
        return this.smallFont;
    }
    public void setSmallFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.smallFont = font;
    }
    public Paint getTitlePaint() {
        return this.titlePaint;
    }
    public void setTitlePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.titlePaint = paint;
    }
    public Paint getSubtitlePaint() {
        return this.subtitlePaint;
    }
    public void setSubtitlePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.subtitlePaint = paint;
    }
    public Paint getChartBackgroundPaint() {
        return this.chartBackgroundPaint;
    }
    public void setChartBackgroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.chartBackgroundPaint = paint;
    }
    public Paint getLegendBackgroundPaint() {
        return this.legendBackgroundPaint;
    }
    public void setLegendBackgroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.legendBackgroundPaint = paint;
    }
    public Paint getLegendItemPaint() {
        return this.legendItemPaint;
    }
    public void setLegendItemPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.legendItemPaint = paint;
    }
    public Paint getPlotBackgroundPaint() {
        return this.plotBackgroundPaint;
    }
    public void setPlotBackgroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.plotBackgroundPaint = paint;
    }
    public Paint getPlotOutlinePaint() {
        return this.plotOutlinePaint;
    }
    public void setPlotOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.plotOutlinePaint = paint;
    }
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }
    public void setLabelLinkStyle ( final PieLabelLinkStyle style ) {
        ParamChecks.nullNotPermitted ( style, "style" );
        this.labelLinkStyle = style;
    }
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }
    public void setLabelLinkPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.labelLinkPaint = paint;
    }
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }
    public void setDomainGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainGridlinePaint = paint;
    }
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }
    public void setRangeGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeGridlinePaint = paint;
    }
    public Paint getBaselinePaint() {
        return this.baselinePaint;
    }
    public void setBaselinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baselinePaint = paint;
    }
    public Paint getCrosshairPaint() {
        return this.crosshairPaint;
    }
    public void setCrosshairPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.crosshairPaint = paint;
    }
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }
    public void setAxisOffset ( final RectangleInsets offset ) {
        ParamChecks.nullNotPermitted ( offset, "offset" );
        this.axisOffset = offset;
    }
    public Paint getAxisLabelPaint() {
        return this.axisLabelPaint;
    }
    public void setAxisLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.axisLabelPaint = paint;
    }
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }
    public void setTickLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickLabelPaint = paint;
    }
    public Paint getItemLabelPaint() {
        return this.itemLabelPaint;
    }
    public void setItemLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.itemLabelPaint = paint;
    }
    public boolean isShadowVisible() {
        return this.shadowVisible;
    }
    public void setShadowVisible ( final boolean visible ) {
        this.shadowVisible = visible;
    }
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }
    public void setShadowPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.shadowPaint = paint;
    }
    public BarPainter getBarPainter() {
        return this.barPainter;
    }
    public void setBarPainter ( final BarPainter painter ) {
        ParamChecks.nullNotPermitted ( painter, "painter" );
        this.barPainter = painter;
    }
    public XYBarPainter getXYBarPainter() {
        return this.xyBarPainter;
    }
    public void setXYBarPainter ( final XYBarPainter painter ) {
        ParamChecks.nullNotPermitted ( painter, "painter" );
        this.xyBarPainter = painter;
    }
    public Paint getThermometerPaint() {
        return this.thermometerPaint;
    }
    public void setThermometerPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.thermometerPaint = paint;
    }
    public Paint getWallPaint() {
        return this.wallPaint;
    }
    public void setWallPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.wallPaint = paint;
    }
    public Paint getErrorIndicatorPaint() {
        return this.errorIndicatorPaint;
    }
    public void setErrorIndicatorPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.errorIndicatorPaint = paint;
    }
    public Paint getGridBandPaint() {
        return this.gridBandPaint;
    }
    public void setGridBandPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.gridBandPaint = paint;
    }
    public Paint getGridBandAlternatePaint() {
        return this.gridBandAlternatePaint;
    }
    public void setGridBandAlternatePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.gridBandAlternatePaint = paint;
    }
    public String getName() {
        return this.name;
    }
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        if ( this.drawingSupplier instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.drawingSupplier;
            try {
                result = ( DrawingSupplier ) pc.clone();
            } catch ( CloneNotSupportedException e ) {
                throw new RuntimeException ( e );
            }
        }
        return result;
    }
    public void setDrawingSupplier ( final DrawingSupplier supplier ) {
        ParamChecks.nullNotPermitted ( supplier, "supplier" );
        this.drawingSupplier = supplier;
    }
    @Override
    public void apply ( final JFreeChart chart ) {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        final TextTitle title = chart.getTitle();
        if ( title != null ) {
            title.setFont ( this.extraLargeFont );
            title.setPaint ( this.titlePaint );
        }
        for ( int subtitleCount = chart.getSubtitleCount(), i = 0; i < subtitleCount; ++i ) {
            this.applyToTitle ( chart.getSubtitle ( i ) );
        }
        chart.setBackgroundPaint ( this.chartBackgroundPaint );
        final Plot plot = chart.getPlot();
        if ( plot != null ) {
            this.applyToPlot ( plot );
        }
    }
    protected void applyToTitle ( final Title title ) {
        if ( title instanceof TextTitle ) {
            final TextTitle tt = ( TextTitle ) title;
            tt.setFont ( this.largeFont );
            tt.setPaint ( this.subtitlePaint );
        } else if ( title instanceof LegendTitle ) {
            final LegendTitle lt = ( LegendTitle ) title;
            if ( lt.getBackgroundPaint() != null ) {
                lt.setBackgroundPaint ( this.legendBackgroundPaint );
            }
            lt.setItemFont ( this.regularFont );
            lt.setItemPaint ( this.legendItemPaint );
            if ( lt.getWrapper() != null ) {
                this.applyToBlockContainer ( lt.getWrapper() );
            }
        } else if ( title instanceof PaintScaleLegend ) {
            final PaintScaleLegend psl = ( PaintScaleLegend ) title;
            psl.setBackgroundPaint ( this.legendBackgroundPaint );
            final ValueAxis axis = psl.getAxis();
            if ( axis != null ) {
                this.applyToValueAxis ( axis );
            }
        } else if ( title instanceof CompositeTitle ) {
            final CompositeTitle ct = ( CompositeTitle ) title;
            final BlockContainer bc = ct.getContainer();
            final List blocks = bc.getBlocks();
            for ( final Block b : blocks ) {
                if ( b instanceof Title ) {
                    this.applyToTitle ( ( Title ) b );
                }
            }
        }
    }
    protected void applyToBlockContainer ( final BlockContainer bc ) {
        for ( final Block b : bc.getBlocks() ) {
            this.applyToBlock ( b );
        }
    }
    protected void applyToBlock ( final Block b ) {
        if ( b instanceof Title ) {
            this.applyToTitle ( ( Title ) b );
        } else if ( b instanceof LabelBlock ) {
            final LabelBlock lb = ( LabelBlock ) b;
            lb.setFont ( this.regularFont );
            lb.setPaint ( this.legendItemPaint );
        }
    }
    protected void applyToPlot ( final Plot plot ) {
        ParamChecks.nullNotPermitted ( plot, "plot" );
        if ( plot.getDrawingSupplier() != null ) {
            plot.setDrawingSupplier ( this.getDrawingSupplier() );
        }
        if ( plot.getBackgroundPaint() != null ) {
            plot.setBackgroundPaint ( this.plotBackgroundPaint );
        }
        plot.setOutlinePaint ( this.plotOutlinePaint );
        if ( plot instanceof PiePlot ) {
            this.applyToPiePlot ( ( PiePlot ) plot );
        } else if ( plot instanceof MultiplePiePlot ) {
            this.applyToMultiplePiePlot ( ( MultiplePiePlot ) plot );
        } else if ( plot instanceof CategoryPlot ) {
            this.applyToCategoryPlot ( ( CategoryPlot ) plot );
        } else if ( plot instanceof XYPlot ) {
            this.applyToXYPlot ( ( XYPlot ) plot );
        } else if ( plot instanceof FastScatterPlot ) {
            this.applyToFastScatterPlot ( ( FastScatterPlot ) plot );
        } else if ( plot instanceof MeterPlot ) {
            this.applyToMeterPlot ( ( MeterPlot ) plot );
        } else if ( plot instanceof ThermometerPlot ) {
            this.applyToThermometerPlot ( ( ThermometerPlot ) plot );
        } else if ( plot instanceof SpiderWebPlot ) {
            this.applyToSpiderWebPlot ( ( SpiderWebPlot ) plot );
        } else if ( plot instanceof PolarPlot ) {
            this.applyToPolarPlot ( ( PolarPlot ) plot );
        }
    }
    protected void applyToPiePlot ( final PiePlot plot ) {
        plot.setLabelLinkPaint ( this.labelLinkPaint );
        plot.setLabelLinkStyle ( this.labelLinkStyle );
        plot.setLabelFont ( this.regularFont );
        plot.setShadowGenerator ( this.shadowGenerator );
        if ( plot.getAutoPopulateSectionPaint() ) {
            plot.clearSectionPaints ( false );
        }
        if ( plot.getAutoPopulateSectionOutlinePaint() ) {
            plot.clearSectionOutlinePaints ( false );
        }
        if ( plot.getAutoPopulateSectionOutlineStroke() ) {
            plot.clearSectionOutlineStrokes ( false );
        }
    }
    protected void applyToMultiplePiePlot ( final MultiplePiePlot plot ) {
        this.apply ( plot.getPieChart() );
    }
    protected void applyToCategoryPlot ( final CategoryPlot plot ) {
        plot.setAxisOffset ( this.axisOffset );
        plot.setDomainGridlinePaint ( this.domainGridlinePaint );
        plot.setRangeGridlinePaint ( this.rangeGridlinePaint );
        plot.setRangeZeroBaselinePaint ( this.baselinePaint );
        plot.setShadowGenerator ( this.shadowGenerator );
        for ( int domainAxisCount = plot.getDomainAxisCount(), i = 0; i < domainAxisCount; ++i ) {
            final CategoryAxis axis = plot.getDomainAxis ( i );
            if ( axis != null ) {
                this.applyToCategoryAxis ( axis );
            }
        }
        for ( int rangeAxisCount = plot.getRangeAxisCount(), j = 0; j < rangeAxisCount; ++j ) {
            final ValueAxis axis2 = plot.getRangeAxis ( j );
            if ( axis2 != null ) {
                this.applyToValueAxis ( axis2 );
            }
        }
        for ( int rendererCount = plot.getRendererCount(), k = 0; k < rendererCount; ++k ) {
            final CategoryItemRenderer r = plot.getRenderer ( k );
            if ( r != null ) {
                this.applyToCategoryItemRenderer ( r );
            }
        }
        if ( plot instanceof CombinedDomainCategoryPlot ) {
            final CombinedDomainCategoryPlot cp = ( CombinedDomainCategoryPlot ) plot;
            for ( final CategoryPlot subplot : cp.getSubplots() ) {
                if ( subplot != null ) {
                    this.applyToPlot ( subplot );
                }
            }
        }
        if ( plot instanceof CombinedRangeCategoryPlot ) {
            final CombinedRangeCategoryPlot cp2 = ( CombinedRangeCategoryPlot ) plot;
            for ( final CategoryPlot subplot : cp2.getSubplots() ) {
                if ( subplot != null ) {
                    this.applyToPlot ( subplot );
                }
            }
        }
    }
    protected void applyToXYPlot ( final XYPlot plot ) {
        plot.setAxisOffset ( this.axisOffset );
        plot.setDomainZeroBaselinePaint ( this.baselinePaint );
        plot.setRangeZeroBaselinePaint ( this.baselinePaint );
        plot.setDomainGridlinePaint ( this.domainGridlinePaint );
        plot.setRangeGridlinePaint ( this.rangeGridlinePaint );
        plot.setDomainCrosshairPaint ( this.crosshairPaint );
        plot.setRangeCrosshairPaint ( this.crosshairPaint );
        plot.setShadowGenerator ( this.shadowGenerator );
        for ( int domainAxisCount = plot.getDomainAxisCount(), i = 0; i < domainAxisCount; ++i ) {
            final ValueAxis axis = plot.getDomainAxis ( i );
            if ( axis != null ) {
                this.applyToValueAxis ( axis );
            }
        }
        for ( int rangeAxisCount = plot.getRangeAxisCount(), j = 0; j < rangeAxisCount; ++j ) {
            final ValueAxis axis2 = plot.getRangeAxis ( j );
            if ( axis2 != null ) {
                this.applyToValueAxis ( axis2 );
            }
        }
        for ( int rendererCount = plot.getRendererCount(), k = 0; k < rendererCount; ++k ) {
            final XYItemRenderer r = plot.getRenderer ( k );
            if ( r != null ) {
                this.applyToXYItemRenderer ( r );
            }
        }
        for ( final XYAnnotation a : plot.getAnnotations() ) {
            this.applyToXYAnnotation ( a );
        }
        if ( plot instanceof CombinedDomainXYPlot ) {
            final CombinedDomainXYPlot cp = ( CombinedDomainXYPlot ) plot;
            for ( final XYPlot subplot : cp.getSubplots() ) {
                if ( subplot != null ) {
                    this.applyToPlot ( subplot );
                }
            }
        }
        if ( plot instanceof CombinedRangeXYPlot ) {
            final CombinedRangeXYPlot cp2 = ( CombinedRangeXYPlot ) plot;
            for ( final XYPlot subplot : cp2.getSubplots() ) {
                if ( subplot != null ) {
                    this.applyToPlot ( subplot );
                }
            }
        }
    }
    protected void applyToFastScatterPlot ( final FastScatterPlot plot ) {
        plot.setDomainGridlinePaint ( this.domainGridlinePaint );
        plot.setRangeGridlinePaint ( this.rangeGridlinePaint );
        final ValueAxis xAxis = plot.getDomainAxis();
        if ( xAxis != null ) {
            this.applyToValueAxis ( xAxis );
        }
        final ValueAxis yAxis = plot.getRangeAxis();
        if ( yAxis != null ) {
            this.applyToValueAxis ( yAxis );
        }
    }
    protected void applyToPolarPlot ( final PolarPlot plot ) {
        plot.setAngleLabelFont ( this.regularFont );
        plot.setAngleLabelPaint ( this.tickLabelPaint );
        plot.setAngleGridlinePaint ( this.domainGridlinePaint );
        plot.setRadiusGridlinePaint ( this.rangeGridlinePaint );
        final ValueAxis axis = plot.getAxis();
        if ( axis != null ) {
            this.applyToValueAxis ( axis );
        }
    }
    protected void applyToSpiderWebPlot ( final SpiderWebPlot plot ) {
        plot.setLabelFont ( this.regularFont );
        plot.setLabelPaint ( this.axisLabelPaint );
        plot.setAxisLinePaint ( this.axisLabelPaint );
    }
    protected void applyToMeterPlot ( final MeterPlot plot ) {
        plot.setDialBackgroundPaint ( this.plotBackgroundPaint );
        plot.setValueFont ( this.largeFont );
        plot.setValuePaint ( this.axisLabelPaint );
        plot.setDialOutlinePaint ( this.plotOutlinePaint );
        plot.setNeedlePaint ( this.thermometerPaint );
        plot.setTickLabelFont ( this.regularFont );
        plot.setTickLabelPaint ( this.tickLabelPaint );
    }
    protected void applyToThermometerPlot ( final ThermometerPlot plot ) {
        plot.setValueFont ( this.largeFont );
        plot.setThermometerPaint ( this.thermometerPaint );
        final ValueAxis axis = plot.getRangeAxis();
        if ( axis != null ) {
            this.applyToValueAxis ( axis );
        }
    }
    protected void applyToCategoryAxis ( final CategoryAxis axis ) {
        axis.setLabelFont ( this.largeFont );
        axis.setLabelPaint ( this.axisLabelPaint );
        axis.setTickLabelFont ( this.regularFont );
        axis.setTickLabelPaint ( this.tickLabelPaint );
        if ( axis instanceof SubCategoryAxis ) {
            final SubCategoryAxis sca = ( SubCategoryAxis ) axis;
            sca.setSubLabelFont ( this.regularFont );
            sca.setSubLabelPaint ( this.tickLabelPaint );
        }
    }
    protected void applyToValueAxis ( final ValueAxis axis ) {
        axis.setLabelFont ( this.largeFont );
        axis.setLabelPaint ( this.axisLabelPaint );
        axis.setTickLabelFont ( this.regularFont );
        axis.setTickLabelPaint ( this.tickLabelPaint );
        if ( axis instanceof SymbolAxis ) {
            this.applyToSymbolAxis ( ( SymbolAxis ) axis );
        }
        if ( axis instanceof PeriodAxis ) {
            this.applyToPeriodAxis ( ( PeriodAxis ) axis );
        }
    }
    protected void applyToSymbolAxis ( final SymbolAxis axis ) {
        axis.setGridBandPaint ( this.gridBandPaint );
        axis.setGridBandAlternatePaint ( this.gridBandAlternatePaint );
    }
    protected void applyToPeriodAxis ( final PeriodAxis axis ) {
        final PeriodAxisLabelInfo[] info = axis.getLabelInfo();
        for ( int i = 0; i < info.length; ++i ) {
            final PeriodAxisLabelInfo e = info[i];
            final PeriodAxisLabelInfo n = new PeriodAxisLabelInfo ( e.getPeriodClass(), e.getDateFormat(), e.getPadding(), this.regularFont, this.tickLabelPaint, e.getDrawDividers(), e.getDividerStroke(), e.getDividerPaint() );
            info[i] = n;
        }
        axis.setLabelInfo ( info );
    }
    protected void applyToAbstractRenderer ( final AbstractRenderer renderer ) {
        if ( renderer.getAutoPopulateSeriesPaint() ) {
            renderer.clearSeriesPaints ( false );
        }
        if ( renderer.getAutoPopulateSeriesStroke() ) {
            renderer.clearSeriesStrokes ( false );
        }
    }
    protected void applyToCategoryItemRenderer ( final CategoryItemRenderer renderer ) {
        ParamChecks.nullNotPermitted ( renderer, "renderer" );
        if ( renderer instanceof AbstractRenderer ) {
            this.applyToAbstractRenderer ( ( AbstractRenderer ) renderer );
        }
        renderer.setBaseItemLabelFont ( this.regularFont );
        renderer.setBaseItemLabelPaint ( this.itemLabelPaint );
        if ( renderer instanceof BarRenderer ) {
            final BarRenderer br = ( BarRenderer ) renderer;
            br.setBarPainter ( this.barPainter );
            br.setShadowVisible ( this.shadowVisible );
            br.setShadowPaint ( this.shadowPaint );
        }
        if ( renderer instanceof BarRenderer3D ) {
            final BarRenderer3D br3d = ( BarRenderer3D ) renderer;
            br3d.setWallPaint ( this.wallPaint );
        }
        if ( renderer instanceof LineRenderer3D ) {
            final LineRenderer3D lr3d = ( LineRenderer3D ) renderer;
            lr3d.setWallPaint ( this.wallPaint );
        }
        if ( renderer instanceof StatisticalBarRenderer ) {
            final StatisticalBarRenderer sbr = ( StatisticalBarRenderer ) renderer;
            sbr.setErrorIndicatorPaint ( this.errorIndicatorPaint );
        }
        if ( renderer instanceof MinMaxCategoryRenderer ) {
            final MinMaxCategoryRenderer mmcr = ( MinMaxCategoryRenderer ) renderer;
            mmcr.setGroupPaint ( this.errorIndicatorPaint );
        }
    }
    protected void applyToXYItemRenderer ( final XYItemRenderer renderer ) {
        ParamChecks.nullNotPermitted ( renderer, "renderer" );
        if ( renderer instanceof AbstractRenderer ) {
            this.applyToAbstractRenderer ( ( AbstractRenderer ) renderer );
        }
        renderer.setBaseItemLabelFont ( this.regularFont );
        renderer.setBaseItemLabelPaint ( this.itemLabelPaint );
        if ( renderer instanceof XYBarRenderer ) {
            final XYBarRenderer br = ( XYBarRenderer ) renderer;
            br.setBarPainter ( this.xyBarPainter );
            br.setShadowVisible ( this.shadowVisible );
        }
    }
    protected void applyToXYAnnotation ( final XYAnnotation annotation ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        if ( annotation instanceof XYTextAnnotation ) {
            final XYTextAnnotation xyta = ( XYTextAnnotation ) annotation;
            xyta.setFont ( this.smallFont );
            xyta.setPaint ( this.itemLabelPaint );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardChartTheme ) ) {
            return false;
        }
        final StandardChartTheme that = ( StandardChartTheme ) obj;
        return this.name.equals ( that.name ) && this.extraLargeFont.equals ( that.extraLargeFont ) && this.largeFont.equals ( that.largeFont ) && this.regularFont.equals ( that.regularFont ) && this.smallFont.equals ( that.smallFont ) && PaintUtilities.equal ( this.titlePaint, that.titlePaint ) && PaintUtilities.equal ( this.subtitlePaint, that.subtitlePaint ) && PaintUtilities.equal ( this.chartBackgroundPaint, that.chartBackgroundPaint ) && PaintUtilities.equal ( this.legendBackgroundPaint, that.legendBackgroundPaint ) && PaintUtilities.equal ( this.legendItemPaint, that.legendItemPaint ) && this.drawingSupplier.equals ( that.drawingSupplier ) && PaintUtilities.equal ( this.plotBackgroundPaint, that.plotBackgroundPaint ) && PaintUtilities.equal ( this.plotOutlinePaint, that.plotOutlinePaint ) && this.labelLinkStyle.equals ( that.labelLinkStyle ) && PaintUtilities.equal ( this.labelLinkPaint, that.labelLinkPaint ) && PaintUtilities.equal ( this.domainGridlinePaint, that.domainGridlinePaint ) && PaintUtilities.equal ( this.rangeGridlinePaint, that.rangeGridlinePaint ) && PaintUtilities.equal ( this.crosshairPaint, that.crosshairPaint ) && this.axisOffset.equals ( ( Object ) that.axisOffset ) && PaintUtilities.equal ( this.axisLabelPaint, that.axisLabelPaint ) && PaintUtilities.equal ( this.tickLabelPaint, that.tickLabelPaint ) && PaintUtilities.equal ( this.itemLabelPaint, that.itemLabelPaint ) && this.shadowVisible == that.shadowVisible && PaintUtilities.equal ( this.shadowPaint, that.shadowPaint ) && this.barPainter.equals ( that.barPainter ) && this.xyBarPainter.equals ( that.xyBarPainter ) && PaintUtilities.equal ( this.thermometerPaint, that.thermometerPaint ) && PaintUtilities.equal ( this.wallPaint, that.wallPaint ) && PaintUtilities.equal ( this.errorIndicatorPaint, that.errorIndicatorPaint ) && PaintUtilities.equal ( this.gridBandPaint, that.gridBandPaint ) && PaintUtilities.equal ( this.gridBandAlternatePaint, that.gridBandAlternatePaint );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.titlePaint, stream );
        SerialUtilities.writePaint ( this.subtitlePaint, stream );
        SerialUtilities.writePaint ( this.chartBackgroundPaint, stream );
        SerialUtilities.writePaint ( this.legendBackgroundPaint, stream );
        SerialUtilities.writePaint ( this.legendItemPaint, stream );
        SerialUtilities.writePaint ( this.plotBackgroundPaint, stream );
        SerialUtilities.writePaint ( this.plotOutlinePaint, stream );
        SerialUtilities.writePaint ( this.labelLinkPaint, stream );
        SerialUtilities.writePaint ( this.baselinePaint, stream );
        SerialUtilities.writePaint ( this.domainGridlinePaint, stream );
        SerialUtilities.writePaint ( this.rangeGridlinePaint, stream );
        SerialUtilities.writePaint ( this.crosshairPaint, stream );
        SerialUtilities.writePaint ( this.axisLabelPaint, stream );
        SerialUtilities.writePaint ( this.tickLabelPaint, stream );
        SerialUtilities.writePaint ( this.itemLabelPaint, stream );
        SerialUtilities.writePaint ( this.shadowPaint, stream );
        SerialUtilities.writePaint ( this.thermometerPaint, stream );
        SerialUtilities.writePaint ( this.wallPaint, stream );
        SerialUtilities.writePaint ( this.errorIndicatorPaint, stream );
        SerialUtilities.writePaint ( this.gridBandPaint, stream );
        SerialUtilities.writePaint ( this.gridBandAlternatePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.titlePaint = SerialUtilities.readPaint ( stream );
        this.subtitlePaint = SerialUtilities.readPaint ( stream );
        this.chartBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.legendBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.legendItemPaint = SerialUtilities.readPaint ( stream );
        this.plotBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.plotOutlinePaint = SerialUtilities.readPaint ( stream );
        this.labelLinkPaint = SerialUtilities.readPaint ( stream );
        this.baselinePaint = SerialUtilities.readPaint ( stream );
        this.domainGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeGridlinePaint = SerialUtilities.readPaint ( stream );
        this.crosshairPaint = SerialUtilities.readPaint ( stream );
        this.axisLabelPaint = SerialUtilities.readPaint ( stream );
        this.tickLabelPaint = SerialUtilities.readPaint ( stream );
        this.itemLabelPaint = SerialUtilities.readPaint ( stream );
        this.shadowPaint = SerialUtilities.readPaint ( stream );
        this.thermometerPaint = SerialUtilities.readPaint ( stream );
        this.wallPaint = SerialUtilities.readPaint ( stream );
        this.errorIndicatorPaint = SerialUtilities.readPaint ( stream );
        this.gridBandPaint = SerialUtilities.readPaint ( stream );
        this.gridBandAlternatePaint = SerialUtilities.readPaint ( stream );
    }
}
