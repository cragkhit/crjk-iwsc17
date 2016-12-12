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
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import java.util.List;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
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
            this.varInfos = Node.access$000();
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
