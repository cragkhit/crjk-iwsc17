package org.apache.catalina.filters;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class RemoteHostFilter extends RequestFilter {
    private static final Log log = LogFactory.getLog ( RemoteHostFilter.class );
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response,
                           FilterChain chain ) throws IOException, ServletException {
        process ( request.getRemoteHost(), request, response, chain );
    }
    @Override
    protected Log getLogger() {
        return log;
    }
}
