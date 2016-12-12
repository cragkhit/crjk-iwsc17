package javax.servlet;
import java.util.Enumeration;
public interface FilterConfig {
    String getFilterName();
    ServletContext getServletContext();
    String getInitParameter ( String p0 );
    Enumeration<String> getInitParameterNames();
}
