package javax.el;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReadWriteLock;
private static class CacheValue {
    private final ReadWriteLock lock;
    private WeakReference<ExpressionFactory> ref;
    public CacheValue() {
        this.lock = new ReentrantReadWriteLock();
    }
    public ReadWriteLock getLock() {
        return this.lock;
    }
    public ExpressionFactory getExpressionFactory() {
        return ( this.ref != null ) ? this.ref.get() : null;
    }
    public void setExpressionFactory ( final ExpressionFactory factory ) {
        this.ref = new WeakReference<ExpressionFactory> ( factory );
    }
}
