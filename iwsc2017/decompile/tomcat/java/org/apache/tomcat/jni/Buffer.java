package org.apache.tomcat.jni;
import java.nio.ByteBuffer;
public class Buffer {
    public static native ByteBuffer malloc ( final int p0 );
    public static native ByteBuffer calloc ( final int p0, final int p1 );
    public static native ByteBuffer palloc ( final long p0, final int p1 );
    public static native ByteBuffer pcalloc ( final long p0, final int p1 );
    public static native ByteBuffer create ( final long p0, final int p1 );
    public static native void free ( final ByteBuffer p0 );
    public static native long address ( final ByteBuffer p0 );
    public static native long size ( final ByteBuffer p0 );
}
