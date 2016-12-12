package org.apache.tomcat.jni;
public interface CertificateVerifier {
    boolean verify ( long p0, byte[][] p1, String p2 );
}
