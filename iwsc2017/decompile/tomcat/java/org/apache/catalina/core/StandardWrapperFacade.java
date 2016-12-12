package org.apache.catalina.core;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
public final class StandardWrapperFacade implements ServletConfig {
    private final ServletConfig config;
    private ServletContext context;
    public StandardWrapperFacade ( final StandardWrapper config ) {
        this.context = null;
        this.config = ( ServletConfig ) config;
    }
    public String getServletName() {
        return this.config.getServletName();
    }
    public ServletContext getServletContext() {
        if ( this.context == null ) {
            this.context = this.config.getServletContext();
            if ( this.context instanceof ApplicationContext ) {
                this.context = ( ( ApplicationContext ) this.context ).getFacade();
            }
        }
        return this.context;
    }
    public String getInitParameter ( final String name ) {
        return this.config.getInitParameter ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        return ( Enumeration<String> ) this.config.getInitParameterNames();
    }
}
