package org.apache.jasper.compiler;
import java.util.Iterator;
import javax.el.ELException;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import java.util.ArrayList;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
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
import java.util.List;
import java.util.Vector;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
import javax.servlet.jsp.tagext.VariableInfo;
abstract class Node implements TagConstants {
    private static final VariableInfo[] ZERO_VARIABLE_INFO;
    protected Attributes attrs;
    protected Attributes taglibAttrs;
    protected Attributes nonTaglibXmlnsAttrs;
    protected Nodes body;
    protected String text;
    protected Mark startMark;
    protected int beginJavaLine;
    protected int endJavaLine;
    protected Node parent;
    protected Nodes namedAttributeNodes;
    protected String qName;
    protected String localName;
    protected String innerClassName;
    public Node() {
    }
    public Node ( final Mark start, final Node parent ) {
        this.startMark = start;
        this.addToParent ( parent );
    }
    public Node ( final String qName, final String localName, final Attributes attrs, final Mark start, final Node parent ) {
        this.qName = qName;
        this.localName = localName;
        this.attrs = attrs;
        this.startMark = start;
        this.addToParent ( parent );
    }
    public Node ( final String qName, final String localName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
        this.qName = qName;
        this.localName = localName;
        this.attrs = attrs;
        this.nonTaglibXmlnsAttrs = nonTaglibXmlnsAttrs;
        this.taglibAttrs = taglibAttrs;
        this.startMark = start;
        this.addToParent ( parent );
    }
    public Node ( final String qName, final String localName, final String text, final Mark start, final Node parent ) {
        this.qName = qName;
        this.localName = localName;
        this.text = text;
        this.startMark = start;
        this.addToParent ( parent );
    }
    public String getQName() {
        return this.qName;
    }
    public String getLocalName() {
        return this.localName;
    }
    public Attributes getAttributes() {
        return this.attrs;
    }
    public Attributes getTaglibAttributes() {
        return this.taglibAttrs;
    }
    public Attributes getNonTaglibXmlnsAttributes() {
        return this.nonTaglibXmlnsAttrs;
    }
    public void setAttributes ( final Attributes attrs ) {
        this.attrs = attrs;
    }
    public String getAttributeValue ( final String name ) {
        return ( this.attrs == null ) ? null : this.attrs.getValue ( name );
    }
    public String getTextAttribute ( final String name ) {
        final String attr = this.getAttributeValue ( name );
        if ( attr != null ) {
            return attr;
        }
        final NamedAttribute namedAttribute = this.getNamedAttributeNode ( name );
        if ( namedAttribute == null ) {
            return null;
        }
        return namedAttribute.getText();
    }
    public NamedAttribute getNamedAttributeNode ( final String name ) {
        NamedAttribute result = null;
        final Nodes nodes = this.getNamedAttributeNodes();
        for ( int numChildNodes = nodes.size(), i = 0; i < numChildNodes; ++i ) {
            final NamedAttribute na = ( NamedAttribute ) nodes.getNode ( i );
            boolean found = false;
            final int index = name.indexOf ( 58 );
            if ( index != -1 ) {
                found = na.getName().equals ( name );
            } else {
                found = na.getLocalName().equals ( name );
            }
            if ( found ) {
                result = na;
                break;
            }
        }
        return result;
    }
    public Nodes getNamedAttributeNodes() {
        if ( this.namedAttributeNodes != null ) {
            return this.namedAttributeNodes;
        }
        final Nodes result = new Nodes();
        final Nodes nodes = this.getBody();
        if ( nodes != null ) {
            for ( int numChildNodes = nodes.size(), i = 0; i < numChildNodes; ++i ) {
                final Node n = nodes.getNode ( i );
                if ( n instanceof NamedAttribute ) {
                    result.add ( n );
                } else if ( ! ( n instanceof Comment ) ) {
                    break;
                }
            }
        }
        return this.namedAttributeNodes = result;
    }
    public Nodes getBody() {
        return this.body;
    }
    public void setBody ( final Nodes body ) {
        this.body = body;
    }
    public String getText() {
        return this.text;
    }
    public Mark getStart() {
        return this.startMark;
    }
    public Node getParent() {
        return this.parent;
    }
    public int getBeginJavaLine() {
        return this.beginJavaLine;
    }
    public void setBeginJavaLine ( final int begin ) {
        this.beginJavaLine = begin;
    }
    public int getEndJavaLine() {
        return this.endJavaLine;
    }
    public void setEndJavaLine ( final int end ) {
        this.endJavaLine = end;
    }
    public Root getRoot() {
        Node n;
        for ( n = this; ! ( n instanceof Root ); n = n.getParent() ) {}
        return ( Root ) n;
    }
    public String getInnerClassName() {
        return this.innerClassName;
    }
    public void setInnerClassName ( final String icn ) {
        this.innerClassName = icn;
    }
    abstract void accept ( final Visitor p0 ) throws JasperException;
    private void addToParent ( final Node parent ) {
        if ( parent != null ) {
            this.parent = parent;
            Nodes parentBody = parent.getBody();
            if ( parentBody == null ) {
                parentBody = new Nodes();
                parent.setBody ( parentBody );
            }
            parentBody.add ( this );
        }
    }
    static {
        ZERO_VARIABLE_INFO = new VariableInfo[0];
    }
    public static class Root extends Node {
        private final Root parentRoot;
        private final boolean isXmlSyntax;
        private String pageEnc;
        private String jspConfigPageEnc;
        private boolean isDefaultPageEncoding;
        private boolean isEncodingSpecifiedInProlog;
        private boolean isBomPresent;
        private int tempSequenceNumber;
        Root ( final Mark start, final Node parent, final boolean isXmlSyntax ) {
            super ( start, parent );
            this.tempSequenceNumber = 0;
            this.isXmlSyntax = isXmlSyntax;
            this.qName = "jsp:root";
            this.localName = "root";
            Node r;
            for ( r = parent; r != null && ! ( r instanceof Root ); r = r.getParent() ) {}
            this.parentRoot = ( Root ) r;
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public boolean isXmlSyntax() {
            return this.isXmlSyntax;
        }
        public void setJspConfigPageEncoding ( final String enc ) {
            this.jspConfigPageEnc = enc;
        }
        public String getJspConfigPageEncoding() {
            return this.jspConfigPageEnc;
        }
        public void setPageEncoding ( final String enc ) {
            this.pageEnc = enc;
        }
        public String getPageEncoding() {
            return this.pageEnc;
        }
        public void setIsDefaultPageEncoding ( final boolean isDefault ) {
            this.isDefaultPageEncoding = isDefault;
        }
        public boolean isDefaultPageEncoding() {
            return this.isDefaultPageEncoding;
        }
        public void setIsEncodingSpecifiedInProlog ( final boolean isSpecified ) {
            this.isEncodingSpecifiedInProlog = isSpecified;
        }
        public boolean isEncodingSpecifiedInProlog() {
            return this.isEncodingSpecifiedInProlog;
        }
        public void setIsBomPresent ( final boolean isBom ) {
            this.isBomPresent = isBom;
        }
        public boolean isBomPresent() {
            return this.isBomPresent;
        }
        public String nextTemporaryVariableName() {
            if ( this.parentRoot == null ) {
                return Constants.TEMP_VARIABLE_NAME_PREFIX + this.tempSequenceNumber++;
            }
            return this.parentRoot.nextTemporaryVariableName();
        }
    }
    public static class JspRoot extends Node {
        public JspRoot ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "root", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class PageDirective extends Node {
        private final Vector<String> imports;
        public PageDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:directive.page", attrs, null, null, start, parent );
        }
        public PageDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "directive.page", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.imports = new Vector<String>();
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void addImport ( final String value ) {
            int start;
            int index;
            for ( start = 0; ( index = value.indexOf ( 44, start ) ) != -1; start = index + 1 ) {
                this.imports.add ( this.validateImport ( value.substring ( start, index ) ) );
            }
            if ( start == 0 ) {
                this.imports.add ( this.validateImport ( value ) );
            } else {
                this.imports.add ( this.validateImport ( value.substring ( start ) ) );
            }
        }
        public List<String> getImports() {
            return this.imports;
        }
        private String validateImport ( final String importEntry ) {
            if ( importEntry.indexOf ( 59 ) > -1 ) {
                throw new IllegalArgumentException ( Localizer.getMessage ( "jsp.error.page.invalid.import" ) );
            }
            return importEntry.trim();
        }
    }
    public static class IncludeDirective extends Node {
        public IncludeDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:directive.include", attrs, null, null, start, parent );
        }
        public IncludeDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "directive.include", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class TaglibDirective extends Node {
        public TaglibDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            super ( "jsp:taglib", "taglib", attrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class TagDirective extends Node {
        private final Vector<String> imports;
        public TagDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:directive.tag", attrs, null, null, start, parent );
        }
        public TagDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "directive.tag", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.imports = new Vector<String>();
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void addImport ( final String value ) {
            int start;
            int index;
            for ( start = 0; ( index = value.indexOf ( 44, start ) ) != -1; start = index + 1 ) {
                this.imports.add ( value.substring ( start, index ).trim() );
            }
            if ( start == 0 ) {
                this.imports.add ( value.trim() );
            } else {
                this.imports.add ( value.substring ( start ).trim() );
            }
        }
        public List<String> getImports() {
            return this.imports;
        }
    }
    public static class AttributeDirective extends Node {
        public AttributeDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:directive.attribute", attrs, null, null, start, parent );
        }
        public AttributeDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "directive.attribute", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class VariableDirective extends Node {
        public VariableDirective ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:directive.variable", attrs, null, null, start, parent );
        }
        public VariableDirective ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "directive.variable", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class InvokeAction extends Node {
        public InvokeAction ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:invoke", attrs, null, null, start, parent );
        }
        public InvokeAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "invoke", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class DoBodyAction extends Node {
        public DoBodyAction ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:doBody", attrs, null, null, start, parent );
        }
        public DoBodyAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "doBody", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class Comment extends Node {
        public Comment ( final String text, final Mark start, final Node parent ) {
            super ( null, null, text, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public abstract static class ScriptingElement extends Node {
        public ScriptingElement ( final String qName, final String localName, final String text, final Mark start, final Node parent ) {
            super ( qName, localName, text, start, parent );
        }
        public ScriptingElement ( final String qName, final String localName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, localName, null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        @Override
        public String getText() {
            String ret = this.text;
            if ( ret == null ) {
                if ( this.body != null ) {
                    final StringBuilder buf = new StringBuilder();
                    for ( int i = 0; i < this.body.size(); ++i ) {
                        buf.append ( this.body.getNode ( i ).getText() );
                    }
                    ret = buf.toString();
                } else {
                    ret = "";
                }
            }
            return ret;
        }
        @Override
        public Mark getStart() {
            if ( this.text == null && this.body != null && this.body.size() > 0 ) {
                return this.body.getNode ( 0 ).getStart();
            }
            return super.getStart();
        }
    }
    public static class Declaration extends ScriptingElement {
        public Declaration ( final String text, final Mark start, final Node parent ) {
            super ( "jsp:declaration", "declaration", text, start, parent );
        }
        public Declaration ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "declaration", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class Expression extends ScriptingElement {
        public Expression ( final String text, final Mark start, final Node parent ) {
            super ( "jsp:expression", "expression", text, start, parent );
        }
        public Expression ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "expression", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class Scriptlet extends ScriptingElement {
        public Scriptlet ( final String text, final Mark start, final Node parent ) {
            super ( "jsp:scriptlet", "scriptlet", text, start, parent );
        }
        public Scriptlet ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "scriptlet", nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class ELExpression extends Node {
        private ELNode.Nodes el;
        private final char type;
        public ELExpression ( final char type, final String text, final Mark start, final Node parent ) {
            super ( null, null, text, start, parent );
            this.type = type;
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setEL ( final ELNode.Nodes el ) {
            this.el = el;
        }
        public ELNode.Nodes getEL() {
            return this.el;
        }
        public char getType() {
            return this.type;
        }
    }
    public static class ParamAction extends Node {
        private JspAttribute value;
        public ParamAction ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:param", attrs, null, null, start, parent );
        }
        public ParamAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "param", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setValue ( final JspAttribute value ) {
            this.value = value;
        }
        public JspAttribute getValue() {
            return this.value;
        }
    }
    public static class ParamsAction extends Node {
        public ParamsAction ( final Mark start, final Node parent ) {
            this ( "jsp:params", ( Attributes ) null, null, start, parent );
        }
        public ParamsAction ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "params", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class FallBackAction extends Node {
        public FallBackAction ( final Mark start, final Node parent ) {
            this ( "jsp:fallback", ( Attributes ) null, null, start, parent );
        }
        public FallBackAction ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "fallback", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class IncludeAction extends Node {
        private JspAttribute page;
        public IncludeAction ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:include", attrs, null, null, start, parent );
        }
        public IncludeAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "include", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setPage ( final JspAttribute page ) {
            this.page = page;
        }
        public JspAttribute getPage() {
            return this.page;
        }
    }
    public static class ForwardAction extends Node {
        private JspAttribute page;
        public ForwardAction ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:forward", attrs, null, null, start, parent );
        }
        public ForwardAction ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "forward", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setPage ( final JspAttribute page ) {
            this.page = page;
        }
        public JspAttribute getPage() {
            return this.page;
        }
    }
    public static class GetProperty extends Node {
        public GetProperty ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:getProperty", attrs, null, null, start, parent );
        }
        public GetProperty ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "getProperty", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class SetProperty extends Node {
        private JspAttribute value;
        public SetProperty ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:setProperty", attrs, null, null, start, parent );
        }
        public SetProperty ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "setProperty", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setValue ( final JspAttribute value ) {
            this.value = value;
        }
        public JspAttribute getValue() {
            return this.value;
        }
    }
    public static class UseBean extends Node {
        private JspAttribute beanName;
        public UseBean ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:useBean", attrs, null, null, start, parent );
        }
        public UseBean ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "useBean", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setBeanName ( final JspAttribute beanName ) {
            this.beanName = beanName;
        }
        public JspAttribute getBeanName() {
            return this.beanName;
        }
    }
    public static class PlugIn extends Node {
        private JspAttribute width;
        private JspAttribute height;
        public PlugIn ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:plugin", attrs, null, null, start, parent );
        }
        public PlugIn ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "plugin", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setHeight ( final JspAttribute height ) {
            this.height = height;
        }
        public void setWidth ( final JspAttribute width ) {
            this.width = width;
        }
        public JspAttribute getHeight() {
            return this.height;
        }
        public JspAttribute getWidth() {
            return this.width;
        }
    }
    public static class UninterpretedTag extends Node {
        private JspAttribute[] jspAttrs;
        public UninterpretedTag ( final String qName, final String localName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setJspAttributes ( final JspAttribute[] jspAttrs ) {
            this.jspAttrs = jspAttrs;
        }
        public JspAttribute[] getJspAttributes() {
            return this.jspAttrs;
        }
    }
    public static class JspElement extends Node {
        private JspAttribute[] jspAttrs;
        private JspAttribute nameAttr;
        public JspElement ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:element", attrs, null, null, start, parent );
        }
        public JspElement ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "element", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void setJspAttributes ( final JspAttribute[] jspAttrs ) {
            this.jspAttrs = jspAttrs;
        }
        public JspAttribute[] getJspAttributes() {
            return this.jspAttrs;
        }
        public void setNameAttribute ( final JspAttribute nameAttr ) {
            this.nameAttr = nameAttr;
        }
        public JspAttribute getNameAttribute() {
            return this.nameAttr;
        }
    }
    public static class JspOutput extends Node {
        public JspOutput ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "output", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class ChildInfo {
        private boolean scriptless;
        private boolean hasUseBean;
        private boolean hasIncludeAction;
        private boolean hasParamAction;
        private boolean hasSetProperty;
        private boolean hasScriptingVars;
        public void setScriptless ( final boolean s ) {
            this.scriptless = s;
        }
        public boolean isScriptless() {
            return this.scriptless;
        }
        public void setHasUseBean ( final boolean u ) {
            this.hasUseBean = u;
        }
        public boolean hasUseBean() {
            return this.hasUseBean;
        }
        public void setHasIncludeAction ( final boolean i ) {
            this.hasIncludeAction = i;
        }
        public boolean hasIncludeAction() {
            return this.hasIncludeAction;
        }
        public void setHasParamAction ( final boolean i ) {
            this.hasParamAction = i;
        }
        public boolean hasParamAction() {
            return this.hasParamAction;
        }
        public void setHasSetProperty ( final boolean s ) {
            this.hasSetProperty = s;
        }
        public boolean hasSetProperty() {
            return this.hasSetProperty;
        }
        public void setHasScriptingVars ( final boolean s ) {
            this.hasScriptingVars = s;
        }
        public boolean hasScriptingVars() {
            return this.hasScriptingVars;
        }
    }
    public static class CustomTag extends Node {
        private final String uri;
        private final String prefix;
        private JspAttribute[] jspAttrs;
        private TagData tagData;
        private String tagHandlerPoolName;
        private final TagInfo tagInfo;
        private final TagFileInfo tagFileInfo;
        private Class<?> tagHandlerClass;
        private VariableInfo[] varInfos;
        private final int customNestingLevel;
        private final ChildInfo childInfo;
        private final boolean implementsIterationTag;
        private final boolean implementsBodyTag;
        private final boolean implementsTryCatchFinally;
        private final boolean implementsJspIdConsumer;
        private final boolean implementsSimpleTag;
        private final boolean implementsDynamicAttributes;
        private List<Object> atBeginScriptingVars;
        private List<Object> atEndScriptingVars;
        private List<Object> nestedScriptingVars;
        private CustomTag customTagParent;
        private Integer numCount;
        private boolean useTagPlugin;
        private TagPluginContext tagPluginContext;
        private Nodes atSTag;
        private Nodes atETag;
        public CustomTag ( final String qName, final String prefix, final String localName, final String uri, final Attributes attrs, final Mark start, final Node parent, final TagInfo tagInfo, final Class<?> tagHandlerClass ) {
            this ( qName, prefix, localName, uri, attrs, null, null, start, parent, tagInfo, tagHandlerClass );
        }
        public CustomTag ( final String qName, final String prefix, final String localName, final String uri, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent, final TagInfo tagInfo, final Class<?> tagHandlerClass ) {
            super ( qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.uri = uri;
            this.prefix = prefix;
            this.tagInfo = tagInfo;
            this.tagFileInfo = null;
            this.tagHandlerClass = tagHandlerClass;
            this.customNestingLevel = this.makeCustomNestingLevel();
            this.childInfo = new ChildInfo();
            this.implementsIterationTag = IterationTag.class.isAssignableFrom ( tagHandlerClass );
            this.implementsBodyTag = BodyTag.class.isAssignableFrom ( tagHandlerClass );
            this.implementsTryCatchFinally = TryCatchFinally.class.isAssignableFrom ( tagHandlerClass );
            this.implementsSimpleTag = SimpleTag.class.isAssignableFrom ( tagHandlerClass );
            this.implementsDynamicAttributes = DynamicAttributes.class.isAssignableFrom ( tagHandlerClass );
            this.implementsJspIdConsumer = JspIdConsumer.class.isAssignableFrom ( tagHandlerClass );
        }
        public CustomTag ( final String qName, final String prefix, final String localName, final String uri, final Attributes attrs, final Mark start, final Node parent, final TagFileInfo tagFileInfo ) {
            this ( qName, prefix, localName, uri, attrs, null, null, start, parent, tagFileInfo );
        }
        public CustomTag ( final String qName, final String prefix, final String localName, final String uri, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent, final TagFileInfo tagFileInfo ) {
            super ( qName, localName, attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.uri = uri;
            this.prefix = prefix;
            this.tagFileInfo = tagFileInfo;
            this.tagInfo = tagFileInfo.getTagInfo();
            this.customNestingLevel = this.makeCustomNestingLevel();
            this.childInfo = new ChildInfo();
            this.implementsIterationTag = false;
            this.implementsBodyTag = false;
            this.implementsTryCatchFinally = false;
            this.implementsSimpleTag = true;
            this.implementsJspIdConsumer = false;
            this.implementsDynamicAttributes = this.tagInfo.hasDynamicAttributes();
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getURI() {
            return this.uri;
        }
        public String getPrefix() {
            return this.prefix;
        }
        public void setJspAttributes ( final JspAttribute[] jspAttrs ) {
            this.jspAttrs = jspAttrs;
        }
        public JspAttribute[] getJspAttributes() {
            return this.jspAttrs;
        }
        public ChildInfo getChildInfo() {
            return this.childInfo;
        }
        public void setTagData ( final TagData tagData ) {
            this.tagData = tagData;
            this.varInfos = this.tagInfo.getVariableInfo ( tagData );
            if ( this.varInfos == null ) {
                this.varInfos = Node.ZERO_VARIABLE_INFO;
            }
        }
        public TagData getTagData() {
            return this.tagData;
        }
        public void setTagHandlerPoolName ( final String s ) {
            this.tagHandlerPoolName = s;
        }
        public String getTagHandlerPoolName() {
            return this.tagHandlerPoolName;
        }
        public TagInfo getTagInfo() {
            return this.tagInfo;
        }
        public TagFileInfo getTagFileInfo() {
            return this.tagFileInfo;
        }
        public boolean isTagFile() {
            return this.tagFileInfo != null;
        }
        public Class<?> getTagHandlerClass() {
            return this.tagHandlerClass;
        }
        public void setTagHandlerClass ( final Class<?> hc ) {
            this.tagHandlerClass = hc;
        }
        public boolean implementsIterationTag() {
            return this.implementsIterationTag;
        }
        public boolean implementsBodyTag() {
            return this.implementsBodyTag;
        }
        public boolean implementsTryCatchFinally() {
            return this.implementsTryCatchFinally;
        }
        public boolean implementsJspIdConsumer() {
            return this.implementsJspIdConsumer;
        }
        public boolean implementsSimpleTag() {
            return this.implementsSimpleTag;
        }
        public boolean implementsDynamicAttributes() {
            return this.implementsDynamicAttributes;
        }
        public TagVariableInfo[] getTagVariableInfos() {
            return this.tagInfo.getTagVariableInfos();
        }
        public VariableInfo[] getVariableInfos() {
            return this.varInfos;
        }
        public void setCustomTagParent ( final CustomTag n ) {
            this.customTagParent = n;
        }
        public CustomTag getCustomTagParent() {
            return this.customTagParent;
        }
        public void setNumCount ( final Integer count ) {
            this.numCount = count;
        }
        public Integer getNumCount() {
            return this.numCount;
        }
        public void setScriptingVars ( final List<Object> vec, final int scope ) {
            switch ( scope ) {
            case 1: {
                this.atBeginScriptingVars = vec;
                break;
            }
            case 2: {
                this.atEndScriptingVars = vec;
                break;
            }
            case 0: {
                this.nestedScriptingVars = vec;
                break;
            }
            }
        }
        public List<Object> getScriptingVars ( final int scope ) {
            List<Object> vec = null;
            switch ( scope ) {
            case 1: {
                vec = this.atBeginScriptingVars;
                break;
            }
            case 2: {
                vec = this.atEndScriptingVars;
                break;
            }
            case 0: {
                vec = this.nestedScriptingVars;
                break;
            }
            }
            return vec;
        }
        public int getCustomNestingLevel() {
            return this.customNestingLevel;
        }
        public boolean checkIfAttributeIsJspFragment ( final String name ) {
            boolean result = false;
            final TagAttributeInfo[] attributes = this.tagInfo.getAttributes();
            for ( int i = 0; i < attributes.length; ++i ) {
                if ( attributes[i].getName().equals ( name ) && attributes[i].isFragment() ) {
                    result = true;
                    break;
                }
            }
            return result;
        }
        public void setUseTagPlugin ( final boolean use ) {
            this.useTagPlugin = use;
        }
        public boolean useTagPlugin() {
            return this.useTagPlugin;
        }
        public void setTagPluginContext ( final TagPluginContext tagPluginContext ) {
            this.tagPluginContext = tagPluginContext;
        }
        public TagPluginContext getTagPluginContext() {
            return this.tagPluginContext;
        }
        public void setAtSTag ( final Nodes sTag ) {
            this.atSTag = sTag;
        }
        public Nodes getAtSTag() {
            return this.atSTag;
        }
        public void setAtETag ( final Nodes eTag ) {
            this.atETag = eTag;
        }
        public Nodes getAtETag() {
            return this.atETag;
        }
        private int makeCustomNestingLevel() {
            int n = 0;
            for ( Node p = this.parent; p != null; p = p.parent ) {
                if ( p instanceof CustomTag && this.qName.equals ( ( ( CustomTag ) p ).qName ) ) {
                    ++n;
                }
            }
            return n;
        }
        public boolean hasEmptyBody() {
            boolean hasEmptyBody = true;
            final Nodes nodes = this.getBody();
            if ( nodes != null ) {
                for ( int numChildNodes = nodes.size(), i = 0; i < numChildNodes; ++i ) {
                    final Node n = nodes.getNode ( i );
                    if ( ! ( n instanceof NamedAttribute ) ) {
                        hasEmptyBody = ( n instanceof JspBody && n.getBody() == null );
                        break;
                    }
                }
            }
            return hasEmptyBody;
        }
    }
    public static class AttributeGenerator extends Node {
        private String name;
        private CustomTag tag;
        public AttributeGenerator ( final Mark start, final String name, final CustomTag tag ) {
            super ( start, null );
            this.name = name;
            this.tag = tag;
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getName() {
            return this.name;
        }
        public CustomTag getTag() {
            return this.tag;
        }
    }
    public static class JspText extends Node {
        public JspText ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "text", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
    }
    public static class NamedAttribute extends Node {
        private String temporaryVariableName;
        private boolean trim;
        private JspAttribute omit;
        private final ChildInfo childInfo;
        private final String name;
        private String localName;
        private String prefix;
        public NamedAttribute ( final Attributes attrs, final Mark start, final Node parent ) {
            this ( "jsp:attribute", attrs, null, null, start, parent );
        }
        public NamedAttribute ( final String qName, final Attributes attrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "attribute", attrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.trim = true;
            if ( "false".equals ( this.getAttributeValue ( "trim" ) ) ) {
                this.trim = false;
            }
            this.childInfo = new ChildInfo();
            this.name = this.getAttributeValue ( "name" );
            if ( this.name != null ) {
                this.localName = this.name;
                final int index = this.name.indexOf ( 58 );
                if ( index != -1 ) {
                    this.prefix = this.name.substring ( 0, index );
                    this.localName = this.name.substring ( index + 1 );
                }
            }
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public String getName() {
            return this.name;
        }
        @Override
        public String getLocalName() {
            return this.localName;
        }
        public String getPrefix() {
            return this.prefix;
        }
        public ChildInfo getChildInfo() {
            return this.childInfo;
        }
        public boolean isTrim() {
            return this.trim;
        }
        public void setOmit ( final JspAttribute omit ) {
            this.omit = omit;
        }
        public JspAttribute getOmit() {
            return this.omit;
        }
        public String getTemporaryVariableName() {
            if ( this.temporaryVariableName == null ) {
                this.temporaryVariableName = this.getRoot().nextTemporaryVariableName();
            }
            return this.temporaryVariableName;
        }
        @Override
        public String getText() {
            String text = "";
            if ( this.getBody() != null ) {
                class AttributeVisitor extends Visitor {
                    private String attrValue;
                    AttributeVisitor() {
                        this.attrValue = null;
                    }
                    @Override
                    public void visit ( final TemplateText txt ) {
                        this.attrValue = txt.getText();
                    }
                    public String getAttrValue() {
                        return this.attrValue;
                    }
                }
                final AttributeVisitor attributeVisitor = new AttributeVisitor();
                try {
                    this.getBody().visit ( attributeVisitor );
                } catch ( JasperException ex ) {}
                text = attributeVisitor.getAttrValue();
            }
            return text;
        }
    }
    public static class JspBody extends Node {
        private final ChildInfo childInfo;
        public JspBody ( final Mark start, final Node parent ) {
            this ( "jsp:body", ( Attributes ) null, null, start, parent );
        }
        public JspBody ( final String qName, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) {
            super ( qName, "body", null, nonTaglibXmlnsAttrs, taglibAttrs, start, parent );
            this.childInfo = new ChildInfo();
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public ChildInfo getChildInfo() {
            return this.childInfo;
        }
    }
    public static class TemplateText extends Node {
        private ArrayList<Integer> extraSmap;
        public TemplateText ( final String text, final Mark start, final Node parent ) {
            super ( null, null, text, start, parent );
            this.extraSmap = null;
        }
        public void accept ( final Visitor v ) throws JasperException {
            v.visit ( this );
        }
        public void ltrim() {
            int index;
            for ( index = 0; index < this.text.length() && this.text.charAt ( index ) <= ' '; ++index ) {}
            this.text = this.text.substring ( index );
        }
        public void setText ( final String text ) {
            this.text = text;
        }
        public void rtrim() {
            int index;
            for ( index = this.text.length(); index > 0 && this.text.charAt ( index - 1 ) <= ' '; --index ) {}
            this.text = this.text.substring ( 0, index );
        }
        public boolean isAllSpace() {
            boolean isAllSpace = true;
            for ( int i = 0; i < this.text.length(); ++i ) {
                if ( !Character.isWhitespace ( this.text.charAt ( i ) ) ) {
                    isAllSpace = false;
                    break;
                }
            }
            return isAllSpace;
        }
        public void addSmap ( final int srcLine ) {
            if ( this.extraSmap == null ) {
                this.extraSmap = new ArrayList<Integer>();
            }
            this.extraSmap.add ( srcLine );
        }
        public ArrayList<Integer> getExtraSmap() {
            return this.extraSmap;
        }
    }
    public static class JspAttribute {
        private final String qName;
        private final String uri;
        private final String localName;
        private final String value;
        private final boolean expression;
        private final boolean dynamic;
        private final ELNode.Nodes el;
        private final TagAttributeInfo tai;
        private final boolean namedAttribute;
        private final NamedAttribute namedAttributeNode;
        JspAttribute ( final TagAttributeInfo tai, final String qName, final String uri, final String localName, final String value, final boolean expr, final ELNode.Nodes el, final boolean dyn ) {
            this.qName = qName;
            this.uri = uri;
            this.localName = localName;
            this.value = value;
            this.namedAttributeNode = null;
            this.expression = expr;
            this.el = el;
            this.dynamic = dyn;
            this.namedAttribute = false;
            this.tai = tai;
        }
        public void validateEL ( final ExpressionFactory ef, final ELContext ctx ) throws ELException {
            if ( this.el != null ) {
                ef.createValueExpression ( ctx, this.value, ( Class ) String.class );
            }
        }
        JspAttribute ( final NamedAttribute na, final TagAttributeInfo tai, final boolean dyn ) {
            this.qName = na.getName();
            this.localName = na.getLocalName();
            this.value = null;
            this.namedAttributeNode = na;
            this.expression = false;
            this.el = null;
            this.dynamic = dyn;
            this.namedAttribute = true;
            this.tai = tai;
            this.uri = null;
        }
        public String getName() {
            return this.qName;
        }
        public String getLocalName() {
            return this.localName;
        }
        public String getURI() {
            return this.uri;
        }
        public TagAttributeInfo getTagAttributeInfo() {
            return this.tai;
        }
        public boolean isDeferredInput() {
            return this.tai != null && this.tai.isDeferredValue();
        }
        public boolean isDeferredMethodInput() {
            return this.tai != null && this.tai.isDeferredMethod();
        }
        public String getExpectedTypeName() {
            if ( this.tai != null ) {
                if ( this.isDeferredInput() ) {
                    return this.tai.getExpectedTypeName();
                }
                if ( this.isDeferredMethodInput() ) {
                    final String m = this.tai.getMethodSignature();
                    if ( m != null ) {
                        final int rti = m.trim().indexOf ( 32 );
                        if ( rti > 0 ) {
                            return m.substring ( 0, rti ).trim();
                        }
                    }
                }
            }
            return "java.lang.Object";
        }
        public String[] getParameterTypeNames() {
            if ( this.tai != null && this.isDeferredMethodInput() ) {
                String m = this.tai.getMethodSignature();
                if ( m != null ) {
                    m = m.trim();
                    m = m.substring ( m.indexOf ( 40 ) + 1 );
                    m = m.substring ( 0, m.length() - 1 );
                    if ( m.trim().length() > 0 ) {
                        final String[] p = m.split ( "," );
                        for ( int i = 0; i < p.length; ++i ) {
                            p[i] = p[i].trim();
                        }
                        return p;
                    }
                }
            }
            return new String[0];
        }
        public String getValue() {
            return this.value;
        }
        public NamedAttribute getNamedAttributeNode() {
            return this.namedAttributeNode;
        }
        public boolean isExpression() {
            return this.expression;
        }
        public boolean isNamedAttribute() {
            return this.namedAttribute;
        }
        public boolean isELInterpreterInput() {
            return this.el != null || this.isDeferredInput() || this.isDeferredMethodInput();
        }
        public boolean isLiteral() {
            return !this.expression && this.el == null && !this.namedAttribute;
        }
        public boolean isDynamic() {
            return this.dynamic;
        }
        public ELNode.Nodes getEL() {
            return this.el;
        }
    }
    public static class Nodes {
        private final List<Node> list;
        private Root root;
        private boolean generatedInBuffer;
        public Nodes() {
            this.list = new Vector<Node>();
        }
        public Nodes ( final Root root ) {
            this.root = root;
            ( this.list = new Vector<Node>() ).add ( root );
        }
        public void add ( final Node n ) {
            this.list.add ( n );
            this.root = null;
        }
        public void remove ( final Node n ) {
            this.list.remove ( n );
        }
        public void visit ( final Visitor v ) throws JasperException {
            for ( final Node n : this.list ) {
                n.accept ( v );
            }
        }
        public int size() {
            return this.list.size();
        }
        public Node getNode ( final int index ) {
            Node n = null;
            try {
                n = this.list.get ( index );
            } catch ( ArrayIndexOutOfBoundsException ex ) {}
            return n;
        }
        public Root getRoot() {
            return this.root;
        }
        public boolean isGeneratedInBuffer() {
            return this.generatedInBuffer;
        }
        public void setGeneratedInBuffer ( final boolean g ) {
            this.generatedInBuffer = g;
        }
    }
    public static class Visitor {
        protected void doVisit ( final Node n ) throws JasperException {
        }
        protected void visitBody ( final Node n ) throws JasperException {
            if ( n.getBody() != null ) {
                n.getBody().visit ( this );
            }
        }
        public void visit ( final Root n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final JspRoot n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final PageDirective n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final TagDirective n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final IncludeDirective n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final TaglibDirective n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final AttributeDirective n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final VariableDirective n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final Comment n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final Declaration n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final Expression n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final Scriptlet n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final ELExpression n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final IncludeAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final ForwardAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final GetProperty n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final SetProperty n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final ParamAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final ParamsAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final FallBackAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final UseBean n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final PlugIn n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final CustomTag n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final UninterpretedTag n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final JspElement n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final JspText n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final NamedAttribute n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final JspBody n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final InvokeAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final DoBodyAction n ) throws JasperException {
            this.doVisit ( n );
            this.visitBody ( n );
        }
        public void visit ( final TemplateText n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final JspOutput n ) throws JasperException {
            this.doVisit ( n );
        }
        public void visit ( final AttributeGenerator n ) throws JasperException {
            this.doVisit ( n );
        }
    }
}
