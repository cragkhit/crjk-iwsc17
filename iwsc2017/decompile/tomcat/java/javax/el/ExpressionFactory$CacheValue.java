package javax.el;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReadWriteLock;
private static class CacheValue {
    private final ReadWriteLock lock;
    private String className;
    private WeakReference<Class<?>> ref;
    public CacheValue() {
        this.lock = new ReentrantReadWriteLock();
    }
    public ReadWriteLock getLock() {
        return this.lock;
    }
    public String getFactoryClassName() {
        return this.className;
    }
    public void setFactoryClassName ( final String className ) {
        this.className = className;
    }
    public Class<?> getFactoryClass() {
        return ( this.ref != null ) ? this.ref.get() : null;
    }
    public void setFactoryClass ( final Class<?> clazz ) {
        this.ref = new WeakReference<Class<?>> ( clazz );
    }
}
