package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public abstract class AbstractCatalinaCommandTask extends AbstractCatalinaTask {
    protected String path;
    protected String version;
    public AbstractCatalinaCommandTask() {
        this.path = null;
        this.version = null;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath ( final String path ) {
        this.path = path;
    }
    public String getVersion() {
        return this.version;
    }
    public void setVersion ( final String version ) {
        this.version = version;
    }
    public StringBuilder createQueryString ( final String command ) throws BuildException {
        final StringBuilder buffer = new StringBuilder();
        try {
            buffer.append ( command );
            if ( this.path == null ) {
                throw new BuildException ( "Must specify 'path' attribute" );
            }
            buffer.append ( "?path=" );
            buffer.append ( URLEncoder.encode ( this.path, this.getCharset() ) );
            if ( this.version != null ) {
                buffer.append ( "&version=" );
                buffer.append ( URLEncoder.encode ( this.version, this.getCharset() ) );
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
        }
        return buffer;
    }
}
