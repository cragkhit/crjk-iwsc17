package org.apache.tomcat.jdbc.pool;
public class XADataSource extends DataSource implements javax.sql.XADataSource {
    public XADataSource() {
        super();
    }
    public XADataSource ( PoolConfiguration poolProperties ) {
        super ( poolProperties );
    }
}
