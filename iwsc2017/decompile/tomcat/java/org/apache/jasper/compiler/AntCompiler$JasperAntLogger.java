package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.PrintStream;
import org.apache.tools.ant.DefaultLogger;
public static class JasperAntLogger extends DefaultLogger {
    protected final StringBuilder reportBuf;
    public JasperAntLogger() {
        this.reportBuf = new StringBuilder();
    }
    protected void printMessage ( final String message, final PrintStream stream, final int priority ) {
    }
    protected void log ( final String message ) {
        this.reportBuf.append ( message );
        this.reportBuf.append ( System.lineSeparator() );
    }
    protected String getReport() {
        final String report = this.reportBuf.toString();
        this.reportBuf.setLength ( 0 );
        return report;
    }
}
