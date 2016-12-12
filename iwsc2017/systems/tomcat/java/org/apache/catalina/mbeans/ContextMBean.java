package org.apache.catalina.mbeans;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
public class ContextMBean extends ContainerMBean {
    public ContextMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }
    public String[] findApplicationParameters() throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        ApplicationParameter[] params = context.findApplicationParameters();
        String[] stringParams = new String[params.length];
        for ( int counter = 0; counter < params.length; counter++ ) {
            stringParams[counter] = params[counter].toString();
        }
        return stringParams;
    }
    public String[] findConstraints() throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        SecurityConstraint[] constraints = context.findConstraints();
        String[] stringConstraints = new String[constraints.length];
        for ( int counter = 0; counter < constraints.length; counter++ ) {
            stringConstraints[counter] = constraints[counter].toString();
        }
        return stringConstraints;
    }
    public String findErrorPage ( int errorCode ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        return context.findErrorPage ( errorCode ).toString();
    }
    public String findErrorPage ( String exceptionType ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        return context.findErrorPage ( exceptionType ).toString();
    }
    public String[] findErrorPages() throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        ErrorPage[] pages = context.findErrorPages();
        String[] stringPages = new String[pages.length];
        for ( int counter = 0; counter < pages.length; counter++ ) {
            stringPages[counter] = pages[counter].toString();
        }
        return stringPages;
    }
    public String findFilterDef ( String name ) throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        FilterDef filterDef = context.findFilterDef ( name );
        return filterDef.toString();
    }
    public String[] findFilterDefs() throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        FilterDef[] filterDefs = context.findFilterDefs();
        String[] stringFilters = new String[filterDefs.length];
        for ( int counter = 0; counter < filterDefs.length; counter++ ) {
            stringFilters[counter] = filterDefs[counter].toString();
        }
        return stringFilters;
    }
    public String[] findFilterMaps() throws MBeanException {
        Context context;
        try {
            context = ( Context ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( RuntimeOperationsException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        FilterMap[] maps = context.findFilterMaps();
        String[] stringMaps = new String[maps.length];
        for ( int counter = 0; counter < maps.length; counter++ ) {
            stringMaps[counter] = maps[counter].toString();
        }
        return stringMaps;
    }
}
