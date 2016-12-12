package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanner;
public class JarScannerSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aJarScanner,
                                StoreDescription parentDesc ) throws Exception {
        if ( aJarScanner instanceof JarScanner ) {
            JarScanner jarScanner = ( JarScanner ) aJarScanner;
            JarScanFilter jarScanFilter = jarScanner.getJarScanFilter();
            if ( jarScanFilter != null ) {
                storeElement ( aWriter, indent, jarScanFilter );
            }
        }
    }
}
