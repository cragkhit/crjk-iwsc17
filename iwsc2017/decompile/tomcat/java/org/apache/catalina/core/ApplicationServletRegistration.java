package org.apache.catalina.core;
import java.util.Collection;
import org.apache.tomcat.util.buf.UDecoder;
import java.util.Collections;
import org.apache.catalina.LifecycleState;
import javax.servlet.ServletSecurityElement;
import javax.servlet.MultipartConfigElement;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import org.apache.catalina.util.ParameterMap;
import java.util.Map;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.ServletRegistration;
public class ApplicationServletRegistration implements ServletRegistration.Dynamic {
    private static final StringManager sm;
    private final Wrapper wrapper;
    private final Context context;
    public ApplicationServletRegistration ( final Wrapper wrapper, final Context context ) {
        this.wrapper = wrapper;
        this.context = context;
    }
    public String getClassName() {
        return this.wrapper.getServletClass();
    }
    public String getInitParameter ( final String name ) {
        return this.wrapper.findInitParameter ( name );
    }
    public Map<String, String> getInitParameters() {
        final ParameterMap<String, String> result = new ParameterMap<String, String>();
        final String[] initParameters;
        final String[] parameterNames = initParameters = this.wrapper.findInitParameters();
        for ( final String parameterName : initParameters ) {
            result.put ( parameterName, this.wrapper.findInitParameter ( parameterName ) );
        }
        result.setLocked ( true );
        return result;
    }
    public String getName() {
        return this.wrapper.getName();
    }
    public boolean setInitParameter ( final String name, final String value ) {
        if ( name == null || value == null ) {
            throw new IllegalArgumentException ( ApplicationServletRegistration.sm.getString ( "applicationFilterRegistration.nullInitParam", name, value ) );
        }
        if ( this.getInitParameter ( name ) != null ) {
            return false;
        }
        this.wrapper.addInitParameter ( name, value );
        return true;
    }
    public Set<String> setInitParameters ( final Map<String, String> initParameters ) {
        final Set<String> conflicts = new HashSet<String>();
        for ( final Map.Entry<String, String> entry : initParameters.entrySet() ) {
            if ( entry.getKey() == null || entry.getValue() == null ) {
                throw new IllegalArgumentException ( ApplicationServletRegistration.sm.getString ( "applicationFilterRegistration.nullInitParams", entry.getKey(), entry.getValue() ) );
            }
            if ( this.getInitParameter ( entry.getKey() ) == null ) {
                continue;
            }
            conflicts.add ( entry.getKey() );
        }
        if ( conflicts.isEmpty() ) {
            for ( final Map.Entry<String, String> entry : initParameters.entrySet() ) {
                this.setInitParameter ( entry.getKey(), entry.getValue() );
            }
        }
        return conflicts;
    }
    public void setAsyncSupported ( final boolean asyncSupported ) {
        this.wrapper.setAsyncSupported ( asyncSupported );
    }
    public void setLoadOnStartup ( final int loadOnStartup ) {
        this.wrapper.setLoadOnStartup ( loadOnStartup );
    }
    public void setMultipartConfig ( final MultipartConfigElement multipartConfig ) {
        this.wrapper.setMultipartConfigElement ( multipartConfig );
    }
    public void setRunAsRole ( final String roleName ) {
        this.wrapper.setRunAs ( roleName );
    }
    public Set<String> setServletSecurity ( final ServletSecurityElement constraint ) {
        if ( constraint == null ) {
            throw new IllegalArgumentException ( ApplicationServletRegistration.sm.getString ( "applicationServletRegistration.setServletSecurity.iae", this.getName(), this.context.getName() ) );
        }
        if ( !this.context.getState().equals ( LifecycleState.STARTING_PREP ) ) {
            throw new IllegalStateException ( ApplicationServletRegistration.sm.getString ( "applicationServletRegistration.setServletSecurity.ise", this.getName(), this.context.getName() ) );
        }
        return this.context.addServletSecurity ( ( ServletRegistration.Dynamic ) this, constraint );
    }
    public Set<String> addMapping ( final String... urlPatterns ) {
        if ( urlPatterns == null ) {
            return Collections.emptySet();
        }
        final Set<String> conflicts = new HashSet<String>();
        for ( final String urlPattern : urlPatterns ) {
            final String wrapperName = this.context.findServletMapping ( urlPattern );
            if ( wrapperName != null ) {
                final Wrapper wrapper = ( Wrapper ) this.context.findChild ( wrapperName );
                if ( wrapper.isOverridable() ) {
                    this.context.removeServletMapping ( urlPattern );
                } else {
                    conflicts.add ( urlPattern );
                }
            }
        }
        if ( !conflicts.isEmpty() ) {
            return conflicts;
        }
        for ( final String urlPattern : urlPatterns ) {
            this.context.addServletMappingDecoded ( UDecoder.URLDecode ( urlPattern, "UTF-8" ), this.wrapper.getName() );
        }
        return Collections.emptySet();
    }
    public Collection<String> getMappings() {
        final Set<String> result = new HashSet<String>();
        final String servletName = this.wrapper.getName();
        final String[] servletMappings;
        final String[] urlPatterns = servletMappings = this.context.findServletMappings();
        for ( final String urlPattern : servletMappings ) {
            final String name = this.context.findServletMapping ( urlPattern );
            if ( name.equals ( servletName ) ) {
                result.add ( urlPattern );
            }
        }
        return result;
    }
    public String getRunAsRole() {
        return this.wrapper.getRunAs();
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.core" );
    }
}
