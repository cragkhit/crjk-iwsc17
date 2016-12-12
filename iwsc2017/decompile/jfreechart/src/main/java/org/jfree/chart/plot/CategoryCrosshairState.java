package org.jfree.chart.plot;
import java.awt.geom.Point2D;
public class CategoryCrosshairState extends CrosshairState {
    private Comparable rowKey;
    private Comparable columnKey;
    public CategoryCrosshairState() {
        this.rowKey = null;
        this.columnKey = null;
    }
    public Comparable getRowKey() {
        return this.rowKey;
    }
    public void setRowKey ( final Comparable key ) {
        this.rowKey = key;
    }
    public Comparable getColumnKey() {
        return this.columnKey;
    }
    public void setColumnKey ( final Comparable key ) {
        this.columnKey = key;
    }
    public void updateCrosshairPoint ( final Comparable rowKey, final Comparable columnKey, final double value, final int datasetIndex, final double transX, final double transY, final PlotOrientation orientation ) {
        final Point2D anchor = this.getAnchor();
        if ( anchor != null ) {
            double xx = anchor.getX();
            double yy = anchor.getY();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                final double temp = yy;
                yy = xx;
                xx = temp;
            }
            final double d = ( transX - xx ) * ( transX - xx ) + ( transY - yy ) * ( transY - yy );
            if ( d < this.getCrosshairDistance() ) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                this.setCrosshairY ( value );
                this.setDatasetIndex ( datasetIndex );
                this.setCrosshairDistance ( d );
            }
        }
    }
    public void updateCrosshairX ( final Comparable rowKey, final Comparable columnKey, final int datasetIndex, final double transX, final PlotOrientation orientation ) {
        final Point2D anchor = this.getAnchor();
        if ( anchor != null ) {
            double anchorX = anchor.getX();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                anchorX = anchor.getY();
            }
            final double d = Math.abs ( transX - anchorX );
            if ( d < this.getCrosshairDistance() ) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                this.setDatasetIndex ( datasetIndex );
                this.setCrosshairDistance ( d );
            }
        }
    }
}
