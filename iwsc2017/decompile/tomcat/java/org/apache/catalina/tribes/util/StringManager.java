package org.apache.catalina.tribes.util;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.Locale;
import java.util.ResourceBundle;
public class StringManager {
    private static int LOCALE_CACHE_SIZE;
    private final ResourceBundle bundle;
    private final Locale locale;
    private static final Map<String, Map<Locale, StringManager>> managers;
    private StringManager ( final String packageName, final Locale locale ) {
        final String bundleName = packageName + ".LocalStrings";
        ResourceBundle bnd = null;
        try {
            bnd = ResourceBundle.getBundle ( bundleName, locale );
        } catch ( MissingResourceException ex ) {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if ( cl != null ) {
                try {
                    bnd = ResourceBundle.getBundle ( bundleName, locale, cl );
                } catch ( MissingResourceException ex2 ) {}
            }
        }
        this.bundle = bnd;
        if ( this.bundle != null ) {
            final Locale bundleLocale = this.bundle.getLocale();
            if ( bundleLocale.equals ( Locale.ROOT ) ) {
                this.locale = Locale.ENGLISH;
            } else {
                this.locale = bundleLocale;
            }
        } else {
            this.locale = null;
        }
    }
    public String getString ( final String key ) {
        if ( key == null ) {
            final String msg = "key may not have a null value";
            throw new IllegalArgumentException ( msg );
        }
        String str = null;
        try {
            if ( this.bundle != null ) {
                str = this.bundle.getString ( key );
            }
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
    public Locale getLocale() {
        return this.locale;
    }
    public static final StringManager getManager ( final Class<?> clazz ) {
        return getManager ( clazz.getPackage().getName() );
    }
    public static final StringManager getManager ( final String packageName ) {
        return getManager ( packageName, Locale.getDefault() );
    }
    public static final synchronized StringManager getManager ( final String packageName, final Locale locale ) {
        Map<Locale, StringManager> map = StringManager.managers.get ( packageName );
        if ( map == null ) {
            map = new LinkedHashMap<Locale, StringManager> ( StringManager.LOCALE_CACHE_SIZE, 1.0f, true ) {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry ( final Map.Entry<Locale, StringManager> eldest ) {
                    return this.size() > StringManager.LOCALE_CACHE_SIZE - 1;
                }
            };
            StringManager.managers.put ( packageName, map );
        }
        StringManager mgr = map.get ( locale );
        if ( mgr == null ) {
            mgr = new StringManager ( packageName, locale );
            map.put ( locale, mgr );
        }
        return mgr;
    }
    public static StringManager getManager ( final String packageName, final Enumeration<Locale> requestedLocales ) {
        while ( requestedLocales.hasMoreElements() ) {
            final Locale locale = requestedLocales.nextElement();
            final StringManager result = getManager ( packageName, locale );
            if ( result.getLocale().equals ( locale ) ) {
                return result;
            }
        }
        return getManager ( packageName );
    }
    static {
        StringManager.LOCALE_CACHE_SIZE = 10;
        managers = new Hashtable<String, Map<Locale, StringManager>>();
    }
}
