package org.apache.catalina.webresources;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
static class BaseLocation {
    private final String basePath;
    private final String archivePath;
    BaseLocation ( final URL url ) {
        File f = null;
        if ( "jar".equals ( url.getProtocol() ) || "war".equals ( url.getProtocol() ) ) {
            final String jarUrl = url.toString();
            int endOfFileUrl = -1;
            if ( "jar".equals ( url.getProtocol() ) ) {
                endOfFileUrl = jarUrl.indexOf ( "!/" );
            } else {
                endOfFileUrl = jarUrl.indexOf ( "*/" );
            }
            final String fileUrl = jarUrl.substring ( 4, endOfFileUrl );
            try {
                f = new File ( new URL ( fileUrl ).toURI() );
            } catch ( MalformedURLException | URISyntaxException e ) {
                throw new IllegalArgumentException ( e );
            }
            final int startOfArchivePath = endOfFileUrl + 2;
            if ( jarUrl.length() > startOfArchivePath ) {
                this.archivePath = jarUrl.substring ( startOfArchivePath );
            } else {
                this.archivePath = null;
            }
        } else {
            if ( !"file".equals ( url.getProtocol() ) ) {
                throw new IllegalArgumentException ( StandardRoot.sm.getString ( "standardRoot.unsupportedProtocol", url.getProtocol() ) );
            }
            try {
                f = new File ( url.toURI() );
            } catch ( URISyntaxException e2 ) {
                throw new IllegalArgumentException ( e2 );
            }
            this.archivePath = null;
        }
        this.basePath = f.getAbsolutePath();
    }
    String getBasePath() {
        return this.basePath;
    }
    String getArchivePath() {
        return this.archivePath;
    }
}
