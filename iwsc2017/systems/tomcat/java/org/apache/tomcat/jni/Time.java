package org.apache.tomcat.jni;
public class Time {
    public static final long APR_USEC_PER_SEC  = 1000000L;
    public static final long APR_MSEC_PER_USEC = 1000L;
    public static long sec ( long t ) {
        return t / APR_USEC_PER_SEC;
    }
    public static long msec ( long t ) {
        return t / APR_MSEC_PER_USEC;
    }
    public static native long now();
    public static native String rfc822 ( long t );
    public static native String ctime ( long t );
    public static native void sleep ( long t );
}
