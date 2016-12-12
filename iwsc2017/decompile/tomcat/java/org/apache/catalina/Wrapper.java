package org.apache.catalina;
import javax.servlet.MultipartConfigElement;
import javax.servlet.UnavailableException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
public interface Wrapper extends Container {
    public static final String ADD_MAPPING_EVENT = "addMapping";
    public static final String REMOVE_MAPPING_EVENT = "removeMapping";
    long getAvailable();
    void setAvailable ( long p0 );
    int getLoadOnStartup();
    void setLoadOnStartup ( int p0 );
    String getRunAs();
    void setRunAs ( String p0 );
    String getServletClass();
    void setServletClass ( String p0 );
    String[] getServletMethods() throws ServletException;
    boolean isUnavailable();
    Servlet getServlet();
    void setServlet ( Servlet p0 );
    void addInitParameter ( String p0, String p1 );
    void addMapping ( String p0 );
    void addSecurityReference ( String p0, String p1 );
    Servlet allocate() throws ServletException;
    void deallocate ( Servlet p0 ) throws ServletException;
    String findInitParameter ( String p0 );
    String[] findInitParameters();
    String[] findMappings();
    String findSecurityReference ( String p0 );
    String[] findSecurityReferences();
    void incrementErrorCount();
    void load() throws ServletException;
    void removeInitParameter ( String p0 );
    void removeMapping ( String p0 );
    void removeSecurityReference ( String p0 );
    void unavailable ( UnavailableException p0 );
    void unload() throws ServletException;
    MultipartConfigElement getMultipartConfigElement();
    void setMultipartConfigElement ( MultipartConfigElement p0 );
    boolean isAsyncSupported();
    void setAsyncSupported ( boolean p0 );
    boolean isEnabled();
    void setEnabled ( boolean p0 );
    void setServletSecurityAnnotationScanRequired ( boolean p0 );
    void servletSecurityAnnotationScan() throws ServletException;
    boolean isOverridable();
    void setOverridable ( boolean p0 );
}