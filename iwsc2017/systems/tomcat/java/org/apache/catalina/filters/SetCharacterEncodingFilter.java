package org.apache.catalina.filters;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class SetCharacterEncodingFilter extends FilterBase {
    private static final Log log =
        LogFactory.getLog ( SetCharacterEncodingFilter.class );
    private String encoding = null;
    public void setEncoding ( String encoding ) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    private boolean ignore = false;
    public void setIgnore ( boolean ignore ) {
        this.ignore = ignore;
    }
    public boolean isIgnore() {
        return ignore;
    }
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain )
    throws IOException, ServletException {
        if ( ignore || ( request.getCharacterEncoding() == null ) ) {
            String characterEncoding = selectEncoding ( request );
            if ( characterEncoding != null ) {
                request.setCharacterEncoding ( characterEncoding );
            }
        }
        chain.doFilter ( request, response );
    }
    @Override
    protected Log getLogger() {
        return log;
    }
    protected String selectEncoding ( ServletRequest request ) {
        return this.encoding;
    }
}
