package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class ResourcesTask extends AbstractCatalinaTask {
    protected String type = null;
    public String getType() {
        return ( this.type );
    }
    public void setType ( String type ) {
        this.type = type;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( type != null ) {
            try {
                execute ( "/resources?type=" +
                          URLEncoder.encode ( type, getCharset() ) );
            } catch ( UnsupportedEncodingException e ) {
                throw new BuildException
                ( "Invalid 'charset' attribute: " + getCharset() );
            }
        } else {
            execute ( "/resources" );
        }
    }
}
