package org.apache.tomcat.util.descriptor;
import java.util.Collections;
import java.util.HashMap;
import org.apache.juli.logging.LogFactory;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.EntityResolver;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;
import java.net.URL;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class DigesterFactory {
    private static final Log log;
    private static final StringManager sm;
    private static final Class<ServletContext> CLASS_SERVLET_CONTEXT;
    private static final Class<?> CLASS_JSP_CONTEXT;
    public static final Map<String, String> SERVLET_API_PUBLIC_IDS;
    public static final Map<String, String> SERVLET_API_SYSTEM_IDS;
    private static void addSelf ( final Map<String, String> ids, final String id ) {
        final String location = locationFor ( id );
        if ( location != null ) {
            ids.put ( id, location );
            ids.put ( location, location );
        }
    }
    private static void add ( final Map<String, String> ids, final String id, final String location ) {
        if ( location != null ) {
            ids.put ( id, location );
        }
    }
    private static String locationFor ( final String name ) {
        URL location = DigesterFactory.CLASS_SERVLET_CONTEXT.getResource ( "resources/" + name );
        if ( location == null && DigesterFactory.CLASS_JSP_CONTEXT != null ) {
            location = DigesterFactory.CLASS_JSP_CONTEXT.getResource ( "resources/" + name );
        }
        if ( location == null ) {
            DigesterFactory.log.warn ( DigesterFactory.sm.getString ( "digesterFactory.missingSchema", name ) );
            return null;
        }
        return location.toExternalForm();
    }
    public static Digester newDigester ( final boolean xmlValidation, final boolean xmlNamespaceAware, final RuleSet rule, final boolean blockExternal ) {
        final Digester digester = new Digester();
        digester.setNamespaceAware ( xmlNamespaceAware );
        digester.setValidating ( xmlValidation );
        digester.setUseContextClassLoader ( true );
        final EntityResolver2 resolver = new LocalResolver ( DigesterFactory.SERVLET_API_PUBLIC_IDS, DigesterFactory.SERVLET_API_SYSTEM_IDS, blockExternal );
        digester.setEntityResolver ( resolver );
        if ( rule != null ) {
            digester.addRuleSet ( rule );
        }
        return digester;
    }
    static {
        log = LogFactory.getLog ( DigesterFactory.class );
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
        CLASS_SERVLET_CONTEXT = ServletContext.class;
        Class<?> jspContext = null;
        try {
            jspContext = Class.forName ( "javax.servlet.jsp.JspContext" );
        } catch ( ClassNotFoundException ex ) {}
        CLASS_JSP_CONTEXT = jspContext;
        final Map<String, String> publicIds = new HashMap<String, String>();
        final Map<String, String> systemIds = new HashMap<String, String>();
        add ( publicIds, "-//W3C//DTD XMLSCHEMA 200102//EN", locationFor ( "XMLSchema.dtd" ) );
        add ( publicIds, "datatypes", locationFor ( "datatypes.dtd" ) );
        add ( systemIds, "http://www.w3.org/2001/xml.xsd", locationFor ( "xml.xsd" ) );
        add ( publicIds, "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", locationFor ( "web-app_2_2.dtd" ) );
        add ( publicIds, "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN", locationFor ( "web-jsptaglibrary_1_1.dtd" ) );
        add ( publicIds, "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", locationFor ( "web-app_2_3.dtd" ) );
        add ( publicIds, "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN", locationFor ( "web-jsptaglibrary_1_2.dtd" ) );
        add ( systemIds, "http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd", locationFor ( "j2ee_web_services_1_1.xsd" ) );
        add ( systemIds, "http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd", locationFor ( "j2ee_web_services_client_1_1.xsd" ) );
        add ( systemIds, "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd", locationFor ( "web-app_2_4.xsd" ) );
        add ( systemIds, "http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd", locationFor ( "web-jsptaglibrary_2_0.xsd" ) );
        addSelf ( systemIds, "j2ee_1_4.xsd" );
        addSelf ( systemIds, "jsp_2_0.xsd" );
        add ( systemIds, "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd", locationFor ( "web-app_2_5.xsd" ) );
        add ( systemIds, "http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd", locationFor ( "web-jsptaglibrary_2_1.xsd" ) );
        addSelf ( systemIds, "javaee_5.xsd" );
        addSelf ( systemIds, "jsp_2_1.xsd" );
        addSelf ( systemIds, "javaee_web_services_1_2.xsd" );
        addSelf ( systemIds, "javaee_web_services_client_1_2.xsd" );
        add ( systemIds, "http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd", locationFor ( "web-app_3_0.xsd" ) );
        add ( systemIds, "http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd", locationFor ( "web-fragment_3_0.xsd" ) );
        addSelf ( systemIds, "web-common_3_0.xsd" );
        addSelf ( systemIds, "javaee_6.xsd" );
        addSelf ( systemIds, "jsp_2_2.xsd" );
        addSelf ( systemIds, "javaee_web_services_1_3.xsd" );
        addSelf ( systemIds, "javaee_web_services_client_1_3.xsd" );
        add ( systemIds, "http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd", locationFor ( "web-app_3_1.xsd" ) );
        add ( systemIds, "http://xmlns.jcp.org/xml/ns/javaee/web-fragment_3_1.xsd", locationFor ( "web-fragment_3_1.xsd" ) );
        addSelf ( systemIds, "web-common_3_1.xsd" );
        addSelf ( systemIds, "javaee_7.xsd" );
        addSelf ( systemIds, "jsp_2_3.xsd" );
        addSelf ( systemIds, "javaee_web_services_1_4.xsd" );
        addSelf ( systemIds, "javaee_web_services_client_1_4.xsd" );
        add ( systemIds, "http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd", locationFor ( "web-app_4_0.xsd" ) );
        add ( systemIds, "http://xmlns.jcp.org/xml/ns/javaee/web-fragment_4_0.xsd", locationFor ( "web-fragment_4_0.xsd" ) );
        addSelf ( systemIds, "web-common_4_0.xsd" );
        addSelf ( systemIds, "javaee_8.xsd" );
        SERVLET_API_PUBLIC_IDS = Collections.unmodifiableMap ( ( Map<? extends String, ? extends String> ) publicIds );
        SERVLET_API_SYSTEM_IDS = Collections.unmodifiableMap ( ( Map<? extends String, ? extends String> ) systemIds );
    }
}
