package org.apache.catalina;
public interface SessionIdGenerator {
    String getJvmRoute();
    void setJvmRoute ( String p0 );
    int getSessionIdLength();
    void setSessionIdLength ( int p0 );
    String generateSessionId();
    String generateSessionId ( String p0 );
}
