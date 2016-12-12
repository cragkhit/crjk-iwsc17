package org.jfree.chart.fx.interaction;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.Zoomable;
import java.awt.geom.Rectangle2D;
import org.jfree.util.ShapeUtilities;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
import java.awt.geom.Point2D;
import org.jfree.chart.fx.ChartViewer;
public class ZoomHandlerFX extends AbstractMouseHandlerFX {
    private ChartViewer viewer;
    private Point2D startPoint;
    public ZoomHandlerFX ( final String id, final ChartViewer parent ) {
        this ( id, parent, false, false, false, false );
    }
    public ZoomHandlerFX ( final String id, final ChartViewer parent, final boolean altKey, final boolean ctrlKey, final boolean metaKey, final boolean shiftKey ) {
        super ( id, altKey, ctrlKey, metaKey, shiftKey );
        this.viewer = parent;
    }
    @Override
    public void handleMousePressed ( final ChartCanvas canvas, final MouseEvent e ) {
        final Point2D pt = new Point2D.Double ( e.getX(), e.getY() );
        final Rectangle2D dataArea = canvas.findDataArea ( pt );
        if ( dataArea != null ) {
            this.startPoint = ShapeUtilities.getPointInRectangle ( e.getX(), e.getY(), dataArea );
        } else {
            this.startPoint = null;
            canvas.clearLiveHandler();
        }
    }
    @Override
    public void handleMouseDragged ( final ChartCanvas canvas, final MouseEvent e ) {
        if ( this.startPoint == null ) {
            canvas.clearLiveHandler();
            return;
        }
        final Plot p = canvas.getChart().getPlot();
        if ( ! ( p instanceof Zoomable ) ) {
            return;
        }
        final Zoomable z = ( Zoomable ) p;
        boolean hZoom;
        boolean vZoom;
        if ( z.getOrientation().isHorizontal() ) {
            hZoom = z.isRangeZoomable();
            vZoom = z.isDomainZoomable();
        } else {
            hZoom = z.isDomainZoomable();
            vZoom = z.isRangeZoomable();
        }
        final Rectangle2D dataArea = canvas.findDataArea ( this.startPoint );
        double x = this.startPoint.getX();
        double y = this.startPoint.getY();
        double w = 0.0;
        double h = 0.0;
        if ( hZoom && vZoom ) {
            final double xmax = Math.min ( e.getX(), dataArea.getMaxX() );
            final double ymax = Math.min ( e.getY(), dataArea.getMaxY() );
            w = xmax - this.startPoint.getX();
            h = ymax - this.startPoint.getY();
        } else if ( hZoom ) {
            final double xmax = Math.min ( e.getX(), dataArea.getMaxX() );
            y = dataArea.getMinY();
            w = xmax - this.startPoint.getX();
            h = dataArea.getHeight();
        } else if ( vZoom ) {
            final double ymax2 = Math.min ( e.getY(), dataArea.getMaxY() );
            x = dataArea.getMinX();
            w = dataArea.getWidth();
            h = ymax2 - this.startPoint.getY();
        }
        this.viewer.showZoomRectangle ( x, y, w, h );
    }
    @Override
    public void handleMouseReleased ( final ChartCanvas canvas, final MouseEvent e ) {
        final Plot p = canvas.getChart().getPlot();
        if ( ! ( p instanceof Zoomable ) ) {
            return;
        }
        final Zoomable z = ( Zoomable ) p;
        boolean hZoom;
        boolean vZoom;
        if ( z.getOrientation().isHorizontal() ) {
            hZoom = z.isRangeZoomable();
            vZoom = z.isDomainZoomable();
        } else {
            hZoom = z.isDomainZoomable();
            vZoom = z.isRangeZoomable();
        }
        final boolean zoomTrigger1 = hZoom && Math.abs ( e.getX() - this.startPoint.getX() ) >= 10.0;
        final boolean zoomTrigger2 = vZoom && Math.abs ( e.getY() - this.startPoint.getY() ) >= 10.0;
        if ( zoomTrigger1 || zoomTrigger2 ) {
            final Point2D endPoint = new Point2D.Double ( e.getX(), e.getY() );
            final PlotRenderingInfo pri = canvas.getRenderingInfo().getPlotInfo();
            if ( ( hZoom && e.getX() < this.startPoint.getX() ) || ( vZoom && e.getY() < this.startPoint.getY() ) ) {
                final boolean saved = p.isNotify();
                p.setNotify ( false );
                z.zoomDomainAxes ( 0.0, pri, endPoint );
                z.zoomRangeAxes ( 0.0, pri, endPoint );
                p.setNotify ( saved );
            } else {
                double x = this.startPoint.getX();
                double y = this.startPoint.getY();
                double w = e.getX() - x;
                double h = e.getY() - y;
                final Rectangle2D dataArea = canvas.findDataArea ( this.startPoint );
                final double maxX = dataArea.getMaxX();
                final double maxY = dataArea.getMaxY();
                if ( !vZoom ) {
                    y = dataArea.getMinY();
                    w = Math.min ( w, maxX - this.startPoint.getX() );
                    h = dataArea.getHeight();
                } else if ( !hZoom ) {
                    x = dataArea.getMinX();
                    w = dataArea.getWidth();
                    h = Math.min ( h, maxY - this.startPoint.getY() );
                } else {
                    w = Math.min ( w, maxX - this.startPoint.getX() );
                    h = Math.min ( h, maxY - this.startPoint.getY() );
                }
                final Rectangle2D zoomArea = new Rectangle2D.Double ( x, y, w, h );
                final boolean saved2 = p.isNotify();
                p.setNotify ( false );
                final double pw0 = this.percentW ( x, dataArea );
                final double pw = this.percentW ( x + w, dataArea );
                final double ph0 = this.percentH ( y, dataArea );
                final double ph = this.percentH ( y + h, dataArea );
                final PlotRenderingInfo info = this.viewer.getRenderingInfo().getPlotInfo();
                if ( z.getOrientation().isVertical() ) {
                    z.zoomDomainAxes ( pw0, pw, info, endPoint );
                    z.zoomRangeAxes ( 1.0 - ph, 1.0 - ph0, info, endPoint );
                } else {
                    z.zoomRangeAxes ( pw0, pw, info, endPoint );
                    z.zoomDomainAxes ( 1.0 - ph, 1.0 - ph0, info, endPoint );
                }
                p.setNotify ( saved2 );
            }
        }
        this.viewer.hideZoomRectangle();
        this.startPoint = null;
        canvas.clearLiveHandler();
    }
    private double percentW ( final double x, final Rectangle2D r ) {
        return ( x - r.getMinX() ) / r.getWidth();
    }
    private double percentH ( final double y, final Rectangle2D r ) {
        return ( y - r.getMinY() ) / r.getHeight();
    }
}
