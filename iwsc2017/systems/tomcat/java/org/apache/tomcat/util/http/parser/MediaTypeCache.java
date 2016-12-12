package org.apache.tomcat.util.http.parser;
import java.io.IOException;
import java.io.StringReader;
import org.apache.tomcat.util.collections.ConcurrentCache;
public class MediaTypeCache {
    private final ConcurrentCache<String, String[]> cache;
    public MediaTypeCache ( int size ) {
        cache = new ConcurrentCache<> ( size );
    }
    public String[] parse ( String input ) {
        String[] result = cache.get ( input );
        if ( result != null ) {
            return result;
        }
        MediaType m = null;
        try {
            m = MediaType.parseMediaType ( new StringReader ( input ) );
        } catch ( IOException e ) {
        }
        if ( m != null ) {
            result = new String[] {m.toStringNoCharset(), m.getCharset() };
            cache.put ( input, result );
        }
        return result;
    }
}
