package org.jfree.data.category;
import org.jfree.data.UnknownKeyException;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.util.ParamChecks;
import java.util.ResourceBundle;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.DataUtilities;
import org.jfree.data.general.AbstractSeriesDataset;
public class DefaultIntervalCategoryDataset extends AbstractSeriesDataset implements IntervalCategoryDataset {
    private Comparable[] seriesKeys;
    private Comparable[] categoryKeys;
    private Number[][] startData;
    private Number[][] endData;
    public DefaultIntervalCategoryDataset ( final double[][] starts, final double[][] ends ) {
        this ( DataUtilities.createNumberArray2D ( starts ), DataUtilities.createNumberArray2D ( ends ) );
    }
    public DefaultIntervalCategoryDataset ( final Number[][] starts, final Number[][] ends ) {
        this ( null, null, starts, ends );
    }
    public DefaultIntervalCategoryDataset ( final String[] seriesNames, final Number[][] starts, final Number[][] ends ) {
        this ( seriesNames, null, starts, ends );
    }
    public DefaultIntervalCategoryDataset ( final Comparable[] seriesKeys, final Comparable[] categoryKeys, final Number[][] starts, final Number[][] ends ) {
        this.startData = starts;
        this.endData = ends;
        if ( starts != null && ends != null ) {
            final String baseName = "org.jfree.data.resources.DataPackageResources";
            final ResourceBundle resources = ResourceBundleWrapper.getBundle ( baseName );
            final int seriesCount = starts.length;
            if ( seriesCount != ends.length ) {
                final String errMsg = "DefaultIntervalCategoryDataset: the number of series in the start value dataset does not match the number of series in the end value dataset.";
                throw new IllegalArgumentException ( errMsg );
            }
            if ( seriesCount > 0 ) {
                if ( seriesKeys != null ) {
                    if ( seriesKeys.length != seriesCount ) {
                        throw new IllegalArgumentException ( "The number of series keys does not match the number of series in the data." );
                    }
                    this.seriesKeys = seriesKeys;
                } else {
                    final String prefix = resources.getString ( "series.default-prefix" ) + " ";
                    this.seriesKeys = this.generateKeys ( seriesCount, prefix );
                }
                final int categoryCount = starts[0].length;
                if ( categoryCount != ends[0].length ) {
                    final String errMsg2 = "DefaultIntervalCategoryDataset: the number of categories in the start value dataset does not match the number of categories in the end value dataset.";
                    throw new IllegalArgumentException ( errMsg2 );
                }
                if ( categoryKeys != null ) {
                    if ( categoryKeys.length != categoryCount ) {
                        throw new IllegalArgumentException ( "The number of category keys does not match the number of categories in the data." );
                    }
                    this.categoryKeys = categoryKeys;
                } else {
                    final String prefix2 = resources.getString ( "categories.default-prefix" ) + " ";
                    this.categoryKeys = this.generateKeys ( categoryCount, prefix2 );
                }
            } else {
                this.seriesKeys = new Comparable[0];
                this.categoryKeys = new Comparable[0];
            }
        }
    }
    @Override
    public int getSeriesCount() {
        int result = 0;
        if ( this.startData != null ) {
            result = this.startData.length;
        }
        return result;
    }
    public int getSeriesIndex ( final Comparable seriesKey ) {
        int result = -1;
        for ( int i = 0; i < this.seriesKeys.length; ++i ) {
            if ( seriesKey.equals ( this.seriesKeys[i] ) ) {
                result = i;
                break;
            }
        }
        return result;
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        if ( series >= this.getSeriesCount() || series < 0 ) {
            throw new IllegalArgumentException ( "No such series : " + series );
        }
        return this.seriesKeys[series];
    }
    public void setSeriesKeys ( final Comparable[] seriesKeys ) {
        ParamChecks.nullNotPermitted ( seriesKeys, "seriesKeys" );
        if ( seriesKeys.length != this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "The number of series keys does not match the data." );
        }
        this.seriesKeys = seriesKeys;
        this.fireDatasetChanged();
    }
    public int getCategoryCount() {
        int result = 0;
        if ( this.startData != null && this.getSeriesCount() > 0 ) {
            result = this.startData[0].length;
        }
        return result;
    }
    @Override
    public List getColumnKeys() {
        if ( this.categoryKeys == null ) {
            return new ArrayList();
        }
        return Collections.unmodifiableList ( ( List<?> ) Arrays.asList ( ( T[] ) this.categoryKeys ) );
    }
    public void setCategoryKeys ( final Comparable[] categoryKeys ) {
        ParamChecks.nullNotPermitted ( categoryKeys, "categoryKeys" );
        if ( categoryKeys.length != this.getCategoryCount() ) {
            throw new IllegalArgumentException ( "The number of categories does not match the data." );
        }
        for ( int i = 0; i < categoryKeys.length; ++i ) {
            if ( categoryKeys[i] == null ) {
                throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.setCategoryKeys(): null category not permitted." );
            }
        }
        this.categoryKeys = categoryKeys;
        this.fireDatasetChanged();
    }
    @Override
    public Number getValue ( final Comparable series, final Comparable category ) {
        final int seriesIndex = this.getSeriesIndex ( series );
        if ( seriesIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'series' key." );
        }
        final int itemIndex = this.getColumnIndex ( category );
        if ( itemIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'category' key." );
        }
        return this.getValue ( seriesIndex, itemIndex );
    }
    @Override
    public Number getValue ( final int series, final int category ) {
        return this.getEndValue ( series, category );
    }
    @Override
    public Number getStartValue ( final Comparable series, final Comparable category ) {
        final int seriesIndex = this.getSeriesIndex ( series );
        if ( seriesIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'series' key." );
        }
        final int itemIndex = this.getColumnIndex ( category );
        if ( itemIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'category' key." );
        }
        return this.getStartValue ( seriesIndex, itemIndex );
    }
    @Override
    public Number getStartValue ( final int series, final int category ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.getValue(): series index out of range." );
        }
        if ( category < 0 || category >= this.getCategoryCount() ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.getValue(): category index out of range." );
        }
        return this.startData[series][category];
    }
    @Override
    public Number getEndValue ( final Comparable series, final Comparable category ) {
        final int seriesIndex = this.getSeriesIndex ( series );
        if ( seriesIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'series' key." );
        }
        final int itemIndex = this.getColumnIndex ( category );
        if ( itemIndex < 0 ) {
            throw new UnknownKeyException ( "Unknown 'category' key." );
        }
        return this.getEndValue ( seriesIndex, itemIndex );
    }
    @Override
    public Number getEndValue ( final int series, final int category ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.getValue(): series index out of range." );
        }
        if ( category < 0 || category >= this.getCategoryCount() ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.getValue(): category index out of range." );
        }
        return this.endData[series][category];
    }
    public void setStartValue ( final int series, final Comparable category, final Number value ) {
        if ( series < 0 || series > this.getSeriesCount() - 1 ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.setValue: series outside valid range." );
        }
        final int categoryIndex = this.getCategoryIndex ( category );
        if ( categoryIndex < 0 ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.setValue: unrecognised category." );
        }
        this.startData[series][categoryIndex] = value;
        this.fireDatasetChanged();
    }
    public void setEndValue ( final int series, final Comparable category, final Number value ) {
        if ( series < 0 || series > this.getSeriesCount() - 1 ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.setValue: series outside valid range." );
        }
        final int categoryIndex = this.getCategoryIndex ( category );
        if ( categoryIndex < 0 ) {
            throw new IllegalArgumentException ( "DefaultIntervalCategoryDataset.setValue: unrecognised category." );
        }
        this.endData[series][categoryIndex] = value;
        this.fireDatasetChanged();
    }
    public int getCategoryIndex ( final Comparable category ) {
        int result = -1;
        for ( int i = 0; i < this.categoryKeys.length; ++i ) {
            if ( category.equals ( this.categoryKeys[i] ) ) {
                result = i;
                break;
            }
        }
        return result;
    }
    private Comparable[] generateKeys ( final int count, final String prefix ) {
        final Comparable[] result = new Comparable[count];
        for ( int i = 0; i < count; ++i ) {
            final String name = prefix + ( i + 1 );
            result[i] = name;
        }
        return result;
    }
    @Override
    public Comparable getColumnKey ( final int column ) {
        return this.categoryKeys[column];
    }
    @Override
    public int getColumnIndex ( final Comparable columnKey ) {
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        return this.getCategoryIndex ( columnKey );
    }
    @Override
    public int getRowIndex ( final Comparable rowKey ) {
        return this.getSeriesIndex ( rowKey );
    }
    @Override
    public List getRowKeys() {
        if ( this.seriesKeys == null ) {
            return new ArrayList();
        }
        return Collections.unmodifiableList ( ( List<?> ) Arrays.asList ( ( T[] ) this.seriesKeys ) );
    }
    @Override
    public Comparable getRowKey ( final int row ) {
        if ( row >= this.getRowCount() || row < 0 ) {
            throw new IllegalArgumentException ( "The 'row' argument is out of bounds." );
        }
        return this.seriesKeys[row];
    }
    @Override
    public int getColumnCount() {
        return this.categoryKeys.length;
    }
    @Override
    public int getRowCount() {
        return this.seriesKeys.length;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultIntervalCategoryDataset ) ) {
            return false;
        }
        final DefaultIntervalCategoryDataset that = ( DefaultIntervalCategoryDataset ) obj;
        return Arrays.equals ( this.seriesKeys, that.seriesKeys ) && Arrays.equals ( this.categoryKeys, that.categoryKeys ) && equal ( this.startData, that.startData ) && equal ( this.endData, that.endData );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultIntervalCategoryDataset clone = ( DefaultIntervalCategoryDataset ) super.clone();
        clone.categoryKeys = this.categoryKeys.clone();
        clone.seriesKeys = this.seriesKeys.clone();
        clone.startData = clone ( this.startData );
        clone.endData = clone ( this.endData );
        return clone;
    }
    private static boolean equal ( final Number[][] array1, final Number[][] array2 ) {
        if ( array1 == null ) {
            return array2 == null;
        }
        if ( array2 == null ) {
            return false;
        }
        if ( array1.length != array2.length ) {
            return false;
        }
        for ( int i = 0; i < array1.length; ++i ) {
            if ( !Arrays.equals ( array1[i], array2[i] ) ) {
                return false;
            }
        }
        return true;
    }
    private static Number[][] clone ( final Number[][] array ) {
        ParamChecks.nullNotPermitted ( array, "array" );
        final Number[][] result = new Number[array.length][];
        for ( int i = 0; i < array.length; ++i ) {
            final Number[] child = array[i];
            final Number[] copychild = new Number[child.length];
            System.arraycopy ( child, 0, copychild, 0, child.length );
            result[i] = copychild;
        }
        return result;
    }
    public List getSeries() {
        if ( this.seriesKeys == null ) {
            return new ArrayList();
        }
        return Collections.unmodifiableList ( ( List<?> ) Arrays.asList ( ( T[] ) this.seriesKeys ) );
    }
    public List getCategories() {
        return this.getColumnKeys();
    }
    public int getItemCount() {
        return this.categoryKeys.length;
    }
}
