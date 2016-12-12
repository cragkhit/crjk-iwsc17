package org.jfree.chart.plot;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.renderer.RendererState;
public class PiePlotState extends RendererState {
    private int passesRequired;
    private double total;
    private double latestAngle;
    private Rectangle2D explodedPieArea;
    private Rectangle2D pieArea;
    private double pieCenterX;
    private double pieCenterY;
    private double pieHRadius;
    private double pieWRadius;
    private Rectangle2D linkArea;
    public PiePlotState ( final PlotRenderingInfo info ) {
        super ( info );
        this.passesRequired = 1;
        this.total = 0.0;
    }
    public int getPassesRequired() {
        return this.passesRequired;
    }
    public void setPassesRequired ( final int passes ) {
        this.passesRequired = passes;
    }
    public double getTotal() {
        return this.total;
    }
    public void setTotal ( final double total ) {
        this.total = total;
    }
    public double getLatestAngle() {
        return this.latestAngle;
    }
    public void setLatestAngle ( final double angle ) {
        this.latestAngle = angle;
    }
    public Rectangle2D getPieArea() {
        return this.pieArea;
    }
    public void setPieArea ( final Rectangle2D area ) {
        this.pieArea = area;
    }
    public Rectangle2D getExplodedPieArea() {
        return this.explodedPieArea;
    }
    public void setExplodedPieArea ( final Rectangle2D area ) {
        this.explodedPieArea = area;
    }
    public double getPieCenterX() {
        return this.pieCenterX;
    }
    public void setPieCenterX ( final double x ) {
        this.pieCenterX = x;
    }
    public double getPieCenterY() {
        return this.pieCenterY;
    }
    public void setPieCenterY ( final double y ) {
        this.pieCenterY = y;
    }
    public Rectangle2D getLinkArea() {
        return this.linkArea;
    }
    public void setLinkArea ( final Rectangle2D area ) {
        this.linkArea = area;
    }
    public double getPieHRadius() {
        return this.pieHRadius;
    }
    public void setPieHRadius ( final double radius ) {
        this.pieHRadius = radius;
    }
    public double getPieWRadius() {
        return this.pieWRadius;
    }
    public void setPieWRadius ( final double radius ) {
        this.pieWRadius = radius;
    }
}
