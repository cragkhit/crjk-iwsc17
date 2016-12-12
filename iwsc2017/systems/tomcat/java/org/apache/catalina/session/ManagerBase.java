package org.apache.catalina.session;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public abstract class ManagerBase extends LifecycleMBeanBase implements Manager {
    private final Log log = LogFactory.getLog ( ManagerBase.class );
    private Context context;
    private static final String name = "ManagerBase";
    protected String secureRandomClass = null;
    protected String secureRandomAlgorithm = "SHA1PRNG";
    protected String secureRandomProvider = null;
    protected SessionIdGenerator sessionIdGenerator = null;
    protected Class<? extends SessionIdGenerator> sessionIdGeneratorClass = null;
    protected volatile int sessionMaxAliveTime;
    private final Object sessionMaxAliveTimeUpdateLock = new Object();
    protected static final int TIMING_STATS_CACHE_SIZE = 100;
    protected final Deque<SessionTiming> sessionCreationTiming =
        new LinkedList<>();
    protected final Deque<SessionTiming> sessionExpirationTiming =
        new LinkedList<>();
    protected final AtomicLong expiredSessions = new AtomicLong ( 0 );
    protected Map<String, Session> sessions = new ConcurrentHashMap<>();
    protected long sessionCounter = 0;
    protected volatile int maxActive = 0;
    private final Object maxActiveUpdateLock = new Object();
    protected int maxActiveSessions = -1;
    protected int rejectedSessions = 0;
    protected volatile int duplicates = 0;
    protected long processingTime = 0;
    private int count = 0;
    protected int processExpiresFrequency = 6;
    protected static final StringManager sm = StringManager.getManager ( ManagerBase.class );
    protected final PropertyChangeSupport support =
        new PropertyChangeSupport ( this );
    private Pattern sessionAttributeNamePattern;
    private Pattern sessionAttributeValueClassNamePattern;
    private boolean warnOnSessionAttributeFilterFailure;
    public ManagerBase() {
        if ( Globals.IS_SECURITY_ENABLED ) {
            setSessionAttributeValueClassNameFilter (
                "java\\.lang\\.(?:Boolean|Integer|Long|Number|String)" );
            setWarnOnSessionAttributeFilterFailure ( true );
        }
    }
    public String getSessionAttributeNameFilter() {
        if ( sessionAttributeNamePattern == null ) {
            return null;
        }
        return sessionAttributeNamePattern.toString();
    }
    public void setSessionAttributeNameFilter ( String sessionAttributeNameFilter )
    throws PatternSyntaxException {
        if ( sessionAttributeNameFilter == null || sessionAttributeNameFilter.length() == 0 ) {
            sessionAttributeNamePattern = null;
        } else {
            sessionAttributeNamePattern = Pattern.compile ( sessionAttributeNameFilter );
        }
    }
    protected Pattern getSessionAttributeNamePattern() {
        return sessionAttributeNamePattern;
    }
    public String getSessionAttributeValueClassNameFilter() {
        if ( sessionAttributeValueClassNamePattern == null ) {
            return null;
        }
        return sessionAttributeValueClassNamePattern.toString();
    }
    protected Pattern getSessionAttributeValueClassNamePattern() {
        return sessionAttributeValueClassNamePattern;
    }
    public void setSessionAttributeValueClassNameFilter ( String sessionAttributeValueClassNameFilter )
    throws PatternSyntaxException {
        if ( sessionAttributeValueClassNameFilter == null ||
                sessionAttributeValueClassNameFilter.length() == 0 ) {
            sessionAttributeValueClassNamePattern = null;
        } else {
            sessionAttributeValueClassNamePattern =
                Pattern.compile ( sessionAttributeValueClassNameFilter );
        }
    }
    public boolean getWarnOnSessionAttributeFilterFailure() {
        return warnOnSessionAttributeFilterFailure;
    }
    public void setWarnOnSessionAttributeFilterFailure (
        boolean warnOnSessionAttributeFilterFailure ) {
        this.warnOnSessionAttributeFilterFailure = warnOnSessionAttributeFilterFailure;
    }
    @Override
    public Context getContext() {
        return context;
    }
    @Override
    public void setContext ( Context context ) {
        if ( this.context == context ) {
            return;
        }
        if ( !getState().equals ( LifecycleState.NEW ) ) {
            throw new IllegalStateException ( sm.getString ( "managerBase.setContextNotNew" ) );
        }
        Context oldContext = this.context;
        this.context = context;
        support.firePropertyChange ( "context", oldContext, this.context );
    }
    public String getClassName() {
        return this.getClass().getName();
    }
    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        if ( sessionIdGenerator != null ) {
            return sessionIdGenerator;
        } else if ( sessionIdGeneratorClass != null ) {
            try {
                sessionIdGenerator = sessionIdGeneratorClass.newInstance();
                return sessionIdGenerator;
            } catch ( IllegalAccessException ex ) {
            } catch ( InstantiationException ex ) {
            }
        }
        return null;
    }
    @Override
    public void setSessionIdGenerator ( SessionIdGenerator sessionIdGenerator ) {
        this.sessionIdGenerator = sessionIdGenerator;
        sessionIdGeneratorClass = sessionIdGenerator.getClass();
    }
    public String getName() {
        return ( name );
    }
    public String getSecureRandomClass() {
        return ( this.secureRandomClass );
    }
    public void setSecureRandomClass ( String secureRandomClass ) {
        String oldSecureRandomClass = this.secureRandomClass;
        this.secureRandomClass = secureRandomClass;
        support.firePropertyChange ( "secureRandomClass", oldSecureRandomClass,
                                     this.secureRandomClass );
    }
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }
    public void setSecureRandomAlgorithm ( String secureRandomAlgorithm ) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }
    public String getSecureRandomProvider() {
        return secureRandomProvider;
    }
    public void setSecureRandomProvider ( String secureRandomProvider ) {
        this.secureRandomProvider = secureRandomProvider;
    }
    @Override
    public int getRejectedSessions() {
        return rejectedSessions;
    }
    @Override
    public long getExpiredSessions() {
        return expiredSessions.get();
    }
    @Override
    public void setExpiredSessions ( long expiredSessions ) {
        this.expiredSessions.set ( expiredSessions );
    }
    public long getProcessingTime() {
        return processingTime;
    }
    public void setProcessingTime ( long processingTime ) {
        this.processingTime = processingTime;
    }
    public int getProcessExpiresFrequency() {
        return ( this.processExpiresFrequency );
    }
    public void setProcessExpiresFrequency ( int processExpiresFrequency ) {
        if ( processExpiresFrequency <= 0 ) {
            return;
        }
        int oldProcessExpiresFrequency = this.processExpiresFrequency;
        this.processExpiresFrequency = processExpiresFrequency;
        support.firePropertyChange ( "processExpiresFrequency",
                                     Integer.valueOf ( oldProcessExpiresFrequency ),
                                     Integer.valueOf ( this.processExpiresFrequency ) );
    }
    @Override
    public void backgroundProcess() {
        count = ( count + 1 ) % processExpiresFrequency;
        if ( count == 0 ) {
            processExpires();
        }
    }
    public void processExpires() {
        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireHere = 0 ;
        if ( log.isDebugEnabled() ) {
            log.debug ( "Start expire sessions " + getName() + " at " + timeNow + " sessioncount " + sessions.length );
        }
        for ( int i = 0; i < sessions.length; i++ ) {
            if ( sessions[i] != null && !sessions[i].isValid() ) {
                expireHere++;
            }
        }
        long timeEnd = System.currentTimeMillis();
        if ( log.isDebugEnabled() ) {
            log.debug ( "End expire sessions " + getName() + " processingTime " + ( timeEnd - timeNow ) + " expired sessions: " + expireHere );
        }
        processingTime += ( timeEnd - timeNow );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( context == null ) {
            throw new LifecycleException ( sm.getString ( "managerBase.contextNull" ) );
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        while ( sessionCreationTiming.size() < TIMING_STATS_CACHE_SIZE ) {
            sessionCreationTiming.add ( null );
        }
        while ( sessionExpirationTiming.size() < TIMING_STATS_CACHE_SIZE ) {
            sessionExpirationTiming.add ( null );
        }
        SessionIdGenerator sessionIdGenerator = getSessionIdGenerator();
        if ( sessionIdGenerator == null ) {
            sessionIdGenerator = new StandardSessionIdGenerator();
            setSessionIdGenerator ( sessionIdGenerator );
        }
        sessionIdGenerator.setJvmRoute ( getJvmRoute() );
        if ( sessionIdGenerator instanceof SessionIdGeneratorBase ) {
            SessionIdGeneratorBase sig = ( SessionIdGeneratorBase ) sessionIdGenerator;
            sig.setSecureRandomAlgorithm ( getSecureRandomAlgorithm() );
            sig.setSecureRandomClass ( getSecureRandomClass() );
            sig.setSecureRandomProvider ( getSecureRandomProvider() );
        }
        if ( sessionIdGenerator instanceof Lifecycle ) {
            ( ( Lifecycle ) sessionIdGenerator ).start();
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Force random number initialization starting" );
            }
            sessionIdGenerator.generateSessionId();
            if ( log.isDebugEnabled() ) {
                log.debug ( "Force random number initialization completed" );
            }
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        if ( sessionIdGenerator instanceof Lifecycle ) {
            ( ( Lifecycle ) sessionIdGenerator ).stop();
        }
    }
    @Override
    public void add ( Session session ) {
        sessions.put ( session.getIdInternal(), session );
        int size = getActiveSessions();
        if ( size > maxActive ) {
            synchronized ( maxActiveUpdateLock ) {
                if ( size > maxActive ) {
                    maxActive = size;
                }
            }
        }
    }
    @Override
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public Session createSession ( String sessionId ) {
        if ( ( maxActiveSessions >= 0 ) &&
                ( getActiveSessions() >= maxActiveSessions ) ) {
            rejectedSessions++;
            throw new TooManyActiveSessionsException (
                sm.getString ( "managerBase.createSession.ise" ),
                maxActiveSessions );
        }
        Session session = createEmptySession();
        session.setNew ( true );
        session.setValid ( true );
        session.setCreationTime ( System.currentTimeMillis() );
        session.setMaxInactiveInterval ( getContext().getSessionTimeout() * 60 );
        String id = sessionId;
        if ( id == null ) {
            id = generateSessionId();
        }
        session.setId ( id );
        sessionCounter++;
        SessionTiming timing = new SessionTiming ( session.getCreationTime(), 0 );
        synchronized ( sessionCreationTiming ) {
            sessionCreationTiming.add ( timing );
            sessionCreationTiming.poll();
        }
        return ( session );
    }
    @Override
    public Session createEmptySession() {
        return ( getNewSession() );
    }
    @Override
    public Session findSession ( String id ) throws IOException {
        if ( id == null ) {
            return null;
        }
        return sessions.get ( id );
    }
    @Override
    public Session[] findSessions() {
        return sessions.values().toArray ( new Session[0] );
    }
    @Override
    public void remove ( Session session ) {
        remove ( session, false );
    }
    @Override
    public void remove ( Session session, boolean update ) {
        if ( update ) {
            long timeNow = System.currentTimeMillis();
            int timeAlive =
                ( int ) ( timeNow - session.getCreationTimeInternal() ) / 1000;
            updateSessionMaxAliveTime ( timeAlive );
            expiredSessions.incrementAndGet();
            SessionTiming timing = new SessionTiming ( timeNow, timeAlive );
            synchronized ( sessionExpirationTiming ) {
                sessionExpirationTiming.add ( timing );
                sessionExpirationTiming.poll();
            }
        }
        if ( session.getIdInternal() != null ) {
            sessions.remove ( session.getIdInternal() );
        }
    }
    @Override
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    public void changeSessionId ( Session session ) {
        String newId = generateSessionId();
        changeSessionId ( session, newId, true, true );
    }
    @Override
    public void changeSessionId ( Session session, String newId ) {
        changeSessionId ( session, newId, true, true );
    }
    protected void changeSessionId ( Session session, String newId,
                                     boolean notifySessionListeners, boolean notifyContainerListeners ) {
        String oldId = session.getIdInternal();
        session.setId ( newId, false );
        session.tellChangedSessionId ( newId, oldId,
                                       notifySessionListeners, notifyContainerListeners );
    }
    @Override
    public boolean willAttributeDistribute ( String name, Object value ) {
        Pattern sessionAttributeNamePattern = getSessionAttributeNamePattern();
        if ( sessionAttributeNamePattern != null ) {
            if ( !sessionAttributeNamePattern.matcher ( name ).matches() ) {
                if ( getWarnOnSessionAttributeFilterFailure() || log.isDebugEnabled() ) {
                    String msg = sm.getString ( "managerBase.sessionAttributeNameFilter",
                                                name, sessionAttributeNamePattern );
                    if ( getWarnOnSessionAttributeFilterFailure() ) {
                        log.warn ( msg );
                    } else {
                        log.debug ( msg );
                    }
                }
                return false;
            }
        }
        Pattern sessionAttributeValueClassNamePattern = getSessionAttributeValueClassNamePattern();
        if ( value != null && sessionAttributeValueClassNamePattern != null ) {
            if ( !sessionAttributeValueClassNamePattern.matcher (
                        value.getClass().getName() ).matches() ) {
                if ( getWarnOnSessionAttributeFilterFailure() || log.isDebugEnabled() ) {
                    String msg = sm.getString ( "managerBase.sessionAttributeValueClassNameFilter",
                                                name, value.getClass().getName(), sessionAttributeNamePattern );
                    if ( getWarnOnSessionAttributeFilterFailure() ) {
                        log.warn ( msg );
                    } else {
                        log.debug ( msg );
                    }
                }
                return false;
            }
        }
        return true;
    }
    protected StandardSession getNewSession() {
        return new StandardSession ( this );
    }
    protected String generateSessionId() {
        String result = null;
        do {
            if ( result != null ) {
                duplicates++;
            }
            result = sessionIdGenerator.generateSessionId();
        } while ( sessions.containsKey ( result ) );
        return result;
    }
    public Engine getEngine() {
        Engine e = null;
        for ( Container c = getContext(); e == null && c != null ; c = c.getParent() ) {
            if ( c instanceof Engine ) {
                e = ( Engine ) c;
            }
        }
        return e;
    }
    public String getJvmRoute() {
        Engine e = getEngine();
        return e == null ? null : e.getJvmRoute();
    }
    @Override
    public void setSessionCounter ( long sessionCounter ) {
        this.sessionCounter = sessionCounter;
    }
    @Override
    public long getSessionCounter() {
        return sessionCounter;
    }
    public int getDuplicates() {
        return duplicates;
    }
    public void setDuplicates ( int duplicates ) {
        this.duplicates = duplicates;
    }
    @Override
    public int getActiveSessions() {
        return sessions.size();
    }
    @Override
    public int getMaxActive() {
        return maxActive;
    }
    @Override
    public void setMaxActive ( int maxActive ) {
        synchronized ( maxActiveUpdateLock ) {
            this.maxActive = maxActive;
        }
    }
    public int getMaxActiveSessions() {
        return ( this.maxActiveSessions );
    }
    public void setMaxActiveSessions ( int max ) {
        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        support.firePropertyChange ( "maxActiveSessions",
                                     Integer.valueOf ( oldMaxActiveSessions ),
                                     Integer.valueOf ( this.maxActiveSessions ) );
    }
    @Override
    public int getSessionMaxAliveTime() {
        return sessionMaxAliveTime;
    }
    @Override
    public void setSessionMaxAliveTime ( int sessionMaxAliveTime ) {
        synchronized ( sessionMaxAliveTimeUpdateLock ) {
            this.sessionMaxAliveTime = sessionMaxAliveTime;
        }
    }
    public void updateSessionMaxAliveTime ( int sessionAliveTime ) {
        if ( sessionAliveTime > this.sessionMaxAliveTime ) {
            synchronized ( sessionMaxAliveTimeUpdateLock ) {
                if ( sessionAliveTime > this.sessionMaxAliveTime ) {
                    this.sessionMaxAliveTime = sessionAliveTime;
                }
            }
        }
    }
    @Override
    public int getSessionAverageAliveTime() {
        List<SessionTiming> copy = new ArrayList<>();
        synchronized ( sessionExpirationTiming ) {
            copy.addAll ( sessionExpirationTiming );
        }
        int counter = 0;
        int result = 0;
        Iterator<SessionTiming> iter = copy.iterator();
        while ( iter.hasNext() ) {
            SessionTiming timing = iter.next();
            if ( timing != null ) {
                int timeAlive = timing.getDuration();
                counter++;
                result =
                    ( result * ( ( counter - 1 ) / counter ) ) + ( timeAlive / counter );
            }
        }
        return result;
    }
    @Override
    public int getSessionCreateRate() {
        List<SessionTiming> copy = new ArrayList<>();
        synchronized ( sessionCreationTiming ) {
            copy.addAll ( sessionCreationTiming );
        }
        return calculateRate ( copy );
    }
    @Override
    public int getSessionExpireRate() {
        List<SessionTiming> copy = new ArrayList<>();
        synchronized ( sessionExpirationTiming ) {
            copy.addAll ( sessionExpirationTiming );
        }
        return calculateRate ( copy );
    }
    private static int calculateRate ( List<SessionTiming> sessionTiming ) {
        long now = System.currentTimeMillis();
        long oldest = now;
        int counter = 0;
        int result = 0;
        Iterator<SessionTiming> iter = sessionTiming.iterator();
        while ( iter.hasNext() ) {
            SessionTiming timing = iter.next();
            if ( timing != null ) {
                counter++;
                if ( timing.getTimestamp() < oldest ) {
                    oldest = timing.getTimestamp();
                }
            }
        }
        if ( counter > 0 ) {
            if ( oldest < now ) {
                result = ( 1000 * 60 * counter ) / ( int ) ( now - oldest );
            } else {
                result = Integer.MAX_VALUE;
            }
        }
        return result;
    }
    public String listSessionIds() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = sessions.keySet().iterator();
        while ( keys.hasNext() ) {
            sb.append ( keys.next() ).append ( " " );
        }
        return sb.toString();
    }
    public String getSessionAttribute ( String sessionId, String key ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return null;
        }
        Object o = s.getSession().getAttribute ( key );
        if ( o == null ) {
            return null;
        }
        return o.toString();
    }
    public HashMap<String, String> getSession ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return null;
        }
        Enumeration<String> ee = s.getSession().getAttributeNames();
        if ( ee == null || !ee.hasMoreElements() ) {
            return null;
        }
        HashMap<String, String> map = new HashMap<>();
        while ( ee.hasMoreElements() ) {
            String attrName = ee.nextElement();
            map.put ( attrName, getSessionAttribute ( sessionId, attrName ) );
        }
        return map;
    }
    public void expireSession ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return;
        }
        s.expire();
    }
    public long getThisAccessedTimestamp ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            return -1 ;
        }
        return s.getThisAccessedTime();
    }
    public String getThisAccessedTime ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getThisAccessedTime() ).toString();
    }
    public long getLastAccessedTimestamp ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            return -1 ;
        }
        return s.getLastAccessedTime();
    }
    public String getLastAccessedTime ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getLastAccessedTime() ).toString();
    }
    public String getCreationTime ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            if ( log.isInfoEnabled() ) {
                log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getCreationTime() ).toString();
    }
    public long getCreationTimestamp ( String sessionId ) {
        Session s = sessions.get ( sessionId );
        if ( s == null ) {
            return -1 ;
        }
        return s.getCreationTime();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( context == null ) {
            sb.append ( "Context is null" );
        } else {
            sb.append ( context.getName() );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    public String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder ( "type=Manager" );
        name.append ( ",host=" );
        name.append ( context.getParent().getName() );
        name.append ( ",context=" );
        String contextName = context.getName();
        if ( !contextName.startsWith ( "/" ) ) {
            name.append ( '/' );
        }
        name.append ( contextName );
        return name.toString();
    }
    @Override
    public String getDomainInternal() {
        return context.getDomain();
    }
    protected static final class SessionTiming {
        private final long timestamp;
        private final int duration;
        public SessionTiming ( long timestamp, int duration ) {
            this.timestamp = timestamp;
            this.duration = duration;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public int getDuration() {
            return duration;
        }
    }
}
