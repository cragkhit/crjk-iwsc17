package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.util.jar.JarEntry;
import org.apache.juli.logging.Log;
public class JarResource extends AbstractSingleArchiveResource {
    private static final Log log;
    public JarResource ( final AbstractArchiveResourceSet archiveResourceSet, final String webAppPath, final String baseUrl, final JarEntry jarEntry ) {
        super ( archiveResourceSet, webAppPath, "jar:" + baseUrl + "!/", jarEntry, baseUrl );
    }
    @Override
    protected Log getLog() {
        return JarResource.log;
    }
    static {
        log = LogFactory.getLog ( JarResource.class );
    }
}
