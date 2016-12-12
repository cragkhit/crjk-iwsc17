// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.sql.Statement;
import java.lang.ref.WeakReference;

protected class StatementEntry
{
    private WeakReference<Statement> statement;
    private Throwable allocationStack;
    
    public StatementEntry(final Statement statement) {
        this.statement = new WeakReference<Statement>(statement);
        if (StatementFinalizer.access$000(StatementFinalizer.this)) {
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
