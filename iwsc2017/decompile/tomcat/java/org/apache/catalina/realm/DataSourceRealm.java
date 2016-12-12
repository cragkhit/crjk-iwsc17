package org.apache.catalina.realm;
import org.apache.catalina.LifecycleException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import javax.naming.Context;
import org.apache.naming.ContextBindings;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.security.Principal;
public class DataSourceRealm extends RealmBase {
    private String preparedRoles;
    private String preparedCredentials;
    protected String dataSourceName;
    protected boolean localDataSource;
    protected static final String name = "DataSourceRealm";
    protected String roleNameCol;
    protected String userCredCol;
    protected String userNameCol;
    protected String userRoleTable;
    protected String userTable;
    private volatile boolean connectionSuccess;
    public DataSourceRealm() {
        this.preparedRoles = null;
        this.preparedCredentials = null;
        this.dataSourceName = null;
        this.localDataSource = false;
        this.roleNameCol = null;
        this.userCredCol = null;
        this.userNameCol = null;
        this.userRoleTable = null;
        this.userTable = null;
        this.connectionSuccess = true;
    }
    public String getDataSourceName() {
        return this.dataSourceName;
    }
    public void setDataSourceName ( final String dataSourceName ) {
        this.dataSourceName = dataSourceName;
    }
    public boolean getLocalDataSource() {
        return this.localDataSource;
    }
    public void setLocalDataSource ( final boolean localDataSource ) {
        this.localDataSource = localDataSource;
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
    public Principal authenticate ( final String username, final String credentials ) {
        if ( username == null || credentials == null ) {
            return null;
        }
        Connection dbConnection = null;
        dbConnection = this.open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return this.authenticate ( dbConnection, username, credentials );
        } finally {
            this.close ( dbConnection );
        }
    }
    @Override
    public boolean isAvailable() {
        return this.connectionSuccess;
    }
    protected Principal authenticate ( final Connection dbConnection, final String username, final String credentials ) {
        if ( username == null || credentials == null ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( DataSourceRealm.sm.getString ( "dataSourceRealm.authenticateFailure", username ) );
            }
            return null;
        }
        final String dbCredentials = this.getPassword ( dbConnection, username );
        if ( dbCredentials == null ) {
            this.getCredentialHandler().mutate ( credentials );
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( DataSourceRealm.sm.getString ( "dataSourceRealm.authenticateFailure", username ) );
            }
            return null;
        }
        final boolean validated = this.getCredentialHandler().matches ( credentials, dbCredentials );
        if ( validated ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( DataSourceRealm.sm.getString ( "dataSourceRealm.authenticateSuccess", username ) );
            }
            final ArrayList<String> list = this.getRoles ( dbConnection, username );
            return new GenericPrincipal ( username, credentials, list );
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( DataSourceRealm.sm.getString ( "dataSourceRealm.authenticateFailure", username ) );
        }
        return null;
    }
    protected void close ( final Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            if ( !dbConnection.getAutoCommit() ) {
                dbConnection.commit();
            }
        } catch ( SQLException e ) {
            this.containerLog.error ( "Exception committing connection before closing:", e );
        }
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            this.containerLog.error ( DataSourceRealm.sm.getString ( "dataSourceRealm.close" ), e );
        }
    }
    protected Connection open() {
        try {
            Context context = null;
            if ( this.localDataSource ) {
                context = ContextBindings.getClassLoader();
                context = ( Context ) context.lookup ( "comp/env" );
            } else {
                context = this.getServer().getGlobalNamingContext();
            }
            final DataSource dataSource = ( DataSource ) context.lookup ( this.dataSourceName );
            final Connection connection = dataSource.getConnection();
            this.connectionSuccess = true;
            return connection;
        } catch ( Exception e ) {
            this.connectionSuccess = false;
            this.containerLog.error ( DataSourceRealm.sm.getString ( "dataSourceRealm.exception" ), e );
            return null;
        }
    }
    @Override
    protected String getName() {
        return "DataSourceRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        Connection dbConnection = null;
        dbConnection = this.open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return this.getPassword ( dbConnection, username );
        } finally {
            this.close ( dbConnection );
        }
    }
    protected String getPassword ( final Connection dbConnection, final String username ) {
        String dbCredentials = null;
        try ( final PreparedStatement stmt = this.credentials ( dbConnection, username );
                    final ResultSet rs = stmt.executeQuery() ) {
            if ( rs.next() ) {
                dbCredentials = rs.getString ( 1 );
            }
            return ( dbCredentials != null ) ? dbCredentials.trim() : null;
        } catch ( SQLException e ) {
            this.containerLog.error ( DataSourceRealm.sm.getString ( "dataSourceRealm.getPassword.exception", username ), e );
            return null;
        }
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        final Connection dbConnection = this.open();
        if ( dbConnection == null ) {
            return new GenericPrincipal ( username, null, null );
        }
        try {
            return new GenericPrincipal ( username, this.getPassword ( dbConnection, username ), this.getRoles ( dbConnection, username ) );
        } finally {
            this.close ( dbConnection );
        }
    }
    protected ArrayList<String> getRoles ( final String username ) {
        Connection dbConnection = null;
        dbConnection = this.open();
        if ( dbConnection == null ) {
            return null;
        }
        try {
            return this.getRoles ( dbConnection, username );
        } finally {
            this.close ( dbConnection );
        }
    }
    protected ArrayList<String> getRoles ( final Connection dbConnection, final String username ) {
        if ( this.allRolesMode != AllRolesMode.STRICT_MODE && !this.isRoleStoreDefined() ) {
            return null;
        }
        ArrayList<String> list = null;
        try ( final PreparedStatement stmt = this.roles ( dbConnection, username );
                    final ResultSet rs = stmt.executeQuery() ) {
            list = new ArrayList<String>();
            while ( rs.next() ) {
                final String role = rs.getString ( 1 );
                if ( role != null ) {
                    list.add ( role.trim() );
                }
            }
            return list;
        } catch ( SQLException e ) {
            this.containerLog.error ( DataSourceRealm.sm.getString ( "dataSourceRealm.getRoles.exception", username ), e );
            return null;
        }
    }
    private PreparedStatement credentials ( final Connection dbConnection, final String username ) throws SQLException {
        final PreparedStatement credentials = dbConnection.prepareStatement ( this.preparedCredentials );
        credentials.setString ( 1, username );
        return credentials;
    }
    private PreparedStatement roles ( final Connection dbConnection, final String username ) throws SQLException {
        final PreparedStatement roles = dbConnection.prepareStatement ( this.preparedRoles );
        roles.setString ( 1, username );
        return roles;
    }
    private boolean isRoleStoreDefined() {
        return this.userRoleTable != null || this.roleNameCol != null;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        StringBuilder temp = new StringBuilder ( "SELECT " );
        temp.append ( this.roleNameCol );
        temp.append ( " FROM " );
        temp.append ( this.userRoleTable );
        temp.append ( " WHERE " );
        temp.append ( this.userNameCol );
        temp.append ( " = ?" );
        this.preparedRoles = temp.toString();
        temp = new StringBuilder ( "SELECT " );
        temp.append ( this.userCredCol );
        temp.append ( " FROM " );
        temp.append ( this.userTable );
        temp.append ( " WHERE " );
        temp.append ( this.userNameCol );
        temp.append ( " = ?" );
        this.preparedCredentials = temp.toString();
        super.startInternal();
    }
}
