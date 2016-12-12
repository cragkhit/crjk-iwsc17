package org.apache.catalina;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
public interface AccessLog {
    public static final String REMOTE_ADDR_ATTRIBUTE = "org.apache.catalina.AccessLog.RemoteAddr";
    public static final String REMOTE_HOST_ATTRIBUTE = "org.apache.catalina.AccessLog.RemoteHost";
    public static final String PROTOCOL_ATTRIBUTE = "org.apache.catalina.AccessLog.Protocol";
    public static final String SERVER_PORT_ATTRIBUTE = "org.apache.catalina.AccessLog.ServerPort";
    void log ( Request p0, Response p1, long p2 );
    void setRequestAttributesEnabled ( boolean p0 );
    boolean getRequestAttributesEnabled();
}
