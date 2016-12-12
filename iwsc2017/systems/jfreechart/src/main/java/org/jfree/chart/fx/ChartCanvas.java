package org.jfree.chart.fx;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.FontSmoothingType;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.fx.interaction.AnchorHandlerFX;
import org.jfree.chart.fx.interaction.DispatchHandlerFX;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.interaction.TooltipHandlerFX;
import org.jfree.chart.fx.interaction.ScrollHandlerFX;
import org.jfree.chart.fx.interaction.PanHandlerFX;
import org.jfree.chart.fx.interaction.MouseHandlerFX;
import org.jfree.chart.fx.overlay.OverlayFX;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.ParamChecks;
import org.jfree.fx.FXGraphics2D;
import org.jfree.fx.FXHints;
public class ChartCanvas extends Canvas implements ChartChangeListener,
    OverlayChangeListener {
    private JFreeChart chart;
    private Graphics2D g2;
    private Point2D anchor;
    private ChartRenderingInfo info;
    private Tooltip tooltip;
    private boolean tooltipEnabled;
    private transient List<ChartMouseListenerFX> chartMouseListeners;
    private MouseHandlerFX liveHandler;
    private List<MouseHandlerFX> availableMouseHandlers;
    private List<MouseHandlerFX> auxiliaryMouseHandlers;
    private ObservableList<OverlayFX> overlays;
    private boolean domainZoomable;
    private boolean rangeZoomable;
    public ChartCanvas ( JFreeChart chart ) {
        this.chart = chart;
        if ( this.chart != null ) {
            this.chart.addChangeListener ( this );
        }
        this.tooltip = null;
        this.tooltipEnabled = true;
        this.chartMouseListeners = new ArrayList<ChartMouseListenerFX>();
        widthProperty().addListener ( e -> draw() );
        heightProperty().addListener ( e -> draw() );
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFontSmoothingType ( FontSmoothingType.LCD );
        FXGraphics2D fxg2 = new FXGraphics2D ( gc );
        fxg2.setRenderingHint ( FXHints.KEY_USE_FX_FONT_METRICS, true );
        fxg2.setZeroStrokeWidth ( 0.1 );
        fxg2.setRenderingHint (
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON );
        this.g2 = fxg2;
        this.liveHandler = null;
        this.availableMouseHandlers = new ArrayList<MouseHandlerFX>();
        this.availableMouseHandlers.add ( new PanHandlerFX ( "pan", true, false,
                                          false, false ) );
        this.auxiliaryMouseHandlers = new ArrayList<MouseHandlerFX>();
        this.auxiliaryMouseHandlers.add ( new TooltipHandlerFX ( "tooltip" ) );
        this.auxiliaryMouseHandlers.add ( new ScrollHandlerFX ( "scroll" ) );
        this.domainZoomable = true;
        this.rangeZoomable = true;
        this.auxiliaryMouseHandlers.add ( new AnchorHandlerFX ( "anchor" ) );
        this.auxiliaryMouseHandlers.add ( new DispatchHandlerFX ( "dispatch" ) );
        this.overlays = FXCollections.observableArrayList();
        setOnMouseMoved ( e -> handleMouseMoved ( e ) );
        setOnMouseClicked ( e -> handleMouseClicked ( e ) );
        setOnMousePressed ( e -> handleMousePressed ( e ) );
        setOnMouseDragged ( e -> handleMouseDragged ( e ) );
        setOnMouseReleased ( e -> handleMouseReleased ( e ) );
        setOnScroll ( e -> handleScroll ( e ) );
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( JFreeChart chart ) {
        if ( this.chart != null ) {
            this.chart.removeChangeListener ( this );
        }
        this.chart = chart;
        if ( this.chart != null ) {
            this.chart.addChangeListener ( this );
        }
        draw();
    }
    public boolean isDomainZoomable() {
        return this.domainZoomable;
    }
    public void setDomainZoomable ( boolean zoomable ) {
        this.domainZoomable = zoomable;
    }
    public boolean isRangeZoomable() {
        return this.rangeZoomable;
    }
    public void setRangeZoomable ( boolean zoomable ) {
        this.rangeZoomable = zoomable;
    }
    public ChartRenderingInfo getRenderingInfo() {
        return this.info;
    }
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }
    public void setTooltipEnabled ( boolean tooltipEnabled ) {
        this.tooltipEnabled = tooltipEnabled;
    }
    public void setAnchor ( Point2D anchor ) {
        this.anchor = anchor;
        if ( this.chart != null ) {
            this.chart.setNotify ( true );
        }
    }
    public void addOverlay ( OverlayFX overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        this.overlays.add ( overlay );
        overlay.addChangeListener ( this );
        draw();
    }
    public void removeOverlay ( OverlayFX overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        boolean removed = this.overlays.remove ( overlay );
        if ( removed ) {
            overlay.removeChangeListener ( this );
            draw();
        }
    }
    @Override
    public void overlayChanged ( OverlayChangeEvent event ) {
        draw();
    }
    public void addChartMouseListener ( ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.add ( listener );
    }
    public void removeChartMouseListener ( ChartMouseListenerFX listener ) {
        this.chartMouseListeners.remove ( listener );
    }
    public MouseHandlerFX getMouseHandler ( String id ) {
        for ( MouseHandlerFX h : this.availableMouseHandlers ) {
            if ( h.getID().equals ( id ) ) {
                return h;
            }
        }
        for ( MouseHandlerFX h : this.auxiliaryMouseHandlers ) {
            if ( h.getID().equals ( id ) ) {
                return h;
            }
        }
        return null;
    }
    public void addMouseHandler ( MouseHandlerFX handler ) {
        if ( !this.hasUniqueID ( handler ) ) {
            throw new IllegalArgumentException (
                "There is already a handler with that ID ("
                + handler.getID() + ")." );
        }
        this.availableMouseHandlers.add ( handler );
    }
    public void removeMouseHandler ( MouseHandlerFX handler ) {
        this.availableMouseHandlers.remove ( handler );
    }
    private boolean hasUniqueID ( MouseHandlerFX handler ) {
        for ( MouseHandlerFX h : this.availableMouseHandlers ) {
            if ( handler.getID().equals ( h.getID() ) ) {
                return false;
            }
        }
        for ( MouseHandlerFX h : this.auxiliaryMouseHandlers ) {
            if ( handler.getID().equals ( h.getID() ) ) {
                return false;
            }
        }
        return true;
    }
    public void clearLiveHandler() {
        this.liveHandler = null;
    }
    public final void draw() {
        GraphicsContext ctx = getGraphicsContext2D();
        ctx.save();
        double width = getWidth();
        double height = getHeight();
        if ( width > 0 && height > 0 ) {
            ctx.clearRect ( 0, 0, width, height );
            this.info = new ChartRenderingInfo();
            if ( this.chart != null ) {
                this.chart.draw ( this.g2, new Rectangle ( ( int ) width,
                                  ( int ) height ), this.anchor, this.info );
            }
        }
        ctx.restore();
        for ( OverlayFX overlay : this.overlays ) {
            overlay.paintOverlay ( g2, this );
        }
        this.anchor = null;
    }
    public Rectangle2D findDataArea ( Point2D point ) {
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D result;
        if ( plotInfo.getSubplotCount() == 0 ) {
            result = plotInfo.getDataArea();
        } else {
            int subplotIndex = plotInfo.getSubplotIndex ( point );
            if ( subplotIndex == -1 ) {
                return null;
            }
            result = plotInfo.getSubplotInfo ( subplotIndex ).getDataArea();
        }
        return result;
    }
    @Override
    public boolean isResizable() {
        return true;
    }
    public void setTooltip ( String text, double x, double y ) {
        if ( text != null ) {
            if ( this.tooltip == null ) {
                this.tooltip = new Tooltip ( text );
                Tooltip.install ( this, this.tooltip );
            } else {
                this.tooltip.setText ( text );
                this.tooltip.setAnchorX ( x );
                this.tooltip.setAnchorY ( y );
            }
        } else {
            Tooltip.uninstall ( this, this.tooltip );
            this.tooltip = null;
        }
    }
    private void handleMousePressed ( MouseEvent e ) {
        if ( this.liveHandler == null ) {
            for ( MouseHandlerFX handler : this.availableMouseHandlers ) {
                if ( handler.isEnabled() && handler.hasMatchingModifiers ( e ) ) {
                    this.liveHandler = handler;
                }
            }
        }
        if ( this.liveHandler != null ) {
            this.liveHandler.handleMousePressed ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMousePressed ( this, e );
            }
        }
    }
    private void handleMouseMoved ( MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseMoved ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseMoved ( this, e );
            }
        }
    }
    private void handleMouseDragged ( MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseDragged ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseDragged ( this, e );
            }
        }
    }
    private void handleMouseReleased ( MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseReleased ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseReleased ( this, e );
            }
        }
    }
    private void handleMouseClicked ( MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseClicked ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseClicked ( this, e );
            }
        }
    }
    protected void handleScroll ( ScrollEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleScroll ( this, e );
        }
        for ( MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleScroll ( this, e );
            }
        }
    }
    @Override
    public void chartChanged ( ChartChangeEvent event ) {
        draw();
    }
    public void dispatchMouseMovedEvent ( Point2D point, MouseEvent e ) {
        double x = point.getX();
        double y = point.getY();
        ChartEntity entity = this.info.getEntityCollection().getEntity ( x, y );
        ChartMouseEventFX event = new ChartMouseEventFX ( this.chart, e, entity );
        for ( ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseMoved ( event );
        }
    }
    public void dispatchMouseClickedEvent ( Point2D point, MouseEvent e ) {
        double x = point.getX();
        double y = point.getY();
        ChartEntity entity = this.info.getEntityCollection().getEntity ( x, y );
        ChartMouseEventFX event = new ChartMouseEventFX ( this.chart, e, entity );
        for ( ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseClicked ( event );
        }
    }
}
