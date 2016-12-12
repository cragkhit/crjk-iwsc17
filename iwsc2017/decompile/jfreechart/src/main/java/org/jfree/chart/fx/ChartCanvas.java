package org.jfree.chart.fx;
import javafx.beans.Observable;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.event.ChartChangeEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.util.Iterator;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.util.ParamChecks;
import javafx.scene.canvas.GraphicsContext;
import javafx.collections.FXCollections;
import org.jfree.chart.fx.interaction.DispatchHandlerFX;
import org.jfree.chart.fx.interaction.AnchorHandlerFX;
import org.jfree.chart.fx.interaction.ScrollHandlerFX;
import org.jfree.chart.fx.interaction.TooltipHandlerFX;
import org.jfree.chart.fx.interaction.PanHandlerFX;
import java.awt.RenderingHints;
import org.jfree.fx.FXHints;
import org.jfree.fx.FXGraphics2D;
import javafx.scene.text.FontSmoothingType;
import java.util.ArrayList;
import org.jfree.chart.fx.overlay.OverlayFX;
import javafx.collections.ObservableList;
import org.jfree.chart.fx.interaction.MouseHandlerFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import java.util.List;
import javafx.scene.control.Tooltip;
import org.jfree.chart.ChartRenderingInfo;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.event.ChartChangeListener;
import javafx.scene.canvas.Canvas;
public class ChartCanvas extends Canvas implements ChartChangeListener, OverlayChangeListener {
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
    public ChartCanvas ( final JFreeChart chart ) {
        this.chart = chart;
        if ( this.chart != null ) {
            this.chart.addChangeListener ( this );
        }
        this.tooltip = null;
        this.tooltipEnabled = true;
        this.chartMouseListeners = new ArrayList<ChartMouseListenerFX>();
        this.widthProperty().addListener ( e -> this.draw() );
        this.heightProperty().addListener ( e -> this.draw() );
        final GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFontSmoothingType ( FontSmoothingType.LCD );
        final FXGraphics2D fxg2 = new FXGraphics2D ( gc );
        fxg2.setRenderingHint ( ( RenderingHints.Key ) FXHints.KEY_USE_FX_FONT_METRICS, ( Object ) true );
        fxg2.setZeroStrokeWidth ( 0.1 );
        fxg2.setRenderingHint ( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
        this.g2 = ( Graphics2D ) fxg2;
        this.liveHandler = null;
        ( this.availableMouseHandlers = new ArrayList<MouseHandlerFX>() ).add ( new PanHandlerFX ( "pan", true, false, false, false ) );
        ( this.auxiliaryMouseHandlers = new ArrayList<MouseHandlerFX>() ).add ( new TooltipHandlerFX ( "tooltip" ) );
        this.auxiliaryMouseHandlers.add ( new ScrollHandlerFX ( "scroll" ) );
        this.domainZoomable = true;
        this.rangeZoomable = true;
        this.auxiliaryMouseHandlers.add ( new AnchorHandlerFX ( "anchor" ) );
        this.auxiliaryMouseHandlers.add ( new DispatchHandlerFX ( "dispatch" ) );
        this.overlays = ( ObservableList<OverlayFX> ) FXCollections.observableArrayList();
        this.setOnMouseMoved ( e -> this.handleMouseMoved ( e ) );
        this.setOnMouseClicked ( e -> this.handleMouseClicked ( e ) );
        this.setOnMousePressed ( e -> this.handleMousePressed ( e ) );
        this.setOnMouseDragged ( e -> this.handleMouseDragged ( e ) );
        this.setOnMouseReleased ( e -> this.handleMouseReleased ( e ) );
        this.setOnScroll ( e -> this.handleScroll ( e ) );
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( final JFreeChart chart ) {
        if ( this.chart != null ) {
            this.chart.removeChangeListener ( this );
        }
        this.chart = chart;
        if ( this.chart != null ) {
            this.chart.addChangeListener ( this );
        }
        this.draw();
    }
    public boolean isDomainZoomable() {
        return this.domainZoomable;
    }
    public void setDomainZoomable ( final boolean zoomable ) {
        this.domainZoomable = zoomable;
    }
    public boolean isRangeZoomable() {
        return this.rangeZoomable;
    }
    public void setRangeZoomable ( final boolean zoomable ) {
        this.rangeZoomable = zoomable;
    }
    public ChartRenderingInfo getRenderingInfo() {
        return this.info;
    }
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }
    public void setTooltipEnabled ( final boolean tooltipEnabled ) {
        this.tooltipEnabled = tooltipEnabled;
    }
    public void setAnchor ( final Point2D anchor ) {
        this.anchor = anchor;
        if ( this.chart != null ) {
            this.chart.setNotify ( true );
        }
    }
    public void addOverlay ( final OverlayFX overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        this.overlays.add ( ( Object ) overlay );
        overlay.addChangeListener ( this );
        this.draw();
    }
    public void removeOverlay ( final OverlayFX overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        final boolean removed = this.overlays.remove ( ( Object ) overlay );
        if ( removed ) {
            overlay.removeChangeListener ( this );
            this.draw();
        }
    }
    public void overlayChanged ( final OverlayChangeEvent event ) {
        this.draw();
    }
    public void addChartMouseListener ( final ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.add ( listener );
    }
    public void removeChartMouseListener ( final ChartMouseListenerFX listener ) {
        this.chartMouseListeners.remove ( listener );
    }
    public MouseHandlerFX getMouseHandler ( final String id ) {
        for ( final MouseHandlerFX h : this.availableMouseHandlers ) {
            if ( h.getID().equals ( id ) ) {
                return h;
            }
        }
        for ( final MouseHandlerFX h : this.auxiliaryMouseHandlers ) {
            if ( h.getID().equals ( id ) ) {
                return h;
            }
        }
        return null;
    }
    public void addMouseHandler ( final MouseHandlerFX handler ) {
        if ( !this.hasUniqueID ( handler ) ) {
            throw new IllegalArgumentException ( "There is already a handler with that ID (" + handler.getID() + ")." );
        }
        this.availableMouseHandlers.add ( handler );
    }
    public void removeMouseHandler ( final MouseHandlerFX handler ) {
        this.availableMouseHandlers.remove ( handler );
    }
    private boolean hasUniqueID ( final MouseHandlerFX handler ) {
        for ( final MouseHandlerFX h : this.availableMouseHandlers ) {
            if ( handler.getID().equals ( h.getID() ) ) {
                return false;
            }
        }
        for ( final MouseHandlerFX h : this.auxiliaryMouseHandlers ) {
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
        final GraphicsContext ctx = this.getGraphicsContext2D();
        ctx.save();
        final double width = this.getWidth();
        final double height = this.getHeight();
        if ( width > 0.0 && height > 0.0 ) {
            ctx.clearRect ( 0.0, 0.0, width, height );
            this.info = new ChartRenderingInfo();
            if ( this.chart != null ) {
                this.chart.draw ( this.g2, new Rectangle ( ( int ) width, ( int ) height ), this.anchor, this.info );
            }
        }
        ctx.restore();
        for ( final OverlayFX overlay : this.overlays ) {
            overlay.paintOverlay ( this.g2, this );
        }
        this.anchor = null;
    }
    public Rectangle2D findDataArea ( final Point2D point ) {
        final PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D result;
        if ( plotInfo.getSubplotCount() == 0 ) {
            result = plotInfo.getDataArea();
        } else {
            final int subplotIndex = plotInfo.getSubplotIndex ( point );
            if ( subplotIndex == -1 ) {
                return null;
            }
            result = plotInfo.getSubplotInfo ( subplotIndex ).getDataArea();
        }
        return result;
    }
    public boolean isResizable() {
        return true;
    }
    public void setTooltip ( final String text, final double x, final double y ) {
        if ( text != null ) {
            if ( this.tooltip == null ) {
                Tooltip.install ( ( Node ) this, this.tooltip = new Tooltip ( text ) );
            } else {
                this.tooltip.setText ( text );
                this.tooltip.setAnchorX ( x );
                this.tooltip.setAnchorY ( y );
            }
        } else {
            Tooltip.uninstall ( ( Node ) this, this.tooltip );
            this.tooltip = null;
        }
    }
    private void handleMousePressed ( final MouseEvent e ) {
        if ( this.liveHandler == null ) {
            for ( final MouseHandlerFX handler : this.availableMouseHandlers ) {
                if ( handler.isEnabled() && handler.hasMatchingModifiers ( e ) ) {
                    this.liveHandler = handler;
                }
            }
        }
        if ( this.liveHandler != null ) {
            this.liveHandler.handleMousePressed ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMousePressed ( this, e );
            }
        }
    }
    private void handleMouseMoved ( final MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseMoved ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseMoved ( this, e );
            }
        }
    }
    private void handleMouseDragged ( final MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseDragged ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseDragged ( this, e );
            }
        }
    }
    private void handleMouseReleased ( final MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseReleased ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseReleased ( this, e );
            }
        }
    }
    private void handleMouseClicked ( final MouseEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleMouseClicked ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleMouseClicked ( this, e );
            }
        }
    }
    protected void handleScroll ( final ScrollEvent e ) {
        if ( this.liveHandler != null && this.liveHandler.isEnabled() ) {
            this.liveHandler.handleScroll ( this, e );
        }
        for ( final MouseHandlerFX handler : this.auxiliaryMouseHandlers ) {
            if ( handler.isEnabled() ) {
                handler.handleScroll ( this, e );
            }
        }
    }
    public void chartChanged ( final ChartChangeEvent event ) {
        this.draw();
    }
    public void dispatchMouseMovedEvent ( final Point2D point, final MouseEvent e ) {
        final double x = point.getX();
        final double y = point.getY();
        final ChartEntity entity = this.info.getEntityCollection().getEntity ( x, y );
        final ChartMouseEventFX event = new ChartMouseEventFX ( this.chart, e, entity );
        for ( final ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseMoved ( event );
        }
    }
    public void dispatchMouseClickedEvent ( final Point2D point, final MouseEvent e ) {
        final double x = point.getX();
        final double y = point.getY();
        final ChartEntity entity = this.info.getEntityCollection().getEntity ( x, y );
        final ChartMouseEventFX event = new ChartMouseEventFX ( this.chart, e, entity );
        for ( final ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseClicked ( event );
        }
    }
}
