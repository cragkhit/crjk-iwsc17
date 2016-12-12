package org.jfree.chart.fx.interaction;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.util.ShapeUtilities;
public class ZoomHandlerFX extends AbstractMouseHandlerFX {
    private ChartViewer viewer;
    private Point2D startPoint;
    public ZoomHandlerFX ( String id, ChartViewer parent ) {
        this ( id, parent, false, false, false, false );
    }
    public ZoomHandlerFX ( String id, ChartViewer parent, boolean altKey,
                           boolean ctrlKey, boolean metaKey, boolean shiftKey ) {
        super ( id, altKey, ctrlKey, metaKey, shiftKey );
        this.viewer = parent;
    }
    @Override
    public void handleMousePressed ( ChartCanvas canvas, MouseEvent e ) {
        Point2D pt = new Point2D.Double ( e.getX(), e.getY() );
        Rectangle2D dataArea = canvas.findDataArea ( pt );
        if ( dataArea != null ) {
            this.startPoint = ShapeUtilities.getPointInRectangle ( e.getX(),
                              e.getY(), dataArea );
        } else {
            this.startPoint = null;
            canvas.clearLiveHandler();
        }
    }
    @Override
    public void handleMouseDragged ( ChartCanvas canvas, MouseEvent e ) {
        if ( this.startPoint == null ) {
            canvas.clearLiveHandler();
            return;
        }
        boolean hZoom, vZoom;
        Plot p = canvas.getChart().getPlot();
        if ( ! ( p instanceof Zoomable ) ) {
            return;
        }
        Zoomable z = ( Zoomable ) p;
        if ( z.getOrientation().isHorizontal() ) {
            hZoom = z.isRangeZoomable();
            vZoom = z.isDomainZoomable();
        } else {
            hZoom = z.isDomainZoomable();
            vZoom = z.isRangeZoomable();
        }
        Rectangle2D dataArea = canvas.findDataArea ( this.startPoint );
        double x = this.startPoint.getX();
        double y = this.startPoint.getY();
        double w = 0;
        double h = 0;
        if ( hZoom && vZoom ) {
            double xmax = Math.min ( e.getX(), dataArea.getMaxX() );
            double ymax = Math.min ( e.getY(), dataArea.getMaxY() );
            w = xmax - this.startPoint.getX();
            h = ymax - this.startPoint.getY();
        } else if ( hZoom ) {
            double xmax = Math.min ( e.getX(), dataArea.getMaxX() );
            y = dataArea.getMinY();
            w = xmax - this.startPoint.getX();
            h = dataArea.getHeight();
        } else if ( vZoom ) {
            double ymax = Math.min ( e.getY(), dataArea.getMaxY() );
            x = dataArea.getMinX();
            w = dataArea.getWidth();
            h = ymax - this.startPoint.getY();
        }
        viewer.showZoomRectangle ( x, y, w, h );
    }
    @Override
    public void handleMouseReleased ( ChartCanvas canvas, MouseEvent e ) {
        Plot p = canvas.getChart().getPlot();
        if ( ! ( p instanceof Zoomable ) ) {
            return;
        }
        boolean hZoom, vZoom;
        Zoomable z = ( Zoomable ) p;
        if ( z.getOrientation().isHorizontal() ) {
            hZoom = z.isRangeZoomable();
            vZoom = z.isDomainZoomable();
        } else {
            hZoom = z.isDomainZoomable();
            vZoom = z.isRangeZoomable();
        }
        boolean zoomTrigger1 = hZoom && Math.abs ( e.getX()
                               - this.startPoint.getX() ) >= 10;
        boolean zoomTrigger2 = vZoom && Math.abs ( e.getY()
                               - this.startPoint.getY() ) >= 10;
        if ( zoomTrigger1 || zoomTrigger2 ) {
            Point2D endPoint = new Point2D.Double ( e.getX(), e.getY() );
            PlotRenderingInfo pri = canvas.getRenderingInfo().getPlotInfo();
            if ( ( hZoom && ( e.getX() < this.startPoint.getX() ) )
                    || ( vZoom && ( e.getY() < this.startPoint.getY() ) ) ) {
                boolean saved = p.isNotify();
                p.setNotify ( false );
                z.zoomDomainAxes ( 0, pri, endPoint );
                z.zoomRangeAxes ( 0, pri, endPoint );
                p.setNotify ( saved );
            } else {
                double x = this.startPoint.getX();
                double y = this.startPoint.getY();
                double w = e.getX() - x;
                double h = e.getY() - y;
                Rectangle2D dataArea = canvas.findDataArea ( this.startPoint );
                double maxX = dataArea.getMaxX();
                double maxY = dataArea.getMaxY();
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
                Rectangle2D zoomArea = new Rectangle2D.Double ( x, y, w, h );
                boolean saved = p.isNotify();
                p.setNotify ( false );
                double pw0 = percentW ( x, dataArea );
                double pw1 = percentW ( x + w, dataArea );
                double ph0 = percentH ( y, dataArea );
                double ph1 = percentH ( y + h, dataArea );
                PlotRenderingInfo info
                    = this.viewer.getRenderingInfo().getPlotInfo();
                if ( z.getOrientation().isVertical() ) {
                    z.zoomDomainAxes ( pw0, pw1, info, endPoint );
                    z.zoomRangeAxes ( 1 - ph1, 1 - ph0, info, endPoint );
                } else {
                    z.zoomRangeAxes ( pw0, pw1, info, endPoint );
                    z.zoomDomainAxes ( 1 - ph1, 1 - ph0, info, endPoint );
                }
                p.setNotify ( saved );
            }
        }
        viewer.hideZoomRectangle();
        this.startPoint = null;
        canvas.clearLiveHandler();
    }
    private double percentW ( double x, Rectangle2D r ) {
        return ( x - r.getMinX() ) / r.getWidth();
    }
    private double percentH ( double y, Rectangle2D r ) {
        return ( y - r.getMinY() ) / r.getHeight();
    }
}
