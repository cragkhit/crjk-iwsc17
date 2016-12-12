package org.apache.tomcat.util.net;
import javax.net.ssl.SSLSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.apache.tomcat.util.res.StringManager;
public abstract class SSLImplementation {
    private static final Log logger = LogFactory.getLog ( SSLImplementation.class );
    private static final StringManager sm = StringManager.getManager ( SSLImplementation.class );
    public static SSLImplementation getInstance ( String className )
    throws ClassNotFoundException {
        if ( className == null ) {
            return new JSSEImplementation();
        }
        try {
            Class<?> clazz = Class.forName ( className );
            return ( SSLImplementation ) clazz.newInstance();
        } catch ( Exception e ) {
            String msg = sm.getString ( "sslImplementation.cnfe", className );
            if ( logger.isDebugEnabled() ) {
                logger.debug ( msg, e );
            }
            throw new ClassNotFoundException ( msg, e );
        }
    }
    public abstract SSLSupport getSSLSupport ( SSLSession session );
    public abstract SSLUtil getSSLUtil ( SSLHostConfigCertificate certificate );
}
