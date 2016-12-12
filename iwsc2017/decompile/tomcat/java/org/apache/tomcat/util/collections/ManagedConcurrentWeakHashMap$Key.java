package org.apache.tomcat.util.collections;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
private static class Key extends WeakReference<Object> {
    private final int hash;
    private boolean dead;
    public Key ( final Object key, final ReferenceQueue<Object> queue ) {
        super ( key, queue );
        this.hash = key.hashCode();
    }
    @Override
    public int hashCode() {
        return this.hash;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( this.dead ) {
            return false;
        }
        if ( ! ( obj instanceof Reference ) ) {
            return false;
        }
        final Object oA = this.get();
        final Object oB = ( ( Reference ) obj ).get();
        return oA == oB || ( oA != null && oB != null && oA.equals ( oB ) );
    }
    public void ackDeath() {
        this.dead = true;
    }
    public boolean isDead() {
        return this.dead;
    }
}
