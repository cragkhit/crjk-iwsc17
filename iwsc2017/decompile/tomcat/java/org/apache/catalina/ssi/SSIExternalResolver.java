package org.apache.catalina.ssi;
import java.io.IOException;
import java.util.Date;
import java.util.Collection;
public interface SSIExternalResolver {
    void addVariableNames ( Collection<String> p0 );
    String getVariableValue ( String p0 );
    void setVariableValue ( String p0, String p1 );
    Date getCurrentDate();
    long getFileSize ( String p0, boolean p1 ) throws IOException;
    long getFileLastModified ( String p0, boolean p1 ) throws IOException;
    String getFileText ( String p0, boolean p1 ) throws IOException;
    void log ( String p0, Throwable p1 );
}
