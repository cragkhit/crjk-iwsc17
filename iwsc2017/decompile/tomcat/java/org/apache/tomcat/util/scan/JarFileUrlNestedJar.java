package org.apache.tomcat.util.scan;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
public class JarFileUrlNestedJar extends AbstractInputStreamJar {
    private final JarFile warFile;
    private final JarEntry jarEntry;
    public JarFileUrlNestedJar ( final URL url ) throws IOException {
        super ( url );
        final JarURLConnection jarConn = ( JarURLConnection ) url.openConnection();
        jarConn.setUseCaches ( false );
        this.warFile = jarConn.getJarFile();
        final String urlAsString = url.toString();
        final int pathStart = urlAsString.indexOf ( "!/" ) + 2;
        final String jarPath = urlAsString.substring ( pathStart );
        this.jarEntry = this.warFile.getJarEntry ( jarPath );
    }
    @Override
    public void close() {
        this.closeStream();
        if ( this.warFile != null ) {
            try {
                this.warFile.close();
            } catch ( IOException ex ) {}
        }
    }
    @Override
    protected NonClosingJarInputStream createJarInputStream() throws IOException {
        return new NonClosingJarInputStream ( this.warFile.getInputStream ( this.jarEntry ) );
    }
}
