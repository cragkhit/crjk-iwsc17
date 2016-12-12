package org.apache.catalina.ssi;
import java.io.IOException;
import java.io.Writer;
import org.apache.catalina.util.IOTools;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
public class SSIExec implements SSICommand {
    protected final SSIInclude ssiInclude;
    protected static final int BUFFER_SIZE = 1024;
    public SSIExec() {
        this.ssiInclude = new SSIInclude();
    }
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        long lastModified = 0L;
        final String configErrMsg = ssiMediator.getConfigErrMsg();
        final String paramName = paramNames[0];
        final String paramValue = paramValues[0];
        final String substitutedValue = ssiMediator.substituteVariables ( paramValue );
        if ( paramName.equalsIgnoreCase ( "cgi" ) ) {
            lastModified = this.ssiInclude.process ( ssiMediator, "include", new String[] { "virtual" }, new String[] { substitutedValue }, writer );
        } else if ( paramName.equalsIgnoreCase ( "cmd" ) ) {
            boolean foundProgram = false;
            try {
                final Runtime rt = Runtime.getRuntime();
                final Process proc = rt.exec ( substitutedValue );
                foundProgram = true;
                final BufferedReader stdOutReader = new BufferedReader ( new InputStreamReader ( proc.getInputStream() ) );
                final BufferedReader stdErrReader = new BufferedReader ( new InputStreamReader ( proc.getErrorStream() ) );
                final char[] buf = new char[1024];
                IOTools.flow ( stdErrReader, writer, buf );
                IOTools.flow ( stdOutReader, writer, buf );
                proc.waitFor();
                lastModified = System.currentTimeMillis();
            } catch ( InterruptedException e ) {
                ssiMediator.log ( "Couldn't exec file: " + substitutedValue, e );
                writer.write ( configErrMsg );
            } catch ( IOException e2 ) {
                if ( foundProgram ) {}
                ssiMediator.log ( "Couldn't exec file: " + substitutedValue, e2 );
            }
        }
        return lastModified;
    }
}
