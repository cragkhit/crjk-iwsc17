package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import org.apache.tools.ant.BuildException;
import java.net.URLEncoder;
public class ResourcesTask extends AbstractCatalinaTask {
    protected String type;
    public ResourcesTask() {
        this.type = null;
    }
    public String getType() {
        return this.type;
    }
    public void setType ( final String type ) {
        this.type = type;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( this.type != null ) {
            try {
                this.execute ( "/resources?type=" + URLEncoder.encode ( this.type, this.getCharset() ) );
                return;
            } catch ( UnsupportedEncodingException e ) {
                throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
            }
        }
        this.execute ( "/resources" );
    }
}
