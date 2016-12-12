package org.apache.tomcat.util.http;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.juli.logging.LogFactory;
import java.text.FieldPosition;
import java.util.Date;
import javax.servlet.http.Cookie;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.BitSet;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.juli.logging.Log;
public final class LegacyCookieProcessor implements CookieProcessor {
    private static final Log log;
    private static final UserDataHelper userDataLog;
    private static final StringManager sm;
    private static final char[] V0_SEPARATORS;
    private static final BitSet V0_SEPARATOR_FLAGS;
    private static final char[] HTTP_SEPARATORS;
    private static final String COOKIE_DATE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> COOKIE_DATE_FORMAT;
    private static final String ANCIENT_DATE;
    private final boolean STRICT_SERVLET_COMPLIANCE;
    private boolean allowEqualsInValue;
    private boolean allowNameOnly;
    private boolean allowHttpSepsInV0;
    private boolean alwaysAddExpires;
    private final BitSet httpSeparatorFlags;
    private final BitSet allowedWithoutQuotes;
    public LegacyCookieProcessor() {
        this.STRICT_SERVLET_COMPLIANCE = Boolean.getBoolean ( "org.apache.catalina.STRICT_SERVLET_COMPLIANCE" );
        this.allowEqualsInValue = false;
        this.allowNameOnly = false;
        this.allowHttpSepsInV0 = false;
        this.alwaysAddExpires = !this.STRICT_SERVLET_COMPLIANCE;
        this.httpSeparatorFlags = new BitSet ( 128 );
        this.allowedWithoutQuotes = new BitSet ( 128 );
        for ( final char c : LegacyCookieProcessor.HTTP_SEPARATORS ) {
            this.httpSeparatorFlags.set ( c );
        }
        final boolean b = this.STRICT_SERVLET_COMPLIANCE;
        if ( b ) {
            this.httpSeparatorFlags.set ( 47 );
        }
        String separators;
        if ( this.getAllowHttpSepsInV0() ) {
            separators = ",; ";
        } else {
            separators = "()<>@,;:\\\"/[]?={} \t";
        }
        this.allowedWithoutQuotes.set ( 32, 127 );
        for ( final char ch : separators.toCharArray() ) {
            this.allowedWithoutQuotes.clear ( ch );
        }
        if ( !this.getAllowHttpSepsInV0() && !this.getForwardSlashIsSeparator() ) {
            this.allowedWithoutQuotes.set ( 47 );
        }
    }
    public boolean getAllowEqualsInValue() {
        return this.allowEqualsInValue;
    }
    public void setAllowEqualsInValue ( final boolean allowEqualsInValue ) {
        this.allowEqualsInValue = allowEqualsInValue;
    }
    public boolean getAllowNameOnly() {
        return this.allowNameOnly;
    }
    public void setAllowNameOnly ( final boolean allowNameOnly ) {
        this.allowNameOnly = allowNameOnly;
    }
    public boolean getAllowHttpSepsInV0() {
        return this.allowHttpSepsInV0;
    }
    public void setAllowHttpSepsInV0 ( final boolean allowHttpSepsInV0 ) {
        this.allowHttpSepsInV0 = allowHttpSepsInV0;
        final char[] charArray;
        final char[] seps = charArray = "()<>@:\\\"[]?={}\t".toCharArray();
        for ( final char sep : charArray ) {
            if ( allowHttpSepsInV0 ) {
                this.allowedWithoutQuotes.set ( sep );
            } else {
                this.allowedWithoutQuotes.clear ( sep );
            }
        }
        if ( this.getForwardSlashIsSeparator() && !allowHttpSepsInV0 ) {
            this.allowedWithoutQuotes.clear ( 47 );
        } else {
            this.allowedWithoutQuotes.set ( 47 );
        }
    }
    public boolean getForwardSlashIsSeparator() {
        return this.httpSeparatorFlags.get ( 47 );
    }
    public void setForwardSlashIsSeparator ( final boolean forwardSlashIsSeparator ) {
        if ( forwardSlashIsSeparator ) {
            this.httpSeparatorFlags.set ( 47 );
        } else {
            this.httpSeparatorFlags.clear ( 47 );
        }
        if ( forwardSlashIsSeparator && !this.getAllowHttpSepsInV0() ) {
            this.allowedWithoutQuotes.clear ( 47 );
        } else {
            this.allowedWithoutQuotes.set ( 47 );
        }
    }
    public boolean getAlwaysAddExpires() {
        return this.alwaysAddExpires;
    }
    public void setAlwaysAddExpires ( final boolean alwaysAddExpires ) {
        this.alwaysAddExpires = alwaysAddExpires;
    }
    @Override
    public Charset getCharset() {
        return StandardCharsets.ISO_8859_1;
    }
    @Override
    public void parseCookieHeader ( final MimeHeaders headers, final ServerCookies serverCookies ) {
        if ( headers == null ) {
            return;
        }
        for ( int pos = headers.findHeader ( "Cookie", 0 ); pos >= 0; pos = headers.findHeader ( "Cookie", ++pos ) ) {
            final MessageBytes cookieValue = headers.getValue ( pos );
            if ( cookieValue != null && !cookieValue.isNull() ) {
                if ( cookieValue.getType() != 2 ) {
                    final Exception e = new Exception();
                    LegacyCookieProcessor.log.debug ( "Cookies: Parsing cookie as String. Expected bytes.", e );
                    cookieValue.toBytes();
                }
                if ( LegacyCookieProcessor.log.isDebugEnabled() ) {
                    LegacyCookieProcessor.log.debug ( "Cookies: Parsing b[]: " + cookieValue.toString() );
                }
                final ByteChunk bc = cookieValue.getByteChunk();
                this.processCookieHeader ( bc.getBytes(), bc.getOffset(), bc.getLength(), serverCookies );
            }
        }
    }
    @Override
    public String generateHeader ( final Cookie cookie ) {
        int version = cookie.getVersion();
        final String value = cookie.getValue();
        final String path = cookie.getPath();
        final String domain = cookie.getDomain();
        final String comment = cookie.getComment();
        if ( version == 0 && ( this.needsQuotes ( value, 0 ) || comment != null || this.needsQuotes ( path, 0 ) || this.needsQuotes ( domain, 0 ) ) ) {
            version = 1;
        }
        final StringBuffer buf = new StringBuffer();
        buf.append ( cookie.getName() );
        buf.append ( "=" );
        this.maybeQuote ( buf, value, version );
        if ( version == 1 ) {
            buf.append ( "; Version=1" );
            if ( comment != null ) {
                buf.append ( "; Comment=" );
                this.maybeQuote ( buf, comment, version );
            }
        }
        if ( domain != null ) {
            buf.append ( "; Domain=" );
            this.maybeQuote ( buf, domain, version );
        }
        final int maxAge = cookie.getMaxAge();
        if ( maxAge >= 0 ) {
            if ( version > 0 ) {
                buf.append ( "; Max-Age=" );
                buf.append ( maxAge );
            }
            if ( version == 0 || this.getAlwaysAddExpires() ) {
                buf.append ( "; Expires=" );
                if ( maxAge == 0 ) {
                    buf.append ( LegacyCookieProcessor.ANCIENT_DATE );
                } else {
                    LegacyCookieProcessor.COOKIE_DATE_FORMAT.get().format ( new Date ( System.currentTimeMillis() + maxAge * 1000L ), buf, new FieldPosition ( 0 ) );
                }
            }
        }
        if ( path != null ) {
            buf.append ( "; Path=" );
            this.maybeQuote ( buf, path, version );
        }
        if ( cookie.getSecure() ) {
            buf.append ( "; Secure" );
        }
        if ( cookie.isHttpOnly() ) {
            buf.append ( "; HttpOnly" );
        }
        return buf.toString();
    }
    private void maybeQuote ( final StringBuffer buf, final String value, final int version ) {
        if ( value == null || value.length() == 0 ) {
            buf.append ( "\"\"" );
        } else if ( alreadyQuoted ( value ) ) {
            buf.append ( '\"' );
            escapeDoubleQuotes ( buf, value, 1, value.length() - 1 );
            buf.append ( '\"' );
        } else if ( this.needsQuotes ( value, version ) ) {
            buf.append ( '\"' );
            escapeDoubleQuotes ( buf, value, 0, value.length() );
            buf.append ( '\"' );
        } else {
            buf.append ( value );
        }
    }
    private static void escapeDoubleQuotes ( final StringBuffer b, final String s, final int beginIndex, final int endIndex ) {
        if ( s.indexOf ( 34 ) == -1 && s.indexOf ( 92 ) == -1 ) {
            b.append ( s );
            return;
        }
        for ( int i = beginIndex; i < endIndex; ++i ) {
            final char c = s.charAt ( i );
            if ( c == '\\' ) {
                b.append ( '\\' ).append ( '\\' );
            } else if ( c == '\"' ) {
                b.append ( '\\' ).append ( '\"' );
            } else {
                b.append ( c );
            }
        }
    }
    private boolean needsQuotes ( final String value, final int version ) {
        if ( value == null ) {
            return false;
        }
        int i = 0;
        int len = value.length();
        if ( alreadyQuoted ( value ) ) {
            ++i;
            --len;
        }
        while ( i < len ) {
            final char c = value.charAt ( i );
            if ( ( c < ' ' && c != '\t' ) || c >= '\u007f' ) {
                throw new IllegalArgumentException ( "Control character in cookie value or attribute." );
            }
            if ( ( version == 0 && !this.allowedWithoutQuotes.get ( c ) ) || ( version == 1 && this.isHttpSeparator ( c ) ) ) {
                return true;
            }
            ++i;
        }
        return false;
    }
    private static boolean alreadyQuoted ( final String value ) {
        return value.length() >= 2 && value.charAt ( 0 ) == '\"' && value.charAt ( value.length() - 1 ) == '\"';
    }
    private final void processCookieHeader ( final byte[] bytes, final int off, final int len, final ServerCookies serverCookies ) {
        if ( len <= 0 || bytes == null ) {
            return;
        }
        final int end = off + len;
        int pos = off;
        int nameStart = 0;
        int nameEnd = 0;
        int valueStart = 0;
        int valueEnd = 0;
        int version = 0;
        ServerCookie sc = null;
        while ( pos < end ) {
            boolean isSpecial = false;
            boolean isQuoted = false;
            while ( pos < end && ( ( this.isHttpSeparator ( ( char ) bytes[pos] ) && !this.getAllowHttpSepsInV0() ) || isV0Separator ( ( char ) bytes[pos] ) || isWhiteSpace ( bytes[pos] ) ) ) {
                ++pos;
            }
            if ( pos >= end ) {
                return;
            }
            if ( bytes[pos] == 36 ) {
                isSpecial = true;
                ++pos;
            }
            valueStart = ( valueEnd = ( nameStart = pos ) );
            for ( nameEnd = ( pos = this.getTokenEndPosition ( bytes, pos, end, version, true ) ); pos < end && isWhiteSpace ( bytes[pos] ); ++pos ) {}
            if ( pos < end - 1 && bytes[pos] == 61 ) {
                while ( ++pos < end && isWhiteSpace ( bytes[pos] ) ) {}
                if ( pos >= end ) {
                    return;
                }
                switch ( bytes[pos] ) {
                case 34: {
                    isQuoted = true;
                    valueStart = pos + 1;
                    valueEnd = ( pos = getQuotedValueEndPosition ( bytes, valueStart, end ) );
                    if ( pos >= end ) {
                        return;
                    }
                    break;
                }
                case 44:
                case 59: {
                    valueEnd = ( valueStart = -1 );
                    break;
                }
                default: {
                    if ( ( version != 0 || isV0Separator ( ( char ) bytes[pos] ) || !this.getAllowHttpSepsInV0() ) && this.isHttpSeparator ( ( char ) bytes[pos] ) && bytes[pos] != 61 ) {
                        final UserDataHelper.Mode logMode = LegacyCookieProcessor.userDataLog.getNextMode();
                        if ( logMode != null ) {
                            String message = LegacyCookieProcessor.sm.getString ( "cookies.invalidCookieToken" );
                            switch ( logMode ) {
                            case INFO_THEN_DEBUG: {
                                message += LegacyCookieProcessor.sm.getString ( "cookies.fallToDebug" );
                            }
                            case INFO: {
                                LegacyCookieProcessor.log.info ( message );
                                break;
                            }
                            case DEBUG: {
                                LegacyCookieProcessor.log.debug ( message );
                                break;
                            }
                            }
                        }
                        while ( pos < end && bytes[pos] != 59 && bytes[pos] != 44 ) {
                            ++pos;
                        }
                        ++pos;
                        sc = null;
                        continue;
                    }
                    valueStart = pos;
                    valueEnd = this.getTokenEndPosition ( bytes, valueStart, end, version, false );
                    if ( valueStart == ( pos = valueEnd ) ) {
                        valueStart = -1;
                        valueEnd = -1;
                        break;
                    }
                    break;
                }
                }
            } else {
                valueEnd = ( valueStart = -1 );
                pos = nameEnd;
            }
            while ( pos < end && isWhiteSpace ( bytes[pos] ) ) {
                ++pos;
            }
            while ( pos < end && bytes[pos] != 59 && bytes[pos] != 44 ) {
                ++pos;
            }
            ++pos;
            if ( isSpecial ) {
                isSpecial = false;
                if ( equals ( "Version", bytes, nameStart, nameEnd ) && sc == null ) {
                    if ( bytes[valueStart] != 49 || valueEnd != valueStart + 1 ) {
                        continue;
                    }
                    version = 1;
                } else {
                    if ( sc == null ) {
                        continue;
                    }
                    if ( equals ( "Domain", bytes, nameStart, nameEnd ) ) {
                        sc.getDomain().setBytes ( bytes, valueStart, valueEnd - valueStart );
                    } else if ( equals ( "Path", bytes, nameStart, nameEnd ) ) {
                        sc.getPath().setBytes ( bytes, valueStart, valueEnd - valueStart );
                    } else {
                        if ( equals ( "Port", bytes, nameStart, nameEnd ) ) {
                            continue;
                        }
                        if ( equals ( "CommentURL", bytes, nameStart, nameEnd ) ) {
                            continue;
                        }
                        final UserDataHelper.Mode logMode = LegacyCookieProcessor.userDataLog.getNextMode();
                        if ( logMode == null ) {
                            continue;
                        }
                        String message = LegacyCookieProcessor.sm.getString ( "cookies.invalidSpecial" );
                        switch ( logMode ) {
                        case INFO_THEN_DEBUG: {
                            message += LegacyCookieProcessor.sm.getString ( "cookies.fallToDebug" );
                        }
                        case INFO: {
                            LegacyCookieProcessor.log.info ( message );
                            continue;
                        }
                        case DEBUG: {
                            LegacyCookieProcessor.log.debug ( message );
                            continue;
                        }
                        }
                    }
                }
            } else {
                if ( valueStart == -1 && !this.getAllowNameOnly() ) {
                    continue;
                }
                sc = serverCookies.addCookie();
                sc.setVersion ( version );
                sc.getName().setBytes ( bytes, nameStart, nameEnd - nameStart );
                if ( valueStart != -1 ) {
                    sc.getValue().setBytes ( bytes, valueStart, valueEnd - valueStart );
                    if ( !isQuoted ) {
                        continue;
                    }
                    unescapeDoubleQuotes ( sc.getValue().getByteChunk() );
                } else {
                    sc.getValue().setString ( "" );
                }
            }
        }
    }
    private final int getTokenEndPosition ( final byte[] bytes, final int off, final int end, final int version, final boolean isName ) {
        int pos;
        for ( pos = off; pos < end && ( !this.isHttpSeparator ( ( char ) bytes[pos] ) || ( version == 0 && this.getAllowHttpSepsInV0() && bytes[pos] != 61 && !isV0Separator ( ( char ) bytes[pos] ) ) || ( !isName && bytes[pos] == 61 && this.getAllowEqualsInValue() ) ); ++pos ) {}
        if ( pos > end ) {
            return end;
        }
        return pos;
    }
    private boolean isHttpSeparator ( final char c ) {
        if ( ( c < ' ' || c >= '\u007f' ) && c != '\t' ) {
            throw new IllegalArgumentException ( "Control character in cookie value or attribute." );
        }
        return this.httpSeparatorFlags.get ( c );
    }
    private static boolean isV0Separator ( final char c ) {
        if ( ( c < ' ' || c >= '\u007f' ) && c != '\t' ) {
            throw new IllegalArgumentException ( "Control character in cookie value or attribute." );
        }
        return LegacyCookieProcessor.V0_SEPARATOR_FLAGS.get ( c );
    }
    private static final int getQuotedValueEndPosition ( final byte[] bytes, final int off, final int end ) {
        int pos = off;
        while ( pos < end ) {
            if ( bytes[pos] == 34 ) {
                return pos;
            }
            if ( bytes[pos] == 92 && pos < end - 1 ) {
                pos += 2;
            } else {
                ++pos;
            }
        }
        return end;
    }
    private static final boolean equals ( final String s, final byte[] b, final int start, final int end ) {
        final int blen = end - start;
        if ( b == null || blen != s.length() ) {
            return false;
        }
        int boff = start;
        for ( int i = 0; i < blen; ++i ) {
            if ( b[boff++] != s.charAt ( i ) ) {
                return false;
            }
        }
        return true;
    }
    private static final boolean isWhiteSpace ( final byte c ) {
        return c == 32 || c == 9 || c == 10 || c == 13 || c == 12;
    }
    private static final void unescapeDoubleQuotes ( final ByteChunk bc ) {
        if ( bc == null || bc.getLength() == 0 || bc.indexOf ( '\"', 0 ) == -1 ) {
            return;
        }
        final byte[] original = bc.getBuffer();
        final int len = bc.getLength();
        final byte[] copy = new byte[len];
        System.arraycopy ( original, bc.getStart(), copy, 0, len );
        int src = 0;
        int dest = 0;
        while ( src < len ) {
            if ( copy[src] == 92 && src < len && copy[src + 1] == 34 ) {
                ++src;
            }
            copy[dest] = copy[src];
            ++dest;
            ++src;
        }
        bc.setBytes ( copy, 0, dest );
    }
    static {
        log = LogFactory.getLog ( LegacyCookieProcessor.class );
        userDataLog = new UserDataHelper ( LegacyCookieProcessor.log );
        sm = StringManager.getManager ( "org.apache.tomcat.util.http" );
        V0_SEPARATORS = new char[] { ',', ';', ' ', '\t' };
        V0_SEPARATOR_FLAGS = new BitSet ( 128 );
        HTTP_SEPARATORS = new char[] { '\t', ' ', '\"', '(', ')', ',', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}' };
        COOKIE_DATE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue() {
                final DateFormat df = new SimpleDateFormat ( "EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US );
                df.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
                return df;
            }
        };
        for ( final char c : LegacyCookieProcessor.V0_SEPARATORS ) {
            LegacyCookieProcessor.V0_SEPARATOR_FLAGS.set ( c );
        }
        ANCIENT_DATE = LegacyCookieProcessor.COOKIE_DATE_FORMAT.get().format ( new Date ( 10000L ) );
    }
}
