package org.apache.jasper.compiler;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ListIterator;
import javax.servlet.jsp.tagext.PageData;
import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
class PageDataImpl extends PageData implements TagConstants {
    private static final String JSP_VERSION = "2.0";
    private static final String CDATA_START_SECTION = "<![CDATA[\n";
    private static final String CDATA_END_SECTION = "]]>\n";
    private final StringBuilder buf;
    public PageDataImpl ( Node.Nodes page, Compiler compiler )
    throws JasperException {
        FirstPassVisitor firstPass = new FirstPassVisitor ( page.getRoot(),
                compiler.getPageInfo() );
        page.visit ( firstPass );
        buf = new StringBuilder();
        SecondPassVisitor secondPass
            = new SecondPassVisitor ( page.getRoot(), buf, compiler,
                                      firstPass.getJspIdPrefix() );
        page.visit ( secondPass );
    }
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream (
                   buf.toString().getBytes ( StandardCharsets.UTF_8 ) );
    }
    private static class FirstPassVisitor
        extends Node.Visitor implements TagConstants {
        private final Node.Root root;
        private final AttributesImpl rootAttrs;
        private final PageInfo pageInfo;
        private String jspIdPrefix;
        public FirstPassVisitor ( Node.Root root, PageInfo pageInfo ) {
            this.root = root;
            this.pageInfo = pageInfo;
            this.rootAttrs = new AttributesImpl();
            this.rootAttrs.addAttribute ( "", "", "version", "CDATA",
                                          JSP_VERSION );
            this.jspIdPrefix = "jsp";
        }
        @Override
        public void visit ( Node.Root n ) throws JasperException {
            visitBody ( n );
            if ( n == root ) {
                if ( !JSP_URI.equals ( rootAttrs.getValue ( "xmlns:jsp" ) ) ) {
                    rootAttrs.addAttribute ( "", "", "xmlns:jsp", "CDATA",
                                             JSP_URI );
                }
                if ( pageInfo.isJspPrefixHijacked() ) {
                    jspIdPrefix += "jsp";
                    while ( pageInfo.containsPrefix ( jspIdPrefix ) ) {
                        jspIdPrefix += "jsp";
                    }
                    rootAttrs.addAttribute ( "", "", "xmlns:" + jspIdPrefix,
                                             "CDATA", JSP_URI );
                }
                root.setAttributes ( rootAttrs );
            }
        }
        @Override
        public void visit ( Node.JspRoot n ) throws JasperException {
            addAttributes ( n.getTaglibAttributes() );
            addAttributes ( n.getNonTaglibXmlnsAttributes() );
            addAttributes ( n.getAttributes() );
            visitBody ( n );
        }
        @Override
        public void visit ( Node.TaglibDirective n ) throws JasperException {
            Attributes attrs = n.getAttributes();
            if ( attrs != null ) {
                String qName = "xmlns:" + attrs.getValue ( "prefix" );
                if ( rootAttrs.getIndex ( qName ) == -1 ) {
                    String location = attrs.getValue ( "uri" );
                    if ( location != null ) {
                        if ( location.startsWith ( "/" ) ) {
                            location = URN_JSPTLD + location;
                        }
                        rootAttrs.addAttribute ( "", "", qName, "CDATA",
                                                 location );
                    } else {
                        location = attrs.getValue ( "tagdir" );
                        rootAttrs.addAttribute ( "", "", qName, "CDATA",
                                                 URN_JSPTAGDIR + location );
                    }
                }
            }
        }
        public String getJspIdPrefix() {
            return jspIdPrefix;
        }
        private void addAttributes ( Attributes attrs ) {
            if ( attrs != null ) {
                int len = attrs.getLength();
                for ( int i = 0; i < len; i++ ) {
                    String qName = attrs.getQName ( i );
                    if ( "version".equals ( qName ) ) {
                        continue;
                    }
                    if ( rootAttrs.getIndex ( qName ) == -1 ) {
                        rootAttrs.addAttribute ( attrs.getURI ( i ),
                                                 attrs.getLocalName ( i ),
                                                 qName,
                                                 attrs.getType ( i ),
                                                 attrs.getValue ( i ) );
                    }
                }
            }
        }
    }
    private static class SecondPassVisitor extends Node.Visitor
        implements TagConstants {
        private final Node.Root root;
        private final StringBuilder buf;
        private final Compiler compiler;
        private final String jspIdPrefix;
        private boolean resetDefaultNS = false;
        private int jspId;
        public SecondPassVisitor ( Node.Root root, StringBuilder buf,
                                   Compiler compiler, String jspIdPrefix ) {
            this.root = root;
            this.buf = buf;
            this.compiler = compiler;
            this.jspIdPrefix = jspIdPrefix;
        }
        @Override
        public void visit ( Node.Root n ) throws JasperException {
            if ( n == this.root ) {
                appendXmlProlog();
                appendTag ( n );
            } else {
                boolean resetDefaultNSSave = resetDefaultNS;
                if ( n.isXmlSyntax() ) {
                    resetDefaultNS = true;
                }
                visitBody ( n );
                resetDefaultNS = resetDefaultNSSave;
            }
        }
        @Override
        public void visit ( Node.JspRoot n ) throws JasperException {
            visitBody ( n );
        }
        @Override
        public void visit ( Node.PageDirective n ) throws JasperException {
            appendPageDirective ( n );
        }
        @Override
        public void visit ( Node.IncludeDirective n ) throws JasperException {
            visitBody ( n );
        }
        @Override
        public void visit ( Node.Comment n ) throws JasperException {
        }
        @Override
        public void visit ( Node.Declaration n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.Expression n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.Scriptlet n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.JspElement n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.ELExpression n ) throws JasperException {
            if ( !n.getRoot().isXmlSyntax() ) {
                buf.append ( "<" ).append ( JSP_TEXT_ACTION );
                buf.append ( " " );
                buf.append ( jspIdPrefix );
                buf.append ( ":id=\"" );
                buf.append ( jspId++ ).append ( "\">" );
            }
            buf.append ( "${" );
            buf.append ( JspUtil.escapeXml ( n.getText() ) );
            buf.append ( "}" );
            if ( !n.getRoot().isXmlSyntax() ) {
                buf.append ( JSP_TEXT_ACTION_END );
            }
            buf.append ( "\n" );
        }
        @Override
        public void visit ( Node.IncludeAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.ForwardAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.GetProperty n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.SetProperty n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.ParamAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.ParamsAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.FallBackAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.UseBean n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.PlugIn n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.NamedAttribute n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.JspBody n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.CustomTag n ) throws JasperException {
            boolean resetDefaultNSSave = resetDefaultNS;
            appendTag ( n, resetDefaultNS );
            resetDefaultNS = resetDefaultNSSave;
        }
        @Override
        public void visit ( Node.UninterpretedTag n ) throws JasperException {
            boolean resetDefaultNSSave = resetDefaultNS;
            appendTag ( n, resetDefaultNS );
            resetDefaultNS = resetDefaultNSSave;
        }
        @Override
        public void visit ( Node.JspText n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.DoBodyAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.InvokeAction n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.TagDirective n ) throws JasperException {
            appendTagDirective ( n );
        }
        @Override
        public void visit ( Node.AttributeDirective n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.VariableDirective n ) throws JasperException {
            appendTag ( n );
        }
        @Override
        public void visit ( Node.TemplateText n ) throws JasperException {
            appendText ( n.getText(), !n.getRoot().isXmlSyntax() );
        }
        private void appendTag ( Node n ) throws JasperException {
            appendTag ( n, false );
        }
        private void appendTag ( Node n, boolean addDefaultNS )
        throws JasperException {
            Node.Nodes body = n.getBody();
            String text = n.getText();
            buf.append ( "<" ).append ( n.getQName() );
            buf.append ( "\n" );
            printAttributes ( n, addDefaultNS );
            buf.append ( "  " ).append ( jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            buf.append ( jspId++ ).append ( "\"\n" );
            if ( ROOT_ACTION.equals ( n.getLocalName() ) || body != null
                    || text != null ) {
                buf.append ( ">\n" );
                if ( ROOT_ACTION.equals ( n.getLocalName() ) ) {
                    if ( compiler.getCompilationContext().isTagFile() ) {
                        appendTagDirective();
                    } else {
                        appendPageDirective();
                    }
                }
                if ( body != null ) {
                    body.visit ( this );
                } else {
                    appendText ( text, false );
                }
                buf.append ( "</" + n.getQName() + ">\n" );
            } else {
                buf.append ( "/>\n" );
            }
        }
        private void appendPageDirective ( Node.PageDirective n ) {
            boolean append = false;
            Attributes attrs = n.getAttributes();
            int len = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String attrName = attrs.getQName ( i );
                if ( !"pageEncoding".equals ( attrName )
                        && !"contentType".equals ( attrName ) ) {
                    append = true;
                    break;
                }
            }
            if ( !append ) {
                return;
            }
            buf.append ( "<" ).append ( n.getQName() );
            buf.append ( "\n" );
            buf.append ( "  " ).append ( jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            buf.append ( jspId++ ).append ( "\"\n" );
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String attrName = attrs.getQName ( i );
                if ( "import".equals ( attrName ) || "contentType".equals ( attrName )
                        || "pageEncoding".equals ( attrName ) ) {
                    continue;
                }
                String value = attrs.getValue ( i );
                buf.append ( "  " ).append ( attrName ).append ( "=\"" );
                buf.append ( JspUtil.getExprInXml ( value ) ).append ( "\"\n" );
            }
            if ( n.getImports().size() > 0 ) {
                boolean first = true;
                ListIterator<String> iter = n.getImports().listIterator();
                while ( iter.hasNext() ) {
                    if ( first ) {
                        first = false;
                        buf.append ( "  import=\"" );
                    } else {
                        buf.append ( "," );
                    }
                    buf.append ( JspUtil.getExprInXml ( iter.next() ) );
                }
                buf.append ( "\"\n" );
            }
            buf.append ( "/>\n" );
        }
        private void appendPageDirective() {
            buf.append ( "<" ).append ( JSP_PAGE_DIRECTIVE_ACTION );
            buf.append ( "\n" );
            buf.append ( "  " ).append ( jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            buf.append ( jspId++ ).append ( "\"\n" );
            buf.append ( "  " ).append ( "pageEncoding" ).append ( "=\"UTF-8\"\n" );
            buf.append ( "  " ).append ( "contentType" ).append ( "=\"" );
            buf.append ( compiler.getPageInfo().getContentType() ).append ( "\"\n" );
            buf.append ( "/>\n" );
        }
        private void appendTagDirective ( Node.TagDirective n )
        throws JasperException {
            boolean append = false;
            Attributes attrs = n.getAttributes();
            int len = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String attrName = attrs.getQName ( i );
                if ( !"pageEncoding".equals ( attrName ) ) {
                    append = true;
                    break;
                }
            }
            if ( !append ) {
                return;
            }
            appendTag ( n );
        }
        private void appendTagDirective() {
            buf.append ( "<" ).append ( JSP_TAG_DIRECTIVE_ACTION );
            buf.append ( "\n" );
            buf.append ( "  " ).append ( jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            buf.append ( jspId++ ).append ( "\"\n" );
            buf.append ( "  " ).append ( "pageEncoding" ).append ( "=\"UTF-8\"\n" );
            buf.append ( "/>\n" );
        }
        private void appendText ( String text, boolean createJspTextElement ) {
            if ( createJspTextElement ) {
                buf.append ( "<" ).append ( JSP_TEXT_ACTION );
                buf.append ( "\n" );
                buf.append ( "  " ).append ( jspIdPrefix ).append ( ":id" ).append ( "=\"" );
                buf.append ( jspId++ ).append ( "\"\n" );
                buf.append ( ">\n" );
                appendCDATA ( text );
                buf.append ( JSP_TEXT_ACTION_END );
                buf.append ( "\n" );
            } else {
                appendCDATA ( text );
            }
        }
        private void appendCDATA ( String text ) {
            buf.append ( CDATA_START_SECTION );
            buf.append ( escapeCDATA ( text ) );
            buf.append ( CDATA_END_SECTION );
        }
        private String escapeCDATA ( String text ) {
            if ( text == null ) {
                return "";
            }
            int len = text.length();
            CharArrayWriter result = new CharArrayWriter ( len );
            for ( int i = 0; i < len; i++ ) {
                if ( ( ( i + 2 ) < len )
                        && ( text.charAt ( i ) == ']' )
                        && ( text.charAt ( i + 1 ) == ']' )
                        && ( text.charAt ( i + 2 ) == '>' ) ) {
                    result.write ( ']' );
                    result.write ( ']' );
                    result.write ( '&' );
                    result.write ( 'g' );
                    result.write ( 't' );
                    result.write ( ';' );
                    i += 2;
                } else {
                    result.write ( text.charAt ( i ) );
                }
            }
            return result.toString();
        }
        private void printAttributes ( Node n, boolean addDefaultNS ) {
            Attributes attrs = n.getTaglibAttributes();
            int len = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String name = attrs.getQName ( i );
                String value = attrs.getValue ( i );
                buf.append ( "  " ).append ( name ).append ( "=\"" ).append ( value ).append ( "\"\n" );
            }
            attrs = n.getNonTaglibXmlnsAttributes();
            len = ( attrs == null ) ? 0 : attrs.getLength();
            boolean defaultNSSeen = false;
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String name = attrs.getQName ( i );
                String value = attrs.getValue ( i );
                buf.append ( "  " ).append ( name ).append ( "=\"" ).append ( value ).append ( "\"\n" );
                defaultNSSeen |= "xmlns".equals ( name );
            }
            if ( addDefaultNS && !defaultNSSeen ) {
                buf.append ( "  xmlns=\"\"\n" );
            }
            resetDefaultNS = false;
            attrs = n.getAttributes();
            len = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < len; i++ ) {
                @SuppressWarnings ( "null" )
                String name = attrs.getQName ( i );
                String value = attrs.getValue ( i );
                buf.append ( "  " ).append ( name ).append ( "=\"" );
                buf.append ( JspUtil.getExprInXml ( value ) ).append ( "\"\n" );
            }
        }
        private void appendXmlProlog() {
            buf.append ( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" );
        }
    }
}
