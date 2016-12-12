package org.apache.tomcat.util.descriptor.tld;
import java.util.Objects;
import org.apache.tomcat.Jar;
import java.io.IOException;
import org.apache.tomcat.util.scan.JarFactory;
import java.io.InputStream;
import java.net.URL;
public class TldResourcePath {
    private final URL url;
    private final String webappPath;
    private final String entryName;
    public TldResourcePath ( final URL url, final String webappPath ) {
        this ( url, webappPath, null );
    }
    public TldResourcePath ( final URL url, final String webappPath, final String entryName ) {
        this.url = url;
        this.webappPath = webappPath;
        this.entryName = entryName;
    }
    public URL getUrl() {
        return this.url;
    }
    public String getWebappPath() {
        return this.webappPath;
    }
    public String getEntryName() {
        return this.entryName;
    }
    public String toExternalForm() {
        if ( this.entryName == null ) {
            return this.url.toExternalForm();
        }
        return "jar:" + this.url.toExternalForm() + "!/" + this.entryName;
    }
    public InputStream openStream() throws IOException {
        if ( this.entryName == null ) {
            return this.url.openStream();
        }
        final URL entryUrl = JarFactory.getJarEntryURL ( this.url, this.entryName );
        return entryUrl.openStream();
    }
    public Jar openJar() throws IOException {
        if ( this.entryName == null ) {
            return null;
        }
        return JarFactory.newInstance ( this.url );
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || this.getClass() != o.getClass() ) {
            return false;
        }
        final TldResourcePath other = ( TldResourcePath ) o;
        return this.url.equals ( other.url ) && Objects.equals ( this.webappPath, other.webappPath ) && Objects.equals ( this.entryName, other.entryName );
    }
    @Override
    public int hashCode() {
        int result = this.url.hashCode();
        result = result * 31 + Objects.hashCode ( this.webappPath );
        result = result * 31 + Objects.hashCode ( this.entryName );
        return result;
    }
}
