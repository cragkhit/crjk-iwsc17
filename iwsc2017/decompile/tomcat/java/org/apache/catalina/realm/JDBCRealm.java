package org.apache.catalina.realm;
import java.util.Hashtable;
import org.apache.catalina.LifecycleException;
import java.util.Properties;
import java.sql.ResultSet;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.Driver;
import java.sql.Connection;
public class JDBCRealm extends RealmBase {
    protected String connectionName;
    protected String connectionPassword;
    protected String connectionURL;
    protected Connection dbConnection;
    protected Driver driver;
    protected String driverName;
    protected static final String name = "JDBCRealm";
    protected PreparedStatement preparedCredentials;
    protected PreparedStatement preparedRoles;
    protected String roleNameCol;
    protected String userCredCol;
    protected String userNameCol;
    protected String userRoleTable;
    protected String userTable;
    public JDBCRealm() {
        this.connectionName = null;
        this.connectionPassword = null;
        this.connectionURL = null;
        this.dbConnection = null;
        this.driver = null;
        this.driverName = null;
        this.preparedCredentials = null;
        this.preparedRoles = null;
        this.roleNameCol = null;
        this.userCredCol = null;
        this.userNameCol = null;
        this.userRoleTable = null;
        this.userTable = null;
    }
    public String getConnectionName() {
        return this.connectionName;
    }
    public void setConnectionName ( final String connectionName ) {
        this.connectionName = connectionName;
    }
    public String getConnectionPassword() {
        return this.connectionPassword;
    }
    public void setConnectionPassword ( final String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public String getConnectionURL() {
        return this.connectionURL;
    }
    public void setConnectionURL ( final String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public String getDriverName() {
        return this.driverName;
    }
    public void setDriverName ( final String driverName ) {
        this.driverName = driverName;
    }
    public String getRoleNameCol() {
        return this.roleNameCol;
    }
    public void setRoleNameCol ( final String roleNameCol ) {
        this.roleNameCol = roleNameCol;
    }
    public String getUserCredCol() {
        return this.userCredCol;
    }
    public void setUserCredCol ( final String userCredCol ) {
        this.userCredCol = userCredCol;
    }
    public String getUserNameCol() {
        return this.userNameCol;
    }
    public void setUserNameCol ( final String userNameCol ) {
        this.userNameCol = userNameCol;
    }
    public String getUserRoleTable() {
        return this.userRoleTable;
    }
    public void setUserRoleTable ( final String userRoleTable ) {
        this.userRoleTable = userRoleTable;
    }
    public String getUserTable() {
        return this.userTable;
    }
    public void setUserTable ( final String userTable ) {
        this.userTable = userTable;
    }
    @Override
    public synchronized Principal authenticate ( final String username, final String credentials ) {
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                this.open();
                final Principal principal = this.authenticate ( this.dbConnection, username, credentials );
                return principal;
            } catch ( SQLException e ) {
                this.containerLog.error ( JDBCRealm.sm.getString ( "jdbcRealm.exception" ), e );
                if ( this.dbConnection != null ) {
                    this.close ( this.dbConnection );
                }
                --numberOfTries;
                continue;
            }
            break;
        }
        return null;
    }
    public synchronized Principal authenticate ( final Connection dbConnection, final String username, final String credentials ) {
        if ( username == null || credentials == null ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( JDBCRealm.sm.getString ( "jdbcRealm.authenticateFailure", username ) );
            }
            return null;
        }
        final String dbCredentials = this.getPassword ( username );
        if ( dbCredentials == null ) {
            this.getCredentialHandler().mutate ( credentials );
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( JDBCRealm.sm.getString ( "jdbcRealm.authenticateFailure", username ) );
            }
            return null;
        }
        final boolean validated = this.getCredentialHandler().matches ( credentials, dbCredentials );
        if ( validated ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( JDBCRealm.sm.getString ( "jdbcRealm.authenticateSuccess", username ) );
            }
            final ArrayList<String> roles = this.getRoles ( username );
            return new GenericPrincipal ( username, credentials, roles );
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( JDBCRealm.sm.getString ( "jdbcRealm.authenticateFailure", username ) );
        }
        return null;
    }
    @Override
    public boolean isAvailable() {
        return this.dbConnection != null;
    }
    protected void close ( final Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            this.preparedCredentials.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedCredentials = null;
        try {
            this.preparedRoles.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedRoles = null;
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            this.containerLog.warn ( JDBCRealm.sm.getString ( "jdbcRealm.close" ), e );
        } finally {
            this.dbConnection = null;
        }
    }
    protected PreparedStatement credentials ( final Connection dbConnection, final String username ) throws SQLException {
        if ( this.preparedCredentials == null ) {
            final StringBuilder sb = new StringBuilder ( "SELECT " );
            sb.append ( this.userCredCol );
            sb.append ( " FROM " );
            sb.append ( this.userTable );
            sb.append ( " WHERE " );
            sb.append ( this.userNameCol );
            sb.append ( " = ?" );
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "credentials query: " + sb.toString() );
            }
            this.preparedCredentials = dbConnection.prepareStatement ( sb.toString() );
        }
        if ( username == null ) {
            this.preparedCredentials.setNull ( 1, 12 );
        } else {
            this.preparedCredentials.setString ( 1, username );
        }
        return this.preparedCredentials;
    }
    @Override
    protected String getName() {
        return "JDBCRealm";
    }
    @Override
    protected synchronized String getPassword ( final String username ) {
        String dbCredentials = null;
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                this.open();
                final PreparedStatement stmt = this.credentials ( this.dbConnection, username );
                try ( final ResultSet rs = stmt.executeQuery() ) {
                    if ( rs.next() ) {
                        dbCredentials = rs.getString ( 1 );
                    }
                    this.dbConnection.commit();
                    if ( dbCredentials != null ) {
                        dbCredentials = dbCredentials.trim();
                    }
                    return dbCredentials;
                }
            } catch ( SQLException e ) {
                this.containerLog.error ( JDBCRealm.sm.getString ( "jdbcRealm.exception" ), e );
                if ( this.dbConnection != null ) {
                    this.close ( this.dbConnection );
                }
                --numberOfTries;
                continue;
            }
            break;
        }
        return null;
    }
    @Override
    protected synchronized Principal getPrincipal ( final String username ) {
        return new GenericPrincipal ( username, this.getPassword ( username ), this.getRoles ( username ) );
    }
    protected ArrayList<String> getRoles ( final String username ) {
        if ( this.allRolesMode != AllRolesMode.STRICT_MODE && !this.isRoleStoreDefined() ) {
            return null;
        }
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                this.open();
                final PreparedStatement stmt = this.roles ( this.dbConnection, username );
                try ( final ResultSet rs = stmt.executeQuery() ) {
                    final ArrayList<String> roleList = new ArrayList<String>();
                    while ( rs.next() ) {
                        final String role = rs.getString ( 1 );
                        if ( null != role ) {
                            roleList.add ( role.trim() );
                        }
                    }
                    return roleList;
                } finally {
                    this.dbConnection.commit();
                }
            } catch ( SQLException e ) {
                this.containerLog.error ( JDBCRealm.sm.getString ( "jdbcRealm.exception" ), e );
                if ( this.dbConnection != null ) {
                    this.close ( this.dbConnection );
                }
                --numberOfTries;
                continue;
            }
            break;
        }
        return null;
    }
    protected Connection open() throws SQLException {
        if ( this.dbConnection != null ) {
            return this.dbConnection;
        }
        if ( this.driver == null ) {
            try {
                final Class<?> clazz = Class.forName ( this.driverName );
                this.driver = ( Driver ) clazz.newInstance();
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                throw new SQLException ( e.getMessage(), e );
            }
        }
        final Properties props = new Properties();
        if ( this.connectionName != null ) {
            ( ( Hashtable<String, String> ) props ).put ( "user", this.connectionName );
        }
        if ( this.connectionPassword != null ) {
            ( ( Hashtable<String, String> ) props ).put ( "password", this.connectionPassword );
        }
        this.dbConnection = this.driver.connect ( this.connectionURL, props );
        if ( this.dbConnection == null ) {
            throw new SQLException ( JDBCRealm.sm.getString ( "jdbcRealm.open.invalidurl", this.driverName, this.connectionURL ) );
        }
        this.dbConnection.setAutoCommit ( false );
        return this.dbConnection;
    }
    protected synchronized PreparedStatement roles ( final Connection dbConnection, final String username ) throws SQLException {
        if ( this.preparedRoles == null ) {
            final StringBuilder sb = new StringBuilder ( "SELECT " );
            sb.append ( this.roleNameCol );
            sb.append ( " FROM " );
            sb.append ( this.userRoleTable );
            sb.append ( " WHERE " );
            sb.append ( this.userNameCol );
            sb.append ( " = ?" );
            this.preparedRoles = dbConnection.prepareStatement ( sb.toString() );
        }
        this.preparedRoles.setString ( 1, username );
        return this.preparedRoles;
    }
    private boolean isRoleStoreDefined() {
        return this.userRoleTable != null || this.roleNameCol != null;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            this.open();
        } catch ( SQLException e ) {
            this.containerLog.error ( JDBCRealm.sm.getString ( "jdbcRealm.open" ), e );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        this.close ( this.dbConnection );
    }
}
