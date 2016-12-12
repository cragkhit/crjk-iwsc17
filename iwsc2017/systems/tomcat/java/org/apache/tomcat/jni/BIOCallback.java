package org.apache.tomcat.jni;
public interface BIOCallback {
    public int write ( byte [] buf );
    public int read ( byte [] buf );
    public int puts ( String data );
    public String gets ( int len );
}
