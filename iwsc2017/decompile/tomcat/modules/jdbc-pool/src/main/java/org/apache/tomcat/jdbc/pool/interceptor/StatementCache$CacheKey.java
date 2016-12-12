// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.util.Arrays;

private static final class CacheKey
{
    private final String stmtType;
    private final Object[] args;
    
    private CacheKey(final String type, final Object[] methodArgs) {
        this.stmtType = type;
        this.args = methodArgs;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + Arrays.hashCode(this.args);
        result = 31 * result + ((this.stmtType == null) ? 0 : this.stmtType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final CacheKey other = (CacheKey)obj;
        if (!Arrays.equals(this.args, other.args)) {
            return false;
        }
        if (this.stmtType == null) {
            if (other.stmtType != null) {
                return false;
            }
        }
        else if (!this.stmtType.equals(other.stmtType)) {
            return false;
        }
        return true;
    }
}
