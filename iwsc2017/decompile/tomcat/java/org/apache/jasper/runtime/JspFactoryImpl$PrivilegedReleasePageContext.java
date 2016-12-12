package org.apache.jasper.runtime;
import javax.servlet.jsp.PageContext;
import java.security.PrivilegedAction;
private static class PrivilegedReleasePageContext implements PrivilegedAction<Void> {
    private JspFactoryImpl factory;
    private PageContext pageContext;
    PrivilegedReleasePageContext ( final JspFactoryImpl factory, final PageContext pageContext ) {
        this.factory = factory;
        this.pageContext = pageContext;
    }
    @Override
    public Void run() {
        JspFactoryImpl.access$100 ( this.factory, this.pageContext );
        return null;
    }
}
