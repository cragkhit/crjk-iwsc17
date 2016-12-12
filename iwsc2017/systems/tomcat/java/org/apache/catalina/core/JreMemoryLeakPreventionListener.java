package org.apache.catalina.core;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.DriverManager;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.compat.JreCompat;
import org.apache.tomcat.util.compat.JreVendor;
import org.apache.tomcat.util.res.StringManager;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
public class JreMemoryLeakPreventionListener implements LifecycleListener {
    private static final Log log =
        LogFactory.getLog ( JreMemoryLeakPreventionListener.class );
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private boolean awtThreadProtection = false;
    public boolean isAWTThreadProtection() {
        return awtThreadProtection;
    }
    public void setAWTThreadProtection ( boolean awtThreadProtection ) {
        this.awtThreadProtection = awtThreadProtection;
    }
    private boolean gcDaemonProtection = true;
    public boolean isGcDaemonProtection() {
        return gcDaemonProtection;
    }
    public void setGcDaemonProtection ( boolean gcDaemonProtection ) {
        this.gcDaemonProtection = gcDaemonProtection;
    }
    private boolean tokenPollerProtection = true;
    public boolean isTokenPollerProtection() {
        return tokenPollerProtection;
    }
    public void setTokenPollerProtection ( boolean tokenPollerProtection ) {
        this.tokenPollerProtection = tokenPollerProtection;
    }
    private boolean urlCacheProtection = true;
    public boolean isUrlCacheProtection() {
        return urlCacheProtection;
    }
    public void setUrlCacheProtection ( boolean urlCacheProtection ) {
        this.urlCacheProtection = urlCacheProtection;
    }
    private boolean xmlParsingProtection = true;
    public boolean isXmlParsingProtection() {
        return xmlParsingProtection;
    }
    public void setXmlParsingProtection ( boolean xmlParsingProtection ) {
        this.xmlParsingProtection = xmlParsingProtection;
    }
    private boolean ldapPoolProtection = true;
    public boolean isLdapPoolProtection() {
        return ldapPoolProtection;
    }
    public void setLdapPoolProtection ( boolean ldapPoolProtection ) {
        this.ldapPoolProtection = ldapPoolProtection;
    }
    private boolean driverManagerProtection = true;
    public boolean isDriverManagerProtection() {
        return driverManagerProtection;
    }
    public void setDriverManagerProtection ( boolean driverManagerProtection ) {
        this.driverManagerProtection = driverManagerProtection;
    }
    private String classesToInitialize = null;
    public String getClassesToInitialize() {
        return classesToInitialize;
    }
    public void setClassesToInitialize ( String classesToInitialize ) {
        this.classesToInitialize = classesToInitialize;
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( Lifecycle.BEFORE_INIT_EVENT.equals ( event.getType() ) ) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader (
                    ClassLoader.getSystemClassLoader() );
                if ( driverManagerProtection ) {
                    DriverManager.getDrivers();
                }
                if ( awtThreadProtection && !JreCompat.isJre9Available() ) {
                    java.awt.Toolkit.getDefaultToolkit();
                }
                if ( gcDaemonProtection && !JreCompat.isJre9Available() ) {
                    try {
                        Class<?> clazz = Class.forName ( "sun.misc.GC" );
                        Method method = clazz.getDeclaredMethod (
                                            "requestLatency",
                                            new Class[] {long.class} );
                        method.invoke ( null, Long.valueOf ( Long.MAX_VALUE - 1 ) );
                    } catch ( ClassNotFoundException e ) {
                        if ( JreVendor.IS_ORACLE_JVM ) {
                            log.error ( sm.getString (
                                            "jreLeakListener.gcDaemonFail" ), e );
                        } else {
                            log.debug ( sm.getString (
                                            "jreLeakListener.gcDaemonFail" ), e );
                        }
                    } catch ( SecurityException | NoSuchMethodException | IllegalArgumentException |
                                  IllegalAccessException e ) {
                        log.error ( sm.getString ( "jreLeakListener.gcDaemonFail" ),
                                    e );
                    } catch ( InvocationTargetException e ) {
                        ExceptionUtils.handleThrowable ( e.getCause() );
                        log.error ( sm.getString ( "jreLeakListener.gcDaemonFail" ),
                                    e );
                    }
                }
                if ( tokenPollerProtection && !JreCompat.isJre9Available() ) {
                    java.security.Security.getProviders();
                }
                if ( urlCacheProtection ) {
                    try {
                        URL url = new URL ( "jar:file://dummy.jar!/" );
                        URLConnection uConn = url.openConnection();
                        uConn.setDefaultUseCaches ( false );
                    } catch ( MalformedURLException e ) {
                        log.error ( sm.getString (
                                        "jreLeakListener.jarUrlConnCacheFail" ), e );
                    } catch ( IOException e ) {
                        log.error ( sm.getString (
                                        "jreLeakListener.jarUrlConnCacheFail" ), e );
                    }
                }
                if ( xmlParsingProtection && !JreCompat.isJre9Available() ) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                        Document document = documentBuilder.newDocument();
                        document.createElement ( "dummy" );
                        DOMImplementationLS implementation =
                            ( DOMImplementationLS ) document.getImplementation();
                        implementation.createLSSerializer().writeToString ( document );
                        document.normalize();
                    } catch ( ParserConfigurationException e ) {
                        log.error ( sm.getString ( "jreLeakListener.xmlParseFail" ),
                                    e );
                    }
                }
                if ( ldapPoolProtection && !JreCompat.isJre9Available() ) {
                    try {
                        Class.forName ( "com.sun.jndi.ldap.LdapPoolManager" );
                    } catch ( ClassNotFoundException e ) {
                        if ( JreVendor.IS_ORACLE_JVM ) {
                            log.error ( sm.getString (
                                            "jreLeakListener.ldapPoolManagerFail" ), e );
                        } else {
                            log.debug ( sm.getString (
                                            "jreLeakListener.ldapPoolManagerFail" ), e );
                        }
                    }
                }
                if ( classesToInitialize != null ) {
                    StringTokenizer strTok =
                        new StringTokenizer ( classesToInitialize, ", \r\n\t" );
                    while ( strTok.hasMoreTokens() ) {
                        String classNameToLoad = strTok.nextToken();
                        try {
                            Class.forName ( classNameToLoad );
                        } catch ( ClassNotFoundException e ) {
                            log.error (
                                sm.getString ( "jreLeakListener.classToInitializeFail",
                                               classNameToLoad ), e );
                        }
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader ( loader );
            }
        }
    }
}
