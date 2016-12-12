package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class JMXGetTask extends AbstractCatalinaTask {
    protected String bean;
    protected String attribute;
    public JMXGetTask() {
        this.bean = null;
        this.attribute = null;
    }
    public String getBean() {
        return this.bean;
    }
    public void setBean ( final String bean ) {
        this.bean = bean;
    }
    public String getAttribute() {
        return this.attribute;
    }
    public void setAttribute ( final String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( this.bean == null || this.attribute == null ) {
            throw new BuildException ( "Must specify 'bean' and 'attribute' attributes" );
        }
        this.log ( "Getting attribute " + this.attribute + " in bean " + this.bean );
        try {
            this.execute ( "/jmxproxy/?get=" + URLEncoder.encode ( this.bean, this.getCharset() ) + "&att=" + URLEncoder.encode ( this.attribute, this.getCharset() ) );
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
        }
    }
}
