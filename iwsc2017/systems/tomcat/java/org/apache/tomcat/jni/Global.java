package org.apache.tomcat.jni;
public class Global {
    public static native long create ( String fname, int mech, long pool )
    throws Error;
    public static native long childInit ( String fname, long pool )
    throws Error;
    public static native int lock ( long mutex );
    public static native int trylock ( long mutex );
    public static native int unlock ( long mutex );
    public static native int destroy ( long mutex );
}
