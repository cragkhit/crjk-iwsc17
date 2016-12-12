package org.apache.jasper.servlet;
import javax.servlet.jsp.JspFactory;
import org.apache.jasper.security.SecurityClassLoad;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import org.apache.jasper.compiler.TldCache;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.tomcat.SimpleInstanceManager;
import org.apache.tomcat.InstanceManager;
import org.apache.jasper.compiler.Localizer;
import javax.servlet.ServletContext;
import java.util.Set;
import org.apache.juli.logging.Log;
import javax.servlet.ServletContainerInitializer;
public class JasperInitializer implements ServletContainerInitializer {
    private static final String MSG = "org.apache.jasper.servlet.JasperInitializer";
    private static final Log log;
    public void onStartup ( final Set<Class<?>> types, final ServletContext context ) throws ServletException {
        if ( JasperInitializer.log.isDebugEnabled() ) {
            JasperInitializer.log.debug ( Localizer.getMessage ( "org.apache.jasper.servlet.JasperInitializer.onStartup", context.getServletContextName() ) );
        }
        if ( context.getAttribute ( InstanceManager.class.getName() ) == null ) {
            context.setAttribute ( InstanceManager.class.getName(), ( Object ) new SimpleInstanceManager() );
        }
        final boolean validate = Boolean.parseBoolean ( context.getInitParameter ( "org.apache.jasper.XML_VALIDATE_TLD" ) );
        final String blockExternalString = context.getInitParameter ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
        final boolean blockExternal = blockExternalString == null || Boolean.parseBoolean ( blockExternalString );
        final TldScanner scanner = this.newTldScanner ( context, true, validate, blockExternal );
        try {
            scanner.scan();
        } catch ( IOException | SAXException e ) {
            throw new ServletException ( ( Throwable ) e );
        }
        for ( final String listener : scanner.getListeners() ) {
            context.addListener ( listener );
        }
        context.setAttribute ( TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, ( Object ) new TldCache ( context, scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap() ) );
    }
    protected TldScanner newTldScanner ( final ServletContext context, final boolean namespaceAware, final boolean validate, final boolean blockExternal ) {
        return new TldScanner ( context, namespaceAware, validate, blockExternal );
    }
    static {
        log = LogFactory.getLog ( JasperInitializer.class );
        final JspFactoryImpl factory = new JspFactoryImpl();
        SecurityClassLoad.securityClassLoad ( factory.getClass().getClassLoader() );
        if ( System.getSecurityManager() != null ) {
            final String basePackage = "org.apache.jasper.";
            try {
                factory.getClass().getClassLoader().loadClass ( basePackage + "runtime.JspFactoryImpl$PrivilegedGetPageContext" );
                factory.getClass().getClassLoader().loadClass ( basePackage + "runtime.JspFactoryImpl$PrivilegedReleasePageContext" );
                factory.getClass().getClassLoader().loadClass ( basePackage + "runtime.JspRuntimeLibrary" );
                factory.getClass().getClassLoader().loadClass ( basePackage + "runtime.ServletResponseWrapperInclude" );
                factory.getClass().getClassLoader().loadClass ( basePackage + "servlet.JspServletWrapper" );
            } catch ( ClassNotFoundException ex ) {
                throw new IllegalStateException ( ex );
            }
        }
        if ( JspFactory.getDefaultFactory() == null ) {
            JspFactory.setDefaultFactory ( ( JspFactory ) factory );
        }
    }
}
