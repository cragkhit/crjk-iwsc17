package org.apache.catalina.ha.tcp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import org.apache.catalina.Cluster;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.ha.ClusterSession;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.ha.session.DeltaManager;
import org.apache.catalina.ha.session.DeltaSession;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class ReplicationValve
    extends ValveBase implements ClusterValve {
    private static final Log log = LogFactory.getLog ( ReplicationValve.class );
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private CatalinaCluster cluster = null ;
    protected Pattern filter = null;
    protected final ThreadLocal<ArrayList<DeltaSession>> crossContextSessions =
        new ThreadLocal<>() ;
    protected boolean doProcessingStats = false;
    protected long totalRequestTime = 0;
    protected long totalSendTime = 0;
    protected long nrOfRequests = 0;
    protected long lastSendTime = 0;
    protected long nrOfFilterRequests = 0;
    protected long nrOfSendRequests = 0;
    protected long nrOfCrossContextSendRequests = 0;
    protected boolean primaryIndicator = false ;
    protected String primaryIndicatorName = "org.apache.catalina.ha.tcp.isPrimarySession";
    public ReplicationValve() {
        super ( true );
    }
    @Override
    public CatalinaCluster getCluster() {
        return cluster;
    }
    @Override
    public void setCluster ( CatalinaCluster cluster ) {
        this.cluster = cluster;
    }
    public String getFilter() {
        if ( filter == null ) {
            return null;
        }
        return filter.toString();
    }
    public void setFilter ( String filter ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "ReplicationValve.filter.loading", filter ) );
        }
        if ( filter == null || filter.length() == 0 ) {
            this.filter = null;
        } else {
            try {
                this.filter = Pattern.compile ( filter );
            } catch ( PatternSyntaxException pse ) {
                log.error ( sm.getString ( "ReplicationValve.filter.failure",
                                           filter ), pse );
            }
        }
    }
    public boolean isPrimaryIndicator() {
        return primaryIndicator;
    }
    public void setPrimaryIndicator ( boolean primaryIndicator ) {
        this.primaryIndicator = primaryIndicator;
    }
    public String getPrimaryIndicatorName() {
        return primaryIndicatorName;
    }
    public void setPrimaryIndicatorName ( String primaryIndicatorName ) {
        this.primaryIndicatorName = primaryIndicatorName;
    }
    public boolean doStatistics() {
        return doProcessingStats;
    }
    public void setStatistics ( boolean doProcessingStats ) {
        this.doProcessingStats = doProcessingStats;
    }
    public long getLastSendTime() {
        return lastSendTime;
    }
    public long getNrOfRequests() {
        return nrOfRequests;
    }
    public long getNrOfFilterRequests() {
        return nrOfFilterRequests;
    }
    public long getNrOfCrossContextSendRequests() {
        return nrOfCrossContextSendRequests;
    }
    public long getNrOfSendRequests() {
        return nrOfSendRequests;
    }
    public long getTotalRequestTime() {
        return totalRequestTime;
    }
    public long getTotalSendTime() {
        return totalSendTime;
    }
    public void registerReplicationSession ( DeltaSession session ) {
        List<DeltaSession> sessions = crossContextSessions.get();
        if ( sessions != null ) {
            if ( !sessions.contains ( session ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "ReplicationValve.crossContext.registerSession",
                                               session.getIdInternal(),
                                               session.getManager().getContext().getName() ) );
                }
                sessions.add ( session );
            }
        }
    }
    @Override
    public void invoke ( Request request, Response response )
    throws IOException, ServletException {
        long totalstart = 0;
        if ( doStatistics() ) {
            totalstart = System.currentTimeMillis();
        }
        if ( primaryIndicator ) {
            createPrimaryIndicator ( request ) ;
        }
        Context context = request.getContext();
        boolean isCrossContext = context != null
                                 && context instanceof StandardContext
                                 && ( ( StandardContext ) context ).getCrossContext();
        try {
            if ( isCrossContext ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "ReplicationValve.crossContext.add" ) );
                }
                crossContextSessions.set ( new ArrayList<DeltaSession>() );
            }
            getNext().invoke ( request, response );
            if ( context != null && cluster != null
                    && context.getManager() instanceof ClusterManager ) {
                ClusterManager clusterManager = ( ClusterManager ) context.getManager();
                if ( cluster.getManager ( clusterManager.getName() ) == null ) {
                    return ;
                }
                if ( cluster.hasMembers() ) {
                    sendReplicationMessage ( request, totalstart, isCrossContext, clusterManager );
                } else {
                    resetReplicationRequest ( request, isCrossContext );
                }
            }
        } finally {
            if ( isCrossContext ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "ReplicationValve.crossContext.remove" ) );
                }
                crossContextSessions.set ( null );
            }
        }
    }
    public void resetStatistics() {
        totalRequestTime = 0 ;
        totalSendTime = 0 ;
        lastSendTime = 0 ;
        nrOfFilterRequests = 0 ;
        nrOfRequests = 0 ;
        nrOfSendRequests = 0;
        nrOfCrossContextSendRequests = 0;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( cluster == null ) {
            Cluster containerCluster = getContainer().getCluster();
            if ( containerCluster instanceof CatalinaCluster ) {
                setCluster ( ( CatalinaCluster ) containerCluster );
            } else {
                if ( log.isWarnEnabled() ) {
                    log.warn ( sm.getString ( "ReplicationValve.nocluster" ) );
                }
            }
        }
        super.startInternal();
    }
    protected void sendReplicationMessage ( Request request, long totalstart, boolean isCrossContext, ClusterManager clusterManager ) {
        long start = 0;
        if ( doStatistics() ) {
            start = System.currentTimeMillis();
        }
        try {
            if ( ! ( clusterManager instanceof DeltaManager ) ) {
                sendInvalidSessions ( clusterManager );
            }
            sendSessionReplicationMessage ( request, clusterManager );
            if ( isCrossContext ) {
                sendCrossContextSession();
            }
        } catch ( Exception x ) {
            log.error ( sm.getString ( "ReplicationValve.send.failure" ), x );
        } finally {
            if ( doStatistics() ) {
                updateStats ( totalstart, start );
            }
        }
    }
    protected void sendCrossContextSession() {
        List<DeltaSession> sessions = crossContextSessions.get();
        if ( sessions != null && sessions.size() > 0 ) {
            for ( Iterator<DeltaSession> iter = sessions.iterator(); iter.hasNext() ; ) {
                Session session = iter.next();
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "ReplicationValve.crossContext.sendDelta",
                                               session.getManager().getContext().getName() ) );
                }
                sendMessage ( session, ( ClusterManager ) session.getManager() );
                if ( doStatistics() ) {
                    nrOfCrossContextSendRequests++;
                }
            }
        }
    }
    protected void resetReplicationRequest ( Request request, boolean isCrossContext ) {
        Session contextSession = request.getSessionInternal ( false );
        if ( contextSession instanceof DeltaSession ) {
            resetDeltaRequest ( contextSession );
            ( ( DeltaSession ) contextSession ).setPrimarySession ( true );
        }
        if ( isCrossContext ) {
            List<DeltaSession> sessions = crossContextSessions.get();
            if ( sessions != null && sessions.size() > 0 ) {
                Iterator<DeltaSession> iter = sessions.iterator();
                for ( ; iter.hasNext() ; ) {
                    Session session = iter.next();
                    resetDeltaRequest ( session );
                    if ( session instanceof DeltaSession ) {
                        ( ( DeltaSession ) contextSession ).setPrimarySession ( true );
                    }
                }
            }
        }
    }
    protected void resetDeltaRequest ( Session session ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "ReplicationValve.resetDeltaRequest" ,
                                       session.getManager().getContext().getName() ) );
        }
        ( ( DeltaSession ) session ).resetDeltaRequest();
    }
    protected void sendSessionReplicationMessage ( Request request,
            ClusterManager manager ) {
        Session session = request.getSessionInternal ( false );
        if ( session != null ) {
            String uri = request.getDecodedRequestURI();
            if ( !isRequestWithoutSessionChange ( uri ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "ReplicationValve.invoke.uri", uri ) );
                }
                sendMessage ( session, manager );
            } else if ( doStatistics() ) {
                nrOfFilterRequests++;
            }
        }
    }
    protected void sendMessage ( Session session,
                                 ClusterManager manager ) {
        String id = session.getIdInternal();
        if ( id != null ) {
            send ( manager, id );
        }
    }
    protected void send ( ClusterManager manager, String sessionId ) {
        ClusterMessage msg = manager.requestCompleted ( sessionId );
        if ( msg != null && cluster != null ) {
            cluster.send ( msg );
            if ( doStatistics() ) {
                nrOfSendRequests++;
            }
        }
    }
    protected void sendInvalidSessions ( ClusterManager manager ) {
        String[] invalidIds = manager.getInvalidatedSessions();
        if ( invalidIds.length > 0 ) {
            for ( int i = 0; i < invalidIds.length; i++ ) {
                try {
                    send ( manager, invalidIds[i] );
                } catch ( Exception x ) {
                    log.error ( sm.getString ( "ReplicationValve.send.invalid.failure", invalidIds[i] ), x );
                }
            }
        }
    }
    protected boolean isRequestWithoutSessionChange ( String uri ) {
        Pattern f = filter;
        return f != null && f.matcher ( uri ).matches();
    }
    protected  void updateStats ( long requestTime, long clusterTime ) {
        synchronized ( this ) {
            lastSendTime = System.currentTimeMillis();
            totalSendTime += lastSendTime - clusterTime;
            totalRequestTime += lastSendTime - requestTime;
            nrOfRequests++;
        }
        if ( log.isInfoEnabled() ) {
            if ( ( nrOfRequests % 100 ) == 0 ) {
                log.info ( sm.getString ( "ReplicationValve.stats",
                                          new Object[] {
                                              Long.valueOf ( totalRequestTime / nrOfRequests ),
                                              Long.valueOf ( totalSendTime / nrOfRequests ),
                                              Long.valueOf ( nrOfRequests ),
                                              Long.valueOf ( nrOfSendRequests ),
                                              Long.valueOf ( nrOfCrossContextSendRequests ),
                                              Long.valueOf ( nrOfFilterRequests ),
                                              Long.valueOf ( totalRequestTime ),
                                              Long.valueOf ( totalSendTime )
                                          } ) );
            }
        }
    }
    protected void createPrimaryIndicator ( Request request ) throws IOException {
        String id = request.getRequestedSessionId();
        if ( ( id != null ) && ( id.length() > 0 ) ) {
            Manager manager = request.getContext().getManager();
            Session session = manager.findSession ( id );
            if ( session instanceof ClusterSession ) {
                ClusterSession cses = ( ClusterSession ) session;
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString (
                                    "ReplicationValve.session.indicator", request.getContext().getName(), id,
                                    primaryIndicatorName,
                                    Boolean.valueOf ( cses.isPrimarySession() ) ) );
                }
                request.setAttribute ( primaryIndicatorName, cses.isPrimarySession() ? Boolean.TRUE : Boolean.FALSE );
            } else {
                if ( log.isDebugEnabled() ) {
                    if ( session != null ) {
                        log.debug ( sm.getString (
                                        "ReplicationValve.session.found", request.getContext().getName(), id ) );
                    } else {
                        log.debug ( sm.getString (
                                        "ReplicationValve.session.invalid", request.getContext().getName(), id ) );
                    }
                }
            }
        }
    }
}
