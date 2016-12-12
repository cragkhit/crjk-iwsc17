package org.apache.tomcat.dbcp.dbcp2;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import javax.management.InstanceNotFoundException;
import java.sql.SQLException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import java.util.Collection;
import java.sql.PreparedStatement;
import javax.management.ObjectName;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
import javax.management.MBeanServer;
import java.sql.Connection;
public class PoolableConnection extends DelegatingConnection<Connection> implements PoolableConnectionMXBean {
    private static MBeanServer MBEAN_SERVER;
    private final ObjectPool<PoolableConnection> _pool;
    private final ObjectName _jmxName;
    private PreparedStatement validationPreparedStatement;
    private String lastValidationSql;
    private boolean _fatalSqlExceptionThrown;
    private final Collection<String> _disconnectionSqlCodes;
    private final boolean _fastFailValidation;
    public PoolableConnection ( final Connection conn, final ObjectPool<PoolableConnection> pool, final ObjectName jmxName, final Collection<String> disconnectSqlCodes, final boolean fastFailValidation ) {
        super ( conn );
        this.validationPreparedStatement = null;
        this.lastValidationSql = null;
        this._fatalSqlExceptionThrown = false;
        this._pool = pool;
        this._jmxName = jmxName;
        this._disconnectionSqlCodes = disconnectSqlCodes;
        this._fastFailValidation = fastFailValidation;
        if ( jmxName != null ) {
            try {
                PoolableConnection.MBEAN_SERVER.registerMBean ( this, jmxName );
            } catch ( InstanceAlreadyExistsException ) {}
            catch ( MBeanRegistrationException ) {}
            catch ( NotCompliantMBeanException ex ) {}
        }
    }
    public PoolableConnection ( final Connection conn, final ObjectPool<PoolableConnection> pool, final ObjectName jmxName ) {
        this ( conn, pool, jmxName, null, false );
    }
    @Override
    protected void passivate() throws SQLException {
        super.passivate();
        this.setClosedInternal ( true );
    }
    @Override
    public boolean isClosed() throws SQLException {
        if ( this.isClosedInternal() ) {
            return true;
        }
        if ( this.getDelegateInternal().isClosed() ) {
            this.close();
            return true;
        }
        return false;
    }
    @Override
    public synchronized void close() throws SQLException {
        if ( this.isClosedInternal() ) {
            return;
        }
        boolean isUnderlyingConectionClosed;
        try {
            isUnderlyingConectionClosed = this.getDelegateInternal().isClosed();
        } catch ( SQLException e ) {
            try {
                this._pool.invalidateObject ( this );
            } catch ( IllegalStateException ise ) {
                this.passivate();
                this.getInnermostDelegate().close();
            } catch ( Exception ex ) {}
            throw new SQLException ( "Cannot close connection (isClosed check failed)", e );
        }
        if ( isUnderlyingConectionClosed ) {
            try {
                this._pool.invalidateObject ( this );
                return;
            } catch ( IllegalStateException e4 ) {
                this.passivate();
                this.getInnermostDelegate().close();
                return;
            } catch ( Exception e2 ) {
                throw new SQLException ( "Cannot close connection (invalidating pooled object failed)", e2 );
            }
        }
        try {
            this._pool.returnObject ( this );
        } catch ( IllegalStateException e4 ) {
            this.passivate();
            this.getInnermostDelegate().close();
        } catch ( SQLException e ) {
            throw e;
        } catch ( RuntimeException e3 ) {
            throw e3;
        } catch ( Exception e2 ) {
            throw new SQLException ( "Cannot close connection (return to pool failed)", e2 );
        }
    }
    @Override
    public void reallyClose() throws SQLException {
        if ( this._jmxName != null ) {
            try {
                PoolableConnection.MBEAN_SERVER.unregisterMBean ( this._jmxName );
            } catch ( MBeanRegistrationException ) {}
            catch ( InstanceNotFoundException ex ) {}
        }
        if ( this.validationPreparedStatement != null ) {
            try {
                this.validationPreparedStatement.close();
            } catch ( SQLException ex2 ) {}
        }
        super.closeInternal();
    }
    @Override
    public String getToString() {
        return this.toString();
    }
    public void validate ( final String sql, int timeout ) throws SQLException {
        if ( this._fastFailValidation && this._fatalSqlExceptionThrown ) {
            throw new SQLException ( Utils.getMessage ( "poolableConnection.validate.fastFail" ) );
        }
        if ( sql != null && sql.length() != 0 ) {
            if ( !sql.equals ( this.lastValidationSql ) ) {
                this.lastValidationSql = sql;
                this.validationPreparedStatement = this.getInnermostDelegateInternal().prepareStatement ( sql );
            }
            if ( timeout > 0 ) {
                this.validationPreparedStatement.setQueryTimeout ( timeout );
            }
            try ( final ResultSet rs = this.validationPreparedStatement.executeQuery() ) {
                if ( !rs.next() ) {
                    throw new SQLException ( "validationQuery didn't return a row" );
                }
            } catch ( SQLException sqle ) {
                throw sqle;
            }
            return;
        }
        if ( timeout < 0 ) {
            timeout = 0;
        }
        if ( !this.isValid ( timeout ) ) {
            throw new SQLException ( "isValid() returned false" );
        }
    }
    private boolean isDisconnectionSqlException ( final SQLException e ) {
        boolean fatalException = false;
        final String sqlState = e.getSQLState();
        if ( sqlState != null ) {
            fatalException = ( ( this._disconnectionSqlCodes == null ) ? ( sqlState.startsWith ( "08" ) || Utils.DISCONNECTION_SQL_CODES.contains ( sqlState ) ) : this._disconnectionSqlCodes.contains ( sqlState ) );
            if ( !fatalException && e.getNextException() != null ) {
                fatalException = this.isDisconnectionSqlException ( e.getNextException() );
            }
        }
        return fatalException;
    }
    @Override
    protected void handleException ( final SQLException e ) throws SQLException {
        this._fatalSqlExceptionThrown |= this.isDisconnectionSqlException ( e );
        super.handleException ( e );
    }
    static {
        PoolableConnection.MBEAN_SERVER = null;
        try {
            PoolableConnection.MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
        } catch ( NoClassDefFoundError ) {}
        catch ( Exception ex ) {}
    }
}
