package org.apache.catalina.webresources.war;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
public class Handler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection ( final URL u ) throws IOException {
        return new WarURLConnection ( u );
    }
    @Override
    protected void setURL ( final URL u, final String protocol, final String host, final int port, final String authority, final String userInfo, String path, final String query, final String ref ) {
        if ( path.startsWith ( "file:" ) && !path.startsWith ( "file:/" ) ) {
            path = "file:/" + path.substring ( 5 );
        }
        super.setURL ( u, protocol, host, port, authority, userInfo, path, query, ref );
    }
}
