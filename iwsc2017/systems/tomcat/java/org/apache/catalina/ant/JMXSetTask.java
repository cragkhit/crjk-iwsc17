package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class JMXSetTask extends AbstractCatalinaTask {
    protected String bean      = null;
    protected String attribute = null;
    protected String value     = null;
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
    public String getValue () {
        return this.value;
    }
    public void setValue ( String value ) {
        this.value = value;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        if ( bean == null || attribute == null || value == null ) {
            throw new BuildException
            ( "Must specify 'bean', 'attribute' and 'value' attributes" );
        }
        log ( "Setting attribute " + attribute +
              " in bean " + bean +
              " to " + value );
        try {
            execute ( "/jmxproxy/?set=" + URLEncoder.encode ( bean, getCharset() )
                      + "&att=" + URLEncoder.encode ( attribute, getCharset() )
                      + "&val=" + URLEncoder.encode ( value, getCharset() ) );
        } catch ( UnsupportedEncodingException e ) {
            throw new BuildException
            ( "Invalid 'charset' attribute: " + getCharset() );
        }
    }
}
