// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import org.apache.juli.logging.LogFactory;
import java.lang.reflect.Method;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.juli.logging.Log;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;

public class ConnectionState extends JdbcInterceptor
{
    private static final Log log;
    protected final String[] readState;
    protected final String[] writeState;
    protected Boolean autoCommit;
    protected Integer transactionIsolation;
    protected Boolean readOnly;
    protected String catalog;
    
    public ConnectionState() {
        this.readState = new String[] { "getAutoCommit", "getTransactionIsolation", "isReadOnly", "getCatalog" };
        this.writeState = new String[] { "setAutoCommit", "setTransactionIsolation", "setReadOnly", "setCatalog" };
        this.autoCommit = null;
        this.transactionIsolation = null;
        this.readOnly = null;
        this.catalog = null;
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
        if (parent == null || con == null) {
            this.autoCommit = null;
            this.transactionIsolation = null;
            this.readOnly = null;
            this.catalog = null;
            return;
        }
        final PoolConfiguration poolProperties = parent.getPoolProperties();
        if (poolProperties.getDefaultTransactionIsolation() != -1) {
            try {
                if (this.transactionIsolation == null || this.transactionIsolation != poolProperties.getDefaultTransactionIsolation()) {
                    con.getConnection().setTransactionIsolation(poolProperties.getDefaultTransactionIsolation());
                    this.transactionIsolation = poolProperties.getDefaultTransactionIsolation();
                }
            }
            catch (SQLException x) {
                this.transactionIsolation = null;
                ConnectionState.log.error((Object)"Unable to reset transaction isolation state to connection.", (Throwable)x);
            }
        }
        if (poolProperties.getDefaultReadOnly() != null) {
            try {
                if (this.readOnly == null || this.readOnly != (boolean)poolProperties.getDefaultReadOnly()) {
                    con.getConnection().setReadOnly(poolProperties.getDefaultReadOnly());
                    this.readOnly = poolProperties.getDefaultReadOnly();
                }
            }
            catch (SQLException x) {
                this.readOnly = null;
                ConnectionState.log.error((Object)"Unable to reset readonly state to connection.", (Throwable)x);
            }
        }
        if (poolProperties.getDefaultAutoCommit() != null) {
            try {
                if (this.autoCommit == null || this.autoCommit != (boolean)poolProperties.getDefaultAutoCommit()) {
                    con.getConnection().setAutoCommit(poolProperties.getDefaultAutoCommit());
                    this.autoCommit = poolProperties.getDefaultAutoCommit();
                }
            }
            catch (SQLException x) {
                this.autoCommit = null;
                ConnectionState.log.error((Object)"Unable to reset autocommit state to connection.", (Throwable)x);
            }
        }
        if (poolProperties.getDefaultCatalog() != null) {
            try {
                if (this.catalog == null || !this.catalog.equals(poolProperties.getDefaultCatalog())) {
                    con.getConnection().setCatalog(poolProperties.getDefaultCatalog());
                    this.catalog = poolProperties.getDefaultCatalog();
                }
            }
            catch (SQLException x) {
                this.catalog = null;
                ConnectionState.log.error((Object)"Unable to reset default catalog state to connection.", (Throwable)x);
            }
        }
    }
    
    @Override
    public void disconnected(final ConnectionPool parent, final PooledConnection con, final boolean finalizing) {
        this.autoCommit = null;
        this.transactionIsolation = null;
        this.readOnly = null;
        this.catalog = null;
        super.disconnected(parent, con, finalizing);
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String name = method.getName();
        boolean read = false;
        int index = -1;
        for (int i = 0; !read && i < this.readState.length; ++i) {
            read = this.compare(name, this.readState[i]);
            if (read) {
                index = i;
            }
        }
        boolean write = false;
        for (int j = 0; !write && !read && j < this.writeState.length; ++j) {
            write = this.compare(name, this.writeState[j]);
            if (write) {
                index = j;
            }
        }
        Object result = null;
        if (read) {
            switch (index) {
                case 0: {
                    result = this.autoCommit;
                    break;
                }
                case 1: {
                    result = this.transactionIsolation;
                    break;
                }
                case 2: {
                    result = this.readOnly;
                    break;
                }
                case 3: {
                    result = this.catalog;
                    break;
                }
            }
            if (result != null) {
                return result;
            }
        }
        result = super.invoke(proxy, method, args);
        if (read || write) {
            switch (index) {
                case 0: {
                    this.autoCommit = (Boolean)(read ? result : args[0]);
                    break;
                }
                case 1: {
                    this.transactionIsolation = (Integer)(read ? result : args[0]);
                    break;
                }
                case 2: {
                    this.readOnly = (Boolean)(read ? result : args[0]);
                    break;
                }
                case 3: {
                    this.catalog = (String)(read ? result : args[0]);
                    break;
                }
            }
        }
        return result;
    }
    
    static {
        log = LogFactory.getLog((Class)ConnectionState.class);
    }
}
