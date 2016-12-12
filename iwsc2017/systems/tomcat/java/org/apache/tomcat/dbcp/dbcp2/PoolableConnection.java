package org.apache.tomcat.dbcp.dbcp2;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.tomcat.dbcp.pool2.ObjectPool;
public class PoolableConnection extends DelegatingConnection<Connection>
    implements PoolableConnectionMXBean {
    private static MBeanServer MBEAN_SERVER = null;
    static {
        try {
            MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
        } catch ( NoClassDefFoundError | Exception ex ) {
        }
    }
    private final ObjectPool<PoolableConnection> _pool;
    private final ObjectName _jmxName;
    private PreparedStatement validationPreparedStatement = null;
    private String lastValidationSql = null;
    private boolean _fatalSqlExceptionThrown = false;
    private final Collection<String> _disconnectionSqlCodes;
    private final boolean _fastFailValidation;
    public PoolableConnection ( final Connection conn,
                                final ObjectPool<PoolableConnection> pool, final ObjectName jmxName, final Collection<String> disconnectSqlCodes,
                                final boolean fastFailValidation ) {
        super ( conn );
        _pool = pool;
        _jmxName = jmxName;
        _disconnectionSqlCodes = disconnectSqlCodes;
        _fastFailValidation = fastFailValidation;
        if ( jmxName != null ) {
            try {
                MBEAN_SERVER.registerMBean ( this, jmxName );
            } catch ( InstanceAlreadyExistsException |
                          MBeanRegistrationException | NotCompliantMBeanException e ) {
            }
        }
    }
    public PoolableConnection ( final Connection conn,
                                final ObjectPool<PoolableConnection> pool, final ObjectName jmxName ) {
        this ( conn, pool, jmxName, null, false );
    }
    @Override
    protected void passivate() throws SQLException {
        super.passivate();
        setClosedInternal ( true );
    }
    @Override
    public boolean isClosed() throws SQLException {
        if ( isClosedInternal() ) {
            return true;
        }
        if ( getDelegateInternal().isClosed() ) {
            close();
            return true;
        }
        return false;
    }
    @Override
    public synchronized void close() throws SQLException {
        if ( isClosedInternal() ) {
            return;
        }
        boolean isUnderlyingConectionClosed;
        try {
            isUnderlyingConectionClosed = getDelegateInternal().isClosed();
        } catch ( final SQLException e ) {
            try {
                _pool.invalidateObject ( this );
            } catch ( final IllegalStateException ise ) {
                passivate();
                getInnermostDelegate().close();
            } catch ( final Exception ie ) {
            }
            throw new SQLException ( "Cannot close connection (isClosed check failed)", e );
        }
        if ( isUnderlyingConectionClosed ) {
            try {
                _pool.invalidateObject ( this );
            } catch ( final IllegalStateException e ) {
                passivate();
                getInnermostDelegate().close();
            } catch ( final Exception e ) {
                throw new SQLException ( "Cannot close connection (invalidating pooled object failed)", e );
            }
        } else {
            try {
                _pool.returnObject ( this );
            } catch ( final IllegalStateException e ) {
                passivate();
                getInnermostDelegate().close();
            } catch ( final SQLException e ) {
                throw e;
            } catch ( final RuntimeException e ) {
                throw e;
            } catch ( final Exception e ) {
                throw new SQLException ( "Cannot close connection (return to pool failed)", e );
            }
        }
    }
    @Override
    public void reallyClose() throws SQLException {
        if ( _jmxName != null ) {
            try {
                MBEAN_SERVER.unregisterMBean ( _jmxName );
            } catch ( MBeanRegistrationException | InstanceNotFoundException e ) {
            }
        }
        if ( validationPreparedStatement != null ) {
            try {
                validationPreparedStatement.close();
            } catch ( final SQLException sqle ) {
            }
        }
        super.closeInternal();
    }
    @Override
    public String getToString() {
        return toString();
    }
    public void validate ( final String sql, int timeout ) throws SQLException {
        if ( _fastFailValidation && _fatalSqlExceptionThrown ) {
            throw new SQLException ( Utils.getMessage ( "poolableConnection.validate.fastFail" ) );
        }
        if ( sql == null || sql.length() == 0 ) {
            if ( timeout < 0 ) {
                timeout = 0;
            }
            if ( !isValid ( timeout ) ) {
                throw new SQLException ( "isValid() returned false" );
            }
            return;
        }
        if ( !sql.equals ( lastValidationSql ) ) {
            lastValidationSql = sql;
            validationPreparedStatement =
                getInnermostDelegateInternal().prepareStatement ( sql );
        }
        if ( timeout > 0 ) {
            validationPreparedStatement.setQueryTimeout ( timeout );
        }
        try ( ResultSet rs = validationPreparedStatement.executeQuery() ) {
            if ( !rs.next() ) {
                throw new SQLException ( "validationQuery didn't return a row" );
            }
        } catch ( final SQLException sqle ) {
            throw sqle;
        }
    }
    private boolean isDisconnectionSqlException ( final SQLException e ) {
        boolean fatalException = false;
        final String sqlState = e.getSQLState();
        if ( sqlState != null ) {
            fatalException = _disconnectionSqlCodes == null ? sqlState.startsWith ( Utils.DISCONNECTION_SQL_CODE_PREFIX )
                             || Utils.DISCONNECTION_SQL_CODES.contains ( sqlState ) : _disconnectionSqlCodes.contains ( sqlState );
            if ( !fatalException ) {
                if ( e.getNextException() != null ) {
                    fatalException = isDisconnectionSqlException ( e.getNextException() );
                }
            }
        }
        return fatalException;
    }
    @Override
    protected void handleException ( final SQLException e ) throws SQLException {
        _fatalSqlExceptionThrown |= isDisconnectionSqlException ( e );
        super.handleException ( e );
    }
}
