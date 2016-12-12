package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.JarScanner;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Realm;
import org.apache.catalina.Manager;
import org.apache.catalina.Loader;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ThreadLocalLeakPreventionListener;
import org.apache.catalina.LifecycleListener;
import java.util.ArrayList;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.catalina.Context;
import java.net.URL;
import java.io.File;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class StandardContextSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aContext ) throws Exception {
        if ( aContext instanceof StandardContext ) {
            final StoreDescription desc = this.getRegistry().findDescription ( aContext.getClass() );
            if ( desc.isStoreSeparate() ) {
                final URL configFile = ( ( StandardContext ) aContext ).getConfigFile();
                if ( configFile != null ) {
                    if ( desc.isExternalAllowed() ) {
                        if ( desc.isBackup() ) {
                            this.storeWithBackup ( ( StandardContext ) aContext );
                        } else {
                            this.storeContextSeparate ( aWriter, indent, ( StandardContext ) aContext );
                        }
                        return;
                    }
                } else if ( desc.isExternalOnly() ) {
                    final Context context = ( StandardContext ) aContext;
                    final Host host = ( Host ) context.getParent();
                    final File configBase = host.getConfigBaseFile();
                    final ContextName cn = new ContextName ( context.getName(), false );
                    final String baseName = cn.getBaseName();
                    final File xml = new File ( configBase, baseName + ".xml" );
                    context.setConfigFile ( xml.toURI().toURL() );
                    if ( desc.isBackup() ) {
                        this.storeWithBackup ( ( StandardContext ) aContext );
                    } else {
                        this.storeContextSeparate ( aWriter, indent, ( StandardContext ) aContext );
                    }
                    return;
                }
            }
        }
        super.store ( aWriter, indent, aContext );
    }
    protected void storeContextSeparate ( final PrintWriter aWriter, final int indent, final StandardContext aContext ) throws Exception {
        final URL configFile = aContext.getConfigFile();
        if ( configFile != null ) {
            File config = new File ( configFile.toURI() );
            if ( !config.isAbsolute() ) {
                config = new File ( System.getProperty ( "catalina.base" ), config.getPath() );
            }
            if ( !config.isFile() || !config.canWrite() ) {
                StandardContextSF.log.error ( "Cannot write context output file at " + configFile + ", not saving." );
                throw new IOException ( "Context save file at " + configFile + " not a file, or not writable." );
            }
            if ( StandardContextSF.log.isInfoEnabled() ) {
                StandardContextSF.log.info ( "Store Context " + aContext.getPath() + " separate at file " + config );
            }
            try ( final FileOutputStream fos = new FileOutputStream ( config );
                        final PrintWriter writer = new PrintWriter ( new OutputStreamWriter ( fos, this.getRegistry().getEncoding() ) ) ) {
                this.storeXMLHead ( writer );
                super.store ( writer, -2, aContext );
            }
        } else {
            super.store ( aWriter, indent, aContext );
        }
    }
    protected void storeWithBackup ( final StandardContext aContext ) throws Exception {
        final StoreFileMover mover = this.getConfigFileWriter ( aContext );
        if ( mover != null ) {
            if ( mover.getConfigOld() == null || mover.getConfigOld().isDirectory() || ( mover.getConfigOld().exists() && !mover.getConfigOld().canWrite() ) ) {
                StandardContextSF.log.error ( "Cannot move orignal context output file at " + mover.getConfigOld() );
                throw new IOException ( "Context orginal file at " + mover.getConfigOld() + " is null, not a file or not writable." );
            }
            final File dir = mover.getConfigSave().getParentFile();
            if ( dir != null && dir.isDirectory() && !dir.canWrite() ) {
                StandardContextSF.log.error ( "Cannot save context output file at " + mover.getConfigSave() );
                throw new IOException ( "Context save file at " + mover.getConfigSave() + " is not writable." );
            }
            if ( StandardContextSF.log.isInfoEnabled() ) {
                StandardContextSF.log.info ( "Store Context " + aContext.getPath() + " separate with backup (at file " + mover.getConfigSave() + " )" );
            }
            try ( final PrintWriter writer = mover.getWriter() ) {
                this.storeXMLHead ( writer );
                super.store ( writer, -2, aContext );
            }
            mover.move();
        }
    }
    protected StoreFileMover getConfigFileWriter ( final Context context ) throws Exception {
        final URL configFile = context.getConfigFile();
        StoreFileMover mover = null;
        if ( configFile != null ) {
            File config = new File ( configFile.toURI() );
            if ( !config.isAbsolute() ) {
                config = new File ( System.getProperty ( "catalina.base" ), config.getPath() );
            }
            mover = new StoreFileMover ( "", config.getCanonicalPath(), this.getRegistry().getEncoding() );
        }
        return mover;
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aContext, final StoreDescription parentDesc ) throws Exception {
        if ( aContext instanceof StandardContext ) {
            final StandardContext context = ( StandardContext ) aContext;
            final LifecycleListener[] listeners = context.findLifecycleListeners();
            final ArrayList<LifecycleListener> listenersArray = new ArrayList<LifecycleListener>();
            for ( final LifecycleListener listener : listeners ) {
                if ( ! ( listener instanceof ThreadLocalLeakPreventionListener ) ) {
                    listenersArray.add ( listener );
                }
            }
            this.storeElementArray ( aWriter, indent, listenersArray.toArray() );
            final Valve[] valves = context.getPipeline().getValves();
            this.storeElementArray ( aWriter, indent, valves );
            final Loader loader = context.getLoader();
            this.storeElement ( aWriter, indent, loader );
            if ( context.getCluster() == null || !context.getDistributable() ) {
                final Manager manager = context.getManager();
                this.storeElement ( aWriter, indent, manager );
            }
            final Realm realm = context.getRealm();
            if ( realm != null ) {
                Realm parentRealm = null;
                if ( context.getParent() != null ) {
                    parentRealm = context.getParent().getRealm();
                }
                if ( realm != parentRealm ) {
                    this.storeElement ( aWriter, indent, realm );
                }
            }
            final WebResourceRoot resources = context.getResources();
            this.storeElement ( aWriter, indent, resources );
            final String[] wLifecycles = context.findWrapperLifecycles();
            this.getStoreAppender().printTagArray ( aWriter, "WrapperListener", indent + 2, wLifecycles );
            final String[] wListeners = context.findWrapperListeners();
            this.getStoreAppender().printTagArray ( aWriter, "WrapperLifecycle", indent + 2, wListeners );
            final ApplicationParameter[] appParams = context.findApplicationParameters();
            this.storeElementArray ( aWriter, indent, appParams );
            final NamingResourcesImpl nresources = context.getNamingResources();
            this.storeElement ( aWriter, indent, nresources );
            String[] wresources = context.findWatchedResources();
            wresources = this.filterWatchedResources ( context, wresources );
            this.getStoreAppender().printTagArray ( aWriter, "WatchedResource", indent + 2, wresources );
            final JarScanner jarScanner = context.getJarScanner();
            this.storeElement ( aWriter, indent, jarScanner );
            final CookieProcessor cookieProcessor = context.getCookieProcessor();
            this.storeElement ( aWriter, indent, cookieProcessor );
        }
    }
    protected File configBase ( final Context context ) {
        File file = new File ( System.getProperty ( "catalina.base" ), "conf" );
        final Container host = context.getParent();
        if ( host instanceof Host ) {
            final Container engine = host.getParent();
            if ( engine instanceof Engine ) {
                file = new File ( file, engine.getName() );
            }
            file = new File ( file, host.getName() );
            try {
                file = file.getCanonicalFile();
            } catch ( IOException e ) {
                StandardContextSF.log.error ( e );
            }
        }
        return file;
    }
    protected String[] filterWatchedResources ( final StandardContext context, final String[] wresources ) throws Exception {
        final File configBase = this.configBase ( context );
        final String confContext = new File ( System.getProperty ( "catalina.base" ), "conf/context.xml" ).getCanonicalPath();
        final String confWeb = new File ( System.getProperty ( "catalina.base" ), "conf/web.xml" ).getCanonicalPath();
        final String confHostDefault = new File ( configBase, "context.xml.default" ).getCanonicalPath();
        final String configFile = ( context.getConfigFile() != null ) ? new File ( context.getConfigFile().toURI() ).getCanonicalPath() : null;
        final String webxml = "WEB-INF/web.xml";
        final List<String> resource = new ArrayList<String>();
        for ( int i = 0; i < wresources.length; ++i ) {
            if ( !wresources[i].equals ( confContext ) ) {
                if ( !wresources[i].equals ( confWeb ) ) {
                    if ( !wresources[i].equals ( confHostDefault ) ) {
                        if ( !wresources[i].equals ( configFile ) ) {
                            if ( !wresources[i].equals ( webxml ) ) {
                                resource.add ( wresources[i] );
                            }
                        }
                    }
                }
            }
        }
        return resource.toArray ( new String[resource.size()] );
    }
    static {
        StandardContextSF.log = LogFactory.getLog ( StandardContextSF.class );
    }
}
