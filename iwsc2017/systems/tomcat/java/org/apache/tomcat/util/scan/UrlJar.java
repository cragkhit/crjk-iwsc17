package org.apache.tomcat.util.scan;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
public class UrlJar extends AbstractInputStreamJar {
    public UrlJar ( URL jarFileURL ) {
        super ( jarFileURL );
    }
    @Override
    public void close() {
        closeStream();
    }
    @Override
    protected NonClosingJarInputStream createJarInputStream() throws IOException {
        JarURLConnection jarConn = ( JarURLConnection ) getJarFileURL().openConnection();
        URL resourceURL = jarConn.getJarFileURL();
        URLConnection resourceConn = resourceURL.openConnection();
        resourceConn.setUseCaches ( false );
        return new NonClosingJarInputStream ( resourceConn.getInputStream() );
    }
}
