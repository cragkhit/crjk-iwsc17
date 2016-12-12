// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.ref.WeakReference;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import java.util.Map;
import java.sql.Statement;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.apache.juli.logging.Log;

public class StatementFinalizer extends AbstractCreateStatementInterceptor
{
    private static final Log log;
    protected List<StatementEntry> statements;
    private boolean logCreationStack;
    
    public StatementFinalizer() {
        this.statements = new LinkedList<StatementEntry>();
        this.logCreationStack = false;
    }
    
    @Override
    public Object createStatement(final Object proxy, final Method method, final Object[] args, final Object statement, final long time) {
        try {
            if (statement instanceof Statement) {
                this.statements.add(new StatementEntry((Statement)statement));
            }
        }
        catch (ClassCastException ex) {}
        return statement;
    }
    
    @Override
    public void closeInvoked() {
        while (this.statements.size() > 0) {
            final StatementEntry ws = this.statements.remove(0);
            final Statement st = ws.getStatement();
            boolean shallClose = false;
            try {
                shallClose = (st != null && !st.isClosed());
                if (!shallClose) {
                    continue;
                }
                st.close();
            }
            catch (Exception ignore) {
                if (!StatementFinalizer.log.isDebugEnabled()) {
                    continue;
                }
                StatementFinalizer.log.debug((Object)"Unable to closed statement upon connection close.", (Throwable)ignore);
            }
            finally {
                if (this.logCreationStack && shallClose) {
                    StatementFinalizer.log.warn((Object)"Statement created, but was not closed at:", ws.getAllocationStack());
                }
            }
        }
    }
    
    @Override
    public void setProperties(final Map<String, PoolProperties.InterceptorProperty> properties) {
        super.setProperties(properties);
        final PoolProperties.InterceptorProperty logProperty = properties.get("trace");
        if (null != logProperty) {
            this.logCreationStack = logProperty.getValueAsBoolean(this.logCreationStack);
        }
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
        this.statements.clear();
        super.reset(parent, con);
    }
    
    static {
        log = LogFactory.getLog((Class)StatementFinalizer.class);
    }
    
    protected class StatementEntry
    {
        private WeakReference<Statement> statement;
        private Throwable allocationStack;
        
        public StatementEntry(final Statement statement) {
            this.statement = new WeakReference<Statement>(statement);
            if (StatementFinalizer.this.logCreationStack) {
                this.allocationStack = new Throwable();
            }
        }
        
        public Statement getStatement() {
            return this.statement.get();
        }
        
        public Throwable getAllocationStack() {
            return this.allocationStack;
        }
    }
}
