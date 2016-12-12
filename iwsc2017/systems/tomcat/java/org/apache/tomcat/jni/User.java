package org.apache.tomcat.jni;
public class User {
    public static native long uidCurrent ( long p )
    throws Error;
    public static native long gidCurrent ( long p )
    throws Error;
    public static native long uid ( String username, long p )
    throws Error;
    public static native long usergid ( String username, long p )
    throws Error;
    public static native long gid ( String groupname, long p )
    throws Error;
    public static native String username ( long userid, long p )
    throws Error;
    public static native String groupname ( long groupid, long p )
    throws Error;
    public static native int uidcompare ( long left, long right );
    public static native int gidcompare ( long left, long right );
    public static native String homepath ( String username, long p )
    throws Error;
}
