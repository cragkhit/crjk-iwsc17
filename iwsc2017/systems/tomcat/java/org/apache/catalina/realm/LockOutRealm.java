package org.apache.catalina.realm;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
public class LockOutRealm extends CombinedRealm {
    private static final Log log = LogFactory.getLog ( LockOutRealm.class );
    protected static final String name = "LockOutRealm";
    protected int failureCount = 5;
    protected int lockOutTime = 300;
    protected int cacheSize = 1000;
    protected int cacheRemovalWarningTime = 3600;
    protected Map<String, LockRecord> failedUsers = null;
    @Override
    protected void startInternal() throws LifecycleException {
        failedUsers = new LinkedHashMap<String, LockRecord> ( cacheSize, 0.75f,
        true ) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry (
                Map.Entry<String, LockRecord> eldest ) {
                if ( size() > cacheSize ) {
                    long timeInCache = ( System.currentTimeMillis() -
                                         eldest.getValue().getLastFailureTime() ) / 1000;
                    if ( timeInCache < cacheRemovalWarningTime ) {
                        log.warn ( sm.getString ( "lockOutRealm.removeWarning",
                                                  eldest.getKey(), Long.valueOf ( timeInCache ) ) );
                    }
                    return true;
                }
                return false;
            }
        };
        super.startInternal();
    }
    @Override
    public Principal authenticate ( String username, String clientDigest,
                                    String nonce, String nc, String cnonce, String qop,
                                    String realmName, String md5a2 ) {
        Principal authenticatedUser = super.authenticate ( username, clientDigest, nonce, nc, cnonce,
                                      qop, realmName, md5a2 );
        return filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( String username, String credentials ) {
        Principal authenticatedUser = super.authenticate ( username, credentials );
        return filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( X509Certificate[] certs ) {
        String username = null;
        if ( certs != null && certs.length > 0 ) {
            username = certs[0].getSubjectDN().getName();
        }
        Principal authenticatedUser = super.authenticate ( certs );
        return filterLockedAccounts ( username, authenticatedUser );
    }
    @Override
    public Principal authenticate ( GSSContext gssContext, boolean storeCreds ) {
        if ( gssContext.isEstablished() ) {
            String username = null;
            GSSName name = null;
            try {
                name = gssContext.getSrcName();
            } catch ( GSSException e ) {
                log.warn ( sm.getString ( "realmBase.gssNameFail" ), e );
                return null;
            }
            username = name.toString();
            Principal authenticatedUser = super.authenticate ( gssContext, storeCreds );
            return filterLockedAccounts ( username, authenticatedUser );
        }
        return null;
    }
    private Principal filterLockedAccounts ( String username, Principal authenticatedUser ) {
        if ( authenticatedUser == null && isAvailable() ) {
            registerAuthFailure ( username );
        }
        if ( isLocked ( username ) ) {
            log.warn ( sm.getString ( "lockOutRealm.authLockedUser", username ) );
            return null;
        }
        if ( authenticatedUser != null ) {
            registerAuthSuccess ( username );
        }
        return authenticatedUser;
    }
    public void unlock ( String username ) {
        registerAuthSuccess ( username );
    }
    private boolean isLocked ( String username ) {
        LockRecord lockRecord = null;
        synchronized ( this ) {
            lockRecord = failedUsers.get ( username );
        }
        if ( lockRecord == null ) {
            return false;
        }
        if ( lockRecord.getFailures() >= failureCount &&
                ( System.currentTimeMillis() -
                  lockRecord.getLastFailureTime() ) / 1000 < lockOutTime ) {
            return true;
        }
        return false;
    }
    private synchronized void registerAuthSuccess ( String username ) {
        failedUsers.remove ( username );
    }
    private void registerAuthFailure ( String username ) {
        LockRecord lockRecord = null;
        synchronized ( this ) {
            if ( !failedUsers.containsKey ( username ) ) {
                lockRecord = new LockRecord();
                failedUsers.put ( username, lockRecord );
            } else {
                lockRecord = failedUsers.get ( username );
                if ( lockRecord.getFailures() >= failureCount &&
                        ( ( System.currentTimeMillis() -
                            lockRecord.getLastFailureTime() ) / 1000 )
                        > lockOutTime ) {
                    lockRecord.setFailures ( 0 );
                }
            }
        }
        lockRecord.registerFailure();
    }
    public int getFailureCount() {
        return failureCount;
    }
    public void setFailureCount ( int failureCount ) {
        this.failureCount = failureCount;
    }
    public int getLockOutTime() {
        return lockOutTime;
    }
    @Override
    protected String getName() {
        return name;
    }
    public void setLockOutTime ( int lockOutTime ) {
        this.lockOutTime = lockOutTime;
    }
    public int getCacheSize() {
        return cacheSize;
    }
    public void setCacheSize ( int cacheSize ) {
        this.cacheSize = cacheSize;
    }
    public int getCacheRemovalWarningTime() {
        return cacheRemovalWarningTime;
    }
    public void setCacheRemovalWarningTime ( int cacheRemovalWarningTime ) {
        this.cacheRemovalWarningTime = cacheRemovalWarningTime;
    }
    protected static class LockRecord {
        private final AtomicInteger failures = new AtomicInteger ( 0 );
        private long lastFailureTime = 0;
        public int getFailures() {
            return failures.get();
        }
        public void setFailures ( int theFailures ) {
            failures.set ( theFailures );
        }
        public long getLastFailureTime() {
            return lastFailureTime;
        }
        public void registerFailure() {
            failures.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();
        }
    }
}
