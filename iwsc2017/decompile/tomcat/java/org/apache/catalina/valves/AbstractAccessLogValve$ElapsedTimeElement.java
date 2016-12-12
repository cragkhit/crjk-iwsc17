package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class ElapsedTimeElement implements AccessLogElement {
    private final boolean millis;
    public ElapsedTimeElement ( final boolean millis ) {
        this.millis = millis;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        if ( this.millis ) {
            buf.append ( Long.toString ( time ) );
        } else {
            buf.append ( Long.toString ( time / 1000L ) );
            buf.append ( '.' );
            int remains = ( int ) ( time % 1000L );
            buf.append ( Long.toString ( remains / 100 ) );
            remains %= 100;
            buf.append ( Long.toString ( remains / 10 ) );
            buf.append ( Long.toString ( remains % 10 ) );
        }
    }
}
