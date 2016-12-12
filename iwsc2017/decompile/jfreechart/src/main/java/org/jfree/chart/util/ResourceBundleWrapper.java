package org.jfree.chart.util;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLClassLoader;
public class ResourceBundleWrapper {
    private static URLClassLoader noCodeBaseClassLoader;
    public static void removeCodeBase ( final URL codeBase, final URLClassLoader urlClassLoader ) {
        final List urlsNoBase = new ArrayList();
        final URL[] urls = urlClassLoader.getURLs();
        for ( int i = 0; i < urls.length; ++i ) {
            if ( !urls[i].sameFile ( codeBase ) ) {
                urlsNoBase.add ( urls[i] );
            }
        }
        final URL[] urlsNoBaseArray = urlsNoBase.toArray ( new URL[0] );
        ResourceBundleWrapper.noCodeBaseClassLoader = URLClassLoader.newInstance ( urlsNoBaseArray );
    }
    public static ResourceBundle getBundle ( final String baseName ) {
        if ( ResourceBundleWrapper.noCodeBaseClassLoader != null ) {
            return ResourceBundle.getBundle ( baseName, Locale.getDefault(), ResourceBundleWrapper.noCodeBaseClassLoader );
        }
        return ResourceBundle.getBundle ( baseName );
    }
    public static ResourceBundle getBundle ( final String baseName, final Locale locale ) {
        if ( ResourceBundleWrapper.noCodeBaseClassLoader != null ) {
            return ResourceBundle.getBundle ( baseName, locale, ResourceBundleWrapper.noCodeBaseClassLoader );
        }
        return ResourceBundle.getBundle ( baseName, locale );
    }
    public static ResourceBundle getBundle ( final String baseName, final Locale locale, final ClassLoader loader ) {
        return ResourceBundle.getBundle ( baseName, locale, loader );
    }
}
