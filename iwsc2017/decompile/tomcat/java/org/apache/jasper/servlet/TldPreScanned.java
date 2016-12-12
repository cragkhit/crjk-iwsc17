package org.apache.jasper.servlet;
import java.util.Iterator;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Collection;
public class TldPreScanned extends TldScanner {
    private final Collection<URL> preScannedURLs;
    public TldPreScanned ( final ServletContext context, final boolean namespaceAware, final boolean validation, final boolean blockExternal, final Collection<URL> preScannedTlds ) {
        super ( context, namespaceAware, validation, blockExternal );
        this.preScannedURLs = preScannedTlds;
    }
    @Override
    public void scanJars() {
        for ( final URL url : this.preScannedURLs ) {
            final String str = url.toExternalForm();
            final int a = str.indexOf ( "jar:" );
            final int b = str.indexOf ( "!/" );
            if ( a < 0 || b <= 0 ) {
                throw new IllegalStateException ( "Bad tld url: " + str );
            }
            final String fileUrl = str.substring ( a + 4, b );
            final String path = str.substring ( b + 2 );
            try {
                this.parseTld ( new TldResourcePath ( new URL ( fileUrl ), null, path ) );
            } catch ( Exception e ) {
                throw new IllegalStateException ( e );
            }
        }
    }
}
