package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class FirstByteTimeElement implements AccessLogElement {
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final long commitTime = response.getCoyoteResponse().getCommitTime();
        if ( commitTime == -1L ) {
            buf.append ( '-' );
        } else {
            final long delta = commitTime - request.getCoyoteRequest().getStartTime();
            buf.append ( Long.toString ( delta ) );
        }
    }
}
