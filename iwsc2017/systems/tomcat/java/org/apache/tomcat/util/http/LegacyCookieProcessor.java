package org.apache.tomcat.util.http;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.Cookie;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.tomcat.util.res.StringManager;
public final class LegacyCookieProcessor implements CookieProcessor {
    private static final Log log = LogFactory.getLog ( LegacyCookieProcessor.class );
    private static final UserDataHelper userDataLog = new UserDataHelper ( log );
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.tomcat.util.http" );
    private static final char[] V0_SEPARATORS = {',', ';', ' ', '\t'};
    private static final BitSet V0_SEPARATOR_FLAGS = new BitSet ( 128 );
    private static final char[] HTTP_SEPARATORS = new char[] {
        '\t', ' ', '\"', '(', ')', ',', ':', ';', '<', '=', '>', '?', '@',
        '[', '\\', ']', '{', '}'
    };
    private static final String COOKIE_DATE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> COOKIE_DATE_FORMAT =
    new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat df =
                new SimpleDateFormat ( COOKIE_DATE_PATTERN, Locale.US );
            df.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
            return df;
        }
    };
    private static final String ANCIENT_DATE;
    static {
        for ( char c : V0_SEPARATORS ) {
            V0_SEPARATOR_FLAGS.set ( c );
        }
        ANCIENT_DATE = COOKIE_DATE_FORMAT.get().format ( new Date ( 10000 ) );
    }
    private final boolean STRICT_SERVLET_COMPLIANCE =
        Boolean.getBoolean ( "org.apache.catalina.STRICT_SERVLET_COMPLIANCE" );
    private boolean allowEqualsInValue = false;
    private boolean allowNameOnly = false;
    private boolean allowHttpSepsInV0 = false;
    private boolean alwaysAddExpires = !STRICT_SERVLET_COMPLIANCE;
    private final BitSet httpSeparatorFlags = new BitSet ( 128 );
    private final BitSet allowedWithoutQuotes = new BitSet ( 128 );
    public LegacyCookieProcessor() {
        for ( char c : HTTP_SEPARATORS ) {
            httpSeparatorFlags.set ( c );
        }
        boolean b = STRICT_SERVLET_COMPLIANCE;
        if ( b ) {
            httpSeparatorFlags.set ( '/' );
        }
        String separators;
        if ( getAllowHttpSepsInV0() ) {
            separators = ",; ";
        } else {
            separators = "()<>@,;:\\\"/[]?={} \t";
        }
        allowedWithoutQuotes.set ( 0x20, 0x7f );
        for ( char ch : separators.toCharArray() ) {
            allowedWithoutQuotes.clear ( ch );
        }
        if ( !getAllowHttpSepsInV0() && !getForwardSlashIsSeparator() ) {
            allowedWithoutQuotes.set ( '/' );
        }
    }
    public boolean getAllowEqualsInValue() {
        return allowEqualsInValue;
    }
    public void setAllowEqualsInValue ( boolean allowEqualsInValue ) {
        this.allowEqualsInValue = allowEqualsInValue;
    }
    public boolean getAllowNameOnly() {
        return allowNameOnly;
    }
    public void setAllowNameOnly ( boolean allowNameOnly ) {
        this.allowNameOnly = allowNameOnly;
    }
    public boolean getAllowHttpSepsInV0() {
        return allowHttpSepsInV0;
    }
    public void setAllowHttpSepsInV0 ( boolean allowHttpSepsInV0 ) {
        this.allowHttpSepsInV0 = allowHttpSepsInV0;
        char[] seps = "()<>@:\\\"[]?={}\t".toCharArray();
        for ( char sep : seps ) {
            if ( allowHttpSepsInV0 ) {
                allowedWithoutQuotes.set ( sep );
            } else {
                allowedWithoutQuotes.clear ( sep );
            }
        }
        if ( getForwardSlashIsSeparator() && !allowHttpSepsInV0 ) {
            allowedWithoutQuotes.clear ( '/' );
        } else {
            allowedWithoutQuotes.set ( '/' );
        }
    }
    public boolean getForwardSlashIsSeparator() {
        return httpSeparatorFlags.get ( '/' );
    }
    public void setForwardSlashIsSeparator ( boolean forwardSlashIsSeparator ) {
        if ( forwardSlashIsSeparator ) {
            httpSeparatorFlags.set ( '/' );
        } else {
            httpSeparatorFlags.clear ( '/' );
        }
        if ( forwardSlashIsSeparator && !getAllowHttpSepsInV0() ) {
            allowedWithoutQuotes.clear ( '/' );
        } else {
            allowedWithoutQuotes.set ( '/' );
        }
    }
    public boolean getAlwaysAddExpires() {
        return alwaysAddExpires;
    }
    public void setAlwaysAddExpires ( boolean alwaysAddExpires ) {
        this.alwaysAddExpires = alwaysAddExpires;
    }
    @Override
    public Charset getCharset() {
        return StandardCharsets.ISO_8859_1;
    }
    @Override
    public void parseCookieHeader ( MimeHeaders headers, ServerCookies serverCookies ) {
        if ( headers == null ) {
            return;
        }
        int pos = headers.findHeader ( "Cookie", 0 );
        while ( pos >= 0 ) {
            MessageBytes cookieValue = headers.getValue ( pos );
            if ( cookieValue != null && !cookieValue.isNull() ) {
                if ( cookieValue.getType() != MessageBytes.T_BYTES ) {
                    Exception e = new Exception();
                    log.debug ( "Cookies: Parsing cookie as String. Expected bytes.", e );
                    cookieValue.toBytes();
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Cookies: Parsing b[]: " + cookieValue.toString() );
                }
                ByteChunk bc = cookieValue.getByteChunk();
                processCookieHeader ( bc.getBytes(), bc.getOffset(), bc.getLength(), serverCookies );
            }
            pos = headers.findHeader ( "Cookie", ++pos );
        }
    }
    @Override
    public String generateHeader ( Cookie cookie ) {
        int version = cookie.getVersion();
        String value = cookie.getValue();
        String path = cookie.getPath();
        String domain = cookie.getDomain();
        String comment = cookie.getComment();
        if ( version == 0 ) {
            if ( needsQuotes ( value, 0 ) || comment != null || needsQuotes ( path, 0 ) || needsQuotes ( domain, 0 ) ) {
                version = 1;
            }
        }
        StringBuffer buf = new StringBuffer();
        buf.append ( cookie.getName() );
        buf.append ( "=" );
        maybeQuote ( buf, value, version );
        if ( version == 1 ) {
            buf.append ( "; Version=1" );
            if ( comment != null ) {
                buf.append ( "; Comment=" );
                maybeQuote ( buf, comment, version );
            }
        }
        if ( domain != null ) {
            buf.append ( "; Domain=" );
            maybeQuote ( buf, domain, version );
        }
        int maxAge = cookie.getMaxAge();
        if ( maxAge >= 0 ) {
            if ( version > 0 ) {
                buf.append ( "; Max-Age=" );
                buf.append ( maxAge );
            }
            if ( version == 0 || getAlwaysAddExpires() ) {
                buf.append ( "; Expires=" );
                if ( maxAge == 0 ) {
                    buf.append ( ANCIENT_DATE );
                } else {
                    COOKIE_DATE_FORMAT.get().format (
                        new Date ( System.currentTimeMillis() + maxAge * 1000L ),
                        buf,
                        new FieldPosition ( 0 ) );
                }
            }
        }
        if ( path != null ) {
            buf.append ( "; Path=" );
            maybeQuote ( buf, path, version );
        }
        if ( cookie.getSecure() ) {
            buf.append ( "; Secure" );
        }
        if ( cookie.isHttpOnly() ) {
            buf.append ( "; HttpOnly" );
        }
        return buf.toString();
    }
    private void maybeQuote ( StringBuffer buf, String value, int version ) {
        if ( value == null || value.length() == 0 ) {
            buf.append ( "\"\"" );
        } else if ( alreadyQuoted ( value ) ) {
            buf.append ( '"' );
            escapeDoubleQuotes ( buf, value, 1, value.length() - 1 );
            buf.append ( '"' );
        } else if ( needsQuotes ( value, version ) ) {
            buf.append ( '"' );
            escapeDoubleQuotes ( buf, value, 0, value.length() );
            buf.append ( '"' );
        } else {
            buf.append ( value );
        }
    }
    private static void escapeDoubleQuotes ( StringBuffer b, String s, int beginIndex, int endIndex ) {
        if ( s.indexOf ( '"' ) == -1 && s.indexOf ( '\\' ) == -1 ) {
            b.append ( s );
            return;
        }
        for ( int i = beginIndex; i < endIndex; i++ ) {
            char c = s.charAt ( i );
            if ( c == '\\' ) {
                b.append ( '\\' ).append ( '\\' );
            } else if ( c == '"' ) {
                b.append ( '\\' ).append ( '"' );
            } else {
                b.append ( c );
            }
        }
    }
    private boolean needsQuotes ( String value, int version ) {
        if ( value == null ) {
            return false;
        }
        int i = 0;
        int len = value.length();
        if ( alreadyQuoted ( value ) ) {
            i++;
            len--;
        }
        for ( ; i < len; i++ ) {
            char c = value.charAt ( i );
            if ( ( c < 0x20 && c != '\t' ) || c >= 0x7f ) {
                throw new IllegalArgumentException (
                    "Control character in cookie value or attribute." );
            }
            if ( version == 0 && !allowedWithoutQuotes.get ( c ) ||
                    version == 1 && isHttpSeparator ( c ) ) {
                return true;
            }
        }
        return false;
    }
    private static boolean alreadyQuoted ( String value ) {
        return value.length() >= 2 &&
               value.charAt ( 0 ) == '\"' &&
               value.charAt ( value.length() - 1 ) == '\"';
    }
    private final void processCookieHeader ( byte bytes[], int off, int len,
            ServerCookies serverCookies ) {
        if ( len <= 0 || bytes == null ) {
            return;
        }
        int end = off + len;
        int pos = off;
        int nameStart = 0;
        int nameEnd = 0;
        int valueStart = 0;
        int valueEnd = 0;
        int version = 0;
        ServerCookie sc = null;
        boolean isSpecial;
        boolean isQuoted;
        while ( pos < end ) {
            isSpecial = false;
            isQuoted = false;
            while ( pos < end &&
                    ( isHttpSeparator ( ( char ) bytes[pos] ) &&
                      !getAllowHttpSepsInV0() ||
                      isV0Separator ( ( char ) bytes[pos] ) ||
                      isWhiteSpace ( bytes[pos] ) ) ) {
                pos++;
            }
            if ( pos >= end ) {
                return;
            }
            if ( bytes[pos] == '$' ) {
                isSpecial = true;
                pos++;
            }
            valueEnd = valueStart = nameStart = pos;
            pos = nameEnd = getTokenEndPosition ( bytes, pos, end, version, true );
            while ( pos < end && isWhiteSpace ( bytes[pos] ) ) {
                pos++;
            }
            if ( pos < ( end - 1 ) && bytes[pos] == '=' ) {
                do {
                    pos++;
                } while ( pos < end && isWhiteSpace ( bytes[pos] ) );
                if ( pos >= end ) {
                    return;
                }
                switch ( bytes[pos] ) {
                case '"':
                    isQuoted = true;
                    valueStart = pos + 1;
                    valueEnd = getQuotedValueEndPosition ( bytes, valueStart, end );
                    pos = valueEnd;
                    if ( pos >= end ) {
                        return;
                    }
                    break;
                case ';':
                case ',':
                    valueStart = valueEnd = -1;
                    break;
                default:
                    if ( version == 0 &&
                            !isV0Separator ( ( char ) bytes[pos] ) &&
                            getAllowHttpSepsInV0() ||
                            !isHttpSeparator ( ( char ) bytes[pos] ) ||
                            bytes[pos] == '=' ) {
                        valueStart = pos;
                        valueEnd = getTokenEndPosition ( bytes, valueStart, end, version, false );
                        pos = valueEnd;
                        if ( valueStart == valueEnd ) {
                            valueStart = -1;
                            valueEnd = -1;
                        }
                    } else  {
                        UserDataHelper.Mode logMode = userDataLog.getNextMode();
                        if ( logMode != null ) {
                            String message = sm.getString (
                                                 "cookies.invalidCookieToken" );
                            switch ( logMode ) {
                            case INFO_THEN_DEBUG:
                                message += sm.getString (
                                               "cookies.fallToDebug" );
                            case INFO:
                                log.info ( message );
                                break;
                            case DEBUG:
                                log.debug ( message );
                            }
                        }
                        while ( pos < end && bytes[pos] != ';' &&
                                bytes[pos] != ',' ) {
                            pos++;
                        }
                        pos++;
                        sc = null;
                        continue;
                    }
                }
            } else {
                valueStart = valueEnd = -1;
                pos = nameEnd;
            }
            while ( pos < end && isWhiteSpace ( bytes[pos] ) ) {
                pos++;
            }
            while ( pos < end && bytes[pos] != ';' && bytes[pos] != ',' ) {
                pos++;
            }
            pos++;
            if ( isSpecial ) {
                isSpecial = false;
                if ( equals ( "Version", bytes, nameStart, nameEnd ) &&
                        sc == null ) {
                    if ( bytes[valueStart] == '1' && valueEnd == ( valueStart + 1 ) ) {
                        version = 1;
                    } else {
                    }
                    continue;
                }
                if ( sc == null ) {
                    continue;
                }
                if ( equals ( "Domain", bytes, nameStart, nameEnd ) ) {
                    sc.getDomain().setBytes ( bytes,
                                              valueStart,
                                              valueEnd - valueStart );
                    continue;
                }
                if ( equals ( "Path", bytes, nameStart, nameEnd ) ) {
                    sc.getPath().setBytes ( bytes,
                                            valueStart,
                                            valueEnd - valueStart );
                    continue;
                }
                if ( equals ( "Port", bytes, nameStart, nameEnd ) ) {
                    continue;
                }
                if ( equals ( "CommentURL", bytes, nameStart, nameEnd ) ) {
                    continue;
                }
                UserDataHelper.Mode logMode = userDataLog.getNextMode();
                if ( logMode != null ) {
                    String message = sm.getString ( "cookies.invalidSpecial" );
                    switch ( logMode ) {
                    case INFO_THEN_DEBUG:
                        message += sm.getString ( "cookies.fallToDebug" );
                    case INFO:
                        log.info ( message );
                        break;
                    case DEBUG:
                        log.debug ( message );
                    }
                }
            } else {
                if ( valueStart == -1 && !getAllowNameOnly() ) {
                    continue;
                }
                sc = serverCookies.addCookie();
                sc.setVersion ( version );
                sc.getName().setBytes ( bytes, nameStart,
                                        nameEnd - nameStart );
                if ( valueStart != -1 ) {
                    sc.getValue().setBytes ( bytes, valueStart,
                                             valueEnd - valueStart );
                    if ( isQuoted ) {
                        unescapeDoubleQuotes ( sc.getValue().getByteChunk() );
                    }
                } else {
                    sc.getValue().setString ( "" );
                }
                continue;
            }
        }
    }
    private final int getTokenEndPosition ( byte bytes[], int off, int end,
                                            int version, boolean isName ) {
        int pos = off;
        while ( pos < end &&
                ( !isHttpSeparator ( ( char ) bytes[pos] ) ||
                  version == 0 && getAllowHttpSepsInV0() && bytes[pos] != '=' &&
                  !isV0Separator ( ( char ) bytes[pos] ) ||
                  !isName && bytes[pos] == '=' && getAllowEqualsInValue() ) ) {
            pos++;
        }
        if ( pos > end ) {
            return end;
        }
        return pos;
    }
    private boolean isHttpSeparator ( final char c ) {
        if ( c < 0x20 || c >= 0x7f ) {
            if ( c != 0x09 ) {
                throw new IllegalArgumentException (
                    "Control character in cookie value or attribute." );
            }
        }
        return httpSeparatorFlags.get ( c );
    }
    private static boolean isV0Separator ( final char c ) {
        if ( c < 0x20 || c >= 0x7f ) {
            if ( c != 0x09 ) {
                throw new IllegalArgumentException (
                    "Control character in cookie value or attribute." );
            }
        }
        return V0_SEPARATOR_FLAGS.get ( c );
    }
    private static final int getQuotedValueEndPosition ( byte bytes[], int off, int end ) {
        int pos = off;
        while ( pos < end ) {
            if ( bytes[pos] == '"' ) {
                return pos;
            } else if ( bytes[pos] == '\\' && pos < ( end - 1 ) ) {
                pos += 2;
            } else {
                pos++;
            }
        }
        return end;
    }
    private static final boolean equals ( String s, byte b[], int start, int end ) {
        int blen = end - start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = start;
        for ( int i = 0; i < blen; i++ ) {
            if ( b[boff++] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    private static final boolean isWhiteSpace ( final byte c ) {
        if ( c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' ) {
            return true;
        } else {
            return false;
        }
    }
    private static final void unescapeDoubleQuotes ( ByteChunk bc ) {
        if ( bc == null || bc.getLength() == 0 || bc.indexOf ( '"', 0 ) == -1 ) {
            return;
        }
        byte[] original = bc.getBuffer();
        int len = bc.getLength();
        byte[] copy = new byte[len];
        System.arraycopy ( original, bc.getStart(), copy, 0, len );
        int src = 0;
        int dest = 0;
        while ( src < len ) {
            if ( copy[src] == '\\' && src < len && copy[src + 1]  == '"' ) {
                src++;
            }
            copy[dest] = copy[src];
            dest ++;
            src ++;
        }
        bc.setBytes ( copy, 0, dest );
    }
}
