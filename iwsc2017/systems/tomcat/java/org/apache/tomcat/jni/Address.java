package org.apache.tomcat.jni;
public class Address {
    public static final String APR_ANYADDR = "0.0.0.0";
    public static native boolean fill ( Sockaddr info, long sa );
    public static native Sockaddr getInfo ( long sa );
    public static native long info ( String hostname, int family,
                                     int port, int flags, long p )
    throws Exception;
    public static native String getnameinfo ( long sa, int flags );
    public static native String getip ( long sa );
    public static native int getservbyname ( long sockaddr, String servname );
    public static native long get ( int which, long sock )
    throws Exception;
    public static native boolean equal ( long a, long b );
}
