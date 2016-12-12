package javax.servlet;
import java.io.IOException;
public interface FilterChain {
    void doFilter ( ServletRequest p0, ServletResponse p1 ) throws IOException, ServletException;
}
