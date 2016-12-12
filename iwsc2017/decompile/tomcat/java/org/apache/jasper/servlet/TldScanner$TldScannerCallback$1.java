package org.apache.jasper.servlet;
import java.net.URL;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import java.io.File;
import java.util.Locale;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
class TldScanner$TldScannerCallback$1 extends SimpleFileVisitor<Path> {
    final   String val$webappPath;
    final   Path val$filePath;
    @Override
    public FileVisitResult visitFile ( final Path file, final BasicFileAttributes attrs ) throws IOException {
        final Path fileName = file.getFileName();
        if ( fileName == null || !fileName.toString().toLowerCase ( Locale.ENGLISH ).endsWith ( ".tld" ) ) {
            return FileVisitResult.CONTINUE;
        }
        TldScannerCallback.access$102 ( TldScannerCallback.this, true );
        String resourcePath;
        if ( this.val$webappPath == null ) {
            resourcePath = null;
        } else {
            String subPath = file.subpath ( this.val$filePath.getNameCount(), file.getNameCount() ).toString();
            if ( '/' != File.separatorChar ) {
                subPath = subPath.replace ( File.separatorChar, '/' );
            }
            resourcePath = this.val$webappPath + "/" + subPath;
        }
        try {
            final URL url = file.toUri().toURL();
            final TldResourcePath path = new TldResourcePath ( url, resourcePath );
            TldScannerCallback.this.this$0.parseTld ( path );
        } catch ( SAXException e ) {
            throw new IOException ( e );
        }
        return FileVisitResult.CONTINUE;
    }
}
