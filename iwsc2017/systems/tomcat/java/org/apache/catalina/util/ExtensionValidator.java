package org.apache.catalina.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.catalina.Context;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public final class ExtensionValidator {
    private static final Log log = LogFactory.getLog ( ExtensionValidator.class );
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    private static volatile ArrayList<Extension> containerAvailableExtensions =
        null;
    private static final ArrayList<ManifestResource> containerManifestResources =
        new ArrayList<>();
    static {
        String systemClasspath = System.getProperty ( "java.class.path" );
        StringTokenizer strTok = new StringTokenizer ( systemClasspath,
                File.pathSeparator );
        while ( strTok.hasMoreTokens() ) {
            String classpathItem = strTok.nextToken();
            if ( classpathItem.toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) ) {
                File item = new File ( classpathItem );
                if ( item.isFile() ) {
                    try {
                        addSystemResource ( item );
                    } catch ( IOException e ) {
                        log.error ( sm.getString
                                    ( "extensionValidator.failload", item ), e );
                    }
                }
            }
        }
        addFolderList ( "java.ext.dirs" );
    }
    public static synchronized boolean validateApplication (
        WebResourceRoot resources,
        Context context )
    throws IOException {
        String appName = context.getName();
        ArrayList<ManifestResource> appManifestResources = new ArrayList<>();
        WebResource resource = resources.getResource ( "/META-INF/MANIFEST.MF" );
        if ( resource.isFile() ) {
            try ( InputStream inputStream = resource.getInputStream() ) {
                Manifest manifest = new Manifest ( inputStream );
                ManifestResource mre = new ManifestResource
                ( sm.getString ( "extensionValidator.web-application-manifest" ),
                  manifest, ManifestResource.WAR );
                appManifestResources.add ( mre );
            }
        }
        WebResource[] manifestResources =
            resources.getClassLoaderResources ( "/META-INF/MANIFEST.MF" );
        for ( WebResource manifestResource : manifestResources ) {
            if ( manifestResource.isFile() ) {
                String jarName = manifestResource.getURL().toExternalForm();
                Manifest jmanifest = null;
                try ( InputStream is = manifestResource.getInputStream() ) {
                    jmanifest = new Manifest ( is );
                    ManifestResource mre = new ManifestResource ( jarName,
                            jmanifest, ManifestResource.APPLICATION );
                    appManifestResources.add ( mre );
                }
            }
        }
        return validateManifestResources ( appName, appManifestResources );
    }
    public static void addSystemResource ( File jarFile ) throws IOException {
        try ( InputStream is = new FileInputStream ( jarFile ) ) {
            Manifest manifest = getManifest ( is );
            if ( manifest != null ) {
                ManifestResource mre = new ManifestResource ( jarFile.getAbsolutePath(), manifest,
                        ManifestResource.SYSTEM );
                containerManifestResources.add ( mre );
            }
        }
    }
    private static boolean validateManifestResources ( String appName,
            ArrayList<ManifestResource> resources ) {
        boolean passes = true;
        int failureCount = 0;
        ArrayList<Extension> availableExtensions = null;
        Iterator<ManifestResource> it = resources.iterator();
        while ( it.hasNext() ) {
            ManifestResource mre = it.next();
            ArrayList<Extension> requiredList = mre.getRequiredExtensions();
            if ( requiredList == null ) {
                continue;
            }
            if ( availableExtensions == null ) {
                availableExtensions = buildAvailableExtensionsList ( resources );
            }
            if ( containerAvailableExtensions == null ) {
                containerAvailableExtensions
                    = buildAvailableExtensionsList ( containerManifestResources );
            }
            Iterator<Extension> rit = requiredList.iterator();
            while ( rit.hasNext() ) {
                boolean found = false;
                Extension requiredExt = rit.next();
                if ( availableExtensions != null ) {
                    Iterator<Extension> ait = availableExtensions.iterator();
                    while ( ait.hasNext() ) {
                        Extension targetExt = ait.next();
                        if ( targetExt.isCompatibleWith ( requiredExt ) ) {
                            requiredExt.setFulfilled ( true );
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found && containerAvailableExtensions != null ) {
                    Iterator<Extension> cit =
                        containerAvailableExtensions.iterator();
                    while ( cit.hasNext() ) {
                        Extension targetExt = cit.next();
                        if ( targetExt.isCompatibleWith ( requiredExt ) ) {
                            requiredExt.setFulfilled ( true );
                            found = true;
                            break;
                        }
                    }
                }
                if ( !found ) {
                    log.info ( sm.getString (
                                   "extensionValidator.extension-not-found-error",
                                   appName, mre.getResourceName(),
                                   requiredExt.getExtensionName() ) );
                    passes = false;
                    failureCount++;
                }
            }
        }
        if ( !passes ) {
            log.info ( sm.getString (
                           "extensionValidator.extension-validation-error", appName,
                           failureCount + "" ) );
        }
        return passes;
    }
    private static ArrayList<Extension> buildAvailableExtensionsList (
        ArrayList<ManifestResource> resources ) {
        ArrayList<Extension> availableList = null;
        Iterator<ManifestResource> it = resources.iterator();
        while ( it.hasNext() ) {
            ManifestResource mre = it.next();
            ArrayList<Extension> list = mre.getAvailableExtensions();
            if ( list != null ) {
                Iterator<Extension> values = list.iterator();
                while ( values.hasNext() ) {
                    Extension ext = values.next();
                    if ( availableList == null ) {
                        availableList = new ArrayList<>();
                        availableList.add ( ext );
                    } else {
                        availableList.add ( ext );
                    }
                }
            }
        }
        return availableList;
    }
    private static Manifest getManifest ( InputStream inStream ) throws IOException {
        Manifest manifest = null;
        try ( JarInputStream jin = new JarInputStream ( inStream ) ) {
            manifest = jin.getManifest();
        }
        return manifest;
    }
    private static void addFolderList ( String property ) {
        String extensionsDir = System.getProperty ( property );
        if ( extensionsDir != null ) {
            StringTokenizer extensionsTok
                = new StringTokenizer ( extensionsDir, File.pathSeparator );
            while ( extensionsTok.hasMoreTokens() ) {
                File targetDir = new File ( extensionsTok.nextToken() );
                if ( !targetDir.isDirectory() ) {
                    continue;
                }
                File[] files = targetDir.listFiles();
                if ( files == null ) {
                    continue;
                }
                for ( int i = 0; i < files.length; i++ ) {
                    if ( files[i].getName().toLowerCase ( Locale.ENGLISH ).endsWith ( ".jar" ) &&
                            files[i].isFile() ) {
                        try {
                            addSystemResource ( files[i] );
                        } catch ( IOException e ) {
                            log.error
                            ( sm.getString
                              ( "extensionValidator.failload", files[i] ), e );
                        }
                    }
                }
            }
        }
    }
}
