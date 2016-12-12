package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import org.apache.jasper.JasperException;
import java.util.HashMap;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import java.util.Vector;
import javax.servlet.jsp.tagext.TagLibraryInfo;
private static class TagFileDirectiveVisitor extends Node.Visitor {
    private static final JspUtil.ValidAttribute[] tagDirectiveAttrs;
    private static final JspUtil.ValidAttribute[] attributeDirectiveAttrs;
    private static final JspUtil.ValidAttribute[] variableDirectiveAttrs;
    private ErrorDispatcher err;
    private TagLibraryInfo tagLibInfo;
    private String name;
    private String path;
    private String bodycontent;
    private String description;
    private String displayName;
    private String smallIcon;
    private String largeIcon;
    private String dynamicAttrsMapName;
    private String example;
    private Vector<TagAttributeInfo> attributeVector;
    private Vector<TagVariableInfo> variableVector;
    private static final String ATTR_NAME = "the name attribute of the attribute directive";
    private static final String VAR_NAME_GIVEN = "the name-given attribute of the variable directive";
    private static final String VAR_NAME_FROM = "the name-from-attribute attribute of the variable directive";
    private static final String VAR_ALIAS = "the alias attribute of the variable directive";
    private static final String TAG_DYNAMIC = "the dynamic-attributes attribute of the tag directive";
    private HashMap<String, NameEntry> nameTable;
    private HashMap<String, NameEntry> nameFromTable;
    public TagFileDirectiveVisitor ( final Compiler compiler, final TagLibraryInfo tagLibInfo, final String name, final String path ) {
        this.name = null;
        this.path = null;
        this.bodycontent = null;
        this.description = null;
        this.displayName = null;
        this.smallIcon = null;
        this.largeIcon = null;
        this.example = null;
        this.nameTable = new HashMap<String, NameEntry>();
        this.nameFromTable = new HashMap<String, NameEntry>();
        this.err = compiler.getErrorDispatcher();
        this.tagLibInfo = tagLibInfo;
        this.name = name;
        this.path = path;
        this.attributeVector = new Vector<TagAttributeInfo>();
        this.variableVector = new Vector<TagVariableInfo>();
    }
    @Override
    public void visit ( final Node.TagDirective n ) throws JasperException {
        JspUtil.checkAttributes ( "Tag directive", n, TagFileDirectiveVisitor.tagDirectiveAttrs, this.err );
        this.bodycontent = this.checkConflict ( n, this.bodycontent, "body-content" );
        if ( this.bodycontent != null && !this.bodycontent.equalsIgnoreCase ( "empty" ) && !this.bodycontent.equalsIgnoreCase ( "tagdependent" ) && !this.bodycontent.equalsIgnoreCase ( "scriptless" ) ) {
            this.err.jspError ( n, "jsp.error.tagdirective.badbodycontent", this.bodycontent );
        }
        this.dynamicAttrsMapName = this.checkConflict ( n, this.dynamicAttrsMapName, "dynamic-attributes" );
        if ( this.dynamicAttrsMapName != null ) {
            this.checkUniqueName ( this.dynamicAttrsMapName, "the dynamic-attributes attribute of the tag directive", n );
        }
        this.smallIcon = this.checkConflict ( n, this.smallIcon, "small-icon" );
        this.largeIcon = this.checkConflict ( n, this.largeIcon, "large-icon" );
        this.description = this.checkConflict ( n, this.description, "description" );
        this.displayName = this.checkConflict ( n, this.displayName, "display-name" );
        this.example = this.checkConflict ( n, this.example, "example" );
    }
    private String checkConflict ( final Node n, final String oldAttrValue, final String attr ) throws JasperException {
        String result = oldAttrValue;
        final String attrValue = n.getAttributeValue ( attr );
        if ( attrValue != null ) {
            if ( oldAttrValue != null && !oldAttrValue.equals ( attrValue ) ) {
                this.err.jspError ( n, "jsp.error.tag.conflict.attr", attr, oldAttrValue, attrValue );
            }
            result = attrValue;
        }
        return result;
    }
    @Override
    public void visit ( final Node.AttributeDirective n ) throws JasperException {
        JspUtil.checkAttributes ( "Attribute directive", n, TagFileDirectiveVisitor.attributeDirectiveAttrs, this.err );
        boolean deferredValue = false;
        boolean deferredValueSpecified = false;
        final String deferredValueString = n.getAttributeValue ( "deferredValue" );
        if ( deferredValueString != null ) {
            deferredValueSpecified = true;
            deferredValue = JspUtil.booleanValue ( deferredValueString );
        }
        String deferredValueType = n.getAttributeValue ( "deferredValueType" );
        if ( deferredValueType != null ) {
            if ( deferredValueSpecified && !deferredValue ) {
                this.err.jspError ( n, "jsp.error.deferredvaluetypewithoutdeferredvalue", new String[0] );
            } else {
                deferredValue = true;
            }
        } else if ( deferredValue ) {
            deferredValueType = "java.lang.Object";
        } else {
            deferredValueType = "java.lang.String";
        }
        boolean deferredMethod = false;
        boolean deferredMethodSpecified = false;
        final String deferredMethodString = n.getAttributeValue ( "deferredMethod" );
        if ( deferredMethodString != null ) {
            deferredMethodSpecified = true;
            deferredMethod = JspUtil.booleanValue ( deferredMethodString );
        }
        String deferredMethodSignature = n.getAttributeValue ( "deferredMethodSignature" );
        if ( deferredMethodSignature != null ) {
            if ( deferredMethodSpecified && !deferredMethod ) {
                this.err.jspError ( n, "jsp.error.deferredmethodsignaturewithoutdeferredmethod", new String[0] );
            } else {
                deferredMethod = true;
            }
        } else if ( deferredMethod ) {
            deferredMethodSignature = "void methodname()";
        }
        if ( deferredMethod && deferredValue ) {
            this.err.jspError ( n, "jsp.error.deferredmethodandvalue", new String[0] );
        }
        final String attrName = n.getAttributeValue ( "name" );
        final boolean required = JspUtil.booleanValue ( n.getAttributeValue ( "required" ) );
        boolean rtexprvalue = true;
        final String rtexprvalueString = n.getAttributeValue ( "rtexprvalue" );
        if ( rtexprvalueString != null ) {
            rtexprvalue = JspUtil.booleanValue ( rtexprvalueString );
        }
        final boolean fragment = JspUtil.booleanValue ( n.getAttributeValue ( "fragment" ) );
        String type = n.getAttributeValue ( "type" );
        if ( fragment ) {
            if ( type != null ) {
                this.err.jspError ( n, "jsp.error.fragmentwithtype", new String[0] );
            }
            rtexprvalue = true;
            if ( rtexprvalueString != null ) {
                this.err.jspError ( n, "jsp.error.frgmentwithrtexprvalue", new String[0] );
            }
        } else {
            if ( type == null ) {
                type = "java.lang.String";
            }
            if ( deferredValue ) {
                type = ValueExpression.class.getName();
            } else if ( deferredMethod ) {
                type = MethodExpression.class.getName();
            }
        }
        if ( ( "2.0".equals ( this.tagLibInfo.getRequiredVersion() ) || "1.2".equals ( this.tagLibInfo.getRequiredVersion() ) ) && ( deferredMethodSpecified || deferredMethod || deferredValueSpecified || deferredValue ) ) {
            this.err.jspError ( "jsp.error.invalid.version", this.path );
        }
        final TagAttributeInfo tagAttributeInfo = new TagAttributeInfo ( attrName, required, type, rtexprvalue, fragment, ( String ) null, deferredValue, deferredMethod, deferredValueType, deferredMethodSignature );
        this.attributeVector.addElement ( tagAttributeInfo );
        this.checkUniqueName ( attrName, "the name attribute of the attribute directive", n, tagAttributeInfo );
    }
    @Override
    public void visit ( final Node.VariableDirective n ) throws JasperException {
        JspUtil.checkAttributes ( "Variable directive", n, TagFileDirectiveVisitor.variableDirectiveAttrs, this.err );
        String nameGiven = n.getAttributeValue ( "name-given" );
        final String nameFromAttribute = n.getAttributeValue ( "name-from-attribute" );
        if ( nameGiven == null && nameFromAttribute == null ) {
            this.err.jspError ( "jsp.error.variable.either.name", new String[0] );
        }
        if ( nameGiven != null && nameFromAttribute != null ) {
            this.err.jspError ( "jsp.error.variable.both.name", new String[0] );
        }
        final String alias = n.getAttributeValue ( "alias" );
        if ( ( nameFromAttribute != null && alias == null ) || ( nameFromAttribute == null && alias != null ) ) {
            this.err.jspError ( "jsp.error.variable.alias", new String[0] );
        }
        String className = n.getAttributeValue ( "variable-class" );
        if ( className == null ) {
            className = "java.lang.String";
        }
        final String declareStr = n.getAttributeValue ( "declare" );
        boolean declare = true;
        if ( declareStr != null ) {
            declare = JspUtil.booleanValue ( declareStr );
        }
        int scope = 0;
        final String scopeStr = n.getAttributeValue ( "scope" );
        if ( scopeStr != null ) {
            if ( !"NESTED".equals ( scopeStr ) ) {
                if ( "AT_BEGIN".equals ( scopeStr ) ) {
                    scope = 1;
                } else if ( "AT_END".equals ( scopeStr ) ) {
                    scope = 2;
                }
            }
        }
        if ( nameFromAttribute != null ) {
            nameGiven = alias;
            this.checkUniqueName ( nameFromAttribute, "the name-from-attribute attribute of the variable directive", n );
            this.checkUniqueName ( alias, "the alias attribute of the variable directive", n );
        } else {
            this.checkUniqueName ( nameGiven, "the name-given attribute of the variable directive", n );
        }
        this.variableVector.addElement ( new TagVariableInfo ( nameGiven, nameFromAttribute, className, declare, scope ) );
    }
    public TagInfo getTagInfo() throws JasperException {
        while ( true ) {
            if ( this.name == null ) {
                if ( this.bodycontent == null ) {
                    this.bodycontent = "scriptless";
                }
                final String tagClassName = JspUtil.getTagHandlerClassName ( this.path, this.tagLibInfo.getReliableURN(), this.err );
                final TagVariableInfo[] tagVariableInfos = new TagVariableInfo[this.variableVector.size()];
                this.variableVector.copyInto ( tagVariableInfos );
                final TagAttributeInfo[] tagAttributeInfo = new TagAttributeInfo[this.attributeVector.size()];
                this.attributeVector.copyInto ( tagAttributeInfo );
                return new JasperTagInfo ( this.name, tagClassName, this.bodycontent, this.description, this.tagLibInfo, null, tagAttributeInfo, this.displayName, this.smallIcon, this.largeIcon, tagVariableInfos, this.dynamicAttrsMapName );
            }
            continue;
        }
    }
    private void checkUniqueName ( final String name, final String type, final Node n ) throws JasperException {
        this.checkUniqueName ( name, type, n, null );
    }
    private void checkUniqueName ( final String name, final String type, final Node n, final TagAttributeInfo attr ) throws JasperException {
        final HashMap<String, NameEntry> table = "the name-from-attribute attribute of the variable directive".equals ( type ) ? this.nameFromTable : this.nameTable;
        final NameEntry nameEntry = table.get ( name );
        if ( nameEntry != null ) {
            if ( !"the dynamic-attributes attribute of the tag directive".equals ( type ) || !"the dynamic-attributes attribute of the tag directive".equals ( nameEntry.getType() ) ) {
                final int line = nameEntry.getNode().getStart().getLineNumber();
                this.err.jspError ( n, "jsp.error.tagfile.nameNotUnique", type, nameEntry.getType(), Integer.toString ( line ) );
            }
        } else {
            table.put ( name, new NameEntry ( type, n, attr ) );
        }
    }
    void postCheck() throws JasperException {
        for ( final Map.Entry<String, NameEntry> entry : this.nameFromTable.entrySet() ) {
            final String key = entry.getKey();
            final NameEntry nameEntry = this.nameTable.get ( key );
            final NameEntry nameFromEntry = entry.getValue();
            final Node nameFromNode = nameFromEntry.getNode();
            if ( nameEntry == null ) {
                this.err.jspError ( nameFromNode, "jsp.error.tagfile.nameFrom.noAttribute", key );
            } else {
                final Node node = nameEntry.getNode();
                final TagAttributeInfo tagAttr = nameEntry.getTagAttributeInfo();
                if ( "java.lang.String".equals ( tagAttr.getTypeName() ) && tagAttr.isRequired() && !tagAttr.canBeRequestTime() ) {
                    continue;
                }
                this.err.jspError ( nameFromNode, "jsp.error.tagfile.nameFrom.badAttribute", key, Integer.toString ( node.getStart().getLineNumber() ) );
            }
        }
    }
    static {
        tagDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "display-name" ), new JspUtil.ValidAttribute ( "body-content" ), new JspUtil.ValidAttribute ( "dynamic-attributes" ), new JspUtil.ValidAttribute ( "small-icon" ), new JspUtil.ValidAttribute ( "large-icon" ), new JspUtil.ValidAttribute ( "description" ), new JspUtil.ValidAttribute ( "example" ), new JspUtil.ValidAttribute ( "pageEncoding" ), new JspUtil.ValidAttribute ( "language" ), new JspUtil.ValidAttribute ( "import" ), new JspUtil.ValidAttribute ( "deferredSyntaxAllowedAsLiteral" ), new JspUtil.ValidAttribute ( "trimDirectiveWhitespaces" ), new JspUtil.ValidAttribute ( "isELIgnored" ) };
        attributeDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name", true ), new JspUtil.ValidAttribute ( "required" ), new JspUtil.ValidAttribute ( "fragment" ), new JspUtil.ValidAttribute ( "rtexprvalue" ), new JspUtil.ValidAttribute ( "type" ), new JspUtil.ValidAttribute ( "deferredValue" ), new JspUtil.ValidAttribute ( "deferredValueType" ), new JspUtil.ValidAttribute ( "deferredMethod" ), new JspUtil.ValidAttribute ( "deferredMethodSignature" ), new JspUtil.ValidAttribute ( "description" ) };
        variableDirectiveAttrs = new JspUtil.ValidAttribute[] { new JspUtil.ValidAttribute ( "name-given" ), new JspUtil.ValidAttribute ( "name-from-attribute" ), new JspUtil.ValidAttribute ( "alias" ), new JspUtil.ValidAttribute ( "variable-class" ), new JspUtil.ValidAttribute ( "scope" ), new JspUtil.ValidAttribute ( "declare" ), new JspUtil.ValidAttribute ( "description" ) };
    }
    static class NameEntry {
        private String type;
        private Node node;
        private TagAttributeInfo attr;
        NameEntry ( final String type, final Node node, final TagAttributeInfo attr ) {
            this.type = type;
            this.node = node;
            this.attr = attr;
        }
        String getType() {
            return this.type;
        }
        Node getNode() {
            return this.node;
        }
        TagAttributeInfo getTagAttributeInfo() {
            return this.attr;
        }
    }
}
