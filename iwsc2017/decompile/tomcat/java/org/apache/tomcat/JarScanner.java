package org.apache.tomcat;
import javax.servlet.ServletContext;
public interface JarScanner {
    void scan ( JarScanType p0, ServletContext p1, JarScannerCallback p2 );
    JarScanFilter getJarScanFilter();
    void setJarScanFilter ( JarScanFilter p0 );
}
