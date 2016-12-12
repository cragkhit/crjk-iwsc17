package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import java.net.URLConnection;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;
import java.lang.reflect.InvocationTargetException;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.compat.JreVendor;
import java.awt.Toolkit;
import org.apache.tomcat.util.compat.JreCompat;
import java.sql.DriverManager;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class JreMemoryLeakPreventionListener implements LifecycleListener {
    private static final Log log;
    private static final StringManager sm;
    private boolean awtThreadProtection;
    private boolean gcDaemonProtection;
    private boolean tokenPollerProtection;
    private boolean urlCacheProtection;
    private boolean xmlParsingProtection;
    private boolean ldapPoolProtection;
    private boolean driverManagerProtection;
    private String classesToInitialize;
    public JreMemoryLeakPreventionListener() {
        this.awtThreadProtection = false;
        this.gcDaemonProtection = true;
        this.tokenPollerProtection = true;
        this.urlCacheProtection = true;
        this.xmlParsingProtection = true;
        this.ldapPoolProtection = true;
        this.driverManagerProtection = true;
        this.classesToInitialize = null;
    }
    public boolean isAWTThreadProtection() {
        return this.awtThreadProtection;
    }
    public void setAWTThreadProtection ( final boolean awtThreadProtection ) {
        this.awtThreadProtection = awtThreadProtection;
    }
    public boolean isGcDaemonProtection() {
        return this.gcDaemonProtection;
    }
    public void setGcDaemonProtection ( final boolean gcDaemonProtection ) {
        this.gcDaemonProtection = gcDaemonProtection;
    }
    public boolean isTokenPollerProtection() {
        return this.tokenPollerProtection;
    }
    public void setTokenPollerProtection ( final boolean tokenPollerProtection ) {
        this.tokenPollerProtection = tokenPollerProtection;
    }
    public boolean isUrlCacheProtection() {
        return this.urlCacheProtection;
    }
    public void setUrlCacheProtection ( final boolean urlCacheProtection ) {
        this.urlCacheProtection = urlCacheProtection;
    }
    public boolean isXmlParsingProtection() {
        return this.xmlParsingProtection;
    }
    public void setXmlParsingProtection ( final boolean xmlParsingProtection ) {
        this.xmlParsingProtection = xmlParsingProtection;
    }
    public boolean isLdapPoolProtection() {
        return this.ldapPoolProtection;
    }
    public void setLdapPoolProtection ( final boolean ldapPoolProtection ) {
        this.ldapPoolProtection = ldapPoolProtection;
    }
    public boolean isDriverManagerProtection() {
        return this.driverManagerProtection;
    }
    public void setDriverManagerProtection ( final boolean driverManagerProtection ) {
        this.driverManagerProtection = driverManagerProtection;
    }
    public String getClassesToInitialize() {
        return this.classesToInitialize;
    }
    public void setClassesToInitialize ( final String classesToInitialize ) {
        this.classesToInitialize = classesToInitialize;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "before_init".equals ( event.getType() ) ) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader ( ClassLoader.getSystemClassLoader() );
                if ( this.driverManagerProtection ) {
                    DriverManager.getDrivers();
                }
                if ( this.awtThreadProtection && !JreCompat.isJre9Available() ) {
                    Toolkit.getDefaultToolkit();
                }
                if ( this.gcDaemonProtection && !JreCompat.isJre9Available() ) {
                    try {
                        final Class<?> clazz = Class.forName ( "sun.misc.GC" );
                        final Method method = clazz.getDeclaredMethod ( "requestLatency", Long.TYPE );
                        method.invoke ( null, 9223372036854775806L );
                    } catch ( ClassNotFoundException e ) {
                        if ( JreVendor.IS_ORACLE_JVM ) {
                            JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.gcDaemonFail" ), e );
                        } else {
                            JreMemoryLeakPreventionListener.log.debug ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.gcDaemonFail" ), e );
                        }
                    } catch ( SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException e2 ) {
                        JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.gcDaemonFail" ), e2 );
                    } catch ( InvocationTargetException e3 ) {
                        ExceptionUtils.handleThrowable ( e3.getCause() );
                        JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.gcDaemonFail" ), e3 );
                    }
                }
                if ( this.tokenPollerProtection && !JreCompat.isJre9Available() ) {
                    Security.getProviders();
                }
                if ( this.urlCacheProtection ) {
                    try {
                        final URL url = new URL ( "jar:file://dummy.jar!/" );
                        final URLConnection uConn = url.openConnection();
                        uConn.setDefaultUseCaches ( false );
                    } catch ( MalformedURLException e4 ) {
                        JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.jarUrlConnCacheFail" ), e4 );
                    } catch ( IOException e5 ) {
                        JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.jarUrlConnCacheFail" ), e5 );
                    }
                }
                if ( this.xmlParsingProtection && !JreCompat.isJre9Available() ) {
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    try {
                        final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                        final Document document = documentBuilder.newDocument();
                        document.createElement ( "dummy" );
                        final DOMImplementationLS implementation = ( DOMImplementationLS ) document.getImplementation();
                        implementation.createLSSerializer().writeToString ( document );
                        document.normalize();
                    } catch ( ParserConfigurationException e6 ) {
                        JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.xmlParseFail" ), e6 );
                    }
                }
                if ( this.ldapPoolProtection && !JreCompat.isJre9Available() ) {
                    try {
                        Class.forName ( "com.sun.jndi.ldap.LdapPoolManager" );
                    } catch ( ClassNotFoundException e ) {
                        if ( JreVendor.IS_ORACLE_JVM ) {
                            JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.ldapPoolManagerFail" ), e );
                        } else {
                            JreMemoryLeakPreventionListener.log.debug ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.ldapPoolManagerFail" ), e );
                        }
                    }
                }
                if ( this.classesToInitialize != null ) {
                    final StringTokenizer strTok = new StringTokenizer ( this.classesToInitialize, ", \r\n\t" );
                    while ( strTok.hasMoreTokens() ) {
                        final String classNameToLoad = strTok.nextToken();
                        try {
                            Class.forName ( classNameToLoad );
                        } catch ( ClassNotFoundException e7 ) {
                            JreMemoryLeakPreventionListener.log.error ( JreMemoryLeakPreventionListener.sm.getString ( "jreLeakListener.classToInitializeFail", classNameToLoad ), e7 );
                        }
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader ( loader );
            }
        }
    }
    static {
        log = LogFactory.getLog ( JreMemoryLeakPreventionListener.class );
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
