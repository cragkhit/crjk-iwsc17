package org.apache.tomcat.jni;
public class Error extends Exception {
    private static final long serialVersionUID = 1L;
    private final int error;
    private final String description;
    private Error ( final int error, final String description ) {
        super ( error + ": " + description );
        this.error = error;
        this.description = description;
    }
    public int getError() {
        return this.error;
    }
    public String getDescription() {
        return this.description;
    }
    public static native int osError();
    public static native int netosError();
    public static native String strerror ( final int p0 );
}
