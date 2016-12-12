package org.apache.catalina.util;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.SessionIdGenerator;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public abstract class SessionIdGeneratorBase extends LifecycleBase
    implements SessionIdGenerator {
    private static final Log log = LogFactory.getLog ( SessionIdGeneratorBase.class );
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    private final Queue<SecureRandom> randoms = new ConcurrentLinkedQueue<>();
    private String secureRandomClass = null;
    private String secureRandomAlgorithm = "SHA1PRNG";
    private String secureRandomProvider = null;
    private String jvmRoute = "";
    private int sessionIdLength = 16;
    public String getSecureRandomClass() {
        return secureRandomClass;
    }
    public void setSecureRandomClass ( String secureRandomClass ) {
        this.secureRandomClass = secureRandomClass;
    }
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }
    public void setSecureRandomAlgorithm ( String secureRandomAlgorithm ) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }
    public String getSecureRandomProvider() {
        return secureRandomProvider;
    }
    public void setSecureRandomProvider ( String secureRandomProvider ) {
        this.secureRandomProvider = secureRandomProvider;
    }
    @Override
    public String getJvmRoute() {
        return jvmRoute;
    }
    @Override
    public void setJvmRoute ( String jvmRoute ) {
        this.jvmRoute = jvmRoute;
    }
    @Override
    public int getSessionIdLength() {
        return sessionIdLength;
    }
    @Override
    public void setSessionIdLength ( int sessionIdLength ) {
        this.sessionIdLength = sessionIdLength;
    }
    @Override
    public String generateSessionId() {
        return generateSessionId ( jvmRoute );
    }
    protected void getRandomBytes ( byte bytes[] ) {
        SecureRandom random = randoms.poll();
        if ( random == null ) {
            random = createSecureRandom();
        }
        random.nextBytes ( bytes );
        randoms.add ( random );
    }
    private SecureRandom createSecureRandom() {
        SecureRandom result = null;
        long t1 = System.currentTimeMillis();
        if ( secureRandomClass != null ) {
            try {
                Class<?> clazz = Class.forName ( secureRandomClass );
                result = ( SecureRandom ) clazz.newInstance();
            } catch ( Exception e ) {
                log.error ( sm.getString ( "sessionIdGeneratorBase.random",
                                           secureRandomClass ), e );
            }
        }
        if ( result == null ) {
            try {
                if ( secureRandomProvider != null &&
                        secureRandomProvider.length() > 0 ) {
                    result = SecureRandom.getInstance ( secureRandomAlgorithm,
                                                        secureRandomProvider );
                } else if ( secureRandomAlgorithm != null &&
                            secureRandomAlgorithm.length() > 0 ) {
                    result = SecureRandom.getInstance ( secureRandomAlgorithm );
                }
            } catch ( NoSuchAlgorithmException e ) {
                log.error ( sm.getString ( "sessionIdGeneratorBase.randomAlgorithm",
                                           secureRandomAlgorithm ), e );
            } catch ( NoSuchProviderException e ) {
                log.error ( sm.getString ( "sessionIdGeneratorBase.randomProvider",
                                           secureRandomProvider ), e );
            }
        }
        if ( result == null ) {
            try {
                result = SecureRandom.getInstance ( "SHA1PRNG" );
            } catch ( NoSuchAlgorithmException e ) {
                log.error ( sm.getString ( "sessionIdGeneratorBase.randomAlgorithm",
                                           secureRandomAlgorithm ), e );
            }
        }
        if ( result == null ) {
            result = new SecureRandom();
        }
        result.nextInt();
        long t2 = System.currentTimeMillis();
        if ( ( t2 - t1 ) > 100 )
            log.info ( sm.getString ( "sessionIdGeneratorBase.createRandom",
                                      result.getAlgorithm(), Long.valueOf ( t2 - t1 ) ) );
        return result;
    }
    @Override
    protected void initInternal() throws LifecycleException {
    }
    @Override
    protected void startInternal() throws LifecycleException {
        generateSessionId();
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        randoms.clear();
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
    }
}
