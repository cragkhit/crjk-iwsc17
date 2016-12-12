package org.apache.catalina.mbeans;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.catalina.Context;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
public class ContextMBean extends ContainerMBean {
    public String[] findApplicationParameters() throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final ApplicationParameter[] params = context.findApplicationParameters();
        final String[] stringParams = new String[params.length];
        for ( int counter = 0; counter < params.length; ++counter ) {
            stringParams[counter] = params[counter].toString();
        }
        return stringParams;
    }
    public String[] findConstraints() throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final SecurityConstraint[] constraints = context.findConstraints();
        final String[] stringConstraints = new String[constraints.length];
        for ( int counter = 0; counter < constraints.length; ++counter ) {
            stringConstraints[counter] = constraints[counter].toString();
        }
        return stringConstraints;
    }
    public String findErrorPage ( final int errorCode ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        return context.findErrorPage ( errorCode ).toString();
    }
    public String findErrorPage ( final String exceptionType ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        return context.findErrorPage ( exceptionType ).toString();
    }
    public String[] findErrorPages() throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final ErrorPage[] pages = context.findErrorPages();
        final String[] stringPages = new String[pages.length];
        for ( int counter = 0; counter < pages.length; ++counter ) {
            stringPages[counter] = pages[counter].toString();
        }
        return stringPages;
    }
    public String findFilterDef ( final String name ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final FilterDef filterDef = context.findFilterDef ( name );
        return filterDef.toString();
    }
    public String[] findFilterDefs() throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final FilterDef[] filterDefs = context.findFilterDefs();
        final String[] stringFilters = new String[filterDefs.length];
        for ( int counter = 0; counter < filterDefs.length; ++counter ) {
            stringFilters[counter] = filterDefs[counter].toString();
        }
        return stringFilters;
    }
    public String[] findFilterMaps() throws MBeanException {
        Context context;
        try {
            context = ( Context ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e2 ) {
            throw new MBeanException ( e2 );
        } catch ( InvalidTargetObjectTypeException e3 ) {
            throw new MBeanException ( e3 );
        }
        final FilterMap[] maps = context.findFilterMaps();
        final String[] stringMaps = new String[maps.length];
        for ( int counter = 0; counter < maps.length; ++counter ) {
            stringMaps[counter] = maps[counter].toString();
        }
        return stringMaps;
    }
}
