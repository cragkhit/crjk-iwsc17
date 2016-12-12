package org.apache.jasper.el;
import javax.el.PropertyNotFoundException;
public final class JspPropertyNotFoundException extends PropertyNotFoundException {
    private static final long serialVersionUID = 1L;
    public JspPropertyNotFoundException ( final String mark, final PropertyNotFoundException e ) {
        super ( mark + " " + e.getMessage(), e.getCause() );
    }
}
