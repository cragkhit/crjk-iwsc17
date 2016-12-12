package org.apache.catalina.webresources;
import java.util.jar.JarEntry;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class WarResource extends AbstractSingleArchiveResource {
    private static final Log log = LogFactory.getLog ( WarResource.class );
    public WarResource ( AbstractArchiveResourceSet archiveResourceSet, String webAppPath,
                         String baseUrl, JarEntry jarEntry ) {
        super ( archiveResourceSet, webAppPath, "war:" + baseUrl + "*/", jarEntry, baseUrl );
    }
    @Override
    protected Log getLog() {
        return log;
    }
}
