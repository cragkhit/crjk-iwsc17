package org.apache.naming;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
public class StringManager {
    private final ResourceBundle bundle;
    private final Locale locale;
    private static final Hashtable<String, StringManager> managers;
    private StringManager ( final String packageName ) {
        final String bundleName = packageName + ".LocalStrings";
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle ( bundleName, Locale.getDefault() );
        } catch ( MissingResourceException ex ) {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if ( cl != null ) {
                try {
                    tempBundle = ResourceBundle.getBundle ( bundleName, Locale.getDefault(), cl );
                } catch ( MissingResourceException ex2 ) {}
            }
        }
        if ( tempBundle != null ) {
            this.locale = tempBundle.getLocale();
        } else {
            this.locale = null;
        }
        this.bundle = tempBundle;
    }
    public String getString ( final String key ) {
        if ( key == null ) {
            final String msg = "key may not have a null value";
            throw new IllegalArgumentException ( msg );
        }
        String str = null;
        try {
            str = this.bundle.getString ( key );
        } catch ( MissingResourceException mre ) {
            str = null;
        }
        return str;
    }
    public String getString ( final String key, final Object... args ) {
        String value = this.getString ( key );
        if ( value == null ) {
            value = key;
        }
        final MessageFormat mf = new MessageFormat ( value );
        mf.setLocale ( this.locale );
        return mf.format ( args, new StringBuffer(), null ).toString();
    }
    public static final synchronized StringManager getManager ( final String packageName ) {
        StringManager mgr = StringManager.managers.get ( packageName );
        if ( mgr == null ) {
            mgr = new StringManager ( packageName );
            StringManager.managers.put ( packageName, mgr );
        }
        return mgr;
    }
    public static final StringManager getManager ( final Class<?> clazz ) {
        return getManager ( clazz.getPackage().getName() );
    }
    static {
        managers = new Hashtable<String, StringManager>();
    }
}
