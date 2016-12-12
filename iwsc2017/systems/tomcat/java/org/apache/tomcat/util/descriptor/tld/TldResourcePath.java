package org.apache.tomcat.util.descriptor.tld;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.scan.JarFactory;
public class TldResourcePath {
    private final URL url;
    private final String webappPath;
    private final String entryName;
    public TldResourcePath ( URL url, String webappPath ) {
        this ( url, webappPath, null );
    }
    public TldResourcePath ( URL url, String webappPath, String entryName ) {
        this.url = url;
        this.webappPath = webappPath;
        this.entryName = entryName;
    }
    public URL getUrl() {
        return url;
    }
    public String getWebappPath() {
        return webappPath;
    }
    public String getEntryName() {
        return entryName;
    }
    public String toExternalForm() {
        if ( entryName == null ) {
            return url.toExternalForm();
        } else {
            return "jar:" + url.toExternalForm() + "!/" + entryName;
        }
    }
    public InputStream openStream() throws IOException {
        if ( entryName == null ) {
            return url.openStream();
        } else {
            URL entryUrl = JarFactory.getJarEntryURL ( url, entryName );
            return entryUrl.openStream();
        }
    }
    public Jar openJar() throws IOException {
        if ( entryName == null ) {
            return null;
        } else {
            return JarFactory.newInstance ( url );
        }
    }
    @Override
    public boolean equals ( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        TldResourcePath other = ( TldResourcePath ) o;
        return url.equals ( other.url ) &&
               Objects.equals ( webappPath, other.webappPath ) &&
               Objects.equals ( entryName, other.entryName );
    }
    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = result * 31 + Objects.hashCode ( webappPath );
        result = result * 31 + Objects.hashCode ( entryName );
        return result;
    }
}
