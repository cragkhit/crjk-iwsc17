package org.apache.catalina.ssi;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
public interface SSIExternalResolver {
    public void addVariableNames ( Collection<String> variableNames );
    public String getVariableValue ( String name );
    public void setVariableValue ( String name, String value );
    public Date getCurrentDate();
    public long getFileSize ( String path, boolean virtual ) throws IOException;
    public long getFileLastModified ( String path, boolean virtual )
    throws IOException;
    public String getFileText ( String path, boolean virtual ) throws IOException;
    public void log ( String message, Throwable throwable );
}
