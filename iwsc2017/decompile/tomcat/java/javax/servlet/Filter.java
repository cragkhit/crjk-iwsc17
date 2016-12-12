package javax.servlet;
import java.io.IOException;
public interface Filter {
default void init ( FilterConfig filterConfig ) throws ServletException {
        }
    void doFilter ( ServletRequest p0, ServletResponse p1, FilterChain p2 ) throws IOException, ServletException;
default void destroy() {
    }
}
