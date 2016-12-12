package org.apache.jasper.compiler;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.jasper.runtime.ExceptionUtils;
public class Localizer {
    private static ResourceBundle bundle;
    static {
        try {
            bundle = ResourceBundle.getBundle (
                         "org.apache.jasper.resources.LocalStrings" );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            t.printStackTrace();
        }
    }
    public static String getMessage ( String errCode ) {
        String errMsg = errCode;
        try {
            errMsg = bundle.getString ( errCode );
        } catch ( MissingResourceException e ) {
        }
        return errMsg;
    }
    public static String getMessage ( String errCode, String arg ) {
        return getMessage ( errCode, new Object[] {arg} );
    }
    public static String getMessage ( String errCode, String arg1, String arg2 ) {
        return getMessage ( errCode, new Object[] {arg1, arg2} );
    }
    public static String getMessage ( String errCode, String arg1, String arg2,
                                      String arg3 ) {
        return getMessage ( errCode, new Object[] {arg1, arg2, arg3} );
    }
    public static String getMessage ( String errCode, String arg1, String arg2,
                                      String arg3, String arg4 ) {
        return getMessage ( errCode, new Object[] {arg1, arg2, arg3, arg4} );
    }
    public static String getMessage ( String errCode, Object[] args ) {
        String errMsg = errCode;
        try {
            errMsg = bundle.getString ( errCode );
            if ( args != null && args.length > 0 ) {
                MessageFormat formatter = new MessageFormat ( errMsg );
                errMsg = formatter.format ( args );
            }
        } catch ( MissingResourceException e ) {
        }
        return errMsg;
    }
}
