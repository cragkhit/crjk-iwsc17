package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import java.lang.reflect.Method;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.Executor;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.Service;
import org.xml.sax.Attributes;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.digester.Rule;
public class ConnectorCreateRule extends Rule {
    private static final Log log;
    protected static final StringManager sm;
    @Override
    public void begin ( final String namespace, final String name, final Attributes attributes ) throws Exception {
        final Service svc = ( Service ) this.digester.peek();
        Executor ex = null;
        if ( attributes.getValue ( "executor" ) != null ) {
            ex = svc.getExecutor ( attributes.getValue ( "executor" ) );
        }
        final Connector con = new Connector ( attributes.getValue ( "protocol" ) );
        if ( ex != null ) {
            setExecutor ( con, ex );
        }
        final String sslImplementationName = attributes.getValue ( "sslImplementationName" );
        if ( sslImplementationName != null ) {
            setSSLImplementationName ( con, sslImplementationName );
        }
        this.digester.push ( con );
    }
    private static void setExecutor ( final Connector con, final Executor ex ) throws Exception {
        final Method m = IntrospectionUtils.findMethod ( con.getProtocolHandler().getClass(), "setExecutor", new Class[] { java.util.concurrent.Executor.class } );
        if ( m != null ) {
            m.invoke ( con.getProtocolHandler(), ex );
        } else {
            ConnectorCreateRule.log.warn ( ConnectorCreateRule.sm.getString ( "connector.noSetExecutor", con ) );
        }
    }
    private static void setSSLImplementationName ( final Connector con, final String sslImplementationName ) throws Exception {
        final Method m = IntrospectionUtils.findMethod ( con.getProtocolHandler().getClass(), "setSslImplementationName", new Class[] { String.class } );
        if ( m != null ) {
            m.invoke ( con.getProtocolHandler(), sslImplementationName );
        } else {
            ConnectorCreateRule.log.warn ( ConnectorCreateRule.sm.getString ( "connector.noSetSSLImplementationName", con ) );
        }
    }
    @Override
    public void end ( final String namespace, final String name ) throws Exception {
        this.digester.pop();
    }
    static {
        log = LogFactory.getLog ( ConnectorCreateRule.class );
        sm = StringManager.getManager ( ConnectorCreateRule.class );
    }
}
