package org.jfree.data.statistics;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.List;
public abstract class BoxAndWhiskerCalculator {
    public static BoxAndWhiskerItem calculateBoxAndWhiskerStatistics ( final List values ) {
        return calculateBoxAndWhiskerStatistics ( values, true );
    }
    public static BoxAndWhiskerItem calculateBoxAndWhiskerStatistics ( final List values, final boolean stripNullAndNaNItems ) {
        ParamChecks.nullNotPermitted ( values, "values" );
        List vlist;
        if ( stripNullAndNaNItems ) {
            vlist = new ArrayList ( values.size() );
            final Iterator iterator = values.listIterator();
            while ( iterator.hasNext() ) {
                final Object obj = iterator.next();
                if ( obj instanceof Number ) {
                    final Number n = ( Number ) obj;
                    final double v = n.doubleValue();
                    if ( Double.isNaN ( v ) ) {
                        continue;
                    }
                    vlist.add ( n );
                }
            }
        } else {
            vlist = values;
        }
        Collections.sort ( ( List<Comparable> ) vlist );
        final double mean = Statistics.calculateMean ( vlist, false );
        final double median = Statistics.calculateMedian ( vlist, false );
        final double q1 = calculateQ1 ( vlist );
        final double q2 = calculateQ3 ( vlist );
        final double interQuartileRange = q2 - q1;
        final double upperOutlierThreshold = q2 + interQuartileRange * 1.5;
        final double lowerOutlierThreshold = q1 - interQuartileRange * 1.5;
        final double upperFaroutThreshold = q2 + interQuartileRange * 2.0;
        final double lowerFaroutThreshold = q1 - interQuartileRange * 2.0;
        double minRegularValue = Double.POSITIVE_INFINITY;
        double maxRegularValue = Double.NEGATIVE_INFINITY;
        double minOutlier = Double.POSITIVE_INFINITY;
        double maxOutlier = Double.NEGATIVE_INFINITY;
        final List outliers = new ArrayList();
        for ( final Number number : vlist ) {
            final double value = number.doubleValue();
            if ( value > upperOutlierThreshold ) {
                outliers.add ( number );
                if ( value > maxOutlier && value <= upperFaroutThreshold ) {
                    maxOutlier = value;
                }
            } else if ( value < lowerOutlierThreshold ) {
                outliers.add ( number );
                if ( value < minOutlier && value >= lowerFaroutThreshold ) {
                    minOutlier = value;
                }
            } else {
                minRegularValue = Math.min ( minRegularValue, value );
                maxRegularValue = Math.max ( maxRegularValue, value );
            }
            minOutlier = Math.min ( minOutlier, minRegularValue );
            maxOutlier = Math.max ( maxOutlier, maxRegularValue );
        }
        return new BoxAndWhiskerItem ( new Double ( mean ), new Double ( median ), new Double ( q1 ), new Double ( q2 ), new Double ( minRegularValue ), new Double ( maxRegularValue ), new Double ( minOutlier ), new Double ( maxOutlier ), outliers );
    }
    public static double calculateQ1 ( final List values ) {
        ParamChecks.nullNotPermitted ( values, "values" );
        double result = Double.NaN;
        final int count = values.size();
        if ( count > 0 ) {
            if ( count % 2 == 1 ) {
                if ( count > 1 ) {
                    result = Statistics.calculateMedian ( values, 0, count / 2 );
                } else {
                    result = Statistics.calculateMedian ( values, 0, 0 );
                }
            } else {
                result = Statistics.calculateMedian ( values, 0, count / 2 - 1 );
            }
        }
        return result;
    }
    public static double calculateQ3 ( final List values ) {
        ParamChecks.nullNotPermitted ( values, "values" );
        double result = Double.NaN;
        final int count = values.size();
        if ( count > 0 ) {
            if ( count % 2 == 1 ) {
                if ( count > 1 ) {
                    result = Statistics.calculateMedian ( values, count / 2, count - 1 );
                } else {
                    result = Statistics.calculateMedian ( values, 0, 0 );
                }
            } else {
                result = Statistics.calculateMedian ( values, count / 2, count - 1 );
            }
        }
        return result;
    }
}
