package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void jspError ( String fname, int line, int column, String errMsg,
                           Exception ex ) throws JasperException {
        throw new JasperException ( fname + " (" +
                                    Localizer.getMessage ( "jsp.error.location",
                                            Integer.toString ( line ), Integer.toString ( column ) ) +
                                    ") " + errMsg, ex );
    }
    @Override
    public void jspError ( String errMsg, Exception ex ) throws JasperException {
        throw new JasperException ( errMsg, ex );
    }
    @Override
    public void javacError ( JavacErrorDetail[] details ) throws JasperException {
        if ( details == null ) {
            return;
        }
        Object[] args = null;
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < details.length; i++ ) {
            if ( details[i].getJspBeginLineNumber() >= 0 ) {
                args = new Object[] {
                    Integer.valueOf ( details[i].getJspBeginLineNumber() ),
                    details[i].getJspFileName()
                };
                buf.append ( System.lineSeparator() );
                buf.append ( System.lineSeparator() );
                buf.append ( Localizer.getMessage ( "jsp.error.single.line.number",
                                                    args ) );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getErrorMessage() );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getJspExtract() );
            } else {
                args = new Object[] {
                    Integer.valueOf ( details[i].getJavaLineNumber() ),
                    details[i].getJavaFileName()
                };
                buf.append ( System.lineSeparator() );
                buf.append ( System.lineSeparator() );
                buf.append ( Localizer.getMessage ( "jsp.error.java.line.number",
                                                    args ) );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getErrorMessage() );
            }
        }
        buf.append ( System.lineSeparator() );
        buf.append ( System.lineSeparator() );
        buf.append ( "Stacktrace:" );
        throw new JasperException (
            Localizer.getMessage ( "jsp.error.unable.compile" ) + ": " + buf );
    }
    @Override
    public void javacError ( String errorReport, Exception exception )
    throws JasperException {
        throw new JasperException (
            Localizer.getMessage ( "jsp.error.unable.compile" ), exception );
    }
}
