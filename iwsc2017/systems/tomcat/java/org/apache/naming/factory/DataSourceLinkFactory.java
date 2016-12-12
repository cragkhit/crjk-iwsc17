package org.apache.naming.factory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.sql.DataSource;
public class DataSourceLinkFactory extends ResourceLinkFactory {
    public static void setGlobalContext ( Context newGlobalContext ) {
        ResourceLinkFactory.setGlobalContext ( newGlobalContext );
    }
    @Override
    public Object getObjectInstance ( Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment )
    throws NamingException {
        Object result = super.getObjectInstance ( obj, name, nameCtx, environment );
        if ( result != null ) {
            Reference ref = ( Reference ) obj;
            RefAddr userAttr = ref.get ( "username" );
            RefAddr passAttr = ref.get ( "password" );
            if ( userAttr.getContent() != null && passAttr.getContent() != null ) {
                result = wrapDataSource ( result, userAttr.getContent().toString(), passAttr.getContent().toString() );
            }
        }
        return result;
    }
    protected Object wrapDataSource ( Object datasource, String username, String password ) throws NamingException {
        try {
            Class<?> proxyClass = Proxy.getProxyClass ( datasource.getClass().getClassLoader(), datasource.getClass().getInterfaces() );
            Constructor<?> proxyConstructor = proxyClass.getConstructor ( new Class[] { InvocationHandler.class } );
            DataSourceHandler handler = new DataSourceHandler ( ( DataSource ) datasource, username, password );
            return proxyConstructor.newInstance ( handler );
        } catch ( Exception x ) {
            if ( x instanceof InvocationTargetException ) {
                Throwable cause = x.getCause();
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
            } else {
                NamingException nx = new NamingException ( x.getMessage() );
                nx.initCause ( x );
                throw nx;
            }
        }
    }
    public static class DataSourceHandler implements InvocationHandler {
        private final DataSource ds;
        private final String username;
        private final String password;
        private final Method getConnection;
        public DataSourceHandler ( DataSource ds, String username, String password ) throws Exception {
            this.ds = ds;
            this.username = username;
            this.password = password;
            getConnection = ds.getClass().getMethod ( "getConnection", String.class, String.class );
        }
        @Override
        public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
            if ( "getConnection".equals ( method.getName() ) && ( args == null || args.length == 0 ) ) {
                args = new String[] {username, password};
                method = getConnection;
            } else if ( "unwrap".equals ( method.getName() ) ) {
                return unwrap ( ( Class<?> ) args[0] );
            }
            try {
                return method.invoke ( ds, args );
            } catch ( Throwable t ) {
                if ( t instanceof InvocationTargetException
                        && t.getCause() != null ) {
                    throw t.getCause();
                } else {
                    throw t;
                }
            }
        }
        public Object unwrap ( Class<?> iface ) throws SQLException {
            if ( iface == DataSource.class ) {
                return ds;
            } else {
                throw new SQLException ( "Not a wrapper of " + iface.getName() );
            }
        }
    }
}
