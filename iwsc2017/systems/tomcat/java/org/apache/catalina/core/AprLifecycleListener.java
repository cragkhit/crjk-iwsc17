package org.apache.catalina.core;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.Library;
import org.apache.tomcat.jni.LibraryNotFoundError;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class AprLifecycleListener
    implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( AprLifecycleListener.class );
    private static boolean instanceCreated = false;
    private static final List<String> initInfoLogMessages = new ArrayList<> ( 3 );
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected static final int TCN_REQUIRED_MAJOR = 1;
    protected static final int TCN_REQUIRED_MINOR = 2;
    protected static final int TCN_REQUIRED_PATCH = 6;
    protected static final int TCN_RECOMMENDED_MINOR = 2;
    protected static final int TCN_RECOMMENDED_PV = 8;
    protected static String SSLEngine = "on";
    protected static String FIPSMode = "off";
    protected static String SSLRandomSeed = "builtin";
    protected static boolean sslInitialized = false;
    protected static boolean aprInitialized = false;
    protected static boolean aprAvailable = false;
    protected static boolean useAprConnector = false;
    protected static boolean useOpenSSL = true;
    protected static boolean fipsModeActive = false;
    private static final int FIPS_ON = 1;
    private static final int FIPS_OFF = 0;
    protected static final Object lock = new Object();
    public static boolean isAprAvailable() {
        if ( instanceCreated ) {
            synchronized ( lock ) {
                init();
            }
        }
        return aprAvailable;
    }
    public AprLifecycleListener() {
        instanceCreated = true;
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( Lifecycle.BEFORE_INIT_EVENT.equals ( event.getType() ) ) {
            synchronized ( lock ) {
                init();
                for ( String msg : initInfoLogMessages ) {
                    log.info ( msg );
                }
                initInfoLogMessages.clear();
                if ( aprAvailable ) {
                    try {
                        initializeSSL();
                    } catch ( Throwable t ) {
                        t = ExceptionUtils.unwrapInvocationTargetException ( t );
                        ExceptionUtils.handleThrowable ( t );
                        log.error ( sm.getString ( "aprListener.sslInit" ), t );
                    }
                }
                if ( ! ( null == FIPSMode || "off".equalsIgnoreCase ( FIPSMode ) ) && !isFIPSModeActive() ) {
                    Error e = new Error (
                        sm.getString ( "aprListener.initializeFIPSFailed" ) );
                    log.fatal ( e.getMessage(), e );
                    throw e;
                }
            }
        } else if ( Lifecycle.AFTER_DESTROY_EVENT.equals ( event.getType() ) ) {
            synchronized ( lock ) {
                if ( !aprAvailable ) {
                    return;
                }
                try {
                    terminateAPR();
                } catch ( Throwable t ) {
                    t = ExceptionUtils.unwrapInvocationTargetException ( t );
                    ExceptionUtils.handleThrowable ( t );
                    log.info ( sm.getString ( "aprListener.aprDestroy" ) );
                }
            }
        }
    }
    private static void terminateAPR()
    throws ClassNotFoundException, NoSuchMethodException,
        IllegalAccessException, InvocationTargetException {
        String methodName = "terminate";
        Method method = Class.forName ( "org.apache.tomcat.jni.Library" )
                        .getMethod ( methodName, ( Class [] ) null );
        method.invoke ( null, ( Object [] ) null );
        aprAvailable = false;
        aprInitialized = false;
        sslInitialized = false;
        fipsModeActive = false;
    }
    private static void init() {
        int major = 0;
        int minor = 0;
        int patch = 0;
        int apver = 0;
        int rqver = TCN_REQUIRED_MAJOR * 1000 + TCN_REQUIRED_MINOR * 100 + TCN_REQUIRED_PATCH;
        int rcver = TCN_REQUIRED_MAJOR * 1000 + TCN_RECOMMENDED_MINOR * 100 + TCN_RECOMMENDED_PV;
        if ( aprInitialized ) {
            return;
        }
        aprInitialized = true;
        try {
            Library.initialize ( null );
            major = Library.TCN_MAJOR_VERSION;
            minor = Library.TCN_MINOR_VERSION;
            patch = Library.TCN_PATCH_VERSION;
            apver = major * 1000 + minor * 100 + patch;
        } catch ( LibraryNotFoundError lnfe ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "aprListener.aprInitDebug",
                                           lnfe.getLibraryNames(), System.getProperty ( "java.library.path" ),
                                           lnfe.getMessage() ), lnfe );
            }
            initInfoLogMessages.add ( sm.getString ( "aprListener.aprInit",
                                      System.getProperty ( "java.library.path" ) ) );
            return;
        } catch ( Throwable t ) {
            t = ExceptionUtils.unwrapInvocationTargetException ( t );
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString ( "aprListener.aprInitError", t.getMessage() ), t );
            return;
        }
        if ( apver < rqver ) {
            log.error ( sm.getString ( "aprListener.tcnInvalid", major + "."
                                       + minor + "." + patch,
                                       TCN_REQUIRED_MAJOR + "." +
                                       TCN_REQUIRED_MINOR + "." +
                                       TCN_REQUIRED_PATCH ) );
            try {
                terminateAPR();
            } catch ( Throwable t ) {
                t = ExceptionUtils.unwrapInvocationTargetException ( t );
                ExceptionUtils.handleThrowable ( t );
            }
            return;
        }
        if ( apver < rcver ) {
            initInfoLogMessages.add ( sm.getString ( "aprListener.tcnVersion",
                                      major + "." + minor + "." + patch,
                                      TCN_REQUIRED_MAJOR + "." +
                                      TCN_RECOMMENDED_MINOR + "." +
                                      TCN_RECOMMENDED_PV ) );
        }
        initInfoLogMessages.add ( sm.getString ( "aprListener.tcnValid",
                                  major + "." + minor + "." + patch,
                                  Library.APR_MAJOR_VERSION + "." +
                                  Library.APR_MINOR_VERSION + "." +
                                  Library.APR_PATCH_VERSION ) );
        initInfoLogMessages.add ( sm.getString ( "aprListener.flags",
                                  Boolean.valueOf ( Library.APR_HAVE_IPV6 ),
                                  Boolean.valueOf ( Library.APR_HAS_SENDFILE ),
                                  Boolean.valueOf ( Library.APR_HAS_SO_ACCEPTFILTER ),
                                  Boolean.valueOf ( Library.APR_HAS_RANDOM ) ) );
        initInfoLogMessages.add ( sm.getString ( "aprListener.config",
                                  Boolean.valueOf ( useAprConnector ),
                                  Boolean.valueOf ( useOpenSSL ) ) );
        aprAvailable = true;
    }
    private static void initializeSSL() throws Exception {
        if ( "off".equalsIgnoreCase ( SSLEngine ) ) {
            return;
        }
        if ( sslInitialized ) {
            return;
        }
        sslInitialized = true;
        String methodName = "randSet";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = String.class;
        Object paramValues[] = new Object[1];
        paramValues[0] = SSLRandomSeed;
        Class<?> clazz = Class.forName ( "org.apache.tomcat.jni.SSL" );
        Method method = clazz.getMethod ( methodName, paramTypes );
        method.invoke ( null, paramValues );
        methodName = "initialize";
        paramValues[0] = "on".equalsIgnoreCase ( SSLEngine ) ? null : SSLEngine;
        method = clazz.getMethod ( methodName, paramTypes );
        method.invoke ( null, paramValues );
        if ( ! ( null == FIPSMode || "off".equalsIgnoreCase ( FIPSMode ) ) ) {
            fipsModeActive = false;
            final boolean enterFipsMode;
            int fipsModeState = SSL.fipsModeGet();
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "aprListener.currentFIPSMode",
                                           Integer.valueOf ( fipsModeState ) ) );
            }
            if ( "on".equalsIgnoreCase ( FIPSMode ) ) {
                if ( fipsModeState == FIPS_ON ) {
                    log.info ( sm.getString ( "aprListener.skipFIPSInitialization" ) );
                    fipsModeActive = true;
                    enterFipsMode = false;
                } else {
                    enterFipsMode = true;
                }
            } else if ( "require".equalsIgnoreCase ( FIPSMode ) ) {
                if ( fipsModeState == FIPS_ON ) {
                    fipsModeActive = true;
                    enterFipsMode = false;
                } else {
                    throw new IllegalStateException (
                        sm.getString ( "aprListener.requireNotInFIPSMode" ) );
                }
            } else if ( "enter".equalsIgnoreCase ( FIPSMode ) ) {
                if ( fipsModeState == FIPS_OFF ) {
                    enterFipsMode = true;
                } else {
                    throw new IllegalStateException ( sm.getString (
                                                          "aprListener.enterAlreadyInFIPSMode",
                                                          Integer.valueOf ( fipsModeState ) ) );
                }
            } else {
                throw new IllegalArgumentException ( sm.getString (
                        "aprListener.wrongFIPSMode", FIPSMode ) );
            }
            if ( enterFipsMode ) {
                log.info ( sm.getString ( "aprListener.initializingFIPS" ) );
                fipsModeState = SSL.fipsModeSet ( FIPS_ON );
                if ( fipsModeState != FIPS_ON ) {
                    String message = sm.getString ( "aprListener.initializeFIPSFailed" );
                    log.error ( message );
                    throw new IllegalStateException ( message );
                }
                fipsModeActive = true;
                log.info ( sm.getString ( "aprListener.initializeFIPSSuccess" ) );
            }
        }
        log.info ( sm.getString ( "aprListener.initializedOpenSSL", SSL.versionString() ) );
    }
    public String getSSLEngine() {
        return SSLEngine;
    }
    public void setSSLEngine ( String SSLEngine ) {
        if ( !SSLEngine.equals ( AprLifecycleListener.SSLEngine ) ) {
            if ( sslInitialized ) {
                throw new IllegalStateException (
                    sm.getString ( "aprListener.tooLateForSSLEngine" ) );
            }
            AprLifecycleListener.SSLEngine = SSLEngine;
        }
    }
    public String getSSLRandomSeed() {
        return SSLRandomSeed;
    }
    public void setSSLRandomSeed ( String SSLRandomSeed ) {
        if ( !SSLRandomSeed.equals ( AprLifecycleListener.SSLRandomSeed ) ) {
            if ( sslInitialized ) {
                throw new IllegalStateException (
                    sm.getString ( "aprListener.tooLateForSSLRandomSeed" ) );
            }
            AprLifecycleListener.SSLRandomSeed = SSLRandomSeed;
        }
    }
    public String getFIPSMode() {
        return FIPSMode;
    }
    public void setFIPSMode ( String FIPSMode ) {
        if ( !FIPSMode.equals ( AprLifecycleListener.FIPSMode ) ) {
            if ( sslInitialized ) {
                throw new IllegalStateException (
                    sm.getString ( "aprListener.tooLateForFIPSMode" ) );
            }
            AprLifecycleListener.FIPSMode = FIPSMode;
        }
    }
    public boolean isFIPSModeActive() {
        return fipsModeActive;
    }
    public void setUseAprConnector ( boolean useAprConnector ) {
        if ( useAprConnector != AprLifecycleListener.useAprConnector ) {
            AprLifecycleListener.useAprConnector = useAprConnector;
        }
    }
    public static boolean getUseAprConnector() {
        return useAprConnector;
    }
    public void setUseOpenSSL ( boolean useOpenSSL ) {
        if ( useOpenSSL != AprLifecycleListener.useOpenSSL ) {
            AprLifecycleListener.useOpenSSL = useOpenSSL;
        }
    }
    public static boolean getUseOpenSSL() {
        return useOpenSSL;
    }
}
