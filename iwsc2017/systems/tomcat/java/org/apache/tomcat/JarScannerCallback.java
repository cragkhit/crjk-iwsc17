package org.apache.tomcat;
import java.io.File;
import java.io.IOException;
public interface JarScannerCallback {
    public void scan ( Jar jar, String webappPath, boolean isWebapp )
    throws IOException;
    public void scan ( File file, String webappPath, boolean isWebapp ) throws IOException;
    public void scanWebInfClasses() throws IOException;
}
