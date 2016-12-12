package org.apache.catalina.webresources.war;
import java.security.Permission;
import java.io.InputStream;
import java.io.IOException;
import org.apache.tomcat.util.buf.UriUtil;
import java.net.URL;
import java.net.URLConnection;
public class WarURLConnection extends URLConnection {
    private final URLConnection wrappedJarUrlConnection;
    private boolean connected;
    protected WarURLConnection ( final URL url ) throws IOException {
        super ( url );
        final URL innerJarUrl = UriUtil.warToJar ( url );
        this.wrappedJarUrlConnection = innerJarUrl.openConnection();
    }
    @Override
    public void connect() throws IOException {
        if ( !this.connected ) {
            this.wrappedJarUrlConnection.connect();
            this.connected = true;
        }
    }
    @Override
    public InputStream getInputStream() throws IOException {
        this.connect();
        return this.wrappedJarUrlConnection.getInputStream();
    }
    @Override
    public Permission getPermission() throws IOException {
        return this.wrappedJarUrlConnection.getPermission();
    }
}
