package org.apache.catalina.realm;
import java.util.Map;
import java.util.LinkedHashMap;
class LockOutRealm$1 extends LinkedHashMap<String, LockRecord> {
    private static final long serialVersionUID = 1L;
    @Override
    protected boolean removeEldestEntry ( final Map.Entry<String, LockRecord> eldest ) {
        if ( this.size() > LockOutRealm.this.cacheSize ) {
            final long timeInCache = ( System.currentTimeMillis() - eldest.getValue().getLastFailureTime() ) / 1000L;
            if ( timeInCache < LockOutRealm.this.cacheRemovalWarningTime ) {
                LockOutRealm.access$000().warn ( RealmBase.sm.getString ( "lockOutRealm.removeWarning", eldest.getKey(), timeInCache ) );
            }
            return true;
        }
        return false;
    }
}
