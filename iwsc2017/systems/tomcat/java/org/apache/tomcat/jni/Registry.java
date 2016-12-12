package org.apache.tomcat.jni;
public class Registry {
    public static final int HKEY_CLASSES_ROOT       = 1;
    public static final int HKEY_CURRENT_CONFIG     = 2;
    public static final int HKEY_CURRENT_USER       = 3;
    public static final int HKEY_LOCAL_MACHINE      = 4;
    public static final int HKEY_USERS              = 5;
    public static final int KEY_ALL_ACCESS          = 0x0001;
    public static final int KEY_CREATE_LINK         = 0x0002;
    public static final int KEY_CREATE_SUB_KEY      = 0x0004;
    public static final int KEY_ENUMERATE_SUB_KEYS  = 0x0008;
    public static final int KEY_EXECUTE             = 0x0010;
    public static final int KEY_NOTIFY              = 0x0020;
    public static final int KEY_QUERY_VALUE         = 0x0040;
    public static final int KEY_READ                = 0x0080;
    public static final int KEY_SET_VALUE           = 0x0100;
    public static final int KEY_WOW64_64KEY         = 0x0200;
    public static final int KEY_WOW64_32KEY         = 0x0400;
    public static final int KEY_WRITE               = 0x0800;
    public static final int REG_BINARY              = 1;
    public static final int REG_DWORD               = 2;
    public static final int REG_EXPAND_SZ           = 3;
    public static final int REG_MULTI_SZ            = 4;
    public static final int REG_QWORD               = 5;
    public static final int REG_SZ                  = 6;
    public static native long create ( int root, String name, int sam, long pool )
    throws Error;
    public static native long open ( int root, String name, int sam, long pool )
    throws Error;
    public static native int close ( long key );
    public static native int getType ( long key, String name );
    public static native int getValueI ( long key, String name )
    throws Error;
    public static native long getValueJ ( long key, String name )
    throws Error;
    public static native int getSize ( long key, String name );
    public static native String getValueS ( long key, String name )
    throws Error;
    public static native String[] getValueA ( long key, String name )
    throws Error;
    public static native byte[] getValueB ( long key, String name )
    throws Error;
    public static native int setValueI ( long key, String name, int val );
    public static native int setValueJ ( long key, String name, long val );
    public static native int setValueS ( long key, String name, String val );
    public static native int setValueE ( long key, String name, String val );
    public static native int setValueA ( long key, String name, String[] val );
    public static native int setValueB ( long key, String name, byte[] val );
    public static native String[] enumKeys ( long key )
    throws Error;
    public static native String[] enumValues ( long key )
    throws Error;
    public static native int deleteValue ( long key, String name );
    public static native int deleteKey ( int root, String name,
                                         boolean onlyIfEmpty );
}
