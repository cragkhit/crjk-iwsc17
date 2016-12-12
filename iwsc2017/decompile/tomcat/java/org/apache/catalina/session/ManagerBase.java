package org.apache.catalina.session;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import java.util.regex.PatternSyntaxException;
import org.apache.catalina.Globals;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import org.apache.juli.logging.LogFactory;
import java.util.regex.Pattern;
import java.beans.PropertyChangeSupport;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Session;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Deque;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Context;
import org.apache.juli.logging.Log;
import org.apache.catalina.Manager;
import org.apache.catalina.util.LifecycleMBeanBase;
public abstract class ManagerBase extends LifecycleMBeanBase implements Manager {
    private final Log log;
    private Context context;
    private static final String name = "ManagerBase";
    protected String secureRandomClass;
    protected String secureRandomAlgorithm;
    protected String secureRandomProvider;
    protected SessionIdGenerator sessionIdGenerator;
    protected Class<? extends SessionIdGenerator> sessionIdGeneratorClass;
    protected volatile int sessionMaxAliveTime;
    private final Object sessionMaxAliveTimeUpdateLock;
    protected static final int TIMING_STATS_CACHE_SIZE = 100;
    protected final Deque<SessionTiming> sessionCreationTiming;
    protected final Deque<SessionTiming> sessionExpirationTiming;
    protected final AtomicLong expiredSessions;
    protected Map<String, Session> sessions;
    protected long sessionCounter;
    protected volatile int maxActive;
    private final Object maxActiveUpdateLock;
    protected int maxActiveSessions;
    protected int rejectedSessions;
    protected volatile int duplicates;
    protected long processingTime;
    private int count;
    protected int processExpiresFrequency;
    protected static final StringManager sm;
    protected final PropertyChangeSupport support;
    private Pattern sessionAttributeNamePattern;
    private Pattern sessionAttributeValueClassNamePattern;
    private boolean warnOnSessionAttributeFilterFailure;
    public ManagerBase() {
        this.log = LogFactory.getLog ( ManagerBase.class );
        this.secureRandomClass = null;
        this.secureRandomAlgorithm = "SHA1PRNG";
        this.secureRandomProvider = null;
        this.sessionIdGenerator = null;
        this.sessionIdGeneratorClass = null;
        this.sessionMaxAliveTimeUpdateLock = new Object();
        this.sessionCreationTiming = new LinkedList<SessionTiming>();
        this.sessionExpirationTiming = new LinkedList<SessionTiming>();
        this.expiredSessions = new AtomicLong ( 0L );
        this.sessions = new ConcurrentHashMap<String, Session>();
        this.sessionCounter = 0L;
        this.maxActive = 0;
        this.maxActiveUpdateLock = new Object();
        this.maxActiveSessions = -1;
        this.rejectedSessions = 0;
        this.duplicates = 0;
        this.processingTime = 0L;
        this.count = 0;
        this.processExpiresFrequency = 6;
        this.support = new PropertyChangeSupport ( this );
        if ( Globals.IS_SECURITY_ENABLED ) {
            this.setSessionAttributeValueClassNameFilter ( "java\\.lang\\.(?:Boolean|Integer|Long|Number|String)" );
            this.setWarnOnSessionAttributeFilterFailure ( true );
        }
    }
    public String getSessionAttributeNameFilter() {
        if ( this.sessionAttributeNamePattern == null ) {
            return null;
        }
        return this.sessionAttributeNamePattern.toString();
    }
    public void setSessionAttributeNameFilter ( final String sessionAttributeNameFilter ) throws PatternSyntaxException {
        if ( sessionAttributeNameFilter == null || sessionAttributeNameFilter.length() == 0 ) {
            this.sessionAttributeNamePattern = null;
        } else {
            this.sessionAttributeNamePattern = Pattern.compile ( sessionAttributeNameFilter );
        }
    }
    protected Pattern getSessionAttributeNamePattern() {
        return this.sessionAttributeNamePattern;
    }
    public String getSessionAttributeValueClassNameFilter() {
        if ( this.sessionAttributeValueClassNamePattern == null ) {
            return null;
        }
        return this.sessionAttributeValueClassNamePattern.toString();
    }
    protected Pattern getSessionAttributeValueClassNamePattern() {
        return this.sessionAttributeValueClassNamePattern;
    }
    public void setSessionAttributeValueClassNameFilter ( final String sessionAttributeValueClassNameFilter ) throws PatternSyntaxException {
        if ( sessionAttributeValueClassNameFilter == null || sessionAttributeValueClassNameFilter.length() == 0 ) {
            this.sessionAttributeValueClassNamePattern = null;
        } else {
            this.sessionAttributeValueClassNamePattern = Pattern.compile ( sessionAttributeValueClassNameFilter );
        }
    }
    public boolean getWarnOnSessionAttributeFilterFailure() {
        return this.warnOnSessionAttributeFilterFailure;
    }
    public void setWarnOnSessionAttributeFilterFailure ( final boolean warnOnSessionAttributeFilterFailure ) {
        this.warnOnSessionAttributeFilterFailure = warnOnSessionAttributeFilterFailure;
    }
    @Override
    public Context getContext() {
        return this.context;
    }
    @Override
    public void setContext ( final Context context ) {
        if ( this.context == context ) {
            return;
        }
        if ( !this.getState().equals ( LifecycleState.NEW ) ) {
            throw new IllegalStateException ( ManagerBase.sm.getString ( "managerBase.setContextNotNew" ) );
        }
        final Context oldContext = this.context;
        this.context = context;
        this.support.firePropertyChange ( "context", oldContext, this.context );
    }
    public String getClassName() {
        return this.getClass().getName();
    }
    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        if ( this.sessionIdGenerator != null ) {
            return this.sessionIdGenerator;
        }
        if ( this.sessionIdGeneratorClass != null ) {
            try {
                return this.sessionIdGenerator = ( SessionIdGenerator ) this.sessionIdGeneratorClass.newInstance();
            } catch ( IllegalAccessException ex ) {}
            catch ( InstantiationException ex2 ) {}
        }
        return null;
    }
    @Override
    public void setSessionIdGenerator ( final SessionIdGenerator sessionIdGenerator ) {
        this.sessionIdGenerator = sessionIdGenerator;
        this.sessionIdGeneratorClass = sessionIdGenerator.getClass();
    }
    public String getName() {
        return "ManagerBase";
    }
    public String getSecureRandomClass() {
        return this.secureRandomClass;
    }
    public void setSecureRandomClass ( final String secureRandomClass ) {
        final String oldSecureRandomClass = this.secureRandomClass;
        this.secureRandomClass = secureRandomClass;
        this.support.firePropertyChange ( "secureRandomClass", oldSecureRandomClass, this.secureRandomClass );
    }
    public String getSecureRandomAlgorithm() {
        return this.secureRandomAlgorithm;
    }
    public void setSecureRandomAlgorithm ( final String secureRandomAlgorithm ) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }
    public String getSecureRandomProvider() {
        return this.secureRandomProvider;
    }
    public void setSecureRandomProvider ( final String secureRandomProvider ) {
        this.secureRandomProvider = secureRandomProvider;
    }
    @Override
    public int getRejectedSessions() {
        return this.rejectedSessions;
    }
    @Override
    public long getExpiredSessions() {
        return this.expiredSessions.get();
    }
    @Override
    public void setExpiredSessions ( final long expiredSessions ) {
        this.expiredSessions.set ( expiredSessions );
    }
    public long getProcessingTime() {
        return this.processingTime;
    }
    public void setProcessingTime ( final long processingTime ) {
        this.processingTime = processingTime;
    }
    public int getProcessExpiresFrequency() {
        return this.processExpiresFrequency;
    }
    public void setProcessExpiresFrequency ( final int processExpiresFrequency ) {
        if ( processExpiresFrequency <= 0 ) {
            return;
        }
        final int oldProcessExpiresFrequency = this.processExpiresFrequency;
        this.processExpiresFrequency = processExpiresFrequency;
        this.support.firePropertyChange ( "processExpiresFrequency", oldProcessExpiresFrequency, ( Object ) this.processExpiresFrequency );
    }
    @Override
    public void backgroundProcess() {
        this.count = ( this.count + 1 ) % this.processExpiresFrequency;
        if ( this.count == 0 ) {
            this.processExpires();
        }
    }
    public void processExpires() {
        final long timeNow = System.currentTimeMillis();
        final Session[] sessions = this.findSessions();
        int expireHere = 0;
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Start expire sessions " + this.getName() + " at " + timeNow + " sessioncount " + sessions.length );
        }
        for ( int i = 0; i < sessions.length; ++i ) {
            if ( sessions[i] != null && !sessions[i].isValid() ) {
                ++expireHere;
            }
        }
        final long timeEnd = System.currentTimeMillis();
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "End expire sessions " + this.getName() + " processingTime " + ( timeEnd - timeNow ) + " expired sessions: " + expireHere );
        }
        this.processingTime += timeEnd - timeNow;
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( this.context == null ) {
            throw new LifecycleException ( ManagerBase.sm.getString ( "managerBase.contextNull" ) );
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        while ( this.sessionCreationTiming.size() < 100 ) {
            this.sessionCreationTiming.add ( null );
        }
        while ( this.sessionExpirationTiming.size() < 100 ) {
            this.sessionExpirationTiming.add ( null );
        }
        SessionIdGenerator sessionIdGenerator = this.getSessionIdGenerator();
        if ( sessionIdGenerator == null ) {
            sessionIdGenerator = new StandardSessionIdGenerator();
            this.setSessionIdGenerator ( sessionIdGenerator );
        }
        sessionIdGenerator.setJvmRoute ( this.getJvmRoute() );
        if ( sessionIdGenerator instanceof SessionIdGeneratorBase ) {
            final SessionIdGeneratorBase sig = ( SessionIdGeneratorBase ) sessionIdGenerator;
            sig.setSecureRandomAlgorithm ( this.getSecureRandomAlgorithm() );
            sig.setSecureRandomClass ( this.getSecureRandomClass() );
            sig.setSecureRandomProvider ( this.getSecureRandomProvider() );
        }
        if ( sessionIdGenerator instanceof Lifecycle ) {
            ( ( Lifecycle ) sessionIdGenerator ).start();
        } else {
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Force random number initialization starting" );
            }
            sessionIdGenerator.generateSessionId();
            if ( this.log.isDebugEnabled() ) {
                this.log.debug ( "Force random number initialization completed" );
            }
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        if ( this.sessionIdGenerator instanceof Lifecycle ) {
            ( ( Lifecycle ) this.sessionIdGenerator ).stop();
        }
    }
    @Override
    public void add ( final Session session ) {
        this.sessions.put ( session.getIdInternal(), session );
        final int size = this.getActiveSessions();
        if ( size > this.maxActive ) {
            synchronized ( this.maxActiveUpdateLock ) {
                if ( size > this.maxActive ) {
                    this.maxActive = size;
                }
            }
        }
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    @Override
    public Session createSession ( final String sessionId ) {
        if ( this.maxActiveSessions >= 0 && this.getActiveSessions() >= this.maxActiveSessions ) {
            ++this.rejectedSessions;
            throw new TooManyActiveSessionsException ( ManagerBase.sm.getString ( "managerBase.createSession.ise" ), this.maxActiveSessions );
        }
        final Session session = this.createEmptySession();
        session.setNew ( true );
        session.setValid ( true );
        session.setCreationTime ( System.currentTimeMillis() );
        session.setMaxInactiveInterval ( this.getContext().getSessionTimeout() * 60 );
        String id = sessionId;
        if ( id == null ) {
            id = this.generateSessionId();
        }
        session.setId ( id );
        ++this.sessionCounter;
        final SessionTiming timing = new SessionTiming ( session.getCreationTime(), 0 );
        synchronized ( this.sessionCreationTiming ) {
            this.sessionCreationTiming.add ( timing );
            this.sessionCreationTiming.poll();
        }
        return session;
    }
    @Override
    public Session createEmptySession() {
        return this.getNewSession();
    }
    @Override
    public Session findSession ( final String id ) throws IOException {
        if ( id == null ) {
            return null;
        }
        return this.sessions.get ( id );
    }
    @Override
    public Session[] findSessions() {
        return this.sessions.values().toArray ( new Session[0] );
    }
    @Override
    public void remove ( final Session session ) {
        this.remove ( session, false );
    }
    @Override
    public void remove ( final Session session, final boolean update ) {
        if ( update ) {
            final long timeNow = System.currentTimeMillis();
            final int timeAlive = ( int ) ( timeNow - session.getCreationTimeInternal() ) / 1000;
            this.updateSessionMaxAliveTime ( timeAlive );
            this.expiredSessions.incrementAndGet();
            final SessionTiming timing = new SessionTiming ( timeNow, timeAlive );
            synchronized ( this.sessionExpirationTiming ) {
                this.sessionExpirationTiming.add ( timing );
                this.sessionExpirationTiming.poll();
            }
        }
        if ( session.getIdInternal() != null ) {
            this.sessions.remove ( session.getIdInternal() );
        }
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    @Override
    public void changeSessionId ( final Session session ) {
        final String newId = this.generateSessionId();
        this.changeSessionId ( session, newId, true, true );
    }
    @Override
    public void changeSessionId ( final Session session, final String newId ) {
        this.changeSessionId ( session, newId, true, true );
    }
    protected void changeSessionId ( final Session session, final String newId, final boolean notifySessionListeners, final boolean notifyContainerListeners ) {
        final String oldId = session.getIdInternal();
        session.setId ( newId, false );
        session.tellChangedSessionId ( newId, oldId, notifySessionListeners, notifyContainerListeners );
    }
    @Override
    public boolean willAttributeDistribute ( final String name, final Object value ) {
        final Pattern sessionAttributeNamePattern = this.getSessionAttributeNamePattern();
        if ( sessionAttributeNamePattern != null && !sessionAttributeNamePattern.matcher ( name ).matches() ) {
            if ( this.getWarnOnSessionAttributeFilterFailure() || this.log.isDebugEnabled() ) {
                final String msg = ManagerBase.sm.getString ( "managerBase.sessionAttributeNameFilter", name, sessionAttributeNamePattern );
                if ( this.getWarnOnSessionAttributeFilterFailure() ) {
                    this.log.warn ( msg );
                } else {
                    this.log.debug ( msg );
                }
            }
            return false;
        }
        final Pattern sessionAttributeValueClassNamePattern = this.getSessionAttributeValueClassNamePattern();
        if ( value != null && sessionAttributeValueClassNamePattern != null && !sessionAttributeValueClassNamePattern.matcher ( value.getClass().getName() ).matches() ) {
            if ( this.getWarnOnSessionAttributeFilterFailure() || this.log.isDebugEnabled() ) {
                final String msg2 = ManagerBase.sm.getString ( "managerBase.sessionAttributeValueClassNameFilter", name, value.getClass().getName(), sessionAttributeNamePattern );
                if ( this.getWarnOnSessionAttributeFilterFailure() ) {
                    this.log.warn ( msg2 );
                } else {
                    this.log.debug ( msg2 );
                }
            }
            return false;
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
                ++this.duplicates;
            }
            result = this.sessionIdGenerator.generateSessionId();
        } while ( this.sessions.containsKey ( result ) );
        return result;
    }
    public Engine getEngine() {
        Engine e = null;
        for ( Container c = this.getContext(); e == null && c != null; c = c.getParent() ) {
            if ( c instanceof Engine ) {
                e = ( Engine ) c;
            }
        }
        return e;
    }
    public String getJvmRoute() {
        final Engine e = this.getEngine();
        return ( e == null ) ? null : e.getJvmRoute();
    }
    @Override
    public void setSessionCounter ( final long sessionCounter ) {
        this.sessionCounter = sessionCounter;
    }
    @Override
    public long getSessionCounter() {
        return this.sessionCounter;
    }
    public int getDuplicates() {
        return this.duplicates;
    }
    public void setDuplicates ( final int duplicates ) {
        this.duplicates = duplicates;
    }
    @Override
    public int getActiveSessions() {
        return this.sessions.size();
    }
    @Override
    public int getMaxActive() {
        return this.maxActive;
    }
    @Override
    public void setMaxActive ( final int maxActive ) {
        synchronized ( this.maxActiveUpdateLock ) {
            this.maxActive = maxActive;
        }
    }
    public int getMaxActiveSessions() {
        return this.maxActiveSessions;
    }
    public void setMaxActiveSessions ( final int max ) {
        final int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        this.support.firePropertyChange ( "maxActiveSessions", oldMaxActiveSessions, ( Object ) this.maxActiveSessions );
    }
    @Override
    public int getSessionMaxAliveTime() {
        return this.sessionMaxAliveTime;
    }
    @Override
    public void setSessionMaxAliveTime ( final int sessionMaxAliveTime ) {
        synchronized ( this.sessionMaxAliveTimeUpdateLock ) {
            this.sessionMaxAliveTime = sessionMaxAliveTime;
        }
    }
    public void updateSessionMaxAliveTime ( final int sessionAliveTime ) {
        if ( sessionAliveTime > this.sessionMaxAliveTime ) {
            synchronized ( this.sessionMaxAliveTimeUpdateLock ) {
                if ( sessionAliveTime > this.sessionMaxAliveTime ) {
                    this.sessionMaxAliveTime = sessionAliveTime;
                }
            }
        }
    }
    @Override
    public int getSessionAverageAliveTime() {
        final List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized ( this.sessionExpirationTiming ) {
            copy.addAll ( this.sessionExpirationTiming );
        }
        int counter = 0;
        int result = 0;
        for ( final SessionTiming timing : copy ) {
            if ( timing != null ) {
                final int timeAlive = timing.getDuration();
                ++counter;
                result = result * ( ( counter - 1 ) / counter ) + timeAlive / counter;
            }
        }
        return result;
    }
    @Override
    public int getSessionCreateRate() {
        final List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized ( this.sessionCreationTiming ) {
            copy.addAll ( this.sessionCreationTiming );
        }
        return calculateRate ( copy );
    }
    @Override
    public int getSessionExpireRate() {
        final List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized ( this.sessionExpirationTiming ) {
            copy.addAll ( this.sessionExpirationTiming );
        }
        return calculateRate ( copy );
    }
    private static int calculateRate ( final List<SessionTiming> sessionTiming ) {
        long oldest;
        final long now = oldest = System.currentTimeMillis();
        int counter = 0;
        int result = 0;
        for ( final SessionTiming timing : sessionTiming ) {
            if ( timing != null ) {
                ++counter;
                if ( timing.getTimestamp() >= oldest ) {
                    continue;
                }
                oldest = timing.getTimestamp();
            }
        }
        if ( counter > 0 ) {
            if ( oldest < now ) {
                result = 60000 * counter / ( int ) ( now - oldest );
            } else {
                result = Integer.MAX_VALUE;
            }
        }
        return result;
    }
    public String listSessionIds() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> keys = this.sessions.keySet().iterator();
        while ( keys.hasNext() ) {
            sb.append ( keys.next() ).append ( " " );
        }
        return sb.toString();
    }
    public String getSessionAttribute ( final String sessionId, final String key ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return null;
        }
        final Object o = s.getSession().getAttribute ( key );
        if ( o == null ) {
            return null;
        }
        return o.toString();
    }
    public HashMap<String, String> getSession ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return null;
        }
        final Enumeration<String> ee = ( Enumeration<String> ) s.getSession().getAttributeNames();
        if ( ee == null || !ee.hasMoreElements() ) {
            return null;
        }
        final HashMap<String, String> map = new HashMap<String, String>();
        while ( ee.hasMoreElements() ) {
            final String attrName = ee.nextElement();
            map.put ( attrName, this.getSessionAttribute ( sessionId, attrName ) );
        }
        return map;
    }
    public void expireSession ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return;
        }
        s.expire();
    }
    public long getThisAccessedTimestamp ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            return -1L;
        }
        return s.getThisAccessedTime();
    }
    public String getThisAccessedTime ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getThisAccessedTime() ).toString();
    }
    public long getLastAccessedTimestamp ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            return -1L;
        }
        return s.getLastAccessedTime();
    }
    public String getLastAccessedTime ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getLastAccessedTime() ).toString();
    }
    public String getCreationTime ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            if ( this.log.isInfoEnabled() ) {
                this.log.info ( "Session not found " + sessionId );
            }
            return "";
        }
        return new Date ( s.getCreationTime() ).toString();
    }
    public long getCreationTimestamp ( final String sessionId ) {
        final Session s = this.sessions.get ( sessionId );
        if ( s == null ) {
            return -1L;
        }
        return s.getCreationTime();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( this.context == null ) {
            sb.append ( "Context is null" );
        } else {
            sb.append ( this.context.getName() );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    public String getObjectNameKeyProperties() {
        final StringBuilder name = new StringBuilder ( "type=Manager" );
        name.append ( ",host=" );
        name.append ( this.context.getParent().getName() );
        name.append ( ",context=" );
        final String contextName = this.context.getName();
        if ( !contextName.startsWith ( "/" ) ) {
            name.append ( '/' );
        }
        name.append ( contextName );
        return name.toString();
    }
    public String getDomainInternal() {
        return this.context.getDomain();
    }
    static {
        sm = StringManager.getManager ( ManagerBase.class );
    }
    protected static final class SessionTiming {
        private final long timestamp;
        private final int duration;
        public SessionTiming ( final long timestamp, final int duration ) {
            this.timestamp = timestamp;
            this.duration = duration;
        }
        public long getTimestamp() {
            return this.timestamp;
        }
        public int getDuration() {
            return this.duration;
        }
    }
}
