package org.apache.tomcat.dbcp.dbcp2;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import java.sql.Driver;
public class DriverConnectionFactory implements ConnectionFactory {
    private final Driver _driver;
    private final String _connectUri;
    private final Properties _props;
    public DriverConnectionFactory ( final Driver driver, final String connectUri, final Properties props ) {
        this._driver = driver;
        this._connectUri = connectUri;
        this._props = props;
    }
    @Override
    public Connection createConnection() throws SQLException {
        return this._driver.connect ( this._connectUri, this._props );
    }
    @Override
    public String toString() {
        return this.getClass().getName() + " [" + String.valueOf ( this._driver ) + ";" + String.valueOf ( this._connectUri ) + ";" + String.valueOf ( this._props ) + "]";
    }
}
