package org.apache.catalina.startup;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class CatalinaProperties {
    private static final Log log = LogFactory.getLog ( CatalinaProperties.class );
    private static Properties properties = null;
    static {
        loadProperties();
    }
    public static String getProperty ( String name ) {
        return properties.getProperty ( name );
    }
    private static void loadProperties() {
        InputStream is = null;
        try {
            String configUrl = System.getProperty ( "catalina.config" );
            if ( configUrl != null ) {
                is = ( new URL ( configUrl ) ).openStream();
            }
        } catch ( Throwable t ) {
            handleThrowable ( t );
        }
        if ( is == null ) {
            try {
                File home = new File ( Bootstrap.getCatalinaBase() );
                File conf = new File ( home, "conf" );
                File propsFile = new File ( conf, "catalina.properties" );
                is = new FileInputStream ( propsFile );
            } catch ( Throwable t ) {
                handleThrowable ( t );
            }
        }
        if ( is == null ) {
            try {
                is = CatalinaProperties.class.getResourceAsStream
                     ( "/org/apache/catalina/startup/catalina.properties" );
            } catch ( Throwable t ) {
                handleThrowable ( t );
            }
        }
        if ( is != null ) {
            try {
                properties = new Properties();
                properties.load ( is );
            } catch ( Throwable t ) {
                handleThrowable ( t );
                log.warn ( t );
            } finally {
                try {
                    is.close();
                } catch ( IOException ioe ) {
                    log.warn ( "Could not close catalina.properties", ioe );
                }
            }
        }
        if ( ( is == null ) ) {
            log.warn ( "Failed to load catalina.properties" );
            properties = new Properties();
        }
        Enumeration<?> enumeration = properties.propertyNames();
        while ( enumeration.hasMoreElements() ) {
            String name = ( String ) enumeration.nextElement();
            String value = properties.getProperty ( name );
            if ( value != null ) {
                System.setProperty ( name, value );
            }
        }
    }
    private static void handleThrowable ( Throwable t ) {
        if ( t instanceof ThreadDeath ) {
            throw ( ThreadDeath ) t;
        }
        if ( t instanceof VirtualMachineError ) {
            throw ( VirtualMachineError ) t;
        }
    }
}
