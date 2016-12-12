package org.apache.jasper.compiler;
import org.apache.jasper.JasperException;
class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void jspError ( final String fname, final int line, final int column, final String errMsg, final Exception ex ) throws JasperException {
        throw new JasperException ( fname + " (" + Localizer.getMessage ( "jsp.error.location", Integer.toString ( line ), Integer.toString ( column ) ) + ") " + errMsg, ex );
    }
    @Override
    public void jspError ( final String errMsg, final Exception ex ) throws JasperException {
        throw new JasperException ( errMsg, ex );
    }
    @Override
    public void javacError ( final JavacErrorDetail[] details ) throws JasperException {
        if ( details == null ) {
            return;
        }
        Object[] args = null;
        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < details.length; ++i ) {
            if ( details[i].getJspBeginLineNumber() >= 0 ) {
                args = new Object[] { details[i].getJspBeginLineNumber(), details[i].getJspFileName() };
                buf.append ( System.lineSeparator() );
                buf.append ( System.lineSeparator() );
                buf.append ( Localizer.getMessage ( "jsp.error.single.line.number", args ) );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getErrorMessage() );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getJspExtract() );
            } else {
                args = new Object[] { details[i].getJavaLineNumber(), details[i].getJavaFileName() };
                buf.append ( System.lineSeparator() );
                buf.append ( System.lineSeparator() );
                buf.append ( Localizer.getMessage ( "jsp.error.java.line.number", args ) );
                buf.append ( System.lineSeparator() );
                buf.append ( details[i].getErrorMessage() );
            }
        }
        buf.append ( System.lineSeparator() );
        buf.append ( System.lineSeparator() );
        buf.append ( "Stacktrace:" );
        throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.compile" ) + ": " + ( Object ) buf );
    }
    @Override
    public void javacError ( final String errorReport, final Exception exception ) throws JasperException {
        throw new JasperException ( Localizer.getMessage ( "jsp.error.unable.compile" ), exception );
    }
}
