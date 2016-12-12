package org.apache.catalina.valves;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
protected class DateAndTimeElement implements AccessLogElement {
    private static final String requestStartPrefix = "begin";
    private static final String responseEndPrefix = "end";
    private static final String prefixSeparator = ":";
    private static final String secFormat = "sec";
    private static final String msecFormat = "msec";
    private static final String msecFractionFormat = "msec_frac";
    private static final String msecPattern = "{#}";
    private static final String trippleMsecPattern = "{#}{#}{#}";
    private final String format;
    private final boolean usesBegin;
    private final FormatType type;
    private boolean usesMsecs;
    protected DateAndTimeElement ( final AbstractAccessLogValve this$0 ) {
        this ( this$0, null );
    }
    private String tidyFormat ( final String format ) {
        boolean escape = false;
        final StringBuilder result = new StringBuilder();
        for ( int len = format.length(), i = 0; i < len; ++i ) {
            final char x = format.charAt ( i );
            if ( escape || x != 'S' ) {
                result.append ( x );
            } else {
                result.append ( "{#}" );
                this.usesMsecs = true;
            }
            if ( x == '\'' ) {
                escape = !escape;
            }
        }
        return result.toString();
    }
    protected DateAndTimeElement ( final String header ) {
        this.usesMsecs = false;
        String format = header;
        boolean usesBegin = false;
        FormatType type = FormatType.CLF;
        if ( format != null ) {
            if ( format.equals ( "begin" ) ) {
                usesBegin = true;
                format = "";
            } else if ( format.startsWith ( "begin:" ) ) {
                usesBegin = true;
                format = format.substring ( 6 );
            } else if ( format.equals ( "end" ) ) {
                usesBegin = false;
                format = "";
            } else if ( format.startsWith ( "end:" ) ) {
                usesBegin = false;
                format = format.substring ( 4 );
            }
            if ( format.length() == 0 ) {
                type = FormatType.CLF;
            } else if ( format.equals ( "sec" ) ) {
                type = FormatType.SEC;
            } else if ( format.equals ( "msec" ) ) {
                type = FormatType.MSEC;
            } else if ( format.equals ( "msec_frac" ) ) {
                type = FormatType.MSEC_FRAC;
            } else {
                type = FormatType.SDF;
                format = this.tidyFormat ( format );
            }
        }
        this.format = format;
        this.usesBegin = usesBegin;
        this.type = type;
    }
    @Override
    public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
        long timestamp = date.getTime();
        if ( this.usesBegin ) {
            timestamp -= time;
        }
        switch ( this.type ) {
        case CLF: {
            buf.append ( AbstractAccessLogValve.access$600().get().getFormat ( timestamp ) );
            break;
        }
        case SEC: {
            buf.append ( Long.toString ( timestamp / 1000L ) );
            break;
        }
        case MSEC: {
            buf.append ( Long.toString ( timestamp ) );
            break;
        }
        case MSEC_FRAC: {
            final long frac = timestamp % 1000L;
            if ( frac < 100L ) {
                if ( frac < 10L ) {
                    buf.append ( '0' );
                    buf.append ( '0' );
                } else {
                    buf.append ( '0' );
                }
            }
            buf.append ( Long.toString ( frac ) );
            break;
        }
        case SDF: {
            String temp = AbstractAccessLogValve.access$600().get().getFormat ( this.format, AbstractAccessLogValve.this.locale, timestamp );
            if ( this.usesMsecs ) {
                final long frac = timestamp % 1000L;
                final StringBuilder trippleMsec = new StringBuilder ( 4 );
                if ( frac < 100L ) {
                    if ( frac < 10L ) {
                        trippleMsec.append ( '0' );
                        trippleMsec.append ( '0' );
                    } else {
                        trippleMsec.append ( '0' );
                    }
                }
                trippleMsec.append ( frac );
                temp = temp.replace ( "{#}{#}{#}", trippleMsec );
                temp = temp.replace ( "{#}", Long.toString ( frac ) );
            }
            buf.append ( temp );
            break;
        }
        }
    }
}
