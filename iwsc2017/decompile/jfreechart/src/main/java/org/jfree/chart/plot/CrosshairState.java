package org.jfree.chart.plot;
import java.awt.geom.Point2D;
public class CrosshairState {
    private boolean calculateDistanceInDataSpace;
    private double anchorX;
    private double anchorY;
    private Point2D anchor;
    private double crosshairX;
    private double crosshairY;
    private int datasetIndex;
    private int domainAxisIndex;
    private int rangeAxisIndex;
    private double distance;
    public CrosshairState() {
        this ( false );
    }
    public CrosshairState ( final boolean calculateDistanceInDataSpace ) {
        this.calculateDistanceInDataSpace = false;
        this.calculateDistanceInDataSpace = calculateDistanceInDataSpace;
    }
    public double getCrosshairDistance() {
        return this.distance;
    }
    public void setCrosshairDistance ( final double distance ) {
        this.distance = distance;
    }
    public void updateCrosshairPoint ( final double x, final double y, final double transX, final double transY, final PlotOrientation orientation ) {
        this.updateCrosshairPoint ( x, y, 0, 0, transX, transY, orientation );
    }
    public void updateCrosshairPoint ( final double x, final double y, final int domainAxisIndex, final int rangeAxisIndex, final double transX, final double transY, final PlotOrientation orientation ) {
        if ( this.anchor != null ) {
            double d = 0.0;
            if ( this.calculateDistanceInDataSpace ) {
                d = ( x - this.anchorX ) * ( x - this.anchorX ) + ( y - this.anchorY ) * ( y - this.anchorY );
            } else {
                double xx = this.anchor.getX();
                double yy = this.anchor.getY();
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    final double temp = yy;
                    yy = xx;
                    xx = temp;
                }
                d = ( transX - xx ) * ( transX - xx ) + ( transY - yy ) * ( transY - yy );
            }
            if ( d < this.distance ) {
                this.crosshairX = x;
                this.crosshairY = y;
                this.domainAxisIndex = domainAxisIndex;
                this.rangeAxisIndex = rangeAxisIndex;
                this.distance = d;
            }
        }
    }
    public void updateCrosshairX ( final double candidateX ) {
        this.updateCrosshairX ( candidateX, 0 );
    }
    public void updateCrosshairX ( final double candidateX, final int domainAxisIndex ) {
        final double d = Math.abs ( candidateX - this.anchorX );
        if ( d < this.distance ) {
            this.crosshairX = candidateX;
            this.domainAxisIndex = domainAxisIndex;
            this.distance = d;
        }
    }
    public void updateCrosshairY ( final double candidateY ) {
        this.updateCrosshairY ( candidateY, 0 );
    }
    public void updateCrosshairY ( final double candidateY, final int rangeAxisIndex ) {
        final double d = Math.abs ( candidateY - this.anchorY );
        if ( d < this.distance ) {
            this.crosshairY = candidateY;
            this.rangeAxisIndex = rangeAxisIndex;
            this.distance = d;
        }
    }
    public Point2D getAnchor() {
        return this.anchor;
    }
    public void setAnchor ( final Point2D anchor ) {
        this.anchor = anchor;
    }
    public double getAnchorX() {
        return this.anchorX;
    }
    public void setAnchorX ( final double x ) {
        this.anchorX = x;
    }
    public double getAnchorY() {
        return this.anchorY;
    }
    public void setAnchorY ( final double y ) {
        this.anchorY = y;
    }
    public double getCrosshairX() {
        return this.crosshairX;
    }
    public void setCrosshairX ( final double x ) {
        this.crosshairX = x;
    }
    public double getCrosshairY() {
        return this.crosshairY;
    }
    public void setCrosshairY ( final double y ) {
        this.crosshairY = y;
    }
    public int getDatasetIndex() {
        return this.datasetIndex;
    }
    public void setDatasetIndex ( final int index ) {
        this.datasetIndex = index;
    }
    public int getDomainAxisIndex() {
        return this.domainAxisIndex;
    }
    public int getRangeAxisIndex() {
        return this.rangeAxisIndex;
    }
}
