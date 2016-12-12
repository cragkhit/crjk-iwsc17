package org.apache.catalina.webresources;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.Certificate;
import java.util.jar.Manifest;
import org.apache.catalina.WebResourceRoot;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class FileResource extends AbstractResource {
    private static final Log log = LogFactory.getLog ( FileResource.class );
    private static final boolean PROPERTIES_NEED_CONVERT;
    static {
        boolean isEBCDIC = false;
        try {
            String encoding = System.getProperty ( "file.encoding" );
            if ( encoding.indexOf ( "EBCDIC" ) != -1 ) {
                isEBCDIC = true;
            }
        } catch ( SecurityException e ) {
        }
        PROPERTIES_NEED_CONVERT = isEBCDIC;
    }
    private final File resource;
    private final String name;
    private final boolean readOnly;
    private final Manifest manifest;
    private final boolean needConvert;
    public FileResource ( WebResourceRoot root, String webAppPath,
                          File resource, boolean readOnly, Manifest manifest ) {
        super ( root, webAppPath );
        this.resource = resource;
        if ( webAppPath.charAt ( webAppPath.length() - 1 ) == '/' ) {
            String realName = resource.getName() + '/';
            if ( webAppPath.endsWith ( realName ) ) {
                name = resource.getName();
            } else {
                int endOfName = webAppPath.length() - 1;
                name = webAppPath.substring (
                           webAppPath.lastIndexOf ( '/', endOfName - 1 ) + 1,
                           endOfName );
            }
        } else {
            name = resource.getName();
        }
        this.readOnly = readOnly;
        this.manifest = manifest;
        this.needConvert = PROPERTIES_NEED_CONVERT && name.endsWith ( ".properties" );
    }
    @Override
    public long getLastModified() {
        return resource.lastModified();
    }
    @Override
    public boolean exists() {
        return resource.exists();
    }
    @Override
    public boolean isVirtual() {
        return false;
    }
    @Override
    public boolean isDirectory() {
        return resource.isDirectory();
    }
    @Override
    public boolean isFile() {
        return resource.isFile();
    }
    @Override
    public boolean delete() {
        if ( readOnly ) {
            return false;
        }
        return resource.delete();
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public long getContentLength() {
        return getContentLengthInternal ( needConvert );
    }
    private long getContentLengthInternal ( boolean convert ) {
        if ( convert ) {
            byte[] content = getContent();
            if ( content == null ) {
                return -1;
            } else {
                return content.length;
            }
        }
        if ( isDirectory() ) {
            return -1;
        }
        return resource.length();
    }
    @Override
    public String getCanonicalPath() {
        try {
            return resource.getCanonicalPath();
        } catch ( IOException ioe ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "fileResource.getCanonicalPathFail",
                                           resource.getPath() ), ioe );
            }
            return null;
        }
    }
    @Override
    public boolean canRead() {
        return resource.canRead();
    }
    @Override
    protected InputStream doGetInputStream() {
        if ( needConvert ) {
            byte[] content = getContent();
            if ( content == null ) {
                return null;
            } else {
                return new ByteArrayInputStream ( content );
            }
        }
        try {
            return new FileInputStream ( resource );
        } catch ( FileNotFoundException fnfe ) {
            return null;
        }
    }
    @Override
    public final byte[] getContent() {
        long len = getContentLengthInternal ( false );
        if ( len > Integer.MAX_VALUE ) {
            throw new ArrayIndexOutOfBoundsException ( sm.getString (
                        "abstractResource.getContentTooLarge", getWebappPath(),
                        Long.valueOf ( len ) ) );
        }
        if ( len < 0 ) {
            return null;
        }
        int size = ( int ) len;
        byte[] result = new byte[size];
        int pos = 0;
        try ( InputStream is = new FileInputStream ( resource ) ) {
            while ( pos < size ) {
                int n = is.read ( result, pos, size - pos );
                if ( n < 0 ) {
                    break;
                }
                pos += n;
            }
        } catch ( IOException ioe ) {
            if ( getLog().isDebugEnabled() ) {
                getLog().debug ( sm.getString ( "abstractResource.getContentFail",
                                                getWebappPath() ), ioe );
            }
            return null;
        }
        if ( needConvert ) {
            String str = new String ( result );
            try {
                result = str.getBytes ( StandardCharsets.UTF_8 );
            } catch ( Exception e ) {
                result = null;
            }
        }
        return result;
    }
    @Override
    public long getCreation() {
        try {
            BasicFileAttributes attrs = Files.readAttributes ( resource.toPath(),
                                        BasicFileAttributes.class );
            return attrs.creationTime().toMillis();
        } catch ( IOException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "fileResource.getCreationFail",
                                           resource.getPath() ), e );
            }
            return 0;
        }
    }
    @Override
    public URL getURL() {
        if ( resource.exists() ) {
            try {
                return resource.toURI().toURL();
            } catch ( MalformedURLException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "fileResource.getUrlFail",
                                               resource.getPath() ), e );
                }
                return null;
            }
        } else {
            return null;
        }
    }
    @Override
    public URL getCodeBase() {
        if ( getWebappPath().startsWith ( "/WEB-INF/classes/" ) && name.endsWith ( ".class" ) ) {
            return getWebResourceRoot().getResource ( "/WEB-INF/classes/" ).getURL();
        } else {
            return getURL();
        }
    }
    @Override
    public Certificate[] getCertificates() {
        return null;
    }
    @Override
    public Manifest getManifest() {
        return manifest;
    }
    @Override
    protected Log getLog() {
        return log;
    }
}