package org.apache.tomcat;
import java.io.File;
import java.io.IOException;
public interface JarScannerCallback {
    void scan ( Jar p0, String p1, boolean p2 ) throws IOException;
    void scan ( File p0, String p1, boolean p2 ) throws IOException;
    void scanWebInfClasses() throws IOException;
}
