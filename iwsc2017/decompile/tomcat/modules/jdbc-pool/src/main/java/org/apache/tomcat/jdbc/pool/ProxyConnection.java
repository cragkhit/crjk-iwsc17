// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import javax.sql.XAConnection;

public class ProxyConnection extends JdbcInterceptor
{
    protected PooledConnection connection;
    protected ConnectionPool pool;
    
    public PooledConnection getConnection() {
        return this.connection;
    }
    
    public void setConnection(final PooledConnection connection) {
        this.connection = connection;
    }
    
    public ConnectionPool getPool() {
        return this.pool;
    }
    
    public void setPool(final ConnectionPool pool) {
        this.pool = pool;
    }
    
    protected ProxyConnection(final ConnectionPool parent, final PooledConnection con, final boolean useEquals) {
        this.connection = null;
        this.pool = null;
        this.pool = parent;
        this.connection = con;
        this.setUseEquals(useEquals);
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
        this.pool = parent;
        this.connection = con;
    }
    
    public boolean isWrapperFor(final Class<?> iface) {
        return (iface == XAConnection.class && this.connection.getXAConnection() != null) || iface.isInstance(this.connection.getConnection());
    }
    
    public Object unwrap(final Class<?> iface) throws SQLException {
        if (iface == PooledConnection.class) {
            return this.connection;
        }
        if (iface == XAConnection.class) {
            return this.connection.getXAConnection();
        }
        if (this.isWrapperFor(iface)) {
            return this.connection.getConnection();
        }
        throw new SQLException("Not a wrapper of " + iface.getName());
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (this.compare("isClosed", method)) {
            return this.isClosed();
        }
        if (this.compare("close", method)) {
            if (this.connection == null) {
                return null;
            }
            final PooledConnection poolc = this.connection;
            this.connection = null;
            this.pool.returnConnection(poolc);
            return null;
        }
        else {
            if (this.compare("toString", method)) {
                return this.toString();
            }
            if (this.compare("getConnection", method) && this.connection != null) {
                return this.connection.getConnection();
            }
            if (method.getDeclaringClass().equals(XAConnection.class)) {
                try {
                    return method.invoke(this.connection.getXAConnection(), args);
                }
                catch (Throwable t) {
                    if (t instanceof InvocationTargetException) {
                        throw (t.getCause() != null) ? t.getCause() : t;
                    }
                    throw t;
                }
            }
            if (this.isClosed()) {
                throw new SQLException("Connection has already been closed.");
            }
            if (this.compare("unwrap", method)) {
                return this.unwrap((Class<?>)args[0]);
            }
            if (this.compare("isWrapperFor", method)) {
                return this.isWrapperFor((Class<?>)args[0]);
            }
            try {
                final PooledConnection poolc = this.connection;
                if (poolc != null) {
                    return method.invoke(poolc.getConnection(), args);
                }
                throw new SQLException("Connection has already been closed.");
            }
            catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    throw (t.getCause() != null) ? t.getCause() : t;
                }
                throw t;
            }
        }
    }
    
    public boolean isClosed() {
        return this.connection == null || this.connection.isDiscarded();
    }
    
    public PooledConnection getDelegateConnection() {
        return this.connection;
    }
    
    public ConnectionPool getParentPool() {
        return this.pool;
    }
    
    @Override
    public String toString() {
        return "ProxyConnection[" + ((this.connection != null) ? this.connection.toString() : "null") + "]";
    }
}
