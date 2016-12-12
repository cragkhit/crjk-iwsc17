package org.apache.catalina;
public interface CredentialHandler {
    boolean matches ( String p0, String p1 );
    String mutate ( String p0 );
}
