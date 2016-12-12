package org.apache.catalina.filters;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletException;
import java.nio.charset.Charset;
import javax.servlet.FilterConfig;
import org.apache.juli.logging.Log;
public class AddDefaultCharsetFilter extends FilterBase {
    private static final Log log;
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private String encoding;
    public void setEncoding ( final String encoding ) {
        this.encoding = encoding;
    }
    @Override
    protected Log getLogger() {
        return AddDefaultCharsetFilter.log;
    }
    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException {
        super.init ( filterConfig );
        if ( this.encoding == null || this.encoding.length() == 0 || this.encoding.equalsIgnoreCase ( "default" ) ) {
            this.encoding = "ISO-8859-1";
        } else if ( this.encoding.equalsIgnoreCase ( "system" ) ) {
            this.encoding = Charset.defaultCharset().name();
        } else if ( !Charset.isSupported ( this.encoding ) ) {
            throw new IllegalArgumentException ( AddDefaultCharsetFilter.sm.getString ( "addDefaultCharset.unsupportedCharset", this.encoding ) );
        }
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( response instanceof HttpServletResponse ) {
            final ResponseWrapper wrapped = new ResponseWrapper ( ( HttpServletResponse ) response, this.encoding );
            chain.doFilter ( request, ( ServletResponse ) wrapped );
        } else {
            chain.doFilter ( request, response );
        }
    }
    static {
        log = LogFactory.getLog ( AddDefaultCharsetFilter.class );
    }
    public static class ResponseWrapper extends HttpServletResponseWrapper {
        private String encoding;
        public ResponseWrapper ( final HttpServletResponse response, final String encoding ) {
            super ( response );
            this.encoding = encoding;
        }
        public void setContentType ( final String ct ) {
            if ( ct != null && ct.startsWith ( "text/" ) ) {
                if ( ct.indexOf ( "charset=" ) < 0 ) {
                    super.setContentType ( ct + ";charset=" + this.encoding );
                } else {
                    super.setContentType ( ct );
                    this.encoding = this.getCharacterEncoding();
                }
            } else {
                super.setContentType ( ct );
            }
        }
        public void setCharacterEncoding ( final String charset ) {
            super.setCharacterEncoding ( charset );
            this.encoding = charset;
        }
    }
}
