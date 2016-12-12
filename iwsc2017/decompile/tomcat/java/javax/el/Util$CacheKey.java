package javax.el;
import java.lang.ref.WeakReference;
private static class CacheKey {
    private final int hash;
    private final WeakReference<ClassLoader> ref;
    public CacheKey ( final ClassLoader key ) {
        this.hash = key.hashCode();
        this.ref = new WeakReference<ClassLoader> ( key );
    }
    @Override
    public int hashCode() {
        return this.hash;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CacheKey ) ) {
            return false;
        }
        final ClassLoader thisKey = this.ref.get();
        return thisKey != null && thisKey == ( ( CacheKey ) obj ).ref.get();
    }
}
