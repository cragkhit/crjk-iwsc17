package org.apache.tomcat.jni;
public class Multicast {
    public static native int join ( long sock, long join,
                                    long iface, long source );
    public static native int leave ( long sock, long addr,
                                     long iface, long source );
    public static native int hops ( long sock, int ttl );
    public static native int loopback ( long sock, boolean opt );
    public static native int ointerface ( long sock, long iface );
}
