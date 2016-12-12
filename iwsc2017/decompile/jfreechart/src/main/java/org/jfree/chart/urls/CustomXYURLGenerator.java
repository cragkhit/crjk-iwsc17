package org.jfree.chart.urls;
import java.util.Collection;
import org.jfree.data.xy.XYDataset;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CustomXYURLGenerator implements XYURLGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -8565933356596551832L;
    private ArrayList urlSeries;
    public CustomXYURLGenerator() {
        this.urlSeries = new ArrayList();
    }
    public int getListCount() {
        return this.urlSeries.size();
    }
    public int getURLCount ( final int list ) {
        int result = 0;
        final List urls = this.urlSeries.get ( list );
        if ( urls != null ) {
            result = urls.size();
        }
        return result;
    }
    public String getURL ( final int series, final int item ) {
        String result = null;
        if ( series < this.getListCount() ) {
            final List urls = this.urlSeries.get ( series );
            if ( urls != null && item < urls.size() ) {
                result = urls.get ( item );
            }
        }
        return result;
    }
    @Override
    public String generateURL ( final XYDataset dataset, final int series, final int item ) {
        return this.getURL ( series, item );
    }
    public void addURLSeries ( final List urls ) {
        List listToAdd = null;
        if ( urls != null ) {
            listToAdd = new ArrayList ( urls );
        }
        this.urlSeries.add ( listToAdd );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CustomXYURLGenerator ) ) {
            return false;
        }
        final CustomXYURLGenerator that = ( CustomXYURLGenerator ) obj;
        final int listCount = this.getListCount();
        if ( listCount != that.getListCount() ) {
            return false;
        }
        for ( int series = 0; series < listCount; ++series ) {
            final int urlCount = this.getURLCount ( series );
            if ( urlCount != that.getURLCount ( series ) ) {
                return false;
            }
            for ( int item = 0; item < urlCount; ++item ) {
                final String u1 = this.getURL ( series, item );
                final String u2 = that.getURL ( series, item );
                if ( u1 != null ) {
                    if ( !u1.equals ( u2 ) ) {
                        return false;
                    }
                } else if ( u2 != null ) {
                    return false;
                }
            }
        }
        return true;
    }
    public Object clone() throws CloneNotSupportedException {
        final CustomXYURLGenerator clone = ( CustomXYURLGenerator ) super.clone();
        clone.urlSeries = new ArrayList ( this.urlSeries );
        return clone;
    }
}
