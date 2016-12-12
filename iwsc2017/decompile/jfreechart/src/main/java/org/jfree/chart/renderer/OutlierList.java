package org.jfree.chart.renderer;
import java.util.Iterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
public class OutlierList {
    private List outliers;
    private Outlier averagedOutlier;
    private boolean multiple;
    public OutlierList ( final Outlier outlier ) {
        this.multiple = false;
        this.outliers = new ArrayList();
        this.setAveragedOutlier ( outlier );
    }
    public boolean add ( final Outlier outlier ) {
        return this.outliers.add ( outlier );
    }
    public int getItemCount() {
        return this.outliers.size();
    }
    public Outlier getAveragedOutlier() {
        return this.averagedOutlier;
    }
    public void setAveragedOutlier ( final Outlier averagedOutlier ) {
        this.averagedOutlier = averagedOutlier;
    }
    public boolean isMultiple() {
        return this.multiple;
    }
    public void setMultiple ( final boolean multiple ) {
        this.multiple = multiple;
    }
    public boolean isOverlapped ( final Outlier other ) {
        if ( other == null ) {
            return false;
        }
        final boolean result = other.overlaps ( this.getAveragedOutlier() );
        return result;
    }
    public void updateAveragedOutlier() {
        double totalXCoords = 0.0;
        double totalYCoords = 0.0;
        final int size = this.getItemCount();
        for ( final Outlier o : this.outliers ) {
            totalXCoords += o.getX();
            totalYCoords += o.getY();
        }
        this.getAveragedOutlier().getPoint().setLocation ( new Point2D.Double ( totalXCoords / size, totalYCoords / size ) );
    }
}
