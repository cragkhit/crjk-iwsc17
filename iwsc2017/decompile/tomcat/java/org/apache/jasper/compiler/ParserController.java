package org.apache.jasper.compiler;
import org.xml.sax.Attributes;
import org.apache.jasper.xmlparser.XMLEncodingDetector;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.apache.tomcat.Jar;
import java.io.IOException;
import org.apache.jasper.JasperException;
import java.util.Stack;
import org.apache.jasper.JspCompilationContext;
class ParserController implements TagConstants {
    private static final String CHARSET = "charset=";
    private final JspCompilationContext ctxt;
    private final Compiler compiler;
    private final ErrorDispatcher err;
    private boolean isXml;
    private final Stack<String> baseDirStack;
    private boolean isEncodingSpecifiedInProlog;
    private boolean isBomPresent;
    private int skip;
    private String sourceEnc;
    private boolean isDefaultPageEncoding;
    private boolean isTagFile;
    private boolean directiveOnly;
    public ParserController ( final JspCompilationContext ctxt, final Compiler compiler ) {
        this.baseDirStack = new Stack<String>();
        this.ctxt = ctxt;
        this.compiler = compiler;
        this.err = compiler.getErrorDispatcher();
    }
    public JspCompilationContext getJspCompilationContext() {
        return this.ctxt;
    }
    public Compiler getCompiler() {
        return this.compiler;
    }
    public Node.Nodes parse ( final String inFileName ) throws JasperException, IOException {
        this.isTagFile = this.ctxt.isTagFile();
        this.directiveOnly = false;
        return this.doParse ( inFileName, null, this.ctxt.getTagFileJar() );
    }
    public Node.Nodes parseDirectives ( final String inFileName ) throws JasperException, IOException {
        this.isTagFile = this.ctxt.isTagFile();
        this.directiveOnly = true;
        return this.doParse ( inFileName, null, this.ctxt.getTagFileJar() );
    }
    public Node.Nodes parse ( final String inFileName, final Node parent, final Jar jar ) throws JasperException, IOException {
        return this.doParse ( inFileName, parent, jar );
    }
    public Node.Nodes parseTagFileDirectives ( final String inFileName, final Jar jar ) throws JasperException, IOException {
        final boolean isTagFileSave = this.isTagFile;
        final boolean directiveOnlySave = this.directiveOnly;
        this.isTagFile = true;
        this.directiveOnly = true;
        final Node.Nodes page = this.doParse ( inFileName, null, jar );
        this.directiveOnly = directiveOnlySave;
        this.isTagFile = isTagFileSave;
        return page;
    }
    private Node.Nodes doParse ( final String inFileName, final Node parent, final Jar jar ) throws FileNotFoundException, JasperException, IOException {
        Node.Nodes parsedPage = null;
        this.isEncodingSpecifiedInProlog = false;
        this.isBomPresent = false;
        this.isDefaultPageEncoding = false;
        final String absFileName = this.resolveFileName ( inFileName );
        final String jspConfigPageEnc = this.getJspConfigPageEncoding ( absFileName );
        this.determineSyntaxAndEncoding ( absFileName, jar, jspConfigPageEnc );
        if ( parent != null ) {
            if ( jar == null ) {
                this.compiler.getPageInfo().addDependant ( absFileName, this.ctxt.getLastModified ( absFileName ) );
            } else {
                final String entry = absFileName.substring ( 1 );
                this.compiler.getPageInfo().addDependant ( jar.getURL ( entry ), jar.getLastModified ( entry ) );
            }
        }
        if ( ( ( this.isXml && this.isEncodingSpecifiedInProlog ) || this.isBomPresent ) && jspConfigPageEnc != null && !jspConfigPageEnc.equals ( this.sourceEnc ) && ( !jspConfigPageEnc.startsWith ( "UTF-16" ) || !this.sourceEnc.startsWith ( "UTF-16" ) ) ) {
            this.err.jspError ( "jsp.error.prolog_config_encoding_mismatch", this.sourceEnc, jspConfigPageEnc );
        }
        if ( this.isXml ) {
            parsedPage = JspDocumentParser.parse ( this, absFileName, jar, parent, this.isTagFile, this.directiveOnly, this.sourceEnc, jspConfigPageEnc, this.isEncodingSpecifiedInProlog, this.isBomPresent );
        } else {
            try ( final InputStreamReader inStreamReader = JspUtil.getReader ( absFileName, this.sourceEnc, jar, this.ctxt, this.err, this.skip ) ) {
                final JspReader jspReader = new JspReader ( this.ctxt, absFileName, inStreamReader, this.err );
                parsedPage = Parser.parse ( this, jspReader, parent, this.isTagFile, this.directiveOnly, jar, this.sourceEnc, jspConfigPageEnc, this.isDefaultPageEncoding, this.isBomPresent );
            }
        }
        this.baseDirStack.pop();
        return parsedPage;
    }
    private String getJspConfigPageEncoding ( final String absFileName ) {
        final JspConfig jspConfig = this.ctxt.getOptions().getJspConfig();
        final JspConfig.JspProperty jspProperty = jspConfig.findJspProperty ( absFileName );
        return jspProperty.getPageEncoding();
    }
    private void determineSyntaxAndEncoding ( final String absFileName, final Jar jar, final String jspConfigPageEnc ) throws JasperException, IOException {
        this.isXml = false;
        boolean isExternal = false;
        boolean revert = false;
        final JspConfig jspConfig = this.ctxt.getOptions().getJspConfig();
        final JspConfig.JspProperty jspProperty = jspConfig.findJspProperty ( absFileName );
        if ( jspProperty.isXml() != null ) {
            this.isXml = JspUtil.booleanValue ( jspProperty.isXml() );
            isExternal = true;
        } else if ( absFileName.endsWith ( ".jspx" ) || absFileName.endsWith ( ".tagx" ) ) {
            this.isXml = true;
            isExternal = true;
        }
        if ( isExternal && !this.isXml ) {
            this.sourceEnc = jspConfigPageEnc;
            if ( this.sourceEnc != null ) {
                return;
            }
            this.sourceEnc = "ISO-8859-1";
        } else {
            final Object[] ret = XMLEncodingDetector.getEncoding ( absFileName, jar, this.ctxt, this.err );
            this.sourceEnc = ( String ) ret[0];
            if ( ret[1] ) {
                this.isEncodingSpecifiedInProlog = true;
            }
            if ( ret[2] ) {
                this.isBomPresent = true;
            }
            this.skip = ( int ) ret[3];
            if ( !this.isXml && this.sourceEnc.equals ( "UTF-8" ) ) {
                this.sourceEnc = "ISO-8859-1";
                revert = true;
            }
        }
        if ( this.isXml ) {
            return;
        }
        JspReader jspReader = null;
        try {
            jspReader = new JspReader ( this.ctxt, absFileName, this.sourceEnc, jar, this.err );
        } catch ( FileNotFoundException ex ) {
            throw new JasperException ( ex );
        }
        final Mark startMark = jspReader.mark();
        if ( !isExternal ) {
            jspReader.reset ( startMark );
            if ( this.hasJspRoot ( jspReader ) ) {
                if ( revert ) {
                    this.sourceEnc = "UTF-8";
                }
                this.isXml = true;
                return;
            }
            if ( revert && this.isBomPresent ) {
                this.sourceEnc = "UTF-8";
            }
            this.isXml = false;
        }
        if ( !this.isBomPresent ) {
            this.sourceEnc = jspConfigPageEnc;
            if ( this.sourceEnc == null ) {
                this.sourceEnc = this.getPageEncodingForJspSyntax ( jspReader, startMark );
                if ( this.sourceEnc == null ) {
                    this.sourceEnc = "ISO-8859-1";
                    this.isDefaultPageEncoding = true;
                }
            }
        }
    }
    private String getPageEncodingForJspSyntax ( final JspReader jspReader, final Mark startMark ) throws JasperException {
        String encoding = null;
        String saveEncoding = null;
        jspReader.reset ( startMark );
        while ( true ) {
            while ( jspReader.skipUntil ( "<" ) != null ) {
                if ( jspReader.matches ( "%--" ) ) {
                    if ( jspReader.skipUntil ( "--%>" ) != null ) {
                        continue;
                    }
                } else {
                    boolean isDirective = jspReader.matches ( "%@" );
                    if ( isDirective ) {
                        jspReader.skipSpaces();
                    } else {
                        isDirective = jspReader.matches ( "jsp:directive." );
                    }
                    if ( !isDirective ) {
                        continue;
                    }
                    if ( !jspReader.matches ( "tag " ) && !jspReader.matches ( "page" ) ) {
                        continue;
                    }
                    jspReader.skipSpaces();
                    final Attributes attrs = Parser.parseAttributes ( this, jspReader );
                    encoding = this.getPageEncodingFromDirective ( attrs, "pageEncoding" );
                    if ( encoding != null ) {
                        break;
                    }
                    encoding = this.getPageEncodingFromDirective ( attrs, "contentType" );
                    if ( encoding == null ) {
                        continue;
                    }
                    saveEncoding = encoding;
                    continue;
                }
                if ( encoding == null ) {
                    encoding = saveEncoding;
                }
                return encoding;
            }
            continue;
        }
    }
    private String getPageEncodingFromDirective ( final Attributes attrs, final String attrName ) {
        final String value = attrs.getValue ( attrName );
        if ( attrName.equals ( "pageEncoding" ) ) {
            return value;
        }
        final String contentType = value;
        String encoding = null;
        if ( contentType != null ) {
            final int loc = contentType.indexOf ( "charset=" );
            if ( loc != -1 ) {
                encoding = contentType.substring ( loc + "charset=".length() );
            }
        }
        return encoding;
    }
    private String resolveFileName ( final String inFileName ) {
        String fileName = inFileName.replace ( '\\', '/' );
        final boolean isAbsolute = fileName.startsWith ( "/" );
        fileName = ( isAbsolute ? fileName : ( this.baseDirStack.peek() + fileName ) );
        final String baseDir = fileName.substring ( 0, fileName.lastIndexOf ( 47 ) + 1 );
        this.baseDirStack.push ( baseDir );
        return fileName;
    }
    private boolean hasJspRoot ( final JspReader reader ) {
        Mark start = null;
        while ( ( start = reader.skipUntil ( "<" ) ) != null ) {
            final int c = reader.nextChar();
            if ( c != 33 && c != 63 ) {
                break;
            }
        }
        if ( start == null ) {
            return false;
        }
        Mark stop = reader.skipUntil ( ":root" );
        if ( stop == null ) {
            return false;
        }
        final String prefix = reader.getText ( start, stop ).substring ( 1 );
        start = stop;
        stop = reader.skipUntil ( ">" );
        if ( stop == null ) {
            return false;
        }
        final String root = reader.getText ( start, stop );
        final String xmlnsDecl = "xmlns:" + prefix;
        int index = root.indexOf ( xmlnsDecl );
        if ( index == -1 ) {
            return false;
        }
        for ( index += xmlnsDecl.length(); index < root.length() && Character.isWhitespace ( root.charAt ( index ) ); ++index ) {}
        if ( index < root.length() && root.charAt ( index ) == '=' ) {
            ++index;
            while ( index < root.length() && Character.isWhitespace ( root.charAt ( index ) ) ) {
                ++index;
            }
            if ( index < root.length() && ( root.charAt ( index ) == '\"' || root.charAt ( index ) == '\'' ) ) {
                ++index;
                if ( root.regionMatches ( index, "http://java.sun.com/JSP/Page", 0, "http://java.sun.com/JSP/Page".length() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
}
