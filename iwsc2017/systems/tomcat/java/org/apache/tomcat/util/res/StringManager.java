package org.apache.tomcat.util.res;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class StringManager {
    private static int LOCALE_CACHE_SIZE = 10;
    private final ResourceBundle bundle;
    private final Locale locale;
    private StringManager ( String packageName, Locale locale ) {
        String bundleName = packageName + ".LocalStrings";
        ResourceBundle bnd = null;
        try {
            bnd = ResourceBundle.getBundle ( bundleName, locale );
        } catch ( MissingResourceException ex ) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if ( cl != null ) {
                try {
                    bnd = ResourceBundle.getBundle ( bundleName, locale, cl );
                } catch ( MissingResourceException ex2 ) {
                }
            }
        }
        bundle = bnd;
        if ( bundle != null ) {
            Locale bundleLocale = bundle.getLocale();
            if ( bundleLocale.equals ( Locale.ROOT ) ) {
                this.locale = Locale.ENGLISH;
            } else {
                this.locale = bundleLocale;
            }
        } else {
            this.locale = null;
        }
    }
    public String getString ( String key ) {
        if ( key == null ) {
            String msg = "key may not have a null value";
            throw new IllegalArgumentException ( msg );
        }
        String str = null;
        try {
            if ( bundle != null ) {
                str = bundle.getString ( key );
            }
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
    public Locale getLocale() {
        return locale;
    }
    private static final Map<String, Map<Locale, StringManager>> managers =
        new Hashtable<>();
    public static final StringManager getManager ( Class<?> clazz ) {
        return getManager ( clazz.getPackage().getName() );
    }
    public static final StringManager getManager ( String packageName ) {
        return getManager ( packageName, Locale.getDefault() );
    }
    public static final synchronized StringManager getManager (
        String packageName, Locale locale ) {
        Map<Locale, StringManager> map = managers.get ( packageName );
        if ( map == null ) {
            map = new LinkedHashMap<Locale, StringManager> ( LOCALE_CACHE_SIZE, 1, true ) {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry (
                    Map.Entry<Locale, StringManager> eldest ) {
                    if ( size() > ( LOCALE_CACHE_SIZE - 1 ) ) {
                        return true;
                    }
                    return false;
                }
            };
            managers.put ( packageName, map );
        }
        StringManager mgr = map.get ( locale );
        if ( mgr == null ) {
            mgr = new StringManager ( packageName, locale );
            map.put ( locale, mgr );
        }
        return mgr;
    }
    public static StringManager getManager ( String packageName,
            Enumeration<Locale> requestedLocales ) {
        while ( requestedLocales.hasMoreElements() ) {
            Locale locale = requestedLocales.nextElement();
            StringManager result = getManager ( packageName, locale );
            if ( result.getLocale().equals ( locale ) ) {
                return result;
            }
        }
        return getManager ( packageName );
    }
}
