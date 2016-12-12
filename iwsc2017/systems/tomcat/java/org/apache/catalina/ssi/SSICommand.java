package org.apache.catalina.ssi;
import java.io.PrintWriter;
public interface SSICommand {
    public long process ( SSIMediator ssiMediator, String commandName,
                          String[] paramNames, String[] paramValues, PrintWriter writer )
    throws SSIStopProcessingException;
}
