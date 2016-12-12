package org.apache.tomcat.jni;
public interface CertificateVerifier {
    boolean verify ( long ssl, byte[][] x509, String authAlgorithm );
}
