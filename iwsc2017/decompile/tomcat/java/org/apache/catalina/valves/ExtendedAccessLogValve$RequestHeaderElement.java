package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class RequestHeaderElement implements AccessLogElement {
    private final String header;
    public RequestHeaderElement ( final String header ) {
        this.header = header;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( ExtendedAccessLogValve.wrap ( request.getHeader ( this.header ) ) );
    }
}
