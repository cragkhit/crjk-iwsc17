package org.apache.tomcat.jdbc.pool;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import javax.sql.XAConnection;
public class ProxyConnection extends JdbcInterceptor {
    protected PooledConnection connection = null;
    protected ConnectionPool pool = null;
    public PooledConnection getConnection() {
        return connection;
    }
    public void setConnection ( PooledConnection connection ) {
        this.connection = connection;
    }
    public ConnectionPool getPool() {
        return pool;
    }
    public void setPool ( ConnectionPool pool ) {
        this.pool = pool;
    }
    protected ProxyConnection ( ConnectionPool parent, PooledConnection con,
                                boolean useEquals ) {
        pool = parent;
        connection = con;
        setUseEquals ( useEquals );
    }
    @Override
    public void reset ( ConnectionPool parent, PooledConnection con ) {
        this.pool = parent;
        this.connection = con;
    }
    public boolean isWrapperFor ( Class<?> iface ) {
        if ( iface == XAConnection.class && connection.getXAConnection() != null ) {
            return true;
        } else {
            return ( iface.isInstance ( connection.getConnection() ) );
        }
    }
    public Object unwrap ( Class<?> iface ) throws SQLException {
        if ( iface == PooledConnection.class ) {
            return connection;
        } else if ( iface == XAConnection.class ) {
            return connection.getXAConnection();
        } else if ( isWrapperFor ( iface ) ) {
            return connection.getConnection();
        } else {
            throw new SQLException ( "Not a wrapper of " + iface.getName() );
        }
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        if ( compare ( ISCLOSED_VAL, method ) ) {
            return Boolean.valueOf ( isClosed() );
        }
        if ( compare ( CLOSE_VAL, method ) ) {
            if ( connection == null ) {
                return null;
            }
            PooledConnection poolc = this.connection;
            this.connection = null;
            pool.returnConnection ( poolc );
            return null;
        } else if ( compare ( TOSTRING_VAL, method ) ) {
            return this.toString();
        } else if ( compare ( GETCONNECTION_VAL, method ) && connection != null ) {
            return connection.getConnection();
        } else if ( method.getDeclaringClass().equals ( XAConnection.class ) ) {
            try {
                return method.invoke ( connection.getXAConnection(), args );
            } catch ( Throwable t ) {
                if ( t instanceof InvocationTargetException ) {
                    throw t.getCause() != null ? t.getCause() : t;
                } else {
                    throw t;
                }
            }
        }
        if ( isClosed() ) {
            throw new SQLException ( "Connection has already been closed." );
        }
        if ( compare ( UNWRAP_VAL, method ) ) {
            return unwrap ( ( Class<?> ) args[0] );
        } else if ( compare ( ISWRAPPERFOR_VAL, method ) ) {
            return Boolean.valueOf ( this.isWrapperFor ( ( Class<?> ) args[0] ) );
        }
        try {
            PooledConnection poolc = connection;
            if ( poolc != null ) {
                return method.invoke ( poolc.getConnection(), args );
            } else {
                throw new SQLException ( "Connection has already been closed." );
            }
        } catch ( Throwable t ) {
            if ( t instanceof InvocationTargetException ) {
                throw t.getCause() != null ? t.getCause() : t;
            } else {
                throw t;
            }
        }
    }
    public boolean isClosed() {
        return connection == null || connection.isDiscarded();
    }
    public PooledConnection getDelegateConnection() {
        return connection;
    }
    public ConnectionPool getParentPool() {
        return pool;
    }
    @Override
    public String toString() {
        return "ProxyConnection[" + ( connection != null ? connection.toString() : "null" ) + "]";
    }
}
