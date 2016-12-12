package org.apache.tomcat.dbcp.dbcp2;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import java.sql.Connection;
private class PoolGuardConnectionWrapper extends DelegatingConnection<Connection> {
    private final ObjectPool<? extends Connection> pool;
    PoolGuardConnectionWrapper ( final ObjectPool<? extends Connection> pool, final Connection delegate ) {
        super ( delegate );
        this.pool = pool;
    }
    @Override
    public Connection getDelegate() {
        if ( PoolingDriver.this.isAccessToUnderlyingConnectionAllowed() ) {
            return super.getDelegate();
        }
        return null;
    }
    @Override
    public Connection getInnermostDelegate() {
        if ( PoolingDriver.this.isAccessToUnderlyingConnectionAllowed() ) {
            return super.getInnermostDelegate();
        }
        return null;
    }
}
