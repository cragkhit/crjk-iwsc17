package org.apache.catalina.util;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Locale;
public class ConcurrentDateFormat {
    private final String format;
    private final Locale locale;
    private final TimeZone timezone;
    private final Queue<SimpleDateFormat> queue;
    public static final String RFC1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final TimeZone GMT;
    private static final ConcurrentDateFormat FORMAT_RFC1123;
    public static String formatRfc1123 ( final Date date ) {
        return ConcurrentDateFormat.FORMAT_RFC1123.format ( date );
    }
    public ConcurrentDateFormat ( final String format, final Locale locale, final TimeZone timezone ) {
        this.queue = new ConcurrentLinkedQueue<SimpleDateFormat>();
        this.format = format;
        this.locale = locale;
        this.timezone = timezone;
        final SimpleDateFormat initial = this.createInstance();
        this.queue.add ( initial );
    }
    public String format ( final Date date ) {
        SimpleDateFormat sdf = this.queue.poll();
        if ( sdf == null ) {
            sdf = this.createInstance();
        }
        final String result = sdf.format ( date );
        this.queue.add ( sdf );
        return result;
    }
    private SimpleDateFormat createInstance() {
        final SimpleDateFormat sdf = new SimpleDateFormat ( this.format, this.locale );
        sdf.setTimeZone ( this.timezone );
        return sdf;
    }
    static {
        GMT = TimeZone.getTimeZone ( "GMT" );
        FORMAT_RFC1123 = new ConcurrentDateFormat ( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US, ConcurrentDateFormat.GMT );
    }
}
