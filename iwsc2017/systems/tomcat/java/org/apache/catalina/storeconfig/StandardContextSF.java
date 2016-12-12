package org.apache.catalina.storeconfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.ThreadLocalLeakPreventionListener;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.util.ContextName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.util.http.CookieProcessor;
public class StandardContextSF extends StoreFactoryBase {
    private static Log log = LogFactory.getLog ( StandardContextSF.class );
    @Override
    public void store ( PrintWriter aWriter, int indent, Object aContext )
    throws Exception {
        if ( aContext instanceof StandardContext ) {
            StoreDescription desc = getRegistry().findDescription (
                                        aContext.getClass() );
            if ( desc.isStoreSeparate() ) {
                URL configFile = ( ( StandardContext ) aContext )
                                 .getConfigFile();
                if ( configFile != null ) {
                    if ( desc.isExternalAllowed() ) {
                        if ( desc.isBackup() ) {
                            storeWithBackup ( ( StandardContext ) aContext );
                        } else
                            storeContextSeparate ( aWriter, indent,
                                                   ( StandardContext ) aContext );
                        return;
                    }
                } else if ( desc.isExternalOnly() ) {
                    Context context = ( ( StandardContext ) aContext );
                    Host host = ( Host ) context.getParent();
                    File configBase = host.getConfigBaseFile();
                    ContextName cn = new ContextName ( context.getName(), false );
                    String baseName = cn.getBaseName();
                    File xml = new File ( configBase, baseName + ".xml" );
                    context.setConfigFile ( xml.toURI().toURL() );
                    if ( desc.isBackup() ) {
                        storeWithBackup ( ( StandardContext ) aContext );
                    } else
                        storeContextSeparate ( aWriter, indent,
                                               ( StandardContext ) aContext );
                    return;
                }
            }
        }
        super.store ( aWriter, indent, aContext );
    }
    protected void storeContextSeparate ( PrintWriter aWriter, int indent,
                                          StandardContext aContext ) throws Exception {
        URL configFile = aContext.getConfigFile();
        if ( configFile != null ) {
            File config = new File ( configFile.toURI() );
            if ( !config.isAbsolute() ) {
                config = new File ( System.getProperty ( "catalina.base" ),
                                    config.getPath() );
            }
            if ( ( !config.isFile() ) || ( !config.canWrite() ) ) {
                log.error ( "Cannot write context output file at "
                            + configFile + ", not saving." );
                throw new IOException ( "Context save file at "
                                        + configFile
                                        + " not a file, or not writable." );
            }
            if ( log.isInfoEnabled() )
                log.info ( "Store Context " + aContext.getPath()
                           + " separate at file " + config );
            try ( FileOutputStream fos = new FileOutputStream ( config );
                        PrintWriter writer = new PrintWriter ( new OutputStreamWriter (
                                    fos , getRegistry().getEncoding() ) ) ) {
                storeXMLHead ( writer );
                super.store ( writer, -2, aContext );
            }
        } else {
            super.store ( aWriter, indent, aContext );
        }
    }
    protected void storeWithBackup ( StandardContext aContext ) throws Exception {
        StoreFileMover mover = getConfigFileWriter ( aContext );
        if ( mover != null ) {
            if ( ( mover.getConfigOld() == null )
                    || ( mover.getConfigOld().isDirectory() )
                    || ( mover.getConfigOld().exists() &&
                         !mover.getConfigOld().canWrite() ) ) {
                log.error ( "Cannot move orignal context output file at "
                            + mover.getConfigOld() );
                throw new IOException ( "Context orginal file at "
                                        + mover.getConfigOld()
                                        + " is null, not a file or not writable." );
            }
            File dir = mover.getConfigSave().getParentFile();
            if ( dir != null && dir.isDirectory() && ( !dir.canWrite() ) ) {
                log.error ( "Cannot save context output file at "
                            + mover.getConfigSave() );
                throw new IOException ( "Context save file at "
                                        + mover.getConfigSave() + " is not writable." );
            }
            if ( log.isInfoEnabled() )
                log.info ( "Store Context " + aContext.getPath()
                           + " separate with backup (at file "
                           + mover.getConfigSave() + " )" );
            try ( PrintWriter writer = mover.getWriter() ) {
                storeXMLHead ( writer );
                super.store ( writer, -2, aContext );
            }
            mover.move();
        }
    }
    protected StoreFileMover getConfigFileWriter ( Context context )
    throws Exception {
        URL configFile = context.getConfigFile();
        StoreFileMover mover = null;
        if ( configFile != null ) {
            File config = new File ( configFile.toURI() );
            if ( !config.isAbsolute() ) {
                config = new File ( System.getProperty ( "catalina.base" ),
                                    config.getPath() );
            }
            mover = new StoreFileMover ( "", config.getCanonicalPath(),
                                         getRegistry().getEncoding() );
        }
        return mover;
    }
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aContext,
                                StoreDescription parentDesc ) throws Exception {
        if ( aContext instanceof StandardContext ) {
            StandardContext context = ( StandardContext ) aContext;
            LifecycleListener listeners[] = context.findLifecycleListeners();
            ArrayList<LifecycleListener> listenersArray = new ArrayList<>();
            for ( LifecycleListener listener : listeners ) {
                if ( ! ( listener instanceof ThreadLocalLeakPreventionListener ) ) {
                    listenersArray.add ( listener );
                }
            }
            storeElementArray ( aWriter, indent, listenersArray.toArray() );
            Valve valves[] = context.getPipeline().getValves();
            storeElementArray ( aWriter, indent, valves );
            Loader loader = context.getLoader();
            storeElement ( aWriter, indent, loader );
            if ( context.getCluster() == null || !context.getDistributable() ) {
                Manager manager = context.getManager();
                storeElement ( aWriter, indent, manager );
            }
            Realm realm = context.getRealm();
            if ( realm != null ) {
                Realm parentRealm = null;
                if ( context.getParent() != null ) {
                    parentRealm = context.getParent().getRealm();
                }
                if ( realm != parentRealm ) {
                    storeElement ( aWriter, indent, realm );
                }
            }
            WebResourceRoot resources = context.getResources();
            storeElement ( aWriter, indent, resources );
            String wLifecycles[] = context.findWrapperLifecycles();
            getStoreAppender().printTagArray ( aWriter, "WrapperListener",
                                               indent + 2, wLifecycles );
            String wListeners[] = context.findWrapperListeners();
            getStoreAppender().printTagArray ( aWriter, "WrapperLifecycle",
                                               indent + 2, wListeners );
            ApplicationParameter[] appParams = context
                                               .findApplicationParameters();
            storeElementArray ( aWriter, indent, appParams );
            NamingResourcesImpl nresources = context.getNamingResources();
            storeElement ( aWriter, indent, nresources );
            String[] wresources = context.findWatchedResources();
            wresources = filterWatchedResources ( context, wresources );
            getStoreAppender().printTagArray ( aWriter, "WatchedResource",
                                               indent + 2, wresources );
            JarScanner jarScanner = context.getJarScanner();
            storeElement ( aWriter, indent, jarScanner );
            CookieProcessor cookieProcessor = context.getCookieProcessor();
            storeElement ( aWriter, indent, cookieProcessor );
        }
    }
    protected File configBase ( Context context ) {
        File file = new File ( System.getProperty ( "catalina.base" ), "conf" );
        Container host = context.getParent();
        if ( host instanceof Host ) {
            Container engine = host.getParent();
            if ( engine instanceof Engine ) {
                file = new File ( file, engine.getName() );
            }
            file = new File ( file, host.getName() );
            try {
                file = file.getCanonicalFile();
            } catch ( IOException e ) {
                log.error ( e );
            }
        }
        return ( file );
    }
    protected String[] filterWatchedResources ( StandardContext context,
            String[] wresources ) throws Exception {
        File configBase = configBase ( context );
        String confContext = new File ( System.getProperty ( "catalina.base" ),
                                        "conf/context.xml" ).getCanonicalPath();
        String confWeb = new File ( System.getProperty ( "catalina.base" ),
                                    "conf/web.xml" ).getCanonicalPath();
        String confHostDefault = new File ( configBase, "context.xml.default" )
        .getCanonicalPath();
        String configFile = ( context.getConfigFile() != null ? new File ( context.getConfigFile().toURI() ).getCanonicalPath() : null );
        String webxml = "WEB-INF/web.xml" ;
        List<String> resource = new ArrayList<>();
        for ( int i = 0; i < wresources.length; i++ ) {
            if ( wresources[i].equals ( confContext ) ) {
                continue;
            }
            if ( wresources[i].equals ( confWeb ) ) {
                continue;
            }
            if ( wresources[i].equals ( confHostDefault ) ) {
                continue;
            }
            if ( wresources[i].equals ( configFile ) ) {
                continue;
            }
            if ( wresources[i].equals ( webxml ) ) {
                continue;
            }
            resource.add ( wresources[i] );
        }
        return resource.toArray ( new String[resource.size()] );
    }
}
