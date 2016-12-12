package org.apache.catalina.ha.session;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.ReplicationStream;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class DeltaManager extends ClusterManagerBase {
    public final Log log = LogFactory.getLog ( DeltaManager.class );
    protected static final StringManager sm = StringManager.getManager ( DeltaManager.class );
    protected String name = null;
    private boolean expireSessionsOnShutdown = false;
    private boolean notifySessionListenersOnReplication = true;
    private boolean notifyContainerListenersOnReplication  = true;
    private volatile boolean stateTransfered = false ;
    private volatile boolean noContextManagerReceived = false ;
    private int stateTransferTimeout = 60;
    private boolean sendAllSessions = true;
    private int sendAllSessionsSize = 1000 ;
    private int sendAllSessionsWaitTime = 2 * 1000 ;
    private final ArrayList<SessionMessage> receivedMessageQueue =
        new ArrayList<>();
    private boolean receiverQueue = false ;
    private boolean stateTimestampDrop = true ;
    private long stateTransferCreateSendTime;
    private long sessionReplaceCounter = 0 ;
    private long counterReceive_EVT_GET_ALL_SESSIONS = 0 ;
    private long counterReceive_EVT_ALL_SESSION_DATA = 0 ;
    private long counterReceive_EVT_SESSION_CREATED = 0 ;
    private long counterReceive_EVT_SESSION_EXPIRED = 0;
    private long counterReceive_EVT_SESSION_ACCESSED = 0 ;
    private long counterReceive_EVT_SESSION_DELTA = 0;
    private int counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;
    private long counterReceive_EVT_CHANGE_SESSION_ID = 0 ;
    private long counterReceive_EVT_ALL_SESSION_NOCONTEXTMANAGER = 0 ;
    private long counterSend_EVT_GET_ALL_SESSIONS = 0 ;
    private long counterSend_EVT_ALL_SESSION_DATA = 0 ;
    private long counterSend_EVT_SESSION_CREATED = 0;
    private long counterSend_EVT_SESSION_DELTA = 0 ;
    private long counterSend_EVT_SESSION_ACCESSED = 0;
    private long counterSend_EVT_SESSION_EXPIRED = 0;
    private int counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0 ;
    private long counterSend_EVT_CHANGE_SESSION_ID = 0;
    private int counterNoStateTransfered = 0 ;
    public DeltaManager() {
        super();
    }
    @Override
    public void setName ( String name ) {
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
    public long getCounterSend_EVT_GET_ALL_SESSIONS() {
        return counterSend_EVT_GET_ALL_SESSIONS;
    }
    public long getCounterSend_EVT_SESSION_ACCESSED() {
        return counterSend_EVT_SESSION_ACCESSED;
    }
    public long getCounterSend_EVT_SESSION_CREATED() {
        return counterSend_EVT_SESSION_CREATED;
    }
    public long getCounterSend_EVT_SESSION_DELTA() {
        return counterSend_EVT_SESSION_DELTA;
    }
    public long getCounterSend_EVT_SESSION_EXPIRED() {
        return counterSend_EVT_SESSION_EXPIRED;
    }
    public long getCounterSend_EVT_ALL_SESSION_DATA() {
        return counterSend_EVT_ALL_SESSION_DATA;
    }
    public int getCounterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE() {
        return counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE;
    }
    public long getCounterSend_EVT_CHANGE_SESSION_ID() {
        return counterSend_EVT_CHANGE_SESSION_ID;
    }
    public long getCounterReceive_EVT_ALL_SESSION_DATA() {
        return counterReceive_EVT_ALL_SESSION_DATA;
    }
    public long getCounterReceive_EVT_GET_ALL_SESSIONS() {
        return counterReceive_EVT_GET_ALL_SESSIONS;
    }
    public long getCounterReceive_EVT_SESSION_ACCESSED() {
        return counterReceive_EVT_SESSION_ACCESSED;
    }
    public long getCounterReceive_EVT_SESSION_CREATED() {
        return counterReceive_EVT_SESSION_CREATED;
    }
    public long getCounterReceive_EVT_SESSION_DELTA() {
        return counterReceive_EVT_SESSION_DELTA;
    }
    public long getCounterReceive_EVT_SESSION_EXPIRED() {
        return counterReceive_EVT_SESSION_EXPIRED;
    }
    public int getCounterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE() {
        return counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE;
    }
    public long getCounterReceive_EVT_CHANGE_SESSION_ID() {
        return counterReceive_EVT_CHANGE_SESSION_ID;
    }
    public long getCounterReceive_EVT_ALL_SESSION_NOCONTEXTMANAGER() {
        return counterReceive_EVT_ALL_SESSION_NOCONTEXTMANAGER;
    }
    @Override
    public long getProcessingTime() {
        return processingTime;
    }
    public long getSessionReplaceCounter() {
        return sessionReplaceCounter;
    }
    public int getCounterNoStateTransfered() {
        return counterNoStateTransfered;
    }
    public int getReceivedQueueSize() {
        return receivedMessageQueue.size() ;
    }
    public int getStateTransferTimeout() {
        return stateTransferTimeout;
    }
    public void setStateTransferTimeout ( int timeoutAllSession ) {
        this.stateTransferTimeout = timeoutAllSession;
    }
    public boolean getStateTransfered() {
        return stateTransfered;
    }
    public void setStateTransfered ( boolean stateTransfered ) {
        this.stateTransfered = stateTransfered;
    }
    public boolean isNoContextManagerReceived() {
        return noContextManagerReceived;
    }
    public void setNoContextManagerReceived ( boolean noContextManagerReceived ) {
        this.noContextManagerReceived = noContextManagerReceived;
    }
    public int getSendAllSessionsWaitTime() {
        return sendAllSessionsWaitTime;
    }
    public void setSendAllSessionsWaitTime ( int sendAllSessionsWaitTime ) {
        this.sendAllSessionsWaitTime = sendAllSessionsWaitTime;
    }
    public boolean isStateTimestampDrop() {
        return stateTimestampDrop;
    }
    public void setStateTimestampDrop ( boolean isTimestampDrop ) {
        this.stateTimestampDrop = isTimestampDrop;
    }
    public boolean isSendAllSessions() {
        return sendAllSessions;
    }
    public void setSendAllSessions ( boolean sendAllSessions ) {
        this.sendAllSessions = sendAllSessions;
    }
    public int getSendAllSessionsSize() {
        return sendAllSessionsSize;
    }
    public void setSendAllSessionsSize ( int sendAllSessionsSize ) {
        this.sendAllSessionsSize = sendAllSessionsSize;
    }
    public boolean isNotifySessionListenersOnReplication() {
        return notifySessionListenersOnReplication;
    }
    public void setNotifySessionListenersOnReplication (
        boolean notifyListenersCreateSessionOnReplication ) {
        this.notifySessionListenersOnReplication = notifyListenersCreateSessionOnReplication;
    }
    public boolean isExpireSessionsOnShutdown() {
        return expireSessionsOnShutdown;
    }
    public void setExpireSessionsOnShutdown ( boolean expireSessionsOnShutdown ) {
        this.expireSessionsOnShutdown = expireSessionsOnShutdown;
    }
    public boolean isNotifyContainerListenersOnReplication() {
        return notifyContainerListenersOnReplication;
    }
    public void setNotifyContainerListenersOnReplication (
        boolean notifyContainerListenersOnReplication ) {
        this.notifyContainerListenersOnReplication = notifyContainerListenersOnReplication;
    }
    @Override
    public Session createSession ( String sessionId ) {
        return createSession ( sessionId, true );
    }
    public Session createSession ( String sessionId, boolean distribute ) {
        DeltaSession session = ( DeltaSession ) super.createSession ( sessionId ) ;
        if ( distribute ) {
            sendCreateSession ( session.getId(), session );
        }
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString ( "deltaManager.createSession.newSession",
                                       session.getId(), Integer.valueOf ( sessions.size() ) ) );
        return ( session );
    }
    protected void sendCreateSession ( String sessionId, DeltaSession session ) {
        if ( cluster.getMembers().length > 0 ) {
            SessionMessage msg =
                new SessionMessageImpl ( getName(),
                                         SessionMessage.EVT_SESSION_CREATED,
                                         null,
                                         sessionId,
                                         sessionId + "-" + System.currentTimeMillis() );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.sendMessage.newSession", name, sessionId ) );
            }
            msg.setTimestamp ( session.getCreationTime() );
            counterSend_EVT_SESSION_CREATED++;
            send ( msg );
        }
    }
    protected void send ( SessionMessage msg ) {
        if ( cluster != null ) {
            cluster.send ( msg );
        }
    }
    @Override
    public Session createEmptySession() {
        return getNewDeltaSession() ;
    }
    protected DeltaSession getNewDeltaSession() {
        return new DeltaSession ( this );
    }
    @Override
    public void changeSessionId ( Session session ) {
        changeSessionId ( session, true );
    }
    @Override
    public void changeSessionId ( Session session, String newId ) {
        changeSessionId ( session, newId, true );
    }
    protected void changeSessionId ( Session session, boolean notify ) {
        String orgSessionID = session.getId();
        super.changeSessionId ( session );
        if ( notify ) {
            sendChangeSessionId ( session.getId(), orgSessionID );
        }
    }
    protected void changeSessionId ( Session session, String newId, boolean notify ) {
        String orgSessionID = session.getId();
        super.changeSessionId ( session, newId );
        if ( notify ) {
            sendChangeSessionId ( session.getId(), orgSessionID );
        }
    }
    protected void sendChangeSessionId ( String newSessionID, String orgSessionID ) {
        if ( cluster.getMembers().length > 0 ) {
            try {
                byte[] data = serializeSessionId ( newSessionID );
                SessionMessage msg = new SessionMessageImpl ( getName(),
                        SessionMessage.EVT_CHANGE_SESSION_ID, data,
                        orgSessionID, orgSessionID + "-"
                        + System.currentTimeMillis() );
                msg.setTimestamp ( System.currentTimeMillis() );
                counterSend_EVT_CHANGE_SESSION_ID++;
                send ( msg );
            } catch ( IOException e ) {
                log.error ( sm.getString ( "deltaManager.unableSerializeSessionID",
                                           newSessionID ), e );
            }
        }
    }
    protected byte[] serializeSessionId ( String sessionId ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream ( bos );
        oos.writeUTF ( sessionId );
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }
    protected String deserializeSessionId ( byte[] data ) throws IOException {
        ReplicationStream ois = getReplicationStream ( data );
        String sessionId = ois.readUTF();
        ois.close();
        return sessionId;
    }
    protected DeltaRequest deserializeDeltaRequest ( DeltaSession session, byte[] data )
    throws ClassNotFoundException, IOException {
        session.lock();
        try {
            ReplicationStream ois = getReplicationStream ( data );
            session.getDeltaRequest().readExternal ( ois );
            ois.close();
            return session.getDeltaRequest();
        } finally {
            session.unlock();
        }
    }
    protected byte[] serializeDeltaRequest ( DeltaSession session, DeltaRequest deltaRequest )
    throws IOException {
        session.lock();
        try {
            return deltaRequest.serialize();
        } finally {
            session.unlock();
        }
    }
    protected void deserializeSessions ( byte[] data ) throws ClassNotFoundException, IOException {
        try ( ObjectInputStream ois = getReplicationStream ( data ) ) {
            Integer count = ( Integer ) ois.readObject();
            int n = count.intValue();
            for ( int i = 0; i < n; i++ ) {
                DeltaSession session = ( DeltaSession ) createEmptySession();
                session.readObjectData ( ois );
                session.setManager ( this );
                session.setValid ( true );
                session.setPrimarySession ( false );
                session.access();
                session.setAccessCount ( 0 );
                session.resetDeltaRequest();
                if ( findSession ( session.getIdInternal() ) == null ) {
                    sessionCounter++;
                } else {
                    sessionReplaceCounter++;
                    if ( log.isWarnEnabled() ) {
                        log.warn ( sm.getString ( "deltaManager.loading.existing.session",
                                                  session.getIdInternal() ) );
                    }
                }
                add ( session );
                if ( notifySessionListenersOnReplication ) {
                    session.tellNew();
                }
            }
        } catch ( ClassNotFoundException e ) {
            log.error ( sm.getString ( "deltaManager.loading.cnfe", e ), e );
            throw e;
        } catch ( IOException e ) {
            log.error ( sm.getString ( "deltaManager.loading.ioe", e ), e );
            throw e;
        }
    }
    protected byte[] serializeSessions ( Session[] currentSessions ) throws IOException {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        try ( ObjectOutputStream oos = new ObjectOutputStream ( new BufferedOutputStream ( fos ) ) ) {
            oos.writeObject ( Integer.valueOf ( currentSessions.length ) );
            for ( int i = 0 ; i < currentSessions.length; i++ ) {
                ( ( DeltaSession ) currentSessions[i] ).writeObjectData ( oos );
            }
            oos.flush();
        } catch ( IOException e ) {
            log.error ( sm.getString ( "deltaManager.unloading.ioe", e ), e );
            throw e;
        }
        return fos.toByteArray();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            if ( cluster == null ) {
                log.error ( sm.getString ( "deltaManager.noCluster", getName() ) );
                return;
            } else {
                if ( log.isInfoEnabled() ) {
                    String type = "unknown" ;
                    if ( cluster.getContainer() instanceof Host ) {
                        type = "Host" ;
                    } else if ( cluster.getContainer() instanceof Engine ) {
                        type = "Engine" ;
                    }
                    log.info ( sm.getString ( "deltaManager.registerCluster",
                                              getName(), type, cluster.getClusterName() ) );
                }
            }
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "deltaManager.startClustering", getName() ) );
            }
            getAllClusterSessions();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "deltaManager.managerLoad" ), t );
        }
        setState ( LifecycleState.STARTING );
    }
    public synchronized void getAllClusterSessions() {
        if ( cluster != null && cluster.getMembers().length > 0 ) {
            long beforeSendTime = System.currentTimeMillis();
            Member mbr = findSessionMasterMember();
            if ( mbr == null ) {
                return;
            }
            SessionMessage msg = new SessionMessageImpl ( this.getName(),
                    SessionMessage.EVT_GET_ALL_SESSIONS, null, "GET-ALL", "GET-ALL-" + getName() );
            msg.setTimestamp ( beforeSendTime );
            stateTransferCreateSendTime = beforeSendTime ;
            counterSend_EVT_GET_ALL_SESSIONS++;
            stateTransfered = false ;
            try {
                synchronized ( receivedMessageQueue ) {
                    receiverQueue = true ;
                }
                cluster.send ( msg, mbr );
                if ( log.isInfoEnabled() )
                    log.info ( sm.getString ( "deltaManager.waitForSessionState",
                                              getName(), mbr, Integer.valueOf ( getStateTransferTimeout() ) ) );
                waitForSendAllSessions ( beforeSendTime );
            } finally {
                synchronized ( receivedMessageQueue ) {
                    for ( Iterator<SessionMessage> iter = receivedMessageQueue.iterator();
                            iter.hasNext(); ) {
                        SessionMessage smsg = iter.next();
                        if ( !stateTimestampDrop ) {
                            messageReceived ( smsg,
                                              smsg.getAddress() != null ? ( Member ) smsg.getAddress() : null );
                        } else {
                            if ( smsg.getEventType() != SessionMessage.EVT_GET_ALL_SESSIONS &&
                                    smsg.getTimestamp() >= stateTransferCreateSendTime ) {
                                messageReceived ( smsg,
                                                  smsg.getAddress() != null ?
                                                  ( Member ) smsg.getAddress() :
                                                  null );
                            } else {
                                if ( log.isWarnEnabled() ) {
                                    log.warn ( sm.getString ( "deltaManager.dropMessage",
                                                              getName(),
                                                              smsg.getEventTypeString(),
                                                              new Date ( stateTransferCreateSendTime ),
                                                              new Date ( smsg.getTimestamp() ) ) );
                                }
                            }
                        }
                    }
                    receivedMessageQueue.clear();
                    receiverQueue = false ;
                }
            }
        } else {
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "deltaManager.noMembers", getName() ) );
            }
        }
    }
    protected Member findSessionMasterMember() {
        Member mbr = null;
        Member mbrs[] = cluster.getMembers();
        if ( mbrs.length != 0 ) {
            mbr = mbrs[0];
        }
        if ( mbr == null && log.isWarnEnabled() ) {
            log.warn ( sm.getString ( "deltaManager.noMasterMember", getName(), "" ) );
        }
        if ( mbr != null && log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.foundMasterMember", getName(), mbr ) );
        }
        return mbr;
    }
    protected void waitForSendAllSessions ( long beforeSendTime ) {
        long reqStart = System.currentTimeMillis();
        long reqNow = reqStart ;
        boolean isTimeout = false;
        if ( getStateTransferTimeout() > 0 ) {
            do {
                try {
                    Thread.sleep ( 100 );
                } catch ( Exception sleep ) {
                }
                reqNow = System.currentTimeMillis();
                isTimeout = ( ( reqNow - reqStart ) > ( 1000L * getStateTransferTimeout() ) );
            } while ( ( !getStateTransfered() ) && ( !isTimeout ) && ( !isNoContextManagerReceived() ) );
        } else {
            if ( getStateTransferTimeout() == -1 ) {
                do {
                    try {
                        Thread.sleep ( 100 );
                    } catch ( Exception sleep ) {
                    }
                } while ( ( !getStateTransfered() ) && ( !isNoContextManagerReceived() ) );
                reqNow = System.currentTimeMillis();
            }
        }
        if ( isTimeout ) {
            counterNoStateTransfered++ ;
            log.error ( sm.getString ( "deltaManager.noSessionState", getName(),
                                       new Date ( beforeSendTime ), Long.valueOf ( reqNow - beforeSendTime ) ) );
        } else if ( isNoContextManagerReceived() ) {
            if ( log.isWarnEnabled() )
                log.warn ( sm.getString ( "deltaManager.noContextManager", getName(),
                                          new Date ( beforeSendTime ), Long.valueOf ( reqNow - beforeSendTime ) ) );
        } else {
            if ( log.isInfoEnabled() )
                log.info ( sm.getString ( "deltaManager.sessionReceived", getName(),
                                          new Date ( beforeSendTime ), Long.valueOf ( reqNow - beforeSendTime ) ) );
        }
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.stopped", getName() ) );
        }
        setState ( LifecycleState.STOPPING );
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "deltaManager.expireSessions", getName() ) );
        }
        Session sessions[] = findSessions();
        for ( int i = 0; i < sessions.length; i++ ) {
            DeltaSession session = ( DeltaSession ) sessions[i];
            if ( !session.isValid() ) {
                continue;
            }
            try {
                session.expire ( true, isExpireSessionsOnShutdown() );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
            }
        }
        super.stopInternal();
    }
    @Override
    public void messageDataReceived ( ClusterMessage cmsg ) {
        if ( cmsg instanceof SessionMessage ) {
            SessionMessage msg = ( SessionMessage ) cmsg;
            switch ( msg.getEventType() ) {
            case SessionMessage.EVT_GET_ALL_SESSIONS:
            case SessionMessage.EVT_SESSION_CREATED:
            case SessionMessage.EVT_SESSION_EXPIRED:
            case SessionMessage.EVT_SESSION_ACCESSED:
            case SessionMessage.EVT_SESSION_DELTA:
            case SessionMessage.EVT_CHANGE_SESSION_ID:
                synchronized ( receivedMessageQueue ) {
                    if ( receiverQueue ) {
                        receivedMessageQueue.add ( msg );
                        return ;
                    }
                }
                break;
            default:
                break;
            }
            messageReceived ( msg, msg.getAddress() != null ? ( Member ) msg.getAddress() : null );
        }
    }
    @Override
    public ClusterMessage requestCompleted ( String sessionId ) {
        return requestCompleted ( sessionId, false );
    }
    @SuppressWarnings ( "null" )
    public ClusterMessage requestCompleted ( String sessionId, boolean expires ) {
        DeltaSession session = null;
        SessionMessage msg = null;
        try {
            session = ( DeltaSession ) findSession ( sessionId );
            if ( session == null ) {
                return null;
            }
            DeltaRequest deltaRequest = session.getDeltaRequest();
            session.lock();
            if ( deltaRequest.getSize() > 0 ) {
                counterSend_EVT_SESSION_DELTA++;
                byte[] data = serializeDeltaRequest ( session, deltaRequest );
                msg = new SessionMessageImpl ( getName(),
                                               SessionMessage.EVT_SESSION_DELTA,
                                               data,
                                               sessionId,
                                               sessionId + "-" + System.currentTimeMillis() );
                session.resetDeltaRequest();
            }
        } catch ( IOException x ) {
            log.error ( sm.getString ( "deltaManager.createMessage.unableCreateDeltaRequest",
                                       sessionId ), x );
            return null;
        } finally {
            if ( session != null ) {
                session.unlock();
            }
        }
        if ( msg == null ) {
            if ( !expires && !session.isPrimarySession() ) {
                counterSend_EVT_SESSION_ACCESSED++;
                msg = new SessionMessageImpl ( getName(),
                                               SessionMessage.EVT_SESSION_ACCESSED,
                                               null,
                                               sessionId,
                                               sessionId + "-" + System.currentTimeMillis() );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "deltaManager.createMessage.accessChangePrimary",
                                               getName(), sessionId ) );
                }
            }
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.createMessage.delta", getName(), sessionId ) );
            }
        }
        if ( !expires ) {
            session.setPrimarySession ( true );
        }
        if ( !expires && ( msg == null ) ) {
            long replDelta = System.currentTimeMillis() - session.getLastTimeReplicated();
            if ( session.getMaxInactiveInterval() >= 0 &&
                    replDelta > ( session.getMaxInactiveInterval() * 1000L ) ) {
                counterSend_EVT_SESSION_ACCESSED++;
                msg = new SessionMessageImpl ( getName(),
                                               SessionMessage.EVT_SESSION_ACCESSED,
                                               null,
                                               sessionId,
                                               sessionId + "-" + System.currentTimeMillis() );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "deltaManager.createMessage.access",
                                               getName(), sessionId ) );
                }
            }
        }
        if ( msg != null ) {
            session.setLastTimeReplicated ( System.currentTimeMillis() );
            msg.setTimestamp ( session.getLastTimeReplicated() );
        }
        return msg;
    }
    public synchronized void resetStatistics() {
        processingTime = 0 ;
        expiredSessions.set ( 0 );
        synchronized ( sessionCreationTiming ) {
            sessionCreationTiming.clear();
            while ( sessionCreationTiming.size() <
                    ManagerBase.TIMING_STATS_CACHE_SIZE ) {
                sessionCreationTiming.add ( null );
            }
        }
        synchronized ( sessionExpirationTiming ) {
            sessionExpirationTiming.clear();
            while ( sessionExpirationTiming.size() <
                    ManagerBase.TIMING_STATS_CACHE_SIZE ) {
                sessionExpirationTiming.add ( null );
            }
        }
        rejectedSessions = 0 ;
        sessionReplaceCounter = 0 ;
        counterNoStateTransfered = 0 ;
        setMaxActive ( getActiveSessions() );
        sessionCounter = getActiveSessions() ;
        counterReceive_EVT_ALL_SESSION_DATA = 0;
        counterReceive_EVT_GET_ALL_SESSIONS = 0;
        counterReceive_EVT_SESSION_ACCESSED = 0 ;
        counterReceive_EVT_SESSION_CREATED = 0 ;
        counterReceive_EVT_SESSION_DELTA = 0 ;
        counterReceive_EVT_SESSION_EXPIRED = 0 ;
        counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0;
        counterReceive_EVT_CHANGE_SESSION_ID = 0;
        counterSend_EVT_ALL_SESSION_DATA = 0;
        counterSend_EVT_GET_ALL_SESSIONS = 0;
        counterSend_EVT_SESSION_ACCESSED = 0 ;
        counterSend_EVT_SESSION_CREATED = 0 ;
        counterSend_EVT_SESSION_DELTA = 0 ;
        counterSend_EVT_SESSION_EXPIRED = 0 ;
        counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE = 0;
        counterSend_EVT_CHANGE_SESSION_ID = 0;
    }
    protected void sessionExpired ( String id ) {
        if ( cluster.getMembers().length > 0 ) {
            counterSend_EVT_SESSION_EXPIRED++ ;
            SessionMessage msg = new SessionMessageImpl ( getName(),
                    SessionMessage.EVT_SESSION_EXPIRED, null, id, id + "-EXPIRED-MSG" );
            msg.setTimestamp ( System.currentTimeMillis() );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.createMessage.expire", getName(), id ) );
            }
            send ( msg );
        }
    }
    public void expireAllLocalSessions() {
        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireDirect  = 0 ;
        int expireIndirect = 0 ;
        if ( log.isDebugEnabled() ) {
            log.debug ( "Start expire all sessions " + getName() + " at " + timeNow +
                        " sessioncount " + sessions.length );
        }
        for ( int i = 0; i < sessions.length; i++ ) {
            if ( sessions[i] instanceof DeltaSession ) {
                DeltaSession session = ( DeltaSession ) sessions[i];
                if ( session.isPrimarySession() ) {
                    if ( session.isValid() ) {
                        session.expire();
                        expireDirect++;
                    } else {
                        expireIndirect++;
                    }
                }
            }
        }
        long timeEnd = System.currentTimeMillis();
        if ( log.isDebugEnabled() ) {
            log.debug ( "End expire sessions " + getName() +
                        " expire processingTime " + ( timeEnd - timeNow ) +
                        " expired direct sessions: " + expireDirect +
                        " expired direct sessions: " + expireIndirect );
        }
    }
    @Override
    public String[] getInvalidatedSessions() {
        return new String[0];
    }
    protected void messageReceived ( SessionMessage msg, Member sender ) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader[] loaders = getClassLoaders();
            Thread.currentThread().setContextClassLoader ( loaders[0] );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.receiveMessage.eventType",
                                           getName(), msg.getEventTypeString(), sender ) );
            }
            switch ( msg.getEventType() ) {
            case SessionMessage.EVT_GET_ALL_SESSIONS:
                handleGET_ALL_SESSIONS ( msg, sender );
                break;
            case SessionMessage.EVT_ALL_SESSION_DATA:
                handleALL_SESSION_DATA ( msg, sender );
                break;
            case SessionMessage.EVT_ALL_SESSION_TRANSFERCOMPLETE:
                handleALL_SESSION_TRANSFERCOMPLETE ( msg, sender );
                break;
            case SessionMessage.EVT_SESSION_CREATED:
                handleSESSION_CREATED ( msg, sender );
                break;
            case SessionMessage.EVT_SESSION_EXPIRED:
                handleSESSION_EXPIRED ( msg, sender );
                break;
            case SessionMessage.EVT_SESSION_ACCESSED:
                handleSESSION_ACCESSED ( msg, sender );
                break;
            case SessionMessage.EVT_SESSION_DELTA:
                handleSESSION_DELTA ( msg, sender );
                break;
            case SessionMessage.EVT_CHANGE_SESSION_ID:
                handleCHANGE_SESSION_ID ( msg, sender );
                break;
            case SessionMessage.EVT_ALL_SESSION_NOCONTEXTMANAGER:
                handleALL_SESSION_NOCONTEXTMANAGER ( msg, sender );
                break;
            default:
                break;
            }
        } catch ( Exception x ) {
            log.error ( sm.getString ( "deltaManager.receiveMessage.error", getName() ), x );
        } finally {
            Thread.currentThread().setContextClassLoader ( contextLoader );
        }
    }
    protected void handleALL_SESSION_TRANSFERCOMPLETE ( SessionMessage msg, Member sender ) {
        counterReceive_EVT_ALL_SESSION_TRANSFERCOMPLETE++ ;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.transfercomplete",
                                       getName(), sender.getHost(), Integer.valueOf ( sender.getPort() ) ) );
        }
        stateTransferCreateSendTime = msg.getTimestamp() ;
        stateTransfered = true ;
    }
    protected void handleSESSION_DELTA ( SessionMessage msg, Member sender )
    throws IOException, ClassNotFoundException {
        counterReceive_EVT_SESSION_DELTA++;
        byte[] delta = msg.getSession();
        DeltaSession session = ( DeltaSession ) findSession ( msg.getSessionID() );
        if ( session != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.receiveMessage.delta",
                                           getName(), msg.getSessionID() ) );
            }
            session.lock();
            try {
                DeltaRequest dreq = deserializeDeltaRequest ( session, delta );
                dreq.execute ( session, isNotifyListenersOnReplication() );
                session.setPrimarySession ( false );
            } finally {
                session.unlock();
            }
        }
    }
    protected void handleSESSION_ACCESSED ( SessionMessage msg, Member sender ) throws IOException {
        counterReceive_EVT_SESSION_ACCESSED++;
        DeltaSession session = ( DeltaSession ) findSession ( msg.getSessionID() );
        if ( session != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.receiveMessage.accessed",
                                           getName(), msg.getSessionID() ) );
            }
            session.access();
            session.setPrimarySession ( false );
            session.endAccess();
        }
    }
    protected void handleSESSION_EXPIRED ( SessionMessage msg, Member sender ) throws IOException {
        counterReceive_EVT_SESSION_EXPIRED++;
        DeltaSession session = ( DeltaSession ) findSession ( msg.getSessionID() );
        if ( session != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "deltaManager.receiveMessage.expired",
                                           getName(), msg.getSessionID() ) );
            }
            session.expire ( notifySessionListenersOnReplication, false );
        }
    }
    protected void handleSESSION_CREATED ( SessionMessage msg, Member sender ) {
        counterReceive_EVT_SESSION_CREATED++;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.createNewSession",
                                       getName(), msg.getSessionID() ) );
        }
        DeltaSession session = ( DeltaSession ) createEmptySession();
        session.setManager ( this );
        session.setValid ( true );
        session.setPrimarySession ( false );
        session.setCreationTime ( msg.getTimestamp() );
        session.setMaxInactiveInterval ( getContext().getSessionTimeout() * 60, false );
        session.access();
        session.setId ( msg.getSessionID(), notifySessionListenersOnReplication );
        session.resetDeltaRequest();
        session.endAccess();
    }
    protected void handleALL_SESSION_DATA ( SessionMessage msg, Member sender )
    throws ClassNotFoundException, IOException {
        counterReceive_EVT_ALL_SESSION_DATA++;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.allSessionDataBegin", getName() ) );
        }
        byte[] data = msg.getSession();
        deserializeSessions ( data );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.allSessionDataAfter", getName() ) );
        }
    }
    protected void handleGET_ALL_SESSIONS ( SessionMessage msg, Member sender ) throws IOException {
        counterReceive_EVT_GET_ALL_SESSIONS++;
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.unloadingBegin", getName() ) );
        }
        Session[] currentSessions = findSessions();
        long findSessionTimestamp = System.currentTimeMillis() ;
        if ( isSendAllSessions() ) {
            sendSessions ( sender, currentSessions, findSessionTimestamp );
        } else {
            int remain = currentSessions.length;
            for ( int i = 0; i < currentSessions.length; i += getSendAllSessionsSize() ) {
                int len = i + getSendAllSessionsSize() > currentSessions.length ?
                          currentSessions.length - i :
                          getSendAllSessionsSize();
                Session[] sendSessions = new Session[len];
                System.arraycopy ( currentSessions, i, sendSessions, 0, len );
                sendSessions ( sender, sendSessions, findSessionTimestamp );
                remain = remain - len;
                if ( getSendAllSessionsWaitTime() > 0 && remain > 0 ) {
                    try {
                        Thread.sleep ( getSendAllSessionsWaitTime() );
                    } catch ( Exception sleep ) {
                    }
                }
            }
        }
        SessionMessage newmsg = new SessionMessageImpl ( name,
                SessionMessage.EVT_ALL_SESSION_TRANSFERCOMPLETE, null, "SESSION-STATE-TRANSFERED",
                "SESSION-STATE-TRANSFERED" + getName() );
        newmsg.setTimestamp ( findSessionTimestamp );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.createMessage.allSessionTransfered", getName() ) );
        }
        counterSend_EVT_ALL_SESSION_TRANSFERCOMPLETE++;
        cluster.send ( newmsg, sender );
    }
    protected void handleCHANGE_SESSION_ID ( SessionMessage msg, Member sender ) throws IOException {
        counterReceive_EVT_CHANGE_SESSION_ID++;
        DeltaSession session = ( DeltaSession ) findSession ( msg.getSessionID() );
        if ( session != null ) {
            String newSessionID = deserializeSessionId ( msg.getSession() );
            session.setPrimarySession ( false );
            changeSessionId ( session, newSessionID, notifySessionListenersOnReplication,
                              notifyContainerListenersOnReplication );
        }
    }
    protected void handleALL_SESSION_NOCONTEXTMANAGER ( SessionMessage msg, Member sender ) {
        counterReceive_EVT_ALL_SESSION_NOCONTEXTMANAGER++ ;
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString ( "deltaManager.receiveMessage.noContextManager",
                                       getName(), sender.getHost(), Integer.valueOf ( sender.getPort() ) ) );
        noContextManagerReceived = true ;
    }
    protected void sendSessions ( Member sender, Session[] currentSessions, long sendTimestamp )
    throws IOException {
        byte[] data = serializeSessions ( currentSessions );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.receiveMessage.unloadingAfter", getName() ) );
        }
        SessionMessage newmsg = new SessionMessageImpl ( name, SessionMessage.EVT_ALL_SESSION_DATA,
                data, "SESSION-STATE", "SESSION-STATE-" + getName() );
        newmsg.setTimestamp ( sendTimestamp );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaManager.createMessage.allSessionData", getName() ) );
        }
        counterSend_EVT_ALL_SESSION_DATA++;
        cluster.send ( newmsg, sender );
    }
    @Override
    public ClusterManager cloneFromTemplate() {
        DeltaManager result = new DeltaManager();
        clone ( result );
        result.expireSessionsOnShutdown = expireSessionsOnShutdown;
        result.notifySessionListenersOnReplication = notifySessionListenersOnReplication;
        result.notifyContainerListenersOnReplication = notifyContainerListenersOnReplication;
        result.stateTransferTimeout = stateTransferTimeout;
        result.sendAllSessions = sendAllSessions;
        result.sendAllSessionsSize = sendAllSessionsSize;
        result.sendAllSessionsWaitTime = sendAllSessionsWaitTime ;
        result.stateTimestampDrop = stateTimestampDrop ;
        return result;
    }
}
