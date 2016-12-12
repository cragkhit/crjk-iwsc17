package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class RequestAttributeElement implements AccessLogElement {
    private final String attribute;
    public RequestAttributeElement ( final String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( ExtendedAccessLogValve.wrap ( request.getAttribute ( this.attribute ) ) );
    }
}
