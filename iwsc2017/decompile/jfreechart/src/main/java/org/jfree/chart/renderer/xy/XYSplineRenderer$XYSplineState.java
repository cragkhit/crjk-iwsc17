package org.jfree.chart.renderer.xy;
import java.util.ArrayList;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Point2D;
import java.util.List;
import java.awt.geom.GeneralPath;
public static class XYSplineState extends State {
    public GeneralPath fillArea;
    public List<Point2D> points;
    public XYSplineState ( final PlotRenderingInfo info ) {
        super ( info );
        this.fillArea = new GeneralPath();
        this.points = new ArrayList<Point2D>();
    }
}
