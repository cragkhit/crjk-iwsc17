package org.apache.catalina.valves;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.StoreManager;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
public class PersistentValve extends ValveBase {
    private static final ClassLoader MY_CLASSLOADER = PersistentValve.class.getClassLoader();
    private volatile boolean clBindRequired;
    public PersistentValve() {
        super ( true );
    }
    @Override
    public void setContainer ( Container container ) {
        super.setContainer ( container );
        if ( container instanceof Engine || container instanceof Host ) {
            clBindRequired = true;
        } else {
            clBindRequired = false;
        }
    }
    @Override
    public void invoke ( Request request, Response response )
    throws IOException, ServletException {
        Context context = request.getContext();
        if ( context == null ) {
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 sm.getString ( "standardHost.noContext" ) );
            return;
        }
        String sessionId = request.getRequestedSessionId();
        Manager manager = context.getManager();
        if ( sessionId != null && manager instanceof StoreManager ) {
            Store store = ( ( StoreManager ) manager ).getStore();
            if ( store != null ) {
                Session session = null;
                try {
                    session = store.load ( sessionId );
                } catch ( Exception e ) {
                    container.getLogger().error ( "deserializeError" );
                }
                if ( session != null ) {
                    if ( !session.isValid() ||
                            isSessionStale ( session, System.currentTimeMillis() ) ) {
                        if ( container.getLogger().isDebugEnabled() ) {
                            container.getLogger().debug ( "session swapped in is invalid or expired" );
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
        if ( container.getLogger().isDebugEnabled() ) {
            container.getLogger().debug ( "sessionId: " + sessionId );
        }
        getNext().invoke ( request, response );
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
            if ( container.getLogger().isDebugEnabled() ) {
                container.getLogger().debug ( "newsessionId: " + newsessionId );
            }
            if ( newsessionId != null ) {
                try {
                    bind ( context );
                    if ( manager instanceof StoreManager ) {
                        Session session = manager.findSession ( newsessionId );
                        Store store = ( ( StoreManager ) manager ).getStore();
                        if ( store != null && session != null && session.isValid() &&
                                !isSessionStale ( session, System.currentTimeMillis() ) ) {
                            store.save ( session );
                            ( ( StoreManager ) manager ).removeSuper ( session );
                            session.recycle();
                        } else {
                            if ( container.getLogger().isDebugEnabled() ) {
                                container.getLogger().debug ( "newsessionId store: " +
                                                              store + " session: " + session +
                                                              " valid: " +
                                                              ( session == null ? "N/A" : Boolean.toString (
                                                                    session.isValid() ) ) +
                                                              " stale: " + isSessionStale ( session,
                                                                      System.currentTimeMillis() ) );
                            }
                        }
                    } else {
                        if ( container.getLogger().isDebugEnabled() ) {
                            container.getLogger().debug ( "newsessionId Manager: " +
                                                          manager );
                        }
                    }
                } finally {
                    unbind ( context );
                }
            }
        }
    }
    protected boolean isSessionStale ( Session session, long timeNow ) {
        if ( session != null ) {
            int maxInactiveInterval = session.getMaxInactiveInterval();
            if ( maxInactiveInterval >= 0 ) {
                int timeIdle =
                    ( int ) ( ( timeNow - session.getThisAccessedTime() ) / 1000L );
                if ( timeIdle >= maxInactiveInterval ) {
                    return true;
                }
            }
        }
        return false;
    }
    private void bind ( Context context ) {
        if ( clBindRequired ) {
            context.bind ( Globals.IS_SECURITY_ENABLED, MY_CLASSLOADER );
        }
    }
    private void unbind ( Context context ) {
        if ( clBindRequired ) {
            context.unbind ( Globals.IS_SECURITY_ENABLED, MY_CLASSLOADER );
        }
    }
}
