package org.apache.tomcat;
import javax.servlet.ServletContext;
public interface JarScanner {
    public void scan ( JarScanType scanType, ServletContext context,
                       JarScannerCallback callback );
    public JarScanFilter getJarScanFilter();
    public void setJarScanFilter ( JarScanFilter jarScanFilter );
}
