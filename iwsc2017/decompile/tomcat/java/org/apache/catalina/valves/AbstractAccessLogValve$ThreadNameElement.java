package org.apache.catalina.valves;
import org.apache.coyote.RequestInfo;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ThreadNameElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final RequestInfo info = request.getCoyoteRequest().getRequestProcessor();
        if ( info != null ) {
            buf.append ( info.getWorkerThreadName() );
        } else {
            buf.append ( "-" );
        }
    }
}
