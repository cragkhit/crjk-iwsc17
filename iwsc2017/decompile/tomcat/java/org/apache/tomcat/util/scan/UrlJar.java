package org.apache.tomcat.util.scan;
import java.io.IOException;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.net.URL;
public class UrlJar extends AbstractInputStreamJar {
    public UrlJar ( final URL jarFileURL ) {
        super ( jarFileURL );
    }
    @Override
    public void close() {
        this.closeStream();
    }
    @Override
    protected NonClosingJarInputStream createJarInputStream() throws IOException {
        final JarURLConnection jarConn = ( JarURLConnection ) this.getJarFileURL().openConnection();
        final URL resourceURL = jarConn.getJarFileURL();
        final URLConnection resourceConn = resourceURL.openConnection();
        resourceConn.setUseCaches ( false );
        return new NonClosingJarInputStream ( resourceConn.getInputStream() );
    }
}
