package org.jfree.data;
import java.util.Iterator;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class KeyedObjects implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 1321582394193530984L;
    private List data;
    public KeyedObjects() {
        this.data = new ArrayList();
    }
    public int getItemCount() {
        return this.data.size();
    }
    public Object getObject ( final int item ) {
        Object result = null;
        final KeyedObject kobj = this.data.get ( item );
        if ( kobj != null ) {
            result = kobj.getObject();
        }
        return result;
    }
    public Comparable getKey ( final int index ) {
        Comparable result = null;
        final KeyedObject item = this.data.get ( index );
        if ( item != null ) {
            result = item.getKey();
        }
        return result;
    }
    public int getIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        int i = 0;
        for ( final KeyedObject ko : this.data ) {
            if ( ko.getKey().equals ( key ) ) {
                return i;
            }
            ++i;
        }
        return -1;
    }
    public List getKeys() {
        final List result = new ArrayList();
        for ( final KeyedObject ko : this.data ) {
            result.add ( ko.getKey() );
        }
        return result;
    }
    public Object getObject ( final Comparable key ) {
        final int index = this.getIndex ( key );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "The key (" + key + ") is not recognised." );
        }
        return this.getObject ( index );
    }
    public void addObject ( final Comparable key, final Object object ) {
        this.setObject ( key, object );
    }
    public void setObject ( final Comparable key, final Object object ) {
        final int keyIndex = this.getIndex ( key );
        if ( keyIndex >= 0 ) {
            final KeyedObject ko = this.data.get ( keyIndex );
            ko.setObject ( object );
        } else {
            final KeyedObject ko = new KeyedObject ( key, object );
            this.data.add ( ko );
        }
    }
    public void insertValue ( final int position, final Comparable key, final Object value ) {
        if ( position < 0 || position > this.data.size() ) {
            throw new IllegalArgumentException ( "'position' out of bounds." );
        }
        ParamChecks.nullNotPermitted ( key, "key" );
        final int pos = this.getIndex ( key );
        if ( pos >= 0 ) {
            this.data.remove ( pos );
        }
        final KeyedObject item = new KeyedObject ( key, value );
        if ( position <= this.data.size() ) {
            this.data.add ( position, item );
        } else {
            this.data.add ( item );
        }
    }
    public void removeValue ( final int index ) {
        this.data.remove ( index );
    }
    public void removeValue ( final Comparable key ) {
        final int index = this.getIndex ( key );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "The key (" + key.toString() + ") is not recognised." );
        }
        this.removeValue ( index );
    }
    public void clear() {
        this.data.clear();
    }
    public Object clone() throws CloneNotSupportedException {
        final KeyedObjects clone = ( KeyedObjects ) super.clone();
        clone.data = new ArrayList();
        for ( final KeyedObject ko : this.data ) {
            clone.data.add ( ko.clone() );
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedObjects ) ) {
            return false;
        }
        final KeyedObjects that = ( KeyedObjects ) obj;
        final int count = this.getItemCount();
        if ( count != that.getItemCount() ) {
            return false;
        }
        for ( int i = 0; i < count; ++i ) {
            final Comparable k1 = this.getKey ( i );
            final Comparable k2 = that.getKey ( i );
            if ( !k1.equals ( k2 ) ) {
                return false;
            }
            final Object o1 = this.getObject ( i );
            final Object o2 = that.getObject ( i );
            if ( o1 == null ) {
                if ( o2 != null ) {
                    return false;
                }
            } else if ( !o1.equals ( o2 ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        return ( this.data != null ) ? this.data.hashCode() : 0;
    }
}
