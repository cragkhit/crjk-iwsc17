package org.jfree.chart.urls;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import org.jfree.data.general.PieDataset;
import java.util.ArrayList;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CustomPieURLGenerator implements PieURLGenerator, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7100607670144900503L;
    private ArrayList urls;
    public CustomPieURLGenerator() {
        this.urls = new ArrayList();
    }
    @Override
    public String generateURL ( final PieDataset dataset, final Comparable key, final int pieIndex ) {
        return this.getURL ( key, pieIndex );
    }
    public int getListCount() {
        return this.urls.size();
    }
    public int getURLCount ( final int list ) {
        int result = 0;
        final Map urlMap = this.urls.get ( list );
        if ( urlMap != null ) {
            result = urlMap.size();
        }
        return result;
    }
    public String getURL ( final Comparable key, final int mapIndex ) {
        String result = null;
        if ( mapIndex < this.getListCount() ) {
            final Map urlMap = this.urls.get ( mapIndex );
            if ( urlMap != null ) {
                result = urlMap.get ( key );
            }
        }
        return result;
    }
    public void addURLs ( final Map urlMap ) {
        this.urls.add ( urlMap );
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( o == this ) {
            return true;
        }
        if ( ! ( o instanceof CustomPieURLGenerator ) ) {
            return false;
        }
        final CustomPieURLGenerator generator = ( CustomPieURLGenerator ) o;
        if ( this.getListCount() != generator.getListCount() ) {
            return false;
        }
        for ( int pieItem = 0; pieItem < this.getListCount(); ++pieItem ) {
            if ( this.getURLCount ( pieItem ) != generator.getURLCount ( pieItem ) ) {
                return false;
            }
            final Set keySet = this.urls.get ( pieItem ).keySet();
            for ( final String key : keySet ) {
                if ( !this.getURL ( key, pieItem ).equals ( generator.getURL ( key, pieItem ) ) ) {
                    return false;
                }
            }
        }
        return true;
    }
    public Object clone() throws CloneNotSupportedException {
        final CustomPieURLGenerator urlGen = new CustomPieURLGenerator();
        for ( final Map map : this.urls ) {
            final Map newMap = new HashMap();
            for ( final String key : map.keySet() ) {
                newMap.put ( key, map.get ( key ) );
            }
            urlGen.addURLs ( newMap );
        }
        return urlGen;
    }
}
