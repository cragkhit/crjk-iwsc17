package org.jfree.data;
import org.jfree.util.ObjectUtilities;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class KeyedObject implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 2677930479256885863L;
    private Comparable key;
    private Object object;
    public KeyedObject ( final Comparable key, final Object object ) {
        this.key = key;
        this.object = object;
    }
    public Comparable getKey() {
        return this.key;
    }
    public Object getObject() {
        return this.object;
    }
    public void setObject ( final Object object ) {
        this.object = object;
    }
    public Object clone() throws CloneNotSupportedException {
        final KeyedObject clone = ( KeyedObject ) super.clone();
        if ( this.object instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.object;
            clone.object = pc.clone();
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedObject ) ) {
            return false;
        }
        final KeyedObject that = ( KeyedObject ) obj;
        return ObjectUtilities.equal ( ( Object ) this.key, ( Object ) that.key ) && ObjectUtilities.equal ( this.object, that.object );
    }
}
