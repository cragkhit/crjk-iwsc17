package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
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
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URL;
import org.apache.tomcat.util.descriptor.tagplugin.TagPluginParser;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.tagplugin.TagPlugin;
import java.util.HashMap;
import javax.servlet.ServletContext;
public class TagPluginManager {
    private static final String META_INF_JASPER_TAG_PLUGINS_XML = "META-INF/org.apache.jasper/tagPlugins.xml";
    private static final String TAG_PLUGINS_XML = "/WEB-INF/tagPlugins.xml";
    private final ServletContext ctxt;
    private HashMap<String, TagPlugin> tagPlugins;
    private boolean initialized;
    public TagPluginManager ( final ServletContext ctxt ) {
        this.initialized = false;
        this.ctxt = ctxt;
    }
    public void apply ( final Node.Nodes page, final ErrorDispatcher err, final PageInfo pageInfo ) throws JasperException {
        this.init ( err );
        if ( !this.tagPlugins.isEmpty() ) {
            page.visit ( new NodeVisitor ( this, pageInfo ) );
        }
    }
    private void init ( final ErrorDispatcher err ) throws JasperException {
        if ( this.initialized ) {
            return;
        }
        final String blockExternalString = this.ctxt.getInitParameter ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
        final boolean blockExternal = blockExternalString == null || Boolean.parseBoolean ( blockExternalString );
        ClassLoader original;
        if ( Constants.IS_SECURITY_ENABLED ) {
            final PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) pa );
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        TagPluginParser parser = null;
        try {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa2 = new PrivilegedSetTccl ( TagPluginManager.class.getClassLoader() );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa2 );
            } else {
                Thread.currentThread().setContextClassLoader ( TagPluginManager.class.getClassLoader() );
            }
            parser = new TagPluginParser ( this.ctxt, blockExternal );
            final Enumeration<URL> urls = this.ctxt.getClassLoader().getResources ( "META-INF/org.apache.jasper/tagPlugins.xml" );
            while ( urls.hasMoreElements() ) {
                final URL url = urls.nextElement();
                parser.parse ( url );
            }
            final URL url = this.ctxt.getResource ( "/WEB-INF/tagPlugins.xml" );
            if ( url != null ) {
                parser.parse ( url );
            }
        } catch ( IOException ) {}
        catch ( SAXException e ) {
            throw new JasperException ( e );
        } finally {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa3 = new PrivilegedSetTccl ( original );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa3 );
            } else {
                Thread.currentThread().setContextClassLoader ( original );
            }
        }
        final Map<String, String> plugins = parser.getPlugins();
        this.tagPlugins = new HashMap<String, TagPlugin> ( plugins.size() );
        for ( final Map.Entry<String, String> entry : plugins.entrySet() ) {
            try {
                final String tagClass = entry.getKey();
                final String pluginName = entry.getValue();
                final Class<?> pluginClass = this.ctxt.getClassLoader().loadClass ( pluginName );
                final TagPlugin plugin = ( TagPlugin ) pluginClass.newInstance();
                this.tagPlugins.put ( tagClass, plugin );
            } catch ( Exception e2 ) {
                err.jspError ( e2 );
            }
        }
        this.initialized = true;
    }
    private void invokePlugin ( final Node.CustomTag n, final PageInfo pageInfo ) {
        final TagPlugin tagPlugin = this.tagPlugins.get ( n.getTagHandlerClass().getName() );
        if ( tagPlugin == null ) {
            return;
        }
        final TagPluginContext tagPluginContext = new TagPluginContextImpl ( n, pageInfo );
        n.setTagPluginContext ( tagPluginContext );
        tagPlugin.doTag ( tagPluginContext );
    }
    private static class NodeVisitor extends Node.Visitor {
        private final TagPluginManager manager;
        private final PageInfo pageInfo;
        public NodeVisitor ( final TagPluginManager manager, final PageInfo pageInfo ) {
            this.manager = manager;
            this.pageInfo = pageInfo;
        }
        @Override
        public void visit ( final Node.CustomTag n ) throws JasperException {
            this.manager.invokePlugin ( n, this.pageInfo );
            this.visitBody ( n );
        }
    }
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
}
