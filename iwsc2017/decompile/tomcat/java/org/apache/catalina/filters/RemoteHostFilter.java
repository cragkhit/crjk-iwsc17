package org.apache.catalina.filters;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.juli.logging.Log;
public final class RemoteHostFilter extends RequestFilter {
    private static final Log log;
    @Override
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        this.process ( request.getRemoteHost(), request, response, chain );
    }
    @Override
    protected Log getLogger() {
        return RemoteHostFilter.log;
    }
    static {
        log = LogFactory.getLog ( RemoteHostFilter.class );
    }
}
