package org.apache.tomcat.dbcp.dbcp2;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
public final class Utils {
    private static final ResourceBundle messages = ResourceBundle.getBundle (
                Utils.class.getPackage().getName() + ".LocalStrings" );
    public static final boolean IS_SECURITY_ENABLED =
        System.getSecurityManager() != null;
    public static final String DISCONNECTION_SQL_CODE_PREFIX = "08";
    public static final Set<String> DISCONNECTION_SQL_CODES;
    static {
        DISCONNECTION_SQL_CODES = new HashSet<>();
        DISCONNECTION_SQL_CODES.add ( "57P01" );
        DISCONNECTION_SQL_CODES.add ( "57P02" );
        DISCONNECTION_SQL_CODES.add ( "57P03" );
        DISCONNECTION_SQL_CODES.add ( "01002" );
        DISCONNECTION_SQL_CODES.add ( "JZ0C0" );
        DISCONNECTION_SQL_CODES.add ( "JZ0C1" );
    }
    private Utils() {
    }
    public static void closeQuietly ( final ResultSet rset ) {
        if ( rset != null ) {
            try {
                rset.close();
            } catch ( final Exception e ) {
            }
        }
    }
    public static void closeQuietly ( final Connection conn ) {
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( final Exception e ) {
            }
        }
    }
    public static void closeQuietly ( final Statement stmt ) {
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( final Exception e ) {
            }
        }
    }
    public static String getMessage ( final String key ) {
        return getMessage ( key, ( Object[] ) null );
    }
    public static String getMessage ( final String key, final Object... args ) {
        final String msg =  messages.getString ( key );
        if ( args == null || args.length == 0 ) {
            return msg;
        }
        final MessageFormat mf = new MessageFormat ( msg );
        return mf.format ( args, new StringBuffer(), null ).toString();
    }
}
