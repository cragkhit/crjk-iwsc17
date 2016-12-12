package org.apache.catalina.ssi;
import org.apache.catalina.util.Strftime;
import java.util.Locale;
import java.io.IOException;
import java.util.Date;
import java.io.PrintWriter;
public final class SSIFlastmod implements SSICommand {
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        long lastModified = 0L;
        final String configErrMsg = ssiMediator.getConfigErrMsg();
        for ( int i = 0; i < paramNames.length; ++i ) {
            final String paramName = paramNames[i];
            final String paramValue = paramValues[i];
            final String substitutedValue = ssiMediator.substituteVariables ( paramValue );
            try {
                if ( paramName.equalsIgnoreCase ( "file" ) || paramName.equalsIgnoreCase ( "virtual" ) ) {
                    final boolean virtual = paramName.equalsIgnoreCase ( "virtual" );
                    lastModified = ssiMediator.getFileLastModified ( substitutedValue, virtual );
                    final Date date = new Date ( lastModified );
                    final String configTimeFmt = ssiMediator.getConfigTimeFmt();
                    writer.write ( this.formatDate ( date, configTimeFmt ) );
                } else {
                    ssiMediator.log ( "#flastmod--Invalid attribute: " + paramName );
                    writer.write ( configErrMsg );
                }
            } catch ( IOException e ) {
                ssiMediator.log ( "#flastmod--Couldn't get last modified for file: " + substitutedValue, e );
                writer.write ( configErrMsg );
            }
        }
        return lastModified;
    }
    protected String formatDate ( final Date date, final String configTimeFmt ) {
        final Strftime strftime = new Strftime ( configTimeFmt, Locale.US );
        return strftime.format ( date );
    }
}
