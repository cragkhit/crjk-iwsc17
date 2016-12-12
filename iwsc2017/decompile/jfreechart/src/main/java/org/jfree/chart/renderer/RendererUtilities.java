package org.jfree.chart.renderer;
import org.jfree.data.DomainOrder;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataset;
public class RendererUtilities {
    public static int findLiveItemsLowerBound ( final XYDataset dataset, final int series, final double xLow, final double xHigh ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( xLow >= xHigh ) {
            throw new IllegalArgumentException ( "Requires xLow < xHigh." );
        }
        final int itemCount = dataset.getItemCount ( series );
        if ( itemCount <= 1 ) {
            return 0;
        }
        if ( dataset.getDomainOrder() == DomainOrder.ASCENDING ) {
            int low = 0;
            int high = itemCount - 1;
            final double lowValue = dataset.getXValue ( series, low );
            if ( lowValue >= xLow ) {
                return low;
            }
            final double highValue = dataset.getXValue ( series, high );
            if ( highValue < xLow ) {
                return high;
            }
            while ( high - low > 1 ) {
                final int mid = ( low + high ) / 2;
                final double midV = dataset.getXValue ( series, mid );
                if ( midV >= xLow ) {
                    high = mid;
                } else {
                    low = mid;
                }
            }
            return high;
        } else {
            if ( dataset.getDomainOrder() != DomainOrder.DESCENDING ) {
                int index = 0;
                for ( double x = dataset.getXValue ( series, index ); index < itemCount && x < xLow; x = dataset.getXValue ( series, index ) ) {
                    if ( ++index < itemCount ) {}
                }
                return Math.min ( Math.max ( 0, index ), itemCount - 1 );
            }
            int low = 0;
            int high = itemCount - 1;
            final double lowValue = dataset.getXValue ( series, low );
            if ( lowValue <= xHigh ) {
                return low;
            }
            final double highValue = dataset.getXValue ( series, high );
            if ( highValue > xHigh ) {
                return high;
            }
            while ( high - low > 1 ) {
                final int mid = ( low + high ) / 2;
                final double midV = dataset.getXValue ( series, mid );
                if ( midV > xHigh ) {
                    low = mid;
                } else {
                    high = mid;
                }
            }
            return high;
        }
    }
    public static int findLiveItemsUpperBound ( final XYDataset dataset, final int series, final double xLow, final double xHigh ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        if ( xLow >= xHigh ) {
            throw new IllegalArgumentException ( "Requires xLow < xHigh." );
        }
        final int itemCount = dataset.getItemCount ( series );
        if ( itemCount <= 1 ) {
            return 0;
        }
        if ( dataset.getDomainOrder() == DomainOrder.ASCENDING ) {
            int low = 0;
            int high = itemCount - 1;
            final double lowValue = dataset.getXValue ( series, low );
            if ( lowValue > xHigh ) {
                return low;
            }
            final double highValue = dataset.getXValue ( series, high );
            if ( highValue <= xHigh ) {
                return high;
            }
            int mid = ( low + high ) / 2;
            while ( high - low > 1 ) {
                final double midV = dataset.getXValue ( series, mid );
                if ( midV <= xHigh ) {
                    low = mid;
                } else {
                    high = mid;
                }
                mid = ( low + high ) / 2;
            }
            return mid;
        } else {
            if ( dataset.getDomainOrder() != DomainOrder.DESCENDING ) {
                int index = itemCount - 1;
                for ( double x = dataset.getXValue ( series, index ); index >= 0 && x > xHigh; x = dataset.getXValue ( series, index ) ) {
                    if ( --index >= 0 ) {}
                }
                return Math.max ( index, 0 );
            }
            int low = 0;
            int high = itemCount - 1;
            int mid2 = ( low + high ) / 2;
            final double lowValue2 = dataset.getXValue ( series, low );
            if ( lowValue2 < xLow ) {
                return low;
            }
            final double highValue2 = dataset.getXValue ( series, high );
            if ( highValue2 >= xLow ) {
                return high;
            }
            while ( high - low > 1 ) {
                final double midV = dataset.getXValue ( series, mid2 );
                if ( midV >= xLow ) {
                    low = mid2;
                } else {
                    high = mid2;
                }
                mid2 = ( low + high ) / 2;
            }
            return mid2;
        }
    }
    public static int[] findLiveItems ( final XYDataset dataset, final int series, final double xLow, final double xHigh ) {
        int i0 = findLiveItemsLowerBound ( dataset, series, xLow, xHigh );
        final int i = findLiveItemsUpperBound ( dataset, series, xLow, xHigh );
        if ( i0 > i ) {
            i0 = i;
        }
        return new int[] { i0, i };
    }
}
