package org.apache.catalina.ssi;
import java.io.PrintWriter;
public final class SSIConfig implements SSICommand {
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        for ( int i = 0; i < paramNames.length; ++i ) {
            final String paramName = paramNames[i];
            final String paramValue = paramValues[i];
            final String substitutedValue = ssiMediator.substituteVariables ( paramValue );
            if ( paramName.equalsIgnoreCase ( "errmsg" ) ) {
                ssiMediator.setConfigErrMsg ( substitutedValue );
            } else if ( paramName.equalsIgnoreCase ( "sizefmt" ) ) {
                ssiMediator.setConfigSizeFmt ( substitutedValue );
            } else if ( paramName.equalsIgnoreCase ( "timefmt" ) ) {
                ssiMediator.setConfigTimeFmt ( substitutedValue );
            } else {
                ssiMediator.log ( "#config--Invalid attribute: " + paramName );
                final String configErrMsg = ssiMediator.getConfigErrMsg();
                writer.write ( configErrMsg );
            }
        }
        return 0L;
    }
}
