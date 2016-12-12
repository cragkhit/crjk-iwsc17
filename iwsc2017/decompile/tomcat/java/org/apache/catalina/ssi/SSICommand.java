package org.apache.catalina.ssi;
import java.io.PrintWriter;
public interface SSICommand {
    long process ( SSIMediator p0, String p1, String[] p2, String[] p3, PrintWriter p4 ) throws SSIStopProcessingException;
}
