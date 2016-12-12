package javax.servlet.http;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
public abstract class HttpFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    @Override
    public void doFilter ( ServletRequest request, ServletResponse response, FilterChain chain )
    throws IOException, ServletException {
        if ( ! ( request instanceof HttpServletRequest ) ) {
            throw new ServletException ( request + " not HttpServletRequest" );
        }
        if ( ! ( response instanceof HttpServletResponse ) ) {
            throw new ServletException ( request + " not HttpServletResponse" );
        }
        doFilter ( ( HttpServletRequest ) request, ( HttpServletResponse ) response, chain );
    }
    protected void doFilter ( HttpServletRequest request, HttpServletResponse response,
                              FilterChain chain ) throws IOException, ServletException {
        chain.doFilter ( request, response );
    }
}
