package org.apache.catalina.ssi;
import java.util.Iterator;
import java.util.Collection;
import java.io.PrintWriter;
public class SSIPrintenv implements SSICommand {
    @Override
    public long process ( final SSIMediator ssiMediator, final String commandName, final String[] paramNames, final String[] paramValues, final PrintWriter writer ) {
        long lastModified = 0L;
        if ( paramNames.length > 0 ) {
            final String errorMessage = ssiMediator.getConfigErrMsg();
            writer.write ( errorMessage );
        } else {
            final Collection<String> variableNames = ssiMediator.getVariableNames();
            for ( final String variableName : variableNames ) {
                String variableValue = ssiMediator.getVariableValue ( variableName );
                if ( variableValue == null ) {
                    variableValue = "(none)";
                }
                writer.write ( variableName );
                writer.write ( 61 );
                writer.write ( variableValue );
                writer.write ( 10 );
                lastModified = System.currentTimeMillis();
            }
        }
        return lastModified;
    }
}
