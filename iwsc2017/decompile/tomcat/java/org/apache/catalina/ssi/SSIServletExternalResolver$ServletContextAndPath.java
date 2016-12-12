package org.apache.catalina.ssi;
import javax.servlet.ServletContext;
protected static class ServletContextAndPath {
    protected final ServletContext servletContext;
    protected final String path;
    public ServletContextAndPath ( final ServletContext servletContext, final String path ) {
        this.servletContext = servletContext;
        this.path = path;
    }
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    public String getPath() {
        return this.path;
    }
}
