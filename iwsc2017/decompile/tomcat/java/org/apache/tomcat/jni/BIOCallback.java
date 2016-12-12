package org.apache.tomcat.jni;
public interface BIOCallback {
    int write ( byte[] p0 );
    int read ( byte[] p0 );
    int puts ( String p0 );
    String gets ( int p0 );
}
