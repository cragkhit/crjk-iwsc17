package org.apache.catalina.realm;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.LogFactory;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSContext;
import java.security.cert.X509Certificate;
import java.security.Principal;
import org.apache.catalina.LifecycleException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.juli.logging.Log;
public class LockOutRealm extends CombinedRealm {
    private static final Log log;
    protected static final String name = "LockOutRealm";
    protected int failureCount;
    protected int lockOutTime;
    protected int cacheSize;
    protected int cacheRemovalWarningTime;
    protected Map<String, LockRecord> failedUsers;
    public LockOutRealm() {
        this.failureCount = 5;
        this.lockOutTime = 300;
        this.cacheSize = 1000;
        this.cacheRemovalWarningTime = 3600;
        this.failedUsers = null;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.failedUsers = new LinkedHashMap<String, LockRecord> ( this.cacheSize, 0.75f, true ) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry ( final Map.Entry<String, LockRecord> eldest ) {
                if ( this.size() > LockOutRealm.this.cacheSize ) {
                    final long timeInCache = ( System.currentTimeMillis() - eldest.getValue().getLastFailureTime() ) / 1000L;
                    if ( timeInCache < LockOutRealm.this.cacheRemovalWarningTime ) {
                        LockOutRealm.log.warn ( RealmBase.sm.getString ( "lockOutRealm.removeWarning", eldest.getKey(), timeInCache ) );
                    }
                    return true;
                }
                return false;
            }
        };
        super.startInternal();
    }
    @Override
    public Principal authenticate ( final String username, final String clientDigest, final String nonce, final String nc, final String cnonce, final String qop, final String realmName, final String md5a2 ) {
        final Principal authenticatedUser = super.authenticate ( username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2 );
        return this.filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( final String username, final String credentials ) {
        final Principal authenticatedUser = super.authenticate ( username, credentials );
        return this.filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( final X509Certificate[] certs ) {
        String username = null;
        if ( certs != null && certs.length > 0 ) {
            username = certs[0].getSubjectDN().getName();
        }
        final Principal authenticatedUser = super.authenticate ( certs );
        return this.filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( final GSSContext gssContext, final boolean storeCreds ) {
        if ( gssContext.isEstablished() ) {
            String username = null;
            GSSName name = null;
            try {
                name = gssContext.getSrcName();
            } catch ( GSSException e ) {
                LockOutRealm.log.warn ( LockOutRealm.sm.getString ( "realmBase.gssNameFail" ), e );
                return null;
            }
            username = name.toString();
            final Principal authenticatedUser = super.authenticate ( gssContext, storeCreds );
            return this.filterLockedAccounts ( username, authenticatedUser );
        }
        return null;
    }
    private Principal filterLockedAccounts ( final String username, final Principal authenticatedUser ) {
        if ( authenticatedUser == null && this.isAvailable() ) {
            this.registerAuthFailure ( username );
        }
        if ( this.isLocked ( username ) ) {
            LockOutRealm.log.warn ( LockOutRealm.sm.getString ( "lockOutRealm.authLockedUser", username ) );
            return null;
        }
        if ( authenticatedUser != null ) {
            this.registerAuthSuccess ( username );
        }
        return authenticatedUser;
    }
    public void unlock ( final String username ) {
        this.registerAuthSuccess ( username );
    }
    private boolean isLocked ( final String username ) {
        LockRecord lockRecord = null;
        synchronized ( this ) {
            lockRecord = this.failedUsers.get ( username );
        }
        return lockRecord != null && ( lockRecord.getFailures() >= this.failureCount && ( System.currentTimeMillis() - lockRecord.getLastFailureTime() ) / 1000L < this.lockOutTime );
    }
    private synchronized void registerAuthSuccess ( final String username ) {
        this.failedUsers.remove ( username );
    }
    private void registerAuthFailure ( final String username ) {
        LockRecord lockRecord = null;
        synchronized ( this ) {
            if ( !this.failedUsers.containsKey ( username ) ) {
                lockRecord = new LockRecord();
                this.failedUsers.put ( username, lockRecord );
            } else {
                lockRecord = this.failedUsers.get ( username );
                if ( lockRecord.getFailures() >= this.failureCount && ( System.currentTimeMillis() - lockRecord.getLastFailureTime() ) / 1000L > this.lockOutTime ) {
                    lockRecord.setFailures ( 0 );
                }
            }
        }
        lockRecord.registerFailure();
    }
    public int getFailureCount() {
        return this.failureCount;
    }
    public void setFailureCount ( final int failureCount ) {
        this.failureCount = failureCount;
    }
    public int getLockOutTime() {
        return this.lockOutTime;
    }
    @Override
    protected String getName() {
        return "LockOutRealm";
    }
    public void setLockOutTime ( final int lockOutTime ) {
        this.lockOutTime = lockOutTime;
    }
    public int getCacheSize() {
        return this.cacheSize;
    }
    public void setCacheSize ( final int cacheSize ) {
        this.cacheSize = cacheSize;
    }
    public int getCacheRemovalWarningTime() {
        return this.cacheRemovalWarningTime;
    }
    public void setCacheRemovalWarningTime ( final int cacheRemovalWarningTime ) {
        this.cacheRemovalWarningTime = cacheRemovalWarningTime;
    }
    static {
        log = LogFactory.getLog ( LockOutRealm.class );
    }
    protected static class LockRecord {
        private final AtomicInteger failures;
        private long lastFailureTime;
        protected LockRecord() {
            this.failures = new AtomicInteger ( 0 );
            this.lastFailureTime = 0L;
        }
        public int getFailures() {
            return this.failures.get();
        }
        public void setFailures ( final int theFailures ) {
            this.failures.set ( theFailures );
        }
        public long getLastFailureTime() {
            return this.lastFailureTime;
        }
        public void registerFailure() {
            this.failures.incrementAndGet();
            this.lastFailureTime = System.currentTimeMillis();
        }
    }
}
