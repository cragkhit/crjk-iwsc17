package org.apache.catalina.session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.ExceptionUtils;
public class JDBCStore extends StoreBase {
    private String name = null;
    protected static final String storeName = "JDBCStore";
    protected static final String threadName = "JDBCStore";
    protected String connectionName = null;
    protected String connectionPassword = null;
    protected String connectionURL = null;
    private Connection dbConnection = null;
    protected Driver driver = null;
    protected String driverName = null;
    protected String dataSourceName = null;
    protected DataSource dataSource = null;
    protected String sessionTable = "tomcat$sessions";
    protected String sessionAppCol = "app";
    protected String sessionIdCol = "id";
    protected String sessionDataCol = "data";
    protected String sessionValidCol = "valid";
    protected String sessionMaxInactiveCol = "maxinactive";
    protected String sessionLastAccessedCol = "lastaccess";
    protected PreparedStatement preparedSizeSql = null;
    protected PreparedStatement preparedSaveSql = null;
    protected PreparedStatement preparedClearSql = null;
    protected PreparedStatement preparedRemoveSql = null;
    protected PreparedStatement preparedLoadSql = null;
    public String getName() {
        if ( name == null ) {
            Container container = manager.getContext();
            String contextName = container.getName();
            if ( !contextName.startsWith ( "/" ) ) {
                contextName = "/" + contextName;
            }
            String hostName = "";
            String engineName = "";
            if ( container.getParent() != null ) {
                Container host = container.getParent();
                hostName = host.getName();
                if ( host.getParent() != null ) {
                    engineName = host.getParent().getName();
                }
            }
            name = "/" + engineName + "/" + hostName + contextName;
        }
        return name;
    }
    public String getThreadName() {
        return threadName;
    }
    @Override
    public String getStoreName() {
        return storeName;
    }
    public void setDriverName ( String driverName ) {
        String oldDriverName = this.driverName;
        this.driverName = driverName;
        support.firePropertyChange ( "driverName",
                                     oldDriverName,
                                     this.driverName );
        this.driverName = driverName;
    }
    public String getDriverName() {
        return driverName;
    }
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
    public void setConnectionURL ( String connectionURL ) {
        String oldConnString = this.connectionURL;
        this.connectionURL = connectionURL;
        support.firePropertyChange ( "connectionURL",
                                     oldConnString,
                                     this.connectionURL );
    }
    public String getConnectionURL() {
        return connectionURL;
    }
    public void setSessionTable ( String sessionTable ) {
        String oldSessionTable = this.sessionTable;
        this.sessionTable = sessionTable;
        support.firePropertyChange ( "sessionTable",
                                     oldSessionTable,
                                     this.sessionTable );
    }
    public String getSessionTable() {
        return sessionTable;
    }
    public void setSessionAppCol ( String sessionAppCol ) {
        String oldSessionAppCol = this.sessionAppCol;
        this.sessionAppCol = sessionAppCol;
        support.firePropertyChange ( "sessionAppCol",
                                     oldSessionAppCol,
                                     this.sessionAppCol );
    }
    public String getSessionAppCol() {
        return this.sessionAppCol;
    }
    public void setSessionIdCol ( String sessionIdCol ) {
        String oldSessionIdCol = this.sessionIdCol;
        this.sessionIdCol = sessionIdCol;
        support.firePropertyChange ( "sessionIdCol",
                                     oldSessionIdCol,
                                     this.sessionIdCol );
    }
    public String getSessionIdCol() {
        return this.sessionIdCol;
    }
    public void setSessionDataCol ( String sessionDataCol ) {
        String oldSessionDataCol = this.sessionDataCol;
        this.sessionDataCol = sessionDataCol;
        support.firePropertyChange ( "sessionDataCol",
                                     oldSessionDataCol,
                                     this.sessionDataCol );
    }
    public String getSessionDataCol() {
        return this.sessionDataCol;
    }
    public void setSessionValidCol ( String sessionValidCol ) {
        String oldSessionValidCol = this.sessionValidCol;
        this.sessionValidCol = sessionValidCol;
        support.firePropertyChange ( "sessionValidCol",
                                     oldSessionValidCol,
                                     this.sessionValidCol );
    }
    public String getSessionValidCol() {
        return this.sessionValidCol;
    }
    public void setSessionMaxInactiveCol ( String sessionMaxInactiveCol ) {
        String oldSessionMaxInactiveCol = this.sessionMaxInactiveCol;
        this.sessionMaxInactiveCol = sessionMaxInactiveCol;
        support.firePropertyChange ( "sessionMaxInactiveCol",
                                     oldSessionMaxInactiveCol,
                                     this.sessionMaxInactiveCol );
    }
    public String getSessionMaxInactiveCol() {
        return this.sessionMaxInactiveCol;
    }
    public void setSessionLastAccessedCol ( String sessionLastAccessedCol ) {
        String oldSessionLastAccessedCol = this.sessionLastAccessedCol;
        this.sessionLastAccessedCol = sessionLastAccessedCol;
        support.firePropertyChange ( "sessionLastAccessedCol",
                                     oldSessionLastAccessedCol,
                                     this.sessionLastAccessedCol );
    }
    public String getSessionLastAccessedCol() {
        return this.sessionLastAccessedCol;
    }
    public void setDataSourceName ( String dataSourceName ) {
        if ( dataSourceName == null || "".equals ( dataSourceName.trim() ) ) {
            manager.getContext().getLogger().warn (
                sm.getString ( getStoreName() + ".missingDataSourceName" ) );
            return;
        }
        this.dataSourceName = dataSourceName;
    }
    public String getDataSourceName() {
        return this.dataSourceName;
    }
    @Override
    public String[] expiredKeys() throws IOException {
        return keys ( true );
    }
    @Override
    public String[] keys() throws IOException {
        return keys ( false );
    }
    private String[] keys ( boolean expiredOnly ) throws IOException {
        String keys[] = null;
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return new String[0];
                }
                try {
                    String keysSql = "SELECT " + sessionIdCol + " FROM "
                                     + sessionTable + " WHERE " + sessionAppCol + " = ?";
                    if ( expiredOnly ) {
                        keysSql += " AND (" + sessionLastAccessedCol + " + "
                                   + sessionMaxInactiveCol + " * 1000 < ?)";
                    }
                    try ( PreparedStatement preparedKeysSql = _conn.prepareStatement ( keysSql ) ) {
                        preparedKeysSql.setString ( 1, getName() );
                        if ( expiredOnly ) {
                            preparedKeysSql.setLong ( 2, System.currentTimeMillis() );
                        }
                        try ( ResultSet rst = preparedKeysSql.executeQuery() ) {
                            ArrayList<String> tmpkeys = new ArrayList<>();
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
                    manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    keys = new String[0];
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } finally {
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
        return keys;
    }
    @Override
    public int getSize() throws IOException {
        int size = 0;
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return size;
                }
                try {
                    if ( preparedSizeSql == null ) {
                        String sizeSql = "SELECT COUNT(" + sessionIdCol
                                         + ") FROM " + sessionTable + " WHERE "
                                         + sessionAppCol + " = ?";
                        preparedSizeSql = _conn.prepareStatement ( sizeSql );
                    }
                    preparedSizeSql.setString ( 1, getName() );
                    try ( ResultSet rst = preparedSizeSql.executeQuery() ) {
                        if ( rst.next() ) {
                            size = rst.getInt ( 1 );
                        }
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } finally {
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
        return size;
    }
    @Override
    public Session load ( String id ) throws ClassNotFoundException, IOException {
        StandardSession _session = null;
        org.apache.catalina.Context context = getManager().getContext();
        Log contextLog = context.getLogger();
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return null;
                }
                ClassLoader oldThreadContextCL = context.bind ( Globals.IS_SECURITY_ENABLED, null );
                try {
                    if ( preparedLoadSql == null ) {
                        String loadSql = "SELECT " + sessionIdCol + ", "
                                         + sessionDataCol + " FROM " + sessionTable
                                         + " WHERE " + sessionIdCol + " = ? AND "
                                         + sessionAppCol + " = ?";
                        preparedLoadSql = _conn.prepareStatement ( loadSql );
                    }
                    preparedLoadSql.setString ( 1, id );
                    preparedLoadSql.setString ( 2, getName() );
                    try ( ResultSet rst = preparedLoadSql.executeQuery() ) {
                        if ( rst.next() ) {
                            try ( ObjectInputStream ois =
                                            getObjectInputStream ( rst.getBinaryStream ( 2 ) ) ) {
                                if ( contextLog.isDebugEnabled() ) {
                                    contextLog.debug ( sm.getString (
                                                           getStoreName() + ".loading", id, sessionTable ) );
                                }
                                _session = ( StandardSession ) manager.createEmptySession();
                                _session.readObjectData ( ois );
                                _session.setManager ( manager );
                            }
                        } else if ( context.getLogger().isDebugEnabled() ) {
                            contextLog.debug ( getStoreName() + ": No persisted data object found" );
                        }
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    contextLog.error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } finally {
                    context.unbind ( Globals.IS_SECURITY_ENABLED, oldThreadContextCL );
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
        return _session;
    }
    @Override
    public void remove ( String id ) throws IOException {
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    remove ( id, _conn );
                    numberOfTries = 0;
                } catch ( SQLException e ) {
                    manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } finally {
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
        if ( manager.getContext().getLogger().isDebugEnabled() ) {
            manager.getContext().getLogger().debug ( sm.getString ( getStoreName() + ".removing", id, sessionTable ) );
        }
    }
    private void remove ( String id, Connection _conn ) throws SQLException {
        if ( preparedRemoveSql == null ) {
            String removeSql = "DELETE FROM " + sessionTable
                               + " WHERE " + sessionIdCol + " = ?  AND "
                               + sessionAppCol + " = ?";
            preparedRemoveSql = _conn.prepareStatement ( removeSql );
        }
        preparedRemoveSql.setString ( 1, id );
        preparedRemoveSql.setString ( 2, getName() );
        preparedRemoveSql.execute();
    }
    @Override
    public void clear() throws IOException {
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    if ( preparedClearSql == null ) {
                        String clearSql = "DELETE FROM " + sessionTable
                                          + " WHERE " + sessionAppCol + " = ?";
                        preparedClearSql = _conn.prepareStatement ( clearSql );
                    }
                    preparedClearSql.setString ( 1, getName() );
                    preparedClearSql.execute();
                    numberOfTries = 0;
                } catch ( SQLException e ) {
                    manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } finally {
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
    }
    @Override
    public void save ( Session session ) throws IOException {
        ByteArrayOutputStream bos = null;
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                Connection _conn = getConnection();
                if ( _conn == null ) {
                    return;
                }
                try {
                    remove ( session.getIdInternal(), _conn );
                    bos = new ByteArrayOutputStream();
                    try ( ObjectOutputStream oos =
                                    new ObjectOutputStream ( new BufferedOutputStream ( bos ) ) ) {
                        ( ( StandardSession ) session ).writeObjectData ( oos );
                    }
                    byte[] obs = bos.toByteArray();
                    int size = obs.length;
                    try ( ByteArrayInputStream bis = new ByteArrayInputStream ( obs, 0, size );
                                InputStream in = new BufferedInputStream ( bis, size ) ) {
                        if ( preparedSaveSql == null ) {
                            String saveSql = "INSERT INTO " + sessionTable + " ("
                                             + sessionIdCol + ", " + sessionAppCol + ", "
                                             + sessionDataCol + ", " + sessionValidCol
                                             + ", " + sessionMaxInactiveCol + ", "
                                             + sessionLastAccessedCol
                                             + ") VALUES (?, ?, ?, ?, ?, ?)";
                            preparedSaveSql = _conn.prepareStatement ( saveSql );
                        }
                        preparedSaveSql.setString ( 1, session.getIdInternal() );
                        preparedSaveSql.setString ( 2, getName() );
                        preparedSaveSql.setBinaryStream ( 3, in, size );
                        preparedSaveSql.setString ( 4, session.isValid() ? "1" : "0" );
                        preparedSaveSql.setInt ( 5, session.getMaxInactiveInterval() );
                        preparedSaveSql.setLong ( 6, session.getLastAccessedTime() );
                        preparedSaveSql.execute();
                        numberOfTries = 0;
                    }
                } catch ( SQLException e ) {
                    manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".SQLException", e ) );
                    if ( dbConnection != null ) {
                        close ( dbConnection );
                    }
                } catch ( IOException e ) {
                } finally {
                    release ( _conn );
                }
                numberOfTries--;
            }
        }
        if ( manager.getContext().getLogger().isDebugEnabled() ) {
            manager.getContext().getLogger().debug ( sm.getString ( getStoreName() + ".saving",
                    session.getIdInternal(), sessionTable ) );
        }
    }
    protected Connection getConnection() {
        Connection conn = null;
        try {
            conn = open();
            if ( conn == null || conn.isClosed() ) {
                manager.getContext().getLogger().info ( sm.getString ( getStoreName() + ".checkConnectionDBClosed" ) );
                conn = open();
                if ( conn == null || conn.isClosed() ) {
                    manager.getContext().getLogger().info ( sm.getString ( getStoreName() + ".checkConnectionDBReOpenFail" ) );
                }
            }
        } catch ( SQLException ex ) {
            manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".checkConnectionSQLException",
                    ex.toString() ) );
        }
        return conn;
    }
    protected Connection open() throws SQLException {
        if ( dbConnection != null ) {
            return dbConnection;
        }
        if ( dataSourceName != null && dataSource == null ) {
            Context initCtx;
            try {
                initCtx = new InitialContext();
                Context envCtx = ( Context ) initCtx.lookup ( "java:comp/env" );
                this.dataSource = ( DataSource ) envCtx.lookup ( this.dataSourceName );
            } catch ( NamingException e ) {
                manager.getContext().getLogger().error (
                    sm.getString ( getStoreName() + ".wrongDataSource",
                                   this.dataSourceName ), e );
            }
        }
        if ( dataSource != null ) {
            return dataSource.getConnection();
        }
        if ( driver == null ) {
            try {
                Class<?> clazz = Class.forName ( driverName );
                driver = ( Driver ) clazz.newInstance();
            } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
                manager.getContext().getLogger().error (
                    sm.getString ( getStoreName() + ".checkConnectionClassNotFoundException",
                                   e.toString() ) );
                throw new SQLException ( e );
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
        dbConnection.setAutoCommit ( true );
        return dbConnection;
    }
    protected void close ( Connection dbConnection ) {
        if ( dbConnection == null ) {
            return;
        }
        try {
            preparedSizeSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedSizeSql = null;
        try {
            preparedSaveSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedSaveSql = null;
        try {
            preparedClearSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        try {
            preparedRemoveSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedRemoveSql = null;
        try {
            preparedLoadSql.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.preparedLoadSql = null;
        try {
            if ( !dbConnection.getAutoCommit() ) {
                dbConnection.commit();
            }
        } catch ( SQLException e ) {
            manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".commitSQLException" ), e );
        }
        try {
            dbConnection.close();
        } catch ( SQLException e ) {
            manager.getContext().getLogger().error ( sm.getString ( getStoreName() + ".close", e.toString() ) );
        } finally {
            this.dbConnection = null;
        }
    }
    protected void release ( Connection conn ) {
        if ( dataSource != null ) {
            close ( conn );
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        if ( dataSourceName == null ) {
            this.dbConnection = getConnection();
        }
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        if ( dbConnection != null ) {
            try {
                dbConnection.commit();
            } catch ( SQLException e ) {
            }
            close ( dbConnection );
        }
    }
}
