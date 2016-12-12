package org.jfree.data.general;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.xy.XYRangeInfo;
import org.jfree.data.category.CategoryRangeInfo;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.KeyedValues;
import org.jfree.util.ArrayUtilities;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.ArrayList;
import org.jfree.data.category.CategoryDataset;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.util.ParamChecks;
public final class DatasetUtilities {
    public static double calculatePieDatasetTotal ( final PieDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final List keys = dataset.getKeys();
        double totalValue = 0.0;
        for ( final Comparable current : keys ) {
            if ( current != null ) {
                final Number value = dataset.getValue ( current );
                double v = 0.0;
                if ( value != null ) {
                    v = value.doubleValue();
                }
                if ( v <= 0.0 ) {
                    continue;
                }
                totalValue += v;
            }
        }
        return totalValue;
    }
    public static PieDataset createPieDatasetForRow ( final CategoryDataset dataset, final Comparable rowKey ) {
        final int row = dataset.getRowIndex ( rowKey );
        return createPieDatasetForRow ( dataset, row );
    }
    public static PieDataset createPieDatasetForRow ( final CategoryDataset dataset, final int row ) {
        final DefaultPieDataset result = new DefaultPieDataset();
        for ( int columnCount = dataset.getColumnCount(), current = 0; current < columnCount; ++current ) {
            final Comparable columnKey = dataset.getColumnKey ( current );
            result.setValue ( columnKey, dataset.getValue ( row, current ) );
        }
        return result;
    }
    public static PieDataset createPieDatasetForColumn ( final CategoryDataset dataset, final Comparable columnKey ) {
        final int column = dataset.getColumnIndex ( columnKey );
        return createPieDatasetForColumn ( dataset, column );
    }
    public static PieDataset createPieDatasetForColumn ( final CategoryDataset dataset, final int column ) {
        final DefaultPieDataset result = new DefaultPieDataset();
        for ( int rowCount = dataset.getRowCount(), i = 0; i < rowCount; ++i ) {
            final Comparable rowKey = dataset.getRowKey ( i );
            result.setValue ( rowKey, dataset.getValue ( i, column ) );
        }
        return result;
    }
    public static PieDataset createConsolidatedPieDataset ( final PieDataset source, final Comparable key, final double minimumPercent ) {
        return createConsolidatedPieDataset ( source, key, minimumPercent, 2 );
    }
    public static PieDataset createConsolidatedPieDataset ( final PieDataset source, final Comparable key, final double minimumPercent, final int minItems ) {
        final DefaultPieDataset result = new DefaultPieDataset();
        final double total = calculatePieDatasetTotal ( source );
        final List keys = source.getKeys();
        final ArrayList otherKeys = new ArrayList();
        for ( final Comparable currentKey : keys ) {
            final Number dataValue = source.getValue ( currentKey );
            if ( dataValue != null ) {
                final double value = dataValue.doubleValue();
                if ( value / total >= minimumPercent ) {
                    continue;
                }
                otherKeys.add ( currentKey );
            }
        }
        final Iterator iterator = keys.iterator();
        double otherValue = 0.0;
        while ( iterator.hasNext() ) {
            final Comparable currentKey2 = iterator.next();
            final Number dataValue2 = source.getValue ( currentKey2 );
            if ( dataValue2 != null ) {
                if ( otherKeys.contains ( currentKey2 ) && otherKeys.size() >= minItems ) {
                    otherValue += dataValue2.doubleValue();
                } else {
                    result.setValue ( currentKey2, dataValue2 );
                }
            }
        }
        if ( otherKeys.size() >= minItems ) {
            result.setValue ( key, otherValue );
        }
        return result;
    }
    public static CategoryDataset createCategoryDataset ( final String rowKeyPrefix, final String columnKeyPrefix, final double[][] data ) {
        final DefaultCategoryDataset result = new DefaultCategoryDataset();
        for ( int r = 0; r < data.length; ++r ) {
            final String rowKey = rowKeyPrefix + ( r + 1 );
            for ( int c = 0; c < data[r].length; ++c ) {
                final String columnKey = columnKeyPrefix + ( c + 1 );
                result.addValue ( new Double ( data[r][c] ), rowKey, columnKey );
            }
        }
        return result;
    }
    public static CategoryDataset createCategoryDataset ( final String rowKeyPrefix, final String columnKeyPrefix, final Number[][] data ) {
        final DefaultCategoryDataset result = new DefaultCategoryDataset();
        for ( int r = 0; r < data.length; ++r ) {
            final String rowKey = rowKeyPrefix + ( r + 1 );
            for ( int c = 0; c < data[r].length; ++c ) {
                final String columnKey = columnKeyPrefix + ( c + 1 );
                result.addValue ( data[r][c], rowKey, columnKey );
            }
        }
        return result;
    }
    public static CategoryDataset createCategoryDataset ( final Comparable[] rowKeys, final Comparable[] columnKeys, final double[][] data ) {
        ParamChecks.nullNotPermitted ( rowKeys, "rowKeys" );
        ParamChecks.nullNotPermitted ( columnKeys, "columnKeys" );
        if ( ArrayUtilities.hasDuplicateItems ( ( Object[] ) rowKeys ) ) {
            throw new IllegalArgumentException ( "Duplicate items in 'rowKeys'." );
        }
        if ( ArrayUtilities.hasDuplicateItems ( ( Object[] ) columnKeys ) ) {
            throw new IllegalArgumentException ( "Duplicate items in 'columnKeys'." );
        }
        if ( rowKeys.length != data.length ) {
            throw new IllegalArgumentException ( "The number of row keys does not match the number of rows in the data array." );
        }
        int columnCount = 0;
        for ( int r = 0; r < data.length; ++r ) {
            columnCount = Math.max ( columnCount, data[r].length );
        }
        if ( columnKeys.length != columnCount ) {
            throw new IllegalArgumentException ( "The number of column keys does not match the number of columns in the data array." );
        }
        final DefaultCategoryDataset result = new DefaultCategoryDataset();
        for ( int r2 = 0; r2 < data.length; ++r2 ) {
            final Comparable rowKey = rowKeys[r2];
            for ( int c = 0; c < data[r2].length; ++c ) {
                final Comparable columnKey = columnKeys[c];
                result.addValue ( new Double ( data[r2][c] ), rowKey, columnKey );
            }
        }
        return result;
    }
    public static CategoryDataset createCategoryDataset ( final Comparable rowKey, final KeyedValues rowData ) {
        ParamChecks.nullNotPermitted ( rowKey, "rowKey" );
        ParamChecks.nullNotPermitted ( rowData, "rowData" );
        final DefaultCategoryDataset result = new DefaultCategoryDataset();
        for ( int i = 0; i < rowData.getItemCount(); ++i ) {
            result.addValue ( rowData.getValue ( i ), rowKey, rowData.getKey ( i ) );
        }
        return result;
    }
    public static XYDataset sampleFunction2D ( final Function2D f, final double start, final double end, final int samples, final Comparable seriesKey ) {
        final XYSeries series = sampleFunction2DToSeries ( f, start, end, samples, seriesKey );
        final XYSeriesCollection collection = new XYSeriesCollection ( series );
        return collection;
    }
    public static XYSeries sampleFunction2DToSeries ( final Function2D f, final double start, final double end, final int samples, final Comparable seriesKey ) {
        ParamChecks.nullNotPermitted ( f, "f" );
        ParamChecks.nullNotPermitted ( seriesKey, "seriesKey" );
        if ( start >= end ) {
            throw new IllegalArgumentException ( "Requires 'start' < 'end'." );
        }
        if ( samples < 2 ) {
            throw new IllegalArgumentException ( "Requires 'samples' > 1" );
        }
        final XYSeries series = new XYSeries ( seriesKey );
        final double step = ( end - start ) / ( samples - 1 );
        for ( int i = 0; i < samples; ++i ) {
            final double x = start + step * i;
            series.add ( x, f.getValue ( x ) );
        }
        return series;
    }
    public static boolean isEmptyOrNull ( final PieDataset dataset ) {
        if ( dataset == null ) {
            return true;
        }
        final int itemCount = dataset.getItemCount();
        if ( itemCount == 0 ) {
            return true;
        }
        for ( int item = 0; item < itemCount; ++item ) {
            final Number y = dataset.getValue ( item );
            if ( y != null ) {
                final double yy = y.doubleValue();
                if ( yy > 0.0 ) {
                    return false;
                }
            }
        }
        return true;
    }
    public static boolean isEmptyOrNull ( final CategoryDataset dataset ) {
        if ( dataset == null ) {
            return true;
        }
        final int rowCount = dataset.getRowCount();
        final int columnCount = dataset.getColumnCount();
        if ( rowCount == 0 || columnCount == 0 ) {
            return true;
        }
        for ( int r = 0; r < rowCount; ++r ) {
            for ( int c = 0; c < columnCount; ++c ) {
                if ( dataset.getValue ( r, c ) != null ) {
                    return false;
                }
            }
        }
        return true;
    }
    public static boolean isEmptyOrNull ( final XYDataset dataset ) {
        if ( dataset != null ) {
            for ( int s = 0; s < dataset.getSeriesCount(); ++s ) {
                if ( dataset.getItemCount ( s ) > 0 ) {
                    return false;
                }
            }
        }
        return true;
    }
    public static Range findDomainBounds ( final XYDataset dataset ) {
        return findDomainBounds ( dataset, true );
    }
    public static Range findDomainBounds ( final XYDataset dataset, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof DomainInfo ) {
            final DomainInfo info = ( DomainInfo ) dataset;
            result = info.getDomainBounds ( includeInterval );
        } else {
            result = iterateDomainBounds ( dataset, includeInterval );
        }
        return result;
    }
    public static Range findDomainBounds ( final XYDataset dataset, final List visibleSeriesKeys, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof XYDomainInfo ) {
            final XYDomainInfo info = ( XYDomainInfo ) dataset;
            result = info.getDomainBounds ( visibleSeriesKeys, includeInterval );
        } else {
            result = iterateToFindDomainBounds ( dataset, visibleSeriesKeys, includeInterval );
        }
        return result;
    }
    public static Range iterateDomainBounds ( final XYDataset dataset ) {
        return iterateDomainBounds ( dataset, true );
    }
    public static Range iterateDomainBounds ( final XYDataset dataset, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int seriesCount = dataset.getSeriesCount();
        if ( includeInterval && dataset instanceof IntervalXYDataset ) {
            final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
            for ( int series = 0; series < seriesCount; ++series ) {
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double value = intervalXYData.getXValue ( series, item );
                    final double lvalue = intervalXYData.getStartXValue ( series, item );
                    final double uvalue = intervalXYData.getEndXValue ( series, item );
                    if ( !Double.isNaN ( value ) ) {
                        minimum = Math.min ( minimum, value );
                        maximum = Math.max ( maximum, value );
                    }
                    if ( !Double.isNaN ( lvalue ) ) {
                        minimum = Math.min ( minimum, lvalue );
                        maximum = Math.max ( maximum, lvalue );
                    }
                    if ( !Double.isNaN ( uvalue ) ) {
                        minimum = Math.min ( minimum, uvalue );
                        maximum = Math.max ( maximum, uvalue );
                    }
                }
            }
        } else {
            for ( int series2 = 0; series2 < seriesCount; ++series2 ) {
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double uvalue;
                    final double lvalue = uvalue = dataset.getXValue ( series2, item2 );
                    if ( !Double.isNaN ( lvalue ) ) {
                        minimum = Math.min ( minimum, lvalue );
                        maximum = Math.max ( maximum, uvalue );
                    }
                }
            }
        }
        if ( minimum > maximum ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range findRangeBounds ( final CategoryDataset dataset ) {
        return findRangeBounds ( dataset, true );
    }
    public static Range findRangeBounds ( final CategoryDataset dataset, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            result = info.getRangeBounds ( includeInterval );
        } else {
            result = iterateRangeBounds ( dataset, includeInterval );
        }
        return result;
    }
    public static Range findRangeBounds ( final CategoryDataset dataset, final List visibleSeriesKeys, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof CategoryRangeInfo ) {
            final CategoryRangeInfo info = ( CategoryRangeInfo ) dataset;
            result = info.getRangeBounds ( visibleSeriesKeys, includeInterval );
        } else {
            result = iterateToFindRangeBounds ( dataset, visibleSeriesKeys, includeInterval );
        }
        return result;
    }
    public static Range findRangeBounds ( final XYDataset dataset ) {
        return findRangeBounds ( dataset, true );
    }
    public static Range findRangeBounds ( final XYDataset dataset, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            result = info.getRangeBounds ( includeInterval );
        } else {
            result = iterateRangeBounds ( dataset, includeInterval );
        }
        return result;
    }
    public static Range findRangeBounds ( final XYDataset dataset, final List visibleSeriesKeys, final Range xRange, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result;
        if ( dataset instanceof XYRangeInfo ) {
            final XYRangeInfo info = ( XYRangeInfo ) dataset;
            result = info.getRangeBounds ( visibleSeriesKeys, xRange, includeInterval );
        } else {
            result = iterateToFindRangeBounds ( dataset, visibleSeriesKeys, xRange, includeInterval );
        }
        return result;
    }
    public static Range iterateCategoryRangeBounds ( final CategoryDataset dataset, final boolean includeInterval ) {
        return iterateRangeBounds ( dataset, includeInterval );
    }
    public static Range iterateRangeBounds ( final CategoryDataset dataset ) {
        return iterateRangeBounds ( dataset, true );
    }
    public static Range iterateRangeBounds ( final CategoryDataset dataset, final boolean includeInterval ) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int rowCount = dataset.getRowCount();
        final int columnCount = dataset.getColumnCount();
        if ( includeInterval && dataset instanceof IntervalCategoryDataset ) {
            final IntervalCategoryDataset icd = ( IntervalCategoryDataset ) dataset;
            for ( int row = 0; row < rowCount; ++row ) {
                for ( int column = 0; column < columnCount; ++column ) {
                    final Number value = icd.getValue ( row, column );
                    double v;
                    if ( value != null && !Double.isNaN ( v = value.doubleValue() ) ) {
                        minimum = Math.min ( v, minimum );
                        maximum = Math.max ( v, maximum );
                    }
                    final Number lvalue = icd.getStartValue ( row, column );
                    if ( lvalue != null && !Double.isNaN ( v = lvalue.doubleValue() ) ) {
                        minimum = Math.min ( v, minimum );
                        maximum = Math.max ( v, maximum );
                    }
                    final Number uvalue = icd.getEndValue ( row, column );
                    if ( uvalue != null && !Double.isNaN ( v = uvalue.doubleValue() ) ) {
                        minimum = Math.min ( v, minimum );
                        maximum = Math.max ( v, maximum );
                    }
                }
            }
        } else {
            for ( int row2 = 0; row2 < rowCount; ++row2 ) {
                for ( int column2 = 0; column2 < columnCount; ++column2 ) {
                    final Number value2 = dataset.getValue ( row2, column2 );
                    if ( value2 != null ) {
                        final double v2 = value2.doubleValue();
                        if ( !Double.isNaN ( v2 ) ) {
                            minimum = Math.min ( minimum, v2 );
                            maximum = Math.max ( maximum, v2 );
                        }
                    }
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range iterateToFindRangeBounds ( final CategoryDataset dataset, final List visibleSeriesKeys, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        ParamChecks.nullNotPermitted ( visibleSeriesKeys, "visibleSeriesKeys" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int columnCount = dataset.getColumnCount();
        if ( includeInterval && dataset instanceof BoxAndWhiskerCategoryDataset ) {
            final BoxAndWhiskerCategoryDataset bx = ( BoxAndWhiskerCategoryDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.getRowIndex ( seriesKey );
                for ( int itemCount = dataset.getColumnCount(), item = 0; item < itemCount; ++item ) {
                    Number lvalue = bx.getMinRegularValue ( series, item );
                    if ( lvalue == null ) {
                        lvalue = bx.getValue ( series, item );
                    }
                    Number uvalue = bx.getMaxRegularValue ( series, item );
                    if ( uvalue == null ) {
                        uvalue = bx.getValue ( series, item );
                    }
                    if ( lvalue != null ) {
                        minimum = Math.min ( minimum, lvalue.doubleValue() );
                    }
                    if ( uvalue != null ) {
                        maximum = Math.max ( maximum, uvalue.doubleValue() );
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof IntervalCategoryDataset ) {
            final IntervalCategoryDataset icd = ( IntervalCategoryDataset ) dataset;
            for ( final Comparable seriesKey2 : visibleSeriesKeys ) {
                final int series2 = dataset.getRowIndex ( seriesKey2 );
                for ( int column = 0; column < columnCount; ++column ) {
                    final Number lvalue2 = icd.getStartValue ( series2, column );
                    final Number uvalue2 = icd.getEndValue ( series2, column );
                    if ( lvalue2 != null && !Double.isNaN ( lvalue2.doubleValue() ) ) {
                        minimum = Math.min ( minimum, lvalue2.doubleValue() );
                    }
                    if ( uvalue2 != null && !Double.isNaN ( uvalue2.doubleValue() ) ) {
                        maximum = Math.max ( maximum, uvalue2.doubleValue() );
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof MultiValueCategoryDataset ) {
            final MultiValueCategoryDataset mvcd = ( MultiValueCategoryDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.getRowIndex ( seriesKey );
                for ( int column2 = 0; column2 < columnCount; ++column2 ) {
                    final List values = mvcd.getValues ( series, column2 );
                    for ( final Object o : values ) {
                        if ( o instanceof Number ) {
                            final double v = ( ( Number ) o ).doubleValue();
                            if ( Double.isNaN ( v ) ) {
                                continue;
                            }
                            minimum = Math.min ( minimum, v );
                            maximum = Math.max ( maximum, v );
                        }
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof StatisticalCategoryDataset ) {
            final StatisticalCategoryDataset scd = ( StatisticalCategoryDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.getRowIndex ( seriesKey );
                for ( int column2 = 0; column2 < columnCount; ++column2 ) {
                    final Number meanN = scd.getMeanValue ( series, column2 );
                    if ( meanN != null ) {
                        double std = 0.0;
                        final Number stdN = scd.getStdDevValue ( series, column2 );
                        if ( stdN != null ) {
                            std = stdN.doubleValue();
                            if ( Double.isNaN ( std ) ) {
                                std = 0.0;
                            }
                        }
                        final double mean = meanN.doubleValue();
                        if ( !Double.isNaN ( mean ) ) {
                            minimum = Math.min ( minimum, mean - std );
                            maximum = Math.max ( maximum, mean + std );
                        }
                    }
                }
            }
        } else {
            for ( final Comparable seriesKey3 : visibleSeriesKeys ) {
                final int series3 = dataset.getRowIndex ( seriesKey3 );
                for ( int column3 = 0; column3 < columnCount; ++column3 ) {
                    final Number value = dataset.getValue ( series3, column3 );
                    if ( value != null ) {
                        final double v2 = value.doubleValue();
                        if ( !Double.isNaN ( v2 ) ) {
                            minimum = Math.min ( minimum, v2 );
                            maximum = Math.max ( maximum, v2 );
                        }
                    }
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range iterateXYRangeBounds ( final XYDataset dataset ) {
        return iterateRangeBounds ( dataset );
    }
    public static Range iterateRangeBounds ( final XYDataset dataset ) {
        return iterateRangeBounds ( dataset, true );
    }
    public static Range iterateRangeBounds ( final XYDataset dataset, final boolean includeInterval ) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int seriesCount = dataset.getSeriesCount();
        if ( includeInterval && dataset instanceof IntervalXYDataset ) {
            final IntervalXYDataset ixyd = ( IntervalXYDataset ) dataset;
            for ( int series = 0; series < seriesCount; ++series ) {
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double value = ixyd.getYValue ( series, item );
                    final double lvalue = ixyd.getStartYValue ( series, item );
                    final double uvalue = ixyd.getEndYValue ( series, item );
                    if ( !Double.isNaN ( value ) ) {
                        minimum = Math.min ( minimum, value );
                        maximum = Math.max ( maximum, value );
                    }
                    if ( !Double.isNaN ( lvalue ) ) {
                        minimum = Math.min ( minimum, lvalue );
                        maximum = Math.max ( maximum, lvalue );
                    }
                    if ( !Double.isNaN ( uvalue ) ) {
                        minimum = Math.min ( minimum, uvalue );
                        maximum = Math.max ( maximum, uvalue );
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof OHLCDataset ) {
            final OHLCDataset ohlc = ( OHLCDataset ) dataset;
            for ( int series = 0; series < seriesCount; ++series ) {
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double lvalue2 = ohlc.getLowValue ( series, item );
                    final double uvalue2 = ohlc.getHighValue ( series, item );
                    if ( !Double.isNaN ( lvalue2 ) ) {
                        minimum = Math.min ( minimum, lvalue2 );
                    }
                    if ( !Double.isNaN ( uvalue2 ) ) {
                        maximum = Math.max ( maximum, uvalue2 );
                    }
                }
            }
        } else {
            for ( int series2 = 0; series2 < seriesCount; ++series2 ) {
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double value2 = dataset.getYValue ( series2, item2 );
                    if ( !Double.isNaN ( value2 ) ) {
                        minimum = Math.min ( minimum, value2 );
                        maximum = Math.max ( maximum, value2 );
                    }
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range findZBounds ( final XYZDataset dataset ) {
        return findZBounds ( dataset, true );
    }
    public static Range findZBounds ( final XYZDataset dataset, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final Range result = iterateZBounds ( dataset, includeInterval );
        return result;
    }
    public static Range findZBounds ( final XYZDataset dataset, final List visibleSeriesKeys, final Range xRange, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final Range result = iterateToFindZBounds ( dataset, visibleSeriesKeys, xRange, includeInterval );
        return result;
    }
    public static Range iterateZBounds ( final XYZDataset dataset ) {
        return iterateZBounds ( dataset, true );
    }
    public static Range iterateZBounds ( final XYZDataset dataset, final boolean includeInterval ) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                final double value = dataset.getZValue ( series, item );
                if ( !Double.isNaN ( value ) ) {
                    minimum = Math.min ( minimum, value );
                    maximum = Math.max ( maximum, value );
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range iterateToFindDomainBounds ( final XYDataset dataset, final List visibleSeriesKeys, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        ParamChecks.nullNotPermitted ( visibleSeriesKeys, "visibleSeriesKeys" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        if ( includeInterval && dataset instanceof IntervalXYDataset ) {
            final IntervalXYDataset ixyd = ( IntervalXYDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.indexOf ( seriesKey );
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double xvalue = ixyd.getXValue ( series, item );
                    final double lvalue = ixyd.getStartXValue ( series, item );
                    final double uvalue = ixyd.getEndXValue ( series, item );
                    if ( !Double.isNaN ( xvalue ) ) {
                        minimum = Math.min ( minimum, xvalue );
                        maximum = Math.max ( maximum, xvalue );
                    }
                    if ( !Double.isNaN ( lvalue ) ) {
                        minimum = Math.min ( minimum, lvalue );
                    }
                    if ( !Double.isNaN ( uvalue ) ) {
                        maximum = Math.max ( maximum, uvalue );
                    }
                }
            }
        } else {
            for ( final Comparable seriesKey2 : visibleSeriesKeys ) {
                final int series2 = dataset.indexOf ( seriesKey2 );
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double x = dataset.getXValue ( series2, item2 );
                    if ( !Double.isNaN ( x ) ) {
                        minimum = Math.min ( minimum, x );
                        maximum = Math.max ( maximum, x );
                    }
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range iterateToFindRangeBounds ( final XYDataset dataset, final List visibleSeriesKeys, final Range xRange, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        ParamChecks.nullNotPermitted ( visibleSeriesKeys, "visibleSeriesKeys" );
        ParamChecks.nullNotPermitted ( xRange, "xRange" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        if ( includeInterval && dataset instanceof OHLCDataset ) {
            final OHLCDataset ohlc = ( OHLCDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.indexOf ( seriesKey );
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double x = ohlc.getXValue ( series, item );
                    if ( xRange.contains ( x ) ) {
                        final double lvalue = ohlc.getLowValue ( series, item );
                        final double uvalue = ohlc.getHighValue ( series, item );
                        if ( !Double.isNaN ( lvalue ) ) {
                            minimum = Math.min ( minimum, lvalue );
                        }
                        if ( !Double.isNaN ( uvalue ) ) {
                            maximum = Math.max ( maximum, uvalue );
                        }
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof BoxAndWhiskerXYDataset ) {
            final BoxAndWhiskerXYDataset bx = ( BoxAndWhiskerXYDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.indexOf ( seriesKey );
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double x = bx.getXValue ( series, item );
                    if ( xRange.contains ( x ) ) {
                        final Number lvalue2 = bx.getMinRegularValue ( series, item );
                        final Number uvalue2 = bx.getMaxRegularValue ( series, item );
                        if ( lvalue2 != null ) {
                            minimum = Math.min ( minimum, lvalue2.doubleValue() );
                        }
                        if ( uvalue2 != null ) {
                            maximum = Math.max ( maximum, uvalue2.doubleValue() );
                        }
                    }
                }
            }
        } else if ( includeInterval && dataset instanceof IntervalXYDataset ) {
            final IntervalXYDataset ixyd = ( IntervalXYDataset ) dataset;
            for ( final Comparable seriesKey : visibleSeriesKeys ) {
                final int series = dataset.indexOf ( seriesKey );
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double x = ixyd.getXValue ( series, item );
                    if ( xRange.contains ( x ) ) {
                        final double yvalue = ixyd.getYValue ( series, item );
                        final double lvalue3 = ixyd.getStartYValue ( series, item );
                        final double uvalue3 = ixyd.getEndYValue ( series, item );
                        if ( !Double.isNaN ( yvalue ) ) {
                            minimum = Math.min ( minimum, yvalue );
                            maximum = Math.max ( maximum, yvalue );
                        }
                        if ( !Double.isNaN ( lvalue3 ) ) {
                            minimum = Math.min ( minimum, lvalue3 );
                        }
                        if ( !Double.isNaN ( uvalue3 ) ) {
                            maximum = Math.max ( maximum, uvalue3 );
                        }
                    }
                }
            }
        } else {
            for ( final Comparable seriesKey2 : visibleSeriesKeys ) {
                final int series2 = dataset.indexOf ( seriesKey2 );
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double x2 = dataset.getXValue ( series2, item2 );
                    final double y = dataset.getYValue ( series2, item2 );
                    if ( xRange.contains ( x2 ) && !Double.isNaN ( y ) ) {
                        minimum = Math.min ( minimum, y );
                        maximum = Math.max ( maximum, y );
                    }
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Range iterateToFindZBounds ( final XYZDataset dataset, final List visibleSeriesKeys, final Range xRange, final boolean includeInterval ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        ParamChecks.nullNotPermitted ( visibleSeriesKeys, "visibleSeriesKeys" );
        ParamChecks.nullNotPermitted ( xRange, "xRange" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for ( final Comparable seriesKey : visibleSeriesKeys ) {
            final int series = dataset.indexOf ( seriesKey );
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                final double x = dataset.getXValue ( series, item );
                final double z = dataset.getZValue ( series, item );
                if ( xRange.contains ( x ) && !Double.isNaN ( z ) ) {
                    minimum = Math.min ( minimum, z );
                    maximum = Math.max ( maximum, z );
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    public static Number findMinimumDomainValue ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof DomainInfo ) {
            final DomainInfo info = ( DomainInfo ) dataset;
            return new Double ( info.getDomainLowerBound ( true ) );
        }
        double minimum = Double.POSITIVE_INFINITY;
        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                double value;
                if ( dataset instanceof IntervalXYDataset ) {
                    final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
                    value = intervalXYData.getStartXValue ( series, item );
                } else {
                    value = dataset.getXValue ( series, item );
                }
                if ( !Double.isNaN ( value ) ) {
                    minimum = Math.min ( minimum, value );
                }
            }
        }
        Number result;
        if ( minimum == Double.POSITIVE_INFINITY ) {
            result = null;
        } else {
            result = new Double ( minimum );
        }
        return result;
    }
    public static Number findMaximumDomainValue ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof DomainInfo ) {
            final DomainInfo info = ( DomainInfo ) dataset;
            return new Double ( info.getDomainUpperBound ( true ) );
        }
        double maximum = Double.NEGATIVE_INFINITY;
        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                double value;
                if ( dataset instanceof IntervalXYDataset ) {
                    final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
                    value = intervalXYData.getEndXValue ( series, item );
                } else {
                    value = dataset.getXValue ( series, item );
                }
                if ( !Double.isNaN ( value ) ) {
                    maximum = Math.max ( maximum, value );
                }
            }
        }
        Number result;
        if ( maximum == Double.NEGATIVE_INFINITY ) {
            result = null;
        } else {
            result = new Double ( maximum );
        }
        return result;
    }
    public static Number findMinimumRangeValue ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            return new Double ( info.getRangeLowerBound ( true ) );
        }
        double minimum = Double.POSITIVE_INFINITY;
        final int seriesCount = dataset.getRowCount();
        final int itemCount = dataset.getColumnCount();
        for ( int series = 0; series < seriesCount; ++series ) {
            for ( int item = 0; item < itemCount; ++item ) {
                Number value;
                if ( dataset instanceof IntervalCategoryDataset ) {
                    final IntervalCategoryDataset icd = ( IntervalCategoryDataset ) dataset;
                    value = icd.getStartValue ( series, item );
                } else {
                    value = dataset.getValue ( series, item );
                }
                if ( value != null ) {
                    minimum = Math.min ( minimum, value.doubleValue() );
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Double ( minimum );
    }
    public static Number findMinimumRangeValue ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            return new Double ( info.getRangeLowerBound ( true ) );
        }
        double minimum = Double.POSITIVE_INFINITY;
        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                double value;
                if ( dataset instanceof IntervalXYDataset ) {
                    final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
                    value = intervalXYData.getStartYValue ( series, item );
                } else if ( dataset instanceof OHLCDataset ) {
                    final OHLCDataset highLowData = ( OHLCDataset ) dataset;
                    value = highLowData.getLowValue ( series, item );
                } else {
                    value = dataset.getYValue ( series, item );
                }
                if ( !Double.isNaN ( value ) ) {
                    minimum = Math.min ( minimum, value );
                }
            }
        }
        if ( minimum == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Double ( minimum );
    }
    public static Number findMaximumRangeValue ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            return new Double ( info.getRangeUpperBound ( true ) );
        }
        double maximum = Double.NEGATIVE_INFINITY;
        final int seriesCount = dataset.getRowCount();
        final int itemCount = dataset.getColumnCount();
        for ( int series = 0; series < seriesCount; ++series ) {
            for ( int item = 0; item < itemCount; ++item ) {
                Number value;
                if ( dataset instanceof IntervalCategoryDataset ) {
                    final IntervalCategoryDataset icd = ( IntervalCategoryDataset ) dataset;
                    value = icd.getEndValue ( series, item );
                } else {
                    value = dataset.getValue ( series, item );
                }
                if ( value != null ) {
                    maximum = Math.max ( maximum, value.doubleValue() );
                }
            }
        }
        if ( maximum == Double.NEGATIVE_INFINITY ) {
            return null;
        }
        return new Double ( maximum );
    }
    public static Number findMaximumRangeValue ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( dataset instanceof RangeInfo ) {
            final RangeInfo info = ( RangeInfo ) dataset;
            return new Double ( info.getRangeUpperBound ( true ) );
        }
        double maximum = Double.NEGATIVE_INFINITY;
        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                double value;
                if ( dataset instanceof IntervalXYDataset ) {
                    final IntervalXYDataset intervalXYData = ( IntervalXYDataset ) dataset;
                    value = intervalXYData.getEndYValue ( series, item );
                } else if ( dataset instanceof OHLCDataset ) {
                    final OHLCDataset highLowData = ( OHLCDataset ) dataset;
                    value = highLowData.getHighValue ( series, item );
                } else {
                    value = dataset.getYValue ( series, item );
                }
                if ( !Double.isNaN ( value ) ) {
                    maximum = Math.max ( maximum, value );
                }
            }
        }
        if ( maximum == Double.NEGATIVE_INFINITY ) {
            return null;
        }
        return new Double ( maximum );
    }
    public static Range findStackedRangeBounds ( final CategoryDataset dataset ) {
        return findStackedRangeBounds ( dataset, 0.0 );
    }
    public static Range findStackedRangeBounds ( final CategoryDataset dataset, final double base ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Range result = null;
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for ( int categoryCount = dataset.getColumnCount(), item = 0; item < categoryCount; ++item ) {
            double positive = base;
            double negative = base;
            for ( int seriesCount = dataset.getRowCount(), series = 0; series < seriesCount; ++series ) {
                final Number number = dataset.getValue ( series, item );
                if ( number != null ) {
                    final double value = number.doubleValue();
                    if ( value > 0.0 ) {
                        positive += value;
                    }
                    if ( value < 0.0 ) {
                        negative += value;
                    }
                }
            }
            minimum = Math.min ( minimum, negative );
            maximum = Math.max ( maximum, positive );
        }
        if ( minimum <= maximum ) {
            result = new Range ( minimum, maximum );
        }
        return result;
    }
    public static Range findStackedRangeBounds ( final CategoryDataset dataset, final KeyToGroupMap map ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        boolean hasValidData = false;
        Range result = null;
        final int[] groupIndex = new int[dataset.getRowCount()];
        for ( int i = 0; i < dataset.getRowCount(); ++i ) {
            groupIndex[i] = map.getGroupIndex ( map.getGroup ( dataset.getRowKey ( i ) ) );
        }
        final int groupCount = map.getGroupCount();
        final double[] minimum = new double[groupCount];
        final double[] maximum = new double[groupCount];
        for ( int categoryCount = dataset.getColumnCount(), item = 0; item < categoryCount; ++item ) {
            final double[] positive = new double[groupCount];
            final double[] negative = new double[groupCount];
            for ( int seriesCount = dataset.getRowCount(), series = 0; series < seriesCount; ++series ) {
                final Number number = dataset.getValue ( series, item );
                if ( number != null ) {
                    hasValidData = true;
                    final double value = number.doubleValue();
                    if ( value > 0.0 ) {
                        positive[groupIndex[series]] += value;
                    }
                    if ( value < 0.0 ) {
                        negative[groupIndex[series]] += value;
                    }
                }
            }
            for ( int g = 0; g < groupCount; ++g ) {
                minimum[g] = Math.min ( minimum[g], negative[g] );
                maximum[g] = Math.max ( maximum[g], positive[g] );
            }
        }
        if ( hasValidData ) {
            for ( int j = 0; j < groupCount; ++j ) {
                result = Range.combine ( result, new Range ( minimum[j], maximum[j] ) );
            }
        }
        return result;
    }
    public static Number findMinimumStackedRangeValue ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Number result = null;
        boolean hasValidData = false;
        double minimum = 0.0;
        for ( int categoryCount = dataset.getColumnCount(), item = 0; item < categoryCount; ++item ) {
            double total = 0.0;
            for ( int seriesCount = dataset.getRowCount(), series = 0; series < seriesCount; ++series ) {
                final Number number = dataset.getValue ( series, item );
                if ( number != null ) {
                    hasValidData = true;
                    final double value = number.doubleValue();
                    if ( value < 0.0 ) {
                        total += value;
                    }
                }
            }
            minimum = Math.min ( minimum, total );
        }
        if ( hasValidData ) {
            result = new Double ( minimum );
        }
        return result;
    }
    public static Number findMaximumStackedRangeValue ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        Number result = null;
        boolean hasValidData = false;
        double maximum = 0.0;
        for ( int categoryCount = dataset.getColumnCount(), item = 0; item < categoryCount; ++item ) {
            double total = 0.0;
            for ( int seriesCount = dataset.getRowCount(), series = 0; series < seriesCount; ++series ) {
                final Number number = dataset.getValue ( series, item );
                if ( number != null ) {
                    hasValidData = true;
                    final double value = number.doubleValue();
                    if ( value > 0.0 ) {
                        total += value;
                    }
                }
            }
            maximum = Math.max ( maximum, total );
        }
        if ( hasValidData ) {
            result = new Double ( maximum );
        }
        return result;
    }
    public static Range findStackedRangeBounds ( final TableXYDataset dataset ) {
        return findStackedRangeBounds ( dataset, 0.0 );
    }
    public static Range findStackedRangeBounds ( final TableXYDataset dataset, final double base ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        double minimum = base;
        double maximum = base;
        for ( int itemNo = 0; itemNo < dataset.getItemCount(); ++itemNo ) {
            double positive = base;
            double negative = base;
            for ( int seriesCount = dataset.getSeriesCount(), seriesNo = 0; seriesNo < seriesCount; ++seriesNo ) {
                final double y = dataset.getYValue ( seriesNo, itemNo );
                if ( !Double.isNaN ( y ) ) {
                    if ( y > 0.0 ) {
                        positive += y;
                    } else {
                        negative += y;
                    }
                }
            }
            if ( positive > maximum ) {
                maximum = positive;
            }
            if ( negative < minimum ) {
                minimum = negative;
            }
        }
        if ( minimum <= maximum ) {
            return new Range ( minimum, maximum );
        }
        return null;
    }
    public static double calculateStackTotal ( final TableXYDataset dataset, final int item ) {
        double total = 0.0;
        for ( int seriesCount = dataset.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final double value = dataset.getYValue ( s, item );
            if ( !Double.isNaN ( value ) ) {
                total += value;
            }
        }
        return total;
    }
    public static Range findCumulativeRangeBounds ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        boolean allItemsNull = true;
        double minimum = 0.0;
        double maximum = 0.0;
        for ( int row = 0; row < dataset.getRowCount(); ++row ) {
            double runningTotal = 0.0;
            for ( int column = 0; column <= dataset.getColumnCount() - 1; ++column ) {
                final Number n = dataset.getValue ( row, column );
                if ( n != null ) {
                    allItemsNull = false;
                    final double value = n.doubleValue();
                    if ( !Double.isNaN ( value ) ) {
                        runningTotal += value;
                        minimum = Math.min ( minimum, runningTotal );
                        maximum = Math.max ( maximum, runningTotal );
                    }
                }
            }
        }
        if ( !allItemsNull ) {
            return new Range ( minimum, maximum );
        }
        return null;
    }
    public static double findYValue ( final XYDataset dataset, final int series, final double x ) {
        final int[] indices = findItemIndicesForX ( dataset, series, x );
        if ( indices[0] == -1 ) {
            return Double.NaN;
        }
        if ( indices[0] == indices[1] ) {
            return dataset.getYValue ( series, indices[0] );
        }
        final double x2 = dataset.getXValue ( series, indices[0] );
        final double x3 = dataset.getXValue ( series, indices[1] );
        final double y0 = dataset.getYValue ( series, indices[0] );
        final double y = dataset.getYValue ( series, indices[1] );
        return y0 + ( y - y0 ) * ( x - x2 ) / ( x3 - x2 );
    }
    public static int[] findItemIndicesForX ( final XYDataset dataset, final int series, final double x ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final int itemCount = dataset.getItemCount ( series );
        if ( itemCount == 0 ) {
            return new int[] { -1, -1 };
        }
        if ( itemCount == 1 ) {
            if ( x == dataset.getXValue ( series, 0 ) ) {
                return new int[] { 0, 0 };
            }
            return new int[] { -1, -1 };
        } else if ( dataset.getDomainOrder() == DomainOrder.ASCENDING ) {
            int low = 0;
            int high = itemCount - 1;
            final double lowValue = dataset.getXValue ( series, low );
            if ( lowValue > x ) {
                return new int[] { -1, -1 };
            }
            if ( lowValue == x ) {
                return new int[] { low, low };
            }
            final double highValue = dataset.getXValue ( series, high );
            if ( highValue < x ) {
                return new int[] { -1, -1 };
            }
            if ( highValue == x ) {
                return new int[] { high, high };
            }
            int mid = ( low + high ) / 2;
            while ( high - low > 1 ) {
                final double midV = dataset.getXValue ( series, mid );
                if ( x == midV ) {
                    return new int[] { mid, mid };
                }
                if ( midV < x ) {
                    low = mid;
                } else {
                    high = mid;
                }
                mid = ( low + high ) / 2;
            }
            return new int[] { low, high };
        } else if ( dataset.getDomainOrder() == DomainOrder.DESCENDING ) {
            int high2 = 0;
            int low2 = itemCount - 1;
            final double lowValue = dataset.getXValue ( series, low2 );
            if ( lowValue > x ) {
                return new int[] { -1, -1 };
            }
            final double highValue = dataset.getXValue ( series, high2 );
            if ( highValue < x ) {
                return new int[] { -1, -1 };
            }
            int mid = ( low2 + high2 ) / 2;
            while ( high2 - low2 > 1 ) {
                final double midV = dataset.getXValue ( series, mid );
                if ( x == midV ) {
                    return new int[] { mid, mid };
                }
                if ( midV < x ) {
                    low2 = mid;
                } else {
                    high2 = mid;
                }
                mid = ( low2 + high2 ) / 2;
            }
            return new int[] { low2, high2 };
        } else {
            final double prev = dataset.getXValue ( series, 0 );
            if ( x == prev ) {
                return new int[] { 0, 0 };
            }
            for ( int i = 1; i < itemCount; ++i ) {
                final double next = dataset.getXValue ( series, i );
                if ( x == next ) {
                    return new int[] { i, i };
                }
                if ( ( x > prev && x < next ) || ( x < prev && x > next ) ) {
                    return new int[] { i - 1, i };
                }
            }
            return new int[] { -1, -1 };
        }
    }
}
