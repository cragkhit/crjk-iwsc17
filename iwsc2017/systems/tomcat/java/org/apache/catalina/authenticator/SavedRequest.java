package org.apache.catalina.authenticator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.http.Cookie;
import org.apache.tomcat.util.buf.ByteChunk;
public final class SavedRequest {
    private final ArrayList<Cookie> cookies = new ArrayList<>();
    public void addCookie ( Cookie cookie ) {
        cookies.add ( cookie );
    }
    public Iterator<Cookie> getCookies() {
        return ( cookies.iterator() );
    }
    private final HashMap<String, ArrayList<String>> headers = new HashMap<>();
    public void addHeader ( String name, String value ) {
        ArrayList<String> values = headers.get ( name );
        if ( values == null ) {
            values = new ArrayList<>();
            headers.put ( name, values );
        }
        values.add ( value );
    }
    public Iterator<String> getHeaderNames() {
        return ( headers.keySet().iterator() );
    }
    public Iterator<String> getHeaderValues ( String name ) {
        ArrayList<String> values = headers.get ( name );
        if ( values == null ) {
            return ( ( new ArrayList<String>() ).iterator() );
        } else {
            return ( values.iterator() );
        }
    }
    private final ArrayList<Locale> locales = new ArrayList<>();
    public void addLocale ( Locale locale ) {
        locales.add ( locale );
    }
    public Iterator<Locale> getLocales() {
        return ( locales.iterator() );
    }
    private String method = null;
    public String getMethod() {
        return ( this.method );
    }
    public void setMethod ( String method ) {
        this.method = method;
    }
    private String queryString = null;
    public String getQueryString() {
        return ( this.queryString );
    }
    public void setQueryString ( String queryString ) {
        this.queryString = queryString;
    }
    private String requestURI = null;
    public String getRequestURI() {
        return ( this.requestURI );
    }
    public void setRequestURI ( String requestURI ) {
        this.requestURI = requestURI;
    }
    private String decodedRequestURI = null;
    public String getDecodedRequestURI() {
        return ( this.decodedRequestURI );
    }
    public void setDecodedRequestURI ( String decodedRequestURI ) {
        this.decodedRequestURI = decodedRequestURI;
    }
    private ByteChunk body = null;
    public ByteChunk getBody() {
        return ( this.body );
    }
    public void setBody ( ByteChunk body ) {
        this.body = body;
    }
    private String contentType = null;
    public String getContentType() {
        return ( this.contentType );
    }
    public void setContentType ( String contentType ) {
        this.contentType = contentType;
    }
}
