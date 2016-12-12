package org.apache.tomcat.jni;
public class Stdlib {
    public static native boolean memread ( byte [] dst, long src, int sz );
    public static native boolean memwrite ( long dst, byte [] src, int sz );
    public static native boolean memset ( long dst, int c, int sz );
    public static native long malloc ( int sz );
    public static native long realloc ( long mem, int sz );
    public static native long calloc ( int num, int sz );
    public static native void free ( long mem );
    public static native int getpid();
    public static native int getppid();
}
