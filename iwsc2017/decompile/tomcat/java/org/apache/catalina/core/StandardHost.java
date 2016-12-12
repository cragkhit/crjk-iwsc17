package org.apache.catalina.core;
import org.apache.catalina.LifecycleEvent;
import org.apache.juli.logging.LogFactory;
import javax.management.ObjectName;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Iterator;
import java.util.List;
import org.apache.catalina.loader.WebappClassLoaderBase;
import java.util.ArrayList;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;
import java.util.Locale;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.apache.catalina.Valve;
import java.util.WeakHashMap;
import org.apache.catalina.Globals;
import java.util.regex.Pattern;
import java.util.Map;
import java.io.File;
import org.apache.juli.logging.Log;
import org.apache.catalina.Host;
public class StandardHost extends ContainerBase implements Host {
    private static final Log log;
    private String[] aliases;
    private final Object aliasesLock;
    private String appBase;
    private volatile File appBaseFile;
    private String xmlBase;
    private volatile File hostConfigBase;
    private boolean autoDeploy;
    private String configClass;
    private String contextClass;
    private boolean deployOnStartup;
    private boolean deployXML;
    private boolean copyXML;
    private String errorReportValveClass;
    private boolean unpackWARs;
    private String workDir;
    private boolean createDirs;
    private final Map<ClassLoader, String> childClassLoaders;
    private Pattern deployIgnore;
    private boolean undeployOldVersions;
    private boolean failCtxIfServletStartFails;
    public StandardHost() {
        this.aliases = new String[0];
        this.aliasesLock = new Object();
        this.appBase = "webapps";
        this.appBaseFile = null;
        this.xmlBase = null;
        this.hostConfigBase = null;
        this.autoDeploy = true;
        this.configClass = "org.apache.catalina.startup.ContextConfig";
        this.contextClass = "org.apache.catalina.core.StandardContext";
        this.deployOnStartup = true;
        this.deployXML = !Globals.IS_SECURITY_ENABLED;
        this.copyXML = false;
        this.errorReportValveClass = "org.apache.catalina.valves.ErrorReportValve";
        this.unpackWARs = true;
        this.workDir = null;
        this.createDirs = true;
        this.childClassLoaders = new WeakHashMap<ClassLoader, String>();
        this.deployIgnore = null;
        this.undeployOldVersions = false;
        this.failCtxIfServletStartFails = false;
        this.pipeline.setBasic ( new StandardHostValve() );
    }
    @Override
    public boolean getUndeployOldVersions() {
        return this.undeployOldVersions;
    }
    @Override
    public void setUndeployOldVersions ( final boolean undeployOldVersions ) {
        this.undeployOldVersions = undeployOldVersions;
    }
    @Override
    public ExecutorService getStartStopExecutor() {
        return this.startStopExecutor;
    }
    @Override
    public String getAppBase() {
        return this.appBase;
    }
    @Override
    public File getAppBaseFile() {
        if ( this.appBaseFile != null ) {
            return this.appBaseFile;
        }
        File file = new File ( this.getAppBase() );
        if ( !file.isAbsolute() ) {
            file = new File ( this.getCatalinaBase(), file.getPath() );
        }
        try {
            file = file.getCanonicalFile();
        } catch ( IOException ex ) {}
        return this.appBaseFile = file;
    }
    @Override
    public void setAppBase ( final String appBase ) {
        if ( appBase.trim().equals ( "" ) ) {
            StandardHost.log.warn ( StandardHost.sm.getString ( "standardHost.problematicAppBase", this.getName() ) );
        }
        final String oldAppBase = this.appBase;
        this.appBase = appBase;
        this.support.firePropertyChange ( "appBase", oldAppBase, this.appBase );
        this.appBaseFile = null;
    }
    @Override
    public String getXmlBase() {
        return this.xmlBase;
    }
    @Override
    public void setXmlBase ( final String xmlBase ) {
        final String oldXmlBase = this.xmlBase;
        this.xmlBase = xmlBase;
        this.support.firePropertyChange ( "xmlBase", oldXmlBase, this.xmlBase );
    }
    @Override
    public File getConfigBaseFile() {
        if ( this.hostConfigBase != null ) {
            return this.hostConfigBase;
        }
        String path = null;
        if ( this.getXmlBase() != null ) {
            path = this.getXmlBase();
        } else {
            final StringBuilder xmlDir = new StringBuilder ( "conf" );
            final Container parent = this.getParent();
            if ( parent instanceof Engine ) {
                xmlDir.append ( '/' );
                xmlDir.append ( parent.getName() );
            }
            xmlDir.append ( '/' );
            xmlDir.append ( this.getName() );
            path = xmlDir.toString();
        }
        File file = new File ( path );
        if ( !file.isAbsolute() ) {
            file = new File ( this.getCatalinaBase(), path );
        }
        try {
            file = file.getCanonicalFile();
        } catch ( IOException ex ) {}
        return this.hostConfigBase = file;
    }
    @Override
    public boolean getCreateDirs() {
        return this.createDirs;
    }
    @Override
    public void setCreateDirs ( final boolean createDirs ) {
        this.createDirs = createDirs;
    }
    @Override
    public boolean getAutoDeploy() {
        return this.autoDeploy;
    }
    @Override
    public void setAutoDeploy ( final boolean autoDeploy ) {
        final boolean oldAutoDeploy = this.autoDeploy;
        this.autoDeploy = autoDeploy;
        this.support.firePropertyChange ( "autoDeploy", oldAutoDeploy, this.autoDeploy );
    }
    @Override
    public String getConfigClass() {
        return this.configClass;
    }
    @Override
    public void setConfigClass ( final String configClass ) {
        final String oldConfigClass = this.configClass;
        this.configClass = configClass;
        this.support.firePropertyChange ( "configClass", oldConfigClass, this.configClass );
    }
    public String getContextClass() {
        return this.contextClass;
    }
    public void setContextClass ( final String contextClass ) {
        final String oldContextClass = this.contextClass;
        this.contextClass = contextClass;
        this.support.firePropertyChange ( "contextClass", oldContextClass, this.contextClass );
    }
    @Override
    public boolean getDeployOnStartup() {
        return this.deployOnStartup;
    }
    @Override
    public void setDeployOnStartup ( final boolean deployOnStartup ) {
        final boolean oldDeployOnStartup = this.deployOnStartup;
        this.deployOnStartup = deployOnStartup;
        this.support.firePropertyChange ( "deployOnStartup", oldDeployOnStartup, this.deployOnStartup );
    }
    public boolean isDeployXML() {
        return this.deployXML;
    }
    public void setDeployXML ( final boolean deployXML ) {
        this.deployXML = deployXML;
    }
    public boolean isCopyXML() {
        return this.copyXML;
    }
    public void setCopyXML ( final boolean copyXML ) {
        this.copyXML = copyXML;
    }
    public String getErrorReportValveClass() {
        return this.errorReportValveClass;
    }
    public void setErrorReportValveClass ( final String errorReportValveClass ) {
        final String oldErrorReportValveClassClass = this.errorReportValveClass;
        this.errorReportValveClass = errorReportValveClass;
        this.support.firePropertyChange ( "errorReportValveClass", oldErrorReportValveClassClass, this.errorReportValveClass );
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName ( String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( StandardHost.sm.getString ( "standardHost.nullName" ) );
        }
        name = name.toLowerCase ( Locale.ENGLISH );
        final String oldName = this.name;
        this.name = name;
        this.support.firePropertyChange ( "name", oldName, this.name );
    }
    public boolean isUnpackWARs() {
        return this.unpackWARs;
    }
    public void setUnpackWARs ( final boolean unpackWARs ) {
        this.unpackWARs = unpackWARs;
    }
    public String getWorkDir() {
        return this.workDir;
    }
    public void setWorkDir ( final String workDir ) {
        this.workDir = workDir;
    }
    @Override
    public String getDeployIgnore() {
        if ( this.deployIgnore == null ) {
            return null;
        }
        return this.deployIgnore.toString();
    }
    @Override
    public Pattern getDeployIgnorePattern() {
        return this.deployIgnore;
    }
    @Override
    public void setDeployIgnore ( final String deployIgnore ) {
        String oldDeployIgnore;
        if ( this.deployIgnore == null ) {
            oldDeployIgnore = null;
        } else {
            oldDeployIgnore = this.deployIgnore.toString();
        }
        if ( deployIgnore == null ) {
            this.deployIgnore = null;
        } else {
            this.deployIgnore = Pattern.compile ( deployIgnore );
        }
        this.support.firePropertyChange ( "deployIgnore", oldDeployIgnore, deployIgnore );
    }
    public boolean isFailCtxIfServletStartFails() {
        return this.failCtxIfServletStartFails;
    }
    public void setFailCtxIfServletStartFails ( final boolean failCtxIfServletStartFails ) {
        final boolean oldFailCtxIfServletStartFails = this.failCtxIfServletStartFails;
        this.failCtxIfServletStartFails = failCtxIfServletStartFails;
        this.support.firePropertyChange ( "failCtxIfServletStartFails", oldFailCtxIfServletStartFails, failCtxIfServletStartFails );
    }
    @Override
    public void addAlias ( String alias ) {
        alias = alias.toLowerCase ( Locale.ENGLISH );
        synchronized ( this.aliasesLock ) {
            for ( int i = 0; i < this.aliases.length; ++i ) {
                if ( this.aliases[i].equals ( alias ) ) {
                    return;
                }
            }
            final String[] newAliases = new String[this.aliases.length + 1];
            for ( int j = 0; j < this.aliases.length; ++j ) {
                newAliases[j] = this.aliases[j];
            }
            newAliases[this.aliases.length] = alias;
            this.aliases = newAliases;
        }
        this.fireContainerEvent ( "addAlias", alias );
    }
    @Override
    public void addChild ( final Container child ) {
        child.addLifecycleListener ( new MemoryLeakTrackingListener() );
        if ( ! ( child instanceof Context ) ) {
            throw new IllegalArgumentException ( StandardHost.sm.getString ( "standardHost.notContext" ) );
        }
        super.addChild ( child );
    }
    public String[] findReloadedContextMemoryLeaks() {
        System.gc();
        final List<String> result = new ArrayList<String>();
        for ( final Map.Entry<ClassLoader, String> entry : this.childClassLoaders.entrySet() ) {
            final ClassLoader cl = entry.getKey();
            if ( cl instanceof WebappClassLoaderBase && ! ( ( WebappClassLoaderBase ) cl ).getState().isAvailable() ) {
                result.add ( entry.getValue() );
            }
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public String[] findAliases() {
        synchronized ( this.aliasesLock ) {
            return this.aliases;
        }
    }
    @Override
    public void removeAlias ( String alias ) {
        alias = alias.toLowerCase ( Locale.ENGLISH );
        synchronized ( this.aliasesLock ) {
            int n = -1;
            for ( int i = 0; i < this.aliases.length; ++i ) {
                if ( this.aliases[i].equals ( alias ) ) {
                    n = i;
                    break;
                }
            }
            if ( n < 0 ) {
                return;
            }
            int j = 0;
            final String[] results = new String[this.aliases.length - 1];
            for ( int k = 0; k < this.aliases.length; ++k ) {
                if ( k != n ) {
                    results[j++] = this.aliases[k];
                }
            }
            this.aliases = results;
        }
        this.fireContainerEvent ( "removeAlias", alias );
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        final String errorValve = this.getErrorReportValveClass();
        if ( errorValve != null && !errorValve.equals ( "" ) ) {
            try {
                boolean found = false;
                final Valve[] valves2;
                final Valve[] valves = valves2 = this.getPipeline().getValves();
                for ( final Valve valve : valves2 ) {
                    if ( errorValve.equals ( valve.getClass().getName() ) ) {
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    final Valve valve2 = ( Valve ) Class.forName ( errorValve ).newInstance();
                    this.getPipeline().addValve ( valve2 );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                StandardHost.log.error ( StandardHost.sm.getString ( "standardHost.invalidErrorReportValveClass", errorValve ), t );
            }
        }
        super.startInternal();
    }
    public String[] getValveNames() throws Exception {
        final Valve[] valves = this.getPipeline().getValves();
        final String[] mbeanNames = new String[valves.length];
        for ( int i = 0; i < valves.length; ++i ) {
            if ( valves[i] instanceof JmxEnabled ) {
                final ObjectName oname = ( ( JmxEnabled ) valves[i] ).getObjectName();
                if ( oname != null ) {
                    mbeanNames[i] = oname.toString();
                }
            }
        }
        return mbeanNames;
    }
    public String[] getAliases() {
        synchronized ( this.aliasesLock ) {
            return this.aliases;
        }
    }
    @Override
    protected String getObjectNameKeyProperties() {
        final StringBuilder keyProperties = new StringBuilder ( "type=Host" );
        keyProperties.append ( this.getMBeanKeyProperties() );
        return keyProperties.toString();
    }
    static {
        log = LogFactory.getLog ( StandardHost.class );
    }
    private class MemoryLeakTrackingListener implements LifecycleListener {
        @Override
        public void lifecycleEvent ( final LifecycleEvent event ) {
            if ( event.getType().equals ( "after_start" ) && event.getSource() instanceof Context ) {
                final Context context = ( Context ) event.getSource();
                StandardHost.this.childClassLoaders.put ( context.getLoader().getClassLoader(), context.getServletContext().getContextPath() );
            }
        }
    }
}
