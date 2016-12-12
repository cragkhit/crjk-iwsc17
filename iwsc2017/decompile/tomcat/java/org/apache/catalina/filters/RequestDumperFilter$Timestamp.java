package org.apache.catalina.filters;
import java.text.SimpleDateFormat;
import java.util.Date;
private static final class Timestamp {
    private final Date date;
    private final SimpleDateFormat format;
    private String dateString;
    private Timestamp() {
        this.date = new Date ( 0L );
        this.format = new SimpleDateFormat ( "dd-MMM-yyyy HH:mm:ss" );
        this.dateString = this.format.format ( this.date );
    }
    private void update() {
        this.dateString = this.format.format ( this.date );
    }
}
