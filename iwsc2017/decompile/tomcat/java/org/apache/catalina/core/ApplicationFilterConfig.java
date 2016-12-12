package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.Util;
import java.util.HashMap;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.Globals;
import org.apache.tomcat.util.log.SystemLogHandler;
import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import javax.management.ObjectName;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import javax.servlet.Filter;
import org.apache.catalina.Context;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import java.io.Serializable;
import javax.servlet.FilterConfig;
public final class ApplicationFilterConfig implements FilterConfig, Serializable {
    private static final long serialVersionUID = 1L;
    static final StringManager sm;
    private static final Log log;
    private static final List<String> emptyString;
    private final transient Context context;
    private transient Filter filter;
    private final FilterDef filterDef;
    private transient InstanceManager instanceManager;
    private ObjectName oname;
    ApplicationFilterConfig ( final Context context, final FilterDef filterDef ) throws ClassCastException, ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException, InvocationTargetException, NamingException {
        this.filter = null;
        this.context = context;
        this.filterDef = filterDef;
        if ( filterDef.getFilter() == null ) {
            this.getFilter();
        } else {
            this.filter = filterDef.getFilter();
            this.getInstanceManager().newInstance ( this.filter );
            this.initFilter();
        }
    }
    public String getFilterName() {
        return this.filterDef.getFilterName();
    }
    public String getFilterClass() {
        return this.filterDef.getFilterClass();
    }
    public String getInitParameter ( final String name ) {
        final Map<String, String> map = this.filterDef.getParameterMap();
        if ( map == null ) {
            return null;
        }
        return map.get ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        final Map<String, String> map = this.filterDef.getParameterMap();
        if ( map == null ) {
            return Collections.enumeration ( ApplicationFilterConfig.emptyString );
        }
        return Collections.enumeration ( map.keySet() );
    }
    public ServletContext getServletContext() {
        return this.context.getServletContext();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "ApplicationFilterConfig[" );
        sb.append ( "name=" );
        sb.append ( this.filterDef.getFilterName() );
        sb.append ( ", filterClass=" );
        sb.append ( this.filterDef.getFilterClass() );
        sb.append ( "]" );
        return sb.toString();
    }
    public Map<String, String> getFilterInitParameterMap() {
        return Collections.unmodifiableMap ( ( Map<? extends String, ? extends String> ) this.filterDef.getParameterMap() );
    }
    Filter getFilter() throws ClassCastException, ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException, InvocationTargetException, NamingException {
        if ( this.filter != null ) {
            return this.filter;
        }
        final String filterClass = this.filterDef.getFilterClass();
        this.filter = ( Filter ) this.getInstanceManager().newInstance ( filterClass );
        this.initFilter();
        return this.filter;
    }
    private void initFilter() throws ServletException {
        if ( this.context instanceof StandardContext && this.context.getSwallowOutput() ) {
            try {
                SystemLogHandler.startCapture();
                this.filter.init ( ( FilterConfig ) this );
            } finally {
                final String capturedlog = SystemLogHandler.stopCapture();
                if ( capturedlog != null && capturedlog.length() > 0 ) {
                    this.getServletContext().log ( capturedlog );
                }
            }
        } else {
            this.filter.init ( ( FilterConfig ) this );
        }
        this.registerJMX();
    }
    FilterDef getFilterDef() {
        return this.filterDef;
    }
    void release() {
        this.unregisterJMX();
        if ( this.filter != null ) {
            try {
                if ( Globals.IS_SECURITY_ENABLED ) {
                    try {
                        SecurityUtil.doAsPrivilege ( "destroy", this.filter );
                    } finally {
                        SecurityUtil.remove ( this.filter );
                    }
                } else {
                    this.filter.destroy();
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                this.context.getLogger().error ( ApplicationFilterConfig.sm.getString ( "applicationFilterConfig.release", this.filterDef.getFilterName(), this.filterDef.getFilterClass() ), t );
            }
            if ( !this.context.getIgnoreAnnotations() ) {
                try {
                    ( ( StandardContext ) this.context ).getInstanceManager().destroyInstance ( this.filter );
                } catch ( Exception e ) {
                    final Throwable t2 = ExceptionUtils.unwrapInvocationTargetException ( e );
                    ExceptionUtils.handleThrowable ( t2 );
                    this.context.getLogger().error ( ApplicationFilterConfig.sm.getString ( "applicationFilterConfig.preDestroy", this.filterDef.getFilterName(), this.filterDef.getFilterClass() ), t2 );
                }
            }
        }
        this.filter = null;
    }
    private InstanceManager getInstanceManager() {
        if ( this.instanceManager == null ) {
            if ( this.context instanceof StandardContext ) {
                this.instanceManager = ( ( StandardContext ) this.context ).getInstanceManager();
            } else {
                this.instanceManager = new DefaultInstanceManager ( null, new HashMap<String, Map<String, String>>(), this.context, this.getClass().getClassLoader() );
            }
        }
        return this.instanceManager;
    }
    private void registerJMX() {
        String parentName = this.context.getName();
        if ( !parentName.startsWith ( "/" ) ) {
            parentName = "/" + parentName;
        }
        String hostName = this.context.getParent().getName();
        hostName = ( ( hostName == null ) ? "DEFAULT" : hostName );
        final String domain = this.context.getParent().getParent().getName();
        final String webMod = "//" + hostName + parentName;
        String onameStr = null;
        String filterName = this.filterDef.getFilterName();
        if ( Util.objectNameValueNeedsQuote ( filterName ) ) {
            filterName = ObjectName.quote ( filterName );
        }
        if ( this.context instanceof StandardContext ) {
            final StandardContext standardContext = ( StandardContext ) this.context;
            onameStr = domain + ":j2eeType=Filter,WebModule=" + webMod + ",name=" + filterName + ",J2EEApplication=" + standardContext.getJ2EEApplication() + ",J2EEServer=" + standardContext.getJ2EEServer();
        } else {
            onameStr = domain + ":j2eeType=Filter,name=" + filterName + ",WebModule=" + webMod;
        }
        try {
            this.oname = new ObjectName ( onameStr );
            Registry.getRegistry ( null, null ).registerComponent ( this, this.oname, null );
        } catch ( Exception ex ) {
            ApplicationFilterConfig.log.info ( ApplicationFilterConfig.sm.getString ( "applicationFilterConfig.jmxRegisterFail", this.getFilterClass(), this.getFilterName() ), ex );
        }
    }
    private void unregisterJMX() {
        if ( this.oname != null ) {
            try {
                Registry.getRegistry ( null, null ).unregisterComponent ( this.oname );
                if ( ApplicationFilterConfig.log.isDebugEnabled() ) {
                    ApplicationFilterConfig.log.debug ( ApplicationFilterConfig.sm.getString ( "applicationFilterConfig.jmxUnregister", this.getFilterClass(), this.getFilterName() ) );
                }
            } catch ( Exception ex ) {
                ApplicationFilterConfig.log.error ( ApplicationFilterConfig.sm.getString ( "applicationFilterConfig.jmxUnregisterFail", this.getFilterClass(), this.getFilterName() ), ex );
            }
        }
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.core" );
        log = LogFactory.getLog ( ApplicationFilterConfig.class );
        emptyString = Collections.emptyList();
    }
}
