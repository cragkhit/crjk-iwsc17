package org.apache.tomcat.jni;
public class Mmap {
    public static final int APR_MMAP_READ  = 1;
    public static final int APR_MMAP_WRITE = 2;
    public static native long create ( long file, long offset, long size, int flag, long pool )
    throws Error;
    public static native long dup ( long mmap, long pool )
    throws Error;
    public static native int delete ( long mm );
    public static native long offset ( long mm, long offset )
    throws Error;
}
