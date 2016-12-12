package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import org.apache.tools.ant.BuildException;
import java.net.URLEncoder;
public class JMXQueryTask extends AbstractCatalinaTask {
    protected String query;
    public JMXQueryTask() {
        this.query = null;
    }
    public String getQuery() {
        return this.query;
    }
    public void setQuery ( final String query ) {
        this.query = query;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        String queryString;
        if ( this.query == null ) {
            queryString = "";
        } else {
            try {
                queryString = "?qry=" + URLEncoder.encode ( this.query, this.getCharset() );
            } catch ( UnsupportedEncodingException e ) {
                throw new BuildException ( "Invalid 'charset' attribute: " + this.getCharset() );
            }
        }
        this.log ( "Query string is " + queryString );
        this.execute ( "/jmxproxy/" + queryString );
    }
}
