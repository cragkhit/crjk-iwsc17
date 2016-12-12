package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLException;
import java.sql.Connection;
private class PoolGuardConnectionWrapper<D extends Connection> extends DelegatingConnection<D> {
    PoolGuardConnectionWrapper ( final D delegate ) {
        super ( delegate );
    }
    @Override
    public D getDelegate() {
        if ( PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ) {
            return super.getDelegate();
        }
        return null;
    }
    @Override
    public Connection getInnermostDelegate() {
        if ( PoolingDataSource.this.isAccessToUnderlyingConnectionAllowed() ) {
            return super.getInnermostDelegate();
        }
        return null;
    }
    @Override
    public void close() throws SQLException {
        if ( this.getDelegateInternal() != null ) {
            super.close();
            super.setDelegate ( null );
        }
    }
    @Override
    public boolean isClosed() throws SQLException {
        return this.getDelegateInternal() == null || super.isClosed();
    }
}
