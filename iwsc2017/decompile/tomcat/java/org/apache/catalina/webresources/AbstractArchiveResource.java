package org.apache.catalina.webresources;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
public abstract class AbstractArchiveResource extends AbstractResource {
    private final AbstractArchiveResourceSet archiveResourceSet;
    private final String baseUrl;
    private final JarEntry resource;
    private final String codeBaseUrl;
    private final String name;
    private boolean readCerts;
    private Certificate[] certificates;
    protected AbstractArchiveResource ( final AbstractArchiveResourceSet archiveResourceSet, final String webAppPath, final String baseUrl, final JarEntry jarEntry, final String codeBaseUrl ) {
        super ( archiveResourceSet.getRoot(), webAppPath );
        this.readCerts = false;
        this.archiveResourceSet = archiveResourceSet;
        this.baseUrl = baseUrl;
        this.resource = jarEntry;
        this.codeBaseUrl = codeBaseUrl;
        String resourceName = this.resource.getName();
        if ( resourceName.charAt ( resourceName.length() - 1 ) == '/' ) {
            resourceName = resourceName.substring ( 0, resourceName.length() - 1 );
        }
        final String internalPath = archiveResourceSet.getInternalPath();
        if ( internalPath.length() > 0 && resourceName.equals ( internalPath.subSequence ( 1, internalPath.length() ) ) ) {
            this.name = "";
        } else {
            final int index = resourceName.lastIndexOf ( 47 );
            if ( index == -1 ) {
                this.name = resourceName;
            } else {
                this.name = resourceName.substring ( index + 1 );
            }
        }
    }
    protected AbstractArchiveResourceSet getArchiveResourceSet() {
        return this.archiveResourceSet;
    }
    protected final String getBase() {
        return this.archiveResourceSet.getBase();
    }
    protected final String getBaseUrl() {
        return this.baseUrl;
    }
    protected final JarEntry getResource() {
        return this.resource;
    }
    @Override
    public long getLastModified() {
        return this.resource.getTime();
    }
    @Override
    public boolean exists() {
        return true;
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
        return !this.resource.isDirectory();
    }
    @Override
    public boolean delete() {
        return false;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public long getContentLength() {
        if ( this.isDirectory() ) {
            return -1L;
        }
        return this.resource.getSize();
    }
    @Override
    public String getCanonicalPath() {
        return null;
    }
    @Override
    public boolean canRead() {
        return true;
    }
    @Override
    public long getCreation() {
        return this.resource.getTime();
    }
    @Override
    public URL getURL() {
        final String url = this.baseUrl + this.resource.getName();
        try {
            return new URL ( url );
        } catch ( MalformedURLException e ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractArchiveResource.sm.getString ( "fileResource.getUrlFail", url ), e );
            }
            return null;
        }
    }
    @Override
    public URL getCodeBase() {
        try {
            return new URL ( this.codeBaseUrl );
        } catch ( MalformedURLException e ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractArchiveResource.sm.getString ( "fileResource.getUrlFail", this.codeBaseUrl ), e );
            }
            return null;
        }
    }
    @Override
    public final byte[] getContent() {
        final long len = this.getContentLength();
        if ( len > 2147483647L ) {
            throw new ArrayIndexOutOfBoundsException ( AbstractArchiveResource.sm.getString ( "abstractResource.getContentTooLarge", this.getWebappPath(), len ) );
        }
        if ( len < 0L ) {
            return null;
        }
        final int size = ( int ) len;
        final byte[] result = new byte[size];
        int pos = 0;
        try ( final JarInputStreamWrapper jisw = this.getJarInputStreamWrapper() ) {
            if ( jisw == null ) {
                return null;
            }
            while ( pos < size ) {
                final int n = jisw.read ( result, pos, size - pos );
                if ( n < 0 ) {
                    break;
                }
                pos += n;
            }
            this.certificates = jisw.getCertificates();
            this.readCerts = true;
        } catch ( IOException ioe ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractArchiveResource.sm.getString ( "abstractResource.getContentFail", this.getWebappPath() ), ioe );
            }
            return null;
        }
        return result;
    }
    @Override
    public Certificate[] getCertificates() {
        if ( !this.readCerts ) {
            throw new IllegalStateException();
        }
        return this.certificates;
    }
    @Override
    public Manifest getManifest() {
        return this.archiveResourceSet.getManifest();
    }
    @Override
    protected final InputStream doGetInputStream() {
        if ( this.isDirectory() ) {
            return null;
        }
        return this.getJarInputStreamWrapper();
    }
    protected abstract JarInputStreamWrapper getJarInputStreamWrapper();
    protected class JarInputStreamWrapper extends InputStream {
        private final JarEntry jarEntry;
        private final InputStream is;
        public JarInputStreamWrapper ( final JarEntry jarEntry, final InputStream is ) {
            this.jarEntry = jarEntry;
            this.is = is;
        }
        @Override
        public int read() throws IOException {
            return this.is.read();
        }
        @Override
        public int read ( final byte[] b ) throws IOException {
            return this.is.read ( b );
        }
        @Override
        public int read ( final byte[] b, final int off, final int len ) throws IOException {
            return this.is.read ( b, off, len );
        }
        @Override
        public long skip ( final long n ) throws IOException {
            return this.is.skip ( n );
        }
        @Override
        public int available() throws IOException {
            return this.is.available();
        }
        @Override
        public void close() throws IOException {
            AbstractArchiveResource.this.archiveResourceSet.closeJarFile();
        }
        @Override
        public synchronized void mark ( final int readlimit ) {
            this.is.mark ( readlimit );
        }
        @Override
        public synchronized void reset() throws IOException {
            this.is.reset();
        }
        @Override
        public boolean markSupported() {
            return this.is.markSupported();
        }
        public Certificate[] getCertificates() {
            return this.jarEntry.getCertificates();
        }
    }
}
