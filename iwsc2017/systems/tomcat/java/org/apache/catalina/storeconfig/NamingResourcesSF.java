package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
public class NamingResourcesSF extends StoreFactoryBase {
    private static Log log = LogFactory.getLog ( NamingResourcesSF.class );
    @Override
    public void store ( PrintWriter aWriter, int indent, Object aElement )
    throws Exception {
        StoreDescription elementDesc = getRegistry().findDescription (
                                           aElement.getClass() );
        if ( elementDesc != null ) {
            if ( log.isDebugEnabled() )
                log.debug ( "store " + elementDesc.getTag() + "( " + aElement
                            + " )" );
            storeChildren ( aWriter, indent, aElement, elementDesc );
        } else {
            if ( log.isWarnEnabled() )
                log.warn ( "Descriptor for element" + aElement.getClass()
                           + " not configured!" );
        }
    }
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aElement,
                                StoreDescription elementDesc ) throws Exception {
        if ( aElement instanceof NamingResourcesImpl ) {
            NamingResourcesImpl resources = ( NamingResourcesImpl ) aElement;
            ContextEjb[] ejbs = resources.findEjbs();
            storeElementArray ( aWriter, indent, ejbs );
            ContextEnvironment[] envs = resources.findEnvironments();
            storeElementArray ( aWriter, indent, envs );
            ContextLocalEjb[] lejbs = resources.findLocalEjbs();
            storeElementArray ( aWriter, indent, lejbs );
            ContextResource[] dresources = resources.findResources();
            storeElementArray ( aWriter, indent, dresources );
            ContextResourceEnvRef[] resEnv = resources.findResourceEnvRefs();
            storeElementArray ( aWriter, indent, resEnv );
            ContextResourceLink[] resourceLinks = resources.findResourceLinks();
            storeElementArray ( aWriter, indent, resourceLinks );
        }
    }
}
