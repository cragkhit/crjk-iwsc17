package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
public class DriverManagerConnectionFactory implements ConnectionFactory {
    static {
        DriverManager.getDrivers();
    }
    public DriverManagerConnectionFactory ( final String connectUri ) {
        _connectUri = connectUri;
        _props = new Properties();
    }
    public DriverManagerConnectionFactory ( final String connectUri, final Properties props ) {
        _connectUri = connectUri;
        _props = props;
    }
    public DriverManagerConnectionFactory ( final String connectUri, final String uname, final String passwd ) {
        _connectUri = connectUri;
        _uname = uname;
        _passwd = passwd;
    }
    @Override
    public Connection createConnection() throws SQLException {
        if ( null == _props ) {
            if ( _uname == null && _passwd == null ) {
                return DriverManager.getConnection ( _connectUri );
            }
            return DriverManager.getConnection ( _connectUri, _uname, _passwd );
        }
        return DriverManager.getConnection ( _connectUri, _props );
    }
    private String _connectUri = null;
    private String _uname = null;
    private String _passwd = null;
    private Properties _props = null;
}
