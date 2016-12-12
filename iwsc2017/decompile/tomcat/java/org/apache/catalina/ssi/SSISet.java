package org.apache.catalina.ssi;
import java.io.PrintWriter;
public class SSISet implements SSICommand {
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) throws SSIStopProcessingException {
        long lastModified = 0L;
        final String errorMessage = ssiMediator.getConfigErrMsg();
        String variableName = null;
        for ( int i = 0; i < paramNames.length; ++i ) {
            final String paramName = paramNames[i];
            final String paramValue = paramValues[i];
            if ( paramName.equalsIgnoreCase ( "var" ) ) {
                variableName = paramValue;
            } else {
                if ( !paramName.equalsIgnoreCase ( "value" ) ) {
                    ssiMediator.log ( "#set--Invalid attribute: " + paramName );
                    writer.write ( errorMessage );
                    throw new SSIStopProcessingException();
                }
                if ( variableName == null ) {
                    ssiMediator.log ( "#set--no variable specified" );
                    writer.write ( errorMessage );
                    throw new SSIStopProcessingException();
                }
                final String substitutedValue = ssiMediator.substituteVariables ( paramValue );
                ssiMediator.setVariableValue ( variableName, substitutedValue );
                lastModified = System.currentTimeMillis();
            }
        }
        return lastModified;
    }
}
