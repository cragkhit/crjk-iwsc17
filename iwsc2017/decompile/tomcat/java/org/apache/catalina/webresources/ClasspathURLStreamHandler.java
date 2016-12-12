package org.apache.catalina.webresources;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.net.URL;
import org.apache.tomcat.util.res.StringManager;
import java.net.URLStreamHandler;
public class ClasspathURLStreamHandler extends URLStreamHandler {
    private static final StringManager sm;
    @Override
    protected URLConnection openConnection ( final URL u ) throws IOException {
        final String path = u.getPath();
        URL classpathUrl = Thread.currentThread().getContextClassLoader().getResource ( path );
        if ( classpathUrl == null ) {
            classpathUrl = ClasspathURLStreamHandler.class.getResource ( path );
        }
        if ( classpathUrl == null ) {
            throw new FileNotFoundException ( ClasspathURLStreamHandler.sm.getString ( "classpathUrlStreamHandler.notFound", u ) );
        }
        return classpathUrl.openConnection();
    }
    static {
        sm = StringManager.getManager ( ClasspathURLStreamHandler.class );
    }
}
