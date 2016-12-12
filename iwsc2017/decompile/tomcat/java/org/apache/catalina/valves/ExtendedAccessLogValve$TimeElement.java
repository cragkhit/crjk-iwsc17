package org.apache.catalina.valves;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected static class TimeElement implements AccessLogElement {
    private static final long INTERVAL = 1000L;
    private static final ThreadLocal<ElementTimestampStruct> currentTime;
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        final ElementTimestampStruct eds = TimeElement.currentTime.get();
        final long millis = eds.currentTimestamp.getTime();
        if ( date.getTime() > millis + 1000L - 1L || date.getTime() < millis ) {
            eds.currentTimestamp.setTime ( date.getTime() - date.getTime() % 1000L );
            eds.currentTimestampString = eds.currentTimestampFormat.format ( eds.currentTimestamp );
        }
        buf.append ( eds.currentTimestampString );
    }
    static {
        currentTime = new ThreadLocal<ElementTimestampStruct>() {
            @Override
            protected ElementTimestampStruct initialValue() {
                return new ElementTimestampStruct ( "HH:mm:ss" );
            }
        };
    }
}
