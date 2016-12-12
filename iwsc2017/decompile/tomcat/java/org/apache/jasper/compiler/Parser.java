package org.apache.jasper.compiler;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.util.Iterator;
import java.util.Collection;
import java.io.FileNotFoundException;
import java.io.CharArrayWriter;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.jasper.util.UniqueAttributesImpl;
import org.xml.sax.Attributes;
import org.apache.jasper.JasperException;
import org.apache.tomcat.Jar;
import org.apache.jasper.JspCompilationContext;
class Parser implements TagConstants {
    private final ParserController parserController;
    private final JspCompilationContext ctxt;
    private final JspReader reader;
    private Mark start;
    private final ErrorDispatcher err;
    private int scriptlessCount;
    private final boolean isTagFile;
    private final boolean directivesOnly;
    private final Jar jar;
    private final PageInfo pageInfo;
    private static final String JAVAX_BODY_CONTENT_PARAM = "JAVAX_BODY_CONTENT_PARAM";
    private static final String JAVAX_BODY_CONTENT_PLUGIN = "JAVAX_BODY_CONTENT_PLUGIN";
    private static final String JAVAX_BODY_CONTENT_TEMPLATE_TEXT = "JAVAX_BODY_CONTENT_TEMPLATE_TEXT";
    private static final boolean STRICT_WHITESPACE;
    private Parser ( final ParserController pc, final JspReader reader, final boolean isTagFile, final boolean directivesOnly, final Jar jar ) {
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.pageInfo = pc.getCompiler().getPageInfo();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.reader = reader;
        this.scriptlessCount = 0;
        this.isTagFile = isTagFile;
        this.directivesOnly = directivesOnly;
        this.jar = jar;
        this.start = reader.mark();
    }
    public static Node.Nodes parse ( final ParserController pc, final JspReader reader, final Node parent, final boolean isTagFile, final boolean directivesOnly, final Jar jar, final String pageEnc, final String jspConfigPageEnc, final boolean isDefaultPageEncoding, final boolean isBomPresent ) throws JasperException {
        final Parser parser = new Parser ( pc, reader, isTagFile, directivesOnly, jar );
        final Node.Root root = new Node.Root ( reader.mark(), parent, false );
        root.setPageEncoding ( pageEnc );
        root.setJspConfigPageEncoding ( jspConfigPageEnc );
        root.setIsDefaultPageEncoding ( isDefaultPageEncoding );
        root.setIsBomPresent ( isBomPresent );
        final PageInfo pageInfo = pc.getCompiler().getPageInfo();
        if ( parent == null && !isTagFile ) {
            parser.addInclude ( root, pageInfo.getIncludePrelude() );
        }
        if ( directivesOnly ) {
            parser.parseFileDirectives ( root );
        } else {
            while ( reader.hasMoreInput() ) {
                parser.parseElements ( root );
            }
        }
        if ( parent == null && !isTagFile ) {
            parser.addInclude ( root, pageInfo.getIncludeCoda() );
        }
        final Node.Nodes page = new Node.Nodes ( root );
        return page;
    }
    Attributes parseAttributes() throws JasperException {
        return this.parseAttributes ( false );
    }
    Attributes parseAttributes ( final boolean pageDirective ) throws JasperException {
        final UniqueAttributesImpl attrs = new UniqueAttributesImpl ( pageDirective );
        this.reader.skipSpaces();
        int ws = 1;
        try {
            while ( this.parseAttribute ( attrs ) ) {
                if ( ws == 0 && Parser.STRICT_WHITESPACE ) {
                    this.err.jspError ( this.reader.mark(), "jsp.error.attribute.nowhitespace", new String[0] );
                }
                ws = this.reader.skipSpaces();
            }
        } catch ( IllegalArgumentException iae ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.attribute.duplicate", new String[0] );
        }
        return attrs;
    }
    public static Attributes parseAttributes ( final ParserController pc, final JspReader reader ) throws JasperException {
        final Parser tmpParser = new Parser ( pc, reader, false, false, null );
        return tmpParser.parseAttributes ( true );
    }
    private boolean parseAttribute ( final AttributesImpl attrs ) throws JasperException {
        final String qName = this.parseName();
        if ( qName == null ) {
            return false;
        }
        boolean ignoreEL = this.pageInfo.isELIgnored();
        String localName = qName;
        String uri = "";
        final int index = qName.indexOf ( 58 );
        if ( index != -1 ) {
            final String prefix = qName.substring ( 0, index );
            uri = this.pageInfo.getURI ( prefix );
            if ( uri == null ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.attribute.invalidPrefix", prefix );
            }
            localName = qName.substring ( index + 1 );
        }
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "=" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.attribute.noequal", new String[0] );
        }
        this.reader.skipSpaces();
        final char quote = ( char ) this.reader.nextChar();
        if ( quote != '\'' && quote != '\"' ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.attribute.noquote", new String[0] );
        }
        String watchString = "";
        if ( this.reader.matches ( "<%=" ) ) {
            watchString = "%>";
            ignoreEL = true;
        }
        watchString += quote;
        final String attrValue = this.parseAttributeValue ( qName, watchString, ignoreEL );
        attrs.addAttribute ( uri, localName, qName, "CDATA", attrValue );
        return true;
    }
    private String parseName() {
        char ch = ( char ) this.reader.peekChar();
        if ( Character.isLetter ( ch ) || ch == '_' || ch == ':' ) {
            final StringBuilder buf = new StringBuilder();
            buf.append ( ch );
            this.reader.nextChar();
            for ( ch = ( char ) this.reader.peekChar(); Character.isLetter ( ch ) || Character.isDigit ( ch ) || ch == '.' || ch == '_' || ch == '-' || ch == ':'; ch = ( char ) this.reader.peekChar() ) {
                buf.append ( ch );
                this.reader.nextChar();
            }
            return buf.toString();
        }
        return null;
    }
    private String parseAttributeValue ( final String qName, final String watch, final boolean ignoreEL ) throws JasperException {
        final boolean quoteAttributeEL = this.ctxt.getOptions().getQuoteAttributeEL();
        final Mark start = this.reader.mark();
        final Mark stop = this.reader.skipUntilIgnoreEsc ( watch, ignoreEL || quoteAttributeEL );
        if ( stop == null ) {
            this.err.jspError ( start, "jsp.error.attribute.unterminated", qName );
        }
        String ret = null;
        try {
            final char quote = watch.charAt ( watch.length() - 1 );
            final boolean isElIgnored = this.pageInfo.isELIgnored() || watch.length() > 1;
            ret = AttributeParser.getUnquoted ( this.reader.getText ( start, stop ), quote, isElIgnored, this.pageInfo.isDeferredSyntaxAllowedAsLiteral(), this.ctxt.getOptions().getStrictQuoteEscaping(), quoteAttributeEL );
        } catch ( IllegalArgumentException iae ) {
            this.err.jspError ( start, iae.getMessage(), new String[0] );
        }
        if ( watch.length() == 1 ) {
            return ret;
        }
        return "<%=" + ret + "%>";
    }
    private String parseScriptText ( final String tx ) {
        final CharArrayWriter cw = new CharArrayWriter();
        final int size = tx.length();
        int i = 0;
        while ( i < size ) {
            final char ch = tx.charAt ( i );
            if ( i + 2 < size && ch == '%' && tx.charAt ( i + 1 ) == '\\' && tx.charAt ( i + 2 ) == '>' ) {
                cw.write ( 37 );
                cw.write ( 62 );
                i += 3;
            } else {
                cw.write ( ch );
                ++i;
            }
        }
        cw.close();
        return cw.toString();
    }
    private void processIncludeDirective ( final String file, final Node parent ) throws JasperException {
        if ( file == null ) {
            return;
        }
        try {
            this.parserController.parse ( file, parent, this.jar );
        } catch ( FileNotFoundException ex2 ) {
            this.err.jspError ( this.start, "jsp.error.file.not.found", file );
        } catch ( Exception ex ) {
            this.err.jspError ( this.start, ex.getMessage(), new String[0] );
        }
    }
    private void parsePageDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes ( true );
        final Node.PageDirective n = new Node.PageDirective ( attrs, this.start, parent );
        for ( int i = 0; i < attrs.getLength(); ++i ) {
            if ( "import".equals ( attrs.getQName ( i ) ) ) {
                n.addImport ( attrs.getValue ( i ) );
            }
        }
    }
    private void parseIncludeDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        final Node includeNode = new Node.IncludeDirective ( attrs, this.start, parent );
        this.processIncludeDirective ( attrs.getValue ( "file" ), includeNode );
    }
    private void addInclude ( final Node parent, final Collection<String> files ) throws JasperException {
        if ( files != null ) {
            for ( final String file : files ) {
                final AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute ( "", "file", "file", "CDATA", file );
                final Node includeNode = new Node.IncludeDirective ( attrs, this.reader.mark(), parent );
                this.processIncludeDirective ( file, includeNode );
            }
        }
    }
    private void parseTaglibDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        final String uri = attrs.getValue ( "uri" );
        final String prefix = attrs.getValue ( "prefix" );
        if ( prefix != null ) {
            final Mark prevMark = this.pageInfo.getNonCustomTagPrefix ( prefix );
            if ( prevMark != null ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.prefix.use_before_dcl", prefix, prevMark.getFile(), "" + prevMark.getLineNumber() );
            }
            if ( uri != null ) {
                final String uriPrev = this.pageInfo.getURI ( prefix );
                if ( uriPrev != null && !uriPrev.equals ( uri ) ) {
                    this.err.jspError ( this.reader.mark(), "jsp.error.prefix.refined", prefix, uri, uriPrev );
                }
                if ( this.pageInfo.getTaglib ( uri ) == null ) {
                    TagLibraryInfoImpl impl = null;
                    if ( this.ctxt.getOptions().isCaching() ) {
                        impl = this.ctxt.getOptions().getCache().get ( uri );
                    }
                    if ( impl == null ) {
                        final TldResourcePath tldResourcePath = this.ctxt.getTldResourcePath ( uri );
                        impl = new TagLibraryInfoImpl ( this.ctxt, this.parserController, this.pageInfo, prefix, uri, tldResourcePath, this.err );
                        if ( this.ctxt.getOptions().isCaching() ) {
                            this.ctxt.getOptions().getCache().put ( uri, impl );
                        }
                    }
                    this.pageInfo.addTaglib ( uri, impl );
                }
                this.pageInfo.addPrefixMapping ( prefix, uri );
            } else {
                final String tagdir = attrs.getValue ( "tagdir" );
                if ( tagdir != null ) {
                    final String urnTagdir = "urn:jsptagdir:" + tagdir;
                    if ( this.pageInfo.getTaglib ( urnTagdir ) == null ) {
                        this.pageInfo.addTaglib ( urnTagdir, new ImplicitTagLibraryInfo ( this.ctxt, this.parserController, this.pageInfo, prefix, tagdir, this.err ) );
                    }
                    this.pageInfo.addPrefixMapping ( prefix, urnTagdir );
                }
            }
        }
        final Node unused = new Node.TaglibDirective ( attrs, this.start, parent );
    }
    private void parseDirective ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        String directive = null;
        if ( this.reader.matches ( "page" ) ) {
            directive = "&lt;%@ page";
            if ( this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.istagfile", directive );
            }
            this.parsePageDirective ( parent );
        } else if ( this.reader.matches ( "include" ) ) {
            directive = "&lt;%@ include";
            this.parseIncludeDirective ( parent );
        } else if ( this.reader.matches ( "taglib" ) ) {
            if ( this.directivesOnly ) {
                return;
            }
            directive = "&lt;%@ taglib";
            this.parseTaglibDirective ( parent );
        } else if ( this.reader.matches ( "tag" ) ) {
            directive = "&lt;%@ tag";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", directive );
            }
            this.parseTagDirective ( parent );
        } else if ( this.reader.matches ( "attribute" ) ) {
            directive = "&lt;%@ attribute";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", directive );
            }
            this.parseAttributeDirective ( parent );
        } else if ( this.reader.matches ( "variable" ) ) {
            directive = "&lt;%@ variable";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", directive );
            }
            this.parseVariableDirective ( parent );
        } else {
            this.err.jspError ( this.reader.mark(), "jsp.error.invalid.directive", new String[0] );
        }
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "%>" ) ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", directive );
        }
    }
    private void parseXMLDirective ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        String eTag = null;
        if ( this.reader.matches ( "page" ) ) {
            eTag = "jsp:directive.page";
            if ( this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.istagfile", "&lt;" + eTag );
            }
            this.parsePageDirective ( parent );
        } else if ( this.reader.matches ( "include" ) ) {
            eTag = "jsp:directive.include";
            this.parseIncludeDirective ( parent );
        } else if ( this.reader.matches ( "tag" ) ) {
            eTag = "jsp:directive.tag";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", "&lt;" + eTag );
            }
            this.parseTagDirective ( parent );
        } else if ( this.reader.matches ( "attribute" ) ) {
            eTag = "jsp:directive.attribute";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", "&lt;" + eTag );
            }
            this.parseAttributeDirective ( parent );
        } else if ( this.reader.matches ( "variable" ) ) {
            eTag = "jsp:directive.variable";
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.directive.isnottagfile", "&lt;" + eTag );
            }
            this.parseVariableDirective ( parent );
        } else {
            this.err.jspError ( this.reader.mark(), "jsp.error.invalid.directive", new String[0] );
        }
        this.reader.skipSpaces();
        if ( this.reader.matches ( ">" ) ) {
            this.reader.skipSpaces();
            if ( !this.reader.matchesETag ( eTag ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;" + eTag );
            }
        } else if ( !this.reader.matches ( "/>" ) ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;" + eTag );
        }
    }
    private void parseTagDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes ( true );
        final Node.TagDirective n = new Node.TagDirective ( attrs, this.start, parent );
        for ( int i = 0; i < attrs.getLength(); ++i ) {
            if ( "import".equals ( attrs.getQName ( i ) ) ) {
                n.addImport ( attrs.getValue ( i ) );
            }
        }
    }
    private void parseAttributeDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        final Node unused = new Node.AttributeDirective ( attrs, this.start, parent );
    }
    private void parseVariableDirective ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        final Node unused = new Node.VariableDirective ( attrs, this.start, parent );
    }
    private void parseComment ( final Node parent ) throws JasperException {
        this.start = this.reader.mark();
        final Mark stop = this.reader.skipUntil ( "--%>" );
        if ( stop == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;%--" );
        }
        final Node unused = new Node.Comment ( this.reader.getText ( this.start, stop ), this.start, parent );
    }
    private void parseDeclaration ( final Node parent ) throws JasperException {
        this.start = this.reader.mark();
        final Mark stop = this.reader.skipUntil ( "%>" );
        if ( stop == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;%!" );
        }
        final Node unused = new Node.Declaration ( this.parseScriptText ( this.reader.getText ( this.start, stop ) ), this.start, parent );
    }
    private void parseXMLDeclaration ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "/>" ) ) {
            if ( !this.reader.matches ( ">" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:declaration&gt;" );
            }
            while ( true ) {
                this.start = this.reader.mark();
                Mark stop = this.reader.skipUntil ( "<" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:declaration&gt;" );
                }
                String text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node unused = new Node.Declaration ( text, this.start, parent );
                if ( !this.reader.matches ( "![CDATA[" ) ) {
                    break;
                }
                this.start = this.reader.mark();
                stop = this.reader.skipUntil ( "]]>" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "CDATA" );
                }
                text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node.Declaration declaration = new Node.Declaration ( text, this.start, parent );
            }
            if ( !this.reader.matchesETagWithoutLessThan ( "jsp:declaration" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:declaration&gt;" );
            }
        }
    }
    private void parseExpression ( final Node parent ) throws JasperException {
        this.start = this.reader.mark();
        final Mark stop = this.reader.skipUntil ( "%>" );
        if ( stop == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;%=" );
        }
        final Node unused = new Node.Expression ( this.parseScriptText ( this.reader.getText ( this.start, stop ) ), this.start, parent );
    }
    private void parseXMLExpression ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "/>" ) ) {
            if ( !this.reader.matches ( ">" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:expression&gt;" );
            }
            while ( true ) {
                this.start = this.reader.mark();
                Mark stop = this.reader.skipUntil ( "<" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:expression&gt;" );
                }
                String text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node unused = new Node.Expression ( text, this.start, parent );
                if ( !this.reader.matches ( "![CDATA[" ) ) {
                    break;
                }
                this.start = this.reader.mark();
                stop = this.reader.skipUntil ( "]]>" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "CDATA" );
                }
                text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node.Expression expression = new Node.Expression ( text, this.start, parent );
            }
            if ( !this.reader.matchesETagWithoutLessThan ( "jsp:expression" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:expression&gt;" );
            }
        }
    }
    private void parseELExpression ( final Node parent, final char type ) throws JasperException {
        this.start = this.reader.mark();
        final Mark last = this.reader.skipELExpression();
        if ( last == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", type + "{" );
        }
        final Node unused = new Node.ELExpression ( type, this.reader.getText ( this.start, last ), this.start, parent );
    }
    private void parseScriptlet ( final Node parent ) throws JasperException {
        this.start = this.reader.mark();
        final Mark stop = this.reader.skipUntil ( "%>" );
        if ( stop == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;%" );
        }
        final Node unused = new Node.Scriptlet ( this.parseScriptText ( this.reader.getText ( this.start, stop ) ), this.start, parent );
    }
    private void parseXMLScriptlet ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "/>" ) ) {
            if ( !this.reader.matches ( ">" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:scriptlet&gt;" );
            }
            while ( true ) {
                this.start = this.reader.mark();
                Mark stop = this.reader.skipUntil ( "<" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:scriptlet&gt;" );
                }
                String text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node unused = new Node.Scriptlet ( text, this.start, parent );
                if ( !this.reader.matches ( "![CDATA[" ) ) {
                    break;
                }
                this.start = this.reader.mark();
                stop = this.reader.skipUntil ( "]]>" );
                if ( stop == null ) {
                    this.err.jspError ( this.start, "jsp.error.unterminated", "CDATA" );
                }
                text = this.parseScriptText ( this.reader.getText ( this.start, stop ) );
                final Node.Scriptlet scriptlet = new Node.Scriptlet ( text, this.start, parent );
            }
            if ( !this.reader.matchesETagWithoutLessThan ( "jsp:scriptlet" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:scriptlet&gt;" );
            }
        }
    }
    private void parseParam ( final Node parent ) throws JasperException {
        if ( !this.reader.matches ( "<jsp:param" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.paramexpected", new String[0] );
        }
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node paramActionNode = new Node.ParamAction ( attrs, this.start, parent );
        this.parseEmptyBody ( paramActionNode, "jsp:param" );
        this.reader.skipSpaces();
    }
    private void parseInclude ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node includeNode = new Node.IncludeAction ( attrs, this.start, parent );
        this.parseOptionalBody ( includeNode, "jsp:include", "JAVAX_BODY_CONTENT_PARAM" );
    }
    private void parseForward ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node forwardNode = new Node.ForwardAction ( attrs, this.start, parent );
        this.parseOptionalBody ( forwardNode, "jsp:forward", "JAVAX_BODY_CONTENT_PARAM" );
    }
    private void parseInvoke ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node invokeNode = new Node.InvokeAction ( attrs, this.start, parent );
        this.parseEmptyBody ( invokeNode, "jsp:invoke" );
    }
    private void parseDoBody ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node doBodyNode = new Node.DoBodyAction ( attrs, this.start, parent );
        this.parseEmptyBody ( doBodyNode, "jsp:doBody" );
    }
    private void parseElement ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node elementNode = new Node.JspElement ( attrs, this.start, parent );
        this.parseOptionalBody ( elementNode, "jsp:element", "JSP" );
    }
    private void parseGetProperty ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node getPropertyNode = new Node.GetProperty ( attrs, this.start, parent );
        this.parseOptionalBody ( getPropertyNode, "jsp:getProperty", "empty" );
    }
    private void parseSetProperty ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node setPropertyNode = new Node.SetProperty ( attrs, this.start, parent );
        this.parseOptionalBody ( setPropertyNode, "jsp:setProperty", "empty" );
    }
    private void parseEmptyBody ( final Node parent, final String tag ) throws JasperException {
        if ( !this.reader.matches ( "/>" ) ) {
            if ( this.reader.matches ( ">" ) ) {
                if ( !this.reader.matchesETag ( tag ) ) {
                    if ( this.reader.matchesOptionalSpacesFollowedBy ( "<jsp:attribute" ) ) {
                        this.parseNamedAttributes ( parent );
                        if ( !this.reader.matchesETag ( tag ) ) {
                            this.err.jspError ( this.reader.mark(), "jsp.error.jspbody.emptybody.only", "&lt;" + tag );
                        }
                    } else {
                        this.err.jspError ( this.reader.mark(), "jsp.error.jspbody.emptybody.only", "&lt;" + tag );
                    }
                }
            } else {
                this.err.jspError ( this.reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
            }
        }
    }
    private void parseUseBean ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node useBeanNode = new Node.UseBean ( attrs, this.start, parent );
        this.parseOptionalBody ( useBeanNode, "jsp:useBean", "JSP" );
    }
    private void parseOptionalBody ( final Node parent, final String tag, final String bodyType ) throws JasperException {
        if ( this.reader.matches ( "/>" ) ) {
            return;
        }
        if ( !this.reader.matches ( ">" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
        }
        if ( this.reader.matchesETag ( tag ) ) {
            return;
        }
        if ( !this.parseJspAttributeAndBody ( parent, tag, bodyType ) ) {
            this.parseBody ( parent, tag, bodyType );
        }
    }
    private boolean parseJspAttributeAndBody ( final Node parent, final String tag, final String bodyType ) throws JasperException {
        boolean result = false;
        if ( this.reader.matchesOptionalSpacesFollowedBy ( "<jsp:attribute" ) ) {
            this.parseNamedAttributes ( parent );
            result = true;
        }
        if ( this.reader.matchesOptionalSpacesFollowedBy ( "<jsp:body" ) ) {
            this.parseJspBody ( parent, bodyType );
            this.reader.skipSpaces();
            if ( !this.reader.matchesETag ( tag ) ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
            }
            result = true;
        } else if ( result && !this.reader.matchesETag ( tag ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.jspbody.required", "&lt;" + tag );
        }
        return result;
    }
    private void parseJspParams ( final Node parent ) throws JasperException {
        final Node jspParamsNode = new Node.ParamsAction ( this.start, parent );
        this.parseOptionalBody ( jspParamsNode, "jsp:params", "JAVAX_BODY_CONTENT_PARAM" );
    }
    private void parseFallBack ( final Node parent ) throws JasperException {
        final Node fallBackNode = new Node.FallBackAction ( this.start, parent );
        this.parseOptionalBody ( fallBackNode, "jsp:fallback", "JAVAX_BODY_CONTENT_TEMPLATE_TEXT" );
    }
    private void parsePlugin ( final Node parent ) throws JasperException {
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        final Node pluginNode = new Node.PlugIn ( attrs, this.start, parent );
        this.parseOptionalBody ( pluginNode, "jsp:plugin", "JAVAX_BODY_CONTENT_PLUGIN" );
    }
    private void parsePluginTags ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        if ( this.reader.matches ( "<jsp:params" ) ) {
            this.parseJspParams ( parent );
            this.reader.skipSpaces();
        }
        if ( this.reader.matches ( "<jsp:fallback" ) ) {
            this.parseFallBack ( parent );
            this.reader.skipSpaces();
        }
    }
    private void parseStandardAction ( final Node parent ) throws JasperException {
        final Mark start = this.reader.mark();
        if ( this.reader.matches ( "include" ) ) {
            this.parseInclude ( parent );
        } else if ( this.reader.matches ( "forward" ) ) {
            this.parseForward ( parent );
        } else if ( this.reader.matches ( "invoke" ) ) {
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.action.isnottagfile", "&lt;jsp:invoke" );
            }
            this.parseInvoke ( parent );
        } else if ( this.reader.matches ( "doBody" ) ) {
            if ( !this.isTagFile ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.action.isnottagfile", "&lt;jsp:doBody" );
            }
            this.parseDoBody ( parent );
        } else if ( this.reader.matches ( "getProperty" ) ) {
            this.parseGetProperty ( parent );
        } else if ( this.reader.matches ( "setProperty" ) ) {
            this.parseSetProperty ( parent );
        } else if ( this.reader.matches ( "useBean" ) ) {
            this.parseUseBean ( parent );
        } else if ( this.reader.matches ( "plugin" ) ) {
            this.parsePlugin ( parent );
        } else if ( this.reader.matches ( "element" ) ) {
            this.parseElement ( parent );
        } else if ( this.reader.matches ( "attribute" ) ) {
            this.err.jspError ( start, "jsp.error.namedAttribute.invalidUse", new String[0] );
        } else if ( this.reader.matches ( "body" ) ) {
            this.err.jspError ( start, "jsp.error.jspbody.invalidUse", new String[0] );
        } else if ( this.reader.matches ( "fallback" ) ) {
            this.err.jspError ( start, "jsp.error.fallback.invalidUse", new String[0] );
        } else if ( this.reader.matches ( "params" ) ) {
            this.err.jspError ( start, "jsp.error.params.invalidUse", new String[0] );
        } else if ( this.reader.matches ( "param" ) ) {
            this.err.jspError ( start, "jsp.error.param.invalidUse", new String[0] );
        } else if ( this.reader.matches ( "output" ) ) {
            this.err.jspError ( start, "jsp.error.jspoutput.invalidUse", new String[0] );
        } else {
            this.err.jspError ( start, "jsp.error.badStandardAction", new String[0] );
        }
    }
    private boolean parseCustomTag ( final Node parent ) throws JasperException {
        if ( this.reader.peekChar() != 60 ) {
            return false;
        }
        this.reader.nextChar();
        final String tagName = this.reader.parseToken ( false );
        final int i = tagName.indexOf ( 58 );
        if ( i == -1 ) {
            this.reader.reset ( this.start );
            return false;
        }
        final String prefix = tagName.substring ( 0, i );
        final String shortTagName = tagName.substring ( i + 1 );
        final String uri = this.pageInfo.getURI ( prefix );
        if ( uri == null ) {
            if ( !this.pageInfo.isErrorOnUndeclaredNamespace() ) {
                this.reader.reset ( this.start );
                this.pageInfo.putNonCustomTagPrefix ( prefix, this.reader.mark() );
                return false;
            }
            this.err.jspError ( this.start, "jsp.error.undeclared_namespace", prefix );
        }
        final TagLibraryInfo tagLibInfo = this.pageInfo.getTaglib ( uri );
        final TagInfo tagInfo = tagLibInfo.getTag ( shortTagName );
        final TagFileInfo tagFileInfo = tagLibInfo.getTagFile ( shortTagName );
        if ( tagInfo == null && tagFileInfo == null ) {
            this.err.jspError ( this.start, "jsp.error.bad_tag", shortTagName, prefix );
        }
        Class<?> tagHandlerClass = null;
        if ( tagInfo != null ) {
            final String handlerClassName = tagInfo.getTagClassName();
            try {
                tagHandlerClass = this.ctxt.getClassLoader().loadClass ( handlerClassName );
            } catch ( Exception e ) {
                this.err.jspError ( this.start, "jsp.error.loadclass.taghandler", handlerClassName, tagName );
            }
        }
        final Attributes attrs = this.parseAttributes();
        this.reader.skipSpaces();
        if ( this.reader.matches ( "/>" ) ) {
            if ( tagInfo != null ) {
                final Node.CustomTag customTag = new Node.CustomTag ( tagName, prefix, shortTagName, uri, attrs, this.start, parent, tagInfo, tagHandlerClass );
            } else {
                final Node.CustomTag customTag2 = new Node.CustomTag ( tagName, prefix, shortTagName, uri, attrs, this.start, parent, tagFileInfo );
            }
            return true;
        }
        String bc;
        if ( tagInfo != null ) {
            bc = tagInfo.getBodyContent();
        } else {
            bc = tagFileInfo.getTagInfo().getBodyContent();
        }
        Node tagNode = null;
        if ( tagInfo != null ) {
            tagNode = new Node.CustomTag ( tagName, prefix, shortTagName, uri, attrs, this.start, parent, tagInfo, tagHandlerClass );
        } else {
            tagNode = new Node.CustomTag ( tagName, prefix, shortTagName, uri, attrs, this.start, parent, tagFileInfo );
        }
        this.parseOptionalBody ( tagNode, tagName, bc );
        return true;
    }
    private void parseTemplateText ( final Node parent ) {
        if ( !this.reader.hasMoreInput() ) {
            return;
        }
        final CharArrayWriter ttext = new CharArrayWriter();
        for ( int ch = this.reader.nextChar(); ch != -1; ch = this.reader.nextChar() ) {
            if ( ch == 60 ) {
                if ( this.reader.peekChar ( 0 ) == 92 && this.reader.peekChar ( 1 ) == 37 ) {
                    ttext.write ( ch );
                    this.reader.nextChar();
                    ttext.write ( this.reader.nextChar() );
                } else {
                    if ( ttext.size() != 0 ) {
                        this.reader.pushChar();
                        break;
                    }
                    ttext.write ( ch );
                }
            } else if ( ch == 92 && !this.pageInfo.isELIgnored() ) {
                final int next = this.reader.peekChar ( 0 );
                if ( next == 36 || next == 35 ) {
                    ttext.write ( this.reader.nextChar() );
                } else {
                    ttext.write ( ch );
                }
            } else if ( ( ch == 36 || ( ch == 35 && !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() ) ) && !this.pageInfo.isELIgnored() ) {
                if ( this.reader.peekChar ( 0 ) == 123 ) {
                    this.reader.pushChar();
                    break;
                }
                ttext.write ( ch );
            } else {
                ttext.write ( ch );
            }
        }
        final Node unused = new Node.TemplateText ( ttext.toString(), this.start, parent );
    }
    private void parseXMLTemplateText ( final Node parent ) throws JasperException {
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "/>" ) ) {
            if ( !this.reader.matches ( ">" ) ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:text&gt;" );
            }
            final CharArrayWriter ttext = new CharArrayWriter();
            for ( int ch = this.reader.nextChar(); ch != -1; ch = this.reader.nextChar() ) {
                if ( ch == 60 ) {
                    if ( !this.reader.matches ( "![CDATA[" ) ) {
                        break;
                    }
                    this.start = this.reader.mark();
                    final Mark stop = this.reader.skipUntil ( "]]>" );
                    if ( stop == null ) {
                        this.err.jspError ( this.start, "jsp.error.unterminated", "CDATA" );
                    }
                    final String text = this.reader.getText ( this.start, stop );
                    ttext.write ( text, 0, text.length() );
                } else if ( ch == 92 ) {
                    final int next = this.reader.peekChar ( 0 );
                    if ( next == 36 || next == 35 ) {
                        ttext.write ( this.reader.nextChar() );
                    } else {
                        ttext.write ( 92 );
                    }
                } else if ( ch == 36 || ch == 35 ) {
                    if ( this.reader.peekChar ( 0 ) == 123 ) {
                        this.reader.nextChar();
                        final Node unused = new Node.TemplateText ( ttext.toString(), this.start, parent );
                        this.parseELExpression ( parent, ( char ) ch );
                        this.start = this.reader.mark();
                        ttext.reset();
                    } else {
                        ttext.write ( ch );
                    }
                } else {
                    ttext.write ( ch );
                }
            }
            final Node unused = new Node.TemplateText ( ttext.toString(), this.start, parent );
            if ( !this.reader.hasMoreInput() ) {
                this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;jsp:text&gt;" );
            } else if ( !this.reader.matchesETagWithoutLessThan ( "jsp:text" ) ) {
                this.err.jspError ( this.start, "jsp.error.jsptext.badcontent", new String[0] );
            }
        }
    }
    private void parseElements ( final Node parent ) throws JasperException {
        if ( this.scriptlessCount > 0 ) {
            this.parseElementsScriptless ( parent );
            return;
        }
        this.start = this.reader.mark();
        if ( this.reader.matches ( "<%--" ) ) {
            this.parseComment ( parent );
        } else if ( this.reader.matches ( "<%@" ) ) {
            this.parseDirective ( parent );
        } else if ( this.reader.matches ( "<jsp:directive." ) ) {
            this.parseXMLDirective ( parent );
        } else if ( this.reader.matches ( "<%!" ) ) {
            this.parseDeclaration ( parent );
        } else if ( this.reader.matches ( "<jsp:declaration" ) ) {
            this.parseXMLDeclaration ( parent );
        } else if ( this.reader.matches ( "<%=" ) ) {
            this.parseExpression ( parent );
        } else if ( this.reader.matches ( "<jsp:expression" ) ) {
            this.parseXMLExpression ( parent );
        } else if ( this.reader.matches ( "<%" ) ) {
            this.parseScriptlet ( parent );
        } else if ( this.reader.matches ( "<jsp:scriptlet" ) ) {
            this.parseXMLScriptlet ( parent );
        } else if ( this.reader.matches ( "<jsp:text" ) ) {
            this.parseXMLTemplateText ( parent );
        } else if ( !this.pageInfo.isELIgnored() && this.reader.matches ( "${" ) ) {
            this.parseELExpression ( parent, '$' );
        } else if ( !this.pageInfo.isELIgnored() && !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() && this.reader.matches ( "#{" ) ) {
            this.parseELExpression ( parent, '#' );
        } else if ( this.reader.matches ( "<jsp:" ) ) {
            this.parseStandardAction ( parent );
        } else if ( !this.parseCustomTag ( parent ) ) {
            this.checkUnbalancedEndTag();
            this.parseTemplateText ( parent );
        }
    }
    private void parseElementsScriptless ( final Node parent ) throws JasperException {
        ++this.scriptlessCount;
        this.start = this.reader.mark();
        if ( this.reader.matches ( "<%--" ) ) {
            this.parseComment ( parent );
        } else if ( this.reader.matches ( "<%@" ) ) {
            this.parseDirective ( parent );
        } else if ( this.reader.matches ( "<jsp:directive." ) ) {
            this.parseXMLDirective ( parent );
        } else if ( this.reader.matches ( "<%!" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<jsp:declaration" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<%=" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<jsp:expression" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<%" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<jsp:scriptlet" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.no.scriptlets", new String[0] );
        } else if ( this.reader.matches ( "<jsp:text" ) ) {
            this.parseXMLTemplateText ( parent );
        } else if ( !this.pageInfo.isELIgnored() && this.reader.matches ( "${" ) ) {
            this.parseELExpression ( parent, '$' );
        } else if ( !this.pageInfo.isELIgnored() && !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() && this.reader.matches ( "#{" ) ) {
            this.parseELExpression ( parent, '#' );
        } else if ( this.reader.matches ( "<jsp:" ) ) {
            this.parseStandardAction ( parent );
        } else if ( !this.parseCustomTag ( parent ) ) {
            this.checkUnbalancedEndTag();
            this.parseTemplateText ( parent );
        }
        --this.scriptlessCount;
    }
    private void parseElementsTemplateText ( final Node parent ) throws JasperException {
        this.start = this.reader.mark();
        if ( this.reader.matches ( "<%--" ) ) {
            this.parseComment ( parent );
        } else if ( this.reader.matches ( "<%@" ) ) {
            this.parseDirective ( parent );
        } else if ( this.reader.matches ( "<jsp:directive." ) ) {
            this.parseXMLDirective ( parent );
        } else if ( this.reader.matches ( "<%!" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Declarations" );
        } else if ( this.reader.matches ( "<jsp:declaration" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Declarations" );
        } else if ( this.reader.matches ( "<%=" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Expressions" );
        } else if ( this.reader.matches ( "<jsp:expression" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Expressions" );
        } else if ( this.reader.matches ( "<%" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Scriptlets" );
        } else if ( this.reader.matches ( "<jsp:scriptlet" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Scriptlets" );
        } else if ( this.reader.matches ( "<jsp:text" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "&lt;jsp:text" );
        } else if ( !this.pageInfo.isELIgnored() && this.reader.matches ( "${" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Expression language" );
        } else if ( !this.pageInfo.isELIgnored() && !this.pageInfo.isDeferredSyntaxAllowedAsLiteral() && this.reader.matches ( "#{" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Expression language" );
        } else if ( this.reader.matches ( "<jsp:" ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Standard actions" );
        } else if ( this.parseCustomTag ( parent ) ) {
            this.err.jspError ( this.reader.mark(), "jsp.error.not.in.template", "Custom actions" );
        } else {
            this.checkUnbalancedEndTag();
            this.parseTemplateText ( parent );
        }
    }
    private void checkUnbalancedEndTag() throws JasperException {
        if ( !this.reader.matches ( "</" ) ) {
            return;
        }
        if ( this.reader.matches ( "jsp:" ) ) {
            this.err.jspError ( this.start, "jsp.error.unbalanced.endtag", "jsp:" );
        }
        final String tagName = this.reader.parseToken ( false );
        final int i = tagName.indexOf ( 58 );
        if ( i == -1 || this.pageInfo.getURI ( tagName.substring ( 0, i ) ) == null ) {
            this.reader.reset ( this.start );
            return;
        }
        this.err.jspError ( this.start, "jsp.error.unbalanced.endtag", tagName );
    }
    private void parseTagDependentBody ( final Node parent, final String tag ) throws JasperException {
        final Mark bodyStart = this.reader.mark();
        final Mark bodyEnd = this.reader.skipUntilETag ( tag );
        if ( bodyEnd == null ) {
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;" + tag );
        }
        final Node unused = new Node.TemplateText ( this.reader.getText ( bodyStart, bodyEnd ), bodyStart, parent );
    }
    private void parseJspBody ( final Node parent, final String bodyType ) throws JasperException {
        final Mark start = this.reader.mark();
        final Node bodyNode = new Node.JspBody ( start, parent );
        this.reader.skipSpaces();
        if ( !this.reader.matches ( "/>" ) ) {
            if ( !this.reader.matches ( ">" ) ) {
                this.err.jspError ( start, "jsp.error.unterminated", "&lt;jsp:body" );
            }
            this.parseBody ( bodyNode, "jsp:body", bodyType );
        }
    }
    private void parseBody ( final Node parent, final String tag, final String bodyType ) throws JasperException {
        if ( bodyType.equalsIgnoreCase ( "tagdependent" ) ) {
            this.parseTagDependentBody ( parent, tag );
        } else if ( bodyType.equalsIgnoreCase ( "empty" ) ) {
            if ( !this.reader.matchesETag ( tag ) ) {
                this.err.jspError ( this.start, "jasper.error.emptybodycontent.nonempty", tag );
            }
        } else if ( bodyType == "JAVAX_BODY_CONTENT_PLUGIN" ) {
            this.parsePluginTags ( parent );
            if ( !this.reader.matchesETag ( tag ) ) {
                this.err.jspError ( this.reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
            }
        } else if ( bodyType.equalsIgnoreCase ( "JSP" ) || bodyType.equalsIgnoreCase ( "scriptless" ) || bodyType == "JAVAX_BODY_CONTENT_PARAM" || bodyType == "JAVAX_BODY_CONTENT_TEMPLATE_TEXT" ) {
            while ( this.reader.hasMoreInput() ) {
                if ( this.reader.matchesETag ( tag ) ) {
                    return;
                }
                if ( tag.equals ( "jsp:body" ) || tag.equals ( "jsp:attribute" ) ) {
                    if ( this.reader.matches ( "<jsp:attribute" ) ) {
                        this.err.jspError ( this.reader.mark(), "jsp.error.nested.jspattribute", new String[0] );
                    } else if ( this.reader.matches ( "<jsp:body" ) ) {
                        this.err.jspError ( this.reader.mark(), "jsp.error.nested.jspbody", new String[0] );
                    }
                }
                if ( bodyType.equalsIgnoreCase ( "JSP" ) ) {
                    this.parseElements ( parent );
                } else if ( bodyType.equalsIgnoreCase ( "scriptless" ) ) {
                    this.parseElementsScriptless ( parent );
                } else if ( bodyType == "JAVAX_BODY_CONTENT_PARAM" ) {
                    this.reader.skipSpaces();
                    this.parseParam ( parent );
                } else {
                    if ( bodyType != "JAVAX_BODY_CONTENT_TEMPLATE_TEXT" ) {
                        continue;
                    }
                    this.parseElementsTemplateText ( parent );
                }
            }
            this.err.jspError ( this.start, "jsp.error.unterminated", "&lt;" + tag );
        } else {
            this.err.jspError ( this.start, "jasper.error.bad.bodycontent.type", new String[0] );
        }
    }
    private void parseNamedAttributes ( final Node parent ) throws JasperException {
        do {
            final Mark start = this.reader.mark();
            final Attributes attrs = this.parseAttributes();
            final Node.NamedAttribute namedAttributeNode = new Node.NamedAttribute ( attrs, start, parent );
            this.reader.skipSpaces();
            if ( !this.reader.matches ( "/>" ) ) {
                if ( !this.reader.matches ( ">" ) ) {
                    this.err.jspError ( start, "jsp.error.unterminated", "&lt;jsp:attribute" );
                }
                if ( namedAttributeNode.isTrim() ) {
                    this.reader.skipSpaces();
                }
                this.parseBody ( namedAttributeNode, "jsp:attribute", this.getAttributeBodyType ( parent, attrs.getValue ( "name" ) ) );
                if ( namedAttributeNode.isTrim() ) {
                    final Node.Nodes subElems = namedAttributeNode.getBody();
                    if ( subElems != null ) {
                        final Node lastNode = subElems.getNode ( subElems.size() - 1 );
                        if ( lastNode instanceof Node.TemplateText ) {
                            ( ( Node.TemplateText ) lastNode ).rtrim();
                        }
                    }
                }
            }
            this.reader.skipSpaces();
        } while ( this.reader.matches ( "<jsp:attribute" ) );
    }
    private String getAttributeBodyType ( final Node n, final String name ) {
        if ( n instanceof Node.CustomTag ) {
            final TagInfo tagInfo = ( ( Node.CustomTag ) n ).getTagInfo();
            final TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            for ( int i = 0; i < tldAttrs.length; ++i ) {
                if ( name.equals ( tldAttrs[i].getName() ) ) {
                    if ( tldAttrs[i].isFragment() ) {
                        return "scriptless";
                    }
                    if ( tldAttrs[i].canBeRequestTime() ) {
                        return "JSP";
                    }
                }
            }
            if ( tagInfo.hasDynamicAttributes() ) {
                return "JSP";
            }
        } else if ( n instanceof Node.IncludeAction ) {
            if ( "page".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.ForwardAction ) {
            if ( "page".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.SetProperty ) {
            if ( "value".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.UseBean ) {
            if ( "beanName".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.PlugIn ) {
            if ( "width".equals ( name ) || "height".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.ParamAction ) {
            if ( "value".equals ( name ) ) {
                return "JSP";
            }
        } else if ( n instanceof Node.JspElement ) {
            return "JSP";
        }
        return "JAVAX_BODY_CONTENT_TEMPLATE_TEXT";
    }
    private void parseFileDirectives ( final Node parent ) throws JasperException {
        this.reader.skipUntil ( "<" );
        while ( this.reader.hasMoreInput() ) {
            this.start = this.reader.mark();
            if ( this.reader.matches ( "%--" ) ) {
                this.reader.skipUntil ( "--%>" );
            } else if ( this.reader.matches ( "%@" ) ) {
                this.parseDirective ( parent );
            } else if ( this.reader.matches ( "jsp:directive." ) ) {
                this.parseXMLDirective ( parent );
            } else if ( this.reader.matches ( "%!" ) ) {
                this.reader.skipUntil ( "%>" );
            } else if ( this.reader.matches ( "%=" ) ) {
                this.reader.skipUntil ( "%>" );
            } else if ( this.reader.matches ( "%" ) ) {
                this.reader.skipUntil ( "%>" );
            }
            this.reader.skipUntil ( "<" );
        }
    }
    static {
        STRICT_WHITESPACE = Boolean.parseBoolean ( System.getProperty ( "org.apache.jasper.compiler.Parser.STRICT_WHITESPACE", "true" ) );
    }
}
