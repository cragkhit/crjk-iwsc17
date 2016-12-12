package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardManager;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class ManagerSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
        if ( elementDesc != null ) {
            if ( aElement instanceof StandardManager ) {
                final StandardManager manager = ( StandardManager ) aElement;
                if ( !this.isDefaultManager ( manager ) ) {
                    if ( ManagerSF.log.isDebugEnabled() ) {
                        ManagerSF.log.debug ( ManagerSF.sm.getString ( "factory.storeTag", elementDesc.getTag(), aElement ) );
                    }
                    super.store ( aWriter, indent, aElement );
                }
            } else {
                super.store ( aWriter, indent, aElement );
            }
        } else if ( ManagerSF.log.isWarnEnabled() ) {
            ManagerSF.log.warn ( ManagerSF.sm.getString ( "factory.storeNoDescriptor", aElement.getClass() ) );
        }
    }
    protected boolean isDefaultManager ( final StandardManager smanager ) {
        return "SESSIONS.ser".equals ( smanager.getPathname() ) && smanager.getMaxActiveSessions() == -1;
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aManager, final StoreDescription parentDesc ) throws Exception {
        if ( aManager instanceof Manager ) {
            final Manager manager = ( Manager ) aManager;
            final SessionIdGenerator sessionIdGenerator = manager.getSessionIdGenerator();
            if ( sessionIdGenerator != null ) {
                this.storeElement ( aWriter, indent, sessionIdGenerator );
            }
        }
    }
    static {
        ManagerSF.log = LogFactory.getLog ( ManagerSF.class );
    }
}
