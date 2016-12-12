package org.apache.catalina;
public interface CredentialHandler {
    boolean matches ( String inputCredentials, String storedCredentials );
    String mutate ( String inputCredentials );
}
