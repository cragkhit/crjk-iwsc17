package org.apache.naming.factory;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
public class DataSourceLinkFactory extends ResourceLinkFactory {
    public static void setGlobalContext ( final Context newGlobalContext ) {
        ResourceLinkFactory.setGlobalContext ( newGlobalContext );
    }
    @Override
    public Object getObjectInstance ( final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment ) throws NamingException {
        Object result = super.getObjectInstance ( obj, name, nameCtx, environment );
        if ( result != null ) {
            final Reference ref = ( Reference ) obj;
            final RefAddr userAttr = ref.get ( "username" );
            final RefAddr passAttr = ref.get ( "password" );
            if ( userAttr.getContent() != null && passAttr.getContent() != null ) {
                result = this.wrapDataSource ( result, userAttr.getContent().toString(), passAttr.getContent().toString() );
            }
        }
        return result;
    }
    protected Object wrapDataSource ( final Object datasource, final String username, final String password ) throws NamingException {
        try {
            final Class<?> proxyClass = Proxy.getProxyClass ( datasource.getClass().getClassLoader(), datasource.getClass().getInterfaces() );
            final Constructor<?> proxyConstructor = proxyClass.getConstructor ( InvocationHandler.class );
            final DataSourceHandler handler = new DataSourceHandler ( ( DataSource ) datasource, username, password );
            return proxyConstructor.newInstance ( handler );
        } catch ( Exception x ) {
            if ( x instanceof InvocationTargetException ) {
                final Throwable cause = x.getCause();
                if ( cause instanceof ThreadDeath ) {
                    throw ( ThreadDeath ) cause;
                }
                if ( cause instanceof VirtualMachineError ) {
                    throw ( VirtualMachineError ) cause;
                }
                if ( cause instanceof Exception ) {
                    x = ( Exception ) cause;
                }
            }
            if ( x instanceof NamingException ) {
                throw ( NamingException ) x;
            }
            final NamingException nx = new NamingException ( x.getMessage() );
            nx.initCause ( x );
            throw nx;
        }
    }
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
}
