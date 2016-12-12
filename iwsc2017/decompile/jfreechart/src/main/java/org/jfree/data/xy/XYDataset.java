package org.jfree.data.xy;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.SeriesDataset;
public interface XYDataset extends SeriesDataset {
    DomainOrder getDomainOrder();
    int getItemCount ( int p0 );
    Number getX ( int p0, int p1 );
    double getXValue ( int p0, int p1 );
    Number getY ( int p0, int p1 );
    double getYValue ( int p0, int p1 );
}
