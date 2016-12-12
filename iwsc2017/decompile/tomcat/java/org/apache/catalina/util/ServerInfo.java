package org.apache.catalina.util;
import java.io.InputStream;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Properties;
public class ServerInfo {
    private static final String serverInfo;
    private static final String serverBuilt;
    private static final String serverNumber;
    public static String getServerInfo() {
        return ServerInfo.serverInfo;
    }
    public static String getServerBuilt() {
        return ServerInfo.serverBuilt;
    }
    public static String getServerNumber() {
        return ServerInfo.serverNumber;
    }
    public static void main ( final String[] args ) {
        System.out.println ( "Server version: " + getServerInfo() );
        System.out.println ( "Server built:   " + getServerBuilt() );
        System.out.println ( "Server number:  " + getServerNumber() );
        System.out.println ( "OS Name:        " + System.getProperty ( "os.name" ) );
        System.out.println ( "OS Version:     " + System.getProperty ( "os.version" ) );
        System.out.println ( "Architecture:   " + System.getProperty ( "os.arch" ) );
        System.out.println ( "JVM Version:    " + System.getProperty ( "java.runtime.version" ) );
        System.out.println ( "JVM Vendor:     " + System.getProperty ( "java.vm.vendor" ) );
    }
    static {
        String info = null;
        String built = null;
        String number = null;
        final Properties props = new Properties();
        try ( final InputStream is = ServerInfo.class.getResourceAsStream ( "/org/apache/catalina/util/ServerInfo.properties" ) ) {
            props.load ( is );
            info = props.getProperty ( "server.info" );
            built = props.getProperty ( "server.built" );
            number = props.getProperty ( "server.number" );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        if ( info == null ) {
            info = "Apache Tomcat 9.0.x-dev";
        }
        if ( built == null ) {
            built = "unknown";
        }
        if ( number == null ) {
            number = "9.0.x";
        }
        serverInfo = info;
        serverBuilt = built;
        serverNumber = number;
    }
}
