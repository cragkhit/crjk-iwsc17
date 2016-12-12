package org.apache.jasper.compiler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.xmlparser.XMLEncodingDetector;
import org.apache.tomcat.Jar;
import org.xml.sax.Attributes;
class ParserController implements TagConstants {
    private static final String CHARSET = "charset=";
    private final JspCompilationContext ctxt;
    private final Compiler compiler;
    private final ErrorDispatcher err;
    private boolean isXml;
    private final Stack<String> baseDirStack = new Stack<>();
    private boolean isEncodingSpecifiedInProlog;
    private boolean isBomPresent;
    private int skip;
    private String sourceEnc;
    private boolean isDefaultPageEncoding;
    private boolean isTagFile;
    private boolean directiveOnly;
    public ParserController ( JspCompilationContext ctxt, Compiler compiler ) {
        this.ctxt = ctxt;
        this.compiler = compiler;
        this.err = compiler.getErrorDispatcher();
    }
    public JspCompilationContext getJspCompilationContext () {
        return ctxt;
    }
    public Compiler getCompiler () {
        return compiler;
    }
    public Node.Nodes parse ( String inFileName ) throws JasperException, IOException {
        isTagFile = ctxt.isTagFile();
        directiveOnly = false;
        return doParse ( inFileName, null, ctxt.getTagFileJar() );
    }
    public Node.Nodes parseDirectives ( String inFileName ) throws JasperException, IOException {
        isTagFile = ctxt.isTagFile();
        directiveOnly = true;
        return doParse ( inFileName, null, ctxt.getTagFileJar() );
    }
    public Node.Nodes parse ( String inFileName, Node parent, Jar jar )
    throws JasperException, IOException {
        return doParse ( inFileName, parent, jar );
    }
    public Node.Nodes parseTagFileDirectives ( String inFileName, Jar jar )
    throws JasperException, IOException {
        boolean isTagFileSave = isTagFile;
        boolean directiveOnlySave = directiveOnly;
        isTagFile = true;
        directiveOnly = true;
        Node.Nodes page = doParse ( inFileName, null, jar );
        directiveOnly = directiveOnlySave;
        isTagFile = isTagFileSave;
        return page;
    }
    private Node.Nodes doParse ( String inFileName, Node parent, Jar jar )
    throws FileNotFoundException, JasperException, IOException {
        Node.Nodes parsedPage = null;
        isEncodingSpecifiedInProlog = false;
        isBomPresent = false;
        isDefaultPageEncoding = false;
        String absFileName = resolveFileName ( inFileName );
        String jspConfigPageEnc = getJspConfigPageEncoding ( absFileName );
        determineSyntaxAndEncoding ( absFileName, jar, jspConfigPageEnc );
        if ( parent != null ) {
            if ( jar == null ) {
                compiler.getPageInfo().addDependant ( absFileName,
                                                      ctxt.getLastModified ( absFileName ) );
            } else {
                String entry = absFileName.substring ( 1 );
                compiler.getPageInfo().addDependant ( jar.getURL ( entry ),
                                                      Long.valueOf ( jar.getLastModified ( entry ) ) );
            }
        }
        if ( ( isXml && isEncodingSpecifiedInProlog ) || isBomPresent ) {
            if ( jspConfigPageEnc != null && !jspConfigPageEnc.equals ( sourceEnc )
                    && ( !jspConfigPageEnc.startsWith ( "UTF-16" )
                         || !sourceEnc.startsWith ( "UTF-16" ) ) ) {
                err.jspError ( "jsp.error.prolog_config_encoding_mismatch",
                               sourceEnc, jspConfigPageEnc );
            }
        }
        if ( isXml ) {
            parsedPage = JspDocumentParser.parse ( this, absFileName, jar, parent,
                                                   isTagFile, directiveOnly, sourceEnc, jspConfigPageEnc,
                                                   isEncodingSpecifiedInProlog, isBomPresent );
        } else {
            try ( InputStreamReader inStreamReader = JspUtil.getReader (
                            absFileName, sourceEnc, jar, ctxt, err, skip ); ) {
                JspReader jspReader = new JspReader ( ctxt, absFileName,
                                                      inStreamReader, err );
                parsedPage = Parser.parse ( this, jspReader, parent, isTagFile,
                                            directiveOnly, jar, sourceEnc, jspConfigPageEnc,
                                            isDefaultPageEncoding, isBomPresent );
            }
        }
        baseDirStack.pop();
        return parsedPage;
    }
    private String getJspConfigPageEncoding ( String absFileName ) {
        JspConfig jspConfig = ctxt.getOptions().getJspConfig();
        JspConfig.JspProperty jspProperty
            = jspConfig.findJspProperty ( absFileName );
        return jspProperty.getPageEncoding();
    }
    private void determineSyntaxAndEncoding ( String absFileName, Jar jar,
            String jspConfigPageEnc )
    throws JasperException, IOException {
        isXml = false;
        boolean isExternal = false;
        boolean revert = false;
        JspConfig jspConfig = ctxt.getOptions().getJspConfig();
        JspConfig.JspProperty jspProperty = jspConfig.findJspProperty (
                                                absFileName );
        if ( jspProperty.isXml() != null ) {
            isXml = JspUtil.booleanValue ( jspProperty.isXml() );
            isExternal = true;
        } else if ( absFileName.endsWith ( ".jspx" )
                    || absFileName.endsWith ( ".tagx" ) ) {
            isXml = true;
            isExternal = true;
        }
        if ( isExternal && !isXml ) {
            sourceEnc = jspConfigPageEnc;
            if ( sourceEnc != null ) {
                return;
            }
            sourceEnc = "ISO-8859-1";
        } else {
            Object[] ret = XMLEncodingDetector.getEncoding ( absFileName, jar,
                           ctxt, err );
            sourceEnc = ( String ) ret[0];
            if ( ( ( Boolean ) ret[1] ).booleanValue() ) {
                isEncodingSpecifiedInProlog = true;
            }
            if ( ( ( Boolean ) ret[2] ).booleanValue() ) {
                isBomPresent = true;
            }
            skip = ( ( Integer ) ret[3] ).intValue();
            if ( !isXml && sourceEnc.equals ( "UTF-8" ) ) {
                sourceEnc = "ISO-8859-1";
                revert = true;
            }
        }
        if ( isXml ) {
            return;
        }
        JspReader jspReader = null;
        try {
            jspReader = new JspReader ( ctxt, absFileName, sourceEnc, jar, err );
        } catch ( FileNotFoundException ex ) {
            throw new JasperException ( ex );
        }
        Mark startMark = jspReader.mark();
        if ( !isExternal ) {
            jspReader.reset ( startMark );
            if ( hasJspRoot ( jspReader ) ) {
                if ( revert ) {
                    sourceEnc = "UTF-8";
                }
                isXml = true;
                return;
            } else {
                if ( revert && isBomPresent ) {
                    sourceEnc = "UTF-8";
                }
                isXml = false;
            }
        }
        if ( !isBomPresent ) {
            sourceEnc = jspConfigPageEnc;
            if ( sourceEnc == null ) {
                sourceEnc = getPageEncodingForJspSyntax ( jspReader, startMark );
                if ( sourceEnc == null ) {
                    sourceEnc = "ISO-8859-1";
                    isDefaultPageEncoding = true;
                }
            }
        }
    }
    private String getPageEncodingForJspSyntax ( JspReader jspReader,
            Mark startMark )
    throws JasperException {
        String encoding = null;
        String saveEncoding = null;
        jspReader.reset ( startMark );
        while ( true ) {
            if ( jspReader.skipUntil ( "<" ) == null ) {
                break;
            }
            if ( jspReader.matches ( "%--" ) ) {
                if ( jspReader.skipUntil ( "--%>" ) == null ) {
                    break;
                }
                continue;
            }
            boolean isDirective = jspReader.matches ( "%@" );
            if ( isDirective ) {
                jspReader.skipSpaces();
            } else {
                isDirective = jspReader.matches ( "jsp:directive." );
            }
            if ( !isDirective ) {
                continue;
            }
            if ( jspReader.matches ( "tag " ) || jspReader.matches ( "page" ) ) {
                jspReader.skipSpaces();
                Attributes attrs = Parser.parseAttributes ( this, jspReader );
                encoding = getPageEncodingFromDirective ( attrs, "pageEncoding" );
                if ( encoding != null ) {
                    break;
                }
                encoding = getPageEncodingFromDirective ( attrs, "contentType" );
                if ( encoding != null ) {
                    saveEncoding = encoding;
                }
            }
        }
        if ( encoding == null ) {
            encoding = saveEncoding;
        }
        return encoding;
    }
    private String getPageEncodingFromDirective ( Attributes attrs,
            String attrName ) {
        String value = attrs.getValue ( attrName );
        if ( attrName.equals ( "pageEncoding" ) ) {
            return value;
        }
        String contentType = value;
        String encoding = null;
        if ( contentType != null ) {
            int loc = contentType.indexOf ( CHARSET );
            if ( loc != -1 ) {
                encoding = contentType.substring ( loc + CHARSET.length() );
            }
        }
        return encoding;
    }
    private String resolveFileName ( String inFileName ) {
        String fileName = inFileName.replace ( '\\', '/' );
        boolean isAbsolute = fileName.startsWith ( "/" );
        fileName = isAbsolute ? fileName
                   : baseDirStack.peek() + fileName;
        String baseDir =
            fileName.substring ( 0, fileName.lastIndexOf ( '/' ) + 1 );
        baseDirStack.push ( baseDir );
        return fileName;
    }
    private boolean hasJspRoot ( JspReader reader ) {
        Mark start = null;
        while ( ( start = reader.skipUntil ( "<" ) ) != null ) {
            int c = reader.nextChar();
            if ( c != '!' && c != '?' ) {
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
        String prefix = reader.getText ( start, stop ).substring ( 1 );
        start = stop;
        stop = reader.skipUntil ( ">" );
        if ( stop == null ) {
            return false;
        }
        String root = reader.getText ( start, stop );
        String xmlnsDecl = "xmlns:" + prefix;
        int index = root.indexOf ( xmlnsDecl );
        if ( index == -1 ) {
            return false;
        }
        index += xmlnsDecl.length();
        while ( index < root.length()
                && Character.isWhitespace ( root.charAt ( index ) ) ) {
            index++;
        }
        if ( index < root.length() && root.charAt ( index ) == '=' ) {
            index++;
            while ( index < root.length()
                    && Character.isWhitespace ( root.charAt ( index ) ) ) {
                index++;
            }
            if ( index < root.length()
                    && ( root.charAt ( index ) == '"' || root.charAt ( index ) == '\'' ) ) {
                index++;
                if ( root.regionMatches ( index, JSP_URI, 0, JSP_URI.length() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
}
