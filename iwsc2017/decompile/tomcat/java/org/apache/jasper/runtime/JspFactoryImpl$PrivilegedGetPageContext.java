package org.apache.jasper.runtime;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Servlet;
import javax.servlet.jsp.PageContext;
import java.security.PrivilegedAction;
private static class PrivilegedGetPageContext implements PrivilegedAction<PageContext> {
    private JspFactoryImpl factory;
    private Servlet servlet;
    private ServletRequest request;
    private ServletResponse response;
    private String errorPageURL;
    private boolean needsSession;
    private int bufferSize;
    private boolean autoflush;
    PrivilegedGetPageContext ( final JspFactoryImpl factory, final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, final int bufferSize, final boolean autoflush ) {
        this.factory = factory;
        this.servlet = servlet;
        this.request = request;
        this.response = response;
        this.errorPageURL = errorPageURL;
        this.needsSession = needsSession;
        this.bufferSize = bufferSize;
        this.autoflush = autoflush;
    }
    @Override
    public PageContext run() {
        return JspFactoryImpl.access$000 ( this.factory, this.servlet, this.request, this.response, this.errorPageURL, this.needsSession, this.bufferSize, this.autoflush );
    }
}
