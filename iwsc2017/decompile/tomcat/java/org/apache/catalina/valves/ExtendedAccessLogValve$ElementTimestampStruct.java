package org.apache.catalina.valves;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
private static class ElementTimestampStruct {
    private final Date currentTimestamp;
    private final SimpleDateFormat currentTimestampFormat;
    private String currentTimestampString;
    ElementTimestampStruct ( final String format ) {
        this.currentTimestamp = new Date ( 0L );
        ( this.currentTimestampFormat = new SimpleDateFormat ( format, Locale.US ) ).setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
    }
}
