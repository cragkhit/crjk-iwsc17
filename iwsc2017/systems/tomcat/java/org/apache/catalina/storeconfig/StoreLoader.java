package org.apache.catalina.storeconfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.Digester;
import org.xml.sax.SAXException;
public class StoreLoader {
    private static Log log = LogFactory.getLog ( StoreLoader.class );
    protected static final Digester digester = createDigester();
    private StoreRegistry registry;
    private URL registryResource ;
    public StoreRegistry getRegistry() {
        return registry;
    }
    public void setRegistry ( StoreRegistry registry ) {
        this.registry = registry;
    }
    protected static Digester createDigester() {
        long t1 = System.currentTimeMillis();
        Digester digester = new Digester();
        digester.setValidating ( false );
        digester.setClassLoader ( StoreRegistry.class.getClassLoader() );
        digester.addObjectCreate ( "Registry",
                                   "org.apache.catalina.storeconfig.StoreRegistry", "className" );
        digester.addSetProperties ( "Registry" );
        digester
        .addObjectCreate ( "Registry/Description",
                           "org.apache.catalina.storeconfig.StoreDescription",
                           "className" );
        digester.addSetProperties ( "Registry/Description" );
        digester.addRule ( "Registry/Description", new StoreFactoryRule (
                               "org.apache.catalina.storeconfig.StoreFactoryBase",
                               "storeFactoryClass",
                               "org.apache.catalina.storeconfig.StoreAppender",
                               "storeAppenderClass" ) );
        digester.addSetNext ( "Registry/Description", "registerDescription",
                              "org.apache.catalina.storeconfig.StoreDescription" );
        digester.addCallMethod ( "Registry/Description/TransientAttribute",
                                 "addTransientAttribute", 0 );
        digester.addCallMethod ( "Registry/Description/TransientChild",
                                 "addTransientChild", 0 );
        long t2 = System.currentTimeMillis();
        if ( log.isDebugEnabled() ) {
            log.debug ( "Digester for server-registry.xml created " + ( t2 - t1 ) );
        }
        return ( digester );
    }
    protected File serverFile ( String aFile ) {
        if ( aFile == null || aFile.length() < 1 ) {
            aFile = "server-registry.xml";
        }
        File file = new File ( aFile );
        if ( !file.isAbsolute() )
            file = new File ( System.getProperty ( "catalina.base" ) + "/conf",
                              aFile );
        try {
            file = file.getCanonicalFile();
        } catch ( IOException e ) {
            log.error ( e );
        }
        return ( file );
    }
    public void load ( String aURL ) {
        synchronized ( digester ) {
            File aRegistryFile = serverFile ( aURL );
            try {
                registry = ( StoreRegistry ) digester.parse ( aRegistryFile );
                registryResource = aRegistryFile.toURI().toURL();
            } catch ( IOException e ) {
                log.error ( e );
            } catch ( SAXException e ) {
                log.error ( e );
            }
        }
    }
    public void load() {
        InputStream is = null;
        registryResource = null ;
        try {
            String configUrl = getConfigUrl();
            if ( configUrl != null ) {
                is = ( new URL ( configUrl ) ).openStream();
                if ( log.isInfoEnabled() )
                    log.info ( "Find registry server-registry.xml from system property at url "
                               + configUrl );
                registryResource = new URL ( configUrl );
            }
        } catch ( Throwable t ) {
        }
        if ( is == null ) {
            try {
                File home = new File ( getCatalinaBase() );
                File conf = new File ( home, "conf" );
                File reg = new File ( conf, "server-registry.xml" );
                is = new FileInputStream ( reg );
                if ( log.isInfoEnabled() )
                    log.info ( "Find registry server-registry.xml at file "
                               + reg.getCanonicalPath() );
                registryResource = reg.toURI().toURL();
            } catch ( Throwable t ) {
            }
        }
        if ( is == null ) {
            try {
                is = StoreLoader.class
                     .getResourceAsStream ( "/org/apache/catalina/storeconfig/server-registry.xml" );
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Find registry server-registry.xml at classpath resource" );
                }
                registryResource = StoreLoader.class
                                   .getResource ( "/org/apache/catalina/storeconfig/server-registry.xml" );
            } catch ( Throwable t ) {
            }
        }
        if ( is != null ) {
            try {
                synchronized ( digester ) {
                    registry = ( StoreRegistry ) digester.parse ( is );
                }
            } catch ( Throwable t ) {
                log.error ( t );
            } finally {
                try {
                    is.close();
                } catch ( IOException e ) {
                }
            }
        }
        if ( is == null ) {
            log.error ( "Failed to load server-registry.xml" );
        }
    }
    private static String getCatalinaHome() {
        return System.getProperty ( "catalina.home", System
                                    .getProperty ( "user.dir" ) );
    }
    private static String getCatalinaBase() {
        return System.getProperty ( "catalina.base", getCatalinaHome() );
    }
    private static String getConfigUrl() {
        return System.getProperty ( "catalina.storeconfig" );
    }
    public URL getRegistryResource() {
        return registryResource;
    }
}
