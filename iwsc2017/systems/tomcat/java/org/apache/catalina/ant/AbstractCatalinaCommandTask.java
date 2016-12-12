package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public abstract class AbstractCatalinaCommandTask extends
    AbstractCatalinaTask {
    protected String path = null;
    public String getPath() {
        return ( this.path );
    }
    public void setPath ( String path ) {
        this.path = path;
    }
    protected String version = null;
    public String getVersion() {
        return ( this.version );
    }
    public void setVersion ( String version ) {
        this.version = version;
    }
    public StringBuilder createQueryString ( String command ) throws BuildException {
        StringBuilder buffer = new StringBuilder();
        try {
            buffer.append ( command );
            if ( path == null ) {
                throw new BuildException ( "Must specify 'path' attribute" );
            } else {
                buffer.append ( "?path=" );
                buffer.append ( URLEncoder.encode ( this.path, getCharset() ) );
                if ( this.version != null ) {
                    buffer.append ( "&version=" );
                    buffer.append ( URLEncoder.encode ( this.version, getCharset() ) );
                }
            }
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException
            ( "Invalid 'charset' attribute: " + getCharset() );
        }
        return buffer;
    }
}
