package org.apache.catalina;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
public interface AccessLog {
    public static final String REMOTE_ADDR_ATTRIBUTE =
        "org.apache.catalina.AccessLog.RemoteAddr";
    public static final String REMOTE_HOST_ATTRIBUTE =
        "org.apache.catalina.AccessLog.RemoteHost";
    public static final String PROTOCOL_ATTRIBUTE =
        "org.apache.catalina.AccessLog.Protocol";
    public static final String SERVER_PORT_ATTRIBUTE =
        "org.apache.catalina.AccessLog.ServerPort";
    public void log ( Request request, Response response, long time );
    public void setRequestAttributesEnabled ( boolean requestAttributesEnabled );
    public boolean getRequestAttributesEnabled();
}
