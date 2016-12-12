package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import java.io.InputStream;
import java.io.FileInputStream;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.File;
import org.apache.tomcat.util.digester.Rule;
import java.net.URL;
import org.apache.tomcat.util.digester.Digester;
import org.apache.juli.logging.Log;
public class StoreLoader {
    private static Log log;
    protected static final Digester digester;
    private StoreRegistry registry;
    private URL registryResource;
    public StoreRegistry getRegistry() {
        return this.registry;
    }
    public void setRegistry ( final StoreRegistry registry ) {
        this.registry = registry;
    }
    protected static Digester createDigester() {
        final long t1 = System.currentTimeMillis();
        final Digester digester = new Digester();
        digester.setValidating ( false );
        digester.setClassLoader ( StoreRegistry.class.getClassLoader() );
        digester.addObjectCreate ( "Registry", "org.apache.catalina.storeconfig.StoreRegistry", "className" );
        digester.addSetProperties ( "Registry" );
        digester.addObjectCreate ( "Registry/Description", "org.apache.catalina.storeconfig.StoreDescription", "className" );
        digester.addSetProperties ( "Registry/Description" );
        digester.addRule ( "Registry/Description", new StoreFactoryRule ( "org.apache.catalina.storeconfig.StoreFactoryBase", "storeFactoryClass", "org.apache.catalina.storeconfig.StoreAppender", "storeAppenderClass" ) );
        digester.addSetNext ( "Registry/Description", "registerDescription", "org.apache.catalina.storeconfig.StoreDescription" );
        digester.addCallMethod ( "Registry/Description/TransientAttribute", "addTransientAttribute", 0 );
        digester.addCallMethod ( "Registry/Description/TransientChild", "addTransientChild", 0 );
        final long t2 = System.currentTimeMillis();
        if ( StoreLoader.log.isDebugEnabled() ) {
            StoreLoader.log.debug ( "Digester for server-registry.xml created " + ( t2 - t1 ) );
        }
        return digester;
    }
    protected File serverFile ( String aFile ) {
        if ( aFile == null || aFile.length() < 1 ) {
            aFile = "server-registry.xml";
        }
        File file = new File ( aFile );
        if ( !file.isAbsolute() ) {
            file = new File ( System.getProperty ( "catalina.base" ) + "/conf", aFile );
        }
        try {
            file = file.getCanonicalFile();
        } catch ( IOException e ) {
            StoreLoader.log.error ( e );
        }
        return file;
    }
    public void load ( final String aURL ) {
        synchronized ( StoreLoader.digester ) {
            final File aRegistryFile = this.serverFile ( aURL );
            try {
                this.registry = ( StoreRegistry ) StoreLoader.digester.parse ( aRegistryFile );
                this.registryResource = aRegistryFile.toURI().toURL();
            } catch ( IOException e ) {
                StoreLoader.log.error ( e );
            } catch ( SAXException e2 ) {
                StoreLoader.log.error ( e2 );
            }
        }
    }
    public void load() {
        InputStream is = null;
        this.registryResource = null;
        try {
            final String configUrl = getConfigUrl();
            if ( configUrl != null ) {
                is = new URL ( configUrl ).openStream();
                if ( StoreLoader.log.isInfoEnabled() ) {
                    StoreLoader.log.info ( "Find registry server-registry.xml from system property at url " + configUrl );
                }
                this.registryResource = new URL ( configUrl );
            }
        } catch ( Throwable t2 ) {}
        if ( is == null ) {
            try {
                final File home = new File ( getCatalinaBase() );
                final File conf = new File ( home, "conf" );
                final File reg = new File ( conf, "server-registry.xml" );
                is = new FileInputStream ( reg );
                if ( StoreLoader.log.isInfoEnabled() ) {
                    StoreLoader.log.info ( "Find registry server-registry.xml at file " + reg.getCanonicalPath() );
                }
                this.registryResource = reg.toURI().toURL();
            } catch ( Throwable t3 ) {}
        }
        if ( is == null ) {
            try {
                is = StoreLoader.class.getResourceAsStream ( "/org/apache/catalina/storeconfig/server-registry.xml" );
                if ( StoreLoader.log.isDebugEnabled() ) {
                    StoreLoader.log.debug ( "Find registry server-registry.xml at classpath resource" );
                }
                this.registryResource = StoreLoader.class.getResource ( "/org/apache/catalina/storeconfig/server-registry.xml" );
            } catch ( Throwable t4 ) {}
        }
        if ( is != null ) {
            try {
                synchronized ( StoreLoader.digester ) {
                    this.registry = ( StoreRegistry ) StoreLoader.digester.parse ( is );
                }
            } catch ( Throwable t ) {
                StoreLoader.log.error ( t );
            } finally {
                try {
                    is.close();
                } catch ( IOException ex ) {}
            }
        }
        if ( is == null ) {
            StoreLoader.log.error ( "Failed to load server-registry.xml" );
        }
    }
    private static String getCatalinaHome() {
        return System.getProperty ( "catalina.home", System.getProperty ( "user.dir" ) );
    }
    private static String getCatalinaBase() {
        return System.getProperty ( "catalina.base", getCatalinaHome() );
    }
    private static String getConfigUrl() {
        return System.getProperty ( "catalina.storeconfig" );
    }
    public URL getRegistryResource() {
        return this.registryResource;
    }
    static {
        StoreLoader.log = LogFactory.getLog ( StoreLoader.class );
        digester = createDigester();
    }
}
