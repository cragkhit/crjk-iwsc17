package org.apache.jasper.compiler;
import javax.servlet.ServletContext;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.scan.StandardJarScanner;
public class JarScannerFactory {
    private JarScannerFactory() {
    }
    public static JarScanner getJarScanner ( ServletContext ctxt ) {
        JarScanner jarScanner =
            ( JarScanner ) ctxt.getAttribute ( JarScanner.class.getName() );
        if ( jarScanner == null ) {
            ctxt.log ( Localizer.getMessage ( "jsp.warning.noJarScanner" ) );
            jarScanner = new StandardJarScanner();
        }
        return jarScanner;
    }
}
