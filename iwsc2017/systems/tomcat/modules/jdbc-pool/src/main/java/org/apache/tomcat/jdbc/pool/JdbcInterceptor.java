package org.apache.tomcat.jdbc.pool;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.PoolProperties.InterceptorProperty;
public abstract class JdbcInterceptor implements InvocationHandler {
    public static final String CLOSE_VAL = "close";
    public static final String TOSTRING_VAL = "toString";
    public static final String ISCLOSED_VAL = "isClosed";
    public static final String GETCONNECTION_VAL = "getConnection";
    public static final String UNWRAP_VAL = "unwrap";
    public static final String ISWRAPPERFOR_VAL = "isWrapperFor";
    public static final String ISVALID_VAL = "isValid";
    public static final String EQUALS_VAL = "equals";
    public static final String HASHCODE_VAL = "hashCode";
    protected Map<String, InterceptorProperty> properties = null;
    private volatile JdbcInterceptor next = null;
    private boolean useEquals = true;
    public JdbcInterceptor() {
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        if ( getNext() != null ) {
            return getNext().invoke ( proxy, method, args );
        } else {
            throw new NullPointerException();
        }
    }
    public JdbcInterceptor getNext() {
        return next;
    }
    public void setNext ( JdbcInterceptor next ) {
        this.next = next;
    }
    public boolean compare ( String name1, String name2 ) {
        if ( isUseEquals() ) {
            return name1.equals ( name2 );
        } else {
            return name1 == name2;
        }
    }
    public boolean compare ( String methodName, Method method ) {
        return compare ( methodName, method.getName() );
    }
    public abstract void reset ( ConnectionPool parent, PooledConnection con );
    public void disconnected ( ConnectionPool parent, PooledConnection con, boolean finalizing ) {
    }
    public Map<String, InterceptorProperty> getProperties() {
        return properties;
    }
    public void setProperties ( Map<String, InterceptorProperty> properties ) {
        this.properties = properties;
        final String useEquals = "useEquals";
        InterceptorProperty p = properties.get ( useEquals );
        if ( p != null ) {
            setUseEquals ( Boolean.parseBoolean ( p.getValue() ) );
        }
    }
    public boolean isUseEquals() {
        return useEquals;
    }
    public void setUseEquals ( boolean useEquals ) {
        this.useEquals = useEquals;
    }
    public void poolClosed ( ConnectionPool pool ) {
    }
    public void poolStarted ( ConnectionPool pool ) {
    }
}
