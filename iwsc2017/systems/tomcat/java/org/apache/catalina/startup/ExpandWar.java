package org.apache.catalina.startup;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;
import org.apache.catalina.Host;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class ExpandWar {
    private static final Log log = LogFactory.getLog ( ExpandWar.class );
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    public static String expand ( Host host, URL war, String pathname )
    throws IOException {
        JarURLConnection juc = ( JarURLConnection ) war.openConnection();
        juc.setUseCaches ( false );
        URL jarFileUrl = juc.getJarFileURL();
        URLConnection jfuc = jarFileUrl.openConnection();
        boolean success = false;
        File docBase = new File ( host.getAppBaseFile(), pathname );
        File warTracker = new File ( host.getAppBaseFile(), pathname + Constants.WarTracker );
        long warLastModified = -1;
        try ( InputStream is = jfuc.getInputStream() ) {
            warLastModified = jfuc.getLastModified();
        }
        if ( docBase.exists() ) {
            if ( !warTracker.exists() || warTracker.lastModified() == warLastModified ) {
                success = true;
                return ( docBase.getAbsolutePath() );
            }
            log.info ( sm.getString ( "expandWar.deleteOld", docBase ) );
            if ( !delete ( docBase ) ) {
                throw new IOException ( sm.getString ( "expandWar.deleteFailed", docBase ) );
            }
        }
        if ( !docBase.mkdir() && !docBase.isDirectory() ) {
            throw new IOException ( sm.getString ( "expandWar.createFailed", docBase ) );
        }
        String canonicalDocBasePrefix = docBase.getCanonicalPath();
        if ( !canonicalDocBasePrefix.endsWith ( File.separator ) ) {
            canonicalDocBasePrefix += File.separator;
        }
        File warTrackerParent = warTracker.getParentFile();
        if ( !warTrackerParent.isDirectory() && !warTrackerParent.mkdirs() ) {
            throw new IOException ( sm.getString ( "expandWar.createFailed", warTrackerParent.getAbsolutePath() ) );
        }
        try ( JarFile jarFile = juc.getJarFile() ) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while ( jarEntries.hasMoreElements() ) {
                JarEntry jarEntry = jarEntries.nextElement();
                String name = jarEntry.getName();
                File expandedFile = new File ( docBase, name );
                if ( !expandedFile.getCanonicalPath().startsWith (
                            canonicalDocBasePrefix ) ) {
                    throw new IllegalArgumentException (
                        sm.getString ( "expandWar.illegalPath", war, name,
                                       expandedFile.getCanonicalPath(),
                                       canonicalDocBasePrefix ) );
                }
                int last = name.lastIndexOf ( '/' );
                if ( last >= 0 ) {
                    File parent = new File ( docBase,
                                             name.substring ( 0, last ) );
                    if ( !parent.mkdirs() && !parent.isDirectory() ) {
                        throw new IOException (
                            sm.getString ( "expandWar.createFailed", parent ) );
                    }
                }
                if ( name.endsWith ( "/" ) ) {
                    continue;
                }
                try ( InputStream input = jarFile.getInputStream ( jarEntry ) ) {
                    if ( null == input ) {
                        throw new ZipException ( sm.getString ( "expandWar.missingJarEntry",
                                                                jarEntry.getName() ) );
                    }
                    expand ( input, expandedFile );
                    long lastModified = jarEntry.getTime();
                    if ( ( lastModified != -1 ) && ( lastModified != 0 ) ) {
                        expandedFile.setLastModified ( lastModified );
                    }
                }
            }
            warTracker.createNewFile();
            warTracker.setLastModified ( warLastModified );
            success = true;
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( !success ) {
                deleteDir ( docBase );
            }
        }
        return docBase.getAbsolutePath();
    }
    public static void validate ( Host host, URL war, String pathname ) throws IOException {
        File docBase = new File ( host.getAppBaseFile(), pathname );
        String canonicalDocBasePrefix = docBase.getCanonicalPath();
        if ( !canonicalDocBasePrefix.endsWith ( File.separator ) ) {
            canonicalDocBasePrefix += File.separator;
        }
        JarURLConnection juc = ( JarURLConnection ) war.openConnection();
        juc.setUseCaches ( false );
        try ( JarFile jarFile = juc.getJarFile() ) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while ( jarEntries.hasMoreElements() ) {
                JarEntry jarEntry = jarEntries.nextElement();
                String name = jarEntry.getName();
                File expandedFile = new File ( docBase, name );
                if ( !expandedFile.getCanonicalPath().startsWith (
                            canonicalDocBasePrefix ) ) {
                    throw new IllegalArgumentException (
                        sm.getString ( "expandWar.illegalPath", war, name,
                                       expandedFile.getCanonicalPath(),
                                       canonicalDocBasePrefix ) );
                }
            }
        } catch ( IOException e ) {
            throw e;
        }
    }
    public static boolean copy ( File src, File dest ) {
        boolean result = true;
        String files[] = null;
        if ( src.isDirectory() ) {
            files = src.list();
            result = dest.mkdir();
        } else {
            files = new String[1];
            files[0] = "";
        }
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; ( i < files.length ) && result; i++ ) {
            File fileSrc = new File ( src, files[i] );
            File fileDest = new File ( dest, files[i] );
            if ( fileSrc.isDirectory() ) {
                result = copy ( fileSrc, fileDest );
            } else {
                try ( FileChannel ic = ( new FileInputStream ( fileSrc ) ).getChannel();
                            FileChannel oc = ( new FileOutputStream ( fileDest ) ).getChannel() ) {
                    ic.transferTo ( 0, ic.size(), oc );
                } catch ( IOException e ) {
                    log.error ( sm.getString ( "expandWar.copy", fileSrc, fileDest ), e );
                    result = false;
                }
            }
        }
        return result;
    }
    public static boolean delete ( File dir ) {
        return delete ( dir, true );
    }
    public static boolean delete ( File dir, boolean logFailure ) {
        boolean result;
        if ( dir.isDirectory() ) {
            result = deleteDir ( dir, logFailure );
        } else {
            if ( dir.exists() ) {
                result = dir.delete();
            } else {
                result = true;
            }
        }
        if ( logFailure && !result ) {
            log.error ( sm.getString (
                            "expandWar.deleteFailed", dir.getAbsolutePath() ) );
        }
        return result;
    }
    public static boolean deleteDir ( File dir ) {
        return deleteDir ( dir, true );
    }
    public static boolean deleteDir ( File dir, boolean logFailure ) {
        String files[] = dir.list();
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; i < files.length; i++ ) {
            File file = new File ( dir, files[i] );
            if ( file.isDirectory() ) {
                deleteDir ( file, logFailure );
            } else {
                file.delete();
            }
        }
        boolean result;
        if ( dir.exists() ) {
            result = dir.delete();
        } else {
            result = true;
        }
        if ( logFailure && !result ) {
            log.error ( sm.getString (
                            "expandWar.deleteFailed", dir.getAbsolutePath() ) );
        }
        return result;
    }
    private static void expand ( InputStream input, File file ) throws IOException {
        try ( BufferedOutputStream output =
                        new BufferedOutputStream ( new FileOutputStream ( file ) ) ) {
            byte buffer[] = new byte[2048];
            while ( true ) {
                int n = input.read ( buffer );
                if ( n <= 0 ) {
                    break;
                }
                output.write ( buffer, 0, n );
            }
        }
    }
}
