package org.apache.tomcat.dbcp.dbcp2;
import java.util.HashSet;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Set;
import java.util.ResourceBundle;
public final class Utils {
    private static final ResourceBundle messages;
    public static final boolean IS_SECURITY_ENABLED;
    public static final String DISCONNECTION_SQL_CODE_PREFIX = "08";
    public static final Set<String> DISCONNECTION_SQL_CODES;
    public static void closeQuietly ( final ResultSet rset ) {
        if ( rset != null ) {
            try {
                rset.close();
            } catch ( Exception ex ) {}
        }
    }
    public static void closeQuietly ( final Connection conn ) {
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( Exception ex ) {}
        }
    }
    public static void closeQuietly ( final Statement stmt ) {
        if ( stmt != null ) {
            try {
                stmt.close();
            } catch ( Exception ex ) {}
        }
    }
    public static String getMessage ( final String key ) {
        return getMessage ( key, ( Object[] ) null );
    }
    public static String getMessage ( final String key, final Object... args ) {
        final String msg = Utils.messages.getString ( key );
        if ( args == null || args.length == 0 ) {
            return msg;
        }
        final MessageFormat mf = new MessageFormat ( msg );
        return mf.format ( args, new StringBuffer(), null ).toString();
    }
    static {
        messages = ResourceBundle.getBundle ( Utils.class.getPackage().getName() + ".LocalStrings" );
        IS_SECURITY_ENABLED = ( System.getSecurityManager() != null );
        ( DISCONNECTION_SQL_CODES = new HashSet<String>() ).add ( "57P01" );
        Utils.DISCONNECTION_SQL_CODES.add ( "57P02" );
        Utils.DISCONNECTION_SQL_CODES.add ( "57P03" );
        Utils.DISCONNECTION_SQL_CODES.add ( "01002" );
        Utils.DISCONNECTION_SQL_CODES.add ( "JZ0C0" );
        Utils.DISCONNECTION_SQL_CODES.add ( "JZ0C1" );
    }
}
