package org.apache.tomcat.jni;
import java.nio.ByteBuffer;
public class Buffer {
    public static native ByteBuffer malloc ( int size );
    public static native ByteBuffer calloc ( int num, int size );
    public static native ByteBuffer palloc ( long p, int size );
    public static native ByteBuffer pcalloc ( long p, int size );
    public static native ByteBuffer create ( long mem, int size );
    public static native void free ( ByteBuffer buf );
    public static native long address ( ByteBuffer buf );
    public static native long size ( ByteBuffer buf );
}
