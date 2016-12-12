package org.apache.catalina.filters;
import java.util.Enumeration;
import javax.servlet.ServletException;
import org.apache.tomcat.util.IntrospectionUtils;
import javax.servlet.FilterConfig;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.Filter;
public abstract class FilterBase implements Filter {
    protected static final StringManager sm;
    protected abstract Log getLogger();
    public void init ( final FilterConfig filterConfig ) throws ServletException {
        final Enumeration<String> paramNames = ( Enumeration<String> ) filterConfig.getInitParameterNames();
        while ( paramNames.hasMoreElements() ) {
            final String paramName = paramNames.nextElement();
            if ( !IntrospectionUtils.setProperty ( this, paramName, filterConfig.getInitParameter ( paramName ) ) ) {
                final String msg = FilterBase.sm.getString ( "filterbase.noSuchProperty", paramName, this.getClass().getName() );
                if ( this.isConfigProblemFatal() ) {
                    throw new ServletException ( msg );
                }
                this.getLogger().warn ( msg );
            }
        }
    }
    protected boolean isConfigProblemFatal() {
        return false;
    }
    static {
        sm = StringManager.getManager ( FilterBase.class );
    }
}
