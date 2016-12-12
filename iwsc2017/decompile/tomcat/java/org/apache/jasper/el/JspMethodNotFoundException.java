package org.apache.jasper.el;
import javax.el.MethodNotFoundException;
public class JspMethodNotFoundException extends MethodNotFoundException {
    private static final long serialVersionUID = 1L;
    public JspMethodNotFoundException ( final String mark, final MethodNotFoundException e ) {
        super ( mark + " " + e.getMessage(), e.getCause() );
    }
}
