package org.apache.tomcat.jni;
public class OS {
    private static final int UNIX      = 1;
    private static final int NETWARE   = 2;
    private static final int WIN32     = 3;
    private static final int WIN64     = 4;
    private static final int LINUX     = 5;
    private static final int SOLARIS   = 6;
    private static final int BSD       = 7;
    private static final int MACOSX    = 8;
    public static final int LOG_EMERG  = 1;
    public static final int LOG_ERROR  = 2;
    public static final int LOG_NOTICE = 3;
    public static final int LOG_WARN   = 4;
    public static final int LOG_INFO   = 5;
    public static final int LOG_DEBUG  = 6;
    private static native boolean is ( int type );
    public static final boolean IS_UNIX    = is ( UNIX );
    public static final boolean IS_NETWARE = is ( NETWARE );
    public static final boolean IS_WIN32   = is ( WIN32 );
    public static final boolean IS_WIN64   = is ( WIN64 );
    public static final boolean IS_LINUX   = is ( LINUX );
    public static final boolean IS_SOLARIS = is ( SOLARIS );
    public static final boolean IS_BSD     = is ( BSD );
    public static final boolean IS_MACOSX  = is ( MACOSX );
    public static native String defaultEncoding ( long pool );
    public static native String localeEncoding ( long pool );
    public static native int random ( byte [] buf, int len );
    public static native int info ( long [] inf );
    public static native String expand ( String str );
    public static native void sysloginit ( String domain );
    public static native void syslog ( int level, String message );
}