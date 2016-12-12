package org.apache.naming;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class StringManager {
    private final ResourceBundle bundle;
    private final Locale locale;
    private StringManager ( String packageName ) {
        String bundleName = packageName + ".LocalStrings";
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle ( bundleName, Locale.getDefault() );
        } catch ( MissingResourceException ex ) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if ( cl != null ) {
                try {
                    tempBundle = ResourceBundle.getBundle (
                                     bundleName, Locale.getDefault(), cl );
                } catch ( MissingResourceException ex2 ) {
                }
            }
        }
        if ( tempBundle != null ) {
            locale = tempBundle.getLocale();
        } else {
            locale = null;
        }
        bundle = tempBundle;
    }
    public String getString ( String key ) {
        if ( key == null ) {
            String msg = "key may not have a null value";
            throw new IllegalArgumentException ( msg );
        }
        String str = null;
        try {
            str = bundle.getString ( key );
        } catch ( MissingResourceException mre ) {
            str = null;
        }
        return str;
    }
    public String getString ( final String key, final Object... args ) {
        String value = getString ( key );
        if ( value == null ) {
            value = key;
        }
        MessageFormat mf = new MessageFormat ( value );
        mf.setLocale ( locale );
        return mf.format ( args, new StringBuffer(), null ).toString();
    }
    private static final Hashtable<String, StringManager> managers =
        new Hashtable<>();
    public static final synchronized StringManager getManager ( String packageName ) {
        StringManager mgr = managers.get ( packageName );
        if ( mgr == null ) {
            mgr = new StringManager ( packageName );
            managers.put ( packageName, mgr );
        }
        return mgr;
    }
    public static final StringManager getManager ( Class<?> clazz ) {
        return getManager ( clazz.getPackage().getName() );
    }
}
