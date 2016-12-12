package org.apache.jasper;
public class JasperException extends javax.servlet.ServletException {
    private static final long serialVersionUID = 1L;
    public JasperException ( String reason ) {
        super ( reason );
    }
    public JasperException ( String reason, Throwable exception ) {
        super ( reason, exception );
    }
    public JasperException ( Throwable exception ) {
        super ( exception );
    }
}
