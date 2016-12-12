package org.jfree.data.statistics;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.Range;
import java.util.List;
import org.jfree.data.KeyedObjects2D;
import org.jfree.util.PublicCloneable;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;
public class DefaultBoxAndWhiskerCategoryDataset extends AbstractDataset implements BoxAndWhiskerCategoryDataset, RangeInfo, PublicCloneable {
    protected KeyedObjects2D data;
    private double minimumRangeValue;
    private int minimumRangeValueRow;
    private int minimumRangeValueColumn;
    private double maximumRangeValue;
    private int maximumRangeValueRow;
    private int maximumRangeValueColumn;
    public DefaultBoxAndWhiskerCategoryDataset() {
        this.data = new KeyedObjects2D();
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
    }
    public void add ( final List list, final Comparable rowKey, final Comparable columnKey ) {
        final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics ( list );
        this.add ( item, rowKey, columnKey );
    }
    public void add ( final BoxAndWhiskerItem item, final Comparable rowKey, final Comparable columnKey ) {
        this.data.addObject ( item, rowKey, columnKey );
        final int r = this.data.getRowIndex ( rowKey );
        final int c = this.data.getColumnIndex ( columnKey );
        if ( ( this.maximumRangeValueRow == r && this.maximumRangeValueColumn == c ) || ( this.minimumRangeValueRow == r && this.minimumRangeValueColumn == c ) ) {
            this.updateBounds();
        } else {
            double minval = Double.NaN;
            if ( item.getMinOutlier() != null ) {
                minval = item.getMinOutlier().doubleValue();
            }
            double maxval = Double.NaN;
            if ( item.getMaxOutlier() != null ) {
                maxval = item.getMaxOutlier().doubleValue();
            }
            if ( Double.isNaN ( this.maximumRangeValue ) ) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            } else if ( maxval > this.maximumRangeValue ) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
            if ( Double.isNaN ( this.minimumRangeValue ) ) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            } else if ( minval < this.minimumRangeValue ) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
        }
        this.fireDatasetChanged();
    }
    public void remove ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        this.data.removeObject ( rowKey, columnKey );
        if ( ( this.maximumRangeValueRow == r && this.maximumRangeValueColumn == c ) || ( this.minimumRangeValueRow == r && this.minimumRangeValueColumn == c ) ) {
            this.updateBounds();
        }
        this.fireDatasetChanged();
    }
    public void removeRow ( final int rowIndex ) {
        this.data.removeRow ( rowIndex );
        this.updateBounds();
        this.fireDatasetChanged();
    }
    public void removeRow ( final Comparable rowKey ) {
        this.data.removeRow ( rowKey );
        this.updateBounds();
        this.fireDatasetChanged();
    }
    public void removeColumn ( final int columnIndex ) {
        this.data.removeColumn ( columnIndex );
        this.updateBounds();
        this.fireDatasetChanged();
    }
    public void removeColumn ( final Comparable columnKey ) {
        this.data.removeColumn ( columnKey );
        this.updateBounds();
        this.fireDatasetChanged();
    }
    public void clear() {
        this.data.clear();
        this.updateBounds();
        this.fireDatasetChanged();
    }
    public BoxAndWhiskerItem getItem ( final int row, final int column ) {
        return ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
    }
    public Number getValue ( final int row, final int column ) {
        return this.getMedianValue ( row, column );
    }
    public Number getValue ( final Comparable rowKey, final Comparable columnKey ) {
        return this.getMedianValue ( rowKey, columnKey );
    }
    @Override
    public Number getMeanValue ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMean();
        }
        return result;
    }
    @Override
    public Number getMeanValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMean();
        }
        return result;
    }
    @Override
    public Number getMedianValue ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMedian();
        }
        return result;
    }
    @Override
    public Number getMedianValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMedian();
        }
        return result;
    }
    @Override
    public Number getQ1Value ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getQ1();
        }
        return result;
    }
    @Override
    public Number getQ1Value ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getQ1();
        }
        return result;
    }
    @Override
    public Number getQ3Value ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getQ3();
        }
        return result;
    }
    @Override
    public Number getQ3Value ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getQ3();
        }
        return result;
    }
    public int getColumnIndex ( final Comparable key ) {
        return this.data.getColumnIndex ( key );
    }
    public Comparable getColumnKey ( final int column ) {
        return this.data.getColumnKey ( column );
    }
    public List getColumnKeys() {
        return this.data.getColumnKeys();
    }
    public int getRowIndex ( final Comparable key ) {
        return this.data.getRowIndex ( key );
    }
    public Comparable getRowKey ( final int row ) {
        return this.data.getRowKey ( row );
    }
    public List getRowKeys() {
        return this.data.getRowKeys();
    }
    public int getRowCount() {
        return this.data.getRowCount();
    }
    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    @Override
    public double getRangeLowerBound ( final boolean includeInterval ) {
        return this.minimumRangeValue;
    }
    @Override
    public double getRangeUpperBound ( final boolean includeInterval ) {
        return this.maximumRangeValue;
    }
    @Override
    public Range getRangeBounds ( final boolean includeInterval ) {
        return new Range ( this.minimumRangeValue, this.maximumRangeValue );
    }
    @Override
    public Number getMinRegularValue ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMinRegularValue();
        }
        return result;
    }
    @Override
    public Number getMinRegularValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMinRegularValue();
        }
        return result;
    }
    @Override
    public Number getMaxRegularValue ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMaxRegularValue();
        }
        return result;
    }
    @Override
    public Number getMaxRegularValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMaxRegularValue();
        }
        return result;
    }
    @Override
    public Number getMinOutlier ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMinOutlier();
        }
        return result;
    }
    @Override
    public Number getMinOutlier ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMinOutlier();
        }
        return result;
    }
    @Override
    public Number getMaxOutlier ( final int row, final int column ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getMaxOutlier();
        }
        return result;
    }
    @Override
    public Number getMaxOutlier ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getMaxOutlier();
        }
        return result;
    }
    @Override
    public List getOutliers ( final int row, final int column ) {
        List result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( row, column );
        if ( item != null ) {
            result = item.getOutliers();
        }
        return result;
    }
    @Override
    public List getOutliers ( final Comparable rowKey, final Comparable columnKey ) {
        List result = null;
        final BoxAndWhiskerItem item = ( BoxAndWhiskerItem ) this.data.getObject ( rowKey, columnKey );
        if ( item != null ) {
            result = item.getOutliers();
        }
        return result;
    }
    private void updateBounds() {
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
        final int rowCount = this.getRowCount();
        final int columnCount = this.getColumnCount();
        for ( int r = 0; r < rowCount; ++r ) {
            for ( int c = 0; c < columnCount; ++c ) {
                final BoxAndWhiskerItem item = this.getItem ( r, c );
                if ( item != null ) {
                    final Number min = item.getMinOutlier();
                    if ( min != null ) {
                        final double minv = min.doubleValue();
                        if ( !Double.isNaN ( minv ) && ( minv < this.minimumRangeValue || Double.isNaN ( this.minimumRangeValue ) ) ) {
                            this.minimumRangeValue = minv;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        }
                    }
                    final Number max = item.getMaxOutlier();
                    if ( max != null ) {
                        final double maxv = max.doubleValue();
                        if ( !Double.isNaN ( maxv ) && ( maxv > this.maximumRangeValue || Double.isNaN ( this.maximumRangeValue ) ) ) {
                            this.maximumRangeValue = maxv;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        }
                    }
                }
            }
        }
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof DefaultBoxAndWhiskerCategoryDataset ) {
            final DefaultBoxAndWhiskerCategoryDataset dataset = ( DefaultBoxAndWhiskerCategoryDataset ) obj;
            return ObjectUtilities.equal ( ( Object ) this.data, ( Object ) dataset.data );
        }
        return false;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultBoxAndWhiskerCategoryDataset clone = ( DefaultBoxAndWhiskerCategoryDataset ) super.clone();
        clone.data = ( KeyedObjects2D ) this.data.clone();
        return clone;
    }
}
