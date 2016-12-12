package org.apache.catalina.ha.session;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Cluster;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class JvmRouteBinderValve extends ValveBase implements ClusterValve {
    public static final Log log = LogFactory.getLog ( JvmRouteBinderValve.class );
    public JvmRouteBinderValve() {
        super ( true );
    }
    protected CatalinaCluster cluster;
    protected static final StringManager sm = StringManager.getManager ( JvmRouteBinderValve.class );
    protected boolean enabled = true;
    protected long numberOfSessions = 0;
    protected String sessionIdAttribute = "org.apache.catalina.ha.session.JvmRouteOrignalSessionID";
    public String getSessionIdAttribute() {
        return sessionIdAttribute;
    }
    public void setSessionIdAttribute ( String sessionIdAttribute ) {
        this.sessionIdAttribute = sessionIdAttribute;
    }
    public long getNumberOfSessions() {
        return numberOfSessions;
    }
    public boolean getEnabled() {
        return enabled;
    }
    public void setEnabled ( boolean enabled ) {
        this.enabled = enabled;
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException,
        ServletException {
        if ( getEnabled() &&
                request.getContext() != null &&
                request.getContext().getDistributable() &&
                !request.isAsyncDispatching() ) {
            Manager manager = request.getContext().getManager();
            if ( manager != null && (
                        ( manager instanceof ClusterManager
                          && getCluster() != null
                          && getCluster().getManager ( ( ( ClusterManager ) manager ).getName() ) != null )
                        ||
                        ( manager instanceof PersistentManager ) ) ) {
                handlePossibleTurnover ( request );
            }
        }
        getNext().invoke ( request, response );
    }
    protected void handlePossibleTurnover ( Request request ) {
        String sessionID = request.getRequestedSessionId() ;
        if ( sessionID != null ) {
            long t1 = System.currentTimeMillis();
            String jvmRoute = getLocalJvmRoute ( request );
            if ( jvmRoute == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "jvmRoute.missingJvmRouteAttribute" ) );
                }
                return;
            }
            handleJvmRoute ( request, sessionID, jvmRoute );
            if ( log.isDebugEnabled() ) {
                long t2 = System.currentTimeMillis();
                long time = t2 - t1;
                log.debug ( sm.getString ( "jvmRoute.turnoverInfo", Long.valueOf ( time ) ) );
            }
        }
    }
    protected String getLocalJvmRoute ( Request request ) {
        Manager manager = getManager ( request );
        if ( manager instanceof ManagerBase ) {
            return ( ( ManagerBase ) manager ).getJvmRoute();
        }
        return null ;
    }
    protected Manager getManager ( Request request ) {
        Manager manager = request.getContext().getManager();
        if ( log.isDebugEnabled() ) {
            if ( manager != null ) {
                log.debug ( sm.getString ( "jvmRoute.foundManager", manager,  request.getContext().getName() ) );
            } else {
                log.debug ( sm.getString ( "jvmRoute.notFoundManager", request.getContext().getName() ) );
            }
        }
        return manager;
    }
    @Override
    public CatalinaCluster getCluster() {
        return cluster;
    }
    @Override
    public void setCluster ( CatalinaCluster cluster ) {
        this.cluster = cluster;
    }
    protected void handleJvmRoute (
        Request request, String sessionId, String localJvmRoute ) {
        String requestJvmRoute = null;
        int index = sessionId.indexOf ( '.' );
        if ( index > 0 ) {
            requestJvmRoute = sessionId
                              .substring ( index + 1, sessionId.length() );
        }
        if ( requestJvmRoute != null && !requestJvmRoute.equals ( localJvmRoute ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "jvmRoute.failover", requestJvmRoute,
                                           localJvmRoute, sessionId ) );
            }
            Session catalinaSession = null;
            try {
                catalinaSession = getManager ( request ).findSession ( sessionId );
            } catch ( IOException e ) {
            }
            String id = sessionId.substring ( 0, index );
            String newSessionID = id + "." + localJvmRoute;
            if ( catalinaSession != null ) {
                changeSessionID ( request, sessionId, newSessionID,
                                  catalinaSession );
                numberOfSessions++;
            } else {
                try {
                    catalinaSession = getManager ( request ).findSession ( newSessionID );
                } catch ( IOException e ) {
                }
                if ( catalinaSession != null ) {
                    changeRequestSessionID ( request, sessionId, newSessionID );
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "jvmRoute.cannotFindSession", sessionId ) );
                    }
                }
            }
        }
    }
    protected void changeSessionID ( Request request, String sessionId,
                                     String newSessionID, Session catalinaSession ) {
        fireLifecycleEvent ( "Before session migration", catalinaSession );
        catalinaSession.getManager().changeSessionId ( catalinaSession, newSessionID );
        changeRequestSessionID ( request, sessionId, newSessionID );
        fireLifecycleEvent ( "After session migration", catalinaSession );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "jvmRoute.changeSession", sessionId,
                                       newSessionID ) );
        }
    }
    protected void changeRequestSessionID ( Request request, String sessionId, String newSessionID ) {
        request.changeSessionId ( newSessionID );
        if ( sessionIdAttribute != null && !"".equals ( sessionIdAttribute ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "jvmRoute.set.orignalsessionid", sessionIdAttribute, sessionId ) );
            }
            request.setAttribute ( sessionIdAttribute, sessionId );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( cluster == null ) {
            Cluster containerCluster = getContainer().getCluster();
            if ( containerCluster instanceof CatalinaCluster ) {
                setCluster ( ( CatalinaCluster ) containerCluster );
            }
        }
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "jvmRoute.valve.started" ) );
            if ( cluster == null ) {
                log.info ( sm.getString ( "jvmRoute.noCluster" ) );
            }
        }
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        cluster = null;
        numberOfSessions = 0;
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "jvmRoute.valve.stopped" ) );
        }
    }
}
