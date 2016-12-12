package javax.el;
import java.lang.ref.WeakReference;
private static class CacheKey {
    private final int hash;
    private final WeakReference<ClassLoader> ref;
    public CacheKey ( final ClassLoader cl ) {
        this.hash = cl.hashCode();
        this.ref = new WeakReference<ClassLoader> ( cl );
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
        final ClassLoader thisCl = this.ref.get();
        return thisCl != null && thisCl == ( ( CacheKey ) obj ).ref.get();
    }
}
