package org.apache.tomcat.jni;
public class Local {
    public static native long create ( String path, long cont )
    throws Exception;
    public static native int bind ( long sock, long sa );
    public static native int listen ( long sock, int backlog );
    public static native long accept ( long sock )
    throws Exception;
    public static native int connect ( long sock, long sa );
}
