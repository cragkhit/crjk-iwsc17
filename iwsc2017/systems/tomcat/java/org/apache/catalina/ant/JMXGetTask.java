package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class JMXGetTask extends AbstractCatalinaTask {
    protected String bean      = null;
    protected String attribute = null;
    public String getBean () {
        return this.bean;
    }
    public void setBean ( String bean ) {
        this.bean = bean;
    }
    public String getAttribute () {
        return this.attribute;
    }
    public void setAttribute ( String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( bean == null || attribute == null ) {
            throw new BuildException
            ( "Must specify 'bean' and 'attribute' attributes" );
        }
        log ( "Getting attribute " + attribute +
              " in bean " + bean );
        try {
            execute ( "/jmxproxy/?get=" + URLEncoder.encode ( bean, getCharset() )
                      + "&att=" + URLEncoder.encode ( attribute, getCharset() ) );
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException
            ( "Invalid 'charset' attribute: " + getCharset() );
        }
    }
}
