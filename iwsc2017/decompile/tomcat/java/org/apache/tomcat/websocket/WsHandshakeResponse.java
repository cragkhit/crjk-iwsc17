package org.apache.tomcat.websocket;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import java.util.List;
import java.util.Map;
import javax.websocket.HandshakeResponse;
public class WsHandshakeResponse implements HandshakeResponse {
    private final Map<String, List<String>> headers;
    public WsHandshakeResponse() {
        this.headers = ( Map<String, List<String>> ) new CaseInsensitiveKeyMap();
    }
    public WsHandshakeResponse ( final Map<String, List<String>> headers ) {
        this.headers = ( Map<String, List<String>> ) new CaseInsensitiveKeyMap();
        for ( final Map.Entry<String, List<String>> entry : headers.entrySet() ) {
            if ( this.headers.containsKey ( entry.getKey() ) ) {
                this.headers.get ( entry.getKey() ).addAll ( entry.getValue() );
            } else {
                final List<String> values = new ArrayList<String> ( entry.getValue() );
                this.headers.put ( entry.getKey(), values );
            }
        }
    }
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }
}
