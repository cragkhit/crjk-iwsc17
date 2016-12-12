package javax.servlet.http;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.servlet.ServletInputStream;
@Deprecated
public class HttpUtils {
    private static final String LSTRING_FILE =
        "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle ( LSTRING_FILE );
    public HttpUtils() {
    }
    public static Hashtable<String, String[]> parseQueryString ( String s ) {
        String valArray[] = null;
        if ( s == null ) {
            throw new IllegalArgumentException();
        }
        Hashtable<String, String[]> ht = new Hashtable<>();
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer ( s, "&" );
        while ( st.hasMoreTokens() ) {
            String pair = st.nextToken();
            int pos = pair.indexOf ( '=' );
            if ( pos == -1 ) {
                throw new IllegalArgumentException();
            }
            String key = parseName ( pair.substring ( 0, pos ), sb );
            String val = parseName ( pair.substring ( pos + 1, pair.length() ), sb );
            if ( ht.containsKey ( key ) ) {
                String oldVals[] = ht.get ( key );
                valArray = new String[oldVals.length + 1];
                for ( int i = 0; i < oldVals.length; i++ ) {
                    valArray[i] = oldVals[i];
                }
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            ht.put ( key, valArray );
        }
        return ht;
    }
    public static Hashtable<String, String[]> parsePostData ( int len,
            ServletInputStream in ) {
        if ( len <= 0 ) {
            return new Hashtable<>();
        }
        if ( in == null ) {
            throw new IllegalArgumentException();
        }
        byte[] postedBytes = new byte [len];
        try {
            int offset = 0;
            do {
                int inputLen = in.read ( postedBytes, offset, len - offset );
                if ( inputLen <= 0 ) {
                    String msg = lStrings.getString ( "err.io.short_read" );
                    throw new IllegalArgumentException ( msg );
                }
                offset += inputLen;
            } while ( ( len - offset ) > 0 );
        } catch ( IOException e ) {
            throw new IllegalArgumentException ( e.getMessage(), e );
        }
        try {
            String postedBody = new String ( postedBytes, 0, len, "8859_1" );
            return parseQueryString ( postedBody );
        } catch ( java.io.UnsupportedEncodingException e ) {
            throw new IllegalArgumentException ( e.getMessage(), e );
        }
    }
    private static String parseName ( String s, StringBuilder sb ) {
        sb.setLength ( 0 );
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt ( i );
            switch ( c ) {
            case '+':
                sb.append ( ' ' );
                break;
            case '%':
                try {
                    sb.append ( ( char ) Integer.parseInt ( s.substring ( i + 1, i + 3 ),
                                                            16 ) );
                    i += 2;
                } catch ( NumberFormatException e ) {
                    throw new IllegalArgumentException();
                } catch ( StringIndexOutOfBoundsException e ) {
                    String rest  = s.substring ( i );
                    sb.append ( rest );
                    if ( rest.length() == 2 ) {
                        i++;
                    }
                }
                break;
            default:
                sb.append ( c );
                break;
            }
        }
        return sb.toString();
    }
    public static StringBuffer getRequestURL ( HttpServletRequest req ) {
        StringBuffer url = new StringBuffer ();
        String scheme = req.getScheme ();
        int port = req.getServerPort ();
        String urlPath = req.getRequestURI();
        url.append ( scheme );
        url.append ( "://" );
        url.append ( req.getServerName () );
        if ( ( scheme.equals ( "http" ) && port != 80 ) || ( scheme.equals ( "https" ) && port != 443 ) ) {
            url.append ( ':' );
            url.append ( req.getServerPort () );
        }
        url.append ( urlPath );
        return url;
    }
}
