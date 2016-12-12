package org.apache.tomcat.util.compat;
import java.util.Locale;
public class JreVendor {
    static {
        String vendor = System.getProperty ( "java.vendor", "" );
        vendor = vendor.toLowerCase ( Locale.ENGLISH );
        if ( vendor.startsWith ( "oracle" ) || vendor.startsWith ( "sun" ) ) {
            IS_ORACLE_JVM = true;
            IS_IBM_JVM = false;
        } else if ( vendor.contains ( "ibm" ) ) {
            IS_ORACLE_JVM = false;
            IS_IBM_JVM = true;
        } else {
            IS_ORACLE_JVM = false;
            IS_IBM_JVM = false;
        }
    }
    public static final boolean IS_ORACLE_JVM;
    public static final boolean IS_IBM_JVM;
}
