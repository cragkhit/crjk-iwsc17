package org.apache.tomcat.jni;
public class Lock {
    public static final int APR_LOCK_FCNTL        = 0;
    public static final int APR_LOCK_FLOCK        = 1;
    public static final int APR_LOCK_SYSVSEM      = 2;
    public static final int APR_LOCK_PROC_PTHREAD = 3;
    public static final int APR_LOCK_POSIXSEM     = 4;
    public static final int APR_LOCK_DEFAULT      = 5;
    public static native long create ( String fname, int mech, long pool )
    throws Error;
    public static native long childInit ( String fname, long pool )
    throws Error;
    public static native int lock ( long mutex );
    public static native int trylock ( long mutex );
    public static native int unlock ( long mutex );
    public static native int destroy ( long mutex );
    public static native String lockfile ( long mutex );
    public static native String name ( long mutex );
    public static native String defname();
}
