package org.apache.catalina.valves;
import org.apache.catalina.Globals;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Manager;
import org.apache.catalina.Context;
import org.apache.catalina.StoreManager;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import org.apache.catalina.Host;
import org.apache.catalina.Engine;
import org.apache.catalina.Container;
public class PersistentValve extends ValveBase {
    private static final ClassLoader MY_CLASSLOADER;
    private volatile boolean clBindRequired;
    public PersistentValve() {
        super ( true );
    }
    @Override
    public void setContainer ( final Container container ) {
        super.setContainer ( container );
        if ( container instanceof Engine || container instanceof Host ) {
            this.clBindRequired = true;
        } else {
            this.clBindRequired = false;
        }
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        final Context context = request.getContext();
        if ( context == null ) {
            response.sendError ( 500, PersistentValve.sm.getString ( "standardHost.noContext" ) );
            return;
        }
        final String sessionId = request.getRequestedSessionId();
        final Manager manager = context.getManager();
        if ( sessionId != null && manager instanceof StoreManager ) {
            final Store store = ( ( StoreManager ) manager ).getStore();
            if ( store != null ) {
                Session session = null;
                try {
                    session = store.load ( sessionId );
                } catch ( Exception e ) {
                    this.container.getLogger().error ( "deserializeError" );
                }
                if ( session != null ) {
                    if ( !session.isValid() || this.isSessionStale ( session, System.currentTimeMillis() ) ) {
                        if ( this.container.getLogger().isDebugEnabled() ) {
                            this.container.getLogger().debug ( "session swapped in is invalid or expired" );
                        }
                        session.expire();
                        store.remove ( sessionId );
                    } else {
                        session.setManager ( manager );
                        manager.add ( session );
                        session.access();
                        session.endAccess();
                    }
                }
            }
        }
        if ( this.container.getLogger().isDebugEnabled() ) {
            this.container.getLogger().debug ( "sessionId: " + sessionId );
        }
        this.getNext().invoke ( request, response );
        if ( !request.isAsync() ) {
            Session hsess;
            try {
                hsess = request.getSessionInternal ( false );
            } catch ( Exception ex ) {
                hsess = null;
            }
            String newsessionId = null;
            if ( hsess != null ) {
                newsessionId = hsess.getIdInternal();
            }
            if ( this.container.getLogger().isDebugEnabled() ) {
                this.container.getLogger().debug ( "newsessionId: " + newsessionId );
            }
            if ( newsessionId != null ) {
                try {
                    this.bind ( context );
                    if ( manager instanceof StoreManager ) {
                        final Session session2 = manager.findSession ( newsessionId );
                        final Store store2 = ( ( StoreManager ) manager ).getStore();
                        if ( store2 != null && session2 != null && session2.isValid() && !this.isSessionStale ( session2, System.currentTimeMillis() ) ) {
                            store2.save ( session2 );
                            ( ( StoreManager ) manager ).removeSuper ( session2 );
                            session2.recycle();
                        } else if ( this.container.getLogger().isDebugEnabled() ) {
                            this.container.getLogger().debug ( "newsessionId store: " + store2 + " session: " + session2 + " valid: " + ( ( session2 == null ) ? "N/A" : Boolean.toString ( session2.isValid() ) ) + " stale: " + this.isSessionStale ( session2, System.currentTimeMillis() ) );
                        }
                    } else if ( this.container.getLogger().isDebugEnabled() ) {
                        this.container.getLogger().debug ( "newsessionId Manager: " + manager );
                    }
                } finally {
                    this.unbind ( context );
                }
            }
        }
    }
    protected boolean isSessionStale ( final Session session, final long timeNow ) {
        if ( session != null ) {
            final int maxInactiveInterval = session.getMaxInactiveInterval();
            if ( maxInactiveInterval >= 0 ) {
                final int timeIdle = ( int ) ( ( timeNow - session.getThisAccessedTime() ) / 1000L );
                if ( timeIdle >= maxInactiveInterval ) {
                    return true;
                }
            }
        }
        return false;
    }
    private void bind ( final Context context ) {
        if ( this.clBindRequired ) {
            context.bind ( Globals.IS_SECURITY_ENABLED, PersistentValve.MY_CLASSLOADER );
        }
    }
    private void unbind ( final Context context ) {
        if ( this.clBindRequired ) {
            context.unbind ( Globals.IS_SECURITY_ENABLED, PersistentValve.MY_CLASSLOADER );
        }
    }
    static {
        MY_CLASSLOADER = PersistentValve.class.getClassLoader();
    }
}
