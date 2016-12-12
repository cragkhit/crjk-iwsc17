package org.jfree.chart;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
public class LegendItemCollection implements Cloneable, Serializable {
    private static final long serialVersionUID = 1365215565589815953L;
    private List items;
    public LegendItemCollection() {
        this.items = new ArrayList();
    }
    public void add ( final LegendItem item ) {
        this.items.add ( item );
    }
    public void addAll ( final LegendItemCollection collection ) {
        this.items.addAll ( collection.items );
    }
    public LegendItem get ( final int index ) {
        return this.items.get ( index );
    }
    public int getItemCount() {
        return this.items.size();
    }
    public Iterator iterator() {
        return this.items.iterator();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LegendItemCollection ) ) {
            return false;
        }
        final LegendItemCollection that = ( LegendItemCollection ) obj;
        return this.items.equals ( that.items );
    }
    public Object clone() throws CloneNotSupportedException {
        final LegendItemCollection clone = ( LegendItemCollection ) super.clone();
        clone.items = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.items );
        return clone;
    }
}
