package javax.servlet.http;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.ResourceBundle;
@Deprecated
public class HttpUtils {
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings;
    public static Hashtable<String, String[]> parseQueryString ( final String s ) {
        String[] valArray = null;
        if ( s == null ) {
            throw new IllegalArgumentException();
        }
        final Hashtable<String, String[]> ht = new Hashtable<String, String[]>();
        final StringBuilder sb = new StringBuilder();
        final StringTokenizer st = new StringTokenizer ( s, "&" );
        while ( st.hasMoreTokens() ) {
            final String pair = st.nextToken();
            final int pos = pair.indexOf ( 61 );
            if ( pos == -1 ) {
                throw new IllegalArgumentException();
            }
            final String key = parseName ( pair.substring ( 0, pos ), sb );
            final String val = parseName ( pair.substring ( pos + 1, pair.length() ), sb );
            if ( ht.containsKey ( key ) ) {
                final String[] oldVals = ht.get ( key );
                valArray = new String[oldVals.length + 1];
                for ( int i = 0; i < oldVals.length; ++i ) {
                    valArray[i] = oldVals[i];
                }
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[] { val };
            }
            ht.put ( key, valArray );
        }
        return ht;
    }
    public static Hashtable<String, String[]> parsePostData ( final int len, final ServletInputStream in ) {
        if ( len <= 0 ) {
            return new Hashtable<String, String[]>();
        }
        if ( in == null ) {
            throw new IllegalArgumentException();
        }
        final byte[] postedBytes = new byte[len];
        try {
            int offset = 0;
            do {
                final int inputLen = in.read ( postedBytes, offset, len - offset );
                if ( inputLen <= 0 ) {
                    final String msg = HttpUtils.lStrings.getString ( "err.io.short_read" );
                    throw new IllegalArgumentException ( msg );
                }
                offset += inputLen;
            } while ( len - offset > 0 );
        } catch ( IOException e ) {
            throw new IllegalArgumentException ( e.getMessage(), e );
        }
        try {
            final String postedBody = new String ( postedBytes, 0, len, "8859_1" );
            return parseQueryString ( postedBody );
        } catch ( UnsupportedEncodingException e2 ) {
            throw new IllegalArgumentException ( e2.getMessage(), e2 );
        }
    }
    private static String parseName ( final String s, final StringBuilder sb ) {
        sb.setLength ( 0 );
        for ( int i = 0; i < s.length(); ++i ) {
            final char c = s.charAt ( i );
            switch ( c ) {
            case '+': {
                sb.append ( ' ' );
                continue;
            }
            case '%': {
                try {
                    sb.append ( ( char ) Integer.parseInt ( s.substring ( i + 1, i + 3 ), 16 ) );
                    i += 2;
                    continue;
                } catch ( NumberFormatException e ) {
                    throw new IllegalArgumentException();
                } catch ( StringIndexOutOfBoundsException e2 ) {
                    final String rest = s.substring ( i );
                    sb.append ( rest );
                    if ( rest.length() == 2 ) {
                        ++i;
                    }
                    continue;
                }
                break;
            }
            }
            sb.append ( c );
        }
        return sb.toString();
    }
    public static StringBuffer getRequestURL ( final HttpServletRequest req ) {
        final StringBuffer url = new StringBuffer();
        final String scheme = req.getScheme();
        final int port = req.getServerPort();
        final String urlPath = req.getRequestURI();
        url.append ( scheme );
        url.append ( "://" );
        url.append ( req.getServerName() );
        if ( ( scheme.equals ( "http" ) && port != 80 ) || ( scheme.equals ( "https" ) && port != 443 ) ) {
            url.append ( ':' );
            url.append ( req.getServerPort() );
        }
        url.append ( urlPath );
        return url;
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.http.LocalStrings" );
    }
}
