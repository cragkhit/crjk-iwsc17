package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class StringElement implements AccessLogElement {
    private final String str;
    public StringElement ( final String str ) {
        this.str = str;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        buf.append ( this.str );
    }
}
