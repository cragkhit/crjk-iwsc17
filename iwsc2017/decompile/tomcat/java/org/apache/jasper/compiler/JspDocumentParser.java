package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.jasper.Constants;
import java.io.FileNotFoundException;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import java.io.CharArrayWriter;
import org.xml.sax.SAXException;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.util.Collection;
import org.apache.jasper.JasperException;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParser;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.LocalResolver;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.Locator;
import org.apache.jasper.JspCompilationContext;
import org.xml.sax.ext.DefaultHandler2;
class JspDocumentParser extends DefaultHandler2 implements TagConstants {
    private static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";
    private static final String JSP_URI = "http://java.sun.com/JSP/Page";
    private final ParserController parserController;
    private final JspCompilationContext ctxt;
    private final PageInfo pageInfo;
    private final String path;
    private StringBuilder charBuffer;
    private Node current;
    private Node scriptlessBodyNode;
    private Locator locator;
    private Mark startMark;
    private boolean inDTD;
    private boolean isValidating;
    private final EntityResolver2 entityResolver;
    private final ErrorDispatcher err;
    private final boolean isTagFile;
    private final boolean directivesOnly;
    private boolean isTop;
    private int tagDependentNesting;
    private boolean tagDependentPending;
    public JspDocumentParser ( final ParserController pc, final String path, final boolean isTagFile, final boolean directivesOnly ) {
        this.tagDependentNesting = 0;
        this.tagDependentPending = false;
        this.parserController = pc;
        this.ctxt = pc.getJspCompilationContext();
        this.pageInfo = pc.getCompiler().getPageInfo();
        this.err = pc.getCompiler().getErrorDispatcher();
        this.path = path;
        this.isTagFile = isTagFile;
        this.directivesOnly = directivesOnly;
        this.isTop = true;
        final String blockExternalString = this.ctxt.getServletContext().getInitParameter ( "org.apache.jasper.XML_BLOCK_EXTERNAL" );
        final boolean blockExternal = blockExternalString == null || Boolean.parseBoolean ( blockExternalString );
        this.entityResolver = new LocalResolver ( DigesterFactory.SERVLET_API_PUBLIC_IDS, DigesterFactory.SERVLET_API_SYSTEM_IDS, blockExternal );
    }
    public static Node.Nodes parse ( final ParserController pc, final String path, final Jar jar, final Node parent, final boolean isTagFile, final boolean directivesOnly, final String pageEnc, final String jspConfigPageEnc, final boolean isEncodingSpecifiedInProlog, final boolean isBomPresent ) throws JasperException {
        final JspDocumentParser jspDocParser = new JspDocumentParser ( pc, path, isTagFile, directivesOnly );
        Node.Nodes pageNodes = null;
        try {
            final Node.Root dummyRoot = new Node.Root ( null, parent, true );
            dummyRoot.setPageEncoding ( pageEnc );
            dummyRoot.setJspConfigPageEncoding ( jspConfigPageEnc );
            dummyRoot.setIsEncodingSpecifiedInProlog ( isEncodingSpecifiedInProlog );
            dummyRoot.setIsBomPresent ( isBomPresent );
            jspDocParser.current = dummyRoot;
            if ( parent == null ) {
                jspDocParser.addInclude ( dummyRoot, jspDocParser.pageInfo.getIncludePrelude() );
            } else {
                jspDocParser.isTop = false;
            }
            jspDocParser.isValidating = false;
            SAXParser saxParser = getSAXParser ( false, jspDocParser );
            InputSource source = JspUtil.getInputSource ( path, jar, jspDocParser.ctxt );
            try {
                saxParser.parse ( source, jspDocParser );
            } catch ( EnableDTDValidationException e3 ) {
                saxParser = getSAXParser ( true, jspDocParser );
                jspDocParser.isValidating = true;
                try {
                    source.getByteStream().close();
                } catch ( IOException ex ) {}
                source = JspUtil.getInputSource ( path, jar, jspDocParser.ctxt );
                saxParser.parse ( source, jspDocParser );
            } finally {
                try {
                    source.getByteStream().close();
                } catch ( IOException ex2 ) {}
            }
            if ( parent == null ) {
                jspDocParser.addInclude ( dummyRoot, jspDocParser.pageInfo.getIncludeCoda() );
            }
            pageNodes = new Node.Nodes ( dummyRoot );
        } catch ( IOException ioe ) {
            jspDocParser.err.jspError ( ioe, "jsp.error.data.file.read", path );
        } catch ( SAXParseException e ) {
            jspDocParser.err.jspError ( new Mark ( jspDocParser.ctxt, path, e.getLineNumber(), e.getColumnNumber() ), e, e.getMessage(), new String[0] );
        } catch ( Exception e2 ) {
            jspDocParser.err.jspError ( e2, "jsp.error.data.file.processing", path );
        }
        return pageNodes;
    }
    private void addInclude ( final Node parent, final Collection<String> files ) throws SAXException {
        if ( files != null ) {
            for ( final String file : files ) {
                final AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute ( "", "file", "file", "CDATA", file );
                final Node includeDir = new Node.IncludeDirective ( attrs, null, parent );
                this.processIncludeDirective ( file, includeDir );
            }
        }
    }
    @Override
    public InputSource getExternalSubset ( final String name, final String baseURI ) throws SAXException, IOException {
        return this.entityResolver.getExternalSubset ( name, baseURI );
    }
    @Override
    public InputSource resolveEntity ( final String publicId, final String systemId ) throws SAXException, IOException {
        return this.entityResolver.resolveEntity ( publicId, systemId );
    }
    @Override
    public InputSource resolveEntity ( final String name, final String publicId, final String baseURI, final String systemId ) throws SAXException, IOException {
        return this.entityResolver.resolveEntity ( name, publicId, baseURI, systemId );
    }
    @Override
    public void startElement ( final String uri, final String localName, final String qName, final Attributes attrs ) throws SAXException {
        AttributesImpl taglibAttrs = null;
        AttributesImpl nonTaglibAttrs = null;
        AttributesImpl nonTaglibXmlnsAttrs = null;
        this.processChars();
        this.checkPrefixes ( uri, qName, attrs );
        if ( this.directivesOnly && ( !"http://java.sun.com/JSP/Page".equals ( uri ) || !localName.startsWith ( "directive." ) ) ) {
            return;
        }
        if ( this.current instanceof Node.JspText ) {
            throw new SAXParseException ( Localizer.getMessage ( "jsp.error.text.has_subelement" ), this.locator );
        }
        this.startMark = new Mark ( this.ctxt, this.path, this.locator.getLineNumber(), this.locator.getColumnNumber() );
        boolean isTaglib = false;
        for ( int i = attrs.getLength() - 1; i >= 0; --i ) {
            isTaglib = false;
            final String attrQName = attrs.getQName ( i );
            if ( !attrQName.startsWith ( "xmlns" ) ) {
                if ( nonTaglibAttrs == null ) {
                    nonTaglibAttrs = new AttributesImpl();
                }
                nonTaglibAttrs.addAttribute ( attrs.getURI ( i ), attrs.getLocalName ( i ), attrs.getQName ( i ), attrs.getType ( i ), attrs.getValue ( i ) );
            } else {
                if ( attrQName.startsWith ( "xmlns:jsp" ) ) {
                    isTaglib = true;
                } else {
                    final String attrUri = attrs.getValue ( i );
                    isTaglib = this.pageInfo.hasTaglib ( attrUri );
                }
                if ( isTaglib ) {
                    if ( taglibAttrs == null ) {
                        taglibAttrs = new AttributesImpl();
                    }
                    taglibAttrs.addAttribute ( attrs.getURI ( i ), attrs.getLocalName ( i ), attrs.getQName ( i ), attrs.getType ( i ), attrs.getValue ( i ) );
                } else {
                    if ( nonTaglibXmlnsAttrs == null ) {
                        nonTaglibXmlnsAttrs = new AttributesImpl();
                    }
                    nonTaglibXmlnsAttrs.addAttribute ( attrs.getURI ( i ), attrs.getLocalName ( i ), attrs.getQName ( i ), attrs.getType ( i ), attrs.getValue ( i ) );
                }
            }
        }
        Node node = null;
        if ( this.tagDependentPending && "http://java.sun.com/JSP/Page".equals ( uri ) && localName.equals ( "body" ) ) {
            this.tagDependentPending = false;
            ++this.tagDependentNesting;
            this.current = this.parseStandardAction ( qName, localName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark );
            return;
        }
        if ( this.tagDependentPending && "http://java.sun.com/JSP/Page".equals ( uri ) && localName.equals ( "attribute" ) ) {
            this.current = this.parseStandardAction ( qName, localName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark );
            return;
        }
        if ( this.tagDependentPending ) {
            this.tagDependentPending = false;
            ++this.tagDependentNesting;
        }
        if ( this.tagDependentNesting > 0 ) {
            node = new Node.UninterpretedTag ( qName, localName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark, this.current );
        } else if ( "http://java.sun.com/JSP/Page".equals ( uri ) ) {
            node = this.parseStandardAction ( qName, localName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark );
        } else {
            node = this.parseCustomAction ( qName, localName, uri, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark, this.current );
            if ( node == null ) {
                node = new Node.UninterpretedTag ( qName, localName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, this.startMark, this.current );
            } else {
                final String bodyType = getBodyType ( ( Node.CustomTag ) node );
                if ( this.scriptlessBodyNode == null && bodyType.equalsIgnoreCase ( "scriptless" ) ) {
                    this.scriptlessBodyNode = node;
                } else if ( "tagdependent".equalsIgnoreCase ( bodyType ) ) {
                    this.tagDependentPending = true;
                }
            }
        }
        this.current = node;
    }
    @Override
    public void characters ( final char[] buf, final int offset, final int len ) {
        if ( this.charBuffer == null ) {
            this.charBuffer = new StringBuilder();
        }
        this.charBuffer.append ( buf, offset, len );
    }
    private void processChars() throws SAXException {
        if ( this.charBuffer == null || this.directivesOnly ) {
            return;
        }
        boolean isAllSpace = true;
        if ( ! ( this.current instanceof Node.JspText ) && ! ( this.current instanceof Node.NamedAttribute ) ) {
            for ( int i = 0; i < this.charBuffer.length(); ++i ) {
                final char ch = this.charBuffer.charAt ( i );
                if ( ch != ' ' && ch != '\n' && ch != '\r' && ch != '\t' ) {
                    isAllSpace = false;
                    break;
                }
            }
        }
        if ( !isAllSpace && this.tagDependentPending ) {
            this.tagDependentPending = false;
            ++this.tagDependentNesting;
        }
        if ( this.tagDependentNesting > 0 || this.pageInfo.isELIgnored() || this.current instanceof Node.ScriptingElement ) {
            if ( this.charBuffer.length() > 0 ) {
                final Node.TemplateText templateText = new Node.TemplateText ( this.charBuffer.toString(), this.startMark, this.current );
            }
            this.startMark = new Mark ( this.ctxt, this.path, this.locator.getLineNumber(), this.locator.getColumnNumber() );
            this.charBuffer = null;
            return;
        }
        if ( this.current instanceof Node.JspText || this.current instanceof Node.NamedAttribute || !isAllSpace ) {
            int line = this.startMark.getLineNumber();
            int column = this.startMark.getColumnNumber();
            final CharArrayWriter ttext = new CharArrayWriter();
            int lastCh = 0;
            int elType = 0;
            for ( int j = 0; j < this.charBuffer.length(); ++j ) {
                int ch2 = this.charBuffer.charAt ( j );
                if ( ch2 == 10 ) {
                    column = 1;
                    ++line;
                } else {
                    ++column;
                }
                Label_0755: {
                    if ( ( lastCh == 36 || lastCh == 35 ) && ch2 == 123 ) {
                        elType = lastCh;
                        if ( ttext.size() > 0 ) {
                            final Node unused = new Node.TemplateText ( ttext.toString(), this.startMark, this.current );
                            ttext.reset();
                            this.startMark = new Mark ( this.ctxt, this.path, line, column - 2 );
                        }
                        ++j;
                        boolean singleQ = false;
                        boolean doubleQ = false;
                        lastCh = 0;
                        while ( j < this.charBuffer.length() ) {
                            ch2 = this.charBuffer.charAt ( j );
                            if ( ch2 == 10 ) {
                                column = 1;
                                ++line;
                            } else {
                                ++column;
                            }
                            if ( lastCh == 92 && ( singleQ || doubleQ ) ) {
                                ttext.write ( ch2 );
                                lastCh = 0;
                            } else {
                                if ( ch2 == 125 ) {
                                    final Node unused2 = new Node.ELExpression ( ( char ) elType, ttext.toString(), this.startMark, this.current );
                                    ttext.reset();
                                    this.startMark = new Mark ( this.ctxt, this.path, line, column );
                                    break Label_0755;
                                }
                                if ( ch2 == 34 ) {
                                    doubleQ = !doubleQ;
                                } else if ( ch2 == 39 ) {
                                    singleQ = !singleQ;
                                }
                                ttext.write ( ch2 );
                                lastCh = ch2;
                            }
                            ++j;
                        }
                        throw new SAXParseException ( Localizer.getMessage ( "jsp.error.unterminated", ( char ) elType + "{" ), this.locator );
                    }
                    if ( lastCh == 92 && ( ch2 == 36 || ch2 == 35 ) ) {
                        if ( this.pageInfo.isELIgnored() ) {
                            ttext.write ( 92 );
                        }
                        ttext.write ( ch2 );
                        ch2 = 0;
                    } else {
                        if ( lastCh == 36 || lastCh == 35 || lastCh == 92 ) {
                            ttext.write ( lastCh );
                        }
                        if ( ch2 != 36 && ch2 != 35 && ch2 != 92 ) {
                            ttext.write ( ch2 );
                        }
                    }
                }
                lastCh = ch2;
            }
            if ( lastCh == 36 || lastCh == 35 || lastCh == 92 ) {
                ttext.write ( lastCh );
            }
            if ( ttext.size() > 0 ) {
                final Node.TemplateText templateText2 = new Node.TemplateText ( ttext.toString(), this.startMark, this.current );
            }
        }
        this.startMark = new Mark ( this.ctxt, this.path, this.locator.getLineNumber(), this.locator.getColumnNumber() );
        this.charBuffer = null;
    }
    @Override
    public void endElement ( final String uri, final String localName, final String qName ) throws SAXException {
        this.processChars();
        if ( this.directivesOnly && ( !"http://java.sun.com/JSP/Page".equals ( uri ) || !localName.startsWith ( "directive." ) ) ) {
            return;
        }
        if ( this.current instanceof Node.NamedAttribute ) {
            final boolean isTrim = ( ( Node.NamedAttribute ) this.current ).isTrim();
            final Node.Nodes subElems = ( ( Node.NamedAttribute ) this.current ).getBody();
            for ( int i = 0; subElems != null && i < subElems.size(); ++i ) {
                final Node subElem = subElems.getNode ( i );
                if ( subElem instanceof Node.TemplateText ) {
                    if ( i == 0 ) {
                        if ( isTrim ) {
                            ( ( Node.TemplateText ) subElem ).ltrim();
                        }
                    } else if ( i == subElems.size() - 1 ) {
                        if ( isTrim ) {
                            ( ( Node.TemplateText ) subElem ).rtrim();
                        }
                    } else if ( ( ( Node.TemplateText ) subElem ).isAllSpace() ) {
                        subElems.remove ( subElem );
                    }
                }
            }
        } else if ( this.current instanceof Node.ScriptingElement ) {
            this.checkScriptingBody ( ( Node.ScriptingElement ) this.current );
        }
        if ( this.isTagDependent ( this.current ) ) {
            --this.tagDependentNesting;
        }
        if ( this.scriptlessBodyNode != null && this.current.equals ( this.scriptlessBodyNode ) ) {
            this.scriptlessBodyNode = null;
        }
        if ( this.current instanceof Node.CustomTag ) {
            final String bodyType = getBodyType ( ( Node.CustomTag ) this.current );
            if ( "empty".equalsIgnoreCase ( bodyType ) ) {
                final Node.Nodes children = this.current.getBody();
                if ( children != null && children.size() > 0 ) {
                    for ( int i = 0; i < children.size(); ++i ) {
                        final Node child = children.getNode ( i );
                        if ( ! ( child instanceof Node.NamedAttribute ) ) {
                            throw new SAXParseException ( Localizer.getMessage ( "jasper.error.emptybodycontent.nonempty", this.current.qName ), this.locator );
                        }
                    }
                }
            }
        }
        if ( this.current.getParent() != null ) {
            this.current = this.current.getParent();
        }
    }
    @Override
    public void setDocumentLocator ( final Locator locator ) {
        this.locator = locator;
    }
    @Override
    public void comment ( final char[] buf, final int offset, final int len ) throws SAXException {
        this.processChars();
        if ( !this.inDTD ) {
            this.startMark = new Mark ( this.ctxt, this.path, this.locator.getLineNumber(), this.locator.getColumnNumber() );
            final Node.Comment comment = new Node.Comment ( new String ( buf, offset, len ), this.startMark, this.current );
        }
    }
    @Override
    public void startCDATA() throws SAXException {
        this.processChars();
        this.startMark = new Mark ( this.ctxt, this.path, this.locator.getLineNumber(), this.locator.getColumnNumber() );
    }
    @Override
    public void endCDATA() throws SAXException {
        this.processChars();
    }
    @Override
    public void startEntity ( final String name ) throws SAXException {
    }
    @Override
    public void endEntity ( final String name ) throws SAXException {
    }
    @Override
    public void startDTD ( final String name, final String publicId, final String systemId ) throws SAXException {
        if ( !this.isValidating ) {
            this.fatalError ( new EnableDTDValidationException ( "jsp.error.enable_dtd_validation", ( Locator ) null ) );
        }
        this.inDTD = true;
    }
    @Override
    public void endDTD() throws SAXException {
        this.inDTD = false;
    }
    @Override
    public void fatalError ( final SAXParseException e ) throws SAXException {
        throw e;
    }
    @Override
    public void error ( final SAXParseException e ) throws SAXException {
        throw e;
    }
    @Override
    public void startPrefixMapping ( final String prefix, final String uri ) throws SAXException {
        if ( this.directivesOnly && !"http://java.sun.com/JSP/Page".equals ( uri ) ) {
            return;
        }
        TagLibraryInfo taglibInfo;
        try {
            taglibInfo = this.getTaglibInfo ( prefix, uri );
        } catch ( JasperException je ) {
            throw new SAXParseException ( Localizer.getMessage ( "jsp.error.could.not.add.taglibraries" ), this.locator, ( Exception ) je );
        }
        if ( taglibInfo != null ) {
            if ( this.pageInfo.getTaglib ( uri ) == null ) {
                this.pageInfo.addTaglib ( uri, taglibInfo );
            }
            this.pageInfo.pushPrefixMapping ( prefix, uri );
        } else {
            this.pageInfo.pushPrefixMapping ( prefix, null );
        }
    }
    @Override
    public void endPrefixMapping ( final String prefix ) throws SAXException {
        if ( this.directivesOnly ) {
            final String uri = this.pageInfo.getURI ( prefix );
            if ( !"http://java.sun.com/JSP/Page".equals ( uri ) ) {
                return;
            }
        }
        this.pageInfo.popPrefixMapping ( prefix );
    }
    private Node parseStandardAction ( final String qName, final String localName, final Attributes nonTaglibAttrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start ) throws SAXException {
        Node node = null;
        if ( localName.equals ( "root" ) ) {
            if ( ! ( this.current instanceof Node.Root ) ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.nested_jsproot" ), this.locator );
            }
            node = new Node.JspRoot ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
            if ( this.isTop ) {
                this.pageInfo.setHasJspRoot ( true );
            }
        } else if ( localName.equals ( "directive.page" ) ) {
            if ( this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.istagfile", localName ), this.locator );
            }
            node = new Node.PageDirective ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
            final String imports = nonTaglibAttrs.getValue ( "import" );
            if ( imports != null ) {
                ( ( Node.PageDirective ) node ).addImport ( imports );
            }
        } else if ( localName.equals ( "directive.include" ) ) {
            node = new Node.IncludeDirective ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
            this.processIncludeDirective ( nonTaglibAttrs.getValue ( "file" ), node );
        } else if ( localName.equals ( "declaration" ) ) {
            if ( this.scriptlessBodyNode != null ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.no.scriptlets", localName ), this.locator );
            }
            node = new Node.Declaration ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "scriptlet" ) ) {
            if ( this.scriptlessBodyNode != null ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.no.scriptlets", localName ), this.locator );
            }
            node = new Node.Scriptlet ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "expression" ) ) {
            if ( this.scriptlessBodyNode != null ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.no.scriptlets", localName ), this.locator );
            }
            node = new Node.Expression ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "useBean" ) ) {
            node = new Node.UseBean ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "setProperty" ) ) {
            node = new Node.SetProperty ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "getProperty" ) ) {
            node = new Node.GetProperty ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "include" ) ) {
            node = new Node.IncludeAction ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "forward" ) ) {
            node = new Node.ForwardAction ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "param" ) ) {
            node = new Node.ParamAction ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "params" ) ) {
            node = new Node.ParamsAction ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "plugin" ) ) {
            node = new Node.PlugIn ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "text" ) ) {
            node = new Node.JspText ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "body" ) ) {
            node = new Node.JspBody ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "attribute" ) ) {
            node = new Node.NamedAttribute ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "output" ) ) {
            node = new Node.JspOutput ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "directive.tag" ) ) {
            if ( !this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.isnottagfile", localName ), this.locator );
            }
            node = new Node.TagDirective ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
            final String imports = nonTaglibAttrs.getValue ( "import" );
            if ( imports != null ) {
                ( ( Node.TagDirective ) node ).addImport ( imports );
            }
        } else if ( localName.equals ( "directive.attribute" ) ) {
            if ( !this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.isnottagfile", localName ), this.locator );
            }
            node = new Node.AttributeDirective ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "directive.variable" ) ) {
            if ( !this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.isnottagfile", localName ), this.locator );
            }
            node = new Node.VariableDirective ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "invoke" ) ) {
            if ( !this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.isnottagfile", localName ), this.locator );
            }
            node = new Node.InvokeAction ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "doBody" ) ) {
            if ( !this.isTagFile ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.action.isnottagfile", localName ), this.locator );
            }
            node = new Node.DoBodyAction ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else if ( localName.equals ( "element" ) ) {
            node = new Node.JspElement ( qName, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        } else {
            if ( !localName.equals ( "fallback" ) ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.xml.badStandardAction", localName ), this.locator );
            }
            node = new Node.FallBackAction ( qName, nonTaglibXmlnsAttrs, taglibAttrs, start, this.current );
        }
        return node;
    }
    private Node parseCustomAction ( final String qName, final String localName, final String uri, final Attributes nonTaglibAttrs, final Attributes nonTaglibXmlnsAttrs, final Attributes taglibAttrs, final Mark start, final Node parent ) throws SAXException {
        final TagLibraryInfo tagLibInfo = this.pageInfo.getTaglib ( uri );
        if ( tagLibInfo == null ) {
            return null;
        }
        final TagInfo tagInfo = tagLibInfo.getTag ( localName );
        final TagFileInfo tagFileInfo = tagLibInfo.getTagFile ( localName );
        if ( tagInfo == null && tagFileInfo == null ) {
            throw new SAXParseException ( Localizer.getMessage ( "jsp.error.xml.bad_tag", localName, uri ), this.locator );
        }
        Class<?> tagHandlerClass = null;
        if ( tagInfo != null ) {
            final String handlerClassName = tagInfo.getTagClassName();
            try {
                tagHandlerClass = this.ctxt.getClassLoader().loadClass ( handlerClassName );
            } catch ( Exception e ) {
                throw new SAXParseException ( Localizer.getMessage ( "jsp.error.loadclass.taghandler", handlerClassName, qName ), this.locator, e );
            }
        }
        final String prefix = this.getPrefix ( qName );
        Node.CustomTag ret = null;
        if ( tagInfo != null ) {
            ret = new Node.CustomTag ( qName, prefix, localName, uri, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent, tagInfo, tagHandlerClass );
        } else {
            ret = new Node.CustomTag ( qName, prefix, localName, uri, nonTaglibAttrs, nonTaglibXmlnsAttrs, taglibAttrs, start, parent, tagFileInfo );
        }
        return ret;
    }
    private TagLibraryInfo getTaglibInfo ( final String prefix, String uri ) throws JasperException {
        TagLibraryInfo result = null;
        if ( uri.startsWith ( "urn:jsptagdir:" ) ) {
            final String tagdir = uri.substring ( "urn:jsptagdir:".length() );
            result = new ImplicitTagLibraryInfo ( this.ctxt, this.parserController, this.pageInfo, prefix, tagdir, this.err );
        } else {
            boolean isPlainUri = false;
            if ( uri.startsWith ( "urn:jsptld:" ) ) {
                uri = uri.substring ( "urn:jsptld:".length() );
            } else {
                isPlainUri = true;
            }
            final TldResourcePath tldResourcePath = this.ctxt.getTldResourcePath ( uri );
            if ( tldResourcePath != null || !isPlainUri ) {
                if ( this.ctxt.getOptions().isCaching() ) {
                    result = this.ctxt.getOptions().getCache().get ( uri );
                }
                if ( result == null ) {
                    result = new TagLibraryInfoImpl ( this.ctxt, this.parserController, this.pageInfo, prefix, uri, tldResourcePath, this.err );
                    if ( this.ctxt.getOptions().isCaching() ) {
                        this.ctxt.getOptions().getCache().put ( uri, result );
                    }
                }
            }
        }
        return result;
    }
    private void checkScriptingBody ( final Node.ScriptingElement scriptingElem ) throws SAXException {
        final Node.Nodes body = scriptingElem.getBody();
        if ( body != null ) {
            for ( int size = body.size(), i = 0; i < size; ++i ) {
                final Node n = body.getNode ( i );
                if ( ! ( n instanceof Node.TemplateText ) ) {
                    String elemType = "scriptlet";
                    if ( scriptingElem instanceof Node.Declaration ) {
                        elemType = "declaration";
                    }
                    if ( scriptingElem instanceof Node.Expression ) {
                        elemType = "expression";
                    }
                    final String msg = Localizer.getMessage ( "jsp.error.parse.xml.scripting.invalid.body", elemType );
                    throw new SAXParseException ( msg, this.locator );
                }
            }
        }
    }
    private void processIncludeDirective ( final String fname, final Node parent ) throws SAXException {
        if ( fname == null ) {
            return;
        }
        try {
            this.parserController.parse ( fname, parent, null );
        } catch ( FileNotFoundException fnfe ) {
            throw new SAXParseException ( Localizer.getMessage ( "jsp.error.file.not.found", fname ), this.locator, fnfe );
        } catch ( Exception e ) {
            throw new SAXParseException ( e.getMessage(), this.locator, e );
        }
    }
    private void checkPrefixes ( final String uri, final String qName, final Attributes attrs ) {
        this.checkPrefix ( uri, qName );
        for ( int len = attrs.getLength(), i = 0; i < len; ++i ) {
            this.checkPrefix ( attrs.getURI ( i ), attrs.getQName ( i ) );
        }
    }
    private void checkPrefix ( final String uri, final String qName ) {
        final String prefix = this.getPrefix ( qName );
        if ( prefix.length() > 0 ) {
            this.pageInfo.addPrefix ( prefix );
            if ( "jsp".equals ( prefix ) && !"http://java.sun.com/JSP/Page".equals ( uri ) ) {
                this.pageInfo.setIsJspPrefixHijacked ( true );
            }
        }
    }
    private String getPrefix ( final String qName ) {
        final int index = qName.indexOf ( 58 );
        if ( index != -1 ) {
            return qName.substring ( 0, index );
        }
        return "";
    }
    private static SAXParser getSAXParser ( final boolean validating, final JspDocumentParser jspDocParser ) throws Exception {
        ClassLoader original;
        if ( Constants.IS_SECURITY_ENABLED ) {
            final PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) pa );
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        try {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa2 = new PrivilegedSetTccl ( JspDocumentParser.class.getClassLoader() );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa2 );
            } else {
                Thread.currentThread().setContextClassLoader ( JspDocumentParser.class.getClassLoader() );
            }
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware ( true );
            factory.setFeature ( "http://xml.org/sax/features/namespace-prefixes", true );
            factory.setValidating ( validating );
            if ( validating ) {
                factory.setFeature ( "http://xml.org/sax/features/validation", true );
                factory.setFeature ( "http://apache.org/xml/features/validation/schema", true );
            }
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setProperty ( "http://xml.org/sax/properties/lexical-handler", jspDocParser );
            xmlReader.setErrorHandler ( jspDocParser );
            return saxParser;
        } finally {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa3 = new PrivilegedSetTccl ( original );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa3 );
            } else {
                Thread.currentThread().setContextClassLoader ( original );
            }
        }
    }
    private static String getBodyType ( final Node.CustomTag custom ) {
        if ( custom.getTagInfo() != null ) {
            return custom.getTagInfo().getBodyContent();
        }
        return custom.getTagFileInfo().getTagInfo().getBodyContent();
    }
    private boolean isTagDependent ( final Node n ) {
        if ( n instanceof Node.CustomTag ) {
            final String bodyType = getBodyType ( ( Node.CustomTag ) n );
            return "tagdependent".equalsIgnoreCase ( bodyType );
        }
        return false;
    }
    private static class EnableDTDValidationException extends SAXParseException {
        private static final long serialVersionUID = 1L;
        EnableDTDValidationException ( final String message, final Locator loc ) {
            super ( message, loc );
        }
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
