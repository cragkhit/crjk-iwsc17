package org.jfree.data.statistics;
import org.jfree.data.Range;
import java.util.List;
import org.jfree.data.KeyedObjects2D;
import org.jfree.util.PublicCloneable;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;
public class DefaultStatisticalCategoryDataset extends AbstractDataset implements StatisticalCategoryDataset, RangeInfo, PublicCloneable {
    private KeyedObjects2D data;
    private double minimumRangeValue;
    private int minimumRangeValueRow;
    private int minimumRangeValueColumn;
    private double minimumRangeValueIncStdDev;
    private int minimumRangeValueIncStdDevRow;
    private int minimumRangeValueIncStdDevColumn;
    private double maximumRangeValue;
    private int maximumRangeValueRow;
    private int maximumRangeValueColumn;
    private double maximumRangeValueIncStdDev;
    private int maximumRangeValueIncStdDevRow;
    private int maximumRangeValueIncStdDevColumn;
    public DefaultStatisticalCategoryDataset() {
        this.data = new KeyedObjects2D();
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
        this.minimumRangeValueIncStdDev = Double.NaN;
        this.minimumRangeValueIncStdDevRow = -1;
        this.minimumRangeValueIncStdDevColumn = -1;
        this.maximumRangeValueIncStdDev = Double.NaN;
        this.maximumRangeValueIncStdDevRow = -1;
        this.maximumRangeValueIncStdDevColumn = -1;
    }
    @Override
    public Number getMeanValue ( final int row, final int column ) {
        Number result = null;
        final MeanAndStandardDeviation masd = ( MeanAndStandardDeviation ) this.data.getObject ( row, column );
        if ( masd != null ) {
            result = masd.getMean();
        }
        return result;
    }
    public Number getValue ( final int row, final int column ) {
        return this.getMeanValue ( row, column );
    }
    public Number getValue ( final Comparable rowKey, final Comparable columnKey ) {
        return this.getMeanValue ( rowKey, columnKey );
    }
    @Override
    public Number getMeanValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final MeanAndStandardDeviation masd = ( MeanAndStandardDeviation ) this.data.getObject ( rowKey, columnKey );
        if ( masd != null ) {
            result = masd.getMean();
        }
        return result;
    }
    @Override
    public Number getStdDevValue ( final int row, final int column ) {
        Number result = null;
        final MeanAndStandardDeviation masd = ( MeanAndStandardDeviation ) this.data.getObject ( row, column );
        if ( masd != null ) {
            result = masd.getStandardDeviation();
        }
        return result;
    }
    @Override
    public Number getStdDevValue ( final Comparable rowKey, final Comparable columnKey ) {
        Number result = null;
        final MeanAndStandardDeviation masd = ( MeanAndStandardDeviation ) this.data.getObject ( rowKey, columnKey );
        if ( masd != null ) {
            result = masd.getStandardDeviation();
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
    public void add ( final double mean, final double standardDeviation, final Comparable rowKey, final Comparable columnKey ) {
        this.add ( new Double ( mean ), new Double ( standardDeviation ), rowKey, columnKey );
    }
    public void add ( final Number mean, final Number standardDeviation, final Comparable rowKey, final Comparable columnKey ) {
        final MeanAndStandardDeviation item = new MeanAndStandardDeviation ( mean, standardDeviation );
        this.data.addObject ( item, rowKey, columnKey );
        double m = Double.NaN;
        double sd = Double.NaN;
        if ( mean != null ) {
            m = mean.doubleValue();
        }
        if ( standardDeviation != null ) {
            sd = standardDeviation.doubleValue();
        }
        final int r = this.data.getColumnIndex ( columnKey );
        final int c = this.data.getRowIndex ( rowKey );
        if ( ( r == this.maximumRangeValueRow && c == this.maximumRangeValueColumn ) || ( r == this.maximumRangeValueIncStdDevRow && c == this.maximumRangeValueIncStdDevColumn ) || ( r == this.minimumRangeValueRow && c == this.minimumRangeValueColumn ) || ( r == this.minimumRangeValueIncStdDevRow && c == this.minimumRangeValueIncStdDevColumn ) ) {
            this.updateBounds();
        } else {
            if ( !Double.isNaN ( m ) && ( Double.isNaN ( this.maximumRangeValue ) || m > this.maximumRangeValue ) ) {
                this.maximumRangeValue = m;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
            if ( !Double.isNaN ( m + sd ) && ( Double.isNaN ( this.maximumRangeValueIncStdDev ) || m + sd > this.maximumRangeValueIncStdDev ) ) {
                this.maximumRangeValueIncStdDev = m + sd;
                this.maximumRangeValueIncStdDevRow = r;
                this.maximumRangeValueIncStdDevColumn = c;
            }
            if ( !Double.isNaN ( m ) && ( Double.isNaN ( this.minimumRangeValue ) || m < this.minimumRangeValue ) ) {
                this.minimumRangeValue = m;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
            if ( !Double.isNaN ( m - sd ) && ( Double.isNaN ( this.minimumRangeValueIncStdDev ) || m - sd < this.minimumRangeValueIncStdDev ) ) {
                this.minimumRangeValueIncStdDev = m - sd;
                this.minimumRangeValueIncStdDevRow = r;
                this.minimumRangeValueIncStdDevColumn = c;
            }
        }
        this.fireDatasetChanged();
    }
    public void remove ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        this.data.removeObject ( rowKey, columnKey );
        if ( ( r == this.maximumRangeValueRow && c == this.maximumRangeValueColumn ) || ( r == this.maximumRangeValueIncStdDevRow && c == this.maximumRangeValueIncStdDevColumn ) || ( r == this.minimumRangeValueRow && c == this.minimumRangeValueColumn ) || ( r == this.minimumRangeValueIncStdDevRow && c == this.minimumRangeValueIncStdDevColumn ) ) {
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
    private void updateBounds() {
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValueIncStdDev = Double.NaN;
        this.maximumRangeValueIncStdDevRow = -1;
        this.maximumRangeValueIncStdDevColumn = -1;
        this.minimumRangeValueIncStdDev = Double.NaN;
        this.minimumRangeValueIncStdDevRow = -1;
        this.minimumRangeValueIncStdDevColumn = -1;
        final int rowCount = this.data.getRowCount();
        final int columnCount = this.data.getColumnCount();
        for ( int r = 0; r < rowCount; ++r ) {
            for ( int c = 0; c < columnCount; ++c ) {
                final MeanAndStandardDeviation masd = ( MeanAndStandardDeviation ) this.data.getObject ( r, c );
                if ( masd != null ) {
                    final double m = masd.getMeanValue();
                    final double sd = masd.getStandardDeviationValue();
                    if ( !Double.isNaN ( m ) ) {
                        if ( Double.isNaN ( this.maximumRangeValue ) ) {
                            this.maximumRangeValue = m;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        } else if ( m > this.maximumRangeValue ) {
                            this.maximumRangeValue = m;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        }
                        if ( Double.isNaN ( this.minimumRangeValue ) ) {
                            this.minimumRangeValue = m;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        } else if ( m < this.minimumRangeValue ) {
                            this.minimumRangeValue = m;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        }
                        if ( !Double.isNaN ( sd ) ) {
                            if ( Double.isNaN ( this.maximumRangeValueIncStdDev ) ) {
                                this.maximumRangeValueIncStdDev = m + sd;
                                this.maximumRangeValueIncStdDevRow = r;
                                this.maximumRangeValueIncStdDevColumn = c;
                            } else if ( m + sd > this.maximumRangeValueIncStdDev ) {
                                this.maximumRangeValueIncStdDev = m + sd;
                                this.maximumRangeValueIncStdDevRow = r;
                                this.maximumRangeValueIncStdDevColumn = c;
                            }
                            if ( Double.isNaN ( this.minimumRangeValueIncStdDev ) ) {
                                this.minimumRangeValueIncStdDev = m - sd;
                                this.minimumRangeValueIncStdDevRow = r;
                                this.minimumRangeValueIncStdDevColumn = c;
                            } else if ( m - sd < this.minimumRangeValueIncStdDev ) {
                                this.minimumRangeValueIncStdDev = m - sd;
                                this.minimumRangeValueIncStdDevRow = r;
                                this.minimumRangeValueIncStdDevColumn = c;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public double getRangeLowerBound ( final boolean includeInterval ) {
        if ( includeInterval && !Double.isNaN ( this.minimumRangeValueIncStdDev ) ) {
            return this.minimumRangeValueIncStdDev;
        }
        return this.minimumRangeValue;
    }
    @Override
    public double getRangeUpperBound ( final boolean includeInterval ) {
        if ( includeInterval && !Double.isNaN ( this.maximumRangeValueIncStdDev ) ) {
            return this.maximumRangeValueIncStdDev;
        }
        return this.maximumRangeValue;
    }
    @Override
    public Range getRangeBounds ( final boolean includeInterval ) {
        final double lower = this.getRangeLowerBound ( includeInterval );
        final double upper = this.getRangeUpperBound ( includeInterval );
        if ( Double.isNaN ( lower ) && Double.isNaN ( upper ) ) {
            return null;
        }
        return new Range ( lower, upper );
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultStatisticalCategoryDataset ) ) {
            return false;
        }
        final DefaultStatisticalCategoryDataset that = ( DefaultStatisticalCategoryDataset ) obj;
        return this.data.equals ( that.data );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultStatisticalCategoryDataset clone = ( DefaultStatisticalCategoryDataset ) super.clone();
        clone.data = ( KeyedObjects2D ) this.data.clone();
        return clone;
    }
}
