package org.apache.tomcat.jdbc.pool;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
public class DisposableConnectionFacade extends JdbcInterceptor {
    protected DisposableConnectionFacade ( JdbcInterceptor interceptor ) {
        setUseEquals ( interceptor.isUseEquals() );
        setNext ( interceptor );
    }
    @Override
    public void reset ( ConnectionPool parent, PooledConnection con ) {
    }
    @Override
    public int hashCode() {
        return System.identityHashCode ( this );
    }
    @Override
    public boolean equals ( Object obj ) {
        return this == obj;
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args )
    throws Throwable {
        if ( compare ( EQUALS_VAL, method ) ) {
            return Boolean.valueOf (
                       this.equals ( Proxy.getInvocationHandler ( args[0] ) ) );
        } else if ( compare ( HASHCODE_VAL, method ) ) {
            return Integer.valueOf ( this.hashCode() );
        } else if ( getNext() == null ) {
            if ( compare ( ISCLOSED_VAL, method ) ) {
                return Boolean.TRUE;
            } else if ( compare ( CLOSE_VAL, method ) ) {
                return null;
            } else if ( compare ( ISVALID_VAL, method ) ) {
                return Boolean.FALSE;
            }
        }
        try {
            return super.invoke ( proxy, method, args );
        } catch ( NullPointerException e ) {
            if ( getNext() == null ) {
                if ( compare ( TOSTRING_VAL, method ) ) {
                    return "DisposableConnectionFacade[null]";
                }
                throw new SQLException (
                    "PooledConnection has already been closed." );
            }
            throw e;
        } finally {
            if ( compare ( CLOSE_VAL, method ) ) {
                setNext ( null );
            }
        }
    }
}
