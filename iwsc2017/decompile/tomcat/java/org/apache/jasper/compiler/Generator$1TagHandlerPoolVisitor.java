package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import java.util.List;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;
import java.util.Arrays;
import java.util.Collections;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import java.util.Vector;
class TagHandlerPoolVisitor extends Node.Visitor {
    private final Vector<String> names;
    TagHandlerPoolVisitor ( final Vector<String> v ) {
        this.names = v;
    }
    @Override
    public void visit ( final Node.CustomTag n ) throws JasperException {
        if ( !n.implementsSimpleTag() ) {
            final String name = this.createTagHandlerPoolName ( n.getPrefix(), n.getLocalName(), n.getAttributes(), n.getNamedAttributeNodes(), n.hasEmptyBody() );
            n.setTagHandlerPoolName ( name );
            if ( !this.names.contains ( name ) ) {
                this.names.add ( name );
            }
        }
        this.visitBody ( n );
    }
    private String createTagHandlerPoolName ( final String prefix, final String shortName, final Attributes attrs, final Node.Nodes namedAttrs, final boolean hasEmptyBody ) {
        final StringBuilder poolName = new StringBuilder ( 64 );
        poolName.append ( "_jspx_tagPool_" ).append ( prefix ).append ( '_' ).append ( shortName );
        if ( attrs != null ) {
            final String[] attrNames = new String[attrs.getLength() + namedAttrs.size()];
            for ( int i = 0; i < attrNames.length; ++i ) {
                attrNames[i] = attrs.getQName ( i );
            }
            for ( int i = 0; i < namedAttrs.size(); ++i ) {
                attrNames[attrs.getLength() + i] = ( ( Node.NamedAttribute ) namedAttrs.getNode ( i ) ).getQName();
            }
            Arrays.sort ( attrNames, Collections.reverseOrder() );
            if ( attrNames.length > 0 ) {
                poolName.append ( '&' );
            }
            for ( int i = 0; i < attrNames.length; ++i ) {
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
