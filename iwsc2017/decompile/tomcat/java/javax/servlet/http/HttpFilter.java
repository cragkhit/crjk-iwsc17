package javax.servlet.http;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.GenericFilter;
public abstract class HttpFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    @Override
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( ! ( request instanceof HttpServletRequest ) ) {
            throw new ServletException ( request + " not HttpServletRequest" );
        }
        if ( ! ( response instanceof HttpServletResponse ) ) {
            throw new ServletException ( request + " not HttpServletResponse" );
        }
        this.doFilter ( ( HttpServletRequest ) request, ( HttpServletResponse ) response, chain );
    }
    protected void doFilter ( final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        chain.doFilter ( request, response );
    }
}
