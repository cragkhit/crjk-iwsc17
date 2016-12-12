package org.apache.tomcat.dbcp.dbcp2;
import org.apache.juli.logging.Log;
import org.apache.tomcat.dbcp.pool2.SwallowedExceptionListener;
public class SwallowedExceptionLogger implements SwallowedExceptionListener {
    private final Log log;
    private final boolean logExpiredConnections;
    public SwallowedExceptionLogger ( final Log log ) {
        this ( log, true );
    }
    public SwallowedExceptionLogger ( final Log log, final boolean logExpiredConnections ) {
        this.log = log;
        this.logExpiredConnections = logExpiredConnections;
    }
    @Override
    public void onSwallowException ( final Exception e ) {
        if ( logExpiredConnections || ! ( e instanceof LifetimeExceededException ) ) {
            log.warn ( Utils.getMessage (
                           "swallowedExceptionLogger.onSwallowedException" ), e );
        }
    }
}
