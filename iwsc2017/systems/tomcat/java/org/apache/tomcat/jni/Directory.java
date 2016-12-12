package org.apache.tomcat.jni;
public class Directory {
    public static native int make ( String path, int perm, long pool );
    public static native int makeRecursive ( String path, int perm, long pool );
    public static native int remove ( String path, long pool );
    public static native String tempGet ( long pool );
    public static native long open ( String dirname, long pool )
    throws Error;
    public static native int close ( long thedir );
    public static native int rewind ( long thedir );
    public static native int read ( FileInfo finfo, int wanted, long thedir );
}
