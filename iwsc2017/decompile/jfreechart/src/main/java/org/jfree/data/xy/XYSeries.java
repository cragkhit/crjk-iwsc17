package org.jfree.data.xy;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesException;
import org.jfree.chart.util.ParamChecks;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.data.general.Series;
public class XYSeries extends Series implements Cloneable, Serializable {
    static final long serialVersionUID = -5908509288197150436L;
    protected List data;
    private int maximumItemCount;
    private boolean autoSort;
    private boolean allowDuplicateXValues;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    public XYSeries ( final Comparable key ) {
        this ( key, true, true );
    }
    public XYSeries ( final Comparable key, final boolean autoSort ) {
        this ( key, autoSort, true );
    }
    public XYSeries ( final Comparable key, final boolean autoSort, final boolean allowDuplicateXValues ) {
        super ( key );
        this.maximumItemCount = Integer.MAX_VALUE;
        this.data = new ArrayList();
        this.autoSort = autoSort;
        this.allowDuplicateXValues = allowDuplicateXValues;
        this.minX = Double.NaN;
        this.maxX = Double.NaN;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
    }
    public double getMinX() {
        return this.minX;
    }
    public double getMaxX() {
        return this.maxX;
    }
    public double getMinY() {
        return this.minY;
    }
    public double getMaxY() {
        return this.maxY;
    }
    private void updateBoundsForAddedItem ( final XYDataItem item ) {
        final double x = item.getXValue();
        this.minX = this.minIgnoreNaN ( this.minX, x );
        this.maxX = this.maxIgnoreNaN ( this.maxX, x );
        if ( item.getY() != null ) {
            final double y = item.getYValue();
            this.minY = this.minIgnoreNaN ( this.minY, y );
            this.maxY = this.maxIgnoreNaN ( this.maxY, y );
        }
    }
    private void updateBoundsForRemovedItem ( final XYDataItem item ) {
        boolean itemContributesToXBounds = false;
        boolean itemContributesToYBounds = false;
        final double x = item.getXValue();
        if ( !Double.isNaN ( x ) && ( x <= this.minX || x >= this.maxX ) ) {
            itemContributesToXBounds = true;
        }
        if ( item.getY() != null ) {
            final double y = item.getYValue();
            if ( !Double.isNaN ( y ) && ( y <= this.minY || y >= this.maxY ) ) {
                itemContributesToYBounds = true;
            }
        }
        if ( itemContributesToYBounds ) {
            this.findBoundsByIteration();
        } else if ( itemContributesToXBounds ) {
            if ( this.getAutoSort() ) {
                this.minX = this.getX ( 0 ).doubleValue();
                this.maxX = this.getX ( this.getItemCount() - 1 ).doubleValue();
            } else {
                this.findBoundsByIteration();
            }
        }
    }
    private void findBoundsByIteration() {
        this.minX = Double.NaN;
        this.maxX = Double.NaN;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
        for ( final XYDataItem item : this.data ) {
            this.updateBoundsForAddedItem ( item );
        }
    }
    public boolean getAutoSort() {
        return this.autoSort;
    }
    public boolean getAllowDuplicateXValues() {
        return this.allowDuplicateXValues;
    }
    @Override
    public int getItemCount() {
        return this.data.size();
    }
    public List getItems() {
        return Collections.unmodifiableList ( ( List<?> ) this.data );
    }
    public int getMaximumItemCount() {
        return this.maximumItemCount;
    }
    public void setMaximumItemCount ( final int maximum ) {
        this.maximumItemCount = maximum;
        final int remove = this.data.size() - maximum;
        if ( remove > 0 ) {
            this.data.subList ( 0, remove ).clear();
            this.findBoundsByIteration();
            this.fireSeriesChanged();
        }
    }
    public void add ( final XYDataItem item ) {
        this.add ( item, true );
    }
    public void add ( final double x, final double y ) {
        this.add ( new Double ( x ), new Double ( y ), true );
    }
    public void add ( final double x, final double y, final boolean notify ) {
        this.add ( new Double ( x ), new Double ( y ), notify );
    }
    public void add ( final double x, final Number y ) {
        this.add ( new Double ( x ), y );
    }
    public void add ( final double x, final Number y, final boolean notify ) {
        this.add ( new Double ( x ), y, notify );
    }
    public void add ( final Number x, final Number y ) {
        this.add ( x, y, true );
    }
    public void add ( final Number x, final Number y, final boolean notify ) {
        final XYDataItem item = new XYDataItem ( x, y );
        this.add ( item, notify );
    }
    public void add ( XYDataItem item, final boolean notify ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        item = ( XYDataItem ) item.clone();
        if ( this.autoSort ) {
            int index = Collections.binarySearch ( this.data, item );
            if ( index < 0 ) {
                this.data.add ( -index - 1, item );
            } else {
                if ( !this.allowDuplicateXValues ) {
                    throw new SeriesException ( "X-value already exists." );
                }
                for ( int size = this.data.size(); index < size && item.compareTo ( this.data.get ( index ) ) == 0; ++index ) {}
                if ( index < this.data.size() ) {
                    this.data.add ( index, item );
                } else {
                    this.data.add ( item );
                }
            }
        } else {
            if ( !this.allowDuplicateXValues ) {
                final int index = this.indexOf ( item.getX() );
                if ( index >= 0 ) {
                    throw new SeriesException ( "X-value already exists." );
                }
            }
            this.data.add ( item );
        }
        this.updateBoundsForAddedItem ( item );
        if ( this.getItemCount() > this.maximumItemCount ) {
            final XYDataItem removed = this.data.remove ( 0 );
            this.updateBoundsForRemovedItem ( removed );
        }
        if ( notify ) {
            this.fireSeriesChanged();
        }
    }
    public void delete ( final int start, final int end ) {
        this.data.subList ( start, end + 1 ).clear();
        this.findBoundsByIteration();
        this.fireSeriesChanged();
    }
    public XYDataItem remove ( final int index ) {
        final XYDataItem removed = this.data.remove ( index );
        this.updateBoundsForRemovedItem ( removed );
        this.fireSeriesChanged();
        return removed;
    }
    public XYDataItem remove ( final Number x ) {
        return this.remove ( this.indexOf ( x ) );
    }
    public void clear() {
        if ( this.data.size() > 0 ) {
            this.data.clear();
            this.minX = Double.NaN;
            this.maxX = Double.NaN;
            this.minY = Double.NaN;
            this.maxY = Double.NaN;
            this.fireSeriesChanged();
        }
    }
    public XYDataItem getDataItem ( final int index ) {
        final XYDataItem item = this.data.get ( index );
        return ( XYDataItem ) item.clone();
    }
    XYDataItem getRawDataItem ( final int index ) {
        return this.data.get ( index );
    }
    public Number getX ( final int index ) {
        return this.getRawDataItem ( index ).getX();
    }
    public Number getY ( final int index ) {
        return this.getRawDataItem ( index ).getY();
    }
    public void update ( final int index, final Number y ) {
        final XYDataItem item = this.getRawDataItem ( index );
        boolean iterate = false;
        final double oldY = item.getYValue();
        if ( !Double.isNaN ( oldY ) ) {
            iterate = ( oldY <= this.minY || oldY >= this.maxY );
        }
        item.setY ( y );
        if ( iterate ) {
            this.findBoundsByIteration();
        } else if ( y != null ) {
            final double yy = y.doubleValue();
            this.minY = this.minIgnoreNaN ( this.minY, yy );
            this.maxY = this.maxIgnoreNaN ( this.maxY, yy );
        }
        this.fireSeriesChanged();
    }
    private double minIgnoreNaN ( final double a, final double b ) {
        if ( Double.isNaN ( a ) ) {
            return b;
        }
        if ( Double.isNaN ( b ) ) {
            return a;
        }
        return Math.min ( a, b );
    }
    private double maxIgnoreNaN ( final double a, final double b ) {
        if ( Double.isNaN ( a ) ) {
            return b;
        }
        if ( Double.isNaN ( b ) ) {
            return a;
        }
        return Math.max ( a, b );
    }
    public void updateByIndex ( final int index, final Number y ) {
        this.update ( index, y );
    }
    public void update ( final Number x, final Number y ) {
        final int index = this.indexOf ( x );
        if ( index < 0 ) {
            throw new SeriesException ( "No observation for x = " + x );
        }
        this.updateByIndex ( index, y );
    }
    public XYDataItem addOrUpdate ( final double x, final double y ) {
        return this.addOrUpdate ( new Double ( x ), new Double ( y ) );
    }
    public XYDataItem addOrUpdate ( final Number x, final Number y ) {
        return this.addOrUpdate ( new XYDataItem ( x, y ) );
    }
    public XYDataItem addOrUpdate ( XYDataItem item ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        if ( this.allowDuplicateXValues ) {
            this.add ( item );
            return null;
        }
        XYDataItem overwritten = null;
        final int index = this.indexOf ( item.getX() );
        if ( index >= 0 ) {
            final XYDataItem existing = this.data.get ( index );
            overwritten = ( XYDataItem ) existing.clone();
            boolean iterate = false;
            final double oldY = existing.getYValue();
            if ( !Double.isNaN ( oldY ) ) {
                iterate = ( oldY <= this.minY || oldY >= this.maxY );
            }
            existing.setY ( item.getY() );
            if ( iterate ) {
                this.findBoundsByIteration();
            } else if ( item.getY() != null ) {
                final double yy = item.getY().doubleValue();
                this.minY = this.minIgnoreNaN ( this.minY, yy );
                this.maxY = this.maxIgnoreNaN ( this.maxY, yy );
            }
        } else {
            item = ( XYDataItem ) item.clone();
            if ( this.autoSort ) {
                this.data.add ( -index - 1, item );
            } else {
                this.data.add ( item );
            }
            this.updateBoundsForAddedItem ( item );
            if ( this.getItemCount() > this.maximumItemCount ) {
                final XYDataItem removed = this.data.remove ( 0 );
                this.updateBoundsForRemovedItem ( removed );
            }
        }
        this.fireSeriesChanged();
        return overwritten;
    }
    public int indexOf ( final Number x ) {
        if ( this.autoSort ) {
            return Collections.binarySearch ( this.data, new XYDataItem ( x, null ) );
        }
        for ( int i = 0; i < this.data.size(); ++i ) {
            final XYDataItem item = this.data.get ( i );
            if ( item.getX().equals ( x ) ) {
                return i;
            }
        }
        return -1;
    }
    public double[][] toArray() {
        final int itemCount = this.getItemCount();
        final double[][] result = new double[2][itemCount];
        for ( int i = 0; i < itemCount; ++i ) {
            result[0][i] = this.getX ( i ).doubleValue();
            final Number y = this.getY ( i );
            if ( y != null ) {
                result[1][i] = y.doubleValue();
            } else {
                result[1][i] = Double.NaN;
            }
        }
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final XYSeries clone = ( XYSeries ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        return clone;
    }
    public XYSeries createCopy ( final int start, final int end ) throws CloneNotSupportedException {
        final XYSeries copy = ( XYSeries ) super.clone();
        copy.data = new ArrayList();
        if ( this.data.size() > 0 ) {
            for ( int index = start; index <= end; ++index ) {
                final XYDataItem item = this.data.get ( index );
                final XYDataItem clone = ( XYDataItem ) item.clone();
                try {
                    copy.add ( clone );
                } catch ( SeriesException e ) {
                    throw new RuntimeException ( "Unable to add cloned data item.", e );
                }
            }
        }
        return copy;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYSeries ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final XYSeries that = ( XYSeries ) obj;
        return this.maximumItemCount == that.maximumItemCount && this.autoSort == that.autoSort && this.allowDuplicateXValues == that.allowDuplicateXValues && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int count = this.getItemCount();
        if ( count > 0 ) {
            final XYDataItem item = this.getRawDataItem ( 0 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 1 ) {
            final XYDataItem item = this.getRawDataItem ( count - 1 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 2 ) {
            final XYDataItem item = this.getRawDataItem ( count / 2 );
            result = 29 * result + item.hashCode();
        }
        result = 29 * result + this.maximumItemCount;
        result = 29 * result + ( this.autoSort ? 1 : 0 );
        result = 29 * result + ( this.allowDuplicateXValues ? 1 : 0 );
        return result;
    }
}
