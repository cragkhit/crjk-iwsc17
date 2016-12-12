package org.apache.catalina.core;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.apache.catalina.Context;
import org.apache.catalina.util.ParameterMap;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.res.StringManager;
public class ApplicationFilterRegistration
    implements FilterRegistration.Dynamic {
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private final FilterDef filterDef;
    private final Context context;
    public ApplicationFilterRegistration ( FilterDef filterDef,
                                           Context context ) {
        this.filterDef = filterDef;
        this.context = context;
    }
    @Override
    public void addMappingForServletNames (
        EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames ) {
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName ( filterDef.getFilterName() );
        if ( dispatcherTypes != null ) {
            for ( DispatcherType dispatcherType : dispatcherTypes ) {
                filterMap.setDispatcher ( dispatcherType.name() );
            }
        }
        if ( servletNames != null ) {
            for ( String servletName : servletNames ) {
                filterMap.addServletName ( servletName );
            }
            if ( isMatchAfter ) {
                context.addFilterMap ( filterMap );
            } else {
                context.addFilterMapBefore ( filterMap );
            }
        }
    }
    @Override
    public void addMappingForUrlPatterns (
        EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns ) {
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName ( filterDef.getFilterName() );
        if ( dispatcherTypes != null ) {
            for ( DispatcherType dispatcherType : dispatcherTypes ) {
                filterMap.setDispatcher ( dispatcherType.name() );
            }
        }
        if ( urlPatterns != null ) {
            for ( String urlPattern : urlPatterns ) {
                filterMap.addURLPattern ( urlPattern );
            }
            if ( isMatchAfter ) {
                context.addFilterMap ( filterMap );
            } else {
                context.addFilterMapBefore ( filterMap );
            }
        }
    }
    @Override
    public Collection<String> getServletNameMappings() {
        Collection<String> result = new HashSet<>();
        FilterMap[] filterMaps = context.findFilterMaps();
        for ( FilterMap filterMap : filterMaps ) {
            if ( filterMap.getFilterName().equals ( filterDef.getFilterName() ) ) {
                for ( String servletName : filterMap.getServletNames() ) {
                    result.add ( servletName );
                }
            }
        }
        return result;
    }
    @Override
    public Collection<String> getUrlPatternMappings() {
        Collection<String> result = new HashSet<>();
        FilterMap[] filterMaps = context.findFilterMaps();
        for ( FilterMap filterMap : filterMaps ) {
            if ( filterMap.getFilterName().equals ( filterDef.getFilterName() ) ) {
                for ( String urlPattern : filterMap.getURLPatterns() ) {
                    result.add ( urlPattern );
                }
            }
        }
        return result;
    }
    @Override
    public String getClassName() {
        return filterDef.getFilterClass();
    }
    @Override
    public String getInitParameter ( String name ) {
        return filterDef.getParameterMap().get ( name );
    }
    @Override
    public Map<String, String> getInitParameters() {
        ParameterMap<String, String> result = new ParameterMap<>();
        result.putAll ( filterDef.getParameterMap() );
        result.setLocked ( true );
        return result;
    }
    @Override
    public String getName() {
        return filterDef.getFilterName();
    }
    @Override
    public boolean setInitParameter ( String name, String value ) {
        if ( name == null || value == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "applicationFilterRegistration.nullInitParam",
                               name, value ) );
        }
        if ( getInitParameter ( name ) != null ) {
            return false;
        }
        filterDef.addInitParameter ( name, value );
        return true;
    }
    @Override
    public Set<String> setInitParameters ( Map<String, String> initParameters ) {
        Set<String> conflicts = new HashSet<>();
        for ( Map.Entry<String, String> entry : initParameters.entrySet() ) {
            if ( entry.getKey() == null || entry.getValue() == null ) {
                throw new IllegalArgumentException ( sm.getString (
                        "applicationFilterRegistration.nullInitParams",
                        entry.getKey(), entry.getValue() ) );
            }
            if ( getInitParameter ( entry.getKey() ) != null ) {
                conflicts.add ( entry.getKey() );
            }
        }
        for ( Map.Entry<String, String> entry : initParameters.entrySet() ) {
            setInitParameter ( entry.getKey(), entry.getValue() );
        }
        return conflicts;
    }
    @Override
    public void setAsyncSupported ( boolean asyncSupported ) {
        filterDef.setAsyncSupported ( Boolean.valueOf ( asyncSupported ).toString() );
    }
}
