package org.apache.catalina.mbeans;
import java.util.ArrayList;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
public class NamingResourcesMBean extends BaseModelMBean {
    public NamingResourcesMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    protected final Registry registry = MBeanUtils.createRegistry();
    protected final ManagedBean managed =
        registry.findManagedBean ( "NamingResources" );
    public String[] getEnvironments() {
        ContextEnvironment[] envs =
            ( ( NamingResourcesImpl ) this.resource ).findEnvironments();
        ArrayList<String> results = new ArrayList<>();
        for ( int i = 0; i < envs.length; i++ ) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName ( managed.getDomain(), envs[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                IllegalArgumentException iae = new IllegalArgumentException
                ( "Cannot create object name for environment " + envs[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getResources() {
        ContextResource[] resources =
            ( ( NamingResourcesImpl ) this.resource ).findResources();
        ArrayList<String> results = new ArrayList<>();
        for ( int i = 0; i < resources.length; i++ ) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName ( managed.getDomain(), resources[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                IllegalArgumentException iae = new IllegalArgumentException
                ( "Cannot create object name for resource " + resources[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getResourceLinks() {
        ContextResourceLink[] resourceLinks =
            ( ( NamingResourcesImpl ) this.resource ).findResourceLinks();
        ArrayList<String> results = new ArrayList<>();
        for ( int i = 0; i < resourceLinks.length; i++ ) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName ( managed.getDomain(), resourceLinks[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                IllegalArgumentException iae = new IllegalArgumentException
                ( "Cannot create object name for resource " + resourceLinks[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String addEnvironment ( String envName, String type, String value )
    throws MalformedObjectNameException {
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextEnvironment env = nresources.findEnvironment ( envName );
        if ( env != null ) {
            throw new IllegalArgumentException
            ( "Invalid environment name - already exists '" + envName + "'" );
        }
        env = new ContextEnvironment();
        env.setName ( envName );
        env.setType ( type );
        env.setValue ( value );
        nresources.addEnvironment ( env );
        ManagedBean managed = registry.findManagedBean ( "ContextEnvironment" );
        ObjectName oname =
            MBeanUtils.createObjectName ( managed.getDomain(), env );
        return ( oname.toString() );
    }
    public String addResource ( String resourceName, String type )
    throws MalformedObjectNameException {
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextResource resource = nresources.findResource ( resourceName );
        if ( resource != null ) {
            throw new IllegalArgumentException
            ( "Invalid resource name - already exists'" + resourceName + "'" );
        }
        resource = new ContextResource();
        resource.setName ( resourceName );
        resource.setType ( type );
        nresources.addResource ( resource );
        ManagedBean managed = registry.findManagedBean ( "ContextResource" );
        ObjectName oname =
            MBeanUtils.createObjectName ( managed.getDomain(), resource );
        return ( oname.toString() );
    }
    public String addResourceLink ( String resourceLinkName, String type )
    throws MalformedObjectNameException {
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextResourceLink resourceLink =
            nresources.findResourceLink ( resourceLinkName );
        if ( resourceLink != null ) {
            throw new IllegalArgumentException
            ( "Invalid resource link name - already exists'" +
              resourceLinkName + "'" );
        }
        resourceLink = new ContextResourceLink();
        resourceLink.setName ( resourceLinkName );
        resourceLink.setType ( type );
        nresources.addResourceLink ( resourceLink );
        ManagedBean managed = registry.findManagedBean ( "ContextResourceLink" );
        ObjectName oname =
            MBeanUtils.createObjectName ( managed.getDomain(), resourceLink );
        return ( oname.toString() );
    }
    public void removeEnvironment ( String envName ) {
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        ContextEnvironment env = nresources.findEnvironment ( envName );
        if ( env == null ) {
            throw new IllegalArgumentException
            ( "Invalid environment name '" + envName + "'" );
        }
        nresources.removeEnvironment ( envName );
    }
    public void removeResource ( String resourceName ) {
        resourceName = ObjectName.unquote ( resourceName );
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        ContextResource resource = nresources.findResource ( resourceName );
        if ( resource == null ) {
            throw new IllegalArgumentException
            ( "Invalid resource name '" + resourceName + "'" );
        }
        nresources.removeResource ( resourceName );
    }
    public void removeResourceLink ( String resourceLinkName ) {
        resourceLinkName = ObjectName.unquote ( resourceLinkName );
        NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        ContextResourceLink resourceLink =
            nresources.findResourceLink ( resourceLinkName );
        if ( resourceLink == null ) {
            throw new IllegalArgumentException
            ( "Invalid resource Link name '" + resourceLinkName + "'" );
        }
        nresources.removeResourceLink ( resourceLinkName );
    }
}
