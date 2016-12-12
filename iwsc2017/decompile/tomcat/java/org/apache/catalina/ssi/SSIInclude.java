package org.apache.catalina.ssi;
import java.io.IOException;
import java.io.PrintWriter;
public final class SSIInclude implements SSICommand {
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
                    final String text = ssiMediator.getFileText ( substitutedValue, virtual );
                    writer.write ( text );
                } else {
                    ssiMediator.log ( "#include--Invalid attribute: " + paramName );
                    writer.write ( configErrMsg );
                }
            } catch ( IOException e ) {
                ssiMediator.log ( "#include--Couldn't include file: " + substitutedValue, e );
                writer.write ( configErrMsg );
            }
        }
        return lastModified;
    }
}
