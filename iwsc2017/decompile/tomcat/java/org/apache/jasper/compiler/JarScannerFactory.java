package org.apache.jasper.compiler;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.apache.tomcat.JarScanner;
import javax.servlet.ServletContext;
public class JarScannerFactory {
    public static JarScanner getJarScanner ( final ServletContext ctxt ) {
        JarScanner jarScanner = ( JarScanner ) ctxt.getAttribute ( JarScanner.class.getName() );
        if ( jarScanner == null ) {
            ctxt.log ( Localizer.getMessage ( "jsp.warning.noJarScanner" ) );
            jarScanner = new StandardJarScanner();
        }
        return jarScanner;
    }
}
