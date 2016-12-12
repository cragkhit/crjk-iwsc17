package org.apache.catalina.core;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
public final class StandardWrapperFacade
    implements ServletConfig {
    public StandardWrapperFacade ( StandardWrapper config ) {
        super();
        this.config = config;
    }
    private final ServletConfig config;
    private ServletContext context = null;
    @Override
    public String getServletName() {
        return config.getServletName();
    }
    @Override
    public ServletContext getServletContext() {
        if ( context == null ) {
            context = config.getServletContext();
            if ( context instanceof ApplicationContext ) {
                context = ( ( ApplicationContext ) context ).getFacade();
            }
        }
        return ( context );
    }
    @Override
    public String getInitParameter ( String name ) {
        return config.getInitParameter ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return config.getInitParameterNames();
    }
}
