package org.apache.jasper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class Constants {
    public static final String JSP_SERVLET_BASE =
        System.getProperty ( "org.apache.jasper.Constants.JSP_SERVLET_BASE", "org.apache.jasper.runtime.HttpJspBase" );
    public static final String SERVICE_METHOD_NAME =
        System.getProperty ( "org.apache.jasper.Constants.SERVICE_METHOD_NAME", "_jspService" );
    private static final String[] PRIVATE_STANDARD_IMPORTS = {
        "javax.servlet.*",
        "javax.servlet.http.*",
        "javax.servlet.jsp.*"
    };
    public static final List<String> STANDARD_IMPORTS =
        Collections.unmodifiableList ( Arrays.asList ( PRIVATE_STANDARD_IMPORTS ) );
    public static final String SERVLET_CLASSPATH =
        System.getProperty ( "org.apache.jasper.Constants.SERVLET_CLASSPATH", "org.apache.catalina.jsp_classpath" );
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    public static final int DEFAULT_TAG_BUFFER_SIZE = 512;
    public static final int MAX_POOL_SIZE = 5;
    public static final String PRECOMPILE =
        System.getProperty ( "org.apache.jasper.Constants.PRECOMPILE", "jsp_precompile" );
    public static final String JSP_PACKAGE_NAME =
        System.getProperty ( "org.apache.jasper.Constants.JSP_PACKAGE_NAME", "org.apache.jsp" );
    public static final String TAG_FILE_PACKAGE_NAME =
        System.getProperty ( "org.apache.jasper.Constants.TAG_FILE_PACKAGE_NAME", "org.apache.jsp.tag" );
    public static final String NS_PLUGIN_URL =
        "http://java.sun.com/products/plugin/";
    public static final String IE_PLUGIN_URL =
        "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";
    public static final String TEMP_VARIABLE_NAME_PREFIX =
        System.getProperty ( "org.apache.jasper.Constants.TEMP_VARIABLE_NAME_PREFIX", "_jspx_temp" );
    public static final boolean IS_SECURITY_ENABLED =
        ( System.getSecurityManager() != null );
    public static final boolean USE_INSTANCE_MANAGER_FOR_TAGS =
        Boolean.parseBoolean ( System.getProperty ( "org.apache.jasper.Constants.USE_INSTANCE_MANAGER_FOR_TAGS", "false" ) );
    public static final String SESSION_PARAMETER_NAME =
        System.getProperty ( "org.apache.catalina.SESSION_PARAMETER_NAME",
                             "jsessionid" );
    public static final String CATALINA_HOME_PROP = "catalina.home";
    public static final String XML_VALIDATION_TLD_INIT_PARAM =
        "org.apache.jasper.XML_VALIDATE_TLD";
    public static final String XML_BLOCK_EXTERNAL_INIT_PARAM =
        "org.apache.jasper.XML_BLOCK_EXTERNAL";
}
