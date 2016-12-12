package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.catalina.deploy.NamingResourcesImpl;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class NamingResourcesSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
        if ( elementDesc != null ) {
            if ( NamingResourcesSF.log.isDebugEnabled() ) {
                NamingResourcesSF.log.debug ( "store " + elementDesc.getTag() + "( " + aElement + " )" );
            }
            this.storeChildren ( aWriter, indent, aElement, elementDesc );
        } else if ( NamingResourcesSF.log.isWarnEnabled() ) {
            NamingResourcesSF.log.warn ( "Descriptor for element" + aElement.getClass() + " not configured!" );
        }
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aElement, final StoreDescription elementDesc ) throws Exception {
        if ( aElement instanceof NamingResourcesImpl ) {
            final NamingResourcesImpl resources = ( NamingResourcesImpl ) aElement;
            final ContextEjb[] ejbs = resources.findEjbs();
            this.storeElementArray ( aWriter, indent, ejbs );
            final ContextEnvironment[] envs = resources.findEnvironments();
            this.storeElementArray ( aWriter, indent, envs );
            final ContextLocalEjb[] lejbs = resources.findLocalEjbs();
            this.storeElementArray ( aWriter, indent, lejbs );
            final ContextResource[] dresources = resources.findResources();
            this.storeElementArray ( aWriter, indent, dresources );
            final ContextResourceEnvRef[] resEnv = resources.findResourceEnvRefs();
            this.storeElementArray ( aWriter, indent, resEnv );
            final ContextResourceLink[] resourceLinks = resources.findResourceLinks();
            this.storeElementArray ( aWriter, indent, resourceLinks );
        }
    }
    static {
        NamingResourcesSF.log = LogFactory.getLog ( NamingResourcesSF.class );
    }
}
