package org.apache.catalina.ha.tcp;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.Manager;
import org.apache.catalina.ha.ClusterSession;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.Session;
import java.util.Iterator;
import org.apache.catalina.ha.session.DeltaManager;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Cluster;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.Context;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.apache.catalina.ha.session.DeltaSession;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.valves.ValveBase;
public class ReplicationValve extends ValveBase implements ClusterValve {
    private static final Log log;
    protected static final StringManager sm;
    private CatalinaCluster cluster;
    protected Pattern filter;
    protected final ThreadLocal<ArrayList<DeltaSession>> crossContextSessions;
    protected boolean doProcessingStats;
    protected long totalRequestTime;
    protected long totalSendTime;
    protected long nrOfRequests;
    protected long lastSendTime;
    protected long nrOfFilterRequests;
    protected long nrOfSendRequests;
    protected long nrOfCrossContextSendRequests;
    protected boolean primaryIndicator;
    protected String primaryIndicatorName;
    public ReplicationValve() {
        super ( true );
        this.cluster = null;
        this.filter = null;
        this.crossContextSessions = new ThreadLocal<ArrayList<DeltaSession>>();
        this.doProcessingStats = false;
        this.totalRequestTime = 0L;
        this.totalSendTime = 0L;
        this.nrOfRequests = 0L;
        this.lastSendTime = 0L;
        this.nrOfFilterRequests = 0L;
        this.nrOfSendRequests = 0L;
        this.nrOfCrossContextSendRequests = 0L;
        this.primaryIndicator = false;
        this.primaryIndicatorName = "org.apache.catalina.ha.tcp.isPrimarySession";
    }
    @Override
    public CatalinaCluster getCluster() {
        return this.cluster;
    }
    @Override
    public void setCluster ( final CatalinaCluster cluster ) {
        this.cluster = cluster;
    }
    public String getFilter() {
        if ( this.filter == null ) {
            return null;
        }
        return this.filter.toString();
    }
    public void setFilter ( final String filter ) {
        if ( ReplicationValve.log.isDebugEnabled() ) {
            ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.filter.loading", filter ) );
        }
        if ( filter == null || filter.length() == 0 ) {
            this.filter = null;
        } else {
            try {
                this.filter = Pattern.compile ( filter );
            } catch ( PatternSyntaxException pse ) {
                ReplicationValve.log.error ( ReplicationValve.sm.getString ( "ReplicationValve.filter.failure", filter ), pse );
            }
        }
    }
    public boolean isPrimaryIndicator() {
        return this.primaryIndicator;
    }
    public void setPrimaryIndicator ( final boolean primaryIndicator ) {
        this.primaryIndicator = primaryIndicator;
    }
    public String getPrimaryIndicatorName() {
        return this.primaryIndicatorName;
    }
    public void setPrimaryIndicatorName ( final String primaryIndicatorName ) {
        this.primaryIndicatorName = primaryIndicatorName;
    }
    public boolean doStatistics() {
        return this.doProcessingStats;
    }
    public void setStatistics ( final boolean doProcessingStats ) {
        this.doProcessingStats = doProcessingStats;
    }
    public long getLastSendTime() {
        return this.lastSendTime;
    }
    public long getNrOfRequests() {
        return this.nrOfRequests;
    }
    public long getNrOfFilterRequests() {
        return this.nrOfFilterRequests;
    }
    public long getNrOfCrossContextSendRequests() {
        return this.nrOfCrossContextSendRequests;
    }
    public long getNrOfSendRequests() {
        return this.nrOfSendRequests;
    }
    public long getTotalRequestTime() {
        return this.totalRequestTime;
    }
    public long getTotalSendTime() {
        return this.totalSendTime;
    }
    public void registerReplicationSession ( final DeltaSession session ) {
        final List<DeltaSession> sessions = this.crossContextSessions.get();
        if ( sessions != null && !sessions.contains ( session ) ) {
            if ( ReplicationValve.log.isDebugEnabled() ) {
                ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.crossContext.registerSession", session.getIdInternal(), session.getManager().getContext().getName() ) );
            }
            sessions.add ( session );
        }
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        long totalstart = 0L;
        if ( this.doStatistics() ) {
            totalstart = System.currentTimeMillis();
        }
        if ( this.primaryIndicator ) {
            this.createPrimaryIndicator ( request );
        }
        final Context context = request.getContext();
        final boolean isCrossContext = context != null && context instanceof StandardContext && ( ( StandardContext ) context ).getCrossContext();
        try {
            if ( isCrossContext ) {
                if ( ReplicationValve.log.isDebugEnabled() ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.crossContext.add" ) );
                }
                this.crossContextSessions.set ( new ArrayList<DeltaSession>() );
            }
            this.getNext().invoke ( request, response );
            if ( context != null && this.cluster != null && context.getManager() instanceof ClusterManager ) {
                final ClusterManager clusterManager = ( ClusterManager ) context.getManager();
                if ( this.cluster.getManager ( clusterManager.getName() ) == null ) {
                    return;
                }
                if ( this.cluster.hasMembers() ) {
                    this.sendReplicationMessage ( request, totalstart, isCrossContext, clusterManager );
                } else {
                    this.resetReplicationRequest ( request, isCrossContext );
                }
            }
        } finally {
            if ( isCrossContext ) {
                if ( ReplicationValve.log.isDebugEnabled() ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.crossContext.remove" ) );
                }
                this.crossContextSessions.set ( null );
            }
        }
    }
    public void resetStatistics() {
        this.totalRequestTime = 0L;
        this.totalSendTime = 0L;
        this.lastSendTime = 0L;
        this.nrOfFilterRequests = 0L;
        this.nrOfRequests = 0L;
        this.nrOfSendRequests = 0L;
        this.nrOfCrossContextSendRequests = 0L;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( this.cluster == null ) {
            final Cluster containerCluster = this.getContainer().getCluster();
            if ( containerCluster instanceof CatalinaCluster ) {
                this.setCluster ( ( CatalinaCluster ) containerCluster );
            } else if ( ReplicationValve.log.isWarnEnabled() ) {
                ReplicationValve.log.warn ( ReplicationValve.sm.getString ( "ReplicationValve.nocluster" ) );
            }
        }
        super.startInternal();
    }
    protected void sendReplicationMessage ( final Request request, final long totalstart, final boolean isCrossContext, final ClusterManager clusterManager ) {
        long start = 0L;
        if ( this.doStatistics() ) {
            start = System.currentTimeMillis();
        }
        try {
            if ( ! ( clusterManager instanceof DeltaManager ) ) {
                this.sendInvalidSessions ( clusterManager );
            }
            this.sendSessionReplicationMessage ( request, clusterManager );
            if ( isCrossContext ) {
                this.sendCrossContextSession();
            }
        } catch ( Exception x ) {
            ReplicationValve.log.error ( ReplicationValve.sm.getString ( "ReplicationValve.send.failure" ), x );
        } finally {
            if ( this.doStatistics() ) {
                this.updateStats ( totalstart, start );
            }
        }
    }
    protected void sendCrossContextSession() {
        final List<DeltaSession> sessions = this.crossContextSessions.get();
        if ( sessions != null && sessions.size() > 0 ) {
            for ( final Session session : sessions ) {
                if ( ReplicationValve.log.isDebugEnabled() ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.crossContext.sendDelta", session.getManager().getContext().getName() ) );
                }
                this.sendMessage ( session, ( ClusterManager ) session.getManager() );
                if ( this.doStatistics() ) {
                    ++this.nrOfCrossContextSendRequests;
                }
            }
        }
    }
    protected void resetReplicationRequest ( final Request request, final boolean isCrossContext ) {
        final Session contextSession = request.getSessionInternal ( false );
        if ( contextSession instanceof DeltaSession ) {
            this.resetDeltaRequest ( contextSession );
            ( ( DeltaSession ) contextSession ).setPrimarySession ( true );
        }
        if ( isCrossContext ) {
            final List<DeltaSession> sessions = this.crossContextSessions.get();
            if ( sessions != null && sessions.size() > 0 ) {
                for ( final Session session : sessions ) {
                    this.resetDeltaRequest ( session );
                    if ( session instanceof DeltaSession ) {
                        ( ( DeltaSession ) contextSession ).setPrimarySession ( true );
                    }
                }
            }
        }
    }
    protected void resetDeltaRequest ( final Session session ) {
        if ( ReplicationValve.log.isDebugEnabled() ) {
            ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.resetDeltaRequest", session.getManager().getContext().getName() ) );
        }
        ( ( DeltaSession ) session ).resetDeltaRequest();
    }
    protected void sendSessionReplicationMessage ( final Request request, final ClusterManager manager ) {
        final Session session = request.getSessionInternal ( false );
        if ( session != null ) {
            final String uri = request.getDecodedRequestURI();
            if ( !this.isRequestWithoutSessionChange ( uri ) ) {
                if ( ReplicationValve.log.isDebugEnabled() ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.invoke.uri", uri ) );
                }
                this.sendMessage ( session, manager );
            } else if ( this.doStatistics() ) {
                ++this.nrOfFilterRequests;
            }
        }
    }
    protected void sendMessage ( final Session session, final ClusterManager manager ) {
        final String id = session.getIdInternal();
        if ( id != null ) {
            this.send ( manager, id );
        }
    }
    protected void send ( final ClusterManager manager, final String sessionId ) {
        final ClusterMessage msg = manager.requestCompleted ( sessionId );
        if ( msg != null && this.cluster != null ) {
            this.cluster.send ( msg );
            if ( this.doStatistics() ) {
                ++this.nrOfSendRequests;
            }
        }
    }
    protected void sendInvalidSessions ( final ClusterManager manager ) {
        final String[] invalidIds = manager.getInvalidatedSessions();
        if ( invalidIds.length > 0 ) {
            for ( int i = 0; i < invalidIds.length; ++i ) {
                try {
                    this.send ( manager, invalidIds[i] );
                } catch ( Exception x ) {
                    ReplicationValve.log.error ( ReplicationValve.sm.getString ( "ReplicationValve.send.invalid.failure", invalidIds[i] ), x );
                }
            }
        }
    }
    protected boolean isRequestWithoutSessionChange ( final String uri ) {
        final Pattern f = this.filter;
        return f != null && f.matcher ( uri ).matches();
    }
    protected void updateStats ( final long requestTime, final long clusterTime ) {
        synchronized ( this ) {
            this.lastSendTime = System.currentTimeMillis();
            this.totalSendTime += this.lastSendTime - clusterTime;
            this.totalRequestTime += this.lastSendTime - requestTime;
            ++this.nrOfRequests;
        }
        if ( ReplicationValve.log.isInfoEnabled() && this.nrOfRequests % 100L == 0L ) {
            ReplicationValve.log.info ( ReplicationValve.sm.getString ( "ReplicationValve.stats", this.totalRequestTime / this.nrOfRequests, this.totalSendTime / this.nrOfRequests, this.nrOfRequests, this.nrOfSendRequests, this.nrOfCrossContextSendRequests, this.nrOfFilterRequests, this.totalRequestTime, this.totalSendTime ) );
        }
    }
    protected void createPrimaryIndicator ( final Request request ) throws IOException {
        final String id = request.getRequestedSessionId();
        if ( id != null && id.length() > 0 ) {
            final Manager manager = request.getContext().getManager();
            final Session session = manager.findSession ( id );
            if ( session instanceof ClusterSession ) {
                final ClusterSession cses = ( ClusterSession ) session;
                if ( ReplicationValve.log.isDebugEnabled() ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.session.indicator", request.getContext().getName(), id, this.primaryIndicatorName, cses.isPrimarySession() ) );
                }
                request.setAttribute ( this.primaryIndicatorName, cses.isPrimarySession() ? Boolean.TRUE : Boolean.FALSE );
            } else if ( ReplicationValve.log.isDebugEnabled() ) {
                if ( session != null ) {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.session.found", request.getContext().getName(), id ) );
                } else {
                    ReplicationValve.log.debug ( ReplicationValve.sm.getString ( "ReplicationValve.session.invalid", request.getContext().getName(), id ) );
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( ReplicationValve.class );
        sm = StringManager.getManager ( "org.apache.catalina.ha.tcp" );
    }
}
