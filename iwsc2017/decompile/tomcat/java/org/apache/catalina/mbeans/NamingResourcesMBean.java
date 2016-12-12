package org.apache.catalina.mbeans;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import javax.management.ObjectName;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import javax.management.MalformedObjectNameException;
import java.util.ArrayList;
import org.apache.catalina.deploy.NamingResourcesImpl;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class NamingResourcesMBean extends BaseModelMBean {
    protected final Registry registry;
    protected final ManagedBean managed;
    public NamingResourcesMBean() throws MBeanException, RuntimeOperationsException {
        this.registry = MBeanUtils.createRegistry();
        this.managed = this.registry.findManagedBean ( "NamingResources" );
    }
    public String[] getEnvironments() {
        final ContextEnvironment[] envs = ( ( NamingResourcesImpl ) this.resource ).findEnvironments();
        final ArrayList<String> results = new ArrayList<String>();
        for ( int i = 0; i < envs.length; ++i ) {
            try {
                final ObjectName oname = MBeanUtils.createObjectName ( this.managed.getDomain(), envs[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( "Cannot create object name for environment " + envs[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getResources() {
        final ContextResource[] resources = ( ( NamingResourcesImpl ) this.resource ).findResources();
        final ArrayList<String> results = new ArrayList<String>();
        for ( int i = 0; i < resources.length; ++i ) {
            try {
                final ObjectName oname = MBeanUtils.createObjectName ( this.managed.getDomain(), resources[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( "Cannot create object name for resource " + resources[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String[] getResourceLinks() {
        final ContextResourceLink[] resourceLinks = ( ( NamingResourcesImpl ) this.resource ).findResourceLinks();
        final ArrayList<String> results = new ArrayList<String>();
        for ( int i = 0; i < resourceLinks.length; ++i ) {
            try {
                final ObjectName oname = MBeanUtils.createObjectName ( this.managed.getDomain(), resourceLinks[i] );
                results.add ( oname.toString() );
            } catch ( MalformedObjectNameException e ) {
                final IllegalArgumentException iae = new IllegalArgumentException ( "Cannot create object name for resource " + resourceLinks[i] );
                iae.initCause ( e );
                throw iae;
            }
        }
        return results.toArray ( new String[results.size()] );
    }
    public String addEnvironment ( final String envName, final String type, final String value ) throws MalformedObjectNameException {
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextEnvironment env = nresources.findEnvironment ( envName );
        if ( env != null ) {
            throw new IllegalArgumentException ( "Invalid environment name - already exists '" + envName + "'" );
        }
        env = new ContextEnvironment();
        env.setName ( envName );
        env.setType ( type );
        env.setValue ( value );
        nresources.addEnvironment ( env );
        final ManagedBean managed = this.registry.findManagedBean ( "ContextEnvironment" );
        final ObjectName oname = MBeanUtils.createObjectName ( managed.getDomain(), env );
        return oname.toString();
    }
    public String addResource ( final String resourceName, final String type ) throws MalformedObjectNameException {
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextResource resource = nresources.findResource ( resourceName );
        if ( resource != null ) {
            throw new IllegalArgumentException ( "Invalid resource name - already exists'" + resourceName + "'" );
        }
        resource = new ContextResource();
        resource.setName ( resourceName );
        resource.setType ( type );
        nresources.addResource ( resource );
        final ManagedBean managed = this.registry.findManagedBean ( "ContextResource" );
        final ObjectName oname = MBeanUtils.createObjectName ( managed.getDomain(), resource );
        return oname.toString();
    }
    public String addResourceLink ( final String resourceLinkName, final String type ) throws MalformedObjectNameException {
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return null;
        }
        ContextResourceLink resourceLink = nresources.findResourceLink ( resourceLinkName );
        if ( resourceLink != null ) {
            throw new IllegalArgumentException ( "Invalid resource link name - already exists'" + resourceLinkName + "'" );
        }
        resourceLink = new ContextResourceLink();
        resourceLink.setName ( resourceLinkName );
        resourceLink.setType ( type );
        nresources.addResourceLink ( resourceLink );
        final ManagedBean managed = this.registry.findManagedBean ( "ContextResourceLink" );
        final ObjectName oname = MBeanUtils.createObjectName ( managed.getDomain(), resourceLink );
        return oname.toString();
    }
    public void removeEnvironment ( final String envName ) {
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        final ContextEnvironment env = nresources.findEnvironment ( envName );
        if ( env == null ) {
            throw new IllegalArgumentException ( "Invalid environment name '" + envName + "'" );
        }
        nresources.removeEnvironment ( envName );
    }
    public void removeResource ( String resourceName ) {
        resourceName = ObjectName.unquote ( resourceName );
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        final ContextResource resource = nresources.findResource ( resourceName );
        if ( resource == null ) {
            throw new IllegalArgumentException ( "Invalid resource name '" + resourceName + "'" );
        }
        nresources.removeResource ( resourceName );
    }
    public void removeResourceLink ( String resourceLinkName ) {
        resourceLinkName = ObjectName.unquote ( resourceLinkName );
        final NamingResourcesImpl nresources = ( NamingResourcesImpl ) this.resource;
        if ( nresources == null ) {
            return;
        }
        final ContextResourceLink resourceLink = nresources.findResourceLink ( resourceLinkName );
        if ( resourceLink == null ) {
            throw new IllegalArgumentException ( "Invalid resource Link name '" + resourceLinkName + "'" );
        }
        nresources.removeResourceLink ( resourceLinkName );
    }
}
