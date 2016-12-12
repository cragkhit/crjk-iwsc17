package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
public interface ErrorHandler {
    void jspError ( String p0, int p1, int p2, String p3, Exception p4 ) throws JasperException;
    void jspError ( String p0, Exception p1 ) throws JasperException;
    void javacError ( JavacErrorDetail[] p0 ) throws JasperException;
    void javacError ( String p0, Exception p1 ) throws JasperException;
}
