// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.reflect.Method;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.ProxyConnection;

public class ResetAbandonedTimer extends AbstractQueryReport
{
    public boolean resetTimer() {
        boolean result = false;
        for (JdbcInterceptor interceptor = this.getNext(); interceptor != null && !result; interceptor = interceptor.getNext()) {
            if (interceptor instanceof ProxyConnection) {
                final PooledConnection con = ((ProxyConnection)interceptor).getConnection();
                if (con == null) {
                    break;
                }
                con.setTimestamp(System.currentTimeMillis());
                result = true;
            }
        }
        return result;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result = super.invoke(proxy, method, args);
        this.resetTimer();
        return result;
    }
    
    @Override
    protected void prepareCall(final String query, final long time) {
        this.resetTimer();
    }
    
    @Override
    protected void prepareStatement(final String sql, final long time) {
        this.resetTimer();
    }
    
    @Override
    public void closeInvoked() {
        this.resetTimer();
    }
    
    @Override
    protected String reportQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        this.resetTimer();
        return super.reportQuery(query, args, name, start, delta);
    }
    
    @Override
    protected String reportSlowQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        this.resetTimer();
        return super.reportSlowQuery(query, args, name, start, delta);
    }
}
