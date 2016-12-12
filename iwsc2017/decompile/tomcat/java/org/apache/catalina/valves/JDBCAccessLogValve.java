package org.apache.catalina.valves;
import java.util.Hashtable;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import java.util.Properties;
import org.apache.tomcat.util.ExceptionUtils;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Driver;
import org.apache.catalina.AccessLog;
public final class JDBCAccessLogValve extends ValveBase implements AccessLog {
    boolean useLongContentLength;
    String connectionName;
    String connectionPassword;
    Driver driver;
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
    boolean requestAttributesEnabled;
    public JDBCAccessLogValve() {
        super ( true );
        this.useLongContentLength = false;
        this.connectionName = null;
        this.connectionPassword = null;
        this.driver = null;
        this.requestAttributesEnabled = true;
        this.driverName = null;
        this.connectionURL = null;
        this.tableName = "access";
        this.remoteHostField = "remoteHost";
        this.userField = "userName";
        this.timestampField = "timestamp";
        this.virtualHostField = "virtualHost";
        this.methodField = "method";
        this.queryField = "query";
        this.statusField = "status";
        this.bytesField = "bytes";
        this.refererField = "referer";
        this.userAgentField = "userAgent";
        this.pattern = "common";
        this.resolveHosts = false;
        this.conn = null;
        this.ps = null;
        this.currentTimeMillis = new Date().getTime();
    }
    @Override
    public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
        this.requestAttributesEnabled = requestAttributesEnabled;
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return this.requestAttributesEnabled;
    }
    public String getConnectionName() {
        return this.connectionName;
    }
    public void setConnectionName ( final String connectionName ) {
        this.connectionName = connectionName;
    }
    public void setDriverName ( final String driverName ) {
        this.driverName = driverName;
    }
    public String getConnectionPassword() {
        return this.connectionPassword;
    }
    public void setConnectionPassword ( final String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public void setConnectionURL ( final String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public void setTableName ( final String tableName ) {
        this.tableName = tableName;
    }
    public void setRemoteHostField ( final String remoteHostField ) {
        this.remoteHostField = remoteHostField;
    }
    public void setUserField ( final String userField ) {
        this.userField = userField;
    }
    public void setTimestampField ( final String timestampField ) {
        this.timestampField = timestampField;
    }
    public void setVirtualHostField ( final String virtualHostField ) {
        this.virtualHostField = virtualHostField;
    }
    public void setMethodField ( final String methodField ) {
        this.methodField = methodField;
    }
    public void setQueryField ( final String queryField ) {
        this.queryField = queryField;
    }
    public void setStatusField ( final String statusField ) {
        this.statusField = statusField;
    }
    public void setBytesField ( final String bytesField ) {
        this.bytesField = bytesField;
    }
    public void setRefererField ( final String refererField ) {
        this.refererField = refererField;
    }
    public void setUserAgentField ( final String userAgentField ) {
        this.userAgentField = userAgentField;
    }
    public void setPattern ( final String pattern ) {
        this.pattern = pattern;
    }
    public void setResolveHosts ( final String resolveHosts ) {
        this.resolveHosts = Boolean.parseBoolean ( resolveHosts );
    }
    public boolean getUseLongContentLength() {
        return this.useLongContentLength;
    }
    public void setUseLongContentLength ( final boolean useLongContentLength ) {
        this.useLongContentLength = useLongContentLength;
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        this.getNext().invoke ( request, response );
    }
    @Override
    public void log ( final Request request, final Response response, final long time ) {
        if ( !this.getState().isAvailable() ) {
            return;
        }
        final String EMPTY = "";
        String remoteHost;
        if ( this.resolveHosts ) {
            if ( this.requestAttributesEnabled ) {
                final Object host = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteHost" );
                if ( host == null ) {
                    remoteHost = request.getRemoteHost();
                } else {
                    remoteHost = ( String ) host;
                }
            } else {
                remoteHost = request.getRemoteHost();
            }
        } else if ( this.requestAttributesEnabled ) {
            final Object addr = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteAddr" );
            if ( addr == null ) {
                remoteHost = request.getRemoteAddr();
            } else {
                remoteHost = ( String ) addr;
            }
        } else {
            remoteHost = request.getRemoteAddr();
        }
        final String user = request.getRemoteUser();
        final String query = request.getRequestURI();
        long bytes = response.getBytesWritten ( true );
        if ( bytes < 0L ) {
            bytes = 0L;
        }
        final int status = response.getStatus();
        String virtualHost = "";
        String method = "";
        String referer = "";
        String userAgent = "";
        final String logPattern = this.pattern;
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
                    this.open();
                    this.ps.setString ( 1, remoteHost );
                    this.ps.setString ( 2, user );
                    this.ps.setTimestamp ( 3, new Timestamp ( this.getCurrentTimeMillis() ) );
                    this.ps.setString ( 4, query );
                    this.ps.setInt ( 5, status );
                    if ( this.useLongContentLength ) {
                        this.ps.setLong ( 6, bytes );
                    } else {
                        if ( bytes > 2147483647L ) {
                            bytes = -1L;
                        }
                        this.ps.setInt ( 6, ( int ) bytes );
                    }
                    if ( logPattern.equals ( "combined" ) ) {
                        this.ps.setString ( 7, virtualHost );
                        this.ps.setString ( 8, method );
                        this.ps.setString ( 9, referer );
                        this.ps.setString ( 10, userAgent );
                    }
                    this.ps.executeUpdate();
                    return;
                } catch ( SQLException e ) {
                    this.container.getLogger().error ( JDBCAccessLogValve.sm.getString ( "jdbcAccessLogValve.exception" ), e );
                    if ( this.conn != null ) {
                        this.close();
                    }
                    --numberOfTries;
                    continue;
                }
                break;
            }
        }
    }
    protected void open() throws SQLException {
        if ( this.conn != null ) {
            return;
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
        ( this.conn = this.driver.connect ( this.connectionURL, props ) ).setAutoCommit ( true );
        final String logPattern = this.pattern;
        if ( logPattern.equals ( "common" ) ) {
            this.ps = this.conn.prepareStatement ( "INSERT INTO " + this.tableName + " (" + this.remoteHostField + ", " + this.userField + ", " + this.timestampField + ", " + this.queryField + ", " + this.statusField + ", " + this.bytesField + ") VALUES(?, ?, ?, ?, ?, ?)" );
        } else if ( logPattern.equals ( "combined" ) ) {
            this.ps = this.conn.prepareStatement ( "INSERT INTO " + this.tableName + " (" + this.remoteHostField + ", " + this.userField + ", " + this.timestampField + ", " + this.queryField + ", " + this.statusField + ", " + this.bytesField + ", " + this.virtualHostField + ", " + this.methodField + ", " + this.refererField + ", " + this.userAgentField + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
        }
    }
    protected void close() {
        if ( this.conn == null ) {
            return;
        }
        try {
            this.ps.close();
        } catch ( Throwable f ) {
            ExceptionUtils.handleThrowable ( f );
        }
        this.ps = null;
        try {
            this.conn.close();
        } catch ( SQLException e ) {
            this.container.getLogger().error ( JDBCAccessLogValve.sm.getString ( "jdbcAccessLogValve.close" ), e );
        } finally {
            this.conn = null;
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        try {
            this.open();
        } catch ( SQLException e ) {
            throw new LifecycleException ( e );
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        this.close();
    }
    public long getCurrentTimeMillis() {
        final long systime = System.currentTimeMillis();
        if ( systime - this.currentTimeMillis > 1000L ) {
            this.currentTimeMillis = new Date ( systime ).getTime();
        }
        return this.currentTimeMillis;
    }
}
