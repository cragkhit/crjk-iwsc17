package org.apache.tomcat.util.scan;
import java.net.MalformedURLException;
import java.io.IOException;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.Jar;
import java.net.URL;
public class JarFactory {
    public static Jar newInstance ( final URL url ) throws IOException {
        final String urlString = url.toString();
        if ( urlString.startsWith ( "jar:file:" ) ) {
            if ( urlString.endsWith ( "!/" ) ) {
                return new JarFileUrlJar ( url, true );
            }
            return new JarFileUrlNestedJar ( url );
        } else {
            if ( urlString.startsWith ( "war:file:" ) ) {
                final URL jarUrl = UriUtil.warToJar ( url );
                return new JarFileUrlNestedJar ( jarUrl );
            }
            if ( urlString.startsWith ( "file:" ) ) {
                return new JarFileUrlJar ( url, false );
            }
            return new UrlJar ( url );
        }
    }
    public static URL getJarEntryURL ( final URL baseUrl, final String entryName ) throws MalformedURLException {
        String baseExternal = baseUrl.toExternalForm();
        if ( baseExternal.startsWith ( "jar" ) ) {
            baseExternal = baseExternal.replaceFirst ( "^jar:", "war:" );
            baseExternal = baseExternal.replaceFirst ( "!/", "*/" );
        }
        return new URL ( "jar:" + baseExternal + "!/" + entryName );
    }
}
