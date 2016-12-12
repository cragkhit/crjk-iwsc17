package org.apache.tomcat.util.http;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.parser.Cookie;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.BitSet;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class Rfc6265CookieProcessor implements CookieProcessor {
    private static final Log log;
    private static final StringManager sm;
    private static final BitSet domainValid;
    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
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
                    if ( Rfc6265CookieProcessor.log.isDebugEnabled() ) {
                        final Exception e = new Exception();
                        Rfc6265CookieProcessor.log.debug ( "Cookies: Parsing cookie as String. Expected bytes.", e );
                    }
                    cookieValue.toBytes();
                }
                if ( Rfc6265CookieProcessor.log.isDebugEnabled() ) {
                    Rfc6265CookieProcessor.log.debug ( "Cookies: Parsing b[]: " + cookieValue.toString() );
                }
                final ByteChunk bc = cookieValue.getByteChunk();
                Cookie.parseCookie ( bc.getBytes(), bc.getOffset(), bc.getLength(), serverCookies );
            }
        }
    }
    @Override
    public String generateHeader ( final javax.servlet.http.Cookie cookie ) {
        final StringBuilder header = new StringBuilder();
        header.append ( cookie.getName() );
        header.append ( '=' );
        final String value = cookie.getValue();
        if ( value != null && value.length() > 0 ) {
            this.validateCookieValue ( value );
            header.append ( value );
        }
        final int maxAge = cookie.getMaxAge();
        if ( maxAge > -1 ) {
            header.append ( ";Max-Age=" );
            header.append ( maxAge );
        }
        final String domain = cookie.getDomain();
        if ( domain != null && domain.length() > 0 ) {
            this.validateDomain ( domain );
            header.append ( ";domain=" );
            header.append ( domain );
        }
        final String path = cookie.getPath();
        if ( path != null && path.length() > 0 ) {
            this.validatePath ( path );
            header.append ( ";path=" );
            header.append ( path );
        }
        if ( cookie.getSecure() ) {
            header.append ( ";Secure" );
        }
        if ( cookie.isHttpOnly() ) {
            header.append ( ";HttpOnly" );
        }
        return header.toString();
    }
    private void validateCookieValue ( final String value ) {
        int start = 0;
        int end = value.length();
        if ( end > 1 && value.charAt ( 0 ) == '\"' && value.charAt ( end - 1 ) == '\"' ) {
            start = 1;
            --end;
        }
        final char[] chars = value.toCharArray();
        for ( int i = start; i < end; ++i ) {
            final char c = chars[i];
            if ( c < '!' || c == '\"' || c == ',' || c == ';' || c == '\\' || c == '\u007f' ) {
                throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidCharInValue", Integer.toString ( c ) ) );
            }
        }
    }
    private void validateDomain ( final String domain ) {
        int i = 0;
        int prev = -1;
        int cur = -1;
        for ( char[] chars = domain.toCharArray(); i < chars.length; ++i ) {
            prev = cur;
            cur = chars[i];
            if ( !Rfc6265CookieProcessor.domainValid.get ( cur ) ) {
                throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidDomain", domain ) );
            }
            if ( ( prev == 46 || prev == -1 ) && ( cur == 46 || cur == 45 ) ) {
                throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidDomain", domain ) );
            }
            if ( prev == 45 && cur == 46 ) {
                throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidDomain", domain ) );
            }
        }
        if ( cur == 46 || cur == 45 ) {
            throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidDomain", domain ) );
        }
    }
    private void validatePath ( final String path ) {
        final char[] chars = path.toCharArray();
        for ( int i = 0; i < chars.length; ++i ) {
            final char ch = chars[i];
            if ( ch < ' ' || ch > '~' || ch == ';' ) {
                throw new IllegalArgumentException ( Rfc6265CookieProcessor.sm.getString ( "rfc6265CookieProcessor.invalidPath", path ) );
            }
        }
    }
    static {
        log = LogFactory.getLog ( Rfc6265CookieProcessor.class );
        sm = StringManager.getManager ( Rfc6265CookieProcessor.class.getPackage().getName() );
        domainValid = new BitSet ( 128 );
        for ( char c = '0'; c <= '9'; ++c ) {
            Rfc6265CookieProcessor.domainValid.set ( c );
        }
        for ( char c = 'a'; c <= 'z'; ++c ) {
            Rfc6265CookieProcessor.domainValid.set ( c );
        }
        for ( char c = 'A'; c <= 'Z'; ++c ) {
            Rfc6265CookieProcessor.domainValid.set ( c );
        }
        Rfc6265CookieProcessor.domainValid.set ( 46 );
        Rfc6265CookieProcessor.domainValid.set ( 45 );
    }
}
