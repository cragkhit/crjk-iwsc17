package org.apache.jasper.security;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public final class SecurityClassLoad {
    private static final Log log;
    public static void securityClassLoad ( final ClassLoader loader ) {
        if ( System.getSecurityManager() == null ) {
            return;
        }
        final String basePackage = "org.apache.jasper.";
        try {
            loader.loadClass ( "org.apache.jasper.runtime.JspFactoryImpl$PrivilegedGetPageContext" );
            loader.loadClass ( "org.apache.jasper.runtime.JspFactoryImpl$PrivilegedReleasePageContext" );
            loader.loadClass ( "org.apache.jasper.runtime.JspRuntimeLibrary" );
            loader.loadClass ( "org.apache.jasper.runtime.ServletResponseWrapperInclude" );
            loader.loadClass ( "org.apache.jasper.runtime.TagHandlerPool" );
            loader.loadClass ( "org.apache.jasper.runtime.JspFragmentHelper" );
            loader.loadClass ( "org.apache.jasper.runtime.ProtectedFunctionMapper" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$1" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$2" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$3" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$4" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$5" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$6" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$7" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$8" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$9" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$10" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$11" );
            loader.loadClass ( "org.apache.jasper.runtime.PageContextImpl$12" );
            loader.loadClass ( "org.apache.jasper.runtime.JspContextWrapper" );
            SecurityUtil.isPackageProtectionEnabled();
            loader.loadClass ( "org.apache.jasper.servlet.JspServletWrapper" );
            loader.loadClass ( "org.apache.jasper.runtime.JspWriterImpl$1" );
        } catch ( ClassNotFoundException ex ) {
            SecurityClassLoad.log.error ( "SecurityClassLoad", ex );
        }
    }
    static {
        log = LogFactory.getLog ( SecurityClassLoad.class );
    }
}
