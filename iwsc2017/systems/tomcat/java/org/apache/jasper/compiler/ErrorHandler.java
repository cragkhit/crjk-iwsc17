package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public interface ErrorHandler {
    public void jspError ( String fname, int line, int column, String msg,
                           Exception exception ) throws JasperException;
    public void jspError ( String msg, Exception exception )
    throws JasperException;
    public void javacError ( JavacErrorDetail[] details )
    throws JasperException;
    public void javacError ( String errorReport, Exception exception )
    throws JasperException;
}
