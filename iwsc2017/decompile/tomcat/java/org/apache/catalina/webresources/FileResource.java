package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.security.cert.Certificate;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.charset.StandardCharsets;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.catalina.WebResourceRoot;
import java.util.jar.Manifest;
import java.io.File;
import org.apache.juli.logging.Log;
public class FileResource extends AbstractResource {
    private static final Log log;
    private static final boolean PROPERTIES_NEED_CONVERT;
    private final File resource;
    private final String name;
    private final boolean readOnly;
    private final Manifest manifest;
    private final boolean needConvert;
    public FileResource ( final WebResourceRoot root, final String webAppPath, final File resource, final boolean readOnly, final Manifest manifest ) {
        super ( root, webAppPath );
        this.resource = resource;
        if ( webAppPath.charAt ( webAppPath.length() - 1 ) == '/' ) {
            final String realName = resource.getName() + '/';
            if ( webAppPath.endsWith ( realName ) ) {
                this.name = resource.getName();
            } else {
                final int endOfName = webAppPath.length() - 1;
                this.name = webAppPath.substring ( webAppPath.lastIndexOf ( 47, endOfName - 1 ) + 1, endOfName );
            }
        } else {
            this.name = resource.getName();
        }
        this.readOnly = readOnly;
        this.manifest = manifest;
        this.needConvert = ( FileResource.PROPERTIES_NEED_CONVERT && this.name.endsWith ( ".properties" ) );
    }
    @Override
    public long getLastModified() {
        return this.resource.lastModified();
    }
    @Override
    public boolean exists() {
        return this.resource.exists();
    }
    @Override
    public boolean isVirtual() {
        return false;
    }
    @Override
    public boolean isDirectory() {
        return this.resource.isDirectory();
    }
    @Override
    public boolean isFile() {
        return this.resource.isFile();
    }
    @Override
    public boolean delete() {
        return !this.readOnly && this.resource.delete();
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public long getContentLength() {
        return this.getContentLengthInternal ( this.needConvert );
    }
    private long getContentLengthInternal ( final boolean convert ) {
        if ( convert ) {
            final byte[] content = this.getContent();
            if ( content == null ) {
                return -1L;
            }
            return content.length;
        } else {
            if ( this.isDirectory() ) {
                return -1L;
            }
            return this.resource.length();
        }
    }
    @Override
    public String getCanonicalPath() {
        try {
            return this.resource.getCanonicalPath();
        } catch ( IOException ioe ) {
            if ( FileResource.log.isDebugEnabled() ) {
                FileResource.log.debug ( FileResource.sm.getString ( "fileResource.getCanonicalPathFail", this.resource.getPath() ), ioe );
            }
            return null;
        }
    }
    @Override
    public boolean canRead() {
        return this.resource.canRead();
    }
    @Override
    protected InputStream doGetInputStream() {
        if ( this.needConvert ) {
            final byte[] content = this.getContent();
            if ( content == null ) {
                return null;
            }
            return new ByteArrayInputStream ( content );
        } else {
            try {
                return new FileInputStream ( this.resource );
            } catch ( FileNotFoundException fnfe ) {
                return null;
            }
        }
    }
    @Override
    public final byte[] getContent() {
        final long len = this.getContentLengthInternal ( false );
        if ( len > 2147483647L ) {
            throw new ArrayIndexOutOfBoundsException ( FileResource.sm.getString ( "abstractResource.getContentTooLarge", this.getWebappPath(), len ) );
        }
        if ( len < 0L ) {
            return null;
        }
        final int size = ( int ) len;
        byte[] result = new byte[size];
        int pos = 0;
        try ( final InputStream is = new FileInputStream ( this.resource ) ) {
            while ( pos < size ) {
                final int n = is.read ( result, pos, size - pos );
                if ( n < 0 ) {
                    break;
                }
                pos += n;
            }
        } catch ( IOException ioe ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( FileResource.sm.getString ( "abstractResource.getContentFail", this.getWebappPath() ), ioe );
            }
            return null;
        }
        if ( this.needConvert ) {
            final String str = new String ( result );
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
            final BasicFileAttributes attrs = Files.readAttributes ( this.resource.toPath(), BasicFileAttributes.class, new LinkOption[0] );
            return attrs.creationTime().toMillis();
        } catch ( IOException e ) {
            if ( FileResource.log.isDebugEnabled() ) {
                FileResource.log.debug ( FileResource.sm.getString ( "fileResource.getCreationFail", this.resource.getPath() ), e );
            }
            return 0L;
        }
    }
    @Override
    public URL getURL() {
        if ( this.resource.exists() ) {
            try {
                return this.resource.toURI().toURL();
            } catch ( MalformedURLException e ) {
                if ( FileResource.log.isDebugEnabled() ) {
                    FileResource.log.debug ( FileResource.sm.getString ( "fileResource.getUrlFail", this.resource.getPath() ), e );
                }
                return null;
            }
        }
        return null;
    }
    @Override
    public URL getCodeBase() {
        if ( this.getWebappPath().startsWith ( "/WEB-INF/classes/" ) && this.name.endsWith ( ".class" ) ) {
            return this.getWebResourceRoot().getResource ( "/WEB-INF/classes/" ).getURL();
        }
        return this.getURL();
    }
    @Override
    public Certificate[] getCertificates() {
        return null;
    }
    @Override
    public Manifest getManifest() {
        return this.manifest;
    }
    @Override
    protected Log getLog() {
        return FileResource.log;
    }
    static {
        log = LogFactory.getLog ( FileResource.class );
        boolean isEBCDIC = false;
        try {
            final String encoding = System.getProperty ( "file.encoding" );
            if ( encoding.indexOf ( "EBCDIC" ) != -1 ) {
                isEBCDIC = true;
            }
        } catch ( SecurityException ex ) {}
        PROPERTIES_NEED_CONVERT = isEBCDIC;
    }
}
