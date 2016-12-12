package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import org.xml.sax.Attributes;
import java.util.List;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
import java.util.HashMap;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
private static class TagPluginContextImpl implements TagPluginContext {
    private final Node.CustomTag node;
    private final PageInfo pageInfo;
    private final HashMap<String, Object> pluginAttributes;
    private Node.Nodes curNodes;
    TagPluginContextImpl ( final Node.CustomTag n, final PageInfo pageInfo ) {
        this.node = n;
        this.pageInfo = pageInfo;
        n.setAtETag ( this.curNodes = new Node.Nodes() );
        n.setAtSTag ( this.curNodes = new Node.Nodes() );
        n.setUseTagPlugin ( true );
        this.pluginAttributes = new HashMap<String, Object>();
    }
    @Override
    public TagPluginContext getParentContext() {
        final Node parent = this.node.getParent();
        if ( ! ( parent instanceof Node.CustomTag ) ) {
            return null;
        }
        return ( ( Node.CustomTag ) parent ).getTagPluginContext();
    }
    @Override
    public void setPluginAttribute ( final String key, final Object value ) {
        this.pluginAttributes.put ( key, value );
    }
    @Override
    public Object getPluginAttribute ( final String key ) {
        return this.pluginAttributes.get ( key );
    }
    @Override
    public boolean isScriptless() {
        return this.node.getChildInfo().isScriptless();
    }
    @Override
    public boolean isConstantAttribute ( final String attribute ) {
        final Node.JspAttribute attr = this.getNodeAttribute ( attribute );
        return attr != null && attr.isLiteral();
    }
    @Override
    public String getConstantAttribute ( final String attribute ) {
        final Node.JspAttribute attr = this.getNodeAttribute ( attribute );
        if ( attr == null ) {
            return null;
        }
        return attr.getValue();
    }
    @Override
    public boolean isAttributeSpecified ( final String attribute ) {
        return this.getNodeAttribute ( attribute ) != null;
    }
    @Override
    public String getTemporaryVariableName() {
        return this.node.getRoot().nextTemporaryVariableName();
    }
    @Override
    public void generateImport ( final String imp ) {
        this.pageInfo.addImport ( imp );
    }
    @Override
    public void generateDeclaration ( final String id, final String text ) {
        if ( this.pageInfo.isPluginDeclared ( id ) ) {
            return;
        }
        this.curNodes.add ( new Node.Declaration ( text, this.node.getStart(), null ) );
    }
    @Override
    public void generateJavaSource ( final String sourceCode ) {
        this.curNodes.add ( new Node.Scriptlet ( sourceCode, this.node.getStart(), null ) );
    }
    @Override
    public void generateAttribute ( final String attributeName ) {
        this.curNodes.add ( new Node.AttributeGenerator ( this.node.getStart(), attributeName, this.node ) );
    }
    @Override
    public void dontUseTagPlugin() {
        this.node.setUseTagPlugin ( false );
    }
    @Override
    public void generateBody() {
        this.curNodes = this.node.getAtETag();
    }
    @Override
    public boolean isTagFile() {
        return this.pageInfo.isTagFile();
    }
    private Node.JspAttribute getNodeAttribute ( final String attribute ) {
        final Node.JspAttribute[] attrs = this.node.getJspAttributes();
        for ( int i = 0; attrs != null && i < attrs.length; ++i ) {
            if ( attrs[i].getName().equals ( attribute ) ) {
                return attrs[i];
            }
        }
        return null;
    }
}
