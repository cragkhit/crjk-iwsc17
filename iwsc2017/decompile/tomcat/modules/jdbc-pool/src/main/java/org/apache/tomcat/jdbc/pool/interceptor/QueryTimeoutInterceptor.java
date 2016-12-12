// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import org.apache.juli.logging.LogFactory;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.Method;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.util.Map;
import org.apache.juli.logging.Log;

public class QueryTimeoutInterceptor extends AbstractCreateStatementInterceptor
{
    private static Log log;
    int timeout;
    
    public QueryTimeoutInterceptor() {
        this.timeout = 1;
    }
    
    @Override
    public void setProperties(final Map<String, PoolProperties.InterceptorProperty> properties) {
        super.setProperties(properties);
        final PoolProperties.InterceptorProperty p = properties.get("queryTimeout");
        if (p != null) {
            this.timeout = p.getValueAsInt(this.timeout);
        }
    }
    
    @Override
    public Object createStatement(final Object proxy, final Method method, final Object[] args, final Object statement, final long time) {
        if (statement instanceof Statement && this.timeout > 0) {
            final Statement s = (Statement)statement;
            try {
                s.setQueryTimeout(this.timeout);
            }
            catch (SQLException x) {
                QueryTimeoutInterceptor.log.warn((Object)("[QueryTimeoutInterceptor] Unable to set query timeout:" + x.getMessage()), (Throwable)x);
            }
        }
        return statement;
    }
    
    @Override
    public void closeInvoked() {
    }
    
    static {
        QueryTimeoutInterceptor.log = LogFactory.getLog((Class)QueryTimeoutInterceptor.class);
    }
}
