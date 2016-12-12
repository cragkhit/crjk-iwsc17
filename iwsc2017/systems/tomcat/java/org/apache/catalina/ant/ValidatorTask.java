package org.apache.catalina.ant;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.catalina.Globals;
import org.apache.catalina.startup.Constants;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tools.ant.BuildException;
import org.xml.sax.InputSource;
public class ValidatorTask extends BaseRedirectorHelperTask {
    protected String path = null;
    public String getPath() {
        return ( this.path );
    }
    public void setPath ( String path ) {
        this.path = path;
    }
    @Override
    public void execute() throws BuildException {
        if ( path == null ) {
            throw new BuildException ( "Must specify 'path'" );
        }
        File file = new File ( path, Constants.ApplicationWebXml );
        if ( !file.canRead() ) {
            throw new BuildException ( "Cannot find web.xml" );
        }
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader
        ( ValidatorTask.class.getClassLoader() );
        Digester digester = DigesterFactory.newDigester (
                                true, true, null, Globals.IS_SECURITY_ENABLED );
        try ( InputStream stream = new BufferedInputStream ( new FileInputStream ( file.getCanonicalFile() ) ); ) {
            InputSource is = new InputSource ( file.toURI().toURL().toExternalForm() );
            is.setByteStream ( stream );
            digester.parse ( is );
            handleOutput ( "web.xml validated" );
        } catch ( Exception e ) {
            if ( isFailOnError() ) {
                throw new BuildException ( "Validation failure", e );
            } else {
                handleErrorOutput ( "Validation failure: " + e );
            }
        } finally {
            Thread.currentThread().setContextClassLoader ( oldCL );
            closeRedirector();
        }
    }
}
