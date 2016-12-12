// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.lang.reflect.Method;
import java.util.Map;
import java.lang.reflect.InvocationHandler;

public abstract class JdbcInterceptor implements InvocationHandler
{
    public static final String CLOSE_VAL = "close";
    public static final String TOSTRING_VAL = "toString";
    public static final String ISCLOSED_VAL = "isClosed";
    public static final String GETCONNECTION_VAL = "getConnection";
    public static final String UNWRAP_VAL = "unwrap";
    public static final String ISWRAPPERFOR_VAL = "isWrapperFor";
    public static final String ISVALID_VAL = "isValid";
    public static final String EQUALS_VAL = "equals";
    public static final String HASHCODE_VAL = "hashCode";
    protected Map<String, PoolProperties.InterceptorProperty> properties;
    private volatile JdbcInterceptor next;
    private boolean useEquals;
    
    public JdbcInterceptor() {
        this.properties = null;
        this.next = null;
        this.useEquals = true;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (this.getNext() != null) {
            return this.getNext().invoke(proxy, method, args);
        }
        throw new NullPointerException();
    }
    
    public JdbcInterceptor getNext() {
        return this.next;
    }
    
    public void setNext(final JdbcInterceptor next) {
        this.next = next;
    }
    
    public boolean compare(final String name1, final String name2) {
        if (this.isUseEquals()) {
            return name1.equals(name2);
        }
        return name1 == name2;
    }
    
    public boolean compare(final String methodName, final Method method) {
        return this.compare(methodName, method.getName());
    }
    
    public abstract void reset(final ConnectionPool p0, final PooledConnection p1);
    
    public void disconnected(final ConnectionPool parent, final PooledConnection con, final boolean finalizing) {
    }
    
    public Map<String, PoolProperties.InterceptorProperty> getProperties() {
        return this.properties;
    }
    
    public void setProperties(final Map<String, PoolProperties.InterceptorProperty> properties) {
        this.properties = properties;
        final String useEquals = "useEquals";
        final PoolProperties.InterceptorProperty p = properties.get("useEquals");
        if (p != null) {
            this.setUseEquals(Boolean.parseBoolean(p.getValue()));
        }
    }
    
    public boolean isUseEquals() {
        return this.useEquals;
    }
    
    public void setUseEquals(final boolean useEquals) {
        this.useEquals = useEquals;
    }
    
    public void poolClosed(final ConnectionPool pool) {
    }
    
    public void poolStarted(final ConnectionPool pool) {
    }
}
