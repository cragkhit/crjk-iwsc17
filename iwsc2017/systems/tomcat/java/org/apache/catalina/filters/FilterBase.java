package org.apache.catalina.filters;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.res.StringManager;
public abstract class FilterBase implements Filter {
    protected static final StringManager sm = StringManager.getManager ( FilterBase.class );
    protected abstract Log getLogger();
    @Override
    public void init ( FilterConfig filterConfig ) throws ServletException {
        Enumeration<String> paramNames = filterConfig.getInitParameterNames();
        while ( paramNames.hasMoreElements() ) {
            String paramName = paramNames.nextElement();
            if ( !IntrospectionUtils.setProperty ( this, paramName,
                                                   filterConfig.getInitParameter ( paramName ) ) ) {
                String msg = sm.getString ( "filterbase.noSuchProperty",
                                            paramName, this.getClass().getName() );
                if ( isConfigProblemFatal() ) {
                    throw new ServletException ( msg );
                } else {
                    getLogger().warn ( msg );
                }
            }
        }
    }
    protected boolean isConfigProblemFatal() {
        return false;
    }
}
