package org.apache.catalina.ssi;
import java.io.PrintWriter;
public class SSIEcho implements SSICommand {
    protected static final String DEFAULT_ENCODING = "entity";
    protected static final String MISSING_VARIABLE_VALUE = "(none)";
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        String encoding = "entity";
        String originalValue = null;
        final String errorMessage = ssiMediator.getConfigErrMsg();
        for ( int i = 0; i < paramNames.length; ++i ) {
            final String paramName = paramNames[i];
            final String paramValue = paramValues[i];
            if ( paramName.equalsIgnoreCase ( "var" ) ) {
                originalValue = paramValue;
            } else if ( paramName.equalsIgnoreCase ( "encoding" ) ) {
                if ( this.isValidEncoding ( paramValue ) ) {
                    encoding = paramValue;
                } else {
                    ssiMediator.log ( "#echo--Invalid encoding: " + paramValue );
                    writer.write ( errorMessage );
                }
            } else {
                ssiMediator.log ( "#echo--Invalid attribute: " + paramName );
                writer.write ( errorMessage );
            }
        }
        String variableValue = ssiMediator.getVariableValue ( originalValue, encoding );
        if ( variableValue == null ) {
            variableValue = "(none)";
        }
        writer.write ( variableValue );
        return System.currentTimeMillis();
    }
    protected boolean isValidEncoding ( final String encoding ) {
        return encoding.equalsIgnoreCase ( "url" ) || encoding.equalsIgnoreCase ( "entity" ) || encoding.equalsIgnoreCase ( "none" );
    }
}
