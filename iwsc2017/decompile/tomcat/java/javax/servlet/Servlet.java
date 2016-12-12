package javax.servlet;
import java.io.IOException;
public interface Servlet {
    void init ( ServletConfig p0 ) throws ServletException;
    ServletConfig getServletConfig();
    void service ( ServletRequest p0, ServletResponse p1 ) throws ServletException, IOException;
    String getServletInfo();
    void destroy();
}
