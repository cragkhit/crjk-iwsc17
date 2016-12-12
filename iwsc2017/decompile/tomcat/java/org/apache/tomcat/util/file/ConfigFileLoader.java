package org.apache.tomcat.util.file;
import java.net.URL;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.io.File;
import org.apache.tomcat.util.res.StringManager;
public class ConfigFileLoader {
    private static final StringManager sm;
    private static final File CATALINA_BASE_FILE;
    private static final URI CATALINA_BASE_URI;
    public static InputStream getInputStream ( final String location ) throws IOException {
        File f = new File ( location );
        if ( !f.isAbsolute() ) {
            f = new File ( ConfigFileLoader.CATALINA_BASE_FILE, location );
        }
        if ( f.isFile() ) {
            return new FileInputStream ( f );
        }
        URI uri;
        if ( ConfigFileLoader.CATALINA_BASE_URI != null ) {
            uri = ConfigFileLoader.CATALINA_BASE_URI.resolve ( location );
        } else {
            uri = URI.create ( location );
        }
        try {
            final URL url = uri.toURL();
            return url.openConnection().getInputStream();
        } catch ( IllegalArgumentException e ) {
            throw new IOException ( ConfigFileLoader.sm.getString ( "configFileLoader.cannotObtainURL", location ), e );
        }
    }
    static {
        sm = StringManager.getManager ( ConfigFileLoader.class.getPackage().getName() );
        final String catalinaBase = System.getProperty ( "catalina.base" );
        if ( catalinaBase != null ) {
            CATALINA_BASE_FILE = new File ( catalinaBase );
            CATALINA_BASE_URI = ConfigFileLoader.CATALINA_BASE_FILE.toURI();
        } else {
            CATALINA_BASE_FILE = null;
            CATALINA_BASE_URI = null;
        }
    }
}
