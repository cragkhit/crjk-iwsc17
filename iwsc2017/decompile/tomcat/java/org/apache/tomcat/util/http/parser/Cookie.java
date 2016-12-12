package org.apache.tomcat.util.http.parser;
import org.apache.juli.logging.LogFactory;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.http.ServerCookie;
import org.apache.tomcat.util.http.ServerCookies;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.juli.logging.Log;
public class Cookie {
    private static final Log log;
    private static final UserDataHelper invalidCookieVersionLog;
    private static final UserDataHelper invalidCookieLog;
    private static final StringManager sm;
    private static final boolean[] isCookieOctet;
    private static final boolean[] isText;
    private static final byte[] VERSION_BYTES;
    private static final byte[] PATH_BYTES;
    private static final byte[] DOMAIN_BYTES;
    private static final byte[] EMPTY_BYTES;
    private static final byte TAB_BYTE = 9;
    private static final byte SPACE_BYTE = 32;
    private static final byte QUOTE_BYTE = 34;
    private static final byte COMMA_BYTE = 44;
    private static final byte FORWARDSLASH_BYTE = 47;
    private static final byte SEMICOLON_BYTE = 59;
    private static final byte EQUALS_BYTE = 61;
    private static final byte SLASH_BYTE = 92;
    private static final byte DEL_BYTE = Byte.MAX_VALUE;
    public static void parseCookie ( final byte[] bytes, final int offset, final int len, final ServerCookies serverCookies ) {
        final ByteBuffer bb = new ByteBuffer ( bytes, offset, len );
        skipLWS ( bb );
        final int mark = bb.position();
        SkipResult skipResult = skipBytes ( bb, Cookie.VERSION_BYTES );
        if ( skipResult != SkipResult.FOUND ) {
            parseCookieRfc6265 ( bb, serverCookies );
            return;
        }
        skipLWS ( bb );
        skipResult = skipByte ( bb, ( byte ) 61 );
        if ( skipResult != SkipResult.FOUND ) {
            bb.position ( mark );
            parseCookieRfc6265 ( bb, serverCookies );
            return;
        }
        skipLWS ( bb );
        final ByteBuffer value = readCookieValue ( bb );
        if ( value != null && value.remaining() == 1 ) {
            if ( value.get() == 49 ) {
                skipLWS ( bb );
                final byte b = bb.get();
                if ( b == 59 || b == 44 ) {
                    parseCookieRfc2109 ( bb, serverCookies );
                }
                return;
            }
            value.rewind();
            logInvalidVersion ( value );
        } else {
            logInvalidVersion ( value );
        }
    }
    public static String unescapeCookieValueRfc2109 ( final String input ) {
        if ( input == null || input.length() < 2 ) {
            return input;
        }
        if ( input.charAt ( 0 ) != '\"' && input.charAt ( input.length() - 1 ) != '\"' ) {
            return input;
        }
        final StringBuilder sb = new StringBuilder ( input.length() );
        final char[] chars = input.toCharArray();
        boolean escaped = false;
        for ( int i = 1; i < input.length() - 1; ++i ) {
            if ( chars[i] == '\\' ) {
                escaped = true;
            } else if ( escaped ) {
                escaped = false;
                if ( chars[i] < '\u0080' ) {
                    sb.append ( chars[i] );
                } else {
                    sb.append ( '\\' );
                    sb.append ( chars[i] );
                }
            } else {
                sb.append ( chars[i] );
            }
        }
        return sb.toString();
    }
    private static void parseCookieRfc6265 ( final ByteBuffer bb, final ServerCookies serverCookies ) {
        boolean moreToProcess = true;
        while ( moreToProcess ) {
            skipLWS ( bb );
            final ByteBuffer name = readToken ( bb );
            ByteBuffer value = null;
            skipLWS ( bb );
            SkipResult skipResult = skipByte ( bb, ( byte ) 61 );
            if ( skipResult == SkipResult.FOUND ) {
                skipLWS ( bb );
                value = readCookieValueRfc6265 ( bb );
                if ( value == null ) {
                    logInvalidHeader ( bb );
                    skipUntilSemiColon ( bb );
                    continue;
                }
                skipLWS ( bb );
            }
            skipResult = skipByte ( bb, ( byte ) 59 );
            if ( skipResult != SkipResult.FOUND ) {
                if ( skipResult == SkipResult.NOT_FOUND ) {
                    logInvalidHeader ( bb );
                    skipUntilSemiColon ( bb );
                    continue;
                }
                moreToProcess = false;
            }
            if ( name.hasRemaining() ) {
                final ServerCookie sc = serverCookies.addCookie();
                sc.getName().setBytes ( name.array(), name.position(), name.remaining() );
                if ( value == null ) {
                    sc.getValue().setBytes ( Cookie.EMPTY_BYTES, 0, Cookie.EMPTY_BYTES.length );
                } else {
                    sc.getValue().setBytes ( value.array(), value.position(), value.remaining() );
                }
            }
        }
    }
    private static void parseCookieRfc2109 ( final ByteBuffer bb, final ServerCookies serverCookies ) {
        boolean moreToProcess = true;
        while ( moreToProcess ) {
            skipLWS ( bb );
            boolean parseAttributes = true;
            final ByteBuffer name = readToken ( bb );
            ByteBuffer value = null;
            ByteBuffer path = null;
            ByteBuffer domain = null;
            skipLWS ( bb );
            SkipResult skipResult = skipByte ( bb, ( byte ) 61 );
            if ( skipResult == SkipResult.FOUND ) {
                skipLWS ( bb );
                value = readCookieValueRfc2109 ( bb, false );
                if ( value == null ) {
                    skipInvalidCookie ( bb );
                    continue;
                }
                skipLWS ( bb );
            }
            skipResult = skipByte ( bb, ( byte ) 44 );
            if ( skipResult == SkipResult.FOUND ) {
                parseAttributes = false;
            }
            skipResult = skipByte ( bb, ( byte ) 59 );
            if ( skipResult == SkipResult.EOF ) {
                parseAttributes = false;
                moreToProcess = false;
            } else if ( skipResult == SkipResult.NOT_FOUND ) {
                skipInvalidCookie ( bb );
                continue;
            }
            if ( parseAttributes ) {
                skipResult = skipBytes ( bb, Cookie.PATH_BYTES );
                if ( skipResult == SkipResult.FOUND ) {
                    skipLWS ( bb );
                    skipResult = skipByte ( bb, ( byte ) 61 );
                    if ( skipResult != SkipResult.FOUND ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                    path = readCookieValueRfc2109 ( bb, true );
                    if ( path == null ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                    skipLWS ( bb );
                    skipResult = skipByte ( bb, ( byte ) 44 );
                    if ( skipResult == SkipResult.FOUND ) {
                        parseAttributes = false;
                    }
                    skipResult = skipByte ( bb, ( byte ) 59 );
                    if ( skipResult == SkipResult.EOF ) {
                        parseAttributes = false;
                        moreToProcess = false;
                    } else if ( skipResult == SkipResult.NOT_FOUND ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                }
            }
            if ( parseAttributes ) {
                skipResult = skipBytes ( bb, Cookie.DOMAIN_BYTES );
                if ( skipResult == SkipResult.FOUND ) {
                    skipLWS ( bb );
                    skipResult = skipByte ( bb, ( byte ) 61 );
                    if ( skipResult != SkipResult.FOUND ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                    domain = readCookieValueRfc2109 ( bb, false );
                    if ( domain == null ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                    skipResult = skipByte ( bb, ( byte ) 44 );
                    if ( skipResult == SkipResult.FOUND ) {
                        parseAttributes = false;
                    }
                    skipResult = skipByte ( bb, ( byte ) 59 );
                    if ( skipResult == SkipResult.EOF ) {
                        parseAttributes = false;
                        moreToProcess = false;
                    } else if ( skipResult == SkipResult.NOT_FOUND ) {
                        skipInvalidCookie ( bb );
                        continue;
                    }
                }
            }
            if ( name.hasRemaining() && value != null && value.hasRemaining() ) {
                final ServerCookie sc = serverCookies.addCookie();
                sc.setVersion ( 1 );
                sc.getName().setBytes ( name.array(), name.position(), name.remaining() );
                sc.getValue().setBytes ( value.array(), value.position(), value.remaining() );
                if ( domain != null ) {
                    sc.getDomain().setBytes ( domain.array(), domain.position(), domain.remaining() );
                }
                if ( path == null ) {
                    continue;
                }
                sc.getPath().setBytes ( path.array(), path.position(), path.remaining() );
            }
        }
    }
    private static void skipInvalidCookie ( final ByteBuffer bb ) {
        logInvalidHeader ( bb );
        skipUntilSemiColonOrComma ( bb );
    }
    private static void skipLWS ( final ByteBuffer bb ) {
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( b != 9 && b != 32 ) {
                bb.rewind();
                break;
            }
        }
    }
    private static void skipUntilSemiColon ( final ByteBuffer bb ) {
        while ( bb.hasRemaining() && bb.get() != 59 ) {}
    }
    private static void skipUntilSemiColonOrComma ( final ByteBuffer bb ) {
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( b == 59 ) {
                break;
            }
            if ( b == 44 ) {
                break;
            }
        }
    }
    private static SkipResult skipByte ( final ByteBuffer bb, final byte target ) {
        if ( !bb.hasRemaining() ) {
            return SkipResult.EOF;
        }
        if ( bb.get() == target ) {
            return SkipResult.FOUND;
        }
        bb.rewind();
        return SkipResult.NOT_FOUND;
    }
    private static SkipResult skipBytes ( final ByteBuffer bb, final byte[] target ) {
        final int mark = bb.position();
        for ( int i = 0; i < target.length; ++i ) {
            if ( !bb.hasRemaining() ) {
                bb.position ( mark );
                return SkipResult.EOF;
            }
            if ( bb.get() != target[i] ) {
                bb.position ( mark );
                return SkipResult.NOT_FOUND;
            }
        }
        return SkipResult.FOUND;
    }
    private static ByteBuffer readCookieValue ( final ByteBuffer bb ) {
        boolean quoted = false;
        if ( bb.hasRemaining() ) {
            if ( bb.get() == 34 ) {
                quoted = true;
            } else {
                bb.rewind();
            }
        }
        final int start = bb.position();
        int end = bb.limit();
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( Cookie.isCookieOctet[b & 0xFF] ) {
                continue;
            }
            if ( b == 59 || b == 44 || b == 32 || b == 9 ) {
                end = bb.position() - 1;
                bb.position ( end );
                break;
            }
            if ( quoted && b == 34 ) {
                end = bb.position() - 1;
                break;
            }
            return null;
        }
        return new ByteBuffer ( bb.bytes, start, end - start );
    }
    private static ByteBuffer readCookieValueRfc6265 ( final ByteBuffer bb ) {
        boolean quoted = false;
        if ( bb.hasRemaining() ) {
            if ( bb.get() == 34 ) {
                quoted = true;
            } else {
                bb.rewind();
            }
        }
        final int start = bb.position();
        int end = bb.limit();
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( Cookie.isCookieOctet[b & 0xFF] ) {
                continue;
            }
            if ( b == 59 || b == 32 || b == 9 ) {
                end = bb.position() - 1;
                bb.position ( end );
                break;
            }
            if ( quoted && b == 34 ) {
                end = bb.position() - 1;
                break;
            }
            return null;
        }
        return new ByteBuffer ( bb.bytes, start, end - start );
    }
    private static ByteBuffer readCookieValueRfc2109 ( final ByteBuffer bb, final boolean allowForwardSlash ) {
        if ( !bb.hasRemaining() ) {
            return null;
        }
        if ( bb.peek() == 34 ) {
            return readQuotedString ( bb );
        }
        if ( allowForwardSlash ) {
            return readTokenAllowForwardSlash ( bb );
        }
        return readToken ( bb );
    }
    private static ByteBuffer readToken ( final ByteBuffer bb ) {
        final int start = bb.position();
        int end = bb.limit();
        while ( bb.hasRemaining() ) {
            if ( !HttpParser.isToken ( bb.get() ) ) {
                end = bb.position() - 1;
                bb.position ( end );
                break;
            }
        }
        return new ByteBuffer ( bb.bytes, start, end - start );
    }
    private static ByteBuffer readTokenAllowForwardSlash ( final ByteBuffer bb ) {
        final int start = bb.position();
        int end = bb.limit();
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( b != 47 && !HttpParser.isToken ( b ) ) {
                end = bb.position() - 1;
                bb.position ( end );
                break;
            }
        }
        return new ByteBuffer ( bb.bytes, start, end - start );
    }
    private static ByteBuffer readQuotedString ( final ByteBuffer bb ) {
        final int start = bb.position();
        bb.get();
        boolean escaped = false;
        while ( bb.hasRemaining() ) {
            final byte b = bb.get();
            if ( b == 92 ) {
                escaped = true;
            } else if ( escaped && b > -1 ) {
                escaped = false;
            } else {
                if ( b == 34 ) {
                    return new ByteBuffer ( bb.bytes, start, bb.position() - start );
                }
                if ( !Cookie.isText[b & 0xFF] ) {
                    return null;
                }
                escaped = false;
            }
        }
        return null;
    }
    private static void logInvalidHeader ( final ByteBuffer bb ) {
        final UserDataHelper.Mode logMode = Cookie.invalidCookieLog.getNextMode();
        if ( logMode != null ) {
            final String headerValue = new String ( bb.array(), bb.position(), bb.limit() - bb.position(), StandardCharsets.UTF_8 );
            String message = Cookie.sm.getString ( "cookie.invalidCookieValue", headerValue );
            switch ( logMode ) {
            case INFO_THEN_DEBUG: {
                message += Cookie.sm.getString ( "cookie.fallToDebug" );
            }
            case INFO: {
                Cookie.log.info ( message );
                break;
            }
            case DEBUG: {
                Cookie.log.debug ( message );
                break;
            }
            }
        }
    }
    private static void logInvalidVersion ( final ByteBuffer value ) {
        final UserDataHelper.Mode logMode = Cookie.invalidCookieVersionLog.getNextMode();
        if ( logMode != null ) {
            String version;
            if ( value == null ) {
                version = Cookie.sm.getString ( "cookie.valueNotPresent" );
            } else {
                version = new String ( value.bytes, value.position(), value.limit() - value.position(), StandardCharsets.UTF_8 );
            }
            String message = Cookie.sm.getString ( "cookie.invalidCookieVersion", version );
            switch ( logMode ) {
            case INFO_THEN_DEBUG: {
                message += Cookie.sm.getString ( "cookie.fallToDebug" );
            }
            case INFO: {
                Cookie.log.info ( message );
                break;
            }
            case DEBUG: {
                Cookie.log.debug ( message );
                break;
            }
            }
        }
    }
    static {
        log = LogFactory.getLog ( Cookie.class );
        invalidCookieVersionLog = new UserDataHelper ( Cookie.log );
        invalidCookieLog = new UserDataHelper ( Cookie.log );
        sm = StringManager.getManager ( "org.apache.tomcat.util.http.parser" );
        isCookieOctet = new boolean[256];
        isText = new boolean[256];
        VERSION_BYTES = "$Version".getBytes ( StandardCharsets.ISO_8859_1 );
        PATH_BYTES = "$Path".getBytes ( StandardCharsets.ISO_8859_1 );
        DOMAIN_BYTES = "$Domain".getBytes ( StandardCharsets.ISO_8859_1 );
        EMPTY_BYTES = new byte[0];
        for ( int i = 0; i < 256; ++i ) {
            if ( i < 33 || i == 34 || i == 44 || i == 59 || i == 92 || i == 127 ) {
                Cookie.isCookieOctet[i] = false;
            } else {
                Cookie.isCookieOctet[i] = true;
            }
        }
        for ( int i = 0; i < 256; ++i ) {
            if ( i < 9 || ( i > 9 && i < 32 ) || i == 127 ) {
                Cookie.isText[i] = false;
            } else {
                Cookie.isText[i] = true;
            }
        }
    }
    private static class ByteBuffer {
        private final byte[] bytes;
        private int limit;
        private int position;
        public ByteBuffer ( final byte[] bytes, final int offset, final int len ) {
            this.position = 0;
            this.bytes = bytes;
            this.position = offset;
            this.limit = offset + len;
        }
        public int position() {
            return this.position;
        }
        public void position ( final int position ) {
            this.position = position;
        }
        public int limit() {
            return this.limit;
        }
        public int remaining() {
            return this.limit - this.position;
        }
        public boolean hasRemaining() {
            return this.position < this.limit;
        }
        public byte get() {
            return this.bytes[this.position++];
        }
        public byte peek() {
            return this.bytes[this.position];
        }
        public void rewind() {
            --this.position;
        }
        public byte[] array() {
            return this.bytes;
        }
        @Override
        public String toString() {
            return "position [" + this.position + "], limit [" + this.limit + "]";
        }
    }
}
