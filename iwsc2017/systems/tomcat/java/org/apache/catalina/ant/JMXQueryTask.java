package org.apache.catalina.ant;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
public class JMXQueryTask extends AbstractCatalinaTask {
    protected String query      = null;
    public String getQuery () {
        return this.query;
    }
    public void setQuery ( String query ) {
        this.query = query;
    }
    @Override
    public void execute() throws BuildException {
        super.execute();
        String queryString;
        if ( query == null ) {
            queryString = "";
        } else {
            try {
                queryString = "?qry=" + URLEncoder.encode ( query, getCharset() );
            } catch ( UnsupportedEncodingException e ) {
                throw new BuildException
                ( "Invalid 'charset' attribute: " + getCharset() );
            }
        }
        log ( "Query string is " + queryString );
        execute ( "/jmxproxy/" + queryString );
    }
}
