package org.apache.catalina.webresources;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.jar.JarEntry;
public abstract class AbstractSingleArchiveResource extends AbstractArchiveResource {
    protected AbstractSingleArchiveResource ( final AbstractArchiveResourceSet archiveResourceSet, final String webAppPath, final String baseUrl, final JarEntry jarEntry, final String codeBaseUrl ) {
        super ( archiveResourceSet, webAppPath, baseUrl, jarEntry, codeBaseUrl );
    }
    @Override
    protected JarInputStreamWrapper getJarInputStreamWrapper() {
        JarFile jarFile = null;
        try {
            jarFile = this.getArchiveResourceSet().openJarFile();
            final JarEntry jarEntry = jarFile.getJarEntry ( this.getResource().getName() );
            final InputStream is = jarFile.getInputStream ( jarEntry );
            return new JarInputStreamWrapper ( this, jarEntry, is );
        } catch ( IOException e ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( AbstractSingleArchiveResource.sm.getString ( "jarResource.getInputStreamFail", this.getResource().getName(), this.getBaseUrl() ), e );
            }
            if ( jarFile != null ) {
                this.getArchiveResourceSet().closeJarFile();
            }
            return null;
        }
    }
}
