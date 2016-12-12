package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.util.jar.JarEntry;
import org.apache.juli.logging.Log;
public class WarResource extends AbstractSingleArchiveResource {
    private static final Log log;
    public WarResource ( final AbstractArchiveResourceSet archiveResourceSet, final String webAppPath, final String baseUrl, final JarEntry jarEntry ) {
        super ( archiveResourceSet, webAppPath, "war:" + baseUrl + "*/", jarEntry, baseUrl );
    }
    @Override
    protected Log getLog() {
        return WarResource.log;
    }
    static {
        log = LogFactory.getLog ( WarResource.class );
    }
}
