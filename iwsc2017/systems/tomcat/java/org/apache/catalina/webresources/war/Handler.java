package org.apache.catalina.webresources.war;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
public class Handler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection ( URL u ) throws IOException {
        return new WarURLConnection ( u );
    }
    @Override
    protected void setURL ( URL u, String protocol, String host, int port, String authority, String userInfo, String path,
                            String query, String ref ) {
        if ( path.startsWith ( "file:" ) && !path.startsWith ( "file:/" ) ) {
            path = "file:/" + path.substring ( 5 );
        }
        super.setURL ( u, protocol, host, port, authority, userInfo, path, query, ref );
    }
}
