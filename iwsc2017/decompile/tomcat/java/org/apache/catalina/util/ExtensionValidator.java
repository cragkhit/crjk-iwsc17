package org.apache.catalina.util;
import org.apache.juli.logging.LogFactory;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.jar.JarInputStream;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.catalina.WebResource;
import java.util.jar.Manifest;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import java.util.ArrayList;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public final class ExtensionValidator {
    private static final Log log;
    private static final StringManager sm;
    private static volatile ArrayList<Extension> containerAvailableExtensions;
    private static final ArrayList<ManifestResource> containerManifestResources;
    public static synchronized boolean validateApplication ( final WebResourceRoot resources, final Context context ) throws IOException {
        final String appName = context.getName();
        final ArrayList<ManifestResource> appManifestResources = new ArrayList<ManifestResource>();
        final WebResource resource = resources.getResource ( "/META-INF/MANIFEST.MF" );
        if ( resource.isFile() ) {
            try ( final InputStream inputStream = resource.getInputStream() ) {
                final Manifest manifest = new Manifest ( inputStream );
                final ManifestResource mre = new ManifestResource ( ExtensionValidator.sm.getString ( "extensionValidator.web-application-manifest" ), manifest, 2 );
                appManifestResources.add ( mre );
            }
        }
        final WebResource[] classLoaderResources;
        final WebResource[] manifestResources = classLoaderResources = resources.getClassLoaderResources ( "/META-INF/MANIFEST.MF" );
        for ( final WebResource manifestResource : classLoaderResources ) {
            if ( manifestResource.isFile() ) {
                final String jarName = manifestResource.getURL().toExternalForm();
                Manifest jmanifest = null;
                try ( final InputStream is = manifestResource.getInputStream() ) {
                    jmanifest = new Manifest ( is );
                    final ManifestResource mre2 = new ManifestResource ( jarName, jmanifest, 3 );
                    appManifestResources.add ( mre2 );
                }
            }
        }
        return validateManifestResources ( appName, appManifestResources );
    }
    public static void addSystemResource ( final File jarFile ) throws IOException {
        try ( final InputStream is = new FileInputStream ( jarFile ) ) {
            final Manifest manifest = getManifest ( is );
            if ( manifest != null ) {
                final ManifestResource mre = new ManifestResource ( jarFile.getAbsolutePath(), manifest, 1 );
                ExtensionValidator.containerManifestResources.add ( mre );
            }
        }
    }
    private static boolean validateManifestResources ( final String appName, final ArrayList<ManifestResource> resources ) {
        boolean passes = true;
        int failureCount = 0;
        ArrayList<Extension> availableExtensions = null;
        for ( final ManifestResource mre : resources ) {
            final ArrayList<Extension> requiredList = mre.getRequiredExtensions();
            if ( requiredList == null ) {
                continue;
            }
            if ( availableExtensions == null ) {
                availableExtensions = buildAvailableExtensionsList ( resources );
            }
            if ( ExtensionValidator.containerAvailableExtensions == null ) {
                ExtensionValidator.containerAvailableExtensions = buildAvailableExtensionsList ( ExtensionValidator.containerManifestResources );
            }
            final Iterator<Extension> rit = requiredList.iterator();
            while ( rit.hasNext() ) {
                boolean found = false;
                final Extension requiredExt = rit.next();
                if ( availableExtensions != null ) {
                    for ( final Extension targetExt : availableExtensions ) {
                        if ( targetExt.isCompatibleWith ( requiredExt ) ) {
                            requiredExt.setFulfilled ( true );
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found && ExtensionValidator.containerAvailableExtensions != null ) {
                    for ( final Extension targetExt : ExtensionValidator.containerAvailableExtensions ) {
                        if ( targetExt.isCompatibleWith ( requiredExt ) ) {
                            requiredExt.setFulfilled ( true );
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found ) {
                    ExtensionValidator.log.info ( ExtensionValidator.sm.getString ( "extensionValidator.extension-not-found-error", appName, mre.getResourceName(), requiredExt.getExtensionName() ) );
                    passes = false;
                    ++failureCount;
                }
            }
        }
        if ( !passes ) {
            ExtensionValidator.log.info ( ExtensionValidator.sm.getString ( "extensionValidator.extension-validation-error", appName, failureCount + "" ) );
        }
        return passes;
    }
    private static ArrayList<Extension> buildAvailableExtensionsList ( final ArrayList<ManifestResource> resources ) {
        ArrayList<Extension> availableList = null;
        for ( final ManifestResource mre : resources ) {
            final ArrayList<Extension> list = mre.getAvailableExtensions();
            if ( list != null ) {
                for ( final Extension ext : list ) {
                    if ( availableList == null ) {
                        availableList = new ArrayList<Extension>();
                        availableList.add ( ext );
                    } else {
                        availableList.add ( ext );
                    }
                }
            }
        }
        return availableList;
    }
    private static Manifest getManifest ( final InputStream inStream ) throws IOException {
        Manifest manifest = null;
        try ( final JarInputStream jin = new JarInputStream ( inStream ) ) {
            manifest = jin.getManifest();
        }
        return manifest;
    }
    private static void addFolderList ( final String property ) {
        final String extensionsDir = System.getProperty ( property );
        if ( extensionsDir != null ) {
            final StringTokenizer extensionsTok = new StringTokenizer ( extensionsDir, File.pathSeparator );
            while ( extensionsTok.hasMoreTokens() ) {
                final File targetDir = new File ( extensionsTok.nextToken() );
                if ( !targetDir.isDirectory() ) {
                    continue;
                }
                final File[] files = targetDir.listFiles();
                if ( files == null ) {
                    continue;
                }
                for ( int i = 0; i < files.length; ++i ) {
                    if ( files[i].getName().toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) && files[i].isFile() ) {
                        try {
                            addSystemResource ( files[i] );
                        } catch ( IOException e ) {
                            ExtensionValidator.log.error ( ExtensionValidator.sm.getString ( "extensionValidator.failload", files[i] ), e );
                        }
                    }
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( ExtensionValidator.class );
        sm = StringManager.getManager ( "org.apache.catalina.util" );
        ExtensionValidator.containerAvailableExtensions = null;
        containerManifestResources = new ArrayList<ManifestResource>();
        final String systemClasspath = System.getProperty ( "java.class.path" );
        final StringTokenizer strTok = new StringTokenizer ( systemClasspath, File.pathSeparator );
        while ( strTok.hasMoreTokens() ) {
            final String classpathItem = strTok.nextToken();
            if ( classpathItem.toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) ) {
                final File item = new File ( classpathItem );
                if ( !item.isFile() ) {
                    continue;
                }
                try {
                    addSystemResource ( item );
                } catch ( IOException e ) {
                    ExtensionValidator.log.error ( ExtensionValidator.sm.getString ( "extensionValidator.failload", item ), e );
                }
            }
        }
        addFolderList ( "java.ext.dirs" );
    }
}
