package org.apache.tomcat.util.http;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.DateFormat;
static final class LegacyCookieProcessor$1 extends ThreadLocal<DateFormat> {
    @Override
    protected DateFormat initialValue() {
        final DateFormat df = new SimpleDateFormat ( "EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US );
        df.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        return df;
    }
}
