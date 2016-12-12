package org.apache.jasper.compiler;
import org.apache.jasper.runtime.ExceptionUtils;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class Localizer {
    private static ResourceBundle bundle;
    public static String getMessage ( final String errCode ) {
        String errMsg = errCode;
        try {
            errMsg = Localizer.bundle.getString ( errCode );
        } catch ( MissingResourceException ex ) {}
        return errMsg;
    }
    public static String getMessage ( final String errCode, final String arg ) {
        return getMessage ( errCode, new Object[] { arg } );
    }
    public static String getMessage ( final String errCode, final String arg1, final String arg2 ) {
        return getMessage ( errCode, new Object[] { arg1, arg2 } );
    }
    public static String getMessage ( final String errCode, final String arg1, final String arg2, final String arg3 ) {
        return getMessage ( errCode, new Object[] { arg1, arg2, arg3 } );
    }
    public static String getMessage ( final String errCode, final String arg1, final String arg2, final String arg3, final String arg4 ) {
        return getMessage ( errCode, new Object[] { arg1, arg2, arg3, arg4 } );
    }
    public static String getMessage ( final String errCode, final Object[] args ) {
        String errMsg = errCode;
        try {
            errMsg = Localizer.bundle.getString ( errCode );
            if ( args != null && args.length > 0 ) {
                final MessageFormat formatter = new MessageFormat ( errMsg );
                errMsg = formatter.format ( args );
            }
        } catch ( MissingResourceException ex ) {}
        return errMsg;
    }
    static {
        try {
            Localizer.bundle = ResourceBundle.getBundle ( "org.apache.jasper.resources.LocalStrings" );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            t.printStackTrace();
        }
    }
}
