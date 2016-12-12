package org.apache.jasper;
import javax.servlet.ServletException;
public class JasperException extends ServletException {
    private static final long serialVersionUID = 1L;
    public JasperException ( final String reason ) {
        super ( reason );
    }
    public JasperException ( final String reason, final Throwable exception ) {
        super ( reason, exception );
    }
    public JasperException ( final Throwable exception ) {
        super ( exception );
    }
}
