package org.apache.tomcat.jni;
public class Procattr {
    public static native long create ( long cont )
    throws Error;
    public static native int ioSet ( long attr, int in, int out, int err );
    public static native int childInSet ( long attr, long in, long parent );
    public static native int childOutSet ( long attr, long out, long parent );
    public static native int childErrSet ( long attr, long err, long parent );
    public static native int dirSet ( long attr, String dir );
    public static native int cmdtypeSet ( long attr, int cmd );
    public static native int detachSet ( long attr, int detach );
    public static native int errorCheckSet ( long attr, int chk );
    public static native int addrspaceSet ( long attr, int addrspace );
    public static native void errfnSet ( long attr, long pool, Object o );
    public static native int userSet ( long attr, String username, String password );
    public static native int groupSet ( long attr, String groupname );
}
