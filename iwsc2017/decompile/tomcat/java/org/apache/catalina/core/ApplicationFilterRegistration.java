package org.apache.catalina.core;
import java.util.Set;
import org.apache.catalina.util.ParameterMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.FilterRegistration;
public class ApplicationFilterRegistration implements FilterRegistration.Dynamic {
    private static final StringManager sm;
    private final FilterDef filterDef;
    private final Context context;
    public ApplicationFilterRegistration ( final FilterDef filterDef, final Context context ) {
        this.filterDef = filterDef;
        this.context = context;
    }
    public void addMappingForServletNames ( final EnumSet<DispatcherType> dispatcherTypes, final boolean isMatchAfter, final String... servletNames ) {
        final FilterMap filterMap = new FilterMap();
        filterMap.setFilterName ( this.filterDef.getFilterName() );
        if ( dispatcherTypes != null ) {
            for ( final DispatcherType dispatcherType : dispatcherTypes ) {
                filterMap.setDispatcher ( dispatcherType.name() );
            }
        }
        if ( servletNames != null ) {
            for ( final String servletName : servletNames ) {
                filterMap.addServletName ( servletName );
            }
            if ( isMatchAfter ) {
                this.context.addFilterMap ( filterMap );
            } else {
                this.context.addFilterMapBefore ( filterMap );
            }
        }
    }
    public void addMappingForUrlPatterns ( final EnumSet<DispatcherType> dispatcherTypes, final boolean isMatchAfter, final String... urlPatterns ) {
        final FilterMap filterMap = new FilterMap();
        filterMap.setFilterName ( this.filterDef.getFilterName() );
        if ( dispatcherTypes != null ) {
            for ( final DispatcherType dispatcherType : dispatcherTypes ) {
                filterMap.setDispatcher ( dispatcherType.name() );
            }
        }
        if ( urlPatterns != null ) {
            for ( final String urlPattern : urlPatterns ) {
                filterMap.addURLPattern ( urlPattern );
            }
            if ( isMatchAfter ) {
                this.context.addFilterMap ( filterMap );
            } else {
                this.context.addFilterMapBefore ( filterMap );
            }
        }
    }
    public Collection<String> getServletNameMappings() {
        final Collection<String> result = new HashSet<String>();
        final FilterMap[] filterMaps2;
        final FilterMap[] filterMaps = filterMaps2 = this.context.findFilterMaps();
        for ( final FilterMap filterMap : filterMaps2 ) {
            if ( filterMap.getFilterName().equals ( this.filterDef.getFilterName() ) ) {
                for ( final String servletName : filterMap.getServletNames() ) {
                    result.add ( servletName );
                }
            }
        }
        return result;
    }
    public Collection<String> getUrlPatternMappings() {
        final Collection<String> result = new HashSet<String>();
        final FilterMap[] filterMaps2;
        final FilterMap[] filterMaps = filterMaps2 = this.context.findFilterMaps();
        for ( final FilterMap filterMap : filterMaps2 ) {
            if ( filterMap.getFilterName().equals ( this.filterDef.getFilterName() ) ) {
                for ( final String urlPattern : filterMap.getURLPatterns() ) {
                    result.add ( urlPattern );
                }
            }
        }
        return result;
    }
    public String getClassName() {
        return this.filterDef.getFilterClass();
    }
    public String getInitParameter ( final String name ) {
        return this.filterDef.getParameterMap().get ( name );
    }
    public Map<String, String> getInitParameters() {
        final ParameterMap<String, String> result = new ParameterMap<String, String>();
        result.putAll ( this.filterDef.getParameterMap() );
        result.setLocked ( true );
        return result;
    }
    public String getName() {
        return this.filterDef.getFilterName();
    }
    public boolean setInitParameter ( final String name, final String value ) {
        if ( name == null || value == null ) {
            throw new IllegalArgumentException ( ApplicationFilterRegistration.sm.getString ( "applicationFilterRegistration.nullInitParam", name, value ) );
        }
        if ( this.getInitParameter ( name ) != null ) {
            return false;
        }
        this.filterDef.addInitParameter ( name, value );
        return true;
    }
    public Set<String> setInitParameters ( final Map<String, String> initParameters ) {
        final Set<String> conflicts = new HashSet<String>();
        for ( final Map.Entry<String, String> entry : initParameters.entrySet() ) {
            if ( entry.getKey() == null || entry.getValue() == null ) {
                throw new IllegalArgumentException ( ApplicationFilterRegistration.sm.getString ( "applicationFilterRegistration.nullInitParams", entry.getKey(), entry.getValue() ) );
            }
            if ( this.getInitParameter ( entry.getKey() ) == null ) {
                continue;
            }
            conflicts.add ( entry.getKey() );
        }
        for ( final Map.Entry<String, String> entry : initParameters.entrySet() ) {
            this.setInitParameter ( entry.getKey(), entry.getValue() );
        }
        return conflicts;
    }
    public void setAsyncSupported ( final boolean asyncSupported ) {
        this.filterDef.setAsyncSupported ( Boolean.valueOf ( asyncSupported ).toString() );
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
