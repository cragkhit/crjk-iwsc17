package org.apache.catalina.valves;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.InetAddress;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class LocalAddrElement implements AccessLogElement {
    private static final String LOCAL_ADDR_VALUE;
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( LocalAddrElement.LOCAL_ADDR_VALUE );
    }
    static {
        String init;
        try {
            init = InetAddress.getLocalHost().getHostAddress();
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            init = "127.0.0.1";
        }
        LOCAL_ADDR_VALUE = init;
    }
}
