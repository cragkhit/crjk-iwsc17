package org.apache.catalina.realm;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.Context;
import javax.sql.DataSource;
import org.apache.catalina.LifecycleException;
import org.apache.naming.ContextBindings;
public class DataSourceRealm extends RealmBase {
    private String preparedRoles = null;
    private String preparedCredentials = null;
    protected String dataSourceName = null;
    protected boolean localDataSource = false;
    protected static final String name = "DataSourceRealm";
    protected String roleNameCol = null;
    protected String userCredCol = null;
    protected String userNameCol = null;
    protected String userRoleTable = null;
    protected String userTable = null;
    private volatile boolean connectionSuccess = true;
    public String getDataSourceName() {
        return dataSourceName;
    }
    public void setDataSourceName ( String dataSourceName ) {
        this.dataSourceName = dataSourceName;
    }
    public boolean getLocalDataSource() {
        return localDataSource;
    }
    public void setLocalDataSource ( boolean localDataSource ) {
        this.localDataSource = localDataSource;
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
    public Principal authenticate ( String username, String credentials ) {
        if ( username == null || credentials == null ) {
            return null;
        }
        Connection dbConnection = null;
        dbConnection = open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return authenticate ( dbConnection, username, credentials );
        } finally {
            close ( dbConnection );
        }
    }
    @Override
    public boolean isAvailable() {
        return connectionSuccess;
    }
    protected Principal authenticate ( Connection dbConnection,
                                       String username,
                                       String credentials ) {
        if ( username == null || credentials == null ) {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "dataSourceRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        String dbCredentials = getPassword ( dbConnection, username );
        if ( dbCredentials == null ) {
            getCredentialHandler().mutate ( credentials );
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "dataSourceRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        boolean validated = getCredentialHandler().matches ( credentials, dbCredentials );
        if ( validated ) {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "dataSourceRealm.authenticateSuccess",
                                                    username ) );
        } else {
            if ( containerLog.isTraceEnabled() )
                containerLog.trace ( sm.getString ( "dataSourceRealm.authenticateFailure",
                                                    username ) );
            return null;
        }
        ArrayList<String> list = getRoles ( dbConnection, username );
        return new GenericPrincipal ( username, credentials, list );
    }
    protected void close ( Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            if ( !dbConnection.getAutoCommit() ) {
                dbConnection.commit();
            }
        } catch ( SQLException e ) {
            containerLog.error ( "Exception committing connection before closing:", e );
        }
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            containerLog.error ( sm.getString ( "dataSourceRealm.close" ), e );
        }
    }
    protected Connection open() {
        try {
            Context context = null;
            if ( localDataSource ) {
                context = ContextBindings.getClassLoader();
                context = ( Context ) context.lookup ( "comp/env" );
            } else {
                context = getServer().getGlobalNamingContext();
            }
            DataSource dataSource = ( DataSource ) context.lookup ( dataSourceName );
            Connection connection = dataSource.getConnection();
            connectionSuccess = true;
            return connection;
        } catch ( Exception e ) {
            connectionSuccess = false;
            containerLog.error ( sm.getString ( "dataSourceRealm.exception" ), e );
        }
        return null;
    }
    @Override
    protected String getName() {
        return ( name );
    }
    @Override
    protected String getPassword ( String username ) {
        Connection dbConnection = null;
        dbConnection = open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return getPassword ( dbConnection, username );
        } finally {
            close ( dbConnection );
        }
    }
    protected String getPassword ( Connection dbConnection,
                                   String username ) {
        String dbCredentials = null;
        try ( PreparedStatement stmt = credentials ( dbConnection, username );
                    ResultSet rs = stmt.executeQuery() ) {
            if ( rs.next() ) {
                dbCredentials = rs.getString ( 1 );
            }
            return ( dbCredentials != null ) ? dbCredentials.trim() : null;
        } catch ( SQLException e ) {
            containerLog.error (
                sm.getString ( "dataSourceRealm.getPassword.exception",
                               username ), e );
        }
        return null;
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        Connection dbConnection = open();
        if ( dbConnection == null ) {
            return new GenericPrincipal ( username, null, null );
        }
        try {
            return ( new GenericPrincipal ( username,
                                            getPassword ( dbConnection, username ),
                                            getRoles ( dbConnection, username ) ) );
        } finally {
            close ( dbConnection );
        }
    }
    protected ArrayList<String> getRoles ( String username ) {
        Connection dbConnection = null;
        dbConnection = open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return getRoles ( dbConnection, username );
        } finally {
            close ( dbConnection );
        }
    }
    protected ArrayList<String> getRoles ( Connection dbConnection,
                                           String username ) {
        if ( allRolesMode != AllRolesMode.STRICT_MODE && !isRoleStoreDefined() ) {
            return null;
        }
        ArrayList<String> list = null;
        try ( PreparedStatement stmt = roles ( dbConnection, username );
                    ResultSet rs = stmt.executeQuery() ) {
            list = new ArrayList<>();
            while ( rs.next() ) {
                String role = rs.getString ( 1 );
                if ( role != null ) {
                    list.add ( role.trim() );
                }
            }
            return list;
        } catch ( SQLException e ) {
            containerLog.error (
                sm.getString ( "dataSourceRealm.getRoles.exception", username ), e );
        }
        return null;
    }
    private PreparedStatement credentials ( Connection dbConnection,
                                            String username )
    throws SQLException {
        PreparedStatement credentials =
            dbConnection.prepareStatement ( preparedCredentials );
        credentials.setString ( 1, username );
        return ( credentials );
    }
    private PreparedStatement roles ( Connection dbConnection, String username )
    throws SQLException {
        PreparedStatement roles =
            dbConnection.prepareStatement ( preparedRoles );
        roles.setString ( 1, username );
        return ( roles );
    }
    private boolean isRoleStoreDefined() {
        return userRoleTable != null || roleNameCol != null;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        StringBuilder temp = new StringBuilder ( "SELECT " );
        temp.append ( roleNameCol );
        temp.append ( " FROM " );
        temp.append ( userRoleTable );
        temp.append ( " WHERE " );
        temp.append ( userNameCol );
        temp.append ( " = ?" );
        preparedRoles = temp.toString();
        temp = new StringBuilder ( "SELECT " );
        temp.append ( userCredCol );
        temp.append ( " FROM " );
        temp.append ( userTable );
        temp.append ( " WHERE " );
        temp.append ( userNameCol );
        temp.append ( " = ?" );
        preparedCredentials = temp.toString();
        super.startInternal();
    }
}
