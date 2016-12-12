package org.apache.tomcat.jdbc.pool.interceptor;
import java.lang.reflect.Method;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.pool.ProxyConnection;
public class ResetAbandonedTimer extends AbstractQueryReport {
    public ResetAbandonedTimer() {
    }
    public boolean resetTimer() {
        boolean result = false;
        JdbcInterceptor interceptor = this.getNext();
        while ( interceptor != null && result == false ) {
            if ( interceptor instanceof ProxyConnection ) {
                PooledConnection con = ( ( ProxyConnection ) interceptor ).getConnection();
                if ( con != null ) {
                    con.setTimestamp ( System.currentTimeMillis() );
                    result = true;
                } else {
                    break;
                }
            }
            interceptor = interceptor.getNext();
        }
        return result;
    }
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
        Object result = super.invoke ( proxy, method, args );
        resetTimer();
        return result;
    }
    @Override
    protected void prepareCall ( String query, long time ) {
        resetTimer();
    }
    @Override
    protected void prepareStatement ( String sql, long time ) {
        resetTimer();
    }
    @Override
    public void closeInvoked() {
        resetTimer();
    }
    @Override
    protected String reportQuery ( String query, Object[] args, String name, long start, long delta ) {
        resetTimer();
        return super.reportQuery ( query, args, name, start, delta );
    }
    @Override
    protected String reportSlowQuery ( String query, Object[] args, String name, long start, long delta ) {
        resetTimer();
        return super.reportSlowQuery ( query, args, name, start, delta );
    }
}
