package org.apache.tomcat.jni;
import java.nio.ByteBuffer;
public class Pool {
    public static native long create ( long parent );
    public static native void clear ( long pool );
    public static native void destroy ( long pool );
    public static native long parentGet ( long pool );
    public static native boolean isAncestor ( long a, long b );
    public static native long cleanupRegister ( long pool, Object o );
    public static native void cleanupKill ( long pool, long data );
    public static native void noteSubprocess ( long a, long proc, int how );
    public static native ByteBuffer alloc ( long p, int size );
    public static native ByteBuffer calloc ( long p, int size );
    public static native int dataSet ( long pool, String key, Object data );
    public static native Object dataGet ( long pool, String key );
    public static native void cleanupForExec();
}
