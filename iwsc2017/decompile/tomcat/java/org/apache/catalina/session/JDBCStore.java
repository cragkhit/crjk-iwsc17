package org.apache.catalina.session;
import java.util.Hashtable;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import org.apache.juli.logging.Log;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.IOException;
import org.apache.catalina.Container;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.Connection;
public class JDBCStore extends StoreBase {
    private String name;
    protected static final String storeName = "JDBCStore";
    protected static final String threadName = "JDBCStore";
    protected String connectionName;
    protected String connectionPassword;
    protected String connectionURL;
    private Connection dbConnection;
    protected Driver driver;
    protected String driverName;
    protected String dataSourceName;
    protected DataSource dataSource;
    protected String sessionTable;
    protected String sessionAppCol;
    protected String sessionIdCol;
    protected String sessionDataCol;
    protected String sessionValidCol;
    protected String sessionMaxInactiveCol;
    protected String sessionLastAccessedCol;
    protected PreparedStatement preparedSizeSql;
    protected PreparedStatement preparedSaveSql;
    protected PreparedStatement preparedClearSql;
    protected PreparedStatement preparedRemoveSql;
    protected PreparedStatement preparedLoadSql;
    public JDBCStore() {
        this.name = null;
        this.connectionName = null;
        this.connectionPassword = null;
        this.connectionURL = null;
        this.dbConnection = null;
        this.driver = null;
        this.driverName = null;
        this.dataSourceName = null;
        this.dataSource = null;
        this.sessionTable = "tomcat$sessions";
        this.sessionAppCol = "app";
        this.sessionIdCol = "id";
        this.sessionDataCol = "data";
        this.sessionValidCol = "valid";
        this.sessionMaxInactiveCol = "maxinactive";
        this.sessionLastAccessedCol = "lastaccess";
        this.preparedSizeSql = null;
        this.preparedSaveSql = null;
        this.preparedClearSql = null;
        this.preparedRemoveSql = null;
        this.preparedLoadSql = null;
    }
    public String getName() {
        if ( this.name == null ) {
            final Container container = this.manager.getContext();
            String contextName = container.getName();
            if ( !contextName.startsWith ( "/" ) ) {
                contextName = "/" + contextName;
            }
            String hostName = "";
            String engineName = "";
            if ( container.getParent() != null ) {
                final Container host = container.getParent();
                hostName = host.getName();
                if ( host.getParent() != null ) {
                    engineName = host.getParent().getName();
                }
            }
            this.name = "/" + engineName + "/" + hostName + contextName;
        }
        return this.name;
    }
    public String getThreadName() {
        return "JDBCStore";
    }
    @Override
    public String getStoreName() {
        return "JDBCStore";
    }
    public void setDriverName ( final String driverName ) {
        final String oldDriverName = this.driverName;
        this.driverName = driverName;
        this.support.firePropertyChange ( "driverName", oldDriverName, this.driverName );
        this.driverName = driverName;
    }
    public String getDriverName() {
        return this.driverName;
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
    public void setConnectionURL ( final String connectionURL ) {
        final String oldConnString = this.connectionURL;
        this.connectionURL = connectionURL;
        this.support.firePropertyChange ( "connectionURL", oldConnString, this.connectionURL );
    }
    public String getConnectionURL() {
        return this.connectionURL;
    }
    public void setSessionTable ( final String sessionTable ) {
        final String oldSessionTable = this.sessionTable;
        this.sessionTable = sessionTable;
        this.support.firePropertyChange ( "sessionTable", oldSessionTable, this.sessionTable );
    }
    public String getSessionTable() {
        return this.sessionTable;
    }
    public void setSessionAppCol ( final String sessionAppCol ) {
        final String oldSessionAppCol = this.sessionAppCol;
        this.sessionAppCol = sessionAppCol;
        this.support.firePropertyChange ( "sessionAppCol", oldSessionAppCol, this.sessionAppCol );
    }
    public String getSessionAppCol() {
        return this.sessionAppCol;
    }
    public void setSessionIdCol ( final String sessionIdCol ) {
        final String oldSessionIdCol = this.sessionIdCol;
        this.sessionIdCol = sessionIdCol;
        this.support.firePropertyChange ( "sessionIdCol", oldSessionIdCol, this.sessionIdCol );
    }
    public String getSessionIdCol() {
        return this.sessionIdCol;
    }
    public void setSessionDataCol ( final String sessionDataCol ) {
        final String oldSessionDataCol = this.sessionDataCol;
        this.sessionDataCol = sessionDataCol;
        this.support.firePropertyChange ( "sessionDataCol", oldSessionDataCol, this.sessionDataCol );
    }
    public String getSessionDataCol() {
        return this.sessionDataCol;
    }
    public void setSessionValidCol ( final String sessionValidCol ) {
        final String oldSessionValidCol = this.sessionValidCol;
        this.sessionValidCol = sessionValidCol;
        this.support.firePropertyChange ( "sessionValidCol", oldSessionValidCol, this.sessionValidCol );
    }
    public String getSessionValidCol() {
        return this.sessionValidCol;
    }
    public void setSessionMaxInactiveCol ( final String sessionMaxInactiveCol ) {
        final String oldSessionMaxInactiveCol = this.sessionMaxInactiveCol;
        this.sessionMaxInactiveCol = sessionMaxInactiveCol;
        this.support.firePropertyChange ( "sessionMaxInactiveCol", oldSessionMaxInactiveCol, this.sessionMaxInactiveCol );
    }
    public String getSessionMaxInactiveCol() {
        return this.sessionMaxInactiveCol;
    }
    public void setSessionLastAccessedCol ( final String sessionLastAccessedCol ) {
        final String oldSessionLastAccessedCol = this.sessionLastAccessedCol;
        this.sessionLastAccessedCol = sessionLastAccessedCol;
        this.support.firePropertyChange ( "sessionLastAccessedCol", oldSessionLastAccessedCol, this.sessionLastAccessedCol );
    }
    public String getSessionLastAccessedCol() {
        return this.sessionLastAccessedCol;
    }
    public void setDataSourceName ( final String dataSourceName ) {
        if ( dataSourceName == null || "".equals ( dataSourceName.trim() ) ) {
            this.manager.getContext().getLogger().warn ( JDBCStore.sm.getString ( this.getStoreName() + ".missingDataSourceName" ) );
            return;
        }
        this.dataSourceName = dataSourceName;
    }
    public String getDataSourceName() {
        return this.dataSourceName;
    }
    @Override
    public String[] expiredKeys() throws IOException {
        return this.keys ( true );
    }
    @Override
    public String[] keys() throws IOException {
        return this.keys ( false );
    }
    private String[] keys ( final boolean expiredOnly ) throws IOException {
        String[] keys = null;
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return new String[0];
                }
                try {
                    String keysSql = "SELECT " + this.sessionIdCol + " FROM " + this.sessionTable + " WHERE " + this.sessionAppCol + " = ?";
                    if ( expiredOnly ) {
                        keysSql = keysSql + " AND (" + this.sessionLastAccessedCol + " + " + this.sessionMaxInactiveCol + " * 1000 < ?)";
                    }
                    try ( final PreparedStatement preparedKeysSql = _conn.prepareStatement ( keysSql ) ) {
                        preparedKeysSql.setString ( 1, this.getName() );
                        if ( expiredOnly ) {
                            preparedKeysSql.setLong ( 2, System.currentTimeMillis() );
                        }
                        try ( final ResultSet rst = preparedKeysSql.executeQuery() ) {
                            final ArrayList<String> tmpkeys = new ArrayList<String>();
                            if ( rst != null ) {
                                while ( rst.next() ) {
                                    tmpkeys.add ( rst.getString ( 1 ) );
                                }
                            }
                            keys = tmpkeys.toArray ( new String[tmpkeys.size()] );
                            numberOfTries = 0;
                        }
                    }
                } catch ( SQLException e ) {
                    this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    keys = new String[0];
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } finally {
                    this.release ( _conn );
                }
            }
        }
        return keys;
    }
    @Override
    public int getSize() throws IOException {
        int size = 0;
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return size;
                }
                try {
                    if ( this.preparedSizeSql == null ) {
                        final String sizeSql = "SELECT COUNT(" + this.sessionIdCol + ") FROM " + this.sessionTable + " WHERE " + this.sessionAppCol + " = ?";
                        this.preparedSizeSql = _conn.prepareStatement ( sizeSql );
                    }
                    this.preparedSizeSql.setString ( 1, this.getName() );
                    try ( final ResultSet rst = this.preparedSizeSql.executeQuery() ) {
                        if ( rst.next() ) {
                            size = rst.getInt ( 1 );
                        }
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } finally {
                    this.release ( _conn );
                }
            }
        }
        return size;
    }
    @Override
    public Session load ( final String id ) throws ClassNotFoundException, IOException {
        StandardSession _session = null;
        final Context context = this.getManager().getContext();
        final Log contextLog = context.getLogger();
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return null;
                }
                final ClassLoader oldThreadContextCL = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                try {
                    if ( this.preparedLoadSql == null ) {
                        final String loadSql = "SELECT " + this.sessionIdCol + ", " + this.sessionDataCol + " FROM " + this.sessionTable + " WHERE " + this.sessionIdCol + " = ? AND " + this.sessionAppCol + " = ?";
                        this.preparedLoadSql = _conn.prepareStatement ( loadSql );
                    }
                    this.preparedLoadSql.setString ( 1, id );
                    this.preparedLoadSql.setString ( 2, this.getName() );
                    try ( final ResultSet rst = this.preparedLoadSql.executeQuery() ) {
                        if ( rst.next() ) {
                            try ( final ObjectInputStream ois = this.getObjectInputStream ( rst.getBinaryStream ( 2 ) ) ) {
                                if ( contextLog.isDebugEnabled() ) {
                                    contextLog.debug ( JDBCStore.sm.getString ( this.getStoreName() + ".loading", id, this.sessionTable ) );
                                }
                                _session = ( StandardSession ) this.manager.createEmptySession();
                                _session.readObjectData ( ois );
                                _session.setManager ( this.manager );
                            }
                        } else if ( context.getLogger().isDebugEnabled() ) {
                            contextLog.debug ( this.getStoreName() + ": No persisted data object found" );
                        }
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    contextLog.error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } finally {
                    context.unbind ( Globals.IS_SECURITY_ENABLED, oldThreadContextCL );
                    this.release ( _conn );
                }
            }
        }
        return _session;
    }
    @Override
    public void remove ( final String id ) throws IOException {
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    this.remove ( id, _conn );
                    numberOfTries = 0;
                } catch ( SQLException e ) {
                    this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } finally {
                    this.release ( _conn );
                }
            }
        }
        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
            this.manager.getContext().getLogger().debug ( JDBCStore.sm.getString ( this.getStoreName() + ".removing", id, this.sessionTable ) );
        }
    }
    private void remove ( final String id, final Connection _conn ) throws SQLException {
        if ( this.preparedRemoveSql == null ) {
            final String removeSql = "DELETE FROM " + this.sessionTable + " WHERE " + this.sessionIdCol + " = ?  AND " + this.sessionAppCol + " = ?";
            this.preparedRemoveSql = _conn.prepareStatement ( removeSql );
        }
        this.preparedRemoveSql.setString ( 1, id );
        this.preparedRemoveSql.setString ( 2, this.getName() );
        this.preparedRemoveSql.execute();
    }
    @Override
    public void clear() throws IOException {
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    if ( this.preparedClearSql == null ) {
                        final String clearSql = "DELETE FROM " + this.sessionTable + " WHERE " + this.sessionAppCol + " = ?";
                        this.preparedClearSql = _conn.prepareStatement ( clearSql );
                    }
                    this.preparedClearSql.setString ( 1, this.getName() );
                    this.preparedClearSql.execute();
                    numberOfTries = 0;
                } catch ( SQLException e ) {
                    this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } finally {
                    this.release ( _conn );
                }
            }
        }
    }
    @Override
    public void save ( final Session session ) throws IOException {
        ByteArrayOutputStream bos = null;
        synchronized ( this ) {
            for ( int numberOfTries = 2; numberOfTries > 0; --numberOfTries ) {
                final Connection _conn = this.getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    this.remove ( session.getIdInternal(), _conn );
                    bos = new ByteArrayOutputStream();
                    try ( final ObjectOutputStream oos = new ObjectOutputStream ( new BufferedOutputStream ( bos ) ) ) {
                        ( ( StandardSession ) session ).writeObjectData ( oos );
                    }
                    final byte[] obs = bos.toByteArray();
                    final int size = obs.length;
                    try ( final ByteArrayInputStream bis = new ByteArrayInputStream ( obs, 0, size );
                                final InputStream in = new BufferedInputStream ( bis, size ) ) {
                        if ( this.preparedSaveSql == null ) {
                            final String saveSql = "INSERT INTO " + this.sessionTable + " (" + this.sessionIdCol + ", " + this.sessionAppCol + ", " + this.sessionDataCol + ", " + this.sessionValidCol + ", " + this.sessionMaxInactiveCol + ", " + this.sessionLastAccessedCol + ") VALUES (?, ?, ?, ?, ?, ?)";
                            this.preparedSaveSql = _conn.prepareStatement ( saveSql );
                        }
                        this.preparedSaveSql.setString ( 1, session.getIdInternal() );
                        this.preparedSaveSql.setString ( 2, this.getName() );
                        this.preparedSaveSql.setBinaryStream ( 3, in, size );
                        this.preparedSaveSql.setString ( 4, session.isValid() ? "1" : "0" );
                        this.preparedSaveSql.setInt ( 5, session.getMaxInactiveInterval() );
                        this.preparedSaveSql.setLong ( 6, session.getLastAccessedTime() );
                        this.preparedSaveSql.execute();
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".SQLException", e ) );
                    if ( this.dbConnection != null ) {
                        this.close ( this.dbConnection );
                    }
                } catch ( IOException ex ) {}
                finally {
                    this.release ( _conn );
                }
            }
        }
        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
            this.manager.getContext().getLogger().debug ( JDBCStore.sm.getString ( this.getStoreName() + ".saving", session.getIdInternal(), this.sessionTable ) );
        }
    }
    protected Connection getConnection() {
        Connection conn = null;
        try {
            conn = this.open();
            if ( conn == null || conn.isClosed() ) {
                this.manager.getContext().getLogger().info ( JDBCStore.sm.getString ( this.getStoreName() + ".checkConnectionDBClosed" ) );
                conn = this.open();
                if ( conn == null || conn.isClosed() ) {
                    this.manager.getContext().getLogger().info ( JDBCStore.sm.getString ( this.getStoreName() + ".checkConnectionDBReOpenFail" ) );
                }
            }
        } catch ( SQLException ex ) {
            this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".checkConnectionSQLException", ex.toString() ) );
        }
        return conn;
    }
    protected Connection open() throws SQLException {
        if ( this.dbConnection != null ) {
            return this.dbConnection;
        }
        if ( this.dataSourceName != null && this.dataSource == null ) {
            try {
                final javax.naming.Context initCtx = new InitialContext();
                final javax.naming.Context envCtx = ( javax.naming.Context ) initCtx.lookup ( "java:comp/env" );
                this.dataSource = ( DataSource ) envCtx.lookup ( this.dataSourceName );
            } catch ( NamingException e ) {
                this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".wrongDataSource", this.dataSourceName ), e );
            }
        }
        if ( this.dataSource != null ) {
            return this.dataSource.getConnection();
        }
        if ( this.driver == null ) {
            try {
                final Class<?> clazz = Class.forName ( this.driverName );
                this.driver = ( Driver ) clazz.newInstance();
            } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e2 ) {
                this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".checkConnectionClassNotFoundException", e2.toString() ) );
                throw new SQLException ( e2 );
            }
        }
        final Properties props = new Properties();
        if ( this.connectionName != null ) {
            ( ( Hashtable<String, String> ) props ).put ( "user", this.connectionName );
        }
        if ( this.connectionPassword != null ) {
            ( ( Hashtable<String, String> ) props ).put ( "password", this.connectionPassword );
        }
        ( this.dbConnection = this.driver.connect ( this.connectionURL, props ) ).setAutoCommit ( true );
        return this.dbConnection;
    }
    protected void close ( final Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            this.preparedSizeSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedSizeSql = null;
        try {
            this.preparedSaveSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedSaveSql = null;
        try {
            this.preparedClearSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        try {
            this.preparedRemoveSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedRemoveSql = null;
        try {
            this.preparedLoadSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedLoadSql = null;
        try {
            if ( !dbConnection.getAutoCommit() ) {
                dbConnection.commit();
            }
        } catch ( SQLException e ) {
            this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".commitSQLException" ), e );
        }
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            this.manager.getContext().getLogger().error ( JDBCStore.sm.getString ( this.getStoreName() + ".close", e.toString() ) );
        } finally {
            this.dbConnection = null;
        }
    }
    protected void release ( final Connection conn ) {
        if ( this.dataSource != null ) {
            this.close ( conn );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( this.dataSourceName == null ) {
            this.dbConnection = this.getConnection();
        }
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        if ( this.dbConnection != null ) {
            try {
                this.dbConnection.commit();
            } catch ( SQLException ex ) {}
            this.close ( this.dbConnection );
        }
    }
}
