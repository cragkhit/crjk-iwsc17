package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ServletContextElement implements AccessLogElement {
    private final String attribute;
    public ServletContextElement ( final String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( ExtendedAccessLogValve.wrap ( request.getContext().getServletContext().getAttribute ( this.attribute ) ) );
    }
}
