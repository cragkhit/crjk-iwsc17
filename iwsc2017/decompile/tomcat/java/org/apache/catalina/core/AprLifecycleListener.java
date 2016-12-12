package org.apache.catalina.core;
import java.util.ArrayList;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.LibraryNotFoundError;
import org.apache.tomcat.jni.Library;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.res.StringManager;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class AprLifecycleListener implements LifecycleListener {
    private static final Log log;
    private static boolean instanceCreated;
    private static final List<String> initInfoLogMessages;
    protected static final StringManager sm;
    protected static final int TCN_REQUIRED_MAJOR = 1;
    protected static final int TCN_REQUIRED_MINOR = 2;
    protected static final int TCN_REQUIRED_PATCH = 6;
    protected static final int TCN_RECOMMENDED_MINOR = 2;
    protected static final int TCN_RECOMMENDED_PV = 8;
    protected static String SSLEngine;
    protected static String FIPSMode;
    protected static String SSLRandomSeed;
    protected static boolean sslInitialized;
    protected static boolean aprInitialized;
    protected static boolean aprAvailable;
    protected static boolean useAprConnector;
    protected static boolean useOpenSSL;
    protected static boolean fipsModeActive;
    private static final int FIPS_ON = 1;
    private static final int FIPS_OFF = 0;
    protected static final Object lock;
    public static boolean isAprAvailable() {
        if ( AprLifecycleListener.instanceCreated ) {
            synchronized ( AprLifecycleListener.lock ) {
                init();
            }
        }
        return AprLifecycleListener.aprAvailable;
    }
    public AprLifecycleListener() {
        AprLifecycleListener.instanceCreated = true;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "before_init".equals ( event.getType() ) ) {
            synchronized ( AprLifecycleListener.lock ) {
                init();
                for ( final String msg : AprLifecycleListener.initInfoLogMessages ) {
                    AprLifecycleListener.log.info ( msg );
                }
                AprLifecycleListener.initInfoLogMessages.clear();
                if ( AprLifecycleListener.aprAvailable ) {
                    try {
                        initializeSSL();
                    } catch ( Throwable t ) {
                        t = ExceptionUtils.unwrapInvocationTargetException ( t );
                        ExceptionUtils.handleThrowable ( t );
                        AprLifecycleListener.log.error ( AprLifecycleListener.sm.getString ( "aprListener.sslInit" ), t );
                    }
                }
                if ( null != AprLifecycleListener.FIPSMode && !"off".equalsIgnoreCase ( AprLifecycleListener.FIPSMode ) && !this.isFIPSModeActive() ) {
                    final Error e = new Error ( AprLifecycleListener.sm.getString ( "aprListener.initializeFIPSFailed" ) );
                    AprLifecycleListener.log.fatal ( e.getMessage(), e );
                    throw e;
                }
            }
        } else if ( "after_destroy".equals ( event.getType() ) ) {
            synchronized ( AprLifecycleListener.lock ) {
                if ( !AprLifecycleListener.aprAvailable ) {
                    return;
                }
                try {
                    terminateAPR();
                } catch ( Throwable t ) {
                    t = ExceptionUtils.unwrapInvocationTargetException ( t );
                    ExceptionUtils.handleThrowable ( t );
                    AprLifecycleListener.log.info ( AprLifecycleListener.sm.getString ( "aprListener.aprDestroy" ) );
                }
            }
        }
    }
    private static void terminateAPR() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String methodName = "terminate";
        final Method method = Class.forName ( "org.apache.tomcat.jni.Library" ).getMethod ( methodName, ( Class<?>[] ) null );
        method.invoke ( null, ( Object[] ) null );
        AprLifecycleListener.aprAvailable = false;
        AprLifecycleListener.aprInitialized = false;
        AprLifecycleListener.sslInitialized = false;
        AprLifecycleListener.fipsModeActive = false;
    }
    private static void init() {
        int major = 0;
        int minor = 0;
        int patch = 0;
        int apver = 0;
        final int rqver = 1206;
        final int rcver = 1208;
        if ( AprLifecycleListener.aprInitialized ) {
            return;
        }
        AprLifecycleListener.aprInitialized = true;
        try {
            Library.initialize ( null );
            major = Library.TCN_MAJOR_VERSION;
            minor = Library.TCN_MINOR_VERSION;
            patch = Library.TCN_PATCH_VERSION;
            apver = major * 1000 + minor * 100 + patch;
        } catch ( LibraryNotFoundError lnfe ) {
            if ( AprLifecycleListener.log.isDebugEnabled() ) {
                AprLifecycleListener.log.debug ( AprLifecycleListener.sm.getString ( "aprListener.aprInitDebug", lnfe.getLibraryNames(), System.getProperty ( "java.library.path" ), lnfe.getMessage() ), lnfe );
            }
            AprLifecycleListener.initInfoLogMessages.add ( AprLifecycleListener.sm.getString ( "aprListener.aprInit", System.getProperty ( "java.library.path" ) ) );
            return;
        } catch ( Throwable t ) {
            t = ExceptionUtils.unwrapInvocationTargetException ( t );
            ExceptionUtils.handleThrowable ( t );
            AprLifecycleListener.log.warn ( AprLifecycleListener.sm.getString ( "aprListener.aprInitError", t.getMessage() ), t );
            return;
        }
        if ( apver < rqver ) {
            AprLifecycleListener.log.error ( AprLifecycleListener.sm.getString ( "aprListener.tcnInvalid", major + "." + minor + "." + patch, "1.2.6" ) );
            try {
                terminateAPR();
            } catch ( Throwable t ) {
                t = ExceptionUtils.unwrapInvocationTargetException ( t );
                ExceptionUtils.handleThrowable ( t );
            }
            return;
        }
        if ( apver < rcver ) {
            AprLifecycleListener.initInfoLogMessages.add ( AprLifecycleListener.sm.getString ( "aprListener.tcnVersion", major + "." + minor + "." + patch, "1.2.8" ) );
        }
        AprLifecycleListener.initInfoLogMessages.add ( AprLifecycleListener.sm.getString ( "aprListener.tcnValid", major + "." + minor + "." + patch, Library.APR_MAJOR_VERSION + "." + Library.APR_MINOR_VERSION + "." + Library.APR_PATCH_VERSION ) );
        AprLifecycleListener.initInfoLogMessages.add ( AprLifecycleListener.sm.getString ( "aprListener.flags", Library.APR_HAVE_IPV6, Library.APR_HAS_SENDFILE, Library.APR_HAS_SO_ACCEPTFILTER, Library.APR_HAS_RANDOM ) );
        AprLifecycleListener.initInfoLogMessages.add ( AprLifecycleListener.sm.getString ( "aprListener.config", AprLifecycleListener.useAprConnector, AprLifecycleListener.useOpenSSL ) );
        AprLifecycleListener.aprAvailable = true;
    }
    private static void initializeSSL() throws Exception {
        if ( "off".equalsIgnoreCase ( AprLifecycleListener.SSLEngine ) ) {
            return;
        }
        if ( AprLifecycleListener.sslInitialized ) {
            return;
        }
        AprLifecycleListener.sslInitialized = true;
        String methodName = "randSet";
        final Class<?>[] paramTypes = ( Class<?>[] ) new Class[] { String.class };
        final Object[] paramValues = { AprLifecycleListener.SSLRandomSeed };
        final Class<?> clazz = Class.forName ( "org.apache.tomcat.jni.SSL" );
        Method method = clazz.getMethod ( methodName, paramTypes );
        method.invoke ( null, paramValues );
        methodName = "initialize";
        paramValues[0] = ( "on".equalsIgnoreCase ( AprLifecycleListener.SSLEngine ) ? null : AprLifecycleListener.SSLEngine );
        method = clazz.getMethod ( methodName, paramTypes );
        method.invoke ( null, paramValues );
        if ( null != AprLifecycleListener.FIPSMode && !"off".equalsIgnoreCase ( AprLifecycleListener.FIPSMode ) ) {
            AprLifecycleListener.fipsModeActive = false;
            int fipsModeState = SSL.fipsModeGet();
            if ( AprLifecycleListener.log.isDebugEnabled() ) {
                AprLifecycleListener.log.debug ( AprLifecycleListener.sm.getString ( "aprListener.currentFIPSMode", fipsModeState ) );
            }
            boolean enterFipsMode;
            if ( "on".equalsIgnoreCase ( AprLifecycleListener.FIPSMode ) ) {
                if ( fipsModeState == 1 ) {
                    AprLifecycleListener.log.info ( AprLifecycleListener.sm.getString ( "aprListener.skipFIPSInitialization" ) );
                    AprLifecycleListener.fipsModeActive = true;
                    enterFipsMode = false;
                } else {
                    enterFipsMode = true;
                }
            } else if ( "require".equalsIgnoreCase ( AprLifecycleListener.FIPSMode ) ) {
                if ( fipsModeState != 1 ) {
                    throw new IllegalStateException ( AprLifecycleListener.sm.getString ( "aprListener.requireNotInFIPSMode" ) );
                }
                AprLifecycleListener.fipsModeActive = true;
                enterFipsMode = false;
            } else {
                if ( !"enter".equalsIgnoreCase ( AprLifecycleListener.FIPSMode ) ) {
                    throw new IllegalArgumentException ( AprLifecycleListener.sm.getString ( "aprListener.wrongFIPSMode", AprLifecycleListener.FIPSMode ) );
                }
                if ( fipsModeState != 0 ) {
                    throw new IllegalStateException ( AprLifecycleListener.sm.getString ( "aprListener.enterAlreadyInFIPSMode", fipsModeState ) );
                }
                enterFipsMode = true;
            }
            if ( enterFipsMode ) {
                AprLifecycleListener.log.info ( AprLifecycleListener.sm.getString ( "aprListener.initializingFIPS" ) );
                fipsModeState = SSL.fipsModeSet ( 1 );
                if ( fipsModeState != 1 ) {
                    final String message = AprLifecycleListener.sm.getString ( "aprListener.initializeFIPSFailed" );
                    AprLifecycleListener.log.error ( message );
                    throw new IllegalStateException ( message );
                }
                AprLifecycleListener.fipsModeActive = true;
                AprLifecycleListener.log.info ( AprLifecycleListener.sm.getString ( "aprListener.initializeFIPSSuccess" ) );
            }
        }
        AprLifecycleListener.log.info ( AprLifecycleListener.sm.getString ( "aprListener.initializedOpenSSL", SSL.versionString() ) );
    }
    public String getSSLEngine() {
        return AprLifecycleListener.SSLEngine;
    }
    public void setSSLEngine ( final String SSLEngine ) {
        if ( !SSLEngine.equals ( AprLifecycleListener.SSLEngine ) ) {
            if ( AprLifecycleListener.sslInitialized ) {
                throw new IllegalStateException ( AprLifecycleListener.sm.getString ( "aprListener.tooLateForSSLEngine" ) );
            }
            AprLifecycleListener.SSLEngine = SSLEngine;
        }
    }
    public String getSSLRandomSeed() {
        return AprLifecycleListener.SSLRandomSeed;
    }
    public void setSSLRandomSeed ( final String SSLRandomSeed ) {
        if ( !SSLRandomSeed.equals ( AprLifecycleListener.SSLRandomSeed ) ) {
            if ( AprLifecycleListener.sslInitialized ) {
                throw new IllegalStateException ( AprLifecycleListener.sm.getString ( "aprListener.tooLateForSSLRandomSeed" ) );
            }
            AprLifecycleListener.SSLRandomSeed = SSLRandomSeed;
        }
    }
    public String getFIPSMode() {
        return AprLifecycleListener.FIPSMode;
    }
    public void setFIPSMode ( final String FIPSMode ) {
        if ( !FIPSMode.equals ( AprLifecycleListener.FIPSMode ) ) {
            if ( AprLifecycleListener.sslInitialized ) {
                throw new IllegalStateException ( AprLifecycleListener.sm.getString ( "aprListener.tooLateForFIPSMode" ) );
            }
            AprLifecycleListener.FIPSMode = FIPSMode;
        }
    }
    public boolean isFIPSModeActive() {
        return AprLifecycleListener.fipsModeActive;
    }
    public void setUseAprConnector ( final boolean useAprConnector ) {
        if ( useAprConnector != AprLifecycleListener.useAprConnector ) {
            AprLifecycleListener.useAprConnector = useAprConnector;
        }
    }
    public static boolean getUseAprConnector() {
        return AprLifecycleListener.useAprConnector;
    }
    public void setUseOpenSSL ( final boolean useOpenSSL ) {
        if ( useOpenSSL != AprLifecycleListener.useOpenSSL ) {
            AprLifecycleListener.useOpenSSL = useOpenSSL;
        }
    }
    public static boolean getUseOpenSSL() {
        return AprLifecycleListener.useOpenSSL;
    }
    static {
        log = LogFactory.getLog ( AprLifecycleListener.class );
        AprLifecycleListener.instanceCreated = false;
        initInfoLogMessages = new ArrayList<String> ( 3 );
        sm = StringManager.getManager ( "org.apache.catalina.core" );
        AprLifecycleListener.SSLEngine = "on";
        AprLifecycleListener.FIPSMode = "off";
        AprLifecycleListener.SSLRandomSeed = "builtin";
        AprLifecycleListener.sslInitialized = false;
        AprLifecycleListener.aprInitialized = false;
        AprLifecycleListener.aprAvailable = false;
        AprLifecycleListener.useAprConnector = false;
        AprLifecycleListener.useOpenSSL = true;
        AprLifecycleListener.fipsModeActive = false;
        lock = new Object();
    }
}
