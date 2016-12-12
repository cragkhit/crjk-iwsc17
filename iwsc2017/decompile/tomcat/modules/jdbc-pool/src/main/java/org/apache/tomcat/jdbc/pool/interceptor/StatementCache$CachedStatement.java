// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.sql.ResultSet;
import java.sql.Statement;

protected class CachedStatement extends StatementProxy<Statement>
{
    boolean cached;
    CacheKey key;
    
    public CachedStatement(final Statement parent, final String sql) {
        super(parent, sql);
        this.cached = false;
    }
    
    @Override
    public void closeInvoked() {
        boolean shouldClose = true;
        if (StatementCache.access$000(StatementCache.this).get() < StatementCache.access$100(StatementCache.this)) {
            final CachedStatement proxy = new CachedStatement(this.getDelegate(), this.getSql());
            proxy.setCacheKey(this.getCacheKey());
            try {
                final ResultSet result = this.getDelegate().getResultSet();
                if (result != null && !result.isClosed()) {
                    result.close();
                }
                final Object actualProxy = this.getConstructor().newInstance(proxy);
                proxy.setActualProxy(actualProxy);
                proxy.setConnection(this.getConnection());
                proxy.setConstructor(this.getConstructor());
                if (StatementCache.this.cacheStatement(proxy)) {
                    proxy.cached = true;
                    shouldClose = false;
                }
            }
            catch (Exception x) {
                StatementCache.this.removeStatement(proxy);
            }
        }
        if (shouldClose) {
            super.closeInvoked();
        }
        this.closed = true;
        this.delegate = null;
    }
    
    public void forceClose() {
        StatementCache.this.removeStatement(this);
        super.closeInvoked();
    }
    
    public CacheKey getCacheKey() {
        return this.key;
    }
    
    public void setCacheKey(final CacheKey cacheKey) {
        this.key = cacheKey;
    }
}
