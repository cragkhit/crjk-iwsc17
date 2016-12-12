package org.apache.jasper.xmlparser;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.util.Locale;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.JasperException;
import java.io.IOException;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.Jar;
import org.apache.jasper.compiler.ErrorDispatcher;
import java.io.Reader;
import java.io.InputStream;
public class XMLEncodingDetector {
    private InputStream stream;
    private String encoding;
    private boolean isEncodingSetInProlog;
    private boolean isBomPresent;
    private int skip;
    private Boolean isBigEndian;
    private Reader reader;
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_XMLDECL_BUFFER_SIZE = 64;
    private final SymbolTable fSymbolTable;
    private final XMLEncodingDetector fCurrentEntity;
    private int fBufferSize;
    private char[] ch;
    private int position;
    private int count;
    private final XMLString fString;
    private final XMLStringBuffer fStringBuffer;
    private final XMLStringBuffer fStringBuffer2;
    private static final String fVersionSymbol = "version";
    private static final String fEncodingSymbol = "encoding";
    private static final String fStandaloneSymbol = "standalone";
    private final String[] fStrings;
    private ErrorDispatcher err;
    public XMLEncodingDetector() {
        this.fBufferSize = 2048;
        this.ch = new char[2048];
        this.fString = new XMLString();
        this.fStringBuffer = new XMLStringBuffer();
        this.fStringBuffer2 = new XMLStringBuffer();
        this.fStrings = new String[3];
        this.fSymbolTable = new SymbolTable();
        this.fCurrentEntity = this;
    }
    public static Object[] getEncoding ( final String fname, final Jar jar, final JspCompilationContext ctxt, final ErrorDispatcher err ) throws IOException, JasperException {
        final InputStream inStream = JspUtil.getInputStream ( fname, jar, ctxt );
        final XMLEncodingDetector detector = new XMLEncodingDetector();
        final Object[] ret = detector.getEncoding ( inStream, err );
        inStream.close();
        return ret;
    }
    private Object[] getEncoding ( final InputStream in, final ErrorDispatcher err ) throws IOException, JasperException {
        this.stream = in;
        this.err = err;
        this.createInitialReader();
        this.scanXMLDecl();
        return new Object[] { this.encoding, this.isEncodingSetInProlog, this.isBomPresent, this.skip };
    }
    void endEntity() {
    }
    private void createInitialReader() throws IOException, JasperException {
        this.stream = new RewindableInputStream ( this.stream );
        if ( this.encoding == null ) {
            byte[] b4 = new byte[4];
            int count;
            for ( count = 0; count < 4; ++count ) {
                b4[count] = ( byte ) this.stream.read();
            }
            if ( count == 4 ) {
                final Object[] encodingDesc = this.getEncodingName ( b4, count );
                this.encoding = ( String ) encodingDesc[0];
                this.isBigEndian = ( Boolean ) encodingDesc[1];
                if ( encodingDesc.length > 3 ) {
                    this.isBomPresent = ( boolean ) encodingDesc[2];
                    this.skip = ( int ) encodingDesc[3];
                } else {
                    this.isBomPresent = true;
                    this.skip = ( int ) encodingDesc[2];
                }
                this.stream.reset();
                if ( this.encoding.equals ( "UTF-8" ) ) {
                    final int b2 = b4[0] & 0xFF;
                    final int b3 = b4[1] & 0xFF;
                    b4 = ( b4[2] & 0xFF );
                    if ( b2 == 239 && b3 == 187 && b4 == 191 ) {
                        final long skipped = this.stream.skip ( 3L );
                        if ( skipped != 3L ) {
                            throw new IOException ( Localizer.getMessage ( "xmlParser.skipBomFail" ) );
                        }
                    }
                }
                this.reader = this.createReader ( this.stream, this.encoding, this.isBigEndian );
            } else {
                this.reader = this.createReader ( this.stream, this.encoding, this.isBigEndian );
            }
        }
    }
    private Reader createReader ( final InputStream inputStream, String encoding, final Boolean isBigEndian ) throws IOException, JasperException {
        if ( encoding == null ) {
            encoding = "UTF-8";
        }
        final String ENCODING = encoding.toUpperCase ( Locale.ENGLISH );
        if ( ENCODING.equals ( "UTF-8" ) ) {
            return new UTF8Reader ( inputStream, this.fBufferSize );
        }
        if ( ENCODING.equals ( "US-ASCII" ) ) {
            return new ASCIIReader ( inputStream, this.fBufferSize );
        }
        if ( ENCODING.equals ( "ISO-10646-UCS-4" ) ) {
            if ( isBigEndian != null ) {
                final boolean isBE = isBigEndian;
                if ( isBE ) {
                    return new UCSReader ( inputStream, ( short ) 8 );
                }
                return new UCSReader ( inputStream, ( short ) 4 );
            } else {
                this.err.jspError ( "jsp.error.xml.encodingByteOrderUnsupported", encoding );
            }
        }
        if ( ENCODING.equals ( "ISO-10646-UCS-2" ) ) {
            if ( isBigEndian != null ) {
                final boolean isBE = isBigEndian;
                if ( isBE ) {
                    return new UCSReader ( inputStream, ( short ) 2 );
                }
                return new UCSReader ( inputStream, ( short ) 1 );
            } else {
                this.err.jspError ( "jsp.error.xml.encodingByteOrderUnsupported", encoding );
            }
        }
        final boolean validIANA = XMLChar.isValidIANAEncoding ( encoding );
        if ( !validIANA ) {
            this.err.jspError ( "jsp.error.xml.encodingDeclInvalid", encoding );
            encoding = "ISO-8859-1";
        }
        String javaEncoding = EncodingMap.getIANA2JavaMapping ( ENCODING );
        if ( javaEncoding == null ) {
            this.err.jspError ( "jsp.error.xml.encodingDeclInvalid", encoding );
            javaEncoding = "ISO8859_1";
        }
        return new InputStreamReader ( inputStream, javaEncoding );
    }
    private Object[] getEncodingName ( final byte[] b4, final int count ) {
        if ( count < 2 ) {
            return new Object[] { "UTF-8", null, Boolean.FALSE, 0 };
        }
        final int b5 = b4[0] & 0xFF;
        final int b6 = b4[1] & 0xFF;
        if ( b5 == 254 && b6 == 255 ) {
            return new Object[] { "UTF-16BE", Boolean.TRUE, 2 };
        }
        if ( b5 == 255 && b6 == 254 ) {
            return new Object[] { "UTF-16LE", Boolean.FALSE, 2 };
        }
        if ( count < 3 ) {
            return new Object[] { "UTF-8", null, Boolean.FALSE, 0 };
        }
        final int b7 = b4[2] & 0xFF;
        if ( b5 == 239 && b6 == 187 && b7 == 191 ) {
            return new Object[] { "UTF-8", null, 3 };
        }
        if ( count < 4 ) {
            return new Object[] { "UTF-8", null, 0 };
        }
        final int b8 = b4[3] & 0xFF;
        if ( b5 == 0 && b6 == 0 && b7 == 0 && b8 == 60 ) {
            return new Object[] { "ISO-10646-UCS-4", Boolean.TRUE, 4 };
        }
        if ( b5 == 60 && b6 == 0 && b7 == 0 && b8 == 0 ) {
            return new Object[] { "ISO-10646-UCS-4", Boolean.FALSE, 4 };
        }
        if ( b5 == 0 && b6 == 0 && b7 == 60 && b8 == 0 ) {
            return new Object[] { "ISO-10646-UCS-4", null, 4 };
        }
        if ( b5 == 0 && b6 == 60 && b7 == 0 && b8 == 0 ) {
            return new Object[] { "ISO-10646-UCS-4", null, 4 };
        }
        if ( b5 == 0 && b6 == 60 && b7 == 0 && b8 == 63 ) {
            return new Object[] { "UTF-16BE", Boolean.TRUE, 4 };
        }
        if ( b5 == 60 && b6 == 0 && b7 == 63 && b8 == 0 ) {
            return new Object[] { "UTF-16LE", Boolean.FALSE, 4 };
        }
        if ( b5 == 76 && b6 == 111 && b7 == 167 && b8 == 148 ) {
            return new Object[] { "CP037", null, 4 };
        }
        return new Object[] { "UTF-8", null, Boolean.FALSE, 0 };
    }
    public boolean isExternal() {
        return true;
    }
    public int peekChar() throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        final int c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if ( this.fCurrentEntity.isExternal() ) {
            return ( c != 13 ) ? c : 10;
        }
        return c;
    }
    public int scanChar() throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        int c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
        boolean external = false;
        if ( c == 10 || ( c == 13 && ( external = this.fCurrentEntity.isExternal() ) ) ) {
            if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                this.fCurrentEntity.ch[0] = ( char ) c;
                this.load ( 1, false );
            }
            if ( c == 13 && external ) {
                if ( this.fCurrentEntity.ch[this.fCurrentEntity.position++] != '\n' ) {
                    final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
                    --fCurrentEntity.position;
                }
                c = 10;
            }
        }
        return c;
    }
    public String scanName() throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        int offset = this.fCurrentEntity.position;
        if ( XMLChar.isNameStart ( this.fCurrentEntity.ch[offset] ) ) {
            if ( ++this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[offset];
                offset = 0;
                if ( this.load ( 1, false ) ) {
                    final String symbol = this.fSymbolTable.addSymbol ( this.fCurrentEntity.ch, 0, 1 );
                    return symbol;
                }
            }
            while ( XMLChar.isName ( this.fCurrentEntity.ch[this.fCurrentEntity.position] ) ) {
                if ( ++this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                    final int length = this.fCurrentEntity.position - offset;
                    if ( length == this.fBufferSize ) {
                        final char[] tmp = new char[this.fBufferSize * 2];
                        System.arraycopy ( this.fCurrentEntity.ch, offset, tmp, 0, length );
                        this.fCurrentEntity.ch = tmp;
                        this.fBufferSize *= 2;
                    } else {
                        System.arraycopy ( this.fCurrentEntity.ch, offset, this.fCurrentEntity.ch, 0, length );
                    }
                    offset = 0;
                    if ( this.load ( length, false ) ) {
                        break;
                    }
                    continue;
                }
            }
        }
        final int length = this.fCurrentEntity.position - offset;
        String symbol2 = null;
        if ( length > 0 ) {
            symbol2 = this.fSymbolTable.addSymbol ( this.fCurrentEntity.ch, offset, length );
        }
        return symbol2;
    }
    public int scanLiteral ( final int quote, final XMLString content ) throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        } else if ( this.fCurrentEntity.position == this.fCurrentEntity.count - 1 ) {
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            this.load ( 1, false );
            this.fCurrentEntity.position = 0;
        }
        int offset = this.fCurrentEntity.position;
        int c = this.fCurrentEntity.ch[offset];
        int newlines = 0;
        final boolean external = this.fCurrentEntity.isExternal();
        if ( c == 10 || ( c == 13 && external ) ) {
            do {
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
                if ( c == 13 && external ) {
                    ++newlines;
                    if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                        offset = 0;
                        this.fCurrentEntity.position = newlines;
                        if ( this.load ( newlines, false ) ) {
                            break;
                        }
                    }
                    if ( this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n' ) {
                        final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
                        ++fCurrentEntity.position;
                        ++offset;
                    } else {
                        ++newlines;
                    }
                } else {
                    if ( c != 10 ) {
                        final XMLEncodingDetector fCurrentEntity2 = this.fCurrentEntity;
                        --fCurrentEntity2.position;
                        break;
                    }
                    ++newlines;
                    if ( this.fCurrentEntity.position != this.fCurrentEntity.count ) {
                        continue;
                    }
                    offset = 0;
                    this.fCurrentEntity.position = newlines;
                    if ( this.load ( newlines, false ) ) {
                        break;
                    }
                    continue;
                }
            } while ( this.fCurrentEntity.position < this.fCurrentEntity.count - 1 );
            for ( int i = offset; i < this.fCurrentEntity.position; ++i ) {
                this.fCurrentEntity.ch[i] = '\n';
            }
            final int length = this.fCurrentEntity.position - offset;
            if ( this.fCurrentEntity.position == this.fCurrentEntity.count - 1 ) {
                content.setValues ( this.fCurrentEntity.ch, offset, length );
                return -1;
            }
        }
        while ( this.fCurrentEntity.position < this.fCurrentEntity.count ) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
            if ( c == quote || c == 37 || !XMLChar.isContent ( c ) ) {
                final XMLEncodingDetector fCurrentEntity3 = this.fCurrentEntity;
                --fCurrentEntity3.position;
                break;
            }
        }
        final int length = this.fCurrentEntity.position - offset;
        content.setValues ( this.fCurrentEntity.ch, offset, length );
        if ( this.fCurrentEntity.position != this.fCurrentEntity.count ) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        } else {
            c = -1;
        }
        return c;
    }
    public boolean scanData ( final String delimiter, final XMLStringBuffer buffer ) throws IOException {
        boolean done = false;
        final int delimLen = delimiter.length();
        final char charAt0 = delimiter.charAt ( 0 );
        final boolean external = this.fCurrentEntity.isExternal();
        do {
            if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                this.load ( 0, true );
            } else if ( this.fCurrentEntity.position >= this.fCurrentEntity.count - delimLen ) {
                System.arraycopy ( this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, 0, this.fCurrentEntity.count - this.fCurrentEntity.position );
                this.load ( this.fCurrentEntity.count - this.fCurrentEntity.position, false );
                this.fCurrentEntity.position = 0;
            }
            if ( this.fCurrentEntity.position >= this.fCurrentEntity.count - delimLen ) {
                final int length = this.fCurrentEntity.count - this.fCurrentEntity.position;
                buffer.append ( this.fCurrentEntity.ch, this.fCurrentEntity.position, length );
                this.fCurrentEntity.position = this.fCurrentEntity.count;
                this.load ( 0, true );
                return false;
            }
            int offset = this.fCurrentEntity.position;
            int c = this.fCurrentEntity.ch[offset];
            int newlines = 0;
            if ( c == 10 || ( c == 13 && external ) ) {
                do {
                    c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
                    if ( c == 13 && external ) {
                        ++newlines;
                        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                            offset = 0;
                            this.fCurrentEntity.position = newlines;
                            if ( this.load ( newlines, false ) ) {
                                break;
                            }
                        }
                        if ( this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n' ) {
                            final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
                            ++fCurrentEntity.position;
                            ++offset;
                        } else {
                            ++newlines;
                        }
                    } else {
                        if ( c != 10 ) {
                            final XMLEncodingDetector fCurrentEntity2 = this.fCurrentEntity;
                            --fCurrentEntity2.position;
                            break;
                        }
                        ++newlines;
                        if ( this.fCurrentEntity.position != this.fCurrentEntity.count ) {
                            continue;
                        }
                        offset = 0;
                        this.fCurrentEntity.position = newlines;
                        this.fCurrentEntity.count = newlines;
                        if ( this.load ( newlines, false ) ) {
                            break;
                        }
                        continue;
                    }
                } while ( this.fCurrentEntity.position < this.fCurrentEntity.count - 1 );
                for ( int i = offset; i < this.fCurrentEntity.position; ++i ) {
                    this.fCurrentEntity.ch[i] = '\n';
                }
                final int length2 = this.fCurrentEntity.position - offset;
                if ( this.fCurrentEntity.position == this.fCurrentEntity.count - 1 ) {
                    buffer.append ( this.fCurrentEntity.ch, offset, length2 );
                    return true;
                }
            }
            Label_0835:
            while ( this.fCurrentEntity.position < this.fCurrentEntity.count ) {
                c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
                if ( c == charAt0 ) {
                    final int delimOffset = this.fCurrentEntity.position - 1;
                    for ( int j = 1; j < delimLen; ++j ) {
                        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                            final XMLEncodingDetector fCurrentEntity3 = this.fCurrentEntity;
                            fCurrentEntity3.position -= j;
                            break Label_0835;
                        }
                        c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
                        if ( delimiter.charAt ( j ) != c ) {
                            final XMLEncodingDetector fCurrentEntity4 = this.fCurrentEntity;
                            --fCurrentEntity4.position;
                            break;
                        }
                    }
                    if ( this.fCurrentEntity.position == delimOffset + delimLen ) {
                        done = true;
                        break;
                    }
                    continue;
                } else {
                    if ( c == 10 || ( external && c == 13 ) ) {
                        final XMLEncodingDetector fCurrentEntity5 = this.fCurrentEntity;
                        --fCurrentEntity5.position;
                        break;
                    }
                    if ( XMLChar.isInvalid ( c ) ) {
                        final XMLEncodingDetector fCurrentEntity6 = this.fCurrentEntity;
                        --fCurrentEntity6.position;
                        final int length2 = this.fCurrentEntity.position - offset;
                        buffer.append ( this.fCurrentEntity.ch, offset, length2 );
                        return true;
                    }
                    continue;
                }
            }
            int length2 = this.fCurrentEntity.position - offset;
            if ( done ) {
                length2 -= delimLen;
            }
            buffer.append ( this.fCurrentEntity.ch, offset, length2 );
        } while ( !done );
        return !done;
    }
    public boolean skipChar ( final int c ) throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        final int cc = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if ( cc == c ) {
            final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
            ++fCurrentEntity.position;
            return true;
        }
        if ( c == 10 && cc == 13 && this.fCurrentEntity.isExternal() ) {
            if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                this.fCurrentEntity.ch[0] = ( char ) cc;
                this.load ( 1, false );
            }
            final XMLEncodingDetector fCurrentEntity2 = this.fCurrentEntity;
            ++fCurrentEntity2.position;
            if ( this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n' ) {
                final XMLEncodingDetector fCurrentEntity3 = this.fCurrentEntity;
                ++fCurrentEntity3.position;
            }
            return true;
        }
        return false;
    }
    public boolean skipSpaces() throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        int c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if ( XMLChar.isSpace ( c ) ) {
            final boolean external = this.fCurrentEntity.isExternal();
            do {
                boolean entityChanged = false;
                if ( c == 10 || ( external && c == 13 ) ) {
                    if ( this.fCurrentEntity.position == this.fCurrentEntity.count - 1 ) {
                        this.fCurrentEntity.ch[0] = ( char ) c;
                        entityChanged = this.load ( 1, true );
                        if ( !entityChanged ) {
                            this.fCurrentEntity.position = 0;
                        }
                    }
                    if ( c == 13 && external && this.fCurrentEntity.ch[++this.fCurrentEntity.position] != '\n' ) {
                        final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
                        --fCurrentEntity.position;
                    }
                }
                if ( !entityChanged ) {
                    final XMLEncodingDetector fCurrentEntity2 = this.fCurrentEntity;
                    ++fCurrentEntity2.position;
                }
                if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                    this.load ( 0, true );
                }
            } while ( XMLChar.isSpace ( c = this.fCurrentEntity.ch[this.fCurrentEntity.position] ) );
            return true;
        }
        return false;
    }
    public boolean skipString ( final String s ) throws IOException {
        if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
            this.load ( 0, true );
        }
        for ( int length = s.length(), i = 0; i < length; ++i ) {
            final char c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
            if ( c != s.charAt ( i ) ) {
                final XMLEncodingDetector fCurrentEntity = this.fCurrentEntity;
                fCurrentEntity.position -= i + 1;
                return false;
            }
            if ( i < length - 1 && this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                System.arraycopy ( this.fCurrentEntity.ch, this.fCurrentEntity.count - i - 1, this.fCurrentEntity.ch, 0, i + 1 );
                if ( this.load ( i + 1, false ) ) {
                    final XMLEncodingDetector fCurrentEntity2 = this.fCurrentEntity;
                    fCurrentEntity2.position -= i + 1;
                    return false;
                }
            }
        }
        return true;
    }
    final boolean load ( final int offset, final boolean changeEntity ) throws IOException {
        final int count = this.fCurrentEntity.reader.read ( this.fCurrentEntity.ch, offset, 64 );
        boolean entityChanged = false;
        if ( count != -1 ) {
            if ( count != 0 ) {
                this.fCurrentEntity.count = count + offset;
                this.fCurrentEntity.position = offset;
            }
        } else {
            this.fCurrentEntity.count = offset;
            this.fCurrentEntity.position = offset;
            entityChanged = true;
            if ( changeEntity ) {
                this.endEntity();
                if ( this.fCurrentEntity == null ) {
                    throw new EOFException();
                }
                if ( this.fCurrentEntity.position == this.fCurrentEntity.count ) {
                    this.load ( 0, false );
                }
            }
        }
        return entityChanged;
    }
    private void scanXMLDecl() throws IOException, JasperException {
        if ( this.skipString ( "<?xml" ) ) {
            if ( XMLChar.isName ( this.peekChar() ) ) {
                this.fStringBuffer.clear();
                this.fStringBuffer.append ( "xml" );
                while ( XMLChar.isName ( this.peekChar() ) ) {
                    this.fStringBuffer.append ( ( char ) this.scanChar() );
                }
                final String target = this.fSymbolTable.addSymbol ( this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length );
                this.scanPIData ( target, this.fString );
            } else {
                this.scanXMLDeclOrTextDecl ( false );
            }
        }
    }
    private void scanXMLDeclOrTextDecl ( final boolean scanningTextDecl ) throws IOException, JasperException {
        this.scanXMLDeclOrTextDecl ( scanningTextDecl, this.fStrings );
        final String encodingPseudoAttr = this.fStrings[1];
        if ( encodingPseudoAttr != null ) {
            this.isEncodingSetInProlog = true;
            this.encoding = encodingPseudoAttr;
        }
    }
    private void scanXMLDeclOrTextDecl ( final boolean scanningTextDecl, final String[] pseudoAttributeValues ) throws IOException, JasperException {
        String version = null;
        String encoding = null;
        String standalone = null;
        final int STATE_VERSION = 0;
        final int STATE_ENCODING = 1;
        final int STATE_STANDALONE = 2;
        final int STATE_DONE = 3;
        int state = 0;
        boolean dataFoundForTarget = false;
        boolean sawSpace = this.skipSpaces();
        while ( this.peekChar() != 63 ) {
            dataFoundForTarget = true;
            final String name = this.scanPseudoAttribute ( scanningTextDecl, this.fString );
            switch ( state ) {
            case 0: {
                if ( name == "version" ) {
                    if ( !sawSpace ) {
                        this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.spaceRequiredBeforeVersionInTextDecl" : "jsp.error.xml.spaceRequiredBeforeVersionInXMLDecl", null );
                    }
                    version = this.fString.toString();
                    state = 1;
                    if ( !version.equals ( "1.0" ) ) {
                        this.err.jspError ( "jsp.error.xml.versionNotSupported", version );
                        break;
                    }
                    break;
                } else {
                    if ( name == "encoding" ) {
                        if ( !scanningTextDecl ) {
                            this.err.jspError ( "jsp.error.xml.versionInfoRequired", new String[0] );
                        }
                        if ( !sawSpace ) {
                            this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.spaceRequiredBeforeEncodingInTextDecl" : "jsp.error.xml.spaceRequiredBeforeEncodingInXMLDecl", null );
                        }
                        encoding = this.fString.toString();
                        state = ( scanningTextDecl ? 3 : 2 );
                        break;
                    }
                    if ( scanningTextDecl ) {
                        this.err.jspError ( "jsp.error.xml.encodingDeclRequired", new String[0] );
                        break;
                    }
                    this.err.jspError ( "jsp.error.xml.versionInfoRequired", new String[0] );
                    break;
                }
                break;
            }
            case 1: {
                if ( name == "encoding" ) {
                    if ( !sawSpace ) {
                        this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.spaceRequiredBeforeEncodingInTextDecl" : "jsp.error.xml.spaceRequiredBeforeEncodingInXMLDecl", null );
                    }
                    encoding = this.fString.toString();
                    state = ( scanningTextDecl ? 3 : 2 );
                    break;
                }
                if ( scanningTextDecl || name != "standalone" ) {
                    this.err.jspError ( "jsp.error.xml.encodingDeclRequired", new String[0] );
                    break;
                }
                if ( !sawSpace ) {
                    this.err.jspError ( "jsp.error.xml.spaceRequiredBeforeStandalone", new String[0] );
                }
                standalone = this.fString.toString();
                state = 3;
                if ( !standalone.equals ( "yes" ) && !standalone.equals ( "no" ) ) {
                    this.err.jspError ( "jsp.error.xml.sdDeclInvalid", new String[0] );
                    break;
                }
                break;
            }
            case 2: {
                if ( name != "standalone" ) {
                    this.err.jspError ( "jsp.error.xml.encodingDeclRequired", new String[0] );
                    break;
                }
                if ( !sawSpace ) {
                    this.err.jspError ( "jsp.error.xml.spaceRequiredBeforeStandalone", new String[0] );
                }
                standalone = this.fString.toString();
                state = 3;
                if ( !standalone.equals ( "yes" ) && !standalone.equals ( "no" ) ) {
                    this.err.jspError ( "jsp.error.xml.sdDeclInvalid", new String[0] );
                    break;
                }
                break;
            }
            default: {
                this.err.jspError ( "jsp.error.xml.noMorePseudoAttributes", new String[0] );
                break;
            }
            }
            sawSpace = this.skipSpaces();
        }
        if ( scanningTextDecl && state != 3 ) {
            this.err.jspError ( "jsp.error.xml.morePseudoAttributes", new String[0] );
        }
        if ( scanningTextDecl ) {
            if ( !dataFoundForTarget && encoding == null ) {
                this.err.jspError ( "jsp.error.xml.encodingDeclRequired", new String[0] );
            }
        } else if ( !dataFoundForTarget && version == null ) {
            this.err.jspError ( "jsp.error.xml.versionInfoRequired", new String[0] );
        }
        if ( !this.skipChar ( 63 ) ) {
            this.err.jspError ( "jsp.error.xml.xmlDeclUnterminated", new String[0] );
        }
        if ( !this.skipChar ( 62 ) ) {
            this.err.jspError ( "jsp.error.xml.xmlDeclUnterminated", new String[0] );
        }
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;
    }
    public String scanPseudoAttribute ( final boolean scanningTextDecl, final XMLString value ) throws IOException, JasperException {
        final String name = this.scanName();
        if ( name == null ) {
            this.err.jspError ( "jsp.error.xml.pseudoAttrNameExpected", new String[0] );
        }
        this.skipSpaces();
        if ( !this.skipChar ( 61 ) ) {
            this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.eqRequiredInTextDecl" : "jsp.error.xml.eqRequiredInXMLDecl", name );
        }
        this.skipSpaces();
        final int quote = this.peekChar();
        if ( quote != 39 && quote != 34 ) {
            this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.quoteRequiredInTextDecl" : "jsp.error.xml.quoteRequiredInXMLDecl", name );
        }
        this.scanChar();
        int c = this.scanLiteral ( quote, value );
        if ( c != quote ) {
            this.fStringBuffer2.clear();
            do {
                this.fStringBuffer2.append ( value );
                if ( c != -1 ) {
                    if ( c == 38 || c == 37 || c == 60 || c == 93 ) {
                        this.fStringBuffer2.append ( ( char ) this.scanChar() );
                    } else if ( XMLChar.isHighSurrogate ( c ) ) {
                        this.scanSurrogates ( this.fStringBuffer2 );
                    } else if ( XMLChar.isInvalid ( c ) ) {
                        final String key = scanningTextDecl ? "jsp.error.xml.invalidCharInTextDecl" : "jsp.error.xml.invalidCharInXMLDecl";
                        this.reportFatalError ( key, Integer.toString ( c, 16 ) );
                        this.scanChar();
                    }
                }
                c = this.scanLiteral ( quote, value );
            } while ( c != quote );
            this.fStringBuffer2.append ( value );
            value.setValues ( this.fStringBuffer2 );
        }
        if ( !this.skipChar ( quote ) ) {
            this.reportFatalError ( scanningTextDecl ? "jsp.error.xml.closeQuoteMissingInTextDecl" : "jsp.error.xml.closeQuoteMissingInXMLDecl", name );
        }
        return name;
    }
    private void scanPIData ( final String target, final XMLString data ) throws IOException, JasperException {
        if ( target.length() == 3 ) {
            final char c0 = Character.toLowerCase ( target.charAt ( 0 ) );
            final char c = Character.toLowerCase ( target.charAt ( 1 ) );
            final char c2 = Character.toLowerCase ( target.charAt ( 2 ) );
            if ( c0 == 'x' && c == 'm' && c2 == 'l' ) {
                this.err.jspError ( "jsp.error.xml.reservedPITarget", new String[0] );
            }
        }
        if ( !this.skipSpaces() ) {
            if ( this.skipString ( "?>" ) ) {
                data.clear();
                return;
            }
            this.err.jspError ( "jsp.error.xml.spaceRequiredInPI", new String[0] );
        }
        this.fStringBuffer.clear();
        if ( this.scanData ( "?>", this.fStringBuffer ) ) {
            do {
                final int c3 = this.peekChar();
                if ( c3 != -1 ) {
                    if ( XMLChar.isHighSurrogate ( c3 ) ) {
                        this.scanSurrogates ( this.fStringBuffer );
                    } else {
                        if ( !XMLChar.isInvalid ( c3 ) ) {
                            continue;
                        }
                        this.err.jspError ( "jsp.error.xml.invalidCharInPI", Integer.toHexString ( c3 ) );
                        this.scanChar();
                    }
                }
            } while ( this.scanData ( "?>", this.fStringBuffer ) );
        }
        data.setValues ( this.fStringBuffer );
    }
    private boolean scanSurrogates ( final XMLStringBuffer buf ) throws IOException, JasperException {
        final int high = this.scanChar();
        final int low = this.peekChar();
        if ( !XMLChar.isLowSurrogate ( low ) ) {
            this.err.jspError ( "jsp.error.xml.invalidCharInContent", Integer.toString ( high, 16 ) );
            return false;
        }
        this.scanChar();
        final int c = XMLChar.supplemental ( ( char ) high, ( char ) low );
        if ( !XMLChar.isValid ( c ) ) {
            this.err.jspError ( "jsp.error.xml.invalidCharInContent", Integer.toString ( c, 16 ) );
            return false;
        }
        buf.append ( ( char ) high );
        buf.append ( ( char ) low );
        return true;
    }
    private void reportFatalError ( final String msgId, final String arg ) throws JasperException {
        this.err.jspError ( msgId, arg );
    }
    private static final class RewindableInputStream extends InputStream {
        private InputStream fInputStream;
        private byte[] fData;
        private int fEndOffset;
        private int fOffset;
        private int fLength;
        private int fMark;
        public RewindableInputStream ( final InputStream is ) {
            this.fData = new byte[64];
            this.fInputStream = is;
            this.fEndOffset = -1;
            this.fOffset = 0;
            this.fLength = 0;
            this.fMark = 0;
        }
        @Override
        public int read() throws IOException {
            int b = 0;
            if ( this.fOffset < this.fLength ) {
                return this.fData[this.fOffset++] & 0xFF;
            }
            if ( this.fOffset == this.fEndOffset ) {
                return -1;
            }
            if ( this.fOffset == this.fData.length ) {
                final byte[] newData = new byte[this.fOffset << 1];
                System.arraycopy ( this.fData, 0, newData, 0, this.fOffset );
                this.fData = newData;
            }
            b = this.fInputStream.read();
            if ( b == -1 ) {
                this.fEndOffset = this.fOffset;
                return -1;
            }
            this.fData[this.fLength++] = ( byte ) b;
            ++this.fOffset;
            return b & 0xFF;
        }
        @Override
        public int read ( final byte[] b, final int off, int len ) throws IOException {
            final int bytesLeft = this.fLength - this.fOffset;
            if ( bytesLeft != 0 ) {
                if ( len < bytesLeft ) {
                    if ( len <= 0 ) {
                        return 0;
                    }
                } else {
                    len = bytesLeft;
                }
                if ( b != null ) {
                    System.arraycopy ( this.fData, this.fOffset, b, off, len );
                }
                this.fOffset += len;
                return len;
            }
            if ( this.fOffset == this.fEndOffset ) {
                return -1;
            }
            final int returnedVal = this.read();
            if ( returnedVal == -1 ) {
                this.fEndOffset = this.fOffset;
                return -1;
            }
            b[off] = ( byte ) returnedVal;
            return 1;
        }
        @Override
        public long skip ( long n ) throws IOException {
            if ( n <= 0L ) {
                return 0L;
            }
            final int bytesLeft = this.fLength - this.fOffset;
            if ( bytesLeft == 0 ) {
                if ( this.fOffset == this.fEndOffset ) {
                    return 0L;
                }
                return this.fInputStream.skip ( n );
            } else {
                if ( n <= bytesLeft ) {
                    this.fOffset += ( int ) n;
                    return n;
                }
                this.fOffset += bytesLeft;
                if ( this.fOffset == this.fEndOffset ) {
                    return bytesLeft;
                }
                n -= bytesLeft;
                return this.fInputStream.skip ( n ) + bytesLeft;
            }
        }
        @Override
        public int available() throws IOException {
            final int bytesLeft = this.fLength - this.fOffset;
            if ( bytesLeft != 0 ) {
                return bytesLeft;
            }
            if ( this.fOffset == this.fEndOffset ) {
                return -1;
            }
            return 0;
        }
        @Override
        public synchronized void mark ( final int howMuch ) {
            this.fMark = this.fOffset;
        }
        @Override
        public synchronized void reset() {
            this.fOffset = this.fMark;
        }
        @Override
        public boolean markSupported() {
            return true;
        }
        @Override
        public void close() throws IOException {
            if ( this.fInputStream != null ) {
                this.fInputStream.close();
                this.fInputStream = null;
            }
        }
    }
}
