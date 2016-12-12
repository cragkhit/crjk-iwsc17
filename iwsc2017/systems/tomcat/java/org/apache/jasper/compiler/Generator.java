package org.apache.jasper.compiler;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.Node.NamedAttribute;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.xml.sax.Attributes;
class Generator {
    private static final Class<?>[] OBJECT_CLASS = { Object.class };
    private static final String VAR_EXPRESSIONFACTORY =
        System.getProperty ( "org.apache.jasper.compiler.Generator.VAR_EXPRESSIONFACTORY", "_el_expressionfactory" );
    private static final String VAR_INSTANCEMANAGER =
        System.getProperty ( "org.apache.jasper.compiler.Generator.VAR_INSTANCEMANAGER", "_jsp_instancemanager" );
    private static final boolean POOL_TAGS_WITH_EXTENDS =
        Boolean.getBoolean ( "org.apache.jasper.compiler.Generator.POOL_TAGS_WITH_EXTENDS" );
    private static final boolean STRICT_GET_PROPERTY = Boolean.parseBoolean (
                System.getProperty (
                    "org.apache.jasper.compiler.Generator.STRICT_GET_PROPERTY",
                    "true" ) );
    private final ServletWriter out;
    private final ArrayList<GenBuffer> methodsBuffered;
    private final FragmentHelperClass fragmentHelperClass;
    private final ErrorDispatcher err;
    private final BeanRepository beanInfo;
    private final Set<String> varInfoNames;
    private final JspCompilationContext ctxt;
    private final boolean isPoolingEnabled;
    private final boolean breakAtLF;
    private String jspIdPrefix;
    private int jspId;
    private final PageInfo pageInfo;
    private final Vector<String> tagHandlerPoolNames;
    private GenBuffer charArrayBuffer;
    private final DateFormat timestampFormat;
    private final ELInterpreter elInterpreter;
    static String quote ( String s ) {
        if ( s == null ) {
            return "null";
        }
        return '"' + escape ( s ) + '"';
    }
    static String escape ( String s ) {
        if ( s == null ) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt ( i );
            if ( c == '"' ) {
                b.append ( '\\' ).append ( '"' );
            } else if ( c == '\\' ) {
                b.append ( '\\' ).append ( '\\' );
            } else if ( c == '\n' ) {
                b.append ( '\\' ).append ( 'n' );
            } else if ( c == '\r' ) {
                b.append ( '\\' ).append ( 'r' );
            } else {
                b.append ( c );
            }
        }
        return b.toString();
    }
    static String quote ( char c ) {
        StringBuilder b = new StringBuilder();
        b.append ( '\'' );
        if ( c == '\'' ) {
            b.append ( '\\' ).append ( '\'' );
        } else if ( c == '\\' ) {
            b.append ( '\\' ).append ( '\\' );
        } else if ( c == '\n' ) {
            b.append ( '\\' ).append ( 'n' );
        } else if ( c == '\r' ) {
            b.append ( '\\' ).append ( 'r' );
        } else {
            b.append ( c );
        }
        b.append ( '\'' );
        return b.toString();
    }
    private String createJspId() {
        if ( this.jspIdPrefix == null ) {
            StringBuilder sb = new StringBuilder ( 32 );
            String name = ctxt.getServletJavaFileName();
            sb.append ( "jsp_" );
            sb.append ( Math.abs ( ( long ) name.hashCode() ) );
            sb.append ( '_' );
            this.jspIdPrefix = sb.toString();
        }
        return this.jspIdPrefix + ( this.jspId++ );
    }
    private void generateDeclarations ( Node.Nodes page ) throws JasperException {
        class DeclarationVisitor extends Node.Visitor {
            private boolean getServletInfoGenerated = false;
            @Override
            public void visit ( Node.PageDirective n ) throws JasperException {
                if ( getServletInfoGenerated ) {
                    return;
                }
                String info = n.getAttributeValue ( "info" );
                if ( info == null ) {
                    return;
                }
                getServletInfoGenerated = true;
                out.printil ( "public java.lang.String getServletInfo() {" );
                out.pushIndent();
                out.printin ( "return " );
                out.print ( quote ( info ) );
                out.println ( ";" );
                out.popIndent();
                out.printil ( "}" );
                out.println();
            }
            @Override
            public void visit ( Node.Declaration n ) throws JasperException {
                n.setBeginJavaLine ( out.getJavaLine() );
                out.printMultiLn ( n.getText() );
                out.println();
                n.setEndJavaLine ( out.getJavaLine() );
            }
            @Override
            public void visit ( Node.CustomTag n ) throws JasperException {
                if ( n.useTagPlugin() ) {
                    if ( n.getAtSTag() != null ) {
                        n.getAtSTag().visit ( this );
                    }
                    visitBody ( n );
                    if ( n.getAtETag() != null ) {
                        n.getAtETag().visit ( this );
                    }
                } else {
                    visitBody ( n );
                }
            }
        }
        out.println();
        page.visit ( new DeclarationVisitor() );
    }
    private void compileTagHandlerPoolList ( Node.Nodes page )
    throws JasperException {
        class TagHandlerPoolVisitor extends Node.Visitor {
            private final Vector<String> names;
            TagHandlerPoolVisitor ( Vector<String> v ) {
                names = v;
            }
            @Override
            public void visit ( Node.CustomTag n ) throws JasperException {
                if ( !n.implementsSimpleTag() ) {
                    String name = createTagHandlerPoolName ( n.getPrefix(), n
                                  .getLocalName(), n.getAttributes(),
                                  n.getNamedAttributeNodes(), n.hasEmptyBody() );
                    n.setTagHandlerPoolName ( name );
                    if ( !names.contains ( name ) ) {
                        names.add ( name );
                    }
                }
                visitBody ( n );
            }
            private String createTagHandlerPoolName ( String prefix,
                    String shortName, Attributes attrs, Node.Nodes namedAttrs,
                    boolean hasEmptyBody ) {
                StringBuilder poolName = new StringBuilder ( 64 );
                poolName.append ( "_jspx_tagPool_" ).append ( prefix ).append ( '_' )
                .append ( shortName );
                if ( attrs != null ) {
                    String[] attrNames =
                        new String[attrs.getLength() + namedAttrs.size()];
                    for ( int i = 0; i < attrNames.length; i++ ) {
                        attrNames[i] = attrs.getQName ( i );
                    }
                    for ( int i = 0; i < namedAttrs.size(); i++ ) {
                        attrNames[attrs.getLength() + i] =
                            ( ( NamedAttribute ) namedAttrs.getNode ( i ) ).getQName();
                    }
                    Arrays.sort ( attrNames, Collections.reverseOrder() );
                    if ( attrNames.length > 0 ) {
                        poolName.append ( '&' );
                    }
                    for ( int i = 0; i < attrNames.length; i++ ) {
                        poolName.append ( '_' );
                        poolName.append ( attrNames[i] );
                    }
                }
                if ( hasEmptyBody ) {
                    poolName.append ( "_nobody" );
                }
                return JspUtil.makeJavaIdentifier ( poolName.toString() );
            }
        }
        page.visit ( new TagHandlerPoolVisitor ( tagHandlerPoolNames ) );
    }
    private void declareTemporaryScriptingVars ( Node.Nodes page )
    throws JasperException {
        class ScriptingVarVisitor extends Node.Visitor {
            private final Vector<String> vars;
            ScriptingVarVisitor() {
                vars = new Vector<>();
            }
            @Override
            public void visit ( Node.CustomTag n ) throws JasperException {
                if ( n.getCustomNestingLevel() > 0 ) {
                    TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
                    VariableInfo[] varInfos = n.getVariableInfos();
                    if ( varInfos.length > 0 ) {
                        for ( int i = 0; i < varInfos.length; i++ ) {
                            String varName = varInfos[i].getVarName();
                            String tmpVarName = "_jspx_" + varName + "_"
                                                + n.getCustomNestingLevel();
                            if ( !vars.contains ( tmpVarName ) ) {
                                vars.add ( tmpVarName );
                                out.printin ( varInfos[i].getClassName() );
                                out.print ( " " );
                                out.print ( tmpVarName );
                                out.print ( " = " );
                                out.print ( null );
                                out.println ( ";" );
                            }
                        }
                    } else {
                        for ( int i = 0; i < tagVarInfos.length; i++ ) {
                            String varName = tagVarInfos[i].getNameGiven();
                            if ( varName == null ) {
                                varName = n.getTagData().getAttributeString (
                                              tagVarInfos[i].getNameFromAttribute() );
                            } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                                continue;
                            }
                            String tmpVarName = "_jspx_" + varName + "_"
                                                + n.getCustomNestingLevel();
                            if ( !vars.contains ( tmpVarName ) ) {
                                vars.add ( tmpVarName );
                                out.printin ( tagVarInfos[i].getClassName() );
                                out.print ( " " );
                                out.print ( tmpVarName );
                                out.print ( " = " );
                                out.print ( null );
                                out.println ( ";" );
                            }
                        }
                    }
                }
                visitBody ( n );
            }
        }
        page.visit ( new ScriptingVarVisitor() );
    }
    private void generateGetters() {
        out.printil ( "public javax.el.ExpressionFactory _jsp_getExpressionFactory() {" );
        out.pushIndent();
        if ( !ctxt.isTagFile() ) {
            out.printin ( "if (" );
            out.print ( VAR_EXPRESSIONFACTORY );
            out.println ( " == null) {" );
            out.pushIndent();
            out.printil ( "synchronized (this) {" );
            out.pushIndent();
            out.printin ( "if (" );
            out.print ( VAR_EXPRESSIONFACTORY );
            out.println ( " == null) {" );
            out.pushIndent();
            out.printin ( VAR_EXPRESSIONFACTORY );
            out.println ( " = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
        }
        out.printin ( "return " );
        out.print ( VAR_EXPRESSIONFACTORY );
        out.println ( ";" );
        out.popIndent();
        out.printil ( "}" );
        out.println();
        out.printil ( "public org.apache.tomcat.InstanceManager _jsp_getInstanceManager() {" );
        out.pushIndent();
        if ( !ctxt.isTagFile() ) {
            out.printin ( "if (" );
            out.print ( VAR_INSTANCEMANAGER );
            out.println ( " == null) {" );
            out.pushIndent();
            out.printil ( "synchronized (this) {" );
            out.pushIndent();
            out.printin ( "if (" );
            out.print ( VAR_INSTANCEMANAGER );
            out.println ( " == null) {" );
            out.pushIndent();
            out.printin ( VAR_INSTANCEMANAGER );
            out.println ( " = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
        }
        out.printin ( "return " );
        out.print ( VAR_INSTANCEMANAGER );
        out.println ( ";" );
        out.popIndent();
        out.printil ( "}" );
        out.println();
    }
    private void generateInit() {
        if ( ctxt.isTagFile() ) {
            out.printil ( "private void _jspInit(javax.servlet.ServletConfig config) {" );
        } else {
            out.printil ( "public void _jspInit() {" );
        }
        out.pushIndent();
        if ( isPoolingEnabled ) {
            for ( int i = 0; i < tagHandlerPoolNames.size(); i++ ) {
                out.printin ( tagHandlerPoolNames.elementAt ( i ) );
                out.print ( " = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(" );
                if ( ctxt.isTagFile() ) {
                    out.print ( "config" );
                } else {
                    out.print ( "getServletConfig()" );
                }
                out.println ( ");" );
            }
        }
        if ( ctxt.isTagFile() ) {
            out.printin ( VAR_EXPRESSIONFACTORY );
            out.println ( " = _jspxFactory.getJspApplicationContext(config.getServletContext()).getExpressionFactory();" );
            out.printin ( VAR_INSTANCEMANAGER );
            out.println ( " = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(config);" );
        }
        out.popIndent();
        out.printil ( "}" );
        out.println();
    }
    private void generateDestroy() {
        out.printil ( "public void _jspDestroy() {" );
        out.pushIndent();
        if ( isPoolingEnabled ) {
            for ( int i = 0; i < tagHandlerPoolNames.size(); i++ ) {
                out.printin ( tagHandlerPoolNames.elementAt ( i ) );
                out.println ( ".release();" );
            }
        }
        out.popIndent();
        out.printil ( "}" );
        out.println();
    }
    private void genPreamblePackage ( String packageName ) {
        if ( !"".equals ( packageName ) && packageName != null ) {
            out.printil ( "package " + packageName + ";" );
            out.println();
        }
    }
    private void genPreambleImports() {
        Iterator<String> iter = pageInfo.getImports().iterator();
        while ( iter.hasNext() ) {
            out.printin ( "import " );
            out.print ( iter.next() );
            out.println ( ";" );
        }
        out.println();
    }
    private void genPreambleStaticInitializers() {
        out.printil ( "private static final javax.servlet.jsp.JspFactory _jspxFactory =" );
        out.printil ( "        javax.servlet.jsp.JspFactory.getDefaultFactory();" );
        out.println();
        out.printil ( "private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;" );
        out.println();
        Map<String, Long> dependants = pageInfo.getDependants();
        if ( !dependants.isEmpty() ) {
            out.printil ( "static {" );
            out.pushIndent();
            out.printin ( "_jspx_dependants = new java.util.HashMap<java.lang.String,java.lang.Long>(" );
            out.print ( "" + dependants.size() );
            out.println ( ");" );
            Iterator<Entry<String, Long>> iter = dependants.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<String, Long> entry = iter.next();
                out.printin ( "_jspx_dependants.put(\"" );
                out.print ( entry.getKey() );
                out.print ( "\", Long.valueOf(" );
                out.print ( entry.getValue().toString() );
                out.println ( "L));" );
            }
            out.popIndent();
            out.printil ( "}" );
            out.println();
        }
        List<String> imports = pageInfo.getImports();
        Set<String> packages = new HashSet<>();
        Set<String> classes = new HashSet<>();
        for ( String importName : imports ) {
            if ( importName == null ) {
                continue;
            }
            String trimmed = importName.trim();
            if ( trimmed.endsWith ( ".*" ) ) {
                packages.add ( trimmed.substring ( 0, trimmed.length() - 2 ) );
            } else {
                classes.add ( trimmed );
            }
        }
        out.printil ( "private static final java.util.Set<java.lang.String> _jspx_imports_packages;" );
        out.println();
        out.printil ( "private static final java.util.Set<java.lang.String> _jspx_imports_classes;" );
        out.println();
        out.printil ( "static {" );
        out.pushIndent();
        if ( packages.size() == 0 ) {
            out.printin ( "_jspx_imports_packages = null;" );
            out.println();
        } else {
            out.printin ( "_jspx_imports_packages = new java.util.HashSet<>();" );
            out.println();
            for ( String packageName : packages ) {
                out.printin ( "_jspx_imports_packages.add(\"" );
                out.print ( packageName );
                out.println ( "\");" );
            }
        }
        if ( classes.size() == 0 ) {
            out.printin ( "_jspx_imports_classes = null;" );
            out.println();
        } else {
            out.printin ( "_jspx_imports_classes = new java.util.HashSet<>();" );
            out.println();
            for ( String className : classes ) {
                out.printin ( "_jspx_imports_classes.add(\"" );
                out.print ( className );
                out.println ( "\");" );
            }
        }
        out.popIndent();
        out.printil ( "}" );
        out.println();
    }
    private void genPreambleClassVariableDeclarations() {
        if ( isPoolingEnabled && !tagHandlerPoolNames.isEmpty() ) {
            for ( int i = 0; i < tagHandlerPoolNames.size(); i++ ) {
                out.printil ( "private org.apache.jasper.runtime.TagHandlerPool "
                              + tagHandlerPoolNames.elementAt ( i ) + ";" );
            }
            out.println();
        }
        out.printin ( "private volatile javax.el.ExpressionFactory " );
        out.print ( VAR_EXPRESSIONFACTORY );
        out.println ( ";" );
        out.printin ( "private volatile org.apache.tomcat.InstanceManager " );
        out.print ( VAR_INSTANCEMANAGER );
        out.println ( ";" );
        out.println();
    }
    private void genPreambleMethods() {
        out.printil ( "public java.util.Map<java.lang.String,java.lang.Long> getDependants() {" );
        out.pushIndent();
        out.printil ( "return _jspx_dependants;" );
        out.popIndent();
        out.printil ( "}" );
        out.println();
        out.printil ( "public java.util.Set<java.lang.String> getPackageImports() {" );
        out.pushIndent();
        out.printil ( "return _jspx_imports_packages;" );
        out.popIndent();
        out.printil ( "}" );
        out.println();
        out.printil ( "public java.util.Set<java.lang.String> getClassImports() {" );
        out.pushIndent();
        out.printil ( "return _jspx_imports_classes;" );
        out.popIndent();
        out.printil ( "}" );
        out.println();
        generateGetters();
        generateInit();
        generateDestroy();
    }
    private void generatePreamble ( Node.Nodes page ) throws JasperException {
        String servletPackageName = ctxt.getServletPackageName();
        String servletClassName = ctxt.getServletClassName();
        String serviceMethodName = Constants.SERVICE_METHOD_NAME;
        genPreamblePackage ( servletPackageName );
        genPreambleImports();
        out.printin ( "public final class " );
        out.print ( servletClassName );
        out.print ( " extends " );
        out.println ( pageInfo.getExtends() );
        out.printin ( "    implements org.apache.jasper.runtime.JspSourceDependent," );
        out.println();
        out.printin ( "                 org.apache.jasper.runtime.JspSourceImports" );
        if ( !pageInfo.isThreadSafe() ) {
            out.println ( "," );
            out.printin ( "                 javax.servlet.SingleThreadModel" );
        }
        out.println ( " {" );
        out.pushIndent();
        generateDeclarations ( page );
        genPreambleStaticInitializers();
        genPreambleClassVariableDeclarations();
        genPreambleMethods();
        out.printin ( "public void " );
        out.print ( serviceMethodName );
        out.println ( "(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)" );
        out.pushIndent();
        out.pushIndent();
        out.printil ( "throws java.io.IOException, javax.servlet.ServletException {" );
        out.popIndent();
        out.println();
        if ( !pageInfo.isErrorPage() ) {
            out.printil ( "final java.lang.String _jspx_method = request.getMethod();" );
            out.printin ( "if (!\"GET\".equals(_jspx_method) && !\"POST\".equals(_jspx_method) && !\"HEAD\".equals(_jspx_method) && " );
            out.println ( "!javax.servlet.DispatcherType.ERROR.equals(request.getDispatcherType())) {" );
            out.pushIndent();
            out.printin ( "response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, " );
            out.println ( "\"" + Localizer.getMessage ( "jsp.error.servlet.invalid.method" ) + "\");" );
            out.printil ( "return;" );
            out.popIndent();
            out.printil ( "}" );
            out.println();
        }
        out.printil ( "final javax.servlet.jsp.PageContext pageContext;" );
        if ( pageInfo.isSession() ) {
            out.printil ( "javax.servlet.http.HttpSession session = null;" );
        }
        if ( pageInfo.isErrorPage() ) {
            out.printil ( "java.lang.Throwable exception = org.apache.jasper.runtime.JspRuntimeLibrary.getThrowable(request);" );
            out.printil ( "if (exception != null) {" );
            out.pushIndent();
            out.printil ( "response.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);" );
            out.popIndent();
            out.printil ( "}" );
        }
        out.printil ( "final javax.servlet.ServletContext application;" );
        out.printil ( "final javax.servlet.ServletConfig config;" );
        out.printil ( "javax.servlet.jsp.JspWriter out = null;" );
        out.printil ( "final java.lang.Object page = this;" );
        out.printil ( "javax.servlet.jsp.JspWriter _jspx_out = null;" );
        out.printil ( "javax.servlet.jsp.PageContext _jspx_page_context = null;" );
        out.println();
        declareTemporaryScriptingVars ( page );
        out.println();
        out.printil ( "try {" );
        out.pushIndent();
        out.printin ( "response.setContentType(" );
        out.print ( quote ( pageInfo.getContentType() ) );
        out.println ( ");" );
        if ( ctxt.getOptions().isXpoweredBy() ) {
            out.printil ( "response.addHeader(\"X-Powered-By\", \"JSP/2.1\");" );
        }
        out.printil ( "pageContext = _jspxFactory.getPageContext(this, request, response," );
        out.printin ( "\t\t\t" );
        out.print ( quote ( pageInfo.getErrorPage() ) );
        out.print ( ", " + pageInfo.isSession() );
        out.print ( ", " + pageInfo.getBuffer() );
        out.print ( ", " + pageInfo.isAutoFlush() );
        out.println ( ");" );
        out.printil ( "_jspx_page_context = pageContext;" );
        out.printil ( "application = pageContext.getServletContext();" );
        out.printil ( "config = pageContext.getServletConfig();" );
        if ( pageInfo.isSession() ) {
            out.printil ( "session = pageContext.getSession();" );
        }
        out.printil ( "out = pageContext.getOut();" );
        out.printil ( "_jspx_out = out;" );
        out.println();
    }
    private void generateXmlProlog ( Node.Nodes page ) {
        String omitXmlDecl = pageInfo.getOmitXmlDecl();
        if ( ( omitXmlDecl != null && !JspUtil.booleanValue ( omitXmlDecl ) )
                || ( omitXmlDecl == null && page.getRoot().isXmlSyntax()
                     && !pageInfo.hasJspRoot() && !ctxt.isTagFile() ) ) {
            String cType = pageInfo.getContentType();
            String charSet = cType.substring ( cType.indexOf ( "charset=" ) + 8 );
            out.printil ( "out.write(\"<?xml version=\\\"1.0\\\" encoding=\\\""
                          + charSet + "\\\"?>\\n\");" );
        }
        String doctypeName = pageInfo.getDoctypeName();
        if ( doctypeName != null ) {
            String doctypePublic = pageInfo.getDoctypePublic();
            String doctypeSystem = pageInfo.getDoctypeSystem();
            out.printin ( "out.write(\"<!DOCTYPE " );
            out.print ( doctypeName );
            if ( doctypePublic == null ) {
                out.print ( " SYSTEM \\\"" );
            } else {
                out.print ( " PUBLIC \\\"" );
                out.print ( doctypePublic );
                out.print ( "\\\" \\\"" );
            }
            out.print ( doctypeSystem );
            out.println ( "\\\">\\n\");" );
        }
    }
    private class GenerateVisitor extends Node.Visitor {
        private final Hashtable<String, Hashtable<String, TagHandlerInfo>> handlerInfos;
        private final Hashtable<String, Integer> tagVarNumbers;
        private String parent;
        private boolean isSimpleTagParent;
        private String pushBodyCountVar;
        private String simpleTagHandlerVar;
        private boolean isSimpleTagHandler;
        private boolean isFragment;
        private final boolean isTagFile;
        private ServletWriter out;
        private final ArrayList<GenBuffer> methodsBuffered;
        private final FragmentHelperClass fragmentHelperClass;
        private int methodNesting;
        private int charArrayCount;
        private HashMap<String, String> textMap;
        public GenerateVisitor ( boolean isTagFile, ServletWriter out,
                                 ArrayList<GenBuffer> methodsBuffered,
                                 FragmentHelperClass fragmentHelperClass ) {
            this.isTagFile = isTagFile;
            this.out = out;
            this.methodsBuffered = methodsBuffered;
            this.fragmentHelperClass = fragmentHelperClass;
            methodNesting = 0;
            handlerInfos = new Hashtable<>();
            tagVarNumbers = new Hashtable<>();
            textMap = new HashMap<>();
        }
        private String attributeValue ( Node.JspAttribute attr, boolean encode,
                                        Class<?> expectedType ) {
            String v = attr.getValue();
            if ( !attr.isNamedAttribute() && ( v == null ) ) {
                return "";
            }
            if ( attr.isExpression() ) {
                if ( encode ) {
                    return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(String.valueOf("
                           + v + "), request.getCharacterEncoding())";
                }
                return v;
            } else if ( attr.isELInterpreterInput() ) {
                v = elInterpreter.interpreterCall ( ctxt, this.isTagFile, v,
                                                    expectedType, attr.getEL().getMapName() );
                if ( encode ) {
                    return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode("
                           + v + ", request.getCharacterEncoding())";
                }
                return v;
            } else if ( attr.isNamedAttribute() ) {
                return attr.getNamedAttributeNode().getTemporaryVariableName();
            } else {
                if ( encode ) {
                    return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode("
                           + quote ( v ) + ", request.getCharacterEncoding())";
                }
                return quote ( v );
            }
        }
        private void printParams ( Node n, String pageParam, boolean literal )
        throws JasperException {
            class ParamVisitor extends Node.Visitor {
                private String separator;
                ParamVisitor ( String separator ) {
                    this.separator = separator;
                }
                @Override
                public void visit ( Node.ParamAction n ) throws JasperException {
                    out.print ( " + " );
                    out.print ( separator );
                    out.print ( " + " );
                    out.print ( "org.apache.jasper.runtime.JspRuntimeLibrary."
                                + "URLEncode(" + quote ( n.getTextAttribute ( "name" ) )
                                + ", request.getCharacterEncoding())" );
                    out.print ( "+ \"=\" + " );
                    out.print ( attributeValue ( n.getValue(), true, String.class ) );
                    separator = "\"&\"";
                }
            }
            String sep;
            if ( literal ) {
                sep = pageParam.indexOf ( '?' ) > 0 ? "\"&\"" : "\"?\"";
            } else {
                sep = "((" + pageParam + ").indexOf('?')>0? '&': '?')";
            }
            if ( n.getBody() != null ) {
                n.getBody().visit ( new ParamVisitor ( sep ) );
            }
        }
        @Override
        public void visit ( Node.Expression n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printin ( "out.print(" );
            out.printMultiLn ( n.getText() );
            out.println ( ");" );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.Scriptlet n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printMultiLn ( n.getText() );
            out.println();
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.ELExpression n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            if ( !pageInfo.isELIgnored() && ( n.getEL() != null ) ) {
                out.printil ( "out.write("
                              + elInterpreter.interpreterCall ( ctxt, this.isTagFile,
                                      n.getType() + "{" + n.getText() + "}",
                                      String.class, n.getEL().getMapName() ) +
                              ");" );
            } else {
                out.printil ( "out.write("
                              + quote ( n.getType() + "{" + n.getText() + "}" ) + ");" );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.IncludeAction n ) throws JasperException {
            String flush = n.getTextAttribute ( "flush" );
            Node.JspAttribute page = n.getPage();
            boolean isFlush = false;
            if ( "true".equals ( flush ) ) {
                isFlush = true;
            }
            n.setBeginJavaLine ( out.getJavaLine() );
            String pageParam;
            if ( page.isNamedAttribute() ) {
                pageParam = generateNamedAttributeValue ( page
                            .getNamedAttributeNode() );
            } else {
                pageParam = attributeValue ( page, false, String.class );
            }
            Node jspBody = findJspBody ( n );
            if ( jspBody != null ) {
                prepareParams ( jspBody );
            } else {
                prepareParams ( n );
            }
            out.printin ( "org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, "
                          + pageParam );
            printParams ( n, pageParam, page.isLiteral() );
            out.println ( ", out, " + isFlush + ");" );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        private void prepareParams ( Node parent ) throws JasperException {
            if ( parent == null ) {
                return;
            }
            Node.Nodes subelements = parent.getBody();
            if ( subelements != null ) {
                for ( int i = 0; i < subelements.size(); i++ ) {
                    Node n = subelements.getNode ( i );
                    if ( n instanceof Node.ParamAction ) {
                        Node.Nodes paramSubElements = n.getBody();
                        for ( int j = 0; ( paramSubElements != null )
                                && ( j < paramSubElements.size() ); j++ ) {
                            Node m = paramSubElements.getNode ( j );
                            if ( m instanceof Node.NamedAttribute ) {
                                generateNamedAttributeValue ( ( Node.NamedAttribute ) m );
                            }
                        }
                    }
                }
            }
        }
        private Node.JspBody findJspBody ( Node parent ) {
            Node.JspBody result = null;
            Node.Nodes subelements = parent.getBody();
            for ( int i = 0; ( subelements != null ) && ( i < subelements.size() ); i++ ) {
                Node n = subelements.getNode ( i );
                if ( n instanceof Node.JspBody ) {
                    result = ( Node.JspBody ) n;
                    break;
                }
            }
            return result;
        }
        @Override
        public void visit ( Node.ForwardAction n ) throws JasperException {
            Node.JspAttribute page = n.getPage();
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printil ( "if (true) {" );
            out.pushIndent();
            String pageParam;
            if ( page.isNamedAttribute() ) {
                pageParam = generateNamedAttributeValue ( page
                            .getNamedAttributeNode() );
            } else {
                pageParam = attributeValue ( page, false, String.class );
            }
            Node jspBody = findJspBody ( n );
            if ( jspBody != null ) {
                prepareParams ( jspBody );
            } else {
                prepareParams ( n );
            }
            out.printin ( "_jspx_page_context.forward(" );
            out.print ( pageParam );
            printParams ( n, pageParam, page.isLiteral() );
            out.println ( ");" );
            if ( isTagFile || isFragment ) {
                out.printil ( "throw new javax.servlet.jsp.SkipPageException();" );
            } else {
                out.printil ( ( methodNesting > 0 ) ? "return true;" : "return;" );
            }
            out.popIndent();
            out.printil ( "}" );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.GetProperty n ) throws JasperException {
            String name = n.getTextAttribute ( "name" );
            String property = n.getTextAttribute ( "property" );
            n.setBeginJavaLine ( out.getJavaLine() );
            if ( beanInfo.checkVariable ( name ) ) {
                Class<?> bean = beanInfo.getBeanType ( name );
                String beanName = bean.getCanonicalName();
                java.lang.reflect.Method meth = JspRuntimeLibrary
                                                .getReadMethod ( bean, property );
                String methodName = meth.getName();
                out.printil ( "out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString("
                              + "((("
                              + beanName
                              + ")_jspx_page_context.findAttribute("
                              + "\""
                              + name + "\"))." + methodName + "())));" );
            } else if ( !STRICT_GET_PROPERTY || varInfoNames.contains ( name ) ) {
                out.printil ( "out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString"
                              + "(org.apache.jasper.runtime.JspRuntimeLibrary.handleGetProperty"
                              + "(_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \""
                              + property
                              + "\")));" );
            } else {
                StringBuilder msg = new StringBuilder();
                msg.append ( "file:" );
                msg.append ( n.getStart() );
                msg.append ( " jsp:getProperty for bean with name '" );
                msg.append ( name );
                msg.append (
                    "'. Name was not previously introduced as per JSP.5.3" );
                throw new JasperException ( msg.toString() );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.SetProperty n ) throws JasperException {
            String name = n.getTextAttribute ( "name" );
            String property = n.getTextAttribute ( "property" );
            String param = n.getTextAttribute ( "param" );
            Node.JspAttribute value = n.getValue();
            n.setBeginJavaLine ( out.getJavaLine() );
            if ( "*".equals ( property ) ) {
                out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspect("
                              + "_jspx_page_context.findAttribute("
                              + "\""
                              + name + "\"), request);" );
            } else if ( value == null ) {
                if ( param == null ) {
                    param = property;
                }
                out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper("
                              + "_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \""
                              + property
                              + "\", request.getParameter(\""
                              + param
                              + "\"), "
                              + "request, \""
                              + param
                              + "\", false);" );
            } else if ( value.isExpression() ) {
                out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.handleSetProperty("
                              + "_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \"" + property + "\"," );
                out.print ( attributeValue ( value, false, null ) );
                out.println ( ");" );
            } else if ( value.isELInterpreterInput() ) {
                out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.handleSetPropertyExpression("
                              + "_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \""
                              + property
                              + "\", "
                              + quote ( value.getValue() )
                              + ", "
                              + "_jspx_page_context, "
                              + value.getEL().getMapName() + ");" );
            } else if ( value.isNamedAttribute() ) {
                String valueVarName = generateNamedAttributeValue ( value
                                      .getNamedAttributeNode() );
                out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper("
                              + "_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \""
                              + property
                              + "\", "
                              + valueVarName
                              + ", null, null, false);" );
            } else {
                out.printin ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper("
                              + "_jspx_page_context.findAttribute(\""
                              + name
                              + "\"), \"" + property + "\", " );
                out.print ( attributeValue ( value, false, null ) );
                out.println ( ", null, null, false);" );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.UseBean n ) throws JasperException {
            String name = n.getTextAttribute ( "id" );
            String scope = n.getTextAttribute ( "scope" );
            String klass = n.getTextAttribute ( "class" );
            String type = n.getTextAttribute ( "type" );
            Node.JspAttribute beanName = n.getBeanName();
            boolean generateNew = false;
            String canonicalName = null;
            if ( klass != null ) {
                try {
                    Class<?> bean = ctxt.getClassLoader().loadClass ( klass );
                    if ( klass.indexOf ( '$' ) >= 0 ) {
                        canonicalName = bean.getCanonicalName();
                    } else {
                        canonicalName = klass;
                    }
                    int modifiers = bean.getModifiers();
                    if ( !Modifier.isPublic ( modifiers )
                            || Modifier.isInterface ( modifiers )
                            || Modifier.isAbstract ( modifiers ) ) {
                        throw new Exception ( "Invalid bean class modifier" );
                    }
                    bean.getConstructor ( new Class[] {} );
                    generateNew = true;
                } catch ( Exception e ) {
                    if ( ctxt.getOptions()
                            .getErrorOnUseBeanInvalidClassAttribute() ) {
                        err.jspError ( n, "jsp.error.invalid.bean", klass );
                    }
                    if ( canonicalName == null ) {
                        canonicalName = klass.replace ( '$', '.' );
                    }
                }
                if ( type == null ) {
                    type = canonicalName;
                }
            }
            String scopename = "javax.servlet.jsp.PageContext.PAGE_SCOPE";
            String lock = null;
            if ( "request".equals ( scope ) ) {
                scopename = "javax.servlet.jsp.PageContext.REQUEST_SCOPE";
            } else if ( "session".equals ( scope ) ) {
                scopename = "javax.servlet.jsp.PageContext.SESSION_SCOPE";
                lock = "session";
            } else if ( "application".equals ( scope ) ) {
                scopename = "javax.servlet.jsp.PageContext.APPLICATION_SCOPE";
                lock = "application";
            }
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printin ( type );
            out.print ( ' ' );
            out.print ( name );
            out.println ( " = null;" );
            if ( lock != null ) {
                out.printin ( "synchronized (" );
                out.print ( lock );
                out.println ( ") {" );
                out.pushIndent();
            }
            out.printin ( name );
            out.print ( " = (" );
            out.print ( type );
            out.print ( ") _jspx_page_context.getAttribute(" );
            out.print ( quote ( name ) );
            out.print ( ", " );
            out.print ( scopename );
            out.println ( ");" );
            out.printin ( "if (" );
            out.print ( name );
            out.println ( " == null){" );
            out.pushIndent();
            if ( klass == null && beanName == null ) {
                out.printin ( "throw new java.lang.InstantiationException(\"bean " );
                out.print ( name );
                out.println ( " not found within scope\");" );
            } else {
                if ( !generateNew ) {
                    String binaryName;
                    if ( beanName != null ) {
                        if ( beanName.isNamedAttribute() ) {
                            binaryName = generateNamedAttributeValue ( beanName
                                         .getNamedAttributeNode() );
                        } else {
                            binaryName = attributeValue ( beanName, false, String.class );
                        }
                    } else {
                        binaryName = quote ( klass );
                    }
                    out.printil ( "try {" );
                    out.pushIndent();
                    out.printin ( name );
                    out.print ( " = (" );
                    out.print ( type );
                    out.print ( ") java.beans.Beans.instantiate(" );
                    out.print ( "this.getClass().getClassLoader(), " );
                    out.print ( binaryName );
                    out.println ( ");" );
                    out.popIndent();
                    out.printil ( "} catch (java.lang.ClassNotFoundException exc) {" );
                    out.pushIndent();
                    out.printil ( "throw new InstantiationException(exc.getMessage());" );
                    out.popIndent();
                    out.printil ( "} catch (java.lang.Exception exc) {" );
                    out.pushIndent();
                    out.printin ( "throw new javax.servlet.ServletException(" );
                    out.print ( "\"Cannot create bean of class \" + " );
                    out.print ( binaryName );
                    out.println ( ", exc);" );
                    out.popIndent();
                    out.printil ( "}" );
                } else {
                    out.printin ( name );
                    out.print ( " = new " );
                    out.print ( canonicalName );
                    out.println ( "();" );
                }
                out.printin ( "_jspx_page_context.setAttribute(" );
                out.print ( quote ( name ) );
                out.print ( ", " );
                out.print ( name );
                out.print ( ", " );
                out.print ( scopename );
                out.println ( ");" );
                visitBody ( n );
            }
            out.popIndent();
            out.printil ( "}" );
            if ( lock != null ) {
                out.popIndent();
                out.printil ( "}" );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        private String makeAttr ( String attr, String value ) {
            if ( value == null ) {
                return "";
            }
            return " " + attr + "=\"" + value + '\"';
        }
        @Override
        public void visit ( Node.PlugIn n ) throws JasperException {
            class ParamVisitor extends Node.Visitor {
                private final boolean ie;
                ParamVisitor ( boolean ie ) {
                    this.ie = ie;
                }
                @Override
                public void visit ( Node.ParamAction n ) throws JasperException {
                    String name = n.getTextAttribute ( "name" );
                    if ( name.equalsIgnoreCase ( "object" ) ) {
                        name = "java_object";
                    } else if ( name.equalsIgnoreCase ( "type" ) ) {
                        name = "java_type";
                    }
                    n.setBeginJavaLine ( out.getJavaLine() );
                    if ( ie ) {
                        out.printil ( "out.write( \"<param name=\\\"" +
                                      escape ( name ) +
                                      "\\\" value=\\\"\" + " +
                                      attributeValue ( n.getValue(), false,
                                                       String.class ) +
                                      " + \"\\\">\" );" );
                        out.printil ( "out.write(\"\\n\");" );
                    } else {
                        out.printil ( "out.write( \" " +
                                      escape ( name ) +
                                      "=\\\"\" + " +
                                      attributeValue ( n.getValue(), false,
                                                       String.class ) +
                                      " + \"\\\"\" );" );
                    }
                    n.setEndJavaLine ( out.getJavaLine() );
                }
            }
            String type = n.getTextAttribute ( "type" );
            String code = n.getTextAttribute ( "code" );
            String name = n.getTextAttribute ( "name" );
            Node.JspAttribute height = n.getHeight();
            Node.JspAttribute width = n.getWidth();
            String hspace = n.getTextAttribute ( "hspace" );
            String vspace = n.getTextAttribute ( "vspace" );
            String align = n.getTextAttribute ( "align" );
            String iepluginurl = n.getTextAttribute ( "iepluginurl" );
            String nspluginurl = n.getTextAttribute ( "nspluginurl" );
            String codebase = n.getTextAttribute ( "codebase" );
            String archive = n.getTextAttribute ( "archive" );
            String jreversion = n.getTextAttribute ( "jreversion" );
            String widthStr = null;
            if ( width != null ) {
                if ( width.isNamedAttribute() ) {
                    widthStr = generateNamedAttributeValue ( width
                               .getNamedAttributeNode() );
                } else {
                    widthStr = attributeValue ( width, false, String.class );
                }
            }
            String heightStr = null;
            if ( height != null ) {
                if ( height.isNamedAttribute() ) {
                    heightStr = generateNamedAttributeValue ( height
                                .getNamedAttributeNode() );
                } else {
                    heightStr = attributeValue ( height, false, String.class );
                }
            }
            if ( iepluginurl == null ) {
                iepluginurl = Constants.IE_PLUGIN_URL;
            }
            if ( nspluginurl == null ) {
                nspluginurl = Constants.NS_PLUGIN_URL;
            }
            n.setBeginJavaLine ( out.getJavaLine() );
            Node.JspBody jspBody = findJspBody ( n );
            if ( jspBody != null ) {
                Node.Nodes subelements = jspBody.getBody();
                if ( subelements != null ) {
                    for ( int i = 0; i < subelements.size(); i++ ) {
                        Node m = subelements.getNode ( i );
                        if ( m instanceof Node.ParamsAction ) {
                            prepareParams ( m );
                            break;
                        }
                    }
                }
            }
            String s0 = "<object"
                        + makeAttr ( "classid", ctxt.getOptions().getIeClassId() )
                        + makeAttr ( "name", name );
            String s1 = "";
            if ( width != null ) {
                s1 = " + \" width=\\\"\" + " + widthStr + " + \"\\\"\"";
            }
            String s2 = "";
            if ( height != null ) {
                s2 = " + \" height=\\\"\" + " + heightStr + " + \"\\\"\"";
            }
            String s3 = makeAttr ( "hspace", hspace ) + makeAttr ( "vspace", vspace )
                        + makeAttr ( "align", align )
                        + makeAttr ( "codebase", iepluginurl ) + '>';
            out.printil ( "out.write(" + quote ( s0 ) + s1 + s2 + " + " + quote ( s3 )
                          + ");" );
            out.printil ( "out.write(\"\\n\");" );
            s0 = "<param name=\"java_code\"" + makeAttr ( "value", code ) + '>';
            out.printil ( "out.write(" + quote ( s0 ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            if ( codebase != null ) {
                s0 = "<param name=\"java_codebase\""
                     + makeAttr ( "value", codebase ) + '>';
                out.printil ( "out.write(" + quote ( s0 ) + ");" );
                out.printil ( "out.write(\"\\n\");" );
            }
            if ( archive != null ) {
                s0 = "<param name=\"java_archive\""
                     + makeAttr ( "value", archive ) + '>';
                out.printil ( "out.write(" + quote ( s0 ) + ");" );
                out.printil ( "out.write(\"\\n\");" );
            }
            s0 = "<param name=\"type\""
                 + makeAttr ( "value", "application/x-java-"
                              + type
                              + ( ( jreversion == null ) ? "" : ";version="
                                  + jreversion ) ) + '>';
            out.printil ( "out.write(" + quote ( s0 ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            if ( n.getBody() != null ) {
                n.getBody().visit ( new ParamVisitor ( true ) );
            }
            out.printil ( "out.write(" + quote ( "<comment>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            s0 = "<EMBED"
                 + makeAttr ( "type", "application/x-java-"
                              + type
                              + ( ( jreversion == null ) ? "" : ";version="
                                  + jreversion ) ) + makeAttr ( "name", name );
            s3 = makeAttr ( "hspace", hspace ) + makeAttr ( "vspace", vspace )
                 + makeAttr ( "align", align )
                 + makeAttr ( "pluginspage", nspluginurl )
                 + makeAttr ( "java_code", code )
                 + makeAttr ( "java_codebase", codebase )
                 + makeAttr ( "java_archive", archive );
            out.printil ( "out.write(" + quote ( s0 ) + s1 + s2 + " + " + quote ( s3 )
                          + ");" );
            if ( n.getBody() != null ) {
                n.getBody().visit ( new ParamVisitor ( false ) );
            }
            out.printil ( "out.write(" + quote ( "/>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            out.printil ( "out.write(" + quote ( "<noembed>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            if ( n.getBody() != null ) {
                visitBody ( n );
                out.printil ( "out.write(\"\\n\");" );
            }
            out.printil ( "out.write(" + quote ( "</noembed>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            out.printil ( "out.write(" + quote ( "</comment>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            out.printil ( "out.write(" + quote ( "</object>" ) + ");" );
            out.printil ( "out.write(\"\\n\");" );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.NamedAttribute n ) throws JasperException {
        }
        @Override
        public void visit ( Node.CustomTag n ) throws JasperException {
            if ( n.useTagPlugin() ) {
                generateTagPlugin ( n );
                return;
            }
            TagHandlerInfo handlerInfo = getTagHandlerInfo ( n );
            String baseVar = createTagVarName ( n.getQName(), n.getPrefix(), n
                                                .getLocalName() );
            String tagEvalVar = "_jspx_eval_" + baseVar;
            String tagHandlerVar = "_jspx_th_" + baseVar;
            String tagPushBodyCountVar = "_jspx_push_body_count_" + baseVar;
            ServletWriter outSave = null;
            Node.ChildInfo ci = n.getChildInfo();
            if ( ci.isScriptless() && !ci.hasScriptingVars() ) {
                String tagMethod = "_jspx_meth_" + baseVar;
                out.printin ( "if (" );
                out.print ( tagMethod );
                out.print ( "(" );
                if ( parent != null ) {
                    out.print ( parent );
                    out.print ( ", " );
                }
                out.print ( "_jspx_page_context" );
                if ( pushBodyCountVar != null ) {
                    out.print ( ", " );
                    out.print ( pushBodyCountVar );
                }
                out.println ( "))" );
                out.pushIndent();
                out.printil ( ( methodNesting > 0 ) ? "return true;" : "return;" );
                out.popIndent();
                outSave = out;
                GenBuffer genBuffer = new GenBuffer ( n,
                                                      n.implementsSimpleTag() ? null : n.getBody() );
                methodsBuffered.add ( genBuffer );
                out = genBuffer.getOut();
                methodNesting++;
                out.println();
                out.pushIndent();
                out.printin ( "private boolean " );
                out.print ( tagMethod );
                out.print ( "(" );
                if ( parent != null ) {
                    out.print ( "javax.servlet.jsp.tagext.JspTag " );
                    out.print ( parent );
                    out.print ( ", " );
                }
                out.print ( "javax.servlet.jsp.PageContext _jspx_page_context" );
                if ( pushBodyCountVar != null ) {
                    out.print ( ", int[] " );
                    out.print ( pushBodyCountVar );
                }
                out.println ( ")" );
                out.printil ( "        throws java.lang.Throwable {" );
                out.pushIndent();
                if ( !isTagFile ) {
                    out.printil ( "javax.servlet.jsp.PageContext pageContext = _jspx_page_context;" );
                }
                out.printil ( "javax.servlet.jsp.JspWriter out = _jspx_page_context.getOut();" );
                generateLocalVariables ( out, n );
            }
            VariableInfo[] infos = n.getVariableInfos();
            if ( infos != null && infos.length > 0 ) {
                for ( int i = 0; i < infos.length; i++ ) {
                    VariableInfo info = infos[i];
                    if ( info != null && info.getVarName() != null ) {
                        pageInfo.getVarInfoNames().add ( info.getVarName() );
                    }
                }
            }
            TagVariableInfo[] tagInfos = n.getTagVariableInfos();
            if ( tagInfos != null && tagInfos.length > 0 ) {
                for ( int i = 0; i < tagInfos.length; i++ ) {
                    TagVariableInfo tagInfo = tagInfos[i];
                    if ( tagInfo != null ) {
                        String name = tagInfo.getNameGiven();
                        if ( name == null ) {
                            String nameFromAttribute =
                                tagInfo.getNameFromAttribute();
                            name = n.getAttributeValue ( nameFromAttribute );
                        }
                        pageInfo.getVarInfoNames().add ( name );
                    }
                }
            }
            if ( n.implementsSimpleTag() ) {
                generateCustomDoTag ( n, handlerInfo, tagHandlerVar );
            } else {
                generateCustomStart ( n, handlerInfo, tagHandlerVar, tagEvalVar,
                                      tagPushBodyCountVar );
                String tmpParent = parent;
                parent = tagHandlerVar;
                boolean isSimpleTagParentSave = isSimpleTagParent;
                isSimpleTagParent = false;
                String tmpPushBodyCountVar = null;
                if ( n.implementsTryCatchFinally() ) {
                    tmpPushBodyCountVar = pushBodyCountVar;
                    pushBodyCountVar = tagPushBodyCountVar;
                }
                boolean tmpIsSimpleTagHandler = isSimpleTagHandler;
                isSimpleTagHandler = false;
                visitBody ( n );
                parent = tmpParent;
                isSimpleTagParent = isSimpleTagParentSave;
                if ( n.implementsTryCatchFinally() ) {
                    pushBodyCountVar = tmpPushBodyCountVar;
                }
                isSimpleTagHandler = tmpIsSimpleTagHandler;
                generateCustomEnd ( n, tagHandlerVar, tagEvalVar,
                                    tagPushBodyCountVar );
            }
            if ( ci.isScriptless() && !ci.hasScriptingVars() ) {
                if ( methodNesting > 0 ) {
                    out.printil ( "return false;" );
                }
                out.popIndent();
                out.printil ( "}" );
                out.popIndent();
                methodNesting--;
                out = outSave;
            }
        }
        private static final String DOUBLE_QUOTE = "\\\"";
        @Override
        public void visit ( Node.UninterpretedTag n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printin ( "out.write(\"<" );
            out.print ( n.getQName() );
            Attributes attrs = n.getNonTaglibXmlnsAttributes();
            if ( attrs != null ) {
                for ( int i = 0; i < attrs.getLength(); i++ ) {
                    out.print ( " " );
                    out.print ( attrs.getQName ( i ) );
                    out.print ( "=" );
                    out.print ( DOUBLE_QUOTE );
                    out.print ( escape ( attrs.getValue ( i ).replace ( "\"", "&quot;" ) ) );
                    out.print ( DOUBLE_QUOTE );
                }
            }
            attrs = n.getAttributes();
            if ( attrs != null ) {
                Node.JspAttribute[] jspAttrs = n.getJspAttributes();
                for ( int i = 0; i < attrs.getLength(); i++ ) {
                    out.print ( " " );
                    out.print ( attrs.getQName ( i ) );
                    out.print ( "=" );
                    if ( jspAttrs[i].isELInterpreterInput() ) {
                        out.print ( "\\\"\" + " );
                        String debug = attributeValue ( jspAttrs[i], false, String.class );
                        out.print ( debug );
                        out.print ( " + \"\\\"" );
                    } else {
                        out.print ( DOUBLE_QUOTE );
                        out.print ( escape ( jspAttrs[i].getValue().replace ( "\"", "&quot;" ) ) );
                        out.print ( DOUBLE_QUOTE );
                    }
                }
            }
            if ( n.getBody() != null ) {
                out.println ( ">\");" );
                visitBody ( n );
                out.printin ( "out.write(\"</" );
                out.print ( n.getQName() );
                out.println ( ">\");" );
            } else {
                out.println ( "/>\");" );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.JspElement n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            Hashtable<String, String> map = new Hashtable<>();
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                String value = null;
                String nvp = null;
                if ( attrs[i].isNamedAttribute() ) {
                    NamedAttribute attr = attrs[i].getNamedAttributeNode();
                    Node.JspAttribute omitAttr = attr.getOmit();
                    String omit;
                    if ( omitAttr == null ) {
                        omit = "false";
                    } else {
                        omit = attributeValue ( omitAttr, false, boolean.class );
                        if ( "true".equals ( omit ) ) {
                            continue;
                        }
                    }
                    value = generateNamedAttributeValue (
                                attrs[i].getNamedAttributeNode() );
                    if ( "false".equals ( omit ) ) {
                        nvp = " + \" " + attrs[i].getName() + "=\\\"\" + " +
                              value + " + \"\\\"\"";
                    } else {
                        nvp = " + (java.lang.Boolean.valueOf(" + omit + ")?\"\":\" " +
                              attrs[i].getName() + "=\\\"\" + " + value +
                              " + \"\\\"\")";
                    }
                } else {
                    value = attributeValue ( attrs[i], false, Object.class );
                    nvp = " + \" " + attrs[i].getName() + "=\\\"\" + " +
                          value + " + \"\\\"\"";
                }
                map.put ( attrs[i].getName(), nvp );
            }
            String elemName = attributeValue ( n.getNameAttribute(), false, String.class );
            out.printin ( "out.write(\"<\"" );
            out.print ( " + " + elemName );
            Enumeration<String> enumeration = map.keys();
            while ( enumeration.hasMoreElements() ) {
                String attrName = enumeration.nextElement();
                out.print ( map.get ( attrName ) );
            }
            boolean hasBody = false;
            Node.Nodes subelements = n.getBody();
            if ( subelements != null ) {
                for ( int i = 0; i < subelements.size(); i++ ) {
                    Node subelem = subelements.getNode ( i );
                    if ( ! ( subelem instanceof Node.NamedAttribute ) ) {
                        hasBody = true;
                        break;
                    }
                }
            }
            if ( hasBody ) {
                out.println ( " + \">\");" );
                n.setEndJavaLine ( out.getJavaLine() );
                visitBody ( n );
                out.printin ( "out.write(\"</\"" );
                out.print ( " + " + elemName );
                out.println ( " + \">\");" );
            } else {
                out.println ( " + \"/>\");" );
                n.setEndJavaLine ( out.getJavaLine() );
            }
        }
        @Override
        public void visit ( Node.TemplateText n ) throws JasperException {
            String text = n.getText();
            int textSize = text.length();
            if ( textSize == 0 ) {
                return;
            }
            if ( textSize <= 3 ) {
                n.setBeginJavaLine ( out.getJavaLine() );
                int lineInc = 0;
                for ( int i = 0; i < textSize; i++ ) {
                    char ch = text.charAt ( i );
                    out.printil ( "out.write(" + quote ( ch ) + ");" );
                    if ( i > 0 ) {
                        n.addSmap ( lineInc );
                    }
                    if ( ch == '\n' ) {
                        lineInc++;
                    }
                }
                n.setEndJavaLine ( out.getJavaLine() );
                return;
            }
            if ( ctxt.getOptions().genStringAsCharArray() ) {
                ServletWriter caOut;
                if ( charArrayBuffer == null ) {
                    charArrayBuffer = new GenBuffer();
                    caOut = charArrayBuffer.getOut();
                    caOut.pushIndent();
                    textMap = new HashMap<>();
                } else {
                    caOut = charArrayBuffer.getOut();
                }
                int textIndex = 0;
                int textLength = text.length();
                while ( textIndex < textLength ) {
                    int len = 0;
                    if ( textLength - textIndex > 16384 ) {
                        len = 16384;
                    } else {
                        len = textLength - textIndex;
                    }
                    String output = text.substring ( textIndex, textIndex + len );
                    String charArrayName = textMap.get ( output );
                    if ( charArrayName == null ) {
                        charArrayName = "_jspx_char_array_" + charArrayCount++;
                        textMap.put ( output, charArrayName );
                        caOut.printin ( "static char[] " );
                        caOut.print ( charArrayName );
                        caOut.print ( " = " );
                        caOut.print ( quote ( output ) );
                        caOut.println ( ".toCharArray();" );
                    }
                    n.setBeginJavaLine ( out.getJavaLine() );
                    out.printil ( "out.write(" + charArrayName + ");" );
                    n.setEndJavaLine ( out.getJavaLine() );
                    textIndex = textIndex + len;
                }
                return;
            }
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printin();
            StringBuilder sb = new StringBuilder ( "out.write(\"" );
            int initLength = sb.length();
            int count = JspUtil.CHUNKSIZE;
            int srcLine = 0;
            for ( int i = 0; i < text.length(); i++ ) {
                char ch = text.charAt ( i );
                --count;
                switch ( ch ) {
                case '"':
                    sb.append ( '\\' ).append ( '\"' );
                    break;
                case '\\':
                    sb.append ( '\\' ).append ( '\\' );
                    break;
                case '\r':
                    sb.append ( '\\' ).append ( 'r' );
                    break;
                case '\n':
                    sb.append ( '\\' ).append ( 'n' );
                    srcLine++;
                    if ( breakAtLF || count < 0 ) {
                        sb.append ( "\");" );
                        out.println ( sb.toString() );
                        if ( i < text.length() - 1 ) {
                            out.printin();
                        }
                        sb.setLength ( initLength );
                        count = JspUtil.CHUNKSIZE;
                    }
                    n.addSmap ( srcLine );
                    break;
                case '\t':
                    sb.append ( '\\' ).append ( 't' );
                    break;
                default:
                    sb.append ( ch );
                }
            }
            if ( sb.length() > initLength ) {
                sb.append ( "\");" );
                out.println ( sb.toString() );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.JspBody n ) throws JasperException {
            if ( n.getBody() != null ) {
                if ( isSimpleTagHandler ) {
                    out.printin ( simpleTagHandlerVar );
                    out.print ( ".setJspBody(" );
                    generateJspFragment ( n, simpleTagHandlerVar );
                    out.println ( ");" );
                } else {
                    visitBody ( n );
                }
            }
        }
        @Override
        public void visit ( Node.InvokeAction n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printil ( "((org.apache.jasper.runtime.JspContextWrapper) this.jspContext).syncBeforeInvoke();" );
            String varReaderAttr = n.getTextAttribute ( "varReader" );
            String varAttr = n.getTextAttribute ( "var" );
            if ( varReaderAttr != null || varAttr != null ) {
                out.printil ( "_jspx_sout = new java.io.StringWriter();" );
            } else {
                out.printil ( "_jspx_sout = null;" );
            }
            out.printin ( "if (" );
            out.print ( toGetterMethod ( n.getTextAttribute ( "fragment" ) ) );
            out.println ( " != null) {" );
            out.pushIndent();
            out.printin ( toGetterMethod ( n.getTextAttribute ( "fragment" ) ) );
            out.println ( ".invoke(_jspx_sout);" );
            out.popIndent();
            out.printil ( "}" );
            if ( varReaderAttr != null || varAttr != null ) {
                String scopeName = n.getTextAttribute ( "scope" );
                out.printin ( "_jspx_page_context.setAttribute(" );
                if ( varReaderAttr != null ) {
                    out.print ( quote ( varReaderAttr ) );
                    out.print ( ", new java.io.StringReader(_jspx_sout.toString())" );
                } else {
                    out.print ( quote ( varAttr ) );
                    out.print ( ", _jspx_sout.toString()" );
                }
                if ( scopeName != null ) {
                    out.print ( ", " );
                    out.print ( getScopeConstant ( scopeName ) );
                }
                out.println ( ");" );
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.DoBodyAction n ) throws JasperException {
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printil ( "((org.apache.jasper.runtime.JspContextWrapper) this.jspContext).syncBeforeInvoke();" );
            String varReaderAttr = n.getTextAttribute ( "varReader" );
            String varAttr = n.getTextAttribute ( "var" );
            if ( varReaderAttr != null || varAttr != null ) {
                out.printil ( "_jspx_sout = new java.io.StringWriter();" );
            } else {
                out.printil ( "_jspx_sout = null;" );
            }
            out.printil ( "if (getJspBody() != null)" );
            out.pushIndent();
            out.printil ( "getJspBody().invoke(_jspx_sout);" );
            out.popIndent();
            if ( varReaderAttr != null || varAttr != null ) {
                String scopeName = n.getTextAttribute ( "scope" );
                out.printin ( "_jspx_page_context.setAttribute(" );
                if ( varReaderAttr != null ) {
                    out.print ( quote ( varReaderAttr ) );
                    out.print ( ", new java.io.StringReader(_jspx_sout.toString())" );
                } else {
                    out.print ( quote ( varAttr ) );
                    out.print ( ", _jspx_sout.toString()" );
                }
                if ( scopeName != null ) {
                    out.print ( ", " );
                    out.print ( getScopeConstant ( scopeName ) );
                }
                out.println ( ");" );
            }
            out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,getJspContext());" );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        @Override
        public void visit ( Node.AttributeGenerator n ) throws JasperException {
            Node.CustomTag tag = n.getTag();
            Node.JspAttribute[] attrs = tag.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                if ( attrs[i].getName().equals ( n.getName() ) ) {
                    out.print ( evaluateAttribute ( getTagHandlerInfo ( tag ),
                                                    attrs[i], tag, null ) );
                    break;
                }
            }
        }
        private TagHandlerInfo getTagHandlerInfo ( Node.CustomTag n )
        throws JasperException {
            Hashtable<String, TagHandlerInfo> handlerInfosByShortName =
                handlerInfos.get ( n.getPrefix() );
            if ( handlerInfosByShortName == null ) {
                handlerInfosByShortName = new Hashtable<>();
                handlerInfos.put ( n.getPrefix(), handlerInfosByShortName );
            }
            TagHandlerInfo handlerInfo =
                handlerInfosByShortName.get ( n.getLocalName() );
            if ( handlerInfo == null ) {
                handlerInfo = new TagHandlerInfo ( n, n.getTagHandlerClass(), err );
                handlerInfosByShortName.put ( n.getLocalName(), handlerInfo );
            }
            return handlerInfo;
        }
        private void generateTagPlugin ( Node.CustomTag n ) throws JasperException {
            if ( n.getAtSTag() != null ) {
                n.getAtSTag().visit ( this );
            }
            visitBody ( n );
            if ( n.getAtETag() != null ) {
                n.getAtETag().visit ( this );
            }
        }
        private void generateCustomStart ( Node.CustomTag n,
                                           TagHandlerInfo handlerInfo, String tagHandlerVar,
                                           String tagEvalVar, String tagPushBodyCountVar )
        throws JasperException {
            Class<?> tagHandlerClass =
                handlerInfo.getTagHandlerClass();
            out.printin ( "//  " );
            out.println ( n.getQName() );
            n.setBeginJavaLine ( out.getJavaLine() );
            declareScriptingVars ( n, VariableInfo.AT_BEGIN );
            saveScriptingVars ( n, VariableInfo.AT_BEGIN );
            String tagHandlerClassName = tagHandlerClass.getCanonicalName();
            if ( isPoolingEnabled && ! ( n.implementsJspIdConsumer() ) ) {
                out.printin ( tagHandlerClassName );
                out.print ( " " );
                out.print ( tagHandlerVar );
                out.print ( " = " );
                out.print ( "(" );
                out.print ( tagHandlerClassName );
                out.print ( ") " );
                out.print ( n.getTagHandlerPoolName() );
                out.print ( ".get(" );
                out.print ( tagHandlerClassName );
                out.println ( ".class);" );
            } else {
                writeNewInstance ( tagHandlerVar, tagHandlerClassName );
            }
            out.printil ( "try {" );
            out.pushIndent();
            generateSetters ( n, tagHandlerVar, handlerInfo, false );
            if ( n.implementsJspIdConsumer() ) {
                out.printin ( tagHandlerVar );
                out.print ( ".setJspId(\"" );
                out.print ( createJspId() );
                out.println ( "\");" );
            }
            if ( n.implementsTryCatchFinally() ) {
                out.printin ( "int[] " );
                out.print ( tagPushBodyCountVar );
                out.println ( " = new int[] { 0 };" );
                out.printil ( "try {" );
                out.pushIndent();
            }
            out.printin ( "int " );
            out.print ( tagEvalVar );
            out.print ( " = " );
            out.print ( tagHandlerVar );
            out.println ( ".doStartTag();" );
            if ( !n.implementsBodyTag() ) {
                syncScriptingVars ( n, VariableInfo.AT_BEGIN );
            }
            if ( !n.hasEmptyBody() ) {
                out.printin ( "if (" );
                out.print ( tagEvalVar );
                out.println ( " != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {" );
                out.pushIndent();
                declareScriptingVars ( n, VariableInfo.NESTED );
                saveScriptingVars ( n, VariableInfo.NESTED );
                if ( n.implementsBodyTag() ) {
                    out.printin ( "if (" );
                    out.print ( tagEvalVar );
                    out.println ( " != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {" );
                    out.pushIndent();
                    out.printil ( "out = _jspx_page_context.pushBody();" );
                    if ( n.implementsTryCatchFinally() ) {
                        out.printin ( tagPushBodyCountVar );
                        out.println ( "[0]++;" );
                    } else if ( pushBodyCountVar != null ) {
                        out.printin ( pushBodyCountVar );
                        out.println ( "[0]++;" );
                    }
                    out.printin ( tagHandlerVar );
                    out.println ( ".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);" );
                    out.printin ( tagHandlerVar );
                    out.println ( ".doInitBody();" );
                    out.popIndent();
                    out.printil ( "}" );
                    syncScriptingVars ( n, VariableInfo.AT_BEGIN );
                    syncScriptingVars ( n, VariableInfo.NESTED );
                } else {
                    syncScriptingVars ( n, VariableInfo.NESTED );
                }
                if ( n.implementsIterationTag() ) {
                    out.printil ( "do {" );
                    out.pushIndent();
                }
            }
            n.setEndJavaLine ( out.getJavaLine() );
        }
        private void writeNewInstance ( String tagHandlerVar, String tagHandlerClassName ) {
            if ( Constants.USE_INSTANCE_MANAGER_FOR_TAGS ) {
                out.printin ( tagHandlerClassName );
                out.print ( " " );
                out.print ( tagHandlerVar );
                out.print ( " = (" );
                out.print ( tagHandlerClassName );
                out.print ( ")" );
                out.print ( "_jsp_getInstanceManager().newInstance(\"" );
                out.print ( tagHandlerClassName );
                out.println ( "\", this.getClass().getClassLoader());" );
            } else {
                out.printin ( tagHandlerClassName );
                out.print ( " " );
                out.print ( tagHandlerVar );
                out.print ( " = (" );
                out.print ( "new " );
                out.print ( tagHandlerClassName );
                out.println ( "());" );
                out.printin ( "_jsp_getInstanceManager().newInstance(" );
                out.print ( tagHandlerVar );
                out.println ( ");" );
            }
        }
        private void writeDestroyInstance ( String tagHandlerVar ) {
            out.printin ( "_jsp_getInstanceManager().destroyInstance(" );
            out.print ( tagHandlerVar );
            out.println ( ");" );
        }
        private void generateCustomEnd ( Node.CustomTag n, String tagHandlerVar,
                                         String tagEvalVar, String tagPushBodyCountVar ) {
            if ( !n.hasEmptyBody() ) {
                if ( n.implementsIterationTag() ) {
                    out.printin ( "int evalDoAfterBody = " );
                    out.print ( tagHandlerVar );
                    out.println ( ".doAfterBody();" );
                    syncScriptingVars ( n, VariableInfo.AT_BEGIN );
                    syncScriptingVars ( n, VariableInfo.NESTED );
                    out.printil ( "if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)" );
                    out.pushIndent();
                    out.printil ( "break;" );
                    out.popIndent();
                    out.popIndent();
                    out.printil ( "} while (true);" );
                }
                restoreScriptingVars ( n, VariableInfo.NESTED );
                if ( n.implementsBodyTag() ) {
                    out.printin ( "if (" );
                    out.print ( tagEvalVar );
                    out.println ( " != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {" );
                    out.pushIndent();
                    out.printil ( "out = _jspx_page_context.popBody();" );
                    if ( n.implementsTryCatchFinally() ) {
                        out.printin ( tagPushBodyCountVar );
                        out.println ( "[0]--;" );
                    } else if ( pushBodyCountVar != null ) {
                        out.printin ( pushBodyCountVar );
                        out.println ( "[0]--;" );
                    }
                    out.popIndent();
                    out.printil ( "}" );
                }
                out.popIndent();
                out.printil ( "}" );
            }
            out.printin ( "if (" );
            out.print ( tagHandlerVar );
            out.println ( ".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {" );
            out.pushIndent();
            if ( isTagFile || isFragment ) {
                out.printil ( "throw new javax.servlet.jsp.SkipPageException();" );
            } else {
                out.printil ( ( methodNesting > 0 ) ? "return true;" : "return;" );
            }
            out.popIndent();
            out.printil ( "}" );
            syncScriptingVars ( n, VariableInfo.AT_BEGIN );
            if ( n.implementsTryCatchFinally() ) {
                out.popIndent();
                out.printil ( "} catch (java.lang.Throwable _jspx_exception) {" );
                out.pushIndent();
                out.printin ( "while (" );
                out.print ( tagPushBodyCountVar );
                out.println ( "[0]-- > 0)" );
                out.pushIndent();
                out.printil ( "out = _jspx_page_context.popBody();" );
                out.popIndent();
                out.printin ( tagHandlerVar );
                out.println ( ".doCatch(_jspx_exception);" );
                out.popIndent();
                out.printil ( "} finally {" );
                out.pushIndent();
                out.printin ( tagHandlerVar );
                out.println ( ".doFinally();" );
            }
            if ( n.implementsTryCatchFinally() ) {
                out.popIndent();
                out.printil ( "}" );
            }
            out.popIndent();
            out.printil ( "} finally {" );
            out.pushIndent();
            if ( isPoolingEnabled && ! ( n.implementsJspIdConsumer() ) ) {
                out.printin ( n.getTagHandlerPoolName() );
                out.print ( ".reuse(" );
                out.print ( tagHandlerVar );
                out.println ( ");" );
            } else {
                out.printin ( tagHandlerVar );
                out.println ( ".release();" );
                writeDestroyInstance ( tagHandlerVar );
            }
            out.popIndent();
            out.printil ( "}" );
            declareScriptingVars ( n, VariableInfo.AT_END );
            syncScriptingVars ( n, VariableInfo.AT_END );
            restoreScriptingVars ( n, VariableInfo.AT_BEGIN );
        }
        private void generateCustomDoTag ( Node.CustomTag n,
                                           TagHandlerInfo handlerInfo, String tagHandlerVar )
        throws JasperException {
            Class<?> tagHandlerClass =
                handlerInfo.getTagHandlerClass();
            n.setBeginJavaLine ( out.getJavaLine() );
            out.printin ( "//  " );
            out.println ( n.getQName() );
            declareScriptingVars ( n, VariableInfo.AT_BEGIN );
            saveScriptingVars ( n, VariableInfo.AT_BEGIN );
            String tagHandlerClassName = tagHandlerClass.getCanonicalName();
            writeNewInstance ( tagHandlerVar, tagHandlerClassName );
            generateSetters ( n, tagHandlerVar, handlerInfo, true );
            if ( n.implementsJspIdConsumer() ) {
                out.printin ( tagHandlerVar );
                out.print ( ".setJspId(\"" );
                out.print ( createJspId() );
                out.println ( "\");" );
            }
            if ( findJspBody ( n ) == null ) {
                if ( !n.hasEmptyBody() ) {
                    out.printin ( tagHandlerVar );
                    out.print ( ".setJspBody(" );
                    generateJspFragment ( n, tagHandlerVar );
                    out.println ( ");" );
                }
            } else {
                String tmpTagHandlerVar = simpleTagHandlerVar;
                simpleTagHandlerVar = tagHandlerVar;
                boolean tmpIsSimpleTagHandler = isSimpleTagHandler;
                isSimpleTagHandler = true;
                visitBody ( n );
                simpleTagHandlerVar = tmpTagHandlerVar;
                isSimpleTagHandler = tmpIsSimpleTagHandler;
            }
            out.printin ( tagHandlerVar );
            out.println ( ".doTag();" );
            restoreScriptingVars ( n, VariableInfo.AT_BEGIN );
            syncScriptingVars ( n, VariableInfo.AT_BEGIN );
            declareScriptingVars ( n, VariableInfo.AT_END );
            syncScriptingVars ( n, VariableInfo.AT_END );
            writeDestroyInstance ( tagHandlerVar );
            n.setEndJavaLine ( out.getJavaLine() );
        }
        private void declareScriptingVars ( Node.CustomTag n, int scope ) {
            if ( isFragment ) {
                return;
            }
            List<Object> vec = n.getScriptingVars ( scope );
            if ( vec != null ) {
                for ( int i = 0; i < vec.size(); i++ ) {
                    Object elem = vec.get ( i );
                    if ( elem instanceof VariableInfo ) {
                        VariableInfo varInfo = ( VariableInfo ) elem;
                        if ( varInfo.getDeclare() ) {
                            out.printin ( varInfo.getClassName() );
                            out.print ( " " );
                            out.print ( varInfo.getVarName() );
                            out.println ( " = null;" );
                        }
                    } else {
                        TagVariableInfo tagVarInfo = ( TagVariableInfo ) elem;
                        if ( tagVarInfo.getDeclare() ) {
                            String varName = tagVarInfo.getNameGiven();
                            if ( varName == null ) {
                                varName = n.getTagData().getAttributeString (
                                              tagVarInfo.getNameFromAttribute() );
                            } else if ( tagVarInfo.getNameFromAttribute() != null ) {
                                continue;
                            }
                            out.printin ( tagVarInfo.getClassName() );
                            out.print ( " " );
                            out.print ( varName );
                            out.println ( " = null;" );
                        }
                    }
                }
            }
        }
        private void saveScriptingVars ( Node.CustomTag n, int scope ) {
            if ( n.getCustomNestingLevel() == 0 ) {
                return;
            }
            if ( isFragment ) {
                return;
            }
            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();
            if ( ( varInfos.length == 0 ) && ( tagVarInfos.length == 0 ) ) {
                return;
            }
            List<Object> declaredVariables = n.getScriptingVars ( scope );
            if ( varInfos.length > 0 ) {
                for ( int i = 0; i < varInfos.length; i++ ) {
                    if ( varInfos[i].getScope() != scope ) {
                        continue;
                    }
                    if ( declaredVariables.contains ( varInfos[i] ) ) {
                        continue;
                    }
                    String varName = varInfos[i].getVarName();
                    String tmpVarName = "_jspx_" + varName + "_"
                                        + n.getCustomNestingLevel();
                    out.printin ( tmpVarName );
                    out.print ( " = " );
                    out.print ( varName );
                    out.println ( ";" );
                }
            } else {
                for ( int i = 0; i < tagVarInfos.length; i++ ) {
                    if ( tagVarInfos[i].getScope() != scope ) {
                        continue;
                    }
                    if ( declaredVariables.contains ( tagVarInfos[i] ) ) {
                        continue;
                    }
                    String varName = tagVarInfos[i].getNameGiven();
                    if ( varName == null ) {
                        varName = n.getTagData().getAttributeString (
                                      tagVarInfos[i].getNameFromAttribute() );
                    } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                        continue;
                    }
                    String tmpVarName = "_jspx_" + varName + "_"
                                        + n.getCustomNestingLevel();
                    out.printin ( tmpVarName );
                    out.print ( " = " );
                    out.print ( varName );
                    out.println ( ";" );
                }
            }
        }
        private void restoreScriptingVars ( Node.CustomTag n, int scope ) {
            if ( n.getCustomNestingLevel() == 0 ) {
                return;
            }
            if ( isFragment ) {
                return;
            }
            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();
            if ( ( varInfos.length == 0 ) && ( tagVarInfos.length == 0 ) ) {
                return;
            }
            List<Object> declaredVariables = n.getScriptingVars ( scope );
            if ( varInfos.length > 0 ) {
                for ( int i = 0; i < varInfos.length; i++ ) {
                    if ( varInfos[i].getScope() != scope ) {
                        continue;
                    }
                    if ( declaredVariables.contains ( varInfos[i] ) ) {
                        continue;
                    }
                    String varName = varInfos[i].getVarName();
                    String tmpVarName = "_jspx_" + varName + "_"
                                        + n.getCustomNestingLevel();
                    out.printin ( varName );
                    out.print ( " = " );
                    out.print ( tmpVarName );
                    out.println ( ";" );
                }
            } else {
                for ( int i = 0; i < tagVarInfos.length; i++ ) {
                    if ( tagVarInfos[i].getScope() != scope ) {
                        continue;
                    }
                    if ( declaredVariables.contains ( tagVarInfos[i] ) ) {
                        continue;
                    }
                    String varName = tagVarInfos[i].getNameGiven();
                    if ( varName == null ) {
                        varName = n.getTagData().getAttributeString (
                                      tagVarInfos[i].getNameFromAttribute() );
                    } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                        continue;
                    }
                    String tmpVarName = "_jspx_" + varName + "_"
                                        + n.getCustomNestingLevel();
                    out.printin ( varName );
                    out.print ( " = " );
                    out.print ( tmpVarName );
                    out.println ( ";" );
                }
            }
        }
        private void syncScriptingVars ( Node.CustomTag n, int scope ) {
            if ( isFragment ) {
                return;
            }
            TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
            VariableInfo[] varInfos = n.getVariableInfos();
            if ( ( varInfos.length == 0 ) && ( tagVarInfos.length == 0 ) ) {
                return;
            }
            if ( varInfos.length > 0 ) {
                for ( int i = 0; i < varInfos.length; i++ ) {
                    if ( varInfos[i].getScope() == scope ) {
                        out.printin ( varInfos[i].getVarName() );
                        out.print ( " = (" );
                        out.print ( varInfos[i].getClassName() );
                        out.print ( ") _jspx_page_context.findAttribute(" );
                        out.print ( quote ( varInfos[i].getVarName() ) );
                        out.println ( ");" );
                    }
                }
            } else {
                for ( int i = 0; i < tagVarInfos.length; i++ ) {
                    if ( tagVarInfos[i].getScope() == scope ) {
                        String name = tagVarInfos[i].getNameGiven();
                        if ( name == null ) {
                            name = n.getTagData().getAttributeString (
                                       tagVarInfos[i].getNameFromAttribute() );
                        } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                            continue;
                        }
                        out.printin ( name );
                        out.print ( " = (" );
                        out.print ( tagVarInfos[i].getClassName() );
                        out.print ( ") _jspx_page_context.findAttribute(" );
                        out.print ( quote ( name ) );
                        out.println ( ");" );
                    }
                }
            }
        }
        private String getJspContextVar() {
            if ( this.isTagFile ) {
                return "this.getJspContext()";
            }
            return "_jspx_page_context";
        }
        private String createTagVarName ( String fullName, String prefix,
                                          String shortName ) {
            String varName;
            synchronized ( tagVarNumbers ) {
                varName = prefix + "_" + shortName + "_";
                if ( tagVarNumbers.get ( fullName ) != null ) {
                    Integer i = tagVarNumbers.get ( fullName );
                    varName = varName + i.intValue();
                    tagVarNumbers.put ( fullName,
                                        Integer.valueOf ( i.intValue() + 1 ) );
                } else {
                    tagVarNumbers.put ( fullName, Integer.valueOf ( 1 ) );
                    varName = varName + "0";
                }
            }
            return JspUtil.makeJavaIdentifier ( varName );
        }
        @SuppressWarnings ( "null" )
        private String evaluateAttribute ( TagHandlerInfo handlerInfo,
                                           Node.JspAttribute attr, Node.CustomTag n, String tagHandlerVar )
        throws JasperException {
            String attrValue = attr.getValue();
            if ( attrValue == null ) {
                if ( attr.isNamedAttribute() ) {
                    if ( n.checkIfAttributeIsJspFragment ( attr.getName() ) ) {
                        attrValue = generateNamedAttributeJspFragment ( attr
                                    .getNamedAttributeNode(), tagHandlerVar );
                    } else {
                        attrValue = generateNamedAttributeValue ( attr
                                    .getNamedAttributeNode() );
                    }
                } else {
                    return null;
                }
            }
            String localName = attr.getLocalName();
            Method m = null;
            Class<?>[] c = null;
            if ( attr.isDynamic() ) {
                c = OBJECT_CLASS;
            } else {
                m = handlerInfo.getSetterMethod ( localName );
                if ( m == null ) {
                    err.jspError ( n, "jsp.error.unable.to_find_method", attr
                                   .getName() );
                }
                c = m.getParameterTypes();
            }
            if ( attr.isExpression() ) {
            } else if ( attr.isNamedAttribute() ) {
                if ( !n.checkIfAttributeIsJspFragment ( attr.getName() )
                        && !attr.isDynamic() ) {
                    attrValue = convertString ( c[0], attrValue, localName,
                                                handlerInfo.getPropertyEditorClass ( localName ), true );
                }
            } else if ( attr.isELInterpreterInput() ) {
                StringBuilder sb = new StringBuilder ( 64 );
                TagAttributeInfo tai = attr.getTagAttributeInfo();
                sb.append ( getJspContextVar() );
                sb.append ( ".getELContext()" );
                String elContext = sb.toString();
                if ( attr.getEL() != null && attr.getEL().getMapName() != null ) {
                    sb.setLength ( 0 );
                    sb.append ( "new org.apache.jasper.el.ELContextWrapper(" );
                    sb.append ( elContext );
                    sb.append ( ',' );
                    sb.append ( attr.getEL().getMapName() );
                    sb.append ( ')' );
                    elContext = sb.toString();
                }
                sb.setLength ( 0 );
                sb.append ( n.getStart().toString() );
                sb.append ( " '" );
                sb.append ( attrValue );
                sb.append ( '\'' );
                String mark = sb.toString();
                sb.setLength ( 0 );
                if ( attr.isDeferredInput()
                        || ( ( tai != null ) && ValueExpression.class.getName().equals ( tai.getTypeName() ) ) ) {
                    sb.append ( "new org.apache.jasper.el.JspValueExpression(" );
                    sb.append ( quote ( mark ) );
                    sb.append ( ",_jsp_getExpressionFactory().createValueExpression(" );
                    if ( attr.getEL() != null ) {
                        sb.append ( elContext );
                        sb.append ( ',' );
                    }
                    sb.append ( quote ( attrValue ) );
                    sb.append ( ',' );
                    sb.append ( JspUtil.toJavaSourceTypeFromTld ( attr.getExpectedTypeName() ) );
                    sb.append ( "))" );
                    boolean evaluate = false;
                    if ( tai != null && tai.canBeRequestTime() ) {
                        evaluate = true;
                    }
                    if ( attr.isDeferredInput() ) {
                        evaluate = false;
                    }
                    if ( attr.isDeferredInput() && tai != null &&
                            tai.canBeRequestTime() ) {
                        evaluate = !attrValue.contains ( "#{" );
                    }
                    if ( evaluate ) {
                        sb.append ( ".getValue(" );
                        sb.append ( getJspContextVar() );
                        sb.append ( ".getELContext()" );
                        sb.append ( ")" );
                    }
                    attrValue = sb.toString();
                } else if ( attr.isDeferredMethodInput()
                            || ( ( tai != null ) && MethodExpression.class.getName().equals ( tai.getTypeName() ) ) ) {
                    sb.append ( "new org.apache.jasper.el.JspMethodExpression(" );
                    sb.append ( quote ( mark ) );
                    sb.append ( ",_jsp_getExpressionFactory().createMethodExpression(" );
                    sb.append ( elContext );
                    sb.append ( ',' );
                    sb.append ( quote ( attrValue ) );
                    sb.append ( ',' );
                    sb.append ( JspUtil.toJavaSourceTypeFromTld ( attr.getExpectedTypeName() ) );
                    sb.append ( ',' );
                    sb.append ( "new java.lang.Class[] {" );
                    String[] p = attr.getParameterTypeNames();
                    for ( int i = 0; i < p.length; i++ ) {
                        sb.append ( JspUtil.toJavaSourceTypeFromTld ( p[i] ) );
                        sb.append ( ',' );
                    }
                    if ( p.length > 0 ) {
                        sb.setLength ( sb.length() - 1 );
                    }
                    sb.append ( "}))" );
                    attrValue = sb.toString();
                } else {
                    String mapName = ( attr.getEL() != null ) ? attr.getEL()
                                     .getMapName() : null;
                    attrValue = elInterpreter.interpreterCall ( ctxt,
                                this.isTagFile, attrValue, c[0], mapName );
                }
            } else {
                attrValue = convertString ( c[0], attrValue, localName,
                                            handlerInfo.getPropertyEditorClass ( localName ), false );
            }
            return attrValue;
        }
        private String generateAliasMap ( Node.CustomTag n,
                                          String tagHandlerVar ) {
            TagVariableInfo[] tagVars = n.getTagVariableInfos();
            String aliasMapVar = null;
            boolean aliasSeen = false;
            for ( int i = 0; i < tagVars.length; i++ ) {
                String nameFrom = tagVars[i].getNameFromAttribute();
                if ( nameFrom != null ) {
                    String aliasedName = n.getAttributeValue ( nameFrom );
                    if ( aliasedName == null ) {
                        continue;
                    }
                    if ( !aliasSeen ) {
                        out.printin ( "java.util.HashMap " );
                        aliasMapVar = tagHandlerVar + "_aliasMap";
                        out.print ( aliasMapVar );
                        out.println ( " = new java.util.HashMap();" );
                        aliasSeen = true;
                    }
                    out.printin ( aliasMapVar );
                    out.print ( ".put(" );
                    out.print ( quote ( tagVars[i].getNameGiven() ) );
                    out.print ( ", " );
                    out.print ( quote ( aliasedName ) );
                    out.println ( ");" );
                }
            }
            return aliasMapVar;
        }
        private void generateSetters ( Node.CustomTag n, String tagHandlerVar,
                                       TagHandlerInfo handlerInfo, boolean simpleTag )
        throws JasperException {
            if ( simpleTag ) {
                String aliasMapVar = null;
                if ( n.isTagFile() ) {
                    aliasMapVar = generateAliasMap ( n, tagHandlerVar );
                }
                out.printin ( tagHandlerVar );
                if ( aliasMapVar == null ) {
                    out.println ( ".setJspContext(_jspx_page_context);" );
                } else {
                    out.print ( ".setJspContext(_jspx_page_context, " );
                    out.print ( aliasMapVar );
                    out.println ( ");" );
                }
            } else {
                out.printin ( tagHandlerVar );
                out.println ( ".setPageContext(_jspx_page_context);" );
            }
            if ( isTagFile && parent == null ) {
                out.printin ( tagHandlerVar );
                out.print ( ".setParent(" );
                out.print ( "new javax.servlet.jsp.tagext.TagAdapter(" );
                out.print ( "(javax.servlet.jsp.tagext.SimpleTag) this ));" );
            } else if ( !simpleTag ) {
                out.printin ( tagHandlerVar );
                out.print ( ".setParent(" );
                if ( parent != null ) {
                    if ( isSimpleTagParent ) {
                        out.print ( "new javax.servlet.jsp.tagext.TagAdapter(" );
                        out.print ( "(javax.servlet.jsp.tagext.SimpleTag) " );
                        out.print ( parent );
                        out.println ( "));" );
                    } else {
                        out.print ( "(javax.servlet.jsp.tagext.Tag) " );
                        out.print ( parent );
                        out.println ( ");" );
                    }
                } else {
                    out.println ( "null);" );
                }
            } else {
                if ( parent != null ) {
                    out.printin ( tagHandlerVar );
                    out.print ( ".setParent(" );
                    out.print ( parent );
                    out.println ( ");" );
                }
            }
            Node.JspAttribute[] attrs = n.getJspAttributes();
            for ( int i = 0; attrs != null && i < attrs.length; i++ ) {
                String attrValue = evaluateAttribute ( handlerInfo, attrs[i], n,
                                                       tagHandlerVar );
                Mark m = n.getStart();
                out.printil ( "// " + m.getFile() + "(" + m.getLineNumber() + "," + m.getColumnNumber() + ") " + attrs[i].getTagAttributeInfo() );
                if ( attrs[i].isDynamic() ) {
                    out.printin ( tagHandlerVar );
                    out.print ( "." );
                    out.print ( "setDynamicAttribute(" );
                    String uri = attrs[i].getURI();
                    if ( "".equals ( uri ) || ( uri == null ) ) {
                        out.print ( "null" );
                    } else {
                        out.print ( "\"" + attrs[i].getURI() + "\"" );
                    }
                    out.print ( ", \"" );
                    out.print ( attrs[i].getLocalName() );
                    out.print ( "\", " );
                    out.print ( attrValue );
                    out.println ( ");" );
                } else {
                    out.printin ( tagHandlerVar );
                    out.print ( "." );
                    out.print ( handlerInfo.getSetterMethod (
                                    attrs[i].getLocalName() ).getName() );
                    out.print ( "(" );
                    out.print ( attrValue );
                    out.println ( ");" );
                }
            }
        }
        private String convertString ( Class<?> c, String s, String attrName,
                                       Class<?> propEditorClass, boolean isNamedAttribute ) {
            String quoted = s;
            if ( !isNamedAttribute ) {
                quoted = quote ( s );
            }
            if ( propEditorClass != null ) {
                String className = c.getCanonicalName();
                return "("
                       + className
                       + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromBeanInfoPropertyEditor("
                       + className + ".class, \"" + attrName + "\", " + quoted
                       + ", " + propEditorClass.getCanonicalName() + ".class)";
            } else if ( c == String.class ) {
                return quoted;
            } else if ( c == boolean.class ) {
                return JspUtil.coerceToPrimitiveBoolean ( s, isNamedAttribute );
            } else if ( c == Boolean.class ) {
                return JspUtil.coerceToBoolean ( s, isNamedAttribute );
            } else if ( c == byte.class ) {
                return JspUtil.coerceToPrimitiveByte ( s, isNamedAttribute );
            } else if ( c == Byte.class ) {
                return JspUtil.coerceToByte ( s, isNamedAttribute );
            } else if ( c == char.class ) {
                return JspUtil.coerceToChar ( s, isNamedAttribute );
            } else if ( c == Character.class ) {
                return JspUtil.coerceToCharacter ( s, isNamedAttribute );
            } else if ( c == double.class ) {
                return JspUtil.coerceToPrimitiveDouble ( s, isNamedAttribute );
            } else if ( c == Double.class ) {
                return JspUtil.coerceToDouble ( s, isNamedAttribute );
            } else if ( c == float.class ) {
                return JspUtil.coerceToPrimitiveFloat ( s, isNamedAttribute );
            } else if ( c == Float.class ) {
                return JspUtil.coerceToFloat ( s, isNamedAttribute );
            } else if ( c == int.class ) {
                return JspUtil.coerceToInt ( s, isNamedAttribute );
            } else if ( c == Integer.class ) {
                return JspUtil.coerceToInteger ( s, isNamedAttribute );
            } else if ( c == short.class ) {
                return JspUtil.coerceToPrimitiveShort ( s, isNamedAttribute );
            } else if ( c == Short.class ) {
                return JspUtil.coerceToShort ( s, isNamedAttribute );
            } else if ( c == long.class ) {
                return JspUtil.coerceToPrimitiveLong ( s, isNamedAttribute );
            } else if ( c == Long.class ) {
                return JspUtil.coerceToLong ( s, isNamedAttribute );
            } else if ( c == Object.class ) {
                return quoted;
            } else {
                String className = c.getCanonicalName();
                return "("
                       + className
                       + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromPropertyEditorManager("
                       + className + ".class, \"" + attrName + "\", " + quoted
                       + ")";
            }
        }
        private String getScopeConstant ( String scope ) {
            String scopeName = "javax.servlet.jsp.PageContext.PAGE_SCOPE";
            if ( "request".equals ( scope ) ) {
                scopeName = "javax.servlet.jsp.PageContext.REQUEST_SCOPE";
            } else if ( "session".equals ( scope ) ) {
                scopeName = "javax.servlet.jsp.PageContext.SESSION_SCOPE";
            } else if ( "application".equals ( scope ) ) {
                scopeName = "javax.servlet.jsp.PageContext.APPLICATION_SCOPE";
            }
            return scopeName;
        }
        private void generateJspFragment ( Node n, String tagHandlerVar )
        throws JasperException {
            FragmentHelperClass.Fragment fragment = fragmentHelperClass
                                                    .openFragment ( n, methodNesting );
            ServletWriter outSave = out;
            out = fragment.getGenBuffer().getOut();
            String tmpParent = parent;
            parent = "_jspx_parent";
            boolean isSimpleTagParentSave = isSimpleTagParent;
            isSimpleTagParent = true;
            boolean tmpIsFragment = isFragment;
            isFragment = true;
            String pushBodyCountVarSave = pushBodyCountVar;
            if ( pushBodyCountVar != null ) {
                pushBodyCountVar = "_jspx_push_body_count";
            }
            visitBody ( n );
            out = outSave;
            parent = tmpParent;
            isSimpleTagParent = isSimpleTagParentSave;
            isFragment = tmpIsFragment;
            pushBodyCountVar = pushBodyCountVarSave;
            fragmentHelperClass.closeFragment ( fragment, methodNesting );
            out.print ( "new " + fragmentHelperClass.getClassName() + "( "
                        + fragment.getId() + ", _jspx_page_context, "
                        + tagHandlerVar + ", " + pushBodyCountVar + ")" );
        }
        public String generateNamedAttributeValue ( Node.NamedAttribute n )
        throws JasperException {
            String varName = n.getTemporaryVariableName();
            Node.Nodes body = n.getBody();
            if ( body != null ) {
                boolean templateTextOptimization = false;
                if ( body.size() == 1 ) {
                    Node bodyElement = body.getNode ( 0 );
                    if ( bodyElement instanceof Node.TemplateText ) {
                        templateTextOptimization = true;
                        out.printil ( "java.lang.String "
                                      + varName
                                      + " = "
                                      + quote ( ( ( Node.TemplateText ) bodyElement )
                                                .getText() ) + ";" );
                    }
                }
                if ( !templateTextOptimization ) {
                    out.printil ( "out = _jspx_page_context.pushBody();" );
                    visitBody ( n );
                    out.printil ( "java.lang.String " + varName + " = "
                                  + "((javax.servlet.jsp.tagext.BodyContent)"
                                  + "out).getString();" );
                    out.printil ( "out = _jspx_page_context.popBody();" );
                }
            } else {
                out.printil ( "java.lang.String " + varName + " = \"\";" );
            }
            return varName;
        }
        public String generateNamedAttributeJspFragment ( Node.NamedAttribute n,
                String tagHandlerVar ) throws JasperException {
            String varName = n.getTemporaryVariableName();
            out.printin ( "javax.servlet.jsp.tagext.JspFragment " + varName
                          + " = " );
            generateJspFragment ( n, tagHandlerVar );
            out.println ( ";" );
            return varName;
        }
    }
    private static void generateLocalVariables ( ServletWriter out, Node n )
    throws JasperException {
        Node.ChildInfo ci;
        if ( n instanceof Node.CustomTag ) {
            ci = ( ( Node.CustomTag ) n ).getChildInfo();
        } else if ( n instanceof Node.JspBody ) {
            ci = ( ( Node.JspBody ) n ).getChildInfo();
        } else if ( n instanceof Node.NamedAttribute ) {
            ci = ( ( Node.NamedAttribute ) n ).getChildInfo();
        } else {
            throw new JasperException ( "Unexpected Node Type" );
        }
        if ( ci.hasUseBean() ) {
            out.printil ( "javax.servlet.http.HttpSession session = _jspx_page_context.getSession();" );
            out.printil ( "javax.servlet.ServletContext application = _jspx_page_context.getServletContext();" );
        }
        if ( ci.hasUseBean() || ci.hasIncludeAction() || ci.hasSetProperty()
                || ci.hasParamAction() ) {
            out.printil ( "javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)_jspx_page_context.getRequest();" );
        }
        if ( ci.hasIncludeAction() ) {
            out.printil ( "javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse)_jspx_page_context.getResponse();" );
        }
    }
    private void genCommonPostamble() {
        for ( int i = 0; i < methodsBuffered.size(); i++ ) {
            GenBuffer methodBuffer = methodsBuffered.get ( i );
            methodBuffer.adjustJavaLines ( out.getJavaLine() - 1 );
            out.printMultiLn ( methodBuffer.toString() );
        }
        if ( fragmentHelperClass.isUsed() ) {
            fragmentHelperClass.generatePostamble();
            fragmentHelperClass.adjustJavaLines ( out.getJavaLine() - 1 );
            out.printMultiLn ( fragmentHelperClass.toString() );
        }
        if ( charArrayBuffer != null ) {
            out.printMultiLn ( charArrayBuffer.toString() );
        }
        out.popIndent();
        out.printil ( "}" );
    }
    private void generatePostamble() {
        out.popIndent();
        out.printil ( "} catch (java.lang.Throwable t) {" );
        out.pushIndent();
        out.printil ( "if (!(t instanceof javax.servlet.jsp.SkipPageException)){" );
        out.pushIndent();
        out.printil ( "out = _jspx_out;" );
        out.printil ( "if (out != null && out.getBufferSize() != 0)" );
        out.pushIndent();
        out.printil ( "try {" );
        out.pushIndent();
        out.printil ( "if (response.isCommitted()) {" );
        out.pushIndent();
        out.printil ( "out.flush();" );
        out.popIndent();
        out.printil ( "} else {" );
        out.pushIndent();
        out.printil ( "out.clearBuffer();" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "} catch (java.io.IOException e) {}" );
        out.popIndent();
        out.printil ( "if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);" );
        out.printil ( "else throw new ServletException(t);" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "} finally {" );
        out.pushIndent();
        out.printil ( "_jspxFactory.releasePageContext(_jspx_page_context);" );
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "}" );
        genCommonPostamble();
    }
    Generator ( ServletWriter out, Compiler compiler ) throws JasperException {
        this.out = out;
        methodsBuffered = new ArrayList<>();
        charArrayBuffer = null;
        err = compiler.getErrorDispatcher();
        ctxt = compiler.getCompilationContext();
        fragmentHelperClass = new FragmentHelperClass ( "Helper" );
        pageInfo = compiler.getPageInfo();
        ELInterpreter elInterpreter = null;
        try {
            elInterpreter = ELInterpreterFactory.getELInterpreter (
                                compiler.getCompilationContext().getServletContext() );
        } catch ( Exception e ) {
            err.jspError ( "jsp.error.el_interpreter_class.instantiation",
                           e.getMessage() );
        }
        this.elInterpreter = elInterpreter;
        if ( pageInfo.getExtends ( false ) == null || POOL_TAGS_WITH_EXTENDS ) {
            isPoolingEnabled = ctxt.getOptions().isPoolingEnabled();
        } else {
            isPoolingEnabled = false;
        }
        beanInfo = pageInfo.getBeanRepository();
        varInfoNames = pageInfo.getVarInfoNames();
        breakAtLF = ctxt.getOptions().getMappedFile();
        if ( isPoolingEnabled ) {
            tagHandlerPoolNames = new Vector<>();
        } else {
            tagHandlerPoolNames = null;
        }
        timestampFormat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" );
        timestampFormat.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
    }
    public static void generate ( ServletWriter out, Compiler compiler,
                                  Node.Nodes page ) throws JasperException {
        Generator gen = new Generator ( out, compiler );
        if ( gen.isPoolingEnabled ) {
            gen.compileTagHandlerPoolList ( page );
        }
        gen.generateCommentHeader();
        if ( gen.ctxt.isTagFile() ) {
            JasperTagInfo tagInfo = ( JasperTagInfo ) gen.ctxt.getTagInfo();
            gen.generateTagHandlerPreamble ( tagInfo, page );
            if ( gen.ctxt.isPrototypeMode() ) {
                return;
            }
            gen.generateXmlProlog ( page );
            gen.fragmentHelperClass.generatePreamble();
            page.visit ( gen.new GenerateVisitor ( gen.ctxt.isTagFile(), out,
                                                   gen.methodsBuffered, gen.fragmentHelperClass ) );
            gen.generateTagHandlerPostamble ( tagInfo );
        } else {
            gen.generatePreamble ( page );
            gen.generateXmlProlog ( page );
            gen.fragmentHelperClass.generatePreamble();
            page.visit ( gen.new GenerateVisitor ( gen.ctxt.isTagFile(), out,
                                                   gen.methodsBuffered, gen.fragmentHelperClass ) );
            gen.generatePostamble();
        }
    }
    private void generateCommentHeader() {
        out.println ( "/*" );
        out.println ( " * Generated by the Jasper component of Apache Tomcat" );
        out.println ( " * Version: " + ctxt.getServletContext().getServerInfo() );
        out.println ( " * Generated at: " + timestampFormat.format ( new Date() ) +
                      " UTC" );
        out.println ( " * Note: The last modified time of this file was set to" );
        out.println ( " *       the last modified time of the source file after" );
        out.println ( " *       generation to assist with modification tracking." );
        out.println ( " */" );
    }
    private void generateTagHandlerPreamble ( JasperTagInfo tagInfo,
            Node.Nodes tag ) throws JasperException {
        String className = tagInfo.getTagClassName();
        int lastIndex = className.lastIndexOf ( '.' );
        if ( lastIndex != -1 ) {
            String pkgName = className.substring ( 0, lastIndex );
            genPreamblePackage ( pkgName );
            className = className.substring ( lastIndex + 1 );
        }
        genPreambleImports();
        out.printin ( "public final class " );
        out.println ( className );
        out.printil ( "    extends javax.servlet.jsp.tagext.SimpleTagSupport" );
        out.printin ( "    implements org.apache.jasper.runtime.JspSourceDependent," );
        out.println();
        out.printin ( "                 org.apache.jasper.runtime.JspSourceImports" );
        if ( tagInfo.hasDynamicAttributes() ) {
            out.println ( "," );
            out.printin ( "               javax.servlet.jsp.tagext.DynamicAttributes" );
        }
        out.println ( " {" );
        out.pushIndent();
        generateDeclarations ( tag );
        genPreambleStaticInitializers();
        out.printil ( "private javax.servlet.jsp.JspContext jspContext;" );
        out.printil ( "private java.io.Writer _jspx_sout;" );
        genPreambleClassVariableDeclarations();
        generateSetJspContext ( tagInfo );
        generateTagHandlerAttributes ( tagInfo );
        if ( tagInfo.hasDynamicAttributes() ) {
            generateSetDynamicAttribute();
        }
        genPreambleMethods();
        out.printil ( "public void doTag() throws javax.servlet.jsp.JspException, java.io.IOException {" );
        if ( ctxt.isPrototypeMode() ) {
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            return;
        }
        out.pushIndent();
        out.printil ( "javax.servlet.jsp.PageContext _jspx_page_context = (javax.servlet.jsp.PageContext)jspContext;" );
        out.printil ( "javax.servlet.http.HttpServletRequest request = "
                      + "(javax.servlet.http.HttpServletRequest) _jspx_page_context.getRequest();" );
        out.printil ( "javax.servlet.http.HttpServletResponse response = "
                      + "(javax.servlet.http.HttpServletResponse) _jspx_page_context.getResponse();" );
        out.printil ( "javax.servlet.http.HttpSession session = _jspx_page_context.getSession();" );
        out.printil ( "javax.servlet.ServletContext application = _jspx_page_context.getServletContext();" );
        out.printil ( "javax.servlet.ServletConfig config = _jspx_page_context.getServletConfig();" );
        out.printil ( "javax.servlet.jsp.JspWriter out = jspContext.getOut();" );
        out.printil ( "_jspInit(config);" );
        out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,jspContext);" );
        generatePageScopedVariables ( tagInfo );
        declareTemporaryScriptingVars ( tag );
        out.println();
        out.printil ( "try {" );
        out.pushIndent();
    }
    private void generateTagHandlerPostamble ( TagInfo tagInfo ) {
        out.popIndent();
        out.printil ( "} catch( java.lang.Throwable t ) {" );
        out.pushIndent();
        out.printil ( "if( t instanceof javax.servlet.jsp.SkipPageException )" );
        out.printil ( "    throw (javax.servlet.jsp.SkipPageException) t;" );
        out.printil ( "if( t instanceof java.io.IOException )" );
        out.printil ( "    throw (java.io.IOException) t;" );
        out.printil ( "if( t instanceof java.lang.IllegalStateException )" );
        out.printil ( "    throw (java.lang.IllegalStateException) t;" );
        out.printil ( "if( t instanceof javax.servlet.jsp.JspException )" );
        out.printil ( "    throw (javax.servlet.jsp.JspException) t;" );
        out.printil ( "throw new javax.servlet.jsp.JspException(t);" );
        out.popIndent();
        out.printil ( "} finally {" );
        out.pushIndent();
        TagAttributeInfo[] attrInfos = tagInfo.getAttributes();
        for ( int i = 0; i < attrInfos.length; i++ ) {
            if ( attrInfos[i].isDeferredMethod() || attrInfos[i].isDeferredValue() ) {
                out.printin ( "_el_variablemapper.setVariable(" );
                out.print ( quote ( attrInfos[i].getName() ) );
                out.print ( ",_el_ve" );
                out.print ( i );
                out.println ( ");" );
            }
        }
        out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,super.getJspContext());" );
        out.printil ( "((org.apache.jasper.runtime.JspContextWrapper) jspContext).syncEndTagFile();" );
        if ( isPoolingEnabled && !tagHandlerPoolNames.isEmpty() ) {
            out.printil ( "_jspDestroy();" );
        }
        out.popIndent();
        out.printil ( "}" );
        out.popIndent();
        out.printil ( "}" );
        genCommonPostamble();
    }
    private void generateTagHandlerAttributes ( TagInfo tagInfo ) {
        if ( tagInfo.hasDynamicAttributes() ) {
            out.printil ( "private java.util.HashMap _jspx_dynamic_attrs = new java.util.HashMap();" );
        }
        TagAttributeInfo[] attrInfos = tagInfo.getAttributes();
        for ( int i = 0; i < attrInfos.length; i++ ) {
            out.printin ( "private " );
            if ( attrInfos[i].isFragment() ) {
                out.print ( "javax.servlet.jsp.tagext.JspFragment " );
            } else {
                out.print ( JspUtil.toJavaSourceType ( attrInfos[i].getTypeName() ) );
                out.print ( " " );
            }
            out.print ( JspUtil.makeJavaIdentifierForAttribute (
                            attrInfos[i].getName() ) );
            out.println ( ";" );
        }
        out.println();
        for ( int i = 0; i < attrInfos.length; i++ ) {
            String javaName =
                JspUtil.makeJavaIdentifierForAttribute ( attrInfos[i].getName() );
            out.printin ( "public " );
            if ( attrInfos[i].isFragment() ) {
                out.print ( "javax.servlet.jsp.tagext.JspFragment " );
            } else {
                out.print ( JspUtil.toJavaSourceType ( attrInfos[i].getTypeName() ) );
                out.print ( " " );
            }
            out.print ( toGetterMethod ( attrInfos[i].getName() ) );
            out.println ( " {" );
            out.pushIndent();
            out.printin ( "return this." );
            out.print ( javaName );
            out.println ( ";" );
            out.popIndent();
            out.printil ( "}" );
            out.println();
            out.printin ( "public void " );
            out.print ( toSetterMethodName ( attrInfos[i].getName() ) );
            if ( attrInfos[i].isFragment() ) {
                out.print ( "(javax.servlet.jsp.tagext.JspFragment " );
            } else {
                out.print ( "(" );
                out.print ( JspUtil.toJavaSourceType ( attrInfos[i].getTypeName() ) );
                out.print ( " " );
            }
            out.print ( javaName );
            out.println ( ") {" );
            out.pushIndent();
            out.printin ( "this." );
            out.print ( javaName );
            out.print ( " = " );
            out.print ( javaName );
            out.println ( ";" );
            if ( ctxt.isTagFile() ) {
                out.printin ( "jspContext.setAttribute(\"" );
                out.print ( attrInfos[i].getName() );
                out.print ( "\", " );
                out.print ( javaName );
                out.println ( ");" );
            }
            out.popIndent();
            out.printil ( "}" );
            out.println();
        }
    }
    private void generateSetJspContext ( TagInfo tagInfo ) {
        boolean nestedSeen = false;
        boolean atBeginSeen = false;
        boolean atEndSeen = false;
        boolean aliasSeen = false;
        TagVariableInfo[] tagVars = tagInfo.getTagVariableInfos();
        for ( int i = 0; i < tagVars.length; i++ ) {
            if ( tagVars[i].getNameFromAttribute() != null
                    && tagVars[i].getNameGiven() != null ) {
                aliasSeen = true;
                break;
            }
        }
        if ( aliasSeen ) {
            out.printil ( "public void setJspContext(javax.servlet.jsp.JspContext ctx, java.util.Map aliasMap) {" );
        } else {
            out.printil ( "public void setJspContext(javax.servlet.jsp.JspContext ctx) {" );
        }
        out.pushIndent();
        out.printil ( "super.setJspContext(ctx);" );
        out.printil ( "java.util.ArrayList _jspx_nested = null;" );
        out.printil ( "java.util.ArrayList _jspx_at_begin = null;" );
        out.printil ( "java.util.ArrayList _jspx_at_end = null;" );
        for ( int i = 0; i < tagVars.length; i++ ) {
            switch ( tagVars[i].getScope() ) {
            case VariableInfo.NESTED:
                if ( !nestedSeen ) {
                    out.printil ( "_jspx_nested = new java.util.ArrayList();" );
                    nestedSeen = true;
                }
                out.printin ( "_jspx_nested.add(" );
                break;
            case VariableInfo.AT_BEGIN:
                if ( !atBeginSeen ) {
                    out.printil ( "_jspx_at_begin = new java.util.ArrayList();" );
                    atBeginSeen = true;
                }
                out.printin ( "_jspx_at_begin.add(" );
                break;
            case VariableInfo.AT_END:
                if ( !atEndSeen ) {
                    out.printil ( "_jspx_at_end = new java.util.ArrayList();" );
                    atEndSeen = true;
                }
                out.printin ( "_jspx_at_end.add(" );
                break;
            }
            out.print ( quote ( tagVars[i].getNameGiven() ) );
            out.println ( ");" );
        }
        if ( aliasSeen ) {
            out.printil ( "this.jspContext = new org.apache.jasper.runtime.JspContextWrapper(this, ctx, _jspx_nested, _jspx_at_begin, _jspx_at_end, aliasMap);" );
        } else {
            out.printil ( "this.jspContext = new org.apache.jasper.runtime.JspContextWrapper(this, ctx, _jspx_nested, _jspx_at_begin, _jspx_at_end, null);" );
        }
        out.popIndent();
        out.printil ( "}" );
        out.println();
        out.printil ( "public javax.servlet.jsp.JspContext getJspContext() {" );
        out.pushIndent();
        out.printil ( "return this.jspContext;" );
        out.popIndent();
        out.printil ( "}" );
    }
    public void generateSetDynamicAttribute() {
        out.printil ( "public void setDynamicAttribute(java.lang.String uri, java.lang.String localName, java.lang.Object value) throws javax.servlet.jsp.JspException {" );
        out.pushIndent();
        out.printil ( "if (uri == null)" );
        out.pushIndent();
        out.printil ( "_jspx_dynamic_attrs.put(localName, value);" );
        out.popIndent();
        out.popIndent();
        out.printil ( "}" );
    }
    private void generatePageScopedVariables ( JasperTagInfo tagInfo ) {
        TagAttributeInfo[] attrInfos = tagInfo.getAttributes();
        boolean variableMapperVar = false;
        for ( int i = 0; i < attrInfos.length; i++ ) {
            String attrName = attrInfos[i].getName();
            if ( attrInfos[i].isDeferredValue() || attrInfos[i].isDeferredMethod() ) {
                if ( !variableMapperVar ) {
                    out.printil ( "javax.el.VariableMapper _el_variablemapper = jspContext.getELContext().getVariableMapper();" );
                    variableMapperVar = true;
                }
                out.printin ( "javax.el.ValueExpression _el_ve" );
                out.print ( i );
                out.print ( " = _el_variablemapper.setVariable(" );
                out.print ( quote ( attrName ) );
                out.print ( ',' );
                if ( attrInfos[i].isDeferredMethod() ) {
                    out.print ( "_jsp_getExpressionFactory().createValueExpression(" );
                    out.print ( toGetterMethod ( attrName ) );
                    out.print ( ",javax.el.MethodExpression.class)" );
                } else {
                    out.print ( toGetterMethod ( attrName ) );
                }
                out.println ( ");" );
            } else {
                out.printil ( "if( " + toGetterMethod ( attrName ) + " != null ) " );
                out.pushIndent();
                out.printin ( "_jspx_page_context.setAttribute(" );
                out.print ( quote ( attrName ) );
                out.print ( ", " );
                out.print ( toGetterMethod ( attrName ) );
                out.println ( ");" );
                out.popIndent();
            }
        }
        if ( tagInfo.hasDynamicAttributes() ) {
            out.printin ( "_jspx_page_context.setAttribute(\"" );
            out.print ( tagInfo.getDynamicAttributesMapName() );
            out.print ( "\", _jspx_dynamic_attrs);" );
        }
    }
    private String toGetterMethod ( String attrName ) {
        char[] attrChars = attrName.toCharArray();
        attrChars[0] = Character.toUpperCase ( attrChars[0] );
        return "get" + new String ( attrChars ) + "()";
    }
    private String toSetterMethodName ( String attrName ) {
        char[] attrChars = attrName.toCharArray();
        attrChars[0] = Character.toUpperCase ( attrChars[0] );
        return "set" + new String ( attrChars );
    }
    private static class TagHandlerInfo {
        private Hashtable<String, Method> methodMaps;
        private Hashtable<String, Class<?>> propertyEditorMaps;
        private Class<?> tagHandlerClass;
        TagHandlerInfo ( Node n, Class<?> tagHandlerClass,
                         ErrorDispatcher err ) throws JasperException {
            this.tagHandlerClass = tagHandlerClass;
            this.methodMaps = new Hashtable<>();
            this.propertyEditorMaps = new Hashtable<>();
            try {
                BeanInfo tagClassInfo = Introspector
                                        .getBeanInfo ( tagHandlerClass );
                PropertyDescriptor[] pd = tagClassInfo.getPropertyDescriptors();
                for ( int i = 0; i < pd.length; i++ ) {
                    if ( pd[i].getWriteMethod() != null ) {
                        methodMaps.put ( pd[i].getName(), pd[i].getWriteMethod() );
                    }
                    if ( pd[i].getPropertyEditorClass() != null )
                        propertyEditorMaps.put ( pd[i].getName(), pd[i]
                                                 .getPropertyEditorClass() );
                }
            } catch ( IntrospectionException ie ) {
                err.jspError ( n, ie, "jsp.error.introspect.taghandler",
                               tagHandlerClass.getName() );
            }
        }
        public Method getSetterMethod ( String attrName ) {
            return methodMaps.get ( attrName );
        }
        public Class<?> getPropertyEditorClass ( String attrName ) {
            return propertyEditorMaps.get ( attrName );
        }
        public Class<?> getTagHandlerClass() {
            return tagHandlerClass;
        }
    }
    private static class GenBuffer {
        private Node node;
        private Node.Nodes body;
        private java.io.CharArrayWriter charWriter;
        protected ServletWriter out;
        GenBuffer() {
            this ( null, null );
        }
        GenBuffer ( Node n, Node.Nodes b ) {
            node = n;
            body = b;
            if ( body != null ) {
                body.setGeneratedInBuffer ( true );
            }
            charWriter = new java.io.CharArrayWriter();
            out = new ServletWriter ( new java.io.PrintWriter ( charWriter ) );
        }
        public ServletWriter getOut() {
            return out;
        }
        @Override
        public String toString() {
            return charWriter.toString();
        }
        public void adjustJavaLines ( final int offset ) {
            if ( node != null ) {
                adjustJavaLine ( node, offset );
            }
            if ( body != null ) {
                try {
                    body.visit ( new Node.Visitor() {
                        @Override
                        public void doVisit ( Node n ) {
                            adjustJavaLine ( n, offset );
                        }
                        @Override
                        public void visit ( Node.CustomTag n )
                        throws JasperException {
                            Node.Nodes b = n.getBody();
                            if ( b != null && !b.isGeneratedInBuffer() ) {
                                b.visit ( this );
                            }
                        }
                    } );
                } catch ( JasperException ex ) {
                }
            }
        }
        private static void adjustJavaLine ( Node n, int offset ) {
            if ( n.getBeginJavaLine() > 0 ) {
                n.setBeginJavaLine ( n.getBeginJavaLine() + offset );
                n.setEndJavaLine ( n.getEndJavaLine() + offset );
            }
        }
    }
    private static class FragmentHelperClass {
        private static class Fragment {
            private GenBuffer genBuffer;
            private int id;
            public Fragment ( int id, Node node ) {
                this.id = id;
                genBuffer = new GenBuffer ( null, node.getBody() );
            }
            public GenBuffer getGenBuffer() {
                return this.genBuffer;
            }
            public int getId() {
                return this.id;
            }
        }
        private boolean used = false;
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private String className;
        private GenBuffer classBuffer = new GenBuffer();
        public FragmentHelperClass ( String className ) {
            this.className = className;
        }
        public String getClassName() {
            return this.className;
        }
        public boolean isUsed() {
            return this.used;
        }
        public void generatePreamble() {
            ServletWriter out = this.classBuffer.getOut();
            out.println();
            out.pushIndent();
            out.printil ( "private class " + className );
            out.printil ( "    extends "
                          + "org.apache.jasper.runtime.JspFragmentHelper" );
            out.printil ( "{" );
            out.pushIndent();
            out.printil ( "private javax.servlet.jsp.tagext.JspTag _jspx_parent;" );
            out.printil ( "private int[] _jspx_push_body_count;" );
            out.println();
            out.printil ( "public " + className
                          + "( int discriminator, javax.servlet.jsp.JspContext jspContext, "
                          + "javax.servlet.jsp.tagext.JspTag _jspx_parent, "
                          + "int[] _jspx_push_body_count ) {" );
            out.pushIndent();
            out.printil ( "super( discriminator, jspContext, _jspx_parent );" );
            out.printil ( "this._jspx_parent = _jspx_parent;" );
            out.printil ( "this._jspx_push_body_count = _jspx_push_body_count;" );
            out.popIndent();
            out.printil ( "}" );
        }
        public Fragment openFragment ( Node parent, int methodNesting )
        throws JasperException {
            Fragment result = new Fragment ( fragments.size(), parent );
            fragments.add ( result );
            this.used = true;
            parent.setInnerClassName ( className );
            ServletWriter out = result.getGenBuffer().getOut();
            out.pushIndent();
            out.pushIndent();
            if ( methodNesting > 0 ) {
                out.printin ( "public boolean invoke" );
            } else {
                out.printin ( "public void invoke" );
            }
            out.println ( result.getId() + "( " + "javax.servlet.jsp.JspWriter out ) " );
            out.pushIndent();
            out.printil ( "throws java.lang.Throwable" );
            out.popIndent();
            out.printil ( "{" );
            out.pushIndent();
            generateLocalVariables ( out, parent );
            return result;
        }
        public void closeFragment ( Fragment fragment, int methodNesting ) {
            ServletWriter out = fragment.getGenBuffer().getOut();
            if ( methodNesting > 0 ) {
                out.printil ( "return false;" );
            } else {
                out.printil ( "return;" );
            }
            out.popIndent();
            out.printil ( "}" );
        }
        public void generatePostamble() {
            ServletWriter out = this.classBuffer.getOut();
            for ( int i = 0; i < fragments.size(); i++ ) {
                Fragment fragment = fragments.get ( i );
                fragment.getGenBuffer().adjustJavaLines ( out.getJavaLine() - 1 );
                out.printMultiLn ( fragment.getGenBuffer().toString() );
            }
            out.printil ( "public void invoke( java.io.Writer writer )" );
            out.pushIndent();
            out.printil ( "throws javax.servlet.jsp.JspException" );
            out.popIndent();
            out.printil ( "{" );
            out.pushIndent();
            out.printil ( "javax.servlet.jsp.JspWriter out = null;" );
            out.printil ( "if( writer != null ) {" );
            out.pushIndent();
            out.printil ( "out = this.jspContext.pushBody(writer);" );
            out.popIndent();
            out.printil ( "} else {" );
            out.pushIndent();
            out.printil ( "out = this.jspContext.getOut();" );
            out.popIndent();
            out.printil ( "}" );
            out.printil ( "try {" );
            out.pushIndent();
            out.printil ( "Object _jspx_saved_JspContext = this.jspContext.getELContext().getContext(javax.servlet.jsp.JspContext.class);" );
            out.printil ( "this.jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,this.jspContext);" );
            out.printil ( "switch( this.discriminator ) {" );
            out.pushIndent();
            for ( int i = 0; i < fragments.size(); i++ ) {
                out.printil ( "case " + i + ":" );
                out.pushIndent();
                out.printil ( "invoke" + i + "( out );" );
                out.printil ( "break;" );
                out.popIndent();
            }
            out.popIndent();
            out.printil ( "}" );
            out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,_jspx_saved_JspContext);" );
            out.popIndent();
            out.printil ( "}" );
            out.printil ( "catch( java.lang.Throwable e ) {" );
            out.pushIndent();
            out.printil ( "if (e instanceof javax.servlet.jsp.SkipPageException)" );
            out.printil ( "    throw (javax.servlet.jsp.SkipPageException) e;" );
            out.printil ( "throw new javax.servlet.jsp.JspException( e );" );
            out.popIndent();
            out.printil ( "}" );
            out.printil ( "finally {" );
            out.pushIndent();
            out.printil ( "if( writer != null ) {" );
            out.pushIndent();
            out.printil ( "this.jspContext.popBody();" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
            out.printil ( "}" );
            out.popIndent();
        }
        @Override
        public String toString() {
            return classBuffer.toString();
        }
        public void adjustJavaLines ( int offset ) {
            for ( int i = 0; i < fragments.size(); i++ ) {
                Fragment fragment = fragments.get ( i );
                fragment.getGenBuffer().adjustJavaLines ( offset );
            }
        }
    }
}
