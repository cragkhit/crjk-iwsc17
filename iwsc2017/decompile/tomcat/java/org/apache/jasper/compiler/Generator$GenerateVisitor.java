package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import java.util.List;
import org.apache.jasper.Constants;
import java.util.Enumeration;
import org.xml.sax.Attributes;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import org.apache.jasper.runtime.JspRuntimeLibrary;
import org.apache.jasper.JasperException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Hashtable;
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
    private static final String DOUBLE_QUOTE = "\\\"";
    public GenerateVisitor ( final boolean isTagFile, final ServletWriter out, final ArrayList<GenBuffer> methodsBuffered, final FragmentHelperClass fragmentHelperClass ) {
        this.isTagFile = isTagFile;
        this.out = out;
        this.methodsBuffered = methodsBuffered;
        this.fragmentHelperClass = fragmentHelperClass;
        this.methodNesting = 0;
        this.handlerInfos = new Hashtable<String, Hashtable<String, TagHandlerInfo>>();
        this.tagVarNumbers = new Hashtable<String, Integer>();
        this.textMap = new HashMap<String, String>();
    }
    private String attributeValue ( final Node.JspAttribute attr, final boolean encode, final Class<?> expectedType ) {
        String v = attr.getValue();
        if ( !attr.isNamedAttribute() && v == null ) {
            return "";
        }
        if ( attr.isExpression() ) {
            if ( encode ) {
                return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(String.valueOf(" + v + "), request.getCharacterEncoding())";
            }
            return v;
        } else if ( attr.isELInterpreterInput() ) {
            v = Generator.access$200 ( Generator.this ).interpreterCall ( Generator.access$100 ( Generator.this ), this.isTagFile, v, expectedType, attr.getEL().getMapName() );
            if ( encode ) {
                return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(" + v + ", request.getCharacterEncoding())";
            }
            return v;
        } else {
            if ( attr.isNamedAttribute() ) {
                return attr.getNamedAttributeNode().getTemporaryVariableName();
            }
            if ( encode ) {
                return "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(" + Generator.quote ( v ) + ", request.getCharacterEncoding())";
            }
            return Generator.quote ( v );
        }
    }
    private void printParams ( final Node n, final String pageParam, final boolean literal ) throws JasperException {
        String sep;
        if ( literal ) {
            sep = ( ( pageParam.indexOf ( 63 ) > 0 ) ? "\"&\"" : "\"?\"" );
        } else {
            sep = "((" + pageParam + ").indexOf('?')>0? '&': '?')";
        }
        if ( n.getBody() != null ) {
            class ParamVisitor extends Node.Visitor {
                private String separator = sep;
                @Override
                public void visit ( final Node.ParamAction n ) throws JasperException {
                    GenerateVisitor.this.out.print ( " + " );
                    GenerateVisitor.this.out.print ( this.separator );
                    GenerateVisitor.this.out.print ( " + " );
                    GenerateVisitor.this.out.print ( "org.apache.jasper.runtime.JspRuntimeLibrary.URLEncode(" + Generator.quote ( n.getTextAttribute ( "name" ) ) + ", request.getCharacterEncoding())" );
                    GenerateVisitor.this.out.print ( "+ \"=\" + " );
                    GenerateVisitor.this.out.print ( GenerateVisitor.this.attributeValue ( n.getValue(), true, String.class ) );
                    this.separator = "\"&\"";
                }
            }
            n.getBody().visit ( new ParamVisitor() );
        }
    }
    @Override
    public void visit ( final Node.Expression n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printin ( "out.print(" );
        this.out.printMultiLn ( n.getText() );
        this.out.println ( ");" );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.Scriptlet n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printMultiLn ( n.getText() );
        this.out.println();
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.ELExpression n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        if ( !Generator.access$500 ( Generator.this ).isELIgnored() && n.getEL() != null ) {
            this.out.printil ( "out.write(" + Generator.access$200 ( Generator.this ).interpreterCall ( Generator.access$100 ( Generator.this ), this.isTagFile, n.getType() + "{" + n.getText() + "}", String.class, n.getEL().getMapName() ) + ");" );
        } else {
            this.out.printil ( "out.write(" + Generator.quote ( n.getType() + "{" + n.getText() + "}" ) + ");" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.IncludeAction n ) throws JasperException {
        final String flush = n.getTextAttribute ( "flush" );
        final Node.JspAttribute page = n.getPage();
        boolean isFlush = false;
        if ( "true".equals ( flush ) ) {
            isFlush = true;
        }
        n.setBeginJavaLine ( this.out.getJavaLine() );
        String pageParam;
        if ( page.isNamedAttribute() ) {
            pageParam = this.generateNamedAttributeValue ( page.getNamedAttributeNode() );
        } else {
            pageParam = this.attributeValue ( page, false, String.class );
        }
        final Node jspBody = this.findJspBody ( n );
        if ( jspBody != null ) {
            this.prepareParams ( jspBody );
        } else {
            this.prepareParams ( n );
        }
        this.out.printin ( "org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, " + pageParam );
        this.printParams ( n, pageParam, page.isLiteral() );
        this.out.println ( ", out, " + isFlush + ");" );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    private void prepareParams ( final Node parent ) throws JasperException {
        if ( parent == null ) {
            return;
        }
        final Node.Nodes subelements = parent.getBody();
        if ( subelements != null ) {
            for ( int i = 0; i < subelements.size(); ++i ) {
                final Node n = subelements.getNode ( i );
                if ( n instanceof Node.ParamAction ) {
                    final Node.Nodes paramSubElements = n.getBody();
                    for ( int j = 0; paramSubElements != null && j < paramSubElements.size(); ++j ) {
                        final Node m = paramSubElements.getNode ( j );
                        if ( m instanceof Node.NamedAttribute ) {
                            this.generateNamedAttributeValue ( ( Node.NamedAttribute ) m );
                        }
                    }
                }
            }
        }
    }
    private Node.JspBody findJspBody ( final Node parent ) {
        Node.JspBody result = null;
        final Node.Nodes subelements = parent.getBody();
        for ( int i = 0; subelements != null && i < subelements.size(); ++i ) {
            final Node n = subelements.getNode ( i );
            if ( n instanceof Node.JspBody ) {
                result = ( Node.JspBody ) n;
                break;
            }
        }
        return result;
    }
    @Override
    public void visit ( final Node.ForwardAction n ) throws JasperException {
        final Node.JspAttribute page = n.getPage();
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printil ( "if (true) {" );
        this.out.pushIndent();
        String pageParam;
        if ( page.isNamedAttribute() ) {
            pageParam = this.generateNamedAttributeValue ( page.getNamedAttributeNode() );
        } else {
            pageParam = this.attributeValue ( page, false, String.class );
        }
        final Node jspBody = this.findJspBody ( n );
        if ( jspBody != null ) {
            this.prepareParams ( jspBody );
        } else {
            this.prepareParams ( n );
        }
        this.out.printin ( "_jspx_page_context.forward(" );
        this.out.print ( pageParam );
        this.printParams ( n, pageParam, page.isLiteral() );
        this.out.println ( ");" );
        if ( this.isTagFile || this.isFragment ) {
            this.out.printil ( "throw new javax.servlet.jsp.SkipPageException();" );
        } else {
            this.out.printil ( ( this.methodNesting > 0 ) ? "return true;" : "return;" );
        }
        this.out.popIndent();
        this.out.printil ( "}" );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.GetProperty n ) throws JasperException {
        final String name = n.getTextAttribute ( "name" );
        final String property = n.getTextAttribute ( "property" );
        n.setBeginJavaLine ( this.out.getJavaLine() );
        if ( Generator.access$600 ( Generator.this ).checkVariable ( name ) ) {
            final Class<?> bean = Generator.access$600 ( Generator.this ).getBeanType ( name );
            final String beanName = bean.getCanonicalName();
            final Method meth = JspRuntimeLibrary.getReadMethod ( bean, property );
            final String methodName = meth.getName();
            this.out.printil ( "out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString((((" + beanName + ")_jspx_page_context.findAttribute(\"" + name + "\"))." + methodName + "())));" );
        } else {
            if ( Generator.access$700() && !Generator.access$800 ( Generator.this ).contains ( name ) ) {
                final StringBuilder msg = new StringBuilder();
                msg.append ( "file:" );
                msg.append ( n.getStart() );
                msg.append ( " jsp:getProperty for bean with name '" );
                msg.append ( name );
                msg.append ( "'. Name was not previously introduced as per JSP.5.3" );
                throw new JasperException ( msg.toString() );
            }
            this.out.printil ( "out.write(org.apache.jasper.runtime.JspRuntimeLibrary.toString(org.apache.jasper.runtime.JspRuntimeLibrary.handleGetProperty(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\")));" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.SetProperty n ) throws JasperException {
        final String name = n.getTextAttribute ( "name" );
        final String property = n.getTextAttribute ( "property" );
        String param = n.getTextAttribute ( "param" );
        final Node.JspAttribute value = n.getValue();
        n.setBeginJavaLine ( this.out.getJavaLine() );
        if ( "*".equals ( property ) ) {
            this.out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspect(_jspx_page_context.findAttribute(\"" + name + "\"), request);" );
        } else if ( value == null ) {
            if ( param == null ) {
                param = property;
            }
            this.out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\", request.getParameter(\"" + param + "\"), request, \"" + param + "\", false);" );
        } else if ( value.isExpression() ) {
            this.out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.handleSetProperty(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\"," );
            this.out.print ( this.attributeValue ( value, false, null ) );
            this.out.println ( ");" );
        } else if ( value.isELInterpreterInput() ) {
            this.out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.handleSetPropertyExpression(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\", " + Generator.quote ( value.getValue() ) + ", _jspx_page_context, " + value.getEL().getMapName() + ");" );
        } else if ( value.isNamedAttribute() ) {
            final String valueVarName = this.generateNamedAttributeValue ( value.getNamedAttributeNode() );
            this.out.printil ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\", " + valueVarName + ", null, null, false);" );
        } else {
            this.out.printin ( "org.apache.jasper.runtime.JspRuntimeLibrary.introspecthelper(_jspx_page_context.findAttribute(\"" + name + "\"), \"" + property + "\", " );
            this.out.print ( this.attributeValue ( value, false, null ) );
            this.out.println ( ", null, null, false);" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.UseBean n ) throws JasperException {
        final String name = n.getTextAttribute ( "id" );
        final String scope = n.getTextAttribute ( "scope" );
        final String klass = n.getTextAttribute ( "class" );
        String type = n.getTextAttribute ( "type" );
        final Node.JspAttribute beanName = n.getBeanName();
        boolean generateNew = false;
        String canonicalName = null;
        if ( klass != null ) {
            try {
                final Class<?> bean = Generator.access$100 ( Generator.this ).getClassLoader().loadClass ( klass );
                if ( klass.indexOf ( 36 ) >= 0 ) {
                    canonicalName = bean.getCanonicalName();
                } else {
                    canonicalName = klass;
                }
                final int modifiers = bean.getModifiers();
                if ( !Modifier.isPublic ( modifiers ) || Modifier.isInterface ( modifiers ) || Modifier.isAbstract ( modifiers ) ) {
                    throw new Exception ( "Invalid bean class modifier" );
                }
                bean.getConstructor ( ( Class<?>[] ) new Class[0] );
                generateNew = true;
            } catch ( Exception e ) {
                if ( Generator.access$100 ( Generator.this ).getOptions().getErrorOnUseBeanInvalidClassAttribute() ) {
                    Generator.access$900 ( Generator.this ).jspError ( n, "jsp.error.invalid.bean", klass );
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
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printin ( type );
        this.out.print ( ' ' );
        this.out.print ( name );
        this.out.println ( " = null;" );
        if ( lock != null ) {
            this.out.printin ( "synchronized (" );
            this.out.print ( lock );
            this.out.println ( ") {" );
            this.out.pushIndent();
        }
        this.out.printin ( name );
        this.out.print ( " = (" );
        this.out.print ( type );
        this.out.print ( ") _jspx_page_context.getAttribute(" );
        this.out.print ( Generator.quote ( name ) );
        this.out.print ( ", " );
        this.out.print ( scopename );
        this.out.println ( ");" );
        this.out.printin ( "if (" );
        this.out.print ( name );
        this.out.println ( " == null){" );
        this.out.pushIndent();
        if ( klass == null && beanName == null ) {
            this.out.printin ( "throw new java.lang.InstantiationException(\"bean " );
            this.out.print ( name );
            this.out.println ( " not found within scope\");" );
        } else {
            if ( !generateNew ) {
                String binaryName;
                if ( beanName != null ) {
                    if ( beanName.isNamedAttribute() ) {
                        binaryName = this.generateNamedAttributeValue ( beanName.getNamedAttributeNode() );
                    } else {
                        binaryName = this.attributeValue ( beanName, false, String.class );
                    }
                } else {
                    binaryName = Generator.quote ( klass );
                }
                this.out.printil ( "try {" );
                this.out.pushIndent();
                this.out.printin ( name );
                this.out.print ( " = (" );
                this.out.print ( type );
                this.out.print ( ") java.beans.Beans.instantiate(" );
                this.out.print ( "this.getClass().getClassLoader(), " );
                this.out.print ( binaryName );
                this.out.println ( ");" );
                this.out.popIndent();
                this.out.printil ( "} catch (java.lang.ClassNotFoundException exc) {" );
                this.out.pushIndent();
                this.out.printil ( "throw new InstantiationException(exc.getMessage());" );
                this.out.popIndent();
                this.out.printil ( "} catch (java.lang.Exception exc) {" );
                this.out.pushIndent();
                this.out.printin ( "throw new javax.servlet.ServletException(" );
                this.out.print ( "\"Cannot create bean of class \" + " );
                this.out.print ( binaryName );
                this.out.println ( ", exc);" );
                this.out.popIndent();
                this.out.printil ( "}" );
            } else {
                this.out.printin ( name );
                this.out.print ( " = new " );
                this.out.print ( canonicalName );
                this.out.println ( "();" );
            }
            this.out.printin ( "_jspx_page_context.setAttribute(" );
            this.out.print ( Generator.quote ( name ) );
            this.out.print ( ", " );
            this.out.print ( name );
            this.out.print ( ", " );
            this.out.print ( scopename );
            this.out.println ( ");" );
            this.visitBody ( n );
        }
        this.out.popIndent();
        this.out.printil ( "}" );
        if ( lock != null ) {
            this.out.popIndent();
            this.out.printil ( "}" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    private String makeAttr ( final String attr, final String value ) {
        if ( value == null ) {
            return "";
        }
        return " " + attr + "=\"" + value + '\"';
    }
    @Override
    public void visit ( final Node.PlugIn n ) throws JasperException {
        final String type = n.getTextAttribute ( "type" );
        final String code = n.getTextAttribute ( "code" );
        final String name = n.getTextAttribute ( "name" );
        final Node.JspAttribute height = n.getHeight();
        final Node.JspAttribute width = n.getWidth();
        final String hspace = n.getTextAttribute ( "hspace" );
        final String vspace = n.getTextAttribute ( "vspace" );
        final String align = n.getTextAttribute ( "align" );
        String iepluginurl = n.getTextAttribute ( "iepluginurl" );
        String nspluginurl = n.getTextAttribute ( "nspluginurl" );
        final String codebase = n.getTextAttribute ( "codebase" );
        final String archive = n.getTextAttribute ( "archive" );
        final String jreversion = n.getTextAttribute ( "jreversion" );
        String widthStr = null;
        if ( width != null ) {
            if ( width.isNamedAttribute() ) {
                widthStr = this.generateNamedAttributeValue ( width.getNamedAttributeNode() );
            } else {
                widthStr = this.attributeValue ( width, false, String.class );
            }
        }
        String heightStr = null;
        if ( height != null ) {
            if ( height.isNamedAttribute() ) {
                heightStr = this.generateNamedAttributeValue ( height.getNamedAttributeNode() );
            } else {
                heightStr = this.attributeValue ( height, false, String.class );
            }
        }
        if ( iepluginurl == null ) {
            iepluginurl = "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";
        }
        if ( nspluginurl == null ) {
            nspluginurl = "http://java.sun.com/products/plugin/";
        }
        n.setBeginJavaLine ( this.out.getJavaLine() );
        final Node.JspBody jspBody = this.findJspBody ( n );
        if ( jspBody != null ) {
            final Node.Nodes subelements = jspBody.getBody();
            if ( subelements != null ) {
                for ( int i = 0; i < subelements.size(); ++i ) {
                    final Node m = subelements.getNode ( i );
                    if ( m instanceof Node.ParamsAction ) {
                        this.prepareParams ( m );
                        break;
                    }
                }
            }
        }
        String s0 = "<object" + this.makeAttr ( "classid", Generator.access$100 ( Generator.this ).getOptions().getIeClassId() ) + this.makeAttr ( "name", name );
        String s = "";
        if ( width != null ) {
            s = " + \" width=\\\"\" + " + widthStr + " + \"\\\"\"";
        }
        String s2 = "";
        if ( height != null ) {
            s2 = " + \" height=\\\"\" + " + heightStr + " + \"\\\"\"";
        }
        String s3 = this.makeAttr ( "hspace", hspace ) + this.makeAttr ( "vspace", vspace ) + this.makeAttr ( "align", align ) + this.makeAttr ( "codebase", iepluginurl ) + '>';
        this.out.printil ( "out.write(" + Generator.quote ( s0 ) + s + s2 + " + " + Generator.quote ( s3 ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        s0 = "<param name=\"java_code\"" + this.makeAttr ( "value", code ) + '>';
        this.out.printil ( "out.write(" + Generator.quote ( s0 ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        if ( codebase != null ) {
            s0 = "<param name=\"java_codebase\"" + this.makeAttr ( "value", codebase ) + '>';
            this.out.printil ( "out.write(" + Generator.quote ( s0 ) + ");" );
            this.out.printil ( "out.write(\"\\n\");" );
        }
        if ( archive != null ) {
            s0 = "<param name=\"java_archive\"" + this.makeAttr ( "value", archive ) + '>';
            this.out.printil ( "out.write(" + Generator.quote ( s0 ) + ");" );
            this.out.printil ( "out.write(\"\\n\");" );
        }
        s0 = "<param name=\"type\"" + this.makeAttr ( "value", "application/x-java-" + type + ( ( jreversion == null ) ? "" : ( ";version=" + jreversion ) ) ) + '>';
        this.out.printil ( "out.write(" + Generator.quote ( s0 ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        class ParamVisitor extends Node.Visitor {
            private final boolean ie = true;
            @Override
            public void visit ( final Node.ParamAction n ) throws JasperException {
                String name = n.getTextAttribute ( "name" );
                if ( name.equalsIgnoreCase ( "object" ) ) {
                    name = "java_object";
                } else if ( name.equalsIgnoreCase ( "type" ) ) {
                    name = "java_type";
                }
                n.setBeginJavaLine ( GenerateVisitor.this.out.getJavaLine() );
                if ( this.ie ) {
                    GenerateVisitor.this.out.printil ( "out.write( \"<param name=\\\"" + Generator.escape ( name ) + "\\\" value=\\\"\" + " + GenerateVisitor.this.attributeValue ( n.getValue(), false, String.class ) + " + \"\\\">\" );" );
                    GenerateVisitor.this.out.printil ( "out.write(\"\\n\");" );
                } else {
                    GenerateVisitor.this.out.printil ( "out.write( \" " + Generator.escape ( name ) + "=\\\"\" + " + GenerateVisitor.this.attributeValue ( n.getValue(), false, String.class ) + " + \"\\\"\" );" );
                }
                n.setEndJavaLine ( GenerateVisitor.this.out.getJavaLine() );
            }
        }
        if ( n.getBody() != null ) {
            n.getBody().visit ( new ParamVisitor() );
        }
        this.out.printil ( "out.write(" + Generator.quote ( "<comment>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        s0 = "<EMBED" + this.makeAttr ( "type", "application/x-java-" + type + ( ( jreversion == null ) ? "" : ( ";version=" + jreversion ) ) ) + this.makeAttr ( "name", name );
        s3 = this.makeAttr ( "hspace", hspace ) + this.makeAttr ( "vspace", vspace ) + this.makeAttr ( "align", align ) + this.makeAttr ( "pluginspage", nspluginurl ) + this.makeAttr ( "java_code", code ) + this.makeAttr ( "java_codebase", codebase ) + this.makeAttr ( "java_archive", archive );
        this.out.printil ( "out.write(" + Generator.quote ( s0 ) + s + s2 + " + " + Generator.quote ( s3 ) + ");" );
        if ( n.getBody() != null ) {
            n.getBody().visit ( new ParamVisitor() );
        }
        this.out.printil ( "out.write(" + Generator.quote ( "/>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        this.out.printil ( "out.write(" + Generator.quote ( "<noembed>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        if ( n.getBody() != null ) {
            this.visitBody ( n );
            this.out.printil ( "out.write(\"\\n\");" );
        }
        this.out.printil ( "out.write(" + Generator.quote ( "</noembed>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        this.out.printil ( "out.write(" + Generator.quote ( "</comment>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        this.out.printil ( "out.write(" + Generator.quote ( "</object>" ) + ");" );
        this.out.printil ( "out.write(\"\\n\");" );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.NamedAttribute n ) throws JasperException {
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        if ( n.useTagPlugin() ) {
            this.generateTagPlugin ( n );
            return;
        }
        final TagHandlerInfo handlerInfo = this.getTagHandlerInfo ( n );
        final String baseVar = this.createTagVarName ( n.getQName(), n.getPrefix(), n.getLocalName() );
        final String tagEvalVar = "_jspx_eval_" + baseVar;
        final String tagHandlerVar = "_jspx_th_" + baseVar;
        final String tagPushBodyCountVar = "_jspx_push_body_count_" + baseVar;
        ServletWriter outSave = null;
        final Node.ChildInfo ci = n.getChildInfo();
        if ( ci.isScriptless() && !ci.hasScriptingVars() ) {
            final String tagMethod = "_jspx_meth_" + baseVar;
            this.out.printin ( "if (" );
            this.out.print ( tagMethod );
            this.out.print ( "(" );
            if ( this.parent != null ) {
                this.out.print ( this.parent );
                this.out.print ( ", " );
            }
            this.out.print ( "_jspx_page_context" );
            if ( this.pushBodyCountVar != null ) {
                this.out.print ( ", " );
                this.out.print ( this.pushBodyCountVar );
            }
            this.out.println ( "))" );
            this.out.pushIndent();
            this.out.printil ( ( this.methodNesting > 0 ) ? "return true;" : "return;" );
            this.out.popIndent();
            outSave = this.out;
            final GenBuffer genBuffer = new GenBuffer ( n, n.implementsSimpleTag() ? null : n.getBody() );
            this.methodsBuffered.add ( genBuffer );
            this.out = genBuffer.getOut();
            ++this.methodNesting;
            this.out.println();
            this.out.pushIndent();
            this.out.printin ( "private boolean " );
            this.out.print ( tagMethod );
            this.out.print ( "(" );
            if ( this.parent != null ) {
                this.out.print ( "javax.servlet.jsp.tagext.JspTag " );
                this.out.print ( this.parent );
                this.out.print ( ", " );
            }
            this.out.print ( "javax.servlet.jsp.PageContext _jspx_page_context" );
            if ( this.pushBodyCountVar != null ) {
                this.out.print ( ", int[] " );
                this.out.print ( this.pushBodyCountVar );
            }
            this.out.println ( ")" );
            this.out.printil ( "        throws java.lang.Throwable {" );
            this.out.pushIndent();
            if ( !this.isTagFile ) {
                this.out.printil ( "javax.servlet.jsp.PageContext pageContext = _jspx_page_context;" );
            }
            this.out.printil ( "javax.servlet.jsp.JspWriter out = _jspx_page_context.getOut();" );
            Generator.access$1000 ( this.out, n );
        }
        final VariableInfo[] infos = n.getVariableInfos();
        if ( infos != null && infos.length > 0 ) {
            for ( int i = 0; i < infos.length; ++i ) {
                final VariableInfo info = infos[i];
                if ( info != null && info.getVarName() != null ) {
                    Generator.access$500 ( Generator.this ).getVarInfoNames().add ( info.getVarName() );
                }
            }
        }
        final TagVariableInfo[] tagInfos = n.getTagVariableInfos();
        if ( tagInfos != null && tagInfos.length > 0 ) {
            for ( int j = 0; j < tagInfos.length; ++j ) {
                final TagVariableInfo tagInfo = tagInfos[j];
                if ( tagInfo != null ) {
                    String name = tagInfo.getNameGiven();
                    if ( name == null ) {
                        final String nameFromAttribute = tagInfo.getNameFromAttribute();
                        name = n.getAttributeValue ( nameFromAttribute );
                    }
                    Generator.access$500 ( Generator.this ).getVarInfoNames().add ( name );
                }
            }
        }
        if ( n.implementsSimpleTag() ) {
            this.generateCustomDoTag ( n, handlerInfo, tagHandlerVar );
        } else {
            this.generateCustomStart ( n, handlerInfo, tagHandlerVar, tagEvalVar, tagPushBodyCountVar );
            final String tmpParent = this.parent;
            this.parent = tagHandlerVar;
            final boolean isSimpleTagParentSave = this.isSimpleTagParent;
            this.isSimpleTagParent = false;
            String tmpPushBodyCountVar = null;
            if ( n.implementsTryCatchFinally() ) {
                tmpPushBodyCountVar = this.pushBodyCountVar;
                this.pushBodyCountVar = tagPushBodyCountVar;
            }
            final boolean tmpIsSimpleTagHandler = this.isSimpleTagHandler;
            this.isSimpleTagHandler = false;
            this.visitBody ( n );
            this.parent = tmpParent;
            this.isSimpleTagParent = isSimpleTagParentSave;
            if ( n.implementsTryCatchFinally() ) {
                this.pushBodyCountVar = tmpPushBodyCountVar;
            }
            this.isSimpleTagHandler = tmpIsSimpleTagHandler;
            this.generateCustomEnd ( n, tagHandlerVar, tagEvalVar, tagPushBodyCountVar );
        }
        if ( ci.isScriptless() && !ci.hasScriptingVars() ) {
            if ( this.methodNesting > 0 ) {
                this.out.printil ( "return false;" );
            }
            this.out.popIndent();
            this.out.printil ( "}" );
            this.out.popIndent();
            --this.methodNesting;
            this.out = outSave;
        }
    }
    @Override
    public void visit ( final Node.UninterpretedTag n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printin ( "out.write(\"<" );
        this.out.print ( n.getQName() );
        Attributes attrs = n.getNonTaglibXmlnsAttributes();
        if ( attrs != null ) {
            for ( int i = 0; i < attrs.getLength(); ++i ) {
                this.out.print ( " " );
                this.out.print ( attrs.getQName ( i ) );
                this.out.print ( "=" );
                this.out.print ( "\\\"" );
                this.out.print ( Generator.escape ( attrs.getValue ( i ).replace ( "\"", "&quot;" ) ) );
                this.out.print ( "\\\"" );
            }
        }
        attrs = n.getAttributes();
        if ( attrs != null ) {
            final Node.JspAttribute[] jspAttrs = n.getJspAttributes();
            for ( int j = 0; j < attrs.getLength(); ++j ) {
                this.out.print ( " " );
                this.out.print ( attrs.getQName ( j ) );
                this.out.print ( "=" );
                if ( jspAttrs[j].isELInterpreterInput() ) {
                    this.out.print ( "\\\"\" + " );
                    final String debug = this.attributeValue ( jspAttrs[j], false, String.class );
                    this.out.print ( debug );
                    this.out.print ( " + \"\\\"" );
                } else {
                    this.out.print ( "\\\"" );
                    this.out.print ( Generator.escape ( jspAttrs[j].getValue().replace ( "\"", "&quot;" ) ) );
                    this.out.print ( "\\\"" );
                }
            }
        }
        if ( n.getBody() != null ) {
            this.out.println ( ">\");" );
            this.visitBody ( n );
            this.out.printin ( "out.write(\"</" );
            this.out.print ( n.getQName() );
            this.out.println ( ">\");" );
        } else {
            this.out.println ( "/>\");" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.JspElement n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        final Hashtable<String, String> map = new Hashtable<String, String>();
        final Node.JspAttribute[] attrs = n.getJspAttributes();
        for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
            String value = null;
            String nvp = null;
            if ( attrs[i].isNamedAttribute() ) {
                final Node.NamedAttribute attr = attrs[i].getNamedAttributeNode();
                final Node.JspAttribute omitAttr = attr.getOmit();
                String omit;
                if ( omitAttr == null ) {
                    omit = "false";
                } else {
                    omit = this.attributeValue ( omitAttr, false, Boolean.TYPE );
                    if ( "true".equals ( omit ) ) {
                        continue;
                    }
                }
                value = this.generateNamedAttributeValue ( attrs[i].getNamedAttributeNode() );
                if ( "false".equals ( omit ) ) {
                    nvp = " + \" " + attrs[i].getName() + "=\\\"\" + " + value + " + \"\\\"\"";
                } else {
                    nvp = " + (java.lang.Boolean.valueOf(" + omit + ")?\"\":\" " + attrs[i].getName() + "=\\\"\" + " + value + " + \"\\\"\")";
                }
            } else {
                value = this.attributeValue ( attrs[i], false, Object.class );
                nvp = " + \" " + attrs[i].getName() + "=\\\"\" + " + value + " + \"\\\"\"";
            }
            map.put ( attrs[i].getName(), nvp );
        }
        final String elemName = this.attributeValue ( n.getNameAttribute(), false, String.class );
        this.out.printin ( "out.write(\"<\"" );
        this.out.print ( " + " + elemName );
        final Enumeration<String> enumeration = map.keys();
        while ( enumeration.hasMoreElements() ) {
            final String attrName = enumeration.nextElement();
            this.out.print ( map.get ( attrName ) );
        }
        boolean hasBody = false;
        final Node.Nodes subelements = n.getBody();
        if ( subelements != null ) {
            for ( int j = 0; j < subelements.size(); ++j ) {
                final Node subelem = subelements.getNode ( j );
                if ( ! ( subelem instanceof Node.NamedAttribute ) ) {
                    hasBody = true;
                    break;
                }
            }
        }
        if ( hasBody ) {
            this.out.println ( " + \">\");" );
            n.setEndJavaLine ( this.out.getJavaLine() );
            this.visitBody ( n );
            this.out.printin ( "out.write(\"</\"" );
            this.out.print ( " + " + elemName );
            this.out.println ( " + \">\");" );
        } else {
            this.out.println ( " + \"/>\");" );
            n.setEndJavaLine ( this.out.getJavaLine() );
        }
    }
    @Override
    public void visit ( final Node.TemplateText n ) throws JasperException {
        final String text = n.getText();
        final int textSize = text.length();
        if ( textSize == 0 ) {
            return;
        }
        if ( textSize <= 3 ) {
            n.setBeginJavaLine ( this.out.getJavaLine() );
            int lineInc = 0;
            for ( int i = 0; i < textSize; ++i ) {
                final char ch = text.charAt ( i );
                this.out.printil ( "out.write(" + Generator.quote ( ch ) + ");" );
                if ( i > 0 ) {
                    n.addSmap ( lineInc );
                }
                if ( ch == '\n' ) {
                    ++lineInc;
                }
            }
            n.setEndJavaLine ( this.out.getJavaLine() );
            return;
        }
        if ( Generator.access$100 ( Generator.this ).getOptions().genStringAsCharArray() ) {
            ServletWriter caOut;
            if ( Generator.access$1100 ( Generator.this ) == null ) {
                Generator.access$1102 ( Generator.this, new GenBuffer() );
                caOut = Generator.access$1100 ( Generator.this ).getOut();
                caOut.pushIndent();
                this.textMap = new HashMap<String, String>();
            } else {
                caOut = Generator.access$1100 ( Generator.this ).getOut();
            }
            int len;
            for ( int textIndex = 0, textLength = text.length(); textIndex < textLength; textIndex += len ) {
                len = 0;
                if ( textLength - textIndex > 16384 ) {
                    len = 16384;
                } else {
                    len = textLength - textIndex;
                }
                final String output = text.substring ( textIndex, textIndex + len );
                String charArrayName = this.textMap.get ( output );
                if ( charArrayName == null ) {
                    charArrayName = "_jspx_char_array_" + this.charArrayCount++;
                    this.textMap.put ( output, charArrayName );
                    caOut.printin ( "static char[] " );
                    caOut.print ( charArrayName );
                    caOut.print ( " = " );
                    caOut.print ( Generator.quote ( output ) );
                    caOut.println ( ".toCharArray();" );
                }
                n.setBeginJavaLine ( this.out.getJavaLine() );
                this.out.printil ( "out.write(" + charArrayName + ");" );
                n.setEndJavaLine ( this.out.getJavaLine() );
            }
            return;
        }
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printin();
        final StringBuilder sb = new StringBuilder ( "out.write(\"" );
        final int initLength = sb.length();
        int count = 1024;
        int srcLine = 0;
        for ( int j = 0; j < text.length(); ++j ) {
            final char ch2 = text.charAt ( j );
            --count;
            switch ( ch2 ) {
            case '\"': {
                sb.append ( '\\' ).append ( '\"' );
                break;
            }
            case '\\': {
                sb.append ( '\\' ).append ( '\\' );
                break;
            }
            case '\r': {
                sb.append ( '\\' ).append ( 'r' );
                break;
            }
            case '\n': {
                sb.append ( '\\' ).append ( 'n' );
                ++srcLine;
                if ( Generator.access$1200 ( Generator.this ) || count < 0 ) {
                    sb.append ( "\");" );
                    this.out.println ( sb.toString() );
                    if ( j < text.length() - 1 ) {
                        this.out.printin();
                    }
                    sb.setLength ( initLength );
                    count = 1024;
                }
                n.addSmap ( srcLine );
                break;
            }
            case '\t': {
                sb.append ( '\\' ).append ( 't' );
                break;
            }
            default: {
                sb.append ( ch2 );
                break;
            }
            }
        }
        if ( sb.length() > initLength ) {
            sb.append ( "\");" );
            this.out.println ( sb.toString() );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.JspBody n ) throws JasperException {
        if ( n.getBody() != null ) {
            if ( this.isSimpleTagHandler ) {
                this.out.printin ( this.simpleTagHandlerVar );
                this.out.print ( ".setJspBody(" );
                this.generateJspFragment ( n, this.simpleTagHandlerVar );
                this.out.println ( ");" );
            } else {
                this.visitBody ( n );
            }
        }
    }
    @Override
    public void visit ( final Node.InvokeAction n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printil ( "((org.apache.jasper.runtime.JspContextWrapper) this.jspContext).syncBeforeInvoke();" );
        final String varReaderAttr = n.getTextAttribute ( "varReader" );
        final String varAttr = n.getTextAttribute ( "var" );
        if ( varReaderAttr != null || varAttr != null ) {
            this.out.printil ( "_jspx_sout = new java.io.StringWriter();" );
        } else {
            this.out.printil ( "_jspx_sout = null;" );
        }
        this.out.printin ( "if (" );
        this.out.print ( Generator.access$1300 ( Generator.this, n.getTextAttribute ( "fragment" ) ) );
        this.out.println ( " != null) {" );
        this.out.pushIndent();
        this.out.printin ( Generator.access$1300 ( Generator.this, n.getTextAttribute ( "fragment" ) ) );
        this.out.println ( ".invoke(_jspx_sout);" );
        this.out.popIndent();
        this.out.printil ( "}" );
        if ( varReaderAttr != null || varAttr != null ) {
            final String scopeName = n.getTextAttribute ( "scope" );
            this.out.printin ( "_jspx_page_context.setAttribute(" );
            if ( varReaderAttr != null ) {
                this.out.print ( Generator.quote ( varReaderAttr ) );
                this.out.print ( ", new java.io.StringReader(_jspx_sout.toString())" );
            } else {
                this.out.print ( Generator.quote ( varAttr ) );
                this.out.print ( ", _jspx_sout.toString()" );
            }
            if ( scopeName != null ) {
                this.out.print ( ", " );
                this.out.print ( this.getScopeConstant ( scopeName ) );
            }
            this.out.println ( ");" );
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.DoBodyAction n ) throws JasperException {
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printil ( "((org.apache.jasper.runtime.JspContextWrapper) this.jspContext).syncBeforeInvoke();" );
        final String varReaderAttr = n.getTextAttribute ( "varReader" );
        final String varAttr = n.getTextAttribute ( "var" );
        if ( varReaderAttr != null || varAttr != null ) {
            this.out.printil ( "_jspx_sout = new java.io.StringWriter();" );
        } else {
            this.out.printil ( "_jspx_sout = null;" );
        }
        this.out.printil ( "if (getJspBody() != null)" );
        this.out.pushIndent();
        this.out.printil ( "getJspBody().invoke(_jspx_sout);" );
        this.out.popIndent();
        if ( varReaderAttr != null || varAttr != null ) {
            final String scopeName = n.getTextAttribute ( "scope" );
            this.out.printin ( "_jspx_page_context.setAttribute(" );
            if ( varReaderAttr != null ) {
                this.out.print ( Generator.quote ( varReaderAttr ) );
                this.out.print ( ", new java.io.StringReader(_jspx_sout.toString())" );
            } else {
                this.out.print ( Generator.quote ( varAttr ) );
                this.out.print ( ", _jspx_sout.toString()" );
            }
            if ( scopeName != null ) {
                this.out.print ( ", " );
                this.out.print ( this.getScopeConstant ( scopeName ) );
            }
            this.out.println ( ");" );
        }
        this.out.printil ( "jspContext.getELContext().putContext(javax.servlet.jsp.JspContext.class,getJspContext());" );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    @Override
    public void visit ( final Node.AttributeGenerator n ) throws JasperException {
        final Node.CustomTag tag = n.getTag();
        final Node.JspAttribute[] attrs = tag.getJspAttributes();
        for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
            if ( attrs[i].getName().equals ( n.getName() ) ) {
                this.out.print ( this.evaluateAttribute ( this.getTagHandlerInfo ( tag ), attrs[i], tag, null ) );
                break;
            }
        }
    }
    private TagHandlerInfo getTagHandlerInfo ( final Node.CustomTag n ) throws JasperException {
        Hashtable<String, TagHandlerInfo> handlerInfosByShortName = this.handlerInfos.get ( n.getPrefix() );
        if ( handlerInfosByShortName == null ) {
            handlerInfosByShortName = new Hashtable<String, TagHandlerInfo>();
            this.handlerInfos.put ( n.getPrefix(), handlerInfosByShortName );
        }
        TagHandlerInfo handlerInfo = handlerInfosByShortName.get ( n.getLocalName() );
        if ( handlerInfo == null ) {
            handlerInfo = new TagHandlerInfo ( n, n.getTagHandlerClass(), Generator.access$900 ( Generator.this ) );
            handlerInfosByShortName.put ( n.getLocalName(), handlerInfo );
        }
        return handlerInfo;
    }
    private void generateTagPlugin ( final Node.CustomTag n ) throws JasperException {
        if ( n.getAtSTag() != null ) {
            n.getAtSTag().visit ( this );
        }
        this.visitBody ( n );
        if ( n.getAtETag() != null ) {
            n.getAtETag().visit ( this );
        }
    }
    private void generateCustomStart ( final Node.CustomTag n, final TagHandlerInfo handlerInfo, final String tagHandlerVar, final String tagEvalVar, final String tagPushBodyCountVar ) throws JasperException {
        final Class<?> tagHandlerClass = handlerInfo.getTagHandlerClass();
        this.out.printin ( "//  " );
        this.out.println ( n.getQName() );
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.declareScriptingVars ( n, 1 );
        this.saveScriptingVars ( n, 1 );
        final String tagHandlerClassName = tagHandlerClass.getCanonicalName();
        if ( Generator.access$1400 ( Generator.this ) && !n.implementsJspIdConsumer() ) {
            this.out.printin ( tagHandlerClassName );
            this.out.print ( " " );
            this.out.print ( tagHandlerVar );
            this.out.print ( " = " );
            this.out.print ( "(" );
            this.out.print ( tagHandlerClassName );
            this.out.print ( ") " );
            this.out.print ( n.getTagHandlerPoolName() );
            this.out.print ( ".get(" );
            this.out.print ( tagHandlerClassName );
            this.out.println ( ".class);" );
        } else {
            this.writeNewInstance ( tagHandlerVar, tagHandlerClassName );
        }
        this.out.printil ( "try {" );
        this.out.pushIndent();
        this.generateSetters ( n, tagHandlerVar, handlerInfo, false );
        if ( n.implementsJspIdConsumer() ) {
            this.out.printin ( tagHandlerVar );
            this.out.print ( ".setJspId(\"" );
            this.out.print ( Generator.access$1500 ( Generator.this ) );
            this.out.println ( "\");" );
        }
        if ( n.implementsTryCatchFinally() ) {
            this.out.printin ( "int[] " );
            this.out.print ( tagPushBodyCountVar );
            this.out.println ( " = new int[] { 0 };" );
            this.out.printil ( "try {" );
            this.out.pushIndent();
        }
        this.out.printin ( "int " );
        this.out.print ( tagEvalVar );
        this.out.print ( " = " );
        this.out.print ( tagHandlerVar );
        this.out.println ( ".doStartTag();" );
        if ( !n.implementsBodyTag() ) {
            this.syncScriptingVars ( n, 1 );
        }
        if ( !n.hasEmptyBody() ) {
            this.out.printin ( "if (" );
            this.out.print ( tagEvalVar );
            this.out.println ( " != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {" );
            this.out.pushIndent();
            this.declareScriptingVars ( n, 0 );
            this.saveScriptingVars ( n, 0 );
            if ( n.implementsBodyTag() ) {
                this.out.printin ( "if (" );
                this.out.print ( tagEvalVar );
                this.out.println ( " != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {" );
                this.out.pushIndent();
                this.out.printil ( "out = _jspx_page_context.pushBody();" );
                if ( n.implementsTryCatchFinally() ) {
                    this.out.printin ( tagPushBodyCountVar );
                    this.out.println ( "[0]++;" );
                } else if ( this.pushBodyCountVar != null ) {
                    this.out.printin ( this.pushBodyCountVar );
                    this.out.println ( "[0]++;" );
                }
                this.out.printin ( tagHandlerVar );
                this.out.println ( ".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);" );
                this.out.printin ( tagHandlerVar );
                this.out.println ( ".doInitBody();" );
                this.out.popIndent();
                this.out.printil ( "}" );
                this.syncScriptingVars ( n, 1 );
                this.syncScriptingVars ( n, 0 );
            } else {
                this.syncScriptingVars ( n, 0 );
            }
            if ( n.implementsIterationTag() ) {
                this.out.printil ( "do {" );
                this.out.pushIndent();
            }
        }
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    private void writeNewInstance ( final String tagHandlerVar, final String tagHandlerClassName ) {
        if ( Constants.USE_INSTANCE_MANAGER_FOR_TAGS ) {
            this.out.printin ( tagHandlerClassName );
            this.out.print ( " " );
            this.out.print ( tagHandlerVar );
            this.out.print ( " = (" );
            this.out.print ( tagHandlerClassName );
            this.out.print ( ")" );
            this.out.print ( "_jsp_getInstanceManager().newInstance(\"" );
            this.out.print ( tagHandlerClassName );
            this.out.println ( "\", this.getClass().getClassLoader());" );
        } else {
            this.out.printin ( tagHandlerClassName );
            this.out.print ( " " );
            this.out.print ( tagHandlerVar );
            this.out.print ( " = (" );
            this.out.print ( "new " );
            this.out.print ( tagHandlerClassName );
            this.out.println ( "());" );
            this.out.printin ( "_jsp_getInstanceManager().newInstance(" );
            this.out.print ( tagHandlerVar );
            this.out.println ( ");" );
        }
    }
    private void writeDestroyInstance ( final String tagHandlerVar ) {
        this.out.printin ( "_jsp_getInstanceManager().destroyInstance(" );
        this.out.print ( tagHandlerVar );
        this.out.println ( ");" );
    }
    private void generateCustomEnd ( final Node.CustomTag n, final String tagHandlerVar, final String tagEvalVar, final String tagPushBodyCountVar ) {
        if ( !n.hasEmptyBody() ) {
            if ( n.implementsIterationTag() ) {
                this.out.printin ( "int evalDoAfterBody = " );
                this.out.print ( tagHandlerVar );
                this.out.println ( ".doAfterBody();" );
                this.syncScriptingVars ( n, 1 );
                this.syncScriptingVars ( n, 0 );
                this.out.printil ( "if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)" );
                this.out.pushIndent();
                this.out.printil ( "break;" );
                this.out.popIndent();
                this.out.popIndent();
                this.out.printil ( "} while (true);" );
            }
            this.restoreScriptingVars ( n, 0 );
            if ( n.implementsBodyTag() ) {
                this.out.printin ( "if (" );
                this.out.print ( tagEvalVar );
                this.out.println ( " != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {" );
                this.out.pushIndent();
                this.out.printil ( "out = _jspx_page_context.popBody();" );
                if ( n.implementsTryCatchFinally() ) {
                    this.out.printin ( tagPushBodyCountVar );
                    this.out.println ( "[0]--;" );
                } else if ( this.pushBodyCountVar != null ) {
                    this.out.printin ( this.pushBodyCountVar );
                    this.out.println ( "[0]--;" );
                }
                this.out.popIndent();
                this.out.printil ( "}" );
            }
            this.out.popIndent();
            this.out.printil ( "}" );
        }
        this.out.printin ( "if (" );
        this.out.print ( tagHandlerVar );
        this.out.println ( ".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {" );
        this.out.pushIndent();
        if ( this.isTagFile || this.isFragment ) {
            this.out.printil ( "throw new javax.servlet.jsp.SkipPageException();" );
        } else {
            this.out.printil ( ( this.methodNesting > 0 ) ? "return true;" : "return;" );
        }
        this.out.popIndent();
        this.out.printil ( "}" );
        this.syncScriptingVars ( n, 1 );
        if ( n.implementsTryCatchFinally() ) {
            this.out.popIndent();
            this.out.printil ( "} catch (java.lang.Throwable _jspx_exception) {" );
            this.out.pushIndent();
            this.out.printin ( "while (" );
            this.out.print ( tagPushBodyCountVar );
            this.out.println ( "[0]-- > 0)" );
            this.out.pushIndent();
            this.out.printil ( "out = _jspx_page_context.popBody();" );
            this.out.popIndent();
            this.out.printin ( tagHandlerVar );
            this.out.println ( ".doCatch(_jspx_exception);" );
            this.out.popIndent();
            this.out.printil ( "} finally {" );
            this.out.pushIndent();
            this.out.printin ( tagHandlerVar );
            this.out.println ( ".doFinally();" );
        }
        if ( n.implementsTryCatchFinally() ) {
            this.out.popIndent();
            this.out.printil ( "}" );
        }
        this.out.popIndent();
        this.out.printil ( "} finally {" );
        this.out.pushIndent();
        if ( Generator.access$1400 ( Generator.this ) && !n.implementsJspIdConsumer() ) {
            this.out.printin ( n.getTagHandlerPoolName() );
            this.out.print ( ".reuse(" );
            this.out.print ( tagHandlerVar );
            this.out.println ( ");" );
        } else {
            this.out.printin ( tagHandlerVar );
            this.out.println ( ".release();" );
            this.writeDestroyInstance ( tagHandlerVar );
        }
        this.out.popIndent();
        this.out.printil ( "}" );
        this.declareScriptingVars ( n, 2 );
        this.syncScriptingVars ( n, 2 );
        this.restoreScriptingVars ( n, 1 );
    }
    private void generateCustomDoTag ( final Node.CustomTag n, final TagHandlerInfo handlerInfo, final String tagHandlerVar ) throws JasperException {
        final Class<?> tagHandlerClass = handlerInfo.getTagHandlerClass();
        n.setBeginJavaLine ( this.out.getJavaLine() );
        this.out.printin ( "//  " );
        this.out.println ( n.getQName() );
        this.declareScriptingVars ( n, 1 );
        this.saveScriptingVars ( n, 1 );
        final String tagHandlerClassName = tagHandlerClass.getCanonicalName();
        this.writeNewInstance ( tagHandlerVar, tagHandlerClassName );
        this.generateSetters ( n, tagHandlerVar, handlerInfo, true );
        if ( n.implementsJspIdConsumer() ) {
            this.out.printin ( tagHandlerVar );
            this.out.print ( ".setJspId(\"" );
            this.out.print ( Generator.access$1500 ( Generator.this ) );
            this.out.println ( "\");" );
        }
        if ( this.findJspBody ( n ) == null ) {
            if ( !n.hasEmptyBody() ) {
                this.out.printin ( tagHandlerVar );
                this.out.print ( ".setJspBody(" );
                this.generateJspFragment ( n, tagHandlerVar );
                this.out.println ( ");" );
            }
        } else {
            final String tmpTagHandlerVar = this.simpleTagHandlerVar;
            this.simpleTagHandlerVar = tagHandlerVar;
            final boolean tmpIsSimpleTagHandler = this.isSimpleTagHandler;
            this.isSimpleTagHandler = true;
            this.visitBody ( n );
            this.simpleTagHandlerVar = tmpTagHandlerVar;
            this.isSimpleTagHandler = tmpIsSimpleTagHandler;
        }
        this.out.printin ( tagHandlerVar );
        this.out.println ( ".doTag();" );
        this.restoreScriptingVars ( n, 1 );
        this.syncScriptingVars ( n, 1 );
        this.declareScriptingVars ( n, 2 );
        this.syncScriptingVars ( n, 2 );
        this.writeDestroyInstance ( tagHandlerVar );
        n.setEndJavaLine ( this.out.getJavaLine() );
    }
    private void declareScriptingVars ( final Node.CustomTag n, final int scope ) {
        if ( this.isFragment ) {
            return;
        }
        final List<Object> vec = n.getScriptingVars ( scope );
        if ( vec != null ) {
            for ( int i = 0; i < vec.size(); ++i ) {
                final Object elem = vec.get ( i );
                if ( elem instanceof VariableInfo ) {
                    final VariableInfo varInfo = ( VariableInfo ) elem;
                    if ( varInfo.getDeclare() ) {
                        this.out.printin ( varInfo.getClassName() );
                        this.out.print ( " " );
                        this.out.print ( varInfo.getVarName() );
                        this.out.println ( " = null;" );
                    }
                } else {
                    final TagVariableInfo tagVarInfo = ( TagVariableInfo ) elem;
                    if ( tagVarInfo.getDeclare() ) {
                        String varName = tagVarInfo.getNameGiven();
                        if ( varName == null ) {
                            varName = n.getTagData().getAttributeString ( tagVarInfo.getNameFromAttribute() );
                        } else if ( tagVarInfo.getNameFromAttribute() != null ) {
                            continue;
                        }
                        this.out.printin ( tagVarInfo.getClassName() );
                        this.out.print ( " " );
                        this.out.print ( varName );
                        this.out.println ( " = null;" );
                    }
                }
            }
        }
    }
    private void saveScriptingVars ( final Node.CustomTag n, final int scope ) {
        if ( n.getCustomNestingLevel() == 0 ) {
            return;
        }
        if ( this.isFragment ) {
            return;
        }
        final TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
        final VariableInfo[] varInfos = n.getVariableInfos();
        if ( varInfos.length == 0 && tagVarInfos.length == 0 ) {
            return;
        }
        final List<Object> declaredVariables = n.getScriptingVars ( scope );
        if ( varInfos.length > 0 ) {
            for ( int i = 0; i < varInfos.length; ++i ) {
                if ( varInfos[i].getScope() == scope ) {
                    if ( !declaredVariables.contains ( varInfos[i] ) ) {
                        final String varName = varInfos[i].getVarName();
                        final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                        this.out.printin ( tmpVarName );
                        this.out.print ( " = " );
                        this.out.print ( varName );
                        this.out.println ( ";" );
                    }
                }
            }
        } else {
            for ( int i = 0; i < tagVarInfos.length; ++i ) {
                if ( tagVarInfos[i].getScope() == scope ) {
                    if ( !declaredVariables.contains ( tagVarInfos[i] ) ) {
                        String varName = tagVarInfos[i].getNameGiven();
                        if ( varName == null ) {
                            varName = n.getTagData().getAttributeString ( tagVarInfos[i].getNameFromAttribute() );
                        } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                            continue;
                        }
                        final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                        this.out.printin ( tmpVarName );
                        this.out.print ( " = " );
                        this.out.print ( varName );
                        this.out.println ( ";" );
                    }
                }
            }
        }
    }
    private void restoreScriptingVars ( final Node.CustomTag n, final int scope ) {
        if ( n.getCustomNestingLevel() == 0 ) {
            return;
        }
        if ( this.isFragment ) {
            return;
        }
        final TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
        final VariableInfo[] varInfos = n.getVariableInfos();
        if ( varInfos.length == 0 && tagVarInfos.length == 0 ) {
            return;
        }
        final List<Object> declaredVariables = n.getScriptingVars ( scope );
        if ( varInfos.length > 0 ) {
            for ( int i = 0; i < varInfos.length; ++i ) {
                if ( varInfos[i].getScope() == scope ) {
                    if ( !declaredVariables.contains ( varInfos[i] ) ) {
                        final String varName = varInfos[i].getVarName();
                        final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                        this.out.printin ( varName );
                        this.out.print ( " = " );
                        this.out.print ( tmpVarName );
                        this.out.println ( ";" );
                    }
                }
            }
        } else {
            for ( int i = 0; i < tagVarInfos.length; ++i ) {
                if ( tagVarInfos[i].getScope() == scope ) {
                    if ( !declaredVariables.contains ( tagVarInfos[i] ) ) {
                        String varName = tagVarInfos[i].getNameGiven();
                        if ( varName == null ) {
                            varName = n.getTagData().getAttributeString ( tagVarInfos[i].getNameFromAttribute() );
                        } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                            continue;
                        }
                        final String tmpVarName = "_jspx_" + varName + "_" + n.getCustomNestingLevel();
                        this.out.printin ( varName );
                        this.out.print ( " = " );
                        this.out.print ( tmpVarName );
                        this.out.println ( ";" );
                    }
                }
            }
        }
    }
    private void syncScriptingVars ( final Node.CustomTag n, final int scope ) {
        if ( this.isFragment ) {
            return;
        }
        final TagVariableInfo[] tagVarInfos = n.getTagVariableInfos();
        final VariableInfo[] varInfos = n.getVariableInfos();
        if ( varInfos.length == 0 && tagVarInfos.length == 0 ) {
            return;
        }
        if ( varInfos.length > 0 ) {
            for ( int i = 0; i < varInfos.length; ++i ) {
                if ( varInfos[i].getScope() == scope ) {
                    this.out.printin ( varInfos[i].getVarName() );
                    this.out.print ( " = (" );
                    this.out.print ( varInfos[i].getClassName() );
                    this.out.print ( ") _jspx_page_context.findAttribute(" );
                    this.out.print ( Generator.quote ( varInfos[i].getVarName() ) );
                    this.out.println ( ");" );
                }
            }
        } else {
            for ( int i = 0; i < tagVarInfos.length; ++i ) {
                if ( tagVarInfos[i].getScope() == scope ) {
                    String name = tagVarInfos[i].getNameGiven();
                    if ( name == null ) {
                        name = n.getTagData().getAttributeString ( tagVarInfos[i].getNameFromAttribute() );
                    } else if ( tagVarInfos[i].getNameFromAttribute() != null ) {
                        continue;
                    }
                    this.out.printin ( name );
                    this.out.print ( " = (" );
                    this.out.print ( tagVarInfos[i].getClassName() );
                    this.out.print ( ") _jspx_page_context.findAttribute(" );
                    this.out.print ( Generator.quote ( name ) );
                    this.out.println ( ");" );
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
    private String createTagVarName ( final String fullName, final String prefix, final String shortName ) {
        String varName;
        synchronized ( this.tagVarNumbers ) {
            varName = prefix + "_" + shortName + "_";
            if ( this.tagVarNumbers.get ( fullName ) != null ) {
                final Integer i = this.tagVarNumbers.get ( fullName );
                varName += ( int ) i;
                this.tagVarNumbers.put ( fullName, i + 1 );
            } else {
                this.tagVarNumbers.put ( fullName, 1 );
                varName += "0";
            }
        }
        return JspUtil.makeJavaIdentifier ( varName );
    }
    private String evaluateAttribute ( final TagHandlerInfo handlerInfo, final Node.JspAttribute attr, final Node.CustomTag n, final String tagHandlerVar ) throws JasperException {
        String attrValue = attr.getValue();
        if ( attrValue == null ) {
            if ( !attr.isNamedAttribute() ) {
                return null;
            }
            if ( n.checkIfAttributeIsJspFragment ( attr.getName() ) ) {
                attrValue = this.generateNamedAttributeJspFragment ( attr.getNamedAttributeNode(), tagHandlerVar );
            } else {
                attrValue = this.generateNamedAttributeValue ( attr.getNamedAttributeNode() );
            }
        }
        final String localName = attr.getLocalName();
        Method m = null;
        Class<?>[] c = null;
        if ( attr.isDynamic() ) {
            c = ( Class<?>[] ) Generator.access$1600();
        } else {
            m = handlerInfo.getSetterMethod ( localName );
            if ( m == null ) {
                Generator.access$900 ( Generator.this ).jspError ( n, "jsp.error.unable.to_find_method", attr.getName() );
            }
            c = m.getParameterTypes();
        }
        if ( !attr.isExpression() ) {
            if ( attr.isNamedAttribute() ) {
                if ( !n.checkIfAttributeIsJspFragment ( attr.getName() ) && !attr.isDynamic() ) {
                    attrValue = this.convertString ( c[0], attrValue, localName, handlerInfo.getPropertyEditorClass ( localName ), true );
                }
            } else if ( attr.isELInterpreterInput() ) {
                final StringBuilder sb = new StringBuilder ( 64 );
                final TagAttributeInfo tai = attr.getTagAttributeInfo();
                sb.append ( this.getJspContextVar() );
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
                final String mark = sb.toString();
                sb.setLength ( 0 );
                if ( attr.isDeferredInput() || ( tai != null && ValueExpression.class.getName().equals ( tai.getTypeName() ) ) ) {
                    sb.append ( "new org.apache.jasper.el.JspValueExpression(" );
                    sb.append ( Generator.quote ( mark ) );
                    sb.append ( ",_jsp_getExpressionFactory().createValueExpression(" );
                    if ( attr.getEL() != null ) {
                        sb.append ( elContext );
                        sb.append ( ',' );
                    }
                    sb.append ( Generator.quote ( attrValue ) );
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
                    if ( attr.isDeferredInput() && tai != null && tai.canBeRequestTime() ) {
                        evaluate = !attrValue.contains ( "#{" );
                    }
                    if ( evaluate ) {
                        sb.append ( ".getValue(" );
                        sb.append ( this.getJspContextVar() );
                        sb.append ( ".getELContext()" );
                        sb.append ( ")" );
                    }
                    attrValue = sb.toString();
                } else if ( attr.isDeferredMethodInput() || ( tai != null && MethodExpression.class.getName().equals ( tai.getTypeName() ) ) ) {
                    sb.append ( "new org.apache.jasper.el.JspMethodExpression(" );
                    sb.append ( Generator.quote ( mark ) );
                    sb.append ( ",_jsp_getExpressionFactory().createMethodExpression(" );
                    sb.append ( elContext );
                    sb.append ( ',' );
                    sb.append ( Generator.quote ( attrValue ) );
                    sb.append ( ',' );
                    sb.append ( JspUtil.toJavaSourceTypeFromTld ( attr.getExpectedTypeName() ) );
                    sb.append ( ',' );
                    sb.append ( "new java.lang.Class[] {" );
                    final String[] p = attr.getParameterTypeNames();
                    for ( int i = 0; i < p.length; ++i ) {
                        sb.append ( JspUtil.toJavaSourceTypeFromTld ( p[i] ) );
                        sb.append ( ',' );
                    }
                    if ( p.length > 0 ) {
                        sb.setLength ( sb.length() - 1 );
                    }
                    sb.append ( "}))" );
                    attrValue = sb.toString();
                } else {
                    final String mapName = ( attr.getEL() != null ) ? attr.getEL().getMapName() : null;
                    attrValue = Generator.access$200 ( Generator.this ).interpreterCall ( Generator.access$100 ( Generator.this ), this.isTagFile, attrValue, c[0], mapName );
                }
            } else {
                attrValue = this.convertString ( c[0], attrValue, localName, handlerInfo.getPropertyEditorClass ( localName ), false );
            }
        }
        return attrValue;
    }
    private String generateAliasMap ( final Node.CustomTag n, final String tagHandlerVar ) {
        final TagVariableInfo[] tagVars = n.getTagVariableInfos();
        String aliasMapVar = null;
        boolean aliasSeen = false;
        for ( int i = 0; i < tagVars.length; ++i ) {
            final String nameFrom = tagVars[i].getNameFromAttribute();
            if ( nameFrom != null ) {
                final String aliasedName = n.getAttributeValue ( nameFrom );
                if ( aliasedName != null ) {
                    if ( !aliasSeen ) {
                        this.out.printin ( "java.util.HashMap " );
                        aliasMapVar = tagHandlerVar + "_aliasMap";
                        this.out.print ( aliasMapVar );
                        this.out.println ( " = new java.util.HashMap();" );
                        aliasSeen = true;
                    }
                    this.out.printin ( aliasMapVar );
                    this.out.print ( ".put(" );
                    this.out.print ( Generator.quote ( tagVars[i].getNameGiven() ) );
                    this.out.print ( ", " );
                    this.out.print ( Generator.quote ( aliasedName ) );
                    this.out.println ( ");" );
                }
            }
        }
        return aliasMapVar;
    }
    private void generateSetters ( final Node.CustomTag n, final String tagHandlerVar, final TagHandlerInfo handlerInfo, final boolean simpleTag ) throws JasperException {
        if ( simpleTag ) {
            String aliasMapVar = null;
            if ( n.isTagFile() ) {
                aliasMapVar = this.generateAliasMap ( n, tagHandlerVar );
            }
            this.out.printin ( tagHandlerVar );
            if ( aliasMapVar == null ) {
                this.out.println ( ".setJspContext(_jspx_page_context);" );
            } else {
                this.out.print ( ".setJspContext(_jspx_page_context, " );
                this.out.print ( aliasMapVar );
                this.out.println ( ");" );
            }
        } else {
            this.out.printin ( tagHandlerVar );
            this.out.println ( ".setPageContext(_jspx_page_context);" );
        }
        if ( this.isTagFile && this.parent == null ) {
            this.out.printin ( tagHandlerVar );
            this.out.print ( ".setParent(" );
            this.out.print ( "new javax.servlet.jsp.tagext.TagAdapter(" );
            this.out.print ( "(javax.servlet.jsp.tagext.SimpleTag) this ));" );
        } else if ( !simpleTag ) {
            this.out.printin ( tagHandlerVar );
            this.out.print ( ".setParent(" );
            if ( this.parent != null ) {
                if ( this.isSimpleTagParent ) {
                    this.out.print ( "new javax.servlet.jsp.tagext.TagAdapter(" );
                    this.out.print ( "(javax.servlet.jsp.tagext.SimpleTag) " );
                    this.out.print ( this.parent );
                    this.out.println ( "));" );
                } else {
                    this.out.print ( "(javax.servlet.jsp.tagext.Tag) " );
                    this.out.print ( this.parent );
                    this.out.println ( ");" );
                }
            } else {
                this.out.println ( "null);" );
            }
        } else if ( this.parent != null ) {
            this.out.printin ( tagHandlerVar );
            this.out.print ( ".setParent(" );
            this.out.print ( this.parent );
            this.out.println ( ");" );
        }
        final Node.JspAttribute[] attrs = n.getJspAttributes();
        for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
            final String attrValue = this.evaluateAttribute ( handlerInfo, attrs[i], n, tagHandlerVar );
            final Mark m = n.getStart();
            this.out.printil ( "// " + m.getFile() + "(" + m.getLineNumber() + "," + m.getColumnNumber() + ") " + attrs[i].getTagAttributeInfo() );
            if ( attrs[i].isDynamic() ) {
                this.out.printin ( tagHandlerVar );
                this.out.print ( "." );
                this.out.print ( "setDynamicAttribute(" );
                final String uri = attrs[i].getURI();
                if ( "".equals ( uri ) || uri == null ) {
                    this.out.print ( "null" );
                } else {
                    this.out.print ( "\"" + attrs[i].getURI() + "\"" );
                }
                this.out.print ( ", \"" );
                this.out.print ( attrs[i].getLocalName() );
                this.out.print ( "\", " );
                this.out.print ( attrValue );
                this.out.println ( ");" );
            } else {
                this.out.printin ( tagHandlerVar );
                this.out.print ( "." );
                this.out.print ( handlerInfo.getSetterMethod ( attrs[i].getLocalName() ).getName() );
                this.out.print ( "(" );
                this.out.print ( attrValue );
                this.out.println ( ");" );
            }
        }
    }
    private String convertString ( final Class<?> c, final String s, final String attrName, final Class<?> propEditorClass, final boolean isNamedAttribute ) {
        String quoted = s;
        if ( !isNamedAttribute ) {
            quoted = Generator.quote ( s );
        }
        if ( propEditorClass != null ) {
            final String className = c.getCanonicalName();
            return "(" + className + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromBeanInfoPropertyEditor(" + className + ".class, \"" + attrName + "\", " + quoted + ", " + propEditorClass.getCanonicalName() + ".class)";
        }
        if ( c == String.class ) {
            return quoted;
        }
        if ( c == Boolean.TYPE ) {
            return JspUtil.coerceToPrimitiveBoolean ( s, isNamedAttribute );
        }
        if ( c == Boolean.class ) {
            return JspUtil.coerceToBoolean ( s, isNamedAttribute );
        }
        if ( c == Byte.TYPE ) {
            return JspUtil.coerceToPrimitiveByte ( s, isNamedAttribute );
        }
        if ( c == Byte.class ) {
            return JspUtil.coerceToByte ( s, isNamedAttribute );
        }
        if ( c == Character.TYPE ) {
            return JspUtil.coerceToChar ( s, isNamedAttribute );
        }
        if ( c == Character.class ) {
            return JspUtil.coerceToCharacter ( s, isNamedAttribute );
        }
        if ( c == Double.TYPE ) {
            return JspUtil.coerceToPrimitiveDouble ( s, isNamedAttribute );
        }
        if ( c == Double.class ) {
            return JspUtil.coerceToDouble ( s, isNamedAttribute );
        }
        if ( c == Float.TYPE ) {
            return JspUtil.coerceToPrimitiveFloat ( s, isNamedAttribute );
        }
        if ( c == Float.class ) {
            return JspUtil.coerceToFloat ( s, isNamedAttribute );
        }
        if ( c == Integer.TYPE ) {
            return JspUtil.coerceToInt ( s, isNamedAttribute );
        }
        if ( c == Integer.class ) {
            return JspUtil.coerceToInteger ( s, isNamedAttribute );
        }
        if ( c == Short.TYPE ) {
            return JspUtil.coerceToPrimitiveShort ( s, isNamedAttribute );
        }
        if ( c == Short.class ) {
            return JspUtil.coerceToShort ( s, isNamedAttribute );
        }
        if ( c == Long.TYPE ) {
            return JspUtil.coerceToPrimitiveLong ( s, isNamedAttribute );
        }
        if ( c == Long.class ) {
            return JspUtil.coerceToLong ( s, isNamedAttribute );
        }
        if ( c == Object.class ) {
            return quoted;
        }
        final String className = c.getCanonicalName();
        return "(" + className + ")org.apache.jasper.runtime.JspRuntimeLibrary.getValueFromPropertyEditorManager(" + className + ".class, \"" + attrName + "\", " + quoted + ")";
    }
    private String getScopeConstant ( final String scope ) {
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
    private void generateJspFragment ( final Node n, final String tagHandlerVar ) throws JasperException {
        final FragmentHelperClass.Fragment fragment = this.fragmentHelperClass.openFragment ( n, this.methodNesting );
        final ServletWriter outSave = this.out;
        this.out = fragment.getGenBuffer().getOut();
        final String tmpParent = this.parent;
        this.parent = "_jspx_parent";
        final boolean isSimpleTagParentSave = this.isSimpleTagParent;
        this.isSimpleTagParent = true;
        final boolean tmpIsFragment = this.isFragment;
        this.isFragment = true;
        final String pushBodyCountVarSave = this.pushBodyCountVar;
        if ( this.pushBodyCountVar != null ) {
            this.pushBodyCountVar = "_jspx_push_body_count";
        }
        this.visitBody ( n );
        this.out = outSave;
        this.parent = tmpParent;
        this.isSimpleTagParent = isSimpleTagParentSave;
        this.isFragment = tmpIsFragment;
        this.pushBodyCountVar = pushBodyCountVarSave;
        this.fragmentHelperClass.closeFragment ( fragment, this.methodNesting );
        this.out.print ( "new " + this.fragmentHelperClass.getClassName() + "( " + fragment.getId() + ", _jspx_page_context, " + tagHandlerVar + ", " + this.pushBodyCountVar + ")" );
    }
    public String generateNamedAttributeValue ( final Node.NamedAttribute n ) throws JasperException {
        final String varName = n.getTemporaryVariableName();
        final Node.Nodes body = n.getBody();
        if ( body != null ) {
            boolean templateTextOptimization = false;
            if ( body.size() == 1 ) {
                final Node bodyElement = body.getNode ( 0 );
                if ( bodyElement instanceof Node.TemplateText ) {
                    templateTextOptimization = true;
                    this.out.printil ( "java.lang.String " + varName + " = " + Generator.quote ( ( ( Node.TemplateText ) bodyElement ).getText() ) + ";" );
                }
            }
            if ( !templateTextOptimization ) {
                this.out.printil ( "out = _jspx_page_context.pushBody();" );
                this.visitBody ( n );
                this.out.printil ( "java.lang.String " + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();" );
                this.out.printil ( "out = _jspx_page_context.popBody();" );
            }
        } else {
            this.out.printil ( "java.lang.String " + varName + " = \"\";" );
        }
        return varName;
    }
    public String generateNamedAttributeJspFragment ( final Node.NamedAttribute n, final String tagHandlerVar ) throws JasperException {
        final String varName = n.getTemporaryVariableName();
        this.out.printin ( "javax.servlet.jsp.tagext.JspFragment " + varName + " = " );
        this.generateJspFragment ( n, tagHandlerVar );
        this.out.println ( ";" );
        return varName;
    }
}
