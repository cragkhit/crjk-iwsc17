package org.apache.catalina.valves;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import javax.servlet.ServletException;
import org.apache.catalina.AccessLog;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.ExceptionUtils;
public final class JDBCAccessLogValve extends ValveBase implements AccessLog {
    public JDBCAccessLogValve() {
        super ( true );
        driverName = null;
        connectionURL = null;
        tableName = "access";
        remoteHostField = "remoteHost";
        userField = "userName";
        timestampField = "timestamp";
        virtualHostField = "virtualHost";
        methodField = "method";
        queryField = "query";
        statusField = "status";
        bytesField = "bytes";
        refererField = "referer";
        userAgentField = "userAgent";
        pattern = "common";
        resolveHosts = false;
        conn = null;
        ps = null;
        currentTimeMillis = new java.util.Date().getTime();
    }
    boolean useLongContentLength = false;
    String connectionName = null;
    String connectionPassword = null;
    Driver driver = null;
    private String driverName;
    private String connectionURL;
    private String tableName;
    private String remoteHostField;
    private String userField;
    private String timestampField;
    private String virtualHostField;
    private String methodField;
    private String queryField;
    private String statusField;
    private String bytesField;
    private String refererField;
    private String userAgentField;
    private String pattern;
    private boolean resolveHosts;
    private Connection conn;
    private PreparedStatement ps;
    private long currentTimeMillis;
    boolean requestAttributesEnabled = true;
    @Override
    public void setRequestAttributesEnabled ( boolean requestAttributesEnabled ) {
        this.requestAttributesEnabled = requestAttributesEnabled;
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return requestAttributesEnabled;
    }
    public String getConnectionName() {
        return connectionName;
    }
    public void setConnectionName ( String connectionName ) {
        this.connectionName = connectionName;
    }
    public void setDriverName ( String driverName ) {
        this.driverName = driverName;
    }
    public String getConnectionPassword() {
        return connectionPassword;
    }
    public void setConnectionPassword ( String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public void setConnectionURL ( String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public void setTableName ( String tableName ) {
        this.tableName = tableName;
    }
    public void setRemoteHostField ( String remoteHostField ) {
        this.remoteHostField = remoteHostField;
    }
    public void setUserField ( String userField ) {
        this.userField = userField;
    }
    public void setTimestampField ( String timestampField ) {
        this.timestampField = timestampField;
    }
    public void setVirtualHostField ( String virtualHostField ) {
        this.virtualHostField = virtualHostField;
    }
    public void setMethodField ( String methodField ) {
        this.methodField = methodField;
    }
    public void setQueryField ( String queryField ) {
        this.queryField = queryField;
    }
    public void setStatusField ( String statusField ) {
        this.statusField = statusField;
    }
    public void setBytesField ( String bytesField ) {
        this.bytesField = bytesField;
    }
    public void setRefererField ( String refererField ) {
        this.refererField = refererField;
    }
    public void setUserAgentField ( String userAgentField ) {
        this.userAgentField = userAgentField;
    }
    public void setPattern ( String pattern ) {
        this.pattern = pattern;
    }
    public void setResolveHosts ( String resolveHosts ) {
        this.resolveHosts = Boolean.parseBoolean ( resolveHosts );
    }
    public boolean getUseLongContentLength() {
        return this.useLongContentLength;
    }
    public void setUseLongContentLength ( boolean useLongContentLength ) {
        this.useLongContentLength = useLongContentLength;
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException,
        ServletException {
        getNext().invoke ( request, response );
    }
    @Override
    public void log ( Request request, Response response, long time ) {
        if ( !getState().isAvailable() ) {
            return;
        }
        final String EMPTY = "" ;
        String remoteHost;
        if ( resolveHosts ) {
            if ( requestAttributesEnabled ) {
                Object host = request.getAttribute ( REMOTE_HOST_ATTRIBUTE );
                if ( host == null ) {
                    remoteHost = request.getRemoteHost();
                } else {
                    remoteHost = ( String ) host;
                }
            } else {
                remoteHost = request.getRemoteHost();
            }
        } else {
            if ( requestAttributesEnabled ) {
                Object addr = request.getAttribute ( REMOTE_ADDR_ATTRIBUTE );
                if ( addr == null ) {
                    remoteHost = request.getRemoteAddr();
                } else {
                    remoteHost = ( String ) addr;
                }
            } else {
                remoteHost = request.getRemoteAddr();
            }
        }
        String user = request.getRemoteUser();
        String query = request.getRequestURI();
        long bytes = response.getBytesWritten ( true );
        if ( bytes < 0 ) {
            bytes = 0;
        }
        int status = response.getStatus();
        String virtualHost = EMPTY;
        String method = EMPTY;
        String referer = EMPTY;
        String userAgent = EMPTY;
        String logPattern = pattern;
        if ( logPattern.equals ( "combined" ) ) {
            virtualHost = request.getServerName();
            method = request.getMethod();
            referer = request.getHeader ( "referer" );
            userAgent = request.getHeader ( "user-agent" );
        }
        synchronized ( this ) {
            int numberOfTries = 2;
            while ( numberOfTries > 0 ) {
                try {
                    open();
                    ps.setString ( 1, remoteHost );
                    ps.setString ( 2, user );
                    ps.setTimestamp ( 3, new Timestamp ( getCurrentTimeMillis() ) );
                    ps.setString ( 4, query );
                    ps.setInt ( 5, status );
                    if ( useLongContentLength ) {
                        ps.setLong ( 6, bytes );
                    } else {
                        if ( bytes > Integer.MAX_VALUE ) {
                            bytes = -1 ;
                        }
                        ps.setInt ( 6, ( int ) bytes );
                    }
                    if ( logPattern.equals ( "combined" ) ) {
                        ps.setString ( 7, virtualHost );
                        ps.setString ( 8, method );
                        ps.setString ( 9, referer );
                        ps.setString ( 10, userAgent );
                    }
                    ps.executeUpdate();
                    return;
                } catch ( SQLException e ) {
                    container.getLogger().error ( sm.getString ( "jdbcAccessLogValve.exception" ), e );
                    if ( conn != null ) {
                        close();
                    }
                }
                numberOfTries--;
            }
        }
    }
    protected void open() throws SQLException {
        if ( conn != null ) {
            return ;
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
        conn = driver.connect ( connectionURL, props );
        conn.setAutoCommit ( true );
        String logPattern = pattern;
        if ( logPattern.equals ( "common" ) ) {
            ps = conn.prepareStatement
                 ( "INSERT INTO " + tableName + " ("
                   + remoteHostField + ", " + userField + ", "
                   + timestampField + ", " + queryField + ", "
                   + statusField + ", " + bytesField
                   + ") VALUES(?, ?, ?, ?, ?, ?)" );
        } else if ( logPattern.equals ( "combined" ) ) {
            ps = conn.prepareStatement
                 ( "INSERT INTO " + tableName + " ("
                   + remoteHostField + ", " + userField + ", "
                   + timestampField + ", " + queryField + ", "
                   + statusField + ", " + bytesField + ", "
                   + virtualHostField + ", " + methodField + ", "
                   + refererField + ", " + userAgentField
                   + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
        }
    }
    protected void close() {
        if ( conn == null ) {
            return;
        }
        try {
            ps.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.ps = null;
        try {
            conn.close();
        } catch ( SQLException e ) {
            container.getLogger().error ( sm.getString ( "jdbcAccessLogValve.close" ), e );
        } finally {
            this.conn = null;
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        try {
            open() ;
        } catch ( SQLException e ) {
            throw new LifecycleException ( e );
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        close() ;
    }
    public long getCurrentTimeMillis() {
        long systime  =  System.currentTimeMillis();
        if ( ( systime - currentTimeMillis ) > 1000 ) {
            currentTimeMillis  =  new java.util.Date ( systime ).getTime();
        }
        return currentTimeMillis;
    }
}
