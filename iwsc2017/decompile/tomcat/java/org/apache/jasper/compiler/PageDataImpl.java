package org.apache.jasper.compiler;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import org.apache.jasper.Constants;
import java.io.CharArrayWriter;
import java.util.ListIterator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import org.apache.jasper.JasperException;
import javax.servlet.jsp.tagext.PageData;
class PageDataImpl extends PageData implements TagConstants {
    private static final String JSP_VERSION = "2.0";
    private static final String CDATA_START_SECTION = "<![CDATA[\n";
    private static final String CDATA_END_SECTION = "]]>\n";
    private final StringBuilder buf;
    public PageDataImpl ( final Node.Nodes page, final Compiler compiler ) throws JasperException {
        final FirstPassVisitor firstPass = new FirstPassVisitor ( page.getRoot(), compiler.getPageInfo() );
        page.visit ( firstPass );
        this.buf = new StringBuilder();
        final SecondPassVisitor secondPass = new SecondPassVisitor ( page.getRoot(), this.buf, compiler, firstPass.getJspIdPrefix() );
        page.visit ( secondPass );
    }
    public InputStream getInputStream() {
        return new ByteArrayInputStream ( this.buf.toString().getBytes ( StandardCharsets.UTF_8 ) );
    }
    private static class FirstPassVisitor extends Node.Visitor implements TagConstants {
        private final Node.Root root;
        private final AttributesImpl rootAttrs;
        private final PageInfo pageInfo;
        private String jspIdPrefix;
        public FirstPassVisitor ( final Node.Root root, final PageInfo pageInfo ) {
            this.root = root;
            this.pageInfo = pageInfo;
            ( this.rootAttrs = new AttributesImpl() ).addAttribute ( "", "", "version", "CDATA", "2.0" );
            this.jspIdPrefix = "jsp";
        }
        @Override
        public void visit ( final Node.Root n ) throws JasperException {
            this.visitBody ( n );
            if ( n == this.root ) {
                if ( !"http://java.sun.com/JSP/Page".equals ( this.rootAttrs.getValue ( "xmlns:jsp" ) ) ) {
                    this.rootAttrs.addAttribute ( "", "", "xmlns:jsp", "CDATA", "http://java.sun.com/JSP/Page" );
                }
                if ( this.pageInfo.isJspPrefixHijacked() ) {
                    this.jspIdPrefix += "jsp";
                    while ( this.pageInfo.containsPrefix ( this.jspIdPrefix ) ) {
                        this.jspIdPrefix += "jsp";
                    }
                    this.rootAttrs.addAttribute ( "", "", "xmlns:" + this.jspIdPrefix, "CDATA", "http://java.sun.com/JSP/Page" );
                }
                this.root.setAttributes ( this.rootAttrs );
            }
        }
        @Override
        public void visit ( final Node.JspRoot n ) throws JasperException {
            this.addAttributes ( n.getTaglibAttributes() );
            this.addAttributes ( n.getNonTaglibXmlnsAttributes() );
            this.addAttributes ( n.getAttributes() );
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.TaglibDirective n ) throws JasperException {
            final Attributes attrs = n.getAttributes();
            if ( attrs != null ) {
                final String qName = "xmlns:" + attrs.getValue ( "prefix" );
                if ( this.rootAttrs.getIndex ( qName ) == -1 ) {
                    String location = attrs.getValue ( "uri" );
                    if ( location != null ) {
                        if ( location.startsWith ( "/" ) ) {
                            location = "urn:jsptld:" + location;
                        }
                        this.rootAttrs.addAttribute ( "", "", qName, "CDATA", location );
                    } else {
                        location = attrs.getValue ( "tagdir" );
                        this.rootAttrs.addAttribute ( "", "", qName, "CDATA", "urn:jsptagdir:" + location );
                    }
                }
            }
        }
        public String getJspIdPrefix() {
            return this.jspIdPrefix;
        }
        private void addAttributes ( final Attributes attrs ) {
            if ( attrs != null ) {
                for ( int len = attrs.getLength(), i = 0; i < len; ++i ) {
                    final String qName = attrs.getQName ( i );
                    if ( !"version".equals ( qName ) ) {
                        if ( this.rootAttrs.getIndex ( qName ) == -1 ) {
                            this.rootAttrs.addAttribute ( attrs.getURI ( i ), attrs.getLocalName ( i ), qName, attrs.getType ( i ), attrs.getValue ( i ) );
                        }
                    }
                }
            }
        }
    }
    private static class SecondPassVisitor extends Node.Visitor implements TagConstants {
        private final Node.Root root;
        private final StringBuilder buf;
        private final Compiler compiler;
        private final String jspIdPrefix;
        private boolean resetDefaultNS;
        private int jspId;
        public SecondPassVisitor ( final Node.Root root, final StringBuilder buf, final Compiler compiler, final String jspIdPrefix ) {
            this.resetDefaultNS = false;
            this.root = root;
            this.buf = buf;
            this.compiler = compiler;
            this.jspIdPrefix = jspIdPrefix;
        }
        @Override
        public void visit ( final Node.Root n ) throws JasperException {
            if ( n == this.root ) {
                this.appendXmlProlog();
                this.appendTag ( n );
            } else {
                final boolean resetDefaultNSSave = this.resetDefaultNS;
                if ( n.isXmlSyntax() ) {
                    this.resetDefaultNS = true;
                }
                this.visitBody ( n );
                this.resetDefaultNS = resetDefaultNSSave;
            }
        }
        @Override
        public void visit ( final Node.JspRoot n ) throws JasperException {
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.PageDirective n ) throws JasperException {
            this.appendPageDirective ( n );
        }
        @Override
        public void visit ( final Node.IncludeDirective n ) throws JasperException {
            this.visitBody ( n );
        }
        @Override
        public void visit ( final Node.Comment n ) throws JasperException {
        }
        @Override
        public void visit ( final Node.Declaration n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.Expression n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.Scriptlet n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.JspElement n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.ELExpression n ) throws JasperException {
            if ( !n.getRoot().isXmlSyntax() ) {
                this.buf.append ( "<" ).append ( "jsp:text" );
                this.buf.append ( " " );
                this.buf.append ( this.jspIdPrefix );
                this.buf.append ( ":id=\"" );
                this.buf.append ( this.jspId++ ).append ( "\">" );
            }
            this.buf.append ( "${" );
            this.buf.append ( JspUtil.escapeXml ( n.getText() ) );
            this.buf.append ( "}" );
            if ( !n.getRoot().isXmlSyntax() ) {
                this.buf.append ( "</jsp:text>" );
            }
            this.buf.append ( "\n" );
        }
        @Override
        public void visit ( final Node.IncludeAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.ForwardAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.GetProperty n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.SetProperty n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.ParamAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.ParamsAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.FallBackAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.UseBean n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.PlugIn n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.NamedAttribute n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.JspBody n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.CustomTag n ) throws JasperException {
            final boolean resetDefaultNSSave = this.resetDefaultNS;
            this.appendTag ( n, this.resetDefaultNS );
            this.resetDefaultNS = resetDefaultNSSave;
        }
        @Override
        public void visit ( final Node.UninterpretedTag n ) throws JasperException {
            final boolean resetDefaultNSSave = this.resetDefaultNS;
            this.appendTag ( n, this.resetDefaultNS );
            this.resetDefaultNS = resetDefaultNSSave;
        }
        @Override
        public void visit ( final Node.JspText n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.DoBodyAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.InvokeAction n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.TagDirective n ) throws JasperException {
            this.appendTagDirective ( n );
        }
        @Override
        public void visit ( final Node.AttributeDirective n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.VariableDirective n ) throws JasperException {
            this.appendTag ( n );
        }
        @Override
        public void visit ( final Node.TemplateText n ) throws JasperException {
            this.appendText ( n.getText(), !n.getRoot().isXmlSyntax() );
        }
        private void appendTag ( final Node n ) throws JasperException {
            this.appendTag ( n, false );
        }
        private void appendTag ( final Node n, final boolean addDefaultNS ) throws JasperException {
            final Node.Nodes body = n.getBody();
            final String text = n.getText();
            this.buf.append ( "<" ).append ( n.getQName() );
            this.buf.append ( "\n" );
            this.printAttributes ( n, addDefaultNS );
            this.buf.append ( "  " ).append ( this.jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            this.buf.append ( this.jspId++ ).append ( "\"\n" );
            if ( "root".equals ( n.getLocalName() ) || body != null || text != null ) {
                this.buf.append ( ">\n" );
                if ( "root".equals ( n.getLocalName() ) ) {
                    if ( this.compiler.getCompilationContext().isTagFile() ) {
                        this.appendTagDirective();
                    } else {
                        this.appendPageDirective();
                    }
                }
                if ( body != null ) {
                    body.visit ( this );
                } else {
                    this.appendText ( text, false );
                }
                this.buf.append ( "</" + n.getQName() + ">\n" );
            } else {
                this.buf.append ( "/>\n" );
            }
        }
        private void appendPageDirective ( final Node.PageDirective n ) {
            boolean append = false;
            final Attributes attrs = n.getAttributes();
            final int len = ( attrs == null ) ? 0 : attrs.getLength();
            for ( int i = 0; i < len; ++i ) {
                final String attrName = attrs.getQName ( i );
                if ( !"pageEncoding".equals ( attrName ) && !"contentType".equals ( attrName ) ) {
                    append = true;
                    break;
                }
            }
            if ( !append ) {
                return;
            }
            this.buf.append ( "<" ).append ( n.getQName() );
            this.buf.append ( "\n" );
            this.buf.append ( "  " ).append ( this.jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            this.buf.append ( this.jspId++ ).append ( "\"\n" );
            for ( int i = 0; i < len; ++i ) {
                final String attrName = attrs.getQName ( i );
                if ( !"import".equals ( attrName ) && !"contentType".equals ( attrName ) ) {
                    if ( !"pageEncoding".equals ( attrName ) ) {
                        final String value = attrs.getValue ( i );
                        this.buf.append ( "  " ).append ( attrName ).append ( "=\"" );
                        this.buf.append ( JspUtil.getExprInXml ( value ) ).append ( "\"\n" );
                    }
                }
            }
            if ( n.getImports().size() > 0 ) {
                boolean first = true;
                final ListIterator<String> iter = n.getImports().listIterator();
                while ( iter.hasNext() ) {
                    if ( first ) {
                        first = false;
                        this.buf.append ( "  import=\"" );
                    } else {
                        this.buf.append ( "," );
                    }
                    this.buf.append ( JspUtil.getExprInXml ( iter.next() ) );
                }
                this.buf.append ( "\"\n" );
            }
            this.buf.append ( "/>\n" );
        }
        private void appendPageDirective() {
            this.buf.append ( "<" ).append ( "jsp:directive.page" );
            this.buf.append ( "\n" );
            this.buf.append ( "  " ).append ( this.jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            this.buf.append ( this.jspId++ ).append ( "\"\n" );
            this.buf.append ( "  " ).append ( "pageEncoding" ).append ( "=\"UTF-8\"\n" );
            this.buf.append ( "  " ).append ( "contentType" ).append ( "=\"" );
            this.buf.append ( this.compiler.getPageInfo().getContentType() ).append ( "\"\n" );
            this.buf.append ( "/>\n" );
        }
        private void appendTagDirective ( final Node.TagDirective n ) throws JasperException {
            boolean append = false;
            final Attributes attrs = n.getAttributes();
            for ( int len = ( attrs == null ) ? 0 : attrs.getLength(), i = 0; i < len; ++i ) {
                final String attrName = attrs.getQName ( i );
                if ( !"pageEncoding".equals ( attrName ) ) {
                    append = true;
                    break;
                }
            }
            if ( !append ) {
                return;
            }
            this.appendTag ( n );
        }
        private void appendTagDirective() {
            this.buf.append ( "<" ).append ( "jsp:directive.tag" );
            this.buf.append ( "\n" );
            this.buf.append ( "  " ).append ( this.jspIdPrefix ).append ( ":id" ).append ( "=\"" );
            this.buf.append ( this.jspId++ ).append ( "\"\n" );
            this.buf.append ( "  " ).append ( "pageEncoding" ).append ( "=\"UTF-8\"\n" );
            this.buf.append ( "/>\n" );
        }
        private void appendText ( final String text, final boolean createJspTextElement ) {
            if ( createJspTextElement ) {
                this.buf.append ( "<" ).append ( "jsp:text" );
                this.buf.append ( "\n" );
                this.buf.append ( "  " ).append ( this.jspIdPrefix ).append ( ":id" ).append ( "=\"" );
                this.buf.append ( this.jspId++ ).append ( "\"\n" );
                this.buf.append ( ">\n" );
                this.appendCDATA ( text );
                this.buf.append ( "</jsp:text>" );
                this.buf.append ( "\n" );
            } else {
                this.appendCDATA ( text );
            }
        }
        private void appendCDATA ( final String text ) {
            this.buf.append ( "<![CDATA[\n" );
            this.buf.append ( this.escapeCDATA ( text ) );
            this.buf.append ( "]]>\n" );
        }
        private String escapeCDATA ( final String text ) {
            if ( text == null ) {
                return "";
            }
            final int len = text.length();
            final CharArrayWriter result = new CharArrayWriter ( len );
            for ( int i = 0; i < len; ++i ) {
                if ( i + 2 < len && text.charAt ( i ) == ']' && text.charAt ( i + 1 ) == ']' && text.charAt ( i + 2 ) == '>' ) {
                    result.write ( 93 );
                    result.write ( 93 );
                    result.write ( 38 );
                    result.write ( 103 );
                    result.write ( 116 );
                    result.write ( 59 );
                    i += 2;
                } else {
                    result.write ( text.charAt ( i ) );
                }
            }
            return result.toString();
        }
        private void printAttributes ( final Node n, final boolean addDefaultNS ) {
            Attributes attrs = n.getTaglibAttributes();
            for ( int len = ( attrs == null ) ? 0 : attrs.getLength(), i = 0; i < len; ++i ) {
                final String name = attrs.getQName ( i );
                final String value = attrs.getValue ( i );
                this.buf.append ( "  " ).append ( name ).append ( "=\"" ).append ( value ).append ( "\"\n" );
            }
            attrs = n.getNonTaglibXmlnsAttributes();
            int len = ( attrs == null ) ? 0 : attrs.getLength();
            boolean defaultNSSeen = false;
            for ( int j = 0; j < len; ++j ) {
                final String name2 = attrs.getQName ( j );
                final String value2 = attrs.getValue ( j );
                this.buf.append ( "  " ).append ( name2 ).append ( "=\"" ).append ( value2 ).append ( "\"\n" );
                defaultNSSeen |= "xmlns".equals ( name2 );
            }
            if ( addDefaultNS && !defaultNSSeen ) {
                this.buf.append ( "  xmlns=\"\"\n" );
            }
            this.resetDefaultNS = false;
            attrs = n.getAttributes();
            len = ( ( attrs == null ) ? 0 : attrs.getLength() );
            for ( int j = 0; j < len; ++j ) {
                final String name2 = attrs.getQName ( j );
                final String value2 = attrs.getValue ( j );
                this.buf.append ( "  " ).append ( name2 ).append ( "=\"" );
                this.buf.append ( JspUtil.getExprInXml ( value2 ) ).append ( "\"\n" );
            }
        }
        private void appendXmlProlog() {
            this.buf.append ( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" );
        }
    }
}
