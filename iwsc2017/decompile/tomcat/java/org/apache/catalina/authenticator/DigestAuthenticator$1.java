package org.apache.catalina.authenticator;
import java.util.Map;
import java.util.LinkedHashMap;
class DigestAuthenticator$1 extends LinkedHashMap<String, NonceInfo> {
    private static final long serialVersionUID = 1L;
    private static final long LOG_SUPPRESS_TIME = 300000L;
    private long lastLog = 0L;
    @Override
    protected boolean removeEldestEntry ( final Map.Entry<String, NonceInfo> eldest ) {
        final long currentTime = System.currentTimeMillis();
        if ( this.size() > DigestAuthenticator.this.getNonceCacheSize() ) {
            if ( this.lastLog < currentTime && currentTime - eldest.getValue().getTimestamp() < DigestAuthenticator.this.getNonceValidity() ) {
                DigestAuthenticator.access$000().warn ( AuthenticatorBase.sm.getString ( "digestAuthenticator.cacheRemove" ) );
                this.lastLog = currentTime + 300000L;
            }
            return true;
        }
        return false;
    }
}
