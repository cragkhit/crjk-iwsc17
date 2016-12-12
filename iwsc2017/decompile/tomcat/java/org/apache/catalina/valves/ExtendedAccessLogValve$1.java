package org.apache.catalina.valves;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.InetAddress;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
class ExtendedAccessLogValve$1 implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        String value;
        try {
            value = InetAddress.getLocalHost().getHostName();
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            value = "localhost";
        }
        buf.append ( value );
    }
}
