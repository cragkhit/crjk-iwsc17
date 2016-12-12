package org.apache.catalina;
public interface SessionIdGenerator {
    public String getJvmRoute();
    public void setJvmRoute ( String jvmRoute );
    public int getSessionIdLength();
    public void setSessionIdLength ( int sessionIdLength );
    public String generateSessionId();
    public String generateSessionId ( String route );
}
