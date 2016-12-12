package org.apache.tomcat.jni;
public class Poll {
    public static final int APR_POLLIN   = 0x001;
    public static final int APR_POLLPRI  = 0x002;
    public static final int APR_POLLOUT  = 0x004;
    public static final int APR_POLLERR  = 0x010;
    public static final int APR_POLLHUP  = 0x020;
    public static final int APR_POLLNVAL = 0x040;
    public static final int APR_POLLSET_THREADSAFE = 0x001;
    public static final int APR_NO_DESC       = 0;
    public static final int APR_POLL_SOCKET   = 1;
    public static final int APR_POLL_FILE     = 2;
    public static final int APR_POLL_LASTDESC = 3;
    public static native long create ( int size, long p, int flags, long ttl )
    throws Error;
    public static native int destroy ( long pollset );
    public static native int add ( long pollset, long sock,
                                   int reqevents );
    public static native int addWithTimeout ( long pollset, long sock,
            int reqevents, long timeout );
    public static native int remove ( long pollset, long sock );
    public static native int poll ( long pollset, long timeout,
                                    long [] descriptors, boolean remove );
    public static native int maintain ( long pollset, long [] descriptors,
                                        boolean remove );
    public static native void setTtl ( long pollset, long ttl );
    public static native long getTtl ( long pollset );
    public static native int pollset ( long pollset, long [] descriptors );
    public static native int interrupt ( long pollset );
    public static native boolean wakeable ( long pollset );
}
