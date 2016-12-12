package org.apache.catalina.realm;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.ExceptionUtils;
public class JDBCRealm
    extends RealmBase {
    protected String connectionName = null;
    protected String connectionPassword = null;
    protected String connectionURL = null;
    protected Connection dbConnection = null;
    protected Driver driver = null;
    protected String driverName = null;
    protected static final String name = "JDBCRealm";
    protected PreparedStatement preparedCredentials = null;
    protected PreparedStatement preparedRoles = null;
    protected String roleNameCol = null;
    protected String userCredCol = null;
    protected String userNameCol = null;
    protected String userRoleTable = null;
    protected String userTable = null;
    public String getConnectionName() {
        return connectionName;
    }
    public void setConnectionName ( String connectionName ) {
        this.connectionName = connectionName;
    }
    public String getConnectionPassword() {
        return connectionPassword;
    }
    public void setConnectionPassword ( String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public String getConnectionURL() {
        return connectionURL;
    }
    public void setConnectionURL ( String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public String getDriverName() {
        return driverName;
    }
    public void setDriverName ( String driverName ) {
        this.driverName = driverName;
    }
    public String getRoleNameCol() {
        return roleNameCol;
    }
    public void setRoleNameCol ( String roleNameCol ) {
        this.roleNameCol = roleNameCol;
    }
    public String getUserCredCol() {
        return userCredCol;
    }
    public void setUserCredCol ( String userCredCol ) {
        this.userCredCol = userCredCol;
    }
    public String getUserNameCol() {
        return userNameCol;
    }
    public void setUserNameCol ( String userNameCol ) {
        this.userNameCol = userNameCol;
    }
    public String getUserRoleTable() {
        return userRoleTable;
    }
    public void setUserRoleTable ( String userRoleTable ) {
        this.userRoleTable = userRoleTable;
    }
    public String getUserTable() {
        return userTable;
    }
    public void setUserTable ( String userTable ) {
        this.userTable = userTable;
    }
    @Override
    public synchronized Principal authenticate ( String username, String credentials ) {
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                open();
                Principal principal = authenticate ( dbConnection,
                                                     username, credentials );
                return ( principal );
            } catch ( SQLException e ) {
                containerLog.error ( sm.getString ( "jdbcRealm.exception" ), e );
                if ( dbConnection != null ) {
                    close ( dbConnection );
                }
            }
            numberOfTries--;
        }
        return null;
    }
    public synchronized Principal authenticate ( Connection dbConnection,
            String username,
            String credentials ) {
        if ( username == null || credentials == null ) {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "jdbcRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        String dbCredentials = getPassword ( username );
        if ( dbCredentials == null ) {
            getCredentialHandler().mutate ( credentials );
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "jdbcRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        boolean validated = getCredentialHandler().matches ( credentials, dbCredentials );
        if ( validated ) {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "jdbcRealm.authenticateSuccess",
                                                    username ) );
        } else {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "jdbcRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        ArrayList<String> roles = getRoles ( username );
        return ( new GenericPrincipal ( username, credentials, roles ) );
    }
    @Override
    public boolean isAvailable() {
        return ( dbConnection != null );
    }
    protected void close ( Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            preparedCredentials.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedCredentials = null;
        try {
            preparedRoles.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedRoles = null;
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            containerLog.warn ( sm.getString ( "jdbcRealm.close" ), e );
        } finally {
            this.dbConnection = null;
        }
    }
    protected PreparedStatement credentials ( Connection dbConnection,
            String username )
    throws SQLException {
        if ( preparedCredentials == null ) {
            StringBuilder sb = new StringBuilder ( "SELECT " );
            sb.append ( userCredCol );
            sb.append ( " FROM " );
            sb.append ( userTable );
            sb.append ( " WHERE " );
            sb.append ( userNameCol );
            sb.append ( " = ?" );
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "credentials query: " + sb.toString() );
            }
            preparedCredentials =
                dbConnection.prepareStatement ( sb.toString() );
        }
        if ( username == null ) {
            preparedCredentials.setNull ( 1, java.sql.Types.VARCHAR );
        } else {
            preparedCredentials.setString ( 1, username );
        }
        return ( preparedCredentials );
    }
    @Override
    protected String getName() {
        return ( name );
    }
    @Override
    protected synchronized String getPassword ( String username ) {
        String dbCredentials = null;
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                open();
                PreparedStatement stmt = credentials ( dbConnection, username );
                try ( ResultSet rs = stmt.executeQuery() ) {
                    if ( rs.next() ) {
                        dbCredentials = rs.getString ( 1 );
                    }
                    dbConnection.commit();
                    if ( dbCredentials != null ) {
                        dbCredentials = dbCredentials.trim();
                    }
                    return dbCredentials;
                }
            } catch ( SQLException e ) {
                containerLog.error ( sm.getString ( "jdbcRealm.exception" ), e );
            }
            if ( dbConnection != null ) {
                close ( dbConnection );
            }
            numberOfTries--;
        }
        return null;
    }
    @Override
    protected synchronized Principal getPrincipal ( String username ) {
        return ( new GenericPrincipal ( username,
                                        getPassword ( username ),
                                        getRoles ( username ) ) );
    }
    protected ArrayList<String> getRoles ( String username ) {
        if ( allRolesMode != AllRolesMode.STRICT_MODE && !isRoleStoreDefined() ) {
            return null;
        }
        int numberOfTries = 2;
        while ( numberOfTries > 0 ) {
            try {
                open();
                PreparedStatement stmt = roles ( dbConnection, username );
                try ( ResultSet rs = stmt.executeQuery() ) {
                    ArrayList<String> roleList = new ArrayList<>();
                    while ( rs.next() ) {
                        String role = rs.getString ( 1 );
                        if ( null != role ) {
                            roleList.add ( role.trim() );
                        }
                    }
                    return roleList;
                } finally {
                    dbConnection.commit();
                }
            } catch ( SQLException e ) {
                containerLog.error ( sm.getString ( "jdbcRealm.exception" ), e );
                if ( dbConnection != null ) {
                    close ( dbConnection );
                }
            }
            numberOfTries--;
        }
        return null;
    }
    protected Connection open() throws SQLException {
        if ( dbConnection != null ) {
            return ( dbConnection );
        }
        if ( driver == null ) {
            try {
                Class<?> clazz = Class.forName ( driverName );
                driver = ( Driver ) clazz.newInstance();
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                throw new SQLException ( e.getMessage(), e );
            }
        }
        Properties props = new Properties();
        if ( connectionName != null ) {
            props.put ( "user", connectionName );
        }
        if ( connectionPassword != null ) {
            props.put ( "password", connectionPassword );
        }
        dbConnection = driver.connect ( connectionURL, props );
        if ( dbConnection == null ) {
            throw new SQLException ( sm.getString (
                                         "jdbcRealm.open.invalidurl", driverName, connectionURL ) );
        }
        dbConnection.setAutoCommit ( false );
        return ( dbConnection );
    }
    protected synchronized PreparedStatement roles ( Connection dbConnection,
            String username )
    throws SQLException {
        if ( preparedRoles == null ) {
            StringBuilder sb = new StringBuilder ( "SELECT " );
            sb.append ( roleNameCol );
            sb.append ( " FROM " );
            sb.append ( userRoleTable );
            sb.append ( " WHERE " );
            sb.append ( userNameCol );
            sb.append ( " = ?" );
            preparedRoles =
                dbConnection.prepareStatement ( sb.toString() );
        }
        preparedRoles.setString ( 1, username );
        return ( preparedRoles );
    }
    private boolean isRoleStoreDefined() {
        return userRoleTable != null || roleNameCol != null;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            open();
        } catch ( SQLException e ) {
            containerLog.error ( sm.getString ( "jdbcRealm.open" ), e );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        close ( this.dbConnection );
    }
}
