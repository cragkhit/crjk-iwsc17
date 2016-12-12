package org.apache.naming.factory;
import java.sql.SQLException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
public static class DataSourceHandler implements InvocationHandler {
    private final DataSource ds;
    private final String username;
    private final String password;
    private final Method getConnection;
    public DataSourceHandler ( final DataSource ds, final String username, final String password ) throws Exception {
        this.ds = ds;
        this.username = username;
        this.password = password;
        this.getConnection = ds.getClass().getMethod ( "getConnection", String.class, String.class );
    }
    @Override
    public Object invoke ( final Object proxy, Method method, Object[] args ) throws Throwable {
        if ( "getConnection".equals ( method.getName() ) && ( args == null || args.length == 0 ) ) {
            args = new String[] { this.username, this.password };
            method = this.getConnection;
        } else if ( "unwrap".equals ( method.getName() ) ) {
            return this.unwrap ( ( Class<?> ) args[0] );
        }
        try {
            return method.invoke ( this.ds, args );
        } catch ( Throwable t ) {
            if ( t instanceof InvocationTargetException && t.getCause() != null ) {
                throw t.getCause();
            }
            throw t;
        }
    }
    public Object unwrap ( final Class<?> iface ) throws SQLException {
        if ( iface == DataSource.class ) {
            return this.ds;
        }
        throw new SQLException ( "Not a wrapper of " + iface.getName() );
    }
}
