package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.security.AccessController;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.net.URL;
import java.util.LinkedHashSet;
import java.io.File;
import org.apache.juli.logging.Log;
public final class ClassLoaderFactory {
    private static final Log log;
    public static ClassLoader createClassLoader ( final File[] unpacked, final File[] packed, final ClassLoader parent ) throws Exception {
        if ( ClassLoaderFactory.log.isDebugEnabled() ) {
            ClassLoaderFactory.log.debug ( "Creating new class loader" );
        }
        final Set<URL> set = new LinkedHashSet<URL>();
        if ( unpacked != null ) {
            for ( int i = 0; i < unpacked.length; ++i ) {
                File file = unpacked[i];
                if ( file.canRead() ) {
                    file = new File ( file.getCanonicalPath() + File.separator );
                    final URL url = file.toURI().toURL();
                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                        ClassLoaderFactory.log.debug ( "  Including directory " + url );
                    }
                    set.add ( url );
                }
            }
        }
        if ( packed != null ) {
            for ( int i = 0; i < packed.length; ++i ) {
                final File directory = packed[i];
                if ( directory.isDirectory() ) {
                    if ( directory.canRead() ) {
                        final String[] filenames = directory.list();
                        if ( filenames != null ) {
                            for ( int j = 0; j < filenames.length; ++j ) {
                                final String filename = filenames[j].toLowerCase ( Locale.ENGLISH );
                                if ( filename.endsWith ( ".jar" ) ) {
                                    final File file2 = new File ( directory, filenames[j] );
                                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                                        ClassLoaderFactory.log.debug ( "  Including jar file " + file2.getAbsolutePath() );
                                    }
                                    final URL url2 = file2.toURI().toURL();
                                    set.add ( url2 );
                                }
                            }
                        }
                    }
                }
            }
        }
        final URL[] array = set.toArray ( new URL[set.size()] );
        return AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                if ( parent == null ) {
                    return new URLClassLoader ( array );
                }
                return new URLClassLoader ( array, parent );
            }
        } );
    }
    public static ClassLoader createClassLoader ( final List<Repository> repositories, final ClassLoader parent ) throws Exception {
        if ( ClassLoaderFactory.log.isDebugEnabled() ) {
            ClassLoaderFactory.log.debug ( "Creating new class loader" );
        }
        final Set<URL> set = new LinkedHashSet<URL>();
        if ( repositories != null ) {
            for ( final Repository repository : repositories ) {
                if ( repository.getType() == RepositoryType.URL ) {
                    final URL url = buildClassLoaderUrl ( repository.getLocation() );
                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                        ClassLoaderFactory.log.debug ( "  Including URL " + url );
                    }
                    set.add ( url );
                } else if ( repository.getType() == RepositoryType.DIR ) {
                    File directory = new File ( repository.getLocation() );
                    directory = directory.getCanonicalFile();
                    if ( !validateFile ( directory, RepositoryType.DIR ) ) {
                        continue;
                    }
                    final URL url2 = buildClassLoaderUrl ( directory );
                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                        ClassLoaderFactory.log.debug ( "  Including directory " + url2 );
                    }
                    set.add ( url2 );
                } else if ( repository.getType() == RepositoryType.JAR ) {
                    File file = new File ( repository.getLocation() );
                    file = file.getCanonicalFile();
                    if ( !validateFile ( file, RepositoryType.JAR ) ) {
                        continue;
                    }
                    final URL url2 = buildClassLoaderUrl ( file );
                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                        ClassLoaderFactory.log.debug ( "  Including jar file " + url2 );
                    }
                    set.add ( url2 );
                } else {
                    if ( repository.getType() != RepositoryType.GLOB ) {
                        continue;
                    }
                    File directory = new File ( repository.getLocation() );
                    directory = directory.getCanonicalFile();
                    if ( !validateFile ( directory, RepositoryType.GLOB ) ) {
                        continue;
                    }
                    if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                        ClassLoaderFactory.log.debug ( "  Including directory glob " + directory.getAbsolutePath() );
                    }
                    final String[] filenames = directory.list();
                    if ( filenames == null ) {
                        continue;
                    }
                    for ( int j = 0; j < filenames.length; ++j ) {
                        final String filename = filenames[j].toLowerCase ( Locale.ENGLISH );
                        if ( filename.endsWith ( ".jar" ) ) {
                            File file2 = new File ( directory, filenames[j] );
                            file2 = file2.getCanonicalFile();
                            if ( validateFile ( file2, RepositoryType.JAR ) ) {
                                if ( ClassLoaderFactory.log.isDebugEnabled() ) {
                                    ClassLoaderFactory.log.debug ( "    Including glob jar file " + file2.getAbsolutePath() );
                                }
                                final URL url3 = buildClassLoaderUrl ( file2 );
                                set.add ( url3 );
                            }
                        }
                    }
                }
            }
        }
        final URL[] array = set.toArray ( new URL[set.size()] );
        if ( ClassLoaderFactory.log.isDebugEnabled() ) {
            for ( int i = 0; i < array.length; ++i ) {
                ClassLoaderFactory.log.debug ( "  location " + i + " is " + array[i] );
            }
        }
        return AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                if ( parent == null ) {
                    return new URLClassLoader ( array );
                }
                return new URLClassLoader ( array, parent );
            }
        } );
    }
    private static boolean validateFile ( final File file, final RepositoryType type ) throws IOException {
        if ( RepositoryType.DIR == type || RepositoryType.GLOB == type ) {
            if ( !file.isDirectory() || !file.canRead() ) {
                final String msg = "Problem with directory [" + file + "], exists: [" + file.exists() + "], isDirectory: [" + file.isDirectory() + "], canRead: [" + file.canRead() + "]";
                File home = new File ( Bootstrap.getCatalinaHome() );
                home = home.getCanonicalFile();
                File base = new File ( Bootstrap.getCatalinaBase() );
                base = base.getCanonicalFile();
                final File defaultValue = new File ( base, "lib" );
                if ( !home.getPath().equals ( base.getPath() ) && file.getPath().equals ( defaultValue.getPath() ) && !file.exists() ) {
                    ClassLoaderFactory.log.debug ( msg );
                } else {
                    ClassLoaderFactory.log.warn ( msg );
                }
                return false;
            }
        } else if ( RepositoryType.JAR == type && !file.canRead() ) {
            ClassLoaderFactory.log.warn ( "Problem with JAR file [" + file + "], exists: [" + file.exists() + "], canRead: [" + file.canRead() + "]" );
            return false;
        }
        return true;
    }
    private static URL buildClassLoaderUrl ( final String urlString ) throws MalformedURLException {
        final String result = urlString.replaceAll ( "!/", "%21/" );
        return new URL ( result );
    }
    private static URL buildClassLoaderUrl ( final File file ) throws MalformedURLException {
        String fileUrlString = file.toURI().toString();
        fileUrlString = fileUrlString.replaceAll ( "!/", "%21/" );
        return new URL ( fileUrlString );
    }
    static {
        log = LogFactory.getLog ( ClassLoaderFactory.class );
    }
    public enum RepositoryType {
        DIR,
        GLOB,
        JAR,
        URL;
    }
    public static class Repository {
        private final String location;
        private final RepositoryType type;
        public Repository ( final String location, final RepositoryType type ) {
            this.location = location;
            this.type = type;
        }
        public String getLocation() {
            return this.location;
        }
        public RepositoryType getType() {
            return this.type;
        }
    }
}
