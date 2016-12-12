package org.apache.tomcat.jni;
import java.nio.ByteBuffer;
public class Shm {
    public static native long create ( long reqsize, String filename, long pool )
    throws Error;
    public static native int remove ( String filename, long pool );
    public static native int destroy ( long m );
    public static native long attach ( String filename, long pool )
    throws Error;
    public static native int detach ( long m );
    public static native long baseaddr ( long m );
    public static native long size ( long m );
    public static native ByteBuffer buffer ( long m );
}
