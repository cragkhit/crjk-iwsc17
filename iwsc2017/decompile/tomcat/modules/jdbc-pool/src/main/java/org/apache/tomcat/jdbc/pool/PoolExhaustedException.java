// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.sql.SQLException;

public class PoolExhaustedException extends SQLException
{
    private static final long serialVersionUID = 3501536931777262475L;
    
    public PoolExhaustedException() {
    }
    
    public PoolExhaustedException(final String reason) {
        super(reason);
    }
    
    public PoolExhaustedException(final Throwable cause) {
        super(cause);
    }
    
    public PoolExhaustedException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public PoolExhaustedException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
    
    public PoolExhaustedException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public PoolExhaustedException(final String reason, final String sqlState, final Throwable cause) {
        super(reason, sqlState, cause);
    }
    
    public PoolExhaustedException(final String reason, final String sqlState, final int vendorCode, final Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }
}
