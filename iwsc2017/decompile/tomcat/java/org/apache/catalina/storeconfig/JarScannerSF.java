package org.apache.catalina.storeconfig;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanner;
import java.io.PrintWriter;
public class JarScannerSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aJarScanner, final StoreDescription parentDesc ) throws Exception {
        if ( aJarScanner instanceof JarScanner ) {
            final JarScanner jarScanner = ( JarScanner ) aJarScanner;
            final JarScanFilter jarScanFilter = jarScanner.getJarScanFilter();
            if ( jarScanFilter != null ) {
                this.storeElement ( aWriter, indent, jarScanFilter );
            }
        }
    }
}
