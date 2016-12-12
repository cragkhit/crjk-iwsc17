package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public abstract class SSLImplementation {
    private static final Log logger;
    private static final StringManager sm;
    public static SSLImplementation getInstance ( final String className ) throws ClassNotFoundException {
        if ( className == null ) {
            return new JSSEImplementation();
        }
        try {
            final Class<?> clazz = Class.forName ( className );
            return ( SSLImplementation ) clazz.newInstance();
        } catch ( Exception e ) {
            final String msg = SSLImplementation.sm.getString ( "sslImplementation.cnfe", className );
            if ( SSLImplementation.logger.isDebugEnabled() ) {
                SSLImplementation.logger.debug ( msg, e );
            }
            throw new ClassNotFoundException ( msg, e );
        }
    }
    public abstract SSLSupport getSSLSupport ( final SSLSession p0 );
    public abstract SSLUtil getSSLUtil ( final SSLHostConfigCertificate p0 );
    static {
        logger = LogFactory.getLog ( SSLImplementation.class );
        sm = StringManager.getManager ( SSLImplementation.class );
    }
}
