package org.apache.jasper.runtime;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import java.security.PrivilegedAction;
class JspFactoryImpl$2 implements PrivilegedAction<JspApplicationContext> {
    final   ServletContext val$context;
    @Override
    public JspApplicationContext run() {
        return ( JspApplicationContext ) JspApplicationContextImpl.getInstance ( this.val$context );
    }
}
