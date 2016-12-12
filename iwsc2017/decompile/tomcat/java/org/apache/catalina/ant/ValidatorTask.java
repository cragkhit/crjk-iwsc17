package org.apache.catalina.ant;
import org.apache.tomcat.util.digester.Digester;
import org.xml.sax.InputSource;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.catalina.Globals;
import java.io.File;
import org.apache.tools.ant.BuildException;
public class ValidatorTask extends BaseRedirectorHelperTask {
    protected String path;
    public ValidatorTask() {
        this.path = null;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath ( final String path ) {
        this.path = path;
    }
    public void execute() throws BuildException {
        if ( this.path == null ) {
            throw new BuildException ( "Must specify 'path'" );
        }
        final File file = new File ( this.path, "/WEB-INF/web.xml" );
        if ( !file.canRead() ) {
            throw new BuildException ( "Cannot find web.xml" );
        }
        final ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader ( ValidatorTask.class.getClassLoader() );
        final Digester digester = DigesterFactory.newDigester ( true, true, null, Globals.IS_SECURITY_ENABLED );
        try ( final InputStream stream = new BufferedInputStream ( new FileInputStream ( file.getCanonicalFile() ) ) ) {
            final InputSource is = new InputSource ( file.toURI().toURL().toExternalForm() );
            is.setByteStream ( stream );
            digester.parse ( is );
            this.handleOutput ( "web.xml validated" );
        } catch ( Exception e ) {
            if ( this.isFailOnError() ) {
                throw new BuildException ( "Validation failure", ( Throwable ) e );
            }
            this.handleErrorOutput ( "Validation failure: " + e );
        } finally {
            Thread.currentThread().setContextClassLoader ( oldCL );
            this.closeRedirector();
        }
    }
}
