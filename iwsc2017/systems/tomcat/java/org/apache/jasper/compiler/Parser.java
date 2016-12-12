package org.apache.jasper.compiler;
import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.util.UniqueAttributesImpl;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
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
    private static final String JAVAX_BODY_CONTENT_PARAM =
        "JAVAX_BODY_CONTENT_PARAM";
    private static final String JAVAX_BODY_CONTENT_PLUGIN =
        "JAVAX_BODY_CONTENT_PLUGIN";
    private static final String JAVAX_BODY_CONTENT_TEMPLATE_TEXT =
        "JAVAX_BODY_CONTENT_TEMPLATE_TEXT";
    private static final boolean STRICT_WHITESPACE = Boolean.parseBoolean (
                System.getProperty (
                    "org.apache.jasper.compiler.Parser.STRICT_WHITESPACE",
                    "true" ) );
    private Parser ( ParserController pc, JspReader reader, boolean isTagFile,
                     boolean directivesOnly, Jar jar ) {
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.pageInfo = pc.getCompiler().getPageInfo();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.reader = reader;
        this.scriptlessCount = 0;
        this.isTagFile = isTagFile;
        this.directivesOnly = directivesOnly;
        this.jar = jar;
        start = reader.mark();
    }
    public static Node.Nodes parse ( ParserController pc, JspReader reader,
                                     Node parent, boolean isTagFile, boolean directivesOnly,
                                     Jar jar, String pageEnc, String jspConfigPageEnc,
                                     boolean isDefaultPageEncoding, boolean isBomPresent )
    throws JasperException {
        Parser parser = new Parser ( pc, reader, isTagFile, directivesOnly, jar );
        Node.Root root = new Node.Root ( reader.mark(), parent, false );
        root.setPageEncoding ( pageEnc );
        root.setJspConfigPageEncoding ( jspConfigPageEnc );
        root.setIsDefaultPageEncoding ( isDefaultPageEncoding );
        root.setIsBomPresent ( isBomPresent );
        PageInfo pageInfo = pc.getCompiler().getPageInfo();
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
        Node.Nodes page = new Node.Nodes ( root );
        return page;
    }
    Attributes parseAttributes() throws JasperException {
        return parseAttributes ( false );
    }
    Attributes parseAttributes ( boolean pageDirective ) throws JasperException {
        UniqueAttributesImpl attrs = new UniqueAttributesImpl ( pageDirective );
        reader.skipSpaces();
        int ws = 1;
        try {
            while ( parseAttribute ( attrs ) ) {
                if ( ws == 0 && STRICT_WHITESPACE ) {
                    err.jspError ( reader.mark(),
                                   "jsp.error.attribute.nowhitespace" );
                }
                ws = reader.skipSpaces();
            }
        } catch ( IllegalArgumentException iae ) {
            err.jspError ( reader.mark(), "jsp.error.attribute.duplicate" );
        }
        return attrs;
    }
    public static Attributes parseAttributes ( ParserController pc,
            JspReader reader ) throws JasperException {
        Parser tmpParser = new Parser ( pc, reader, false, false, null );
        return tmpParser.parseAttributes ( true );
    }
    private boolean parseAttribute ( AttributesImpl attrs )
    throws JasperException {
        String qName = parseName();
        if ( qName == null ) {
            return false;
        }
        boolean ignoreEL = pageInfo.isELIgnored();
        String localName = qName;
        String uri = "";
        int index = qName.indexOf ( ':' );
        if ( index != -1 ) {
            String prefix = qName.substring ( 0, index );
            uri = pageInfo.getURI ( prefix );
            if ( uri == null ) {
                err.jspError ( reader.mark(),
                               "jsp.error.attribute.invalidPrefix", prefix );
            }
            localName = qName.substring ( index + 1 );
        }
        reader.skipSpaces();
        if ( !reader.matches ( "=" ) ) {
            err.jspError ( reader.mark(), "jsp.error.attribute.noequal" );
        }
        reader.skipSpaces();
        char quote = ( char ) reader.nextChar();
        if ( quote != '\'' && quote != '"' ) {
            err.jspError ( reader.mark(), "jsp.error.attribute.noquote" );
        }
        String watchString = "";
        if ( reader.matches ( "<%=" ) ) {
            watchString = "%>";
            ignoreEL = true;
        }
        watchString = watchString + quote;
        String attrValue = parseAttributeValue ( qName, watchString, ignoreEL );
        attrs.addAttribute ( uri, localName, qName, "CDATA", attrValue );
        return true;
    }
    private String parseName() {
        char ch = ( char ) reader.peekChar();
        if ( Character.isLetter ( ch ) || ch == '_' || ch == ':' ) {
            StringBuilder buf = new StringBuilder();
            buf.append ( ch );
            reader.nextChar();
            ch = ( char ) reader.peekChar();
            while ( Character.isLetter ( ch ) || Character.isDigit ( ch ) || ch == '.'
                    || ch == '_' || ch == '-' || ch == ':' ) {
                buf.append ( ch );
                reader.nextChar();
                ch = ( char ) reader.peekChar();
            }
            return buf.toString();
        }
        return null;
    }
    private String parseAttributeValue ( String qName, String watch, boolean ignoreEL ) throws JasperException {
        boolean quoteAttributeEL = ctxt.getOptions().getQuoteAttributeEL();
        Mark start = reader.mark();
        Mark stop = reader.skipUntilIgnoreEsc ( watch, ignoreEL || quoteAttributeEL );
        if ( stop == null ) {
            err.jspError ( start, "jsp.error.attribute.unterminated", qName );
        }
        String ret = null;
        try {
            char quote = watch.charAt ( watch.length() - 1 );
            boolean isElIgnored =
                pageInfo.isELIgnored() || watch.length() > 1;
            ret = AttributeParser.getUnquoted ( reader.getText ( start, stop ),
                                                quote, isElIgnored,
                                                pageInfo.isDeferredSyntaxAllowedAsLiteral(),
                                                ctxt.getOptions().getStrictQuoteEscaping(),
                                                quoteAttributeEL );
        } catch ( IllegalArgumentException iae ) {
            err.jspError ( start, iae.getMessage() );
        }
        if ( watch.length() == 1 ) {
            return ret;
        }
        return "<%=" + ret + "%>";
    }
    private String parseScriptText ( String tx ) {
        CharArrayWriter cw = new CharArrayWriter();
        int size = tx.length();
        int i = 0;
        while ( i < size ) {
            char ch = tx.charAt ( i );
            if ( i + 2 < size && ch == '%' && tx.charAt ( i + 1 ) == '\\'
                    && tx.charAt ( i + 2 ) == '>' ) {
                cw.write ( '%' );
                cw.write ( '>' );
                i += 3;
            } else {
                cw.write ( ch );
                ++i;
            }
        }
        cw.close();
        return cw.toString();
    }
    private void processIncludeDirective ( String file, Node parent )
    throws JasperException {
        if ( file == null ) {
            return;
        }
        try {
            parserController.parse ( file, parent, jar );
        } catch ( FileNotFoundException ex ) {
            err.jspError ( start, "jsp.error.file.not.found", file );
        } catch ( Exception ex ) {
            err.jspError ( start, ex.getMessage() );
        }
    }
    private void parsePageDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes ( true );
        Node.PageDirective n = new Node.PageDirective ( attrs, start, parent );
        for ( int i = 0; i < attrs.getLength(); i++ ) {
            if ( "import".equals ( attrs.getQName ( i ) ) ) {
                n.addImport ( attrs.getValue ( i ) );
            }
        }
    }
    private void parseIncludeDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        Node includeNode = new Node.IncludeDirective ( attrs, start, parent );
        processIncludeDirective ( attrs.getValue ( "file" ), includeNode );
    }
    private void addInclude ( Node parent, Collection<String> files ) throws JasperException {
        if ( files != null ) {
            Iterator<String> iter = files.iterator();
            while ( iter.hasNext() ) {
                String file = iter.next();
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute ( "", "file", "file", "CDATA", file );
                Node includeNode = new Node.IncludeDirective ( attrs, reader
                        .mark(), parent );
                processIncludeDirective ( file, includeNode );
            }
        }
    }
    private void parseTaglibDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        String uri = attrs.getValue ( "uri" );
        String prefix = attrs.getValue ( "prefix" );
        if ( prefix != null ) {
            Mark prevMark = pageInfo.getNonCustomTagPrefix ( prefix );
            if ( prevMark != null ) {
                err.jspError ( reader.mark(), "jsp.error.prefix.use_before_dcl",
                               prefix, prevMark.getFile(), ""
                               + prevMark.getLineNumber() );
            }
            if ( uri != null ) {
                String uriPrev = pageInfo.getURI ( prefix );
                if ( uriPrev != null && !uriPrev.equals ( uri ) ) {
                    err.jspError ( reader.mark(), "jsp.error.prefix.refined",
                                   prefix, uri, uriPrev );
                }
                if ( pageInfo.getTaglib ( uri ) == null ) {
                    TagLibraryInfoImpl impl = null;
                    if ( ctxt.getOptions().isCaching() ) {
                        impl = ( TagLibraryInfoImpl ) ctxt.getOptions()
                               .getCache().get ( uri );
                    }
                    if ( impl == null ) {
                        TldResourcePath tldResourcePath = ctxt.getTldResourcePath ( uri );
                        impl = new TagLibraryInfoImpl ( ctxt, parserController,
                                                        pageInfo, prefix, uri, tldResourcePath, err );
                        if ( ctxt.getOptions().isCaching() ) {
                            ctxt.getOptions().getCache().put ( uri, impl );
                        }
                    }
                    pageInfo.addTaglib ( uri, impl );
                }
                pageInfo.addPrefixMapping ( prefix, uri );
            } else {
                String tagdir = attrs.getValue ( "tagdir" );
                if ( tagdir != null ) {
                    String urnTagdir = URN_JSPTAGDIR + tagdir;
                    if ( pageInfo.getTaglib ( urnTagdir ) == null ) {
                        pageInfo.addTaglib ( urnTagdir,
                                             new ImplicitTagLibraryInfo ( ctxt,
                                                     parserController, pageInfo, prefix,
                                                     tagdir, err ) );
                    }
                    pageInfo.addPrefixMapping ( prefix, urnTagdir );
                }
            }
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.TaglibDirective ( attrs, start, parent );
    }
    private void parseDirective ( Node parent ) throws JasperException {
        reader.skipSpaces();
        String directive = null;
        if ( reader.matches ( "page" ) ) {
            directive = "&lt;%@ page";
            if ( isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.istagfile",
                               directive );
            }
            parsePageDirective ( parent );
        } else if ( reader.matches ( "include" ) ) {
            directive = "&lt;%@ include";
            parseIncludeDirective ( parent );
        } else if ( reader.matches ( "taglib" ) ) {
            if ( directivesOnly ) {
                return;
            }
            directive = "&lt;%@ taglib";
            parseTaglibDirective ( parent );
        } else if ( reader.matches ( "tag" ) ) {
            directive = "&lt;%@ tag";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               directive );
            }
            parseTagDirective ( parent );
        } else if ( reader.matches ( "attribute" ) ) {
            directive = "&lt;%@ attribute";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               directive );
            }
            parseAttributeDirective ( parent );
        } else if ( reader.matches ( "variable" ) ) {
            directive = "&lt;%@ variable";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               directive );
            }
            parseVariableDirective ( parent );
        } else {
            err.jspError ( reader.mark(), "jsp.error.invalid.directive" );
        }
        reader.skipSpaces();
        if ( !reader.matches ( "%>" ) ) {
            err.jspError ( start, "jsp.error.unterminated", directive );
        }
    }
    private void parseXMLDirective ( Node parent ) throws JasperException {
        reader.skipSpaces();
        String eTag = null;
        if ( reader.matches ( "page" ) ) {
            eTag = "jsp:directive.page";
            if ( isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.istagfile",
                               "&lt;" + eTag );
            }
            parsePageDirective ( parent );
        } else if ( reader.matches ( "include" ) ) {
            eTag = "jsp:directive.include";
            parseIncludeDirective ( parent );
        } else if ( reader.matches ( "tag" ) ) {
            eTag = "jsp:directive.tag";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               "&lt;" + eTag );
            }
            parseTagDirective ( parent );
        } else if ( reader.matches ( "attribute" ) ) {
            eTag = "jsp:directive.attribute";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               "&lt;" + eTag );
            }
            parseAttributeDirective ( parent );
        } else if ( reader.matches ( "variable" ) ) {
            eTag = "jsp:directive.variable";
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.directive.isnottagfile",
                               "&lt;" + eTag );
            }
            parseVariableDirective ( parent );
        } else {
            err.jspError ( reader.mark(), "jsp.error.invalid.directive" );
        }
        reader.skipSpaces();
        if ( reader.matches ( ">" ) ) {
            reader.skipSpaces();
            if ( !reader.matchesETag ( eTag ) ) {
                err.jspError ( start, "jsp.error.unterminated", "&lt;" + eTag );
            }
        } else if ( !reader.matches ( "/>" ) ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;" + eTag );
        }
    }
    private void parseTagDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes ( true );
        Node.TagDirective n = new Node.TagDirective ( attrs, start, parent );
        for ( int i = 0; i < attrs.getLength(); i++ ) {
            if ( "import".equals ( attrs.getQName ( i ) ) ) {
                n.addImport ( attrs.getValue ( i ) );
            }
        }
    }
    private void parseAttributeDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        @SuppressWarnings ( "unused" )
        Node unused = new Node.AttributeDirective ( attrs, start, parent );
    }
    private void parseVariableDirective ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        @SuppressWarnings ( "unused" )
        Node unused = new Node.VariableDirective ( attrs, start, parent );
    }
    private void parseComment ( Node parent ) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil ( "--%>" );
        if ( stop == null ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;%--" );
        }
        @SuppressWarnings ( "unused" )
        Node unused =
            new Node.Comment ( reader.getText ( start, stop ), start, parent );
    }
    private void parseDeclaration ( Node parent ) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil ( "%>" );
        if ( stop == null ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;%!" );
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.Declaration (
            parseScriptText ( reader.getText ( start, stop ) ), start, parent );
    }
    private void parseXMLDeclaration ( Node parent ) throws JasperException {
        reader.skipSpaces();
        if ( !reader.matches ( "/>" ) ) {
            if ( !reader.matches ( ">" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:declaration&gt;" );
            }
            Mark stop;
            String text;
            while ( true ) {
                start = reader.mark();
                stop = reader.skipUntil ( "<" );
                if ( stop == null ) {
                    err.jspError ( start, "jsp.error.unterminated",
                                   "&lt;jsp:declaration&gt;" );
                }
                text = parseScriptText ( reader.getText ( start, stop ) );
                @SuppressWarnings ( "unused" )
                Node unused = new Node.Declaration ( text, start, parent );
                if ( reader.matches ( "![CDATA[" ) ) {
                    start = reader.mark();
                    stop = reader.skipUntil ( "]]>" );
                    if ( stop == null ) {
                        err.jspError ( start, "jsp.error.unterminated", "CDATA" );
                    }
                    text = parseScriptText ( reader.getText ( start, stop ) );
                    @SuppressWarnings ( "unused" )
                    Node unused2 = new Node.Declaration ( text, start, parent );
                } else {
                    break;
                }
            }
            if ( !reader.matchesETagWithoutLessThan ( "jsp:declaration" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:declaration&gt;" );
            }
        }
    }
    private void parseExpression ( Node parent ) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil ( "%>" );
        if ( stop == null ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;%=" );
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.Expression (
            parseScriptText ( reader.getText ( start, stop ) ), start, parent );
    }
    private void parseXMLExpression ( Node parent ) throws JasperException {
        reader.skipSpaces();
        if ( !reader.matches ( "/>" ) ) {
            if ( !reader.matches ( ">" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:expression&gt;" );
            }
            Mark stop;
            String text;
            while ( true ) {
                start = reader.mark();
                stop = reader.skipUntil ( "<" );
                if ( stop == null ) {
                    err.jspError ( start, "jsp.error.unterminated",
                                   "&lt;jsp:expression&gt;" );
                }
                text = parseScriptText ( reader.getText ( start, stop ) );
                @SuppressWarnings ( "unused" )
                Node unused = new Node.Expression ( text, start, parent );
                if ( reader.matches ( "![CDATA[" ) ) {
                    start = reader.mark();
                    stop = reader.skipUntil ( "]]>" );
                    if ( stop == null ) {
                        err.jspError ( start, "jsp.error.unterminated", "CDATA" );
                    }
                    text = parseScriptText ( reader.getText ( start, stop ) );
                    @SuppressWarnings ( "unused" )
                    Node unused2 = new Node.Expression ( text, start, parent );
                } else {
                    break;
                }
            }
            if ( !reader.matchesETagWithoutLessThan ( "jsp:expression" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:expression&gt;" );
            }
        }
    }
    private void parseELExpression ( Node parent, char type )
    throws JasperException {
        start = reader.mark();
        Mark last = reader.skipELExpression();
        if ( last == null ) {
            err.jspError ( start, "jsp.error.unterminated", type + "{" );
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.ELExpression ( type, reader.getText ( start, last ),
                                              start, parent );
    }
    private void parseScriptlet ( Node parent ) throws JasperException {
        start = reader.mark();
        Mark stop = reader.skipUntil ( "%>" );
        if ( stop == null ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;%" );
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.Scriptlet (
            parseScriptText ( reader.getText ( start, stop ) ), start, parent );
    }
    private void parseXMLScriptlet ( Node parent ) throws JasperException {
        reader.skipSpaces();
        if ( !reader.matches ( "/>" ) ) {
            if ( !reader.matches ( ">" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:scriptlet&gt;" );
            }
            Mark stop;
            String text;
            while ( true ) {
                start = reader.mark();
                stop = reader.skipUntil ( "<" );
                if ( stop == null ) {
                    err.jspError ( start, "jsp.error.unterminated",
                                   "&lt;jsp:scriptlet&gt;" );
                }
                text = parseScriptText ( reader.getText ( start, stop ) );
                @SuppressWarnings ( "unused" )
                Node unused = new Node.Scriptlet ( text, start, parent );
                if ( reader.matches ( "![CDATA[" ) ) {
                    start = reader.mark();
                    stop = reader.skipUntil ( "]]>" );
                    if ( stop == null ) {
                        err.jspError ( start, "jsp.error.unterminated", "CDATA" );
                    }
                    text = parseScriptText ( reader.getText ( start, stop ) );
                    @SuppressWarnings ( "unused" )
                    Node unused2 = new Node.Scriptlet ( text, start, parent );
                } else {
                    break;
                }
            }
            if ( !reader.matchesETagWithoutLessThan ( "jsp:scriptlet" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:scriptlet&gt;" );
            }
        }
    }
    private void parseParam ( Node parent ) throws JasperException {
        if ( !reader.matches ( "<jsp:param" ) ) {
            err.jspError ( reader.mark(), "jsp.error.paramexpected" );
        }
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node paramActionNode = new Node.ParamAction ( attrs, start, parent );
        parseEmptyBody ( paramActionNode, "jsp:param" );
        reader.skipSpaces();
    }
    private void parseInclude ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node includeNode = new Node.IncludeAction ( attrs, start, parent );
        parseOptionalBody ( includeNode, "jsp:include", JAVAX_BODY_CONTENT_PARAM );
    }
    private void parseForward ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node forwardNode = new Node.ForwardAction ( attrs, start, parent );
        parseOptionalBody ( forwardNode, "jsp:forward", JAVAX_BODY_CONTENT_PARAM );
    }
    private void parseInvoke ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node invokeNode = new Node.InvokeAction ( attrs, start, parent );
        parseEmptyBody ( invokeNode, "jsp:invoke" );
    }
    private void parseDoBody ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node doBodyNode = new Node.DoBodyAction ( attrs, start, parent );
        parseEmptyBody ( doBodyNode, "jsp:doBody" );
    }
    private void parseElement ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node elementNode = new Node.JspElement ( attrs, start, parent );
        parseOptionalBody ( elementNode, "jsp:element", TagInfo.BODY_CONTENT_JSP );
    }
    private void parseGetProperty ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node getPropertyNode = new Node.GetProperty ( attrs, start, parent );
        parseOptionalBody ( getPropertyNode, "jsp:getProperty",
                            TagInfo.BODY_CONTENT_EMPTY );
    }
    private void parseSetProperty ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node setPropertyNode = new Node.SetProperty ( attrs, start, parent );
        parseOptionalBody ( setPropertyNode, "jsp:setProperty",
                            TagInfo.BODY_CONTENT_EMPTY );
    }
    private void parseEmptyBody ( Node parent, String tag ) throws JasperException {
        if ( reader.matches ( "/>" ) ) {
        } else if ( reader.matches ( ">" ) ) {
            if ( reader.matchesETag ( tag ) ) {
            } else if ( reader.matchesOptionalSpacesFollowedBy ( "<jsp:attribute" ) ) {
                parseNamedAttributes ( parent );
                if ( !reader.matchesETag ( tag ) ) {
                    err.jspError ( reader.mark(),
                                   "jsp.error.jspbody.emptybody.only", "&lt;" + tag );
                }
            } else {
                err.jspError ( reader.mark(), "jsp.error.jspbody.emptybody.only",
                               "&lt;" + tag );
            }
        } else {
            err.jspError ( reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
        }
    }
    private void parseUseBean ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node useBeanNode = new Node.UseBean ( attrs, start, parent );
        parseOptionalBody ( useBeanNode, "jsp:useBean", TagInfo.BODY_CONTENT_JSP );
    }
    private void parseOptionalBody ( Node parent, String tag, String bodyType )
    throws JasperException {
        if ( reader.matches ( "/>" ) ) {
            return;
        }
        if ( !reader.matches ( ">" ) ) {
            err.jspError ( reader.mark(), "jsp.error.unterminated", "&lt;" + tag );
        }
        if ( reader.matchesETag ( tag ) ) {
            return;
        }
        if ( !parseJspAttributeAndBody ( parent, tag, bodyType ) ) {
            parseBody ( parent, tag, bodyType );
        }
    }
    private boolean parseJspAttributeAndBody ( Node parent, String tag,
            String bodyType ) throws JasperException {
        boolean result = false;
        if ( reader.matchesOptionalSpacesFollowedBy ( "<jsp:attribute" ) ) {
            parseNamedAttributes ( parent );
            result = true;
        }
        if ( reader.matchesOptionalSpacesFollowedBy ( "<jsp:body" ) ) {
            parseJspBody ( parent, bodyType );
            reader.skipSpaces();
            if ( !reader.matchesETag ( tag ) ) {
                err.jspError ( reader.mark(), "jsp.error.unterminated", "&lt;"
                               + tag );
            }
            result = true;
        } else if ( result && !reader.matchesETag ( tag ) ) {
            err.jspError ( reader.mark(), "jsp.error.jspbody.required", "&lt;"
                           + tag );
        }
        return result;
    }
    private void parseJspParams ( Node parent ) throws JasperException {
        Node jspParamsNode = new Node.ParamsAction ( start, parent );
        parseOptionalBody ( jspParamsNode, "jsp:params", JAVAX_BODY_CONTENT_PARAM );
    }
    private void parseFallBack ( Node parent ) throws JasperException {
        Node fallBackNode = new Node.FallBackAction ( start, parent );
        parseOptionalBody ( fallBackNode, "jsp:fallback",
                            JAVAX_BODY_CONTENT_TEMPLATE_TEXT );
    }
    private void parsePlugin ( Node parent ) throws JasperException {
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        Node pluginNode = new Node.PlugIn ( attrs, start, parent );
        parseOptionalBody ( pluginNode, "jsp:plugin", JAVAX_BODY_CONTENT_PLUGIN );
    }
    private void parsePluginTags ( Node parent ) throws JasperException {
        reader.skipSpaces();
        if ( reader.matches ( "<jsp:params" ) ) {
            parseJspParams ( parent );
            reader.skipSpaces();
        }
        if ( reader.matches ( "<jsp:fallback" ) ) {
            parseFallBack ( parent );
            reader.skipSpaces();
        }
    }
    private void parseStandardAction ( Node parent ) throws JasperException {
        Mark start = reader.mark();
        if ( reader.matches ( INCLUDE_ACTION ) ) {
            parseInclude ( parent );
        } else if ( reader.matches ( FORWARD_ACTION ) ) {
            parseForward ( parent );
        } else if ( reader.matches ( INVOKE_ACTION ) ) {
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.action.isnottagfile",
                               "&lt;jsp:invoke" );
            }
            parseInvoke ( parent );
        } else if ( reader.matches ( DOBODY_ACTION ) ) {
            if ( !isTagFile ) {
                err.jspError ( reader.mark(), "jsp.error.action.isnottagfile",
                               "&lt;jsp:doBody" );
            }
            parseDoBody ( parent );
        } else if ( reader.matches ( GET_PROPERTY_ACTION ) ) {
            parseGetProperty ( parent );
        } else if ( reader.matches ( SET_PROPERTY_ACTION ) ) {
            parseSetProperty ( parent );
        } else if ( reader.matches ( USE_BEAN_ACTION ) ) {
            parseUseBean ( parent );
        } else if ( reader.matches ( PLUGIN_ACTION ) ) {
            parsePlugin ( parent );
        } else if ( reader.matches ( ELEMENT_ACTION ) ) {
            parseElement ( parent );
        } else if ( reader.matches ( ATTRIBUTE_ACTION ) ) {
            err.jspError ( start, "jsp.error.namedAttribute.invalidUse" );
        } else if ( reader.matches ( BODY_ACTION ) ) {
            err.jspError ( start, "jsp.error.jspbody.invalidUse" );
        } else if ( reader.matches ( FALLBACK_ACTION ) ) {
            err.jspError ( start, "jsp.error.fallback.invalidUse" );
        } else if ( reader.matches ( PARAMS_ACTION ) ) {
            err.jspError ( start, "jsp.error.params.invalidUse" );
        } else if ( reader.matches ( PARAM_ACTION ) ) {
            err.jspError ( start, "jsp.error.param.invalidUse" );
        } else if ( reader.matches ( OUTPUT_ACTION ) ) {
            err.jspError ( start, "jsp.error.jspoutput.invalidUse" );
        } else {
            err.jspError ( start, "jsp.error.badStandardAction" );
        }
    }
    @SuppressWarnings ( "null" )
    private boolean parseCustomTag ( Node parent ) throws JasperException {
        if ( reader.peekChar() != '<' ) {
            return false;
        }
        reader.nextChar();
        String tagName = reader.parseToken ( false );
        int i = tagName.indexOf ( ':' );
        if ( i == -1 ) {
            reader.reset ( start );
            return false;
        }
        String prefix = tagName.substring ( 0, i );
        String shortTagName = tagName.substring ( i + 1 );
        String uri = pageInfo.getURI ( prefix );
        if ( uri == null ) {
            if ( pageInfo.isErrorOnUndeclaredNamespace() ) {
                err.jspError ( start, "jsp.error.undeclared_namespace", prefix );
            } else {
                reader.reset ( start );
                pageInfo.putNonCustomTagPrefix ( prefix, reader.mark() );
                return false;
            }
        }
        TagLibraryInfo tagLibInfo = pageInfo.getTaglib ( uri );
        TagInfo tagInfo = tagLibInfo.getTag ( shortTagName );
        TagFileInfo tagFileInfo = tagLibInfo.getTagFile ( shortTagName );
        if ( tagInfo == null && tagFileInfo == null ) {
            err.jspError ( start, "jsp.error.bad_tag", shortTagName, prefix );
        }
        Class<?> tagHandlerClass = null;
        if ( tagInfo != null ) {
            String handlerClassName = tagInfo.getTagClassName();
            try {
                tagHandlerClass = ctxt.getClassLoader().loadClass (
                                      handlerClassName );
            } catch ( Exception e ) {
                err.jspError ( start, "jsp.error.loadclass.taghandler",
                               handlerClassName, tagName );
            }
        }
        Attributes attrs = parseAttributes();
        reader.skipSpaces();
        if ( reader.matches ( "/>" ) ) {
            if ( tagInfo != null ) {
                @SuppressWarnings ( "unused" )
                Node unused = new Node.CustomTag ( tagName, prefix, shortTagName,
                                                   uri, attrs, start, parent, tagInfo, tagHandlerClass );
            } else {
                @SuppressWarnings ( "unused" )
                Node unused = new Node.CustomTag ( tagName, prefix, shortTagName,
                                                   uri, attrs, start, parent, tagFileInfo );
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
            tagNode = new Node.CustomTag ( tagName, prefix, shortTagName, uri,
                                           attrs, start, parent, tagInfo, tagHandlerClass );
        } else {
            tagNode = new Node.CustomTag ( tagName, prefix, shortTagName, uri,
                                           attrs, start, parent, tagFileInfo );
        }
        parseOptionalBody ( tagNode, tagName, bc );
        return true;
    }
    private void parseTemplateText ( Node parent ) {
        if ( !reader.hasMoreInput() ) {
            return;
        }
        CharArrayWriter ttext = new CharArrayWriter();
        int ch = reader.nextChar();
        while ( ch != -1 ) {
            if ( ch == '<' ) {
                if ( reader.peekChar ( 0 ) == '\\' && reader.peekChar ( 1 ) == '%' ) {
                    ttext.write ( ch );
                    reader.nextChar();
                    ttext.write ( reader.nextChar() );
                } else {
                    if ( ttext.size() == 0 ) {
                        ttext.write ( ch );
                    } else {
                        reader.pushChar();
                        break;
                    }
                }
            } else if ( ch == '\\' && !pageInfo.isELIgnored() ) {
                int next = reader.peekChar ( 0 );
                if ( next == '$' || next == '#' ) {
                    ttext.write ( reader.nextChar() );
                } else {
                    ttext.write ( ch );
                }
            } else if ( ( ch == '$' || ch == '#' && !pageInfo.isDeferredSyntaxAllowedAsLiteral() ) &&
                        !pageInfo.isELIgnored() ) {
                if ( reader.peekChar ( 0 ) == '{' ) {
                    reader.pushChar();
                    break;
                } else {
                    ttext.write ( ch );
                }
            } else {
                ttext.write ( ch );
            }
            ch = reader.nextChar();
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.TemplateText ( ttext.toString(), start, parent );
    }
    private void parseXMLTemplateText ( Node parent ) throws JasperException {
        reader.skipSpaces();
        if ( !reader.matches ( "/>" ) ) {
            if ( !reader.matches ( ">" ) ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:text&gt;" );
            }
            CharArrayWriter ttext = new CharArrayWriter();
            int ch = reader.nextChar();
            while ( ch != -1 ) {
                if ( ch == '<' ) {
                    if ( !reader.matches ( "![CDATA[" ) ) {
                        break;
                    }
                    start = reader.mark();
                    Mark stop = reader.skipUntil ( "]]>" );
                    if ( stop == null ) {
                        err.jspError ( start, "jsp.error.unterminated", "CDATA" );
                    }
                    String text = reader.getText ( start, stop );
                    ttext.write ( text, 0, text.length() );
                } else if ( ch == '\\' ) {
                    int next = reader.peekChar ( 0 );
                    if ( next == '$' || next == '#' ) {
                        ttext.write ( reader.nextChar() );
                    } else {
                        ttext.write ( '\\' );
                    }
                } else if ( ch == '$' || ch == '#' ) {
                    if ( reader.peekChar ( 0 ) == '{' ) {
                        reader.nextChar();
                        @SuppressWarnings ( "unused" )
                        Node unused = new Node.TemplateText (
                            ttext.toString(), start, parent );
                        parseELExpression ( parent, ( char ) ch );
                        start = reader.mark();
                        ttext.reset();
                    } else {
                        ttext.write ( ch );
                    }
                } else {
                    ttext.write ( ch );
                }
                ch = reader.nextChar();
            }
            @SuppressWarnings ( "unused" )
            Node unused =
                new Node.TemplateText ( ttext.toString(), start, parent );
            if ( !reader.hasMoreInput() ) {
                err.jspError ( start, "jsp.error.unterminated",
                               "&lt;jsp:text&gt;" );
            } else if ( !reader.matchesETagWithoutLessThan ( "jsp:text" ) ) {
                err.jspError ( start, "jsp.error.jsptext.badcontent" );
            }
        }
    }
    private void parseElements ( Node parent ) throws JasperException {
        if ( scriptlessCount > 0 ) {
            parseElementsScriptless ( parent );
            return;
        }
        start = reader.mark();
        if ( reader.matches ( "<%--" ) ) {
            parseComment ( parent );
        } else if ( reader.matches ( "<%@" ) ) {
            parseDirective ( parent );
        } else if ( reader.matches ( "<jsp:directive." ) ) {
            parseXMLDirective ( parent );
        } else if ( reader.matches ( "<%!" ) ) {
            parseDeclaration ( parent );
        } else if ( reader.matches ( "<jsp:declaration" ) ) {
            parseXMLDeclaration ( parent );
        } else if ( reader.matches ( "<%=" ) ) {
            parseExpression ( parent );
        } else if ( reader.matches ( "<jsp:expression" ) ) {
            parseXMLExpression ( parent );
        } else if ( reader.matches ( "<%" ) ) {
            parseScriptlet ( parent );
        } else if ( reader.matches ( "<jsp:scriptlet" ) ) {
            parseXMLScriptlet ( parent );
        } else if ( reader.matches ( "<jsp:text" ) ) {
            parseXMLTemplateText ( parent );
        } else if ( !pageInfo.isELIgnored() && reader.matches ( "${" ) ) {
            parseELExpression ( parent, '$' );
        } else if ( !pageInfo.isELIgnored()
                    && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                    && reader.matches ( "#{" ) ) {
            parseELExpression ( parent, '#' );
        } else if ( reader.matches ( "<jsp:" ) ) {
            parseStandardAction ( parent );
        } else if ( !parseCustomTag ( parent ) ) {
            checkUnbalancedEndTag();
            parseTemplateText ( parent );
        }
    }
    private void parseElementsScriptless ( Node parent ) throws JasperException {
        scriptlessCount++;
        start = reader.mark();
        if ( reader.matches ( "<%--" ) ) {
            parseComment ( parent );
        } else if ( reader.matches ( "<%@" ) ) {
            parseDirective ( parent );
        } else if ( reader.matches ( "<jsp:directive." ) ) {
            parseXMLDirective ( parent );
        } else if ( reader.matches ( "<%!" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<jsp:declaration" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<%=" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<jsp:expression" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<%" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<jsp:scriptlet" ) ) {
            err.jspError ( reader.mark(), "jsp.error.no.scriptlets" );
        } else if ( reader.matches ( "<jsp:text" ) ) {
            parseXMLTemplateText ( parent );
        } else if ( !pageInfo.isELIgnored() && reader.matches ( "${" ) ) {
            parseELExpression ( parent, '$' );
        } else if ( !pageInfo.isELIgnored()
                    && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                    && reader.matches ( "#{" ) ) {
            parseELExpression ( parent, '#' );
        } else if ( reader.matches ( "<jsp:" ) ) {
            parseStandardAction ( parent );
        } else if ( !parseCustomTag ( parent ) ) {
            checkUnbalancedEndTag();
            parseTemplateText ( parent );
        }
        scriptlessCount--;
    }
    private void parseElementsTemplateText ( Node parent ) throws JasperException {
        start = reader.mark();
        if ( reader.matches ( "<%--" ) ) {
            parseComment ( parent );
        } else if ( reader.matches ( "<%@" ) ) {
            parseDirective ( parent );
        } else if ( reader.matches ( "<jsp:directive." ) ) {
            parseXMLDirective ( parent );
        } else if ( reader.matches ( "<%!" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Declarations" );
        } else if ( reader.matches ( "<jsp:declaration" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Declarations" );
        } else if ( reader.matches ( "<%=" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Expressions" );
        } else if ( reader.matches ( "<jsp:expression" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Expressions" );
        } else if ( reader.matches ( "<%" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Scriptlets" );
        } else if ( reader.matches ( "<jsp:scriptlet" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Scriptlets" );
        } else if ( reader.matches ( "<jsp:text" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "&lt;jsp:text" );
        } else if ( !pageInfo.isELIgnored() && reader.matches ( "${" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Expression language" );
        } else if ( !pageInfo.isELIgnored()
                    && !pageInfo.isDeferredSyntaxAllowedAsLiteral()
                    && reader.matches ( "#{" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Expression language" );
        } else if ( reader.matches ( "<jsp:" ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Standard actions" );
        } else if ( parseCustomTag ( parent ) ) {
            err.jspError ( reader.mark(), "jsp.error.not.in.template",
                           "Custom actions" );
        } else {
            checkUnbalancedEndTag();
            parseTemplateText ( parent );
        }
    }
    private void checkUnbalancedEndTag() throws JasperException {
        if ( !reader.matches ( "</" ) ) {
            return;
        }
        if ( reader.matches ( "jsp:" ) ) {
            err.jspError ( start, "jsp.error.unbalanced.endtag", "jsp:" );
        }
        String tagName = reader.parseToken ( false );
        int i = tagName.indexOf ( ':' );
        if ( i == -1 || pageInfo.getURI ( tagName.substring ( 0, i ) ) == null ) {
            reader.reset ( start );
            return;
        }
        err.jspError ( start, "jsp.error.unbalanced.endtag", tagName );
    }
    private void parseTagDependentBody ( Node parent, String tag )
    throws JasperException {
        Mark bodyStart = reader.mark();
        Mark bodyEnd = reader.skipUntilETag ( tag );
        if ( bodyEnd == null ) {
            err.jspError ( start, "jsp.error.unterminated", "&lt;" + tag );
        }
        @SuppressWarnings ( "unused" )
        Node unused = new Node.TemplateText ( reader.getText ( bodyStart, bodyEnd ),
                                              bodyStart, parent );
    }
    private void parseJspBody ( Node parent, String bodyType )
    throws JasperException {
        Mark start = reader.mark();
        Node bodyNode = new Node.JspBody ( start, parent );
        reader.skipSpaces();
        if ( !reader.matches ( "/>" ) ) {
            if ( !reader.matches ( ">" ) ) {
                err.jspError ( start, "jsp.error.unterminated", "&lt;jsp:body" );
            }
            parseBody ( bodyNode, "jsp:body", bodyType );
        }
    }
    private void parseBody ( Node parent, String tag, String bodyType )
    throws JasperException {
        if ( bodyType.equalsIgnoreCase ( TagInfo.BODY_CONTENT_TAG_DEPENDENT ) ) {
            parseTagDependentBody ( parent, tag );
        } else if ( bodyType.equalsIgnoreCase ( TagInfo.BODY_CONTENT_EMPTY ) ) {
            if ( !reader.matchesETag ( tag ) ) {
                err.jspError ( start, "jasper.error.emptybodycontent.nonempty",
                               tag );
            }
        } else if ( bodyType == JAVAX_BODY_CONTENT_PLUGIN ) {
            parsePluginTags ( parent );
            if ( !reader.matchesETag ( tag ) ) {
                err.jspError ( reader.mark(), "jsp.error.unterminated", "&lt;"
                               + tag );
            }
        } else if ( bodyType.equalsIgnoreCase ( TagInfo.BODY_CONTENT_JSP )
                    || bodyType.equalsIgnoreCase ( TagInfo.BODY_CONTENT_SCRIPTLESS )
                    || ( bodyType == JAVAX_BODY_CONTENT_PARAM )
                    || ( bodyType == JAVAX_BODY_CONTENT_TEMPLATE_TEXT ) ) {
            while ( reader.hasMoreInput() ) {
                if ( reader.matchesETag ( tag ) ) {
                    return;
                }
                if ( tag.equals ( "jsp:body" ) || tag.equals ( "jsp:attribute" ) ) {
                    if ( reader.matches ( "<jsp:attribute" ) ) {
                        err.jspError ( reader.mark(),
                                       "jsp.error.nested.jspattribute" );
                    } else if ( reader.matches ( "<jsp:body" ) ) {
                        err.jspError ( reader.mark(), "jsp.error.nested.jspbody" );
                    }
                }
                if ( bodyType.equalsIgnoreCase ( TagInfo.BODY_CONTENT_JSP ) ) {
                    parseElements ( parent );
                } else if ( bodyType
                            .equalsIgnoreCase ( TagInfo.BODY_CONTENT_SCRIPTLESS ) ) {
                    parseElementsScriptless ( parent );
                } else if ( bodyType == JAVAX_BODY_CONTENT_PARAM ) {
                    reader.skipSpaces();
                    parseParam ( parent );
                } else if ( bodyType == JAVAX_BODY_CONTENT_TEMPLATE_TEXT ) {
                    parseElementsTemplateText ( parent );
                }
            }
            err.jspError ( start, "jsp.error.unterminated", "&lt;" + tag );
        } else {
            err.jspError ( start, "jasper.error.bad.bodycontent.type" );
        }
    }
    private void parseNamedAttributes ( Node parent ) throws JasperException {
        do {
            Mark start = reader.mark();
            Attributes attrs = parseAttributes();
            Node.NamedAttribute namedAttributeNode = new Node.NamedAttribute (
                attrs, start, parent );
            reader.skipSpaces();
            if ( !reader.matches ( "/>" ) ) {
                if ( !reader.matches ( ">" ) ) {
                    err.jspError ( start, "jsp.error.unterminated",
                                   "&lt;jsp:attribute" );
                }
                if ( namedAttributeNode.isTrim() ) {
                    reader.skipSpaces();
                }
                parseBody ( namedAttributeNode, "jsp:attribute",
                            getAttributeBodyType ( parent, attrs.getValue ( "name" ) ) );
                if ( namedAttributeNode.isTrim() ) {
                    Node.Nodes subElems = namedAttributeNode.getBody();
                    if ( subElems != null ) {
                        Node lastNode = subElems.getNode ( subElems.size() - 1 );
                        if ( lastNode instanceof Node.TemplateText ) {
                            ( ( Node.TemplateText ) lastNode ).rtrim();
                        }
                    }
                }
            }
            reader.skipSpaces();
        } while ( reader.matches ( "<jsp:attribute" ) );
    }
    private String getAttributeBodyType ( Node n, String name ) {
        if ( n instanceof Node.CustomTag ) {
            TagInfo tagInfo = ( ( Node.CustomTag ) n ).getTagInfo();
            TagAttributeInfo[] tldAttrs = tagInfo.getAttributes();
            for ( int i = 0; i < tldAttrs.length; i++ ) {
                if ( name.equals ( tldAttrs[i].getName() ) ) {
                    if ( tldAttrs[i].isFragment() ) {
                        return TagInfo.BODY_CONTENT_SCRIPTLESS;
                    }
                    if ( tldAttrs[i].canBeRequestTime() ) {
                        return TagInfo.BODY_CONTENT_JSP;
                    }
                }
            }
            if ( tagInfo.hasDynamicAttributes() ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.IncludeAction ) {
            if ( "page".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.ForwardAction ) {
            if ( "page".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.SetProperty ) {
            if ( "value".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.UseBean ) {
            if ( "beanName".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.PlugIn ) {
            if ( "width".equals ( name ) || "height".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.ParamAction ) {
            if ( "value".equals ( name ) ) {
                return TagInfo.BODY_CONTENT_JSP;
            }
        } else if ( n instanceof Node.JspElement ) {
            return TagInfo.BODY_CONTENT_JSP;
        }
        return JAVAX_BODY_CONTENT_TEMPLATE_TEXT;
    }
    private void parseFileDirectives ( Node parent ) throws JasperException {
        reader.skipUntil ( "<" );
        while ( reader.hasMoreInput() ) {
            start = reader.mark();
            if ( reader.matches ( "%--" ) ) {
                reader.skipUntil ( "--%>" );
            } else if ( reader.matches ( "%@" ) ) {
                parseDirective ( parent );
            } else if ( reader.matches ( "jsp:directive." ) ) {
                parseXMLDirective ( parent );
            } else if ( reader.matches ( "%!" ) ) {
                reader.skipUntil ( "%>" );
            } else if ( reader.matches ( "%=" ) ) {
                reader.skipUntil ( "%>" );
            } else if ( reader.matches ( "%" ) ) {
                reader.skipUntil ( "%>" );
            }
            reader.skipUntil ( "<" );
        }
    }
}
