// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import javax.sql.XADataSource;

public class XADataSource extends DataSource implements javax.sql.XADataSource
{
    public XADataSource() {
    }
    
    public XADataSource(final PoolConfiguration poolProperties) {
        super(poolProperties);
    }
}
