package org.apache.jasper.xmlparser;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.ErrorDispatcher;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.compiler.Localizer;
import org.apache.tomcat.Jar;
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
    private int fBufferSize = DEFAULT_BUFFER_SIZE;
    private char[] ch = new char[DEFAULT_BUFFER_SIZE];
    private int position;
    private int count;
    private final XMLString fString = new XMLString();
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private static final String fVersionSymbol = "version";
    private static final String fEncodingSymbol = "encoding";
    private static final String fStandaloneSymbol = "standalone";
    private final String[] fStrings = new String[3];
    private ErrorDispatcher err;
    public XMLEncodingDetector() {
        fSymbolTable = new SymbolTable();
        fCurrentEntity = this;
    }
    public static Object[] getEncoding ( String fname, Jar jar,
                                         JspCompilationContext ctxt,
                                         ErrorDispatcher err )
    throws IOException, JasperException {
        InputStream inStream = JspUtil.getInputStream ( fname, jar, ctxt );
        XMLEncodingDetector detector = new XMLEncodingDetector();
        Object[] ret = detector.getEncoding ( inStream, err );
        inStream.close();
        return ret;
    }
    private Object[] getEncoding ( InputStream in, ErrorDispatcher err )
    throws IOException, JasperException {
        this.stream = in;
        this.err = err;
        createInitialReader();
        scanXMLDecl();
        return new Object[] { this.encoding,
                              Boolean.valueOf ( this.isEncodingSetInProlog ),
                              Boolean.valueOf ( this.isBomPresent ),
                              Integer.valueOf ( this.skip )
                            };
    }
    void endEntity() {
    }
    private void createInitialReader() throws IOException, JasperException {
        stream = new RewindableInputStream ( stream );
        if ( encoding == null ) {
            final byte[] b4 = new byte[4];
            int count = 0;
            for ( ; count < 4; count++ ) {
                b4[count] = ( byte ) stream.read();
            }
            if ( count == 4 ) {
                Object [] encodingDesc = getEncodingName ( b4, count );
                encoding = ( String ) ( encodingDesc[0] );
                isBigEndian = ( Boolean ) ( encodingDesc[1] );
                if ( encodingDesc.length > 3 ) {
                    isBomPresent = ( ( Boolean ) ( encodingDesc[2] ) ).booleanValue();
                    skip = ( ( Integer ) ( encodingDesc[3] ) ).intValue();
                } else {
                    isBomPresent = true;
                    skip = ( ( Integer ) ( encodingDesc[2] ) ).intValue();
                }
                stream.reset();
                if ( encoding.equals ( "UTF-8" ) ) {
                    int b0 = b4[0] & 0xFF;
                    int b1 = b4[1] & 0xFF;
                    int b2 = b4[2] & 0xFF;
                    if ( b0 == 0xEF && b1 == 0xBB && b2 == 0xBF ) {
                        long skipped = stream.skip ( 3 );
                        if ( skipped != 3 ) {
                            throw new IOException ( Localizer.getMessage (
                                                        "xmlParser.skipBomFail" ) );
                        }
                    }
                }
                reader = createReader ( stream, encoding, isBigEndian );
            } else {
                reader = createReader ( stream, encoding, isBigEndian );
            }
        }
    }
    private Reader createReader ( InputStream inputStream, String encoding,
                                  Boolean isBigEndian )
    throws IOException, JasperException {
        if ( encoding == null ) {
            encoding = "UTF-8";
        }
        String ENCODING = encoding.toUpperCase ( Locale.ENGLISH );
        if ( ENCODING.equals ( "UTF-8" ) ) {
            return new UTF8Reader ( inputStream, fBufferSize );
        }
        if ( ENCODING.equals ( "US-ASCII" ) ) {
            return new ASCIIReader ( inputStream, fBufferSize );
        }
        if ( ENCODING.equals ( "ISO-10646-UCS-4" ) ) {
            if ( isBigEndian != null ) {
                boolean isBE = isBigEndian.booleanValue();
                if ( isBE ) {
                    return new UCSReader ( inputStream, UCSReader.UCS4BE );
                } else {
                    return new UCSReader ( inputStream, UCSReader.UCS4LE );
                }
            } else {
                err.jspError ( "jsp.error.xml.encodingByteOrderUnsupported",
                               encoding );
            }
        }
        if ( ENCODING.equals ( "ISO-10646-UCS-2" ) ) {
            if ( isBigEndian != null ) {
                boolean isBE = isBigEndian.booleanValue();
                if ( isBE ) {
                    return new UCSReader ( inputStream, UCSReader.UCS2BE );
                } else {
                    return new UCSReader ( inputStream, UCSReader.UCS2LE );
                }
            } else {
                err.jspError ( "jsp.error.xml.encodingByteOrderUnsupported",
                               encoding );
            }
        }
        boolean validIANA = XMLChar.isValidIANAEncoding ( encoding );
        if ( !validIANA ) {
            err.jspError ( "jsp.error.xml.encodingDeclInvalid", encoding );
            encoding = "ISO-8859-1";
        }
        String javaEncoding = EncodingMap.getIANA2JavaMapping ( ENCODING );
        if ( javaEncoding == null ) {
            err.jspError ( "jsp.error.xml.encodingDeclInvalid", encoding );
            javaEncoding = "ISO8859_1";
        }
        return new InputStreamReader ( inputStream, javaEncoding );
    }
    private Object[] getEncodingName ( byte[] b4, int count ) {
        if ( count < 2 ) {
            return new Object[] {"UTF-8", null, Boolean.FALSE, Integer.valueOf ( 0 ) };
        }
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if ( b0 == 0xFE && b1 == 0xFF ) {
            return new Object [] {"UTF-16BE", Boolean.TRUE, Integer.valueOf ( 2 ) };
        }
        if ( b0 == 0xFF && b1 == 0xFE ) {
            return new Object [] {"UTF-16LE", Boolean.FALSE, Integer.valueOf ( 2 ) };
        }
        if ( count < 3 ) {
            return new Object [] {"UTF-8", null, Boolean.FALSE, Integer.valueOf ( 0 ) };
        }
        int b2 = b4[2] & 0xFF;
        if ( b0 == 0xEF && b1 == 0xBB && b2 == 0xBF ) {
            return new Object [] {"UTF-8", null, Integer.valueOf ( 3 ) };
        }
        if ( count < 4 ) {
            return new Object [] {"UTF-8", null, Integer.valueOf ( 0 ) };
        }
        int b3 = b4[3] & 0xFF;
        if ( b0 == 0x00 && b1 == 0x00 && b2 == 0x00 && b3 == 0x3C ) {
            return new Object [] {"ISO-10646-UCS-4", Boolean.TRUE, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x3C && b1 == 0x00 && b2 == 0x00 && b3 == 0x00 ) {
            return new Object [] {"ISO-10646-UCS-4", Boolean.FALSE, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x00 && b1 == 0x00 && b2 == 0x3C && b3 == 0x00 ) {
            return new Object [] {"ISO-10646-UCS-4", null, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x00 ) {
            return new Object [] {"ISO-10646-UCS-4", null, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F ) {
            return new Object [] {"UTF-16BE", Boolean.TRUE, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00 ) {
            return new Object [] {"UTF-16LE", Boolean.FALSE, Integer.valueOf ( 4 ) };
        }
        if ( b0 == 0x4C && b1 == 0x6F && b2 == 0xA7 && b3 == 0x94 ) {
            return new Object [] {"CP037", null, Integer.valueOf ( 4 ) };
        }
        return new Object [] {"UTF-8", null, Boolean.FALSE, Integer.valueOf ( 0 ) };
    }
    public boolean isExternal() {
        return true;
    }
    public int peekChar() throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if ( fCurrentEntity.isExternal() ) {
            return c != '\r' ? c : '\n';
        } else {
            return c;
        }
    }
    public int scanChar() throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position++];
        boolean external = false;
        if ( c == '\n' ||
                ( c == '\r' && ( external = fCurrentEntity.isExternal() ) ) ) {
            if ( fCurrentEntity.position == fCurrentEntity.count ) {
                fCurrentEntity.ch[0] = ( char ) c;
                load ( 1, false );
            }
            if ( c == '\r' && external ) {
                if ( fCurrentEntity.ch[fCurrentEntity.position++] != '\n' ) {
                    fCurrentEntity.position--;
                }
                c = '\n';
            }
        }
        return c;
    }
    public String scanName() throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        int offset = fCurrentEntity.position;
        if ( XMLChar.isNameStart ( fCurrentEntity.ch[offset] ) ) {
            if ( ++fCurrentEntity.position == fCurrentEntity.count ) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[offset];
                offset = 0;
                if ( load ( 1, false ) ) {
                    String symbol = fSymbolTable.addSymbol ( fCurrentEntity.ch,
                                    0, 1 );
                    return symbol;
                }
            }
            while ( XMLChar.isName ( fCurrentEntity.ch[fCurrentEntity.position] ) ) {
                if ( ++fCurrentEntity.position == fCurrentEntity.count ) {
                    int length = fCurrentEntity.position - offset;
                    if ( length == fBufferSize ) {
                        char[] tmp = new char[fBufferSize * 2];
                        System.arraycopy ( fCurrentEntity.ch, offset,
                                           tmp, 0, length );
                        fCurrentEntity.ch = tmp;
                        fBufferSize *= 2;
                    } else {
                        System.arraycopy ( fCurrentEntity.ch, offset,
                                           fCurrentEntity.ch, 0, length );
                    }
                    offset = 0;
                    if ( load ( length, false ) ) {
                        break;
                    }
                }
            }
        }
        int length = fCurrentEntity.position - offset;
        String symbol = null;
        if ( length > 0 ) {
            symbol = fSymbolTable.addSymbol ( fCurrentEntity.ch, offset, length );
        }
        return symbol;
    }
    public int scanLiteral ( int quote, XMLString content )
    throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        } else if ( fCurrentEntity.position == fCurrentEntity.count - 1 ) {
            fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
            load ( 1, false );
            fCurrentEntity.position = 0;
        }
        int offset = fCurrentEntity.position;
        int c = fCurrentEntity.ch[offset];
        int newlines = 0;
        boolean external = fCurrentEntity.isExternal();
        if ( c == '\n' || ( c == '\r' && external ) ) {
            do {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ( c == '\r' && external ) {
                    newlines++;
                    if ( fCurrentEntity.position == fCurrentEntity.count ) {
                        offset = 0;
                        fCurrentEntity.position = newlines;
                        if ( load ( newlines, false ) ) {
                            break;
                        }
                    }
                    if ( fCurrentEntity.ch[fCurrentEntity.position] == '\n' ) {
                        fCurrentEntity.position++;
                        offset++;
                    } else {
                        newlines++;
                    }
                } else if ( c == '\n' ) {
                    newlines++;
                    if ( fCurrentEntity.position == fCurrentEntity.count ) {
                        offset = 0;
                        fCurrentEntity.position = newlines;
                        if ( load ( newlines, false ) ) {
                            break;
                        }
                    }
                } else {
                    fCurrentEntity.position--;
                    break;
                }
            } while ( fCurrentEntity.position < fCurrentEntity.count - 1 );
            for ( int i = offset; i < fCurrentEntity.position; i++ ) {
                fCurrentEntity.ch[i] = '\n';
            }
            int length = fCurrentEntity.position - offset;
            if ( fCurrentEntity.position == fCurrentEntity.count - 1 ) {
                content.setValues ( fCurrentEntity.ch, offset, length );
                return -1;
            }
        }
        while ( fCurrentEntity.position < fCurrentEntity.count ) {
            c = fCurrentEntity.ch[fCurrentEntity.position++];
            if ( c == quote || c == '%' || !XMLChar.isContent ( c ) ) {
                fCurrentEntity.position--;
                break;
            }
        }
        int length = fCurrentEntity.position - offset;
        content.setValues ( fCurrentEntity.ch, offset, length );
        if ( fCurrentEntity.position != fCurrentEntity.count ) {
            c = fCurrentEntity.ch[fCurrentEntity.position];
        } else {
            c = -1;
        }
        return c;
    }
    public boolean scanData ( String delimiter, XMLStringBuffer buffer )
    throws IOException {
        boolean done = false;
        int delimLen = delimiter.length();
        char charAt0 = delimiter.charAt ( 0 );
        boolean external = fCurrentEntity.isExternal();
        do {
            if ( fCurrentEntity.position == fCurrentEntity.count ) {
                load ( 0, true );
            } else if ( fCurrentEntity.position >= fCurrentEntity.count - delimLen ) {
                System.arraycopy ( fCurrentEntity.ch, fCurrentEntity.position,
                                   fCurrentEntity.ch, 0, fCurrentEntity.count - fCurrentEntity.position );
                load ( fCurrentEntity.count - fCurrentEntity.position, false );
                fCurrentEntity.position = 0;
            }
            if ( fCurrentEntity.position >= fCurrentEntity.count - delimLen ) {
                int length = fCurrentEntity.count - fCurrentEntity.position;
                buffer.append ( fCurrentEntity.ch, fCurrentEntity.position,
                                length );
                fCurrentEntity.position = fCurrentEntity.count;
                load ( 0, true );
                return false;
            }
            int offset = fCurrentEntity.position;
            int c = fCurrentEntity.ch[offset];
            int newlines = 0;
            if ( c == '\n' || ( c == '\r' && external ) ) {
                do {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if ( c == '\r' && external ) {
                        newlines++;
                        if ( fCurrentEntity.position == fCurrentEntity.count ) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            if ( load ( newlines, false ) ) {
                                break;
                            }
                        }
                        if ( fCurrentEntity.ch[fCurrentEntity.position] == '\n' ) {
                            fCurrentEntity.position++;
                            offset++;
                        } else {
                            newlines++;
                        }
                    } else if ( c == '\n' ) {
                        newlines++;
                        if ( fCurrentEntity.position == fCurrentEntity.count ) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            fCurrentEntity.count = newlines;
                            if ( load ( newlines, false ) ) {
                                break;
                            }
                        }
                    } else {
                        fCurrentEntity.position--;
                        break;
                    }
                } while ( fCurrentEntity.position < fCurrentEntity.count - 1 );
                for ( int i = offset; i < fCurrentEntity.position; i++ ) {
                    fCurrentEntity.ch[i] = '\n';
                }
                int length = fCurrentEntity.position - offset;
                if ( fCurrentEntity.position == fCurrentEntity.count - 1 ) {
                    buffer.append ( fCurrentEntity.ch, offset, length );
                    return true;
                }
            }
            OUTER: while ( fCurrentEntity.position < fCurrentEntity.count ) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ( c == charAt0 ) {
                    int delimOffset = fCurrentEntity.position - 1;
                    for ( int i = 1; i < delimLen; i++ ) {
                        if ( fCurrentEntity.position == fCurrentEntity.count ) {
                            fCurrentEntity.position -= i;
                            break OUTER;
                        }
                        c = fCurrentEntity.ch[fCurrentEntity.position++];
                        if ( delimiter.charAt ( i ) != c ) {
                            fCurrentEntity.position--;
                            break;
                        }
                    }
                    if ( fCurrentEntity.position == delimOffset + delimLen ) {
                        done = true;
                        break;
                    }
                } else if ( c == '\n' || ( external && c == '\r' ) ) {
                    fCurrentEntity.position--;
                    break;
                } else if ( XMLChar.isInvalid ( c ) ) {
                    fCurrentEntity.position--;
                    int length = fCurrentEntity.position - offset;
                    buffer.append ( fCurrentEntity.ch, offset, length );
                    return true;
                }
            }
            int length = fCurrentEntity.position - offset;
            if ( done ) {
                length -= delimLen;
            }
            buffer.append ( fCurrentEntity.ch, offset, length );
        } while ( !done );
        return !done;
    }
    public boolean skipChar ( int c ) throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        int cc = fCurrentEntity.ch[fCurrentEntity.position];
        if ( cc == c ) {
            fCurrentEntity.position++;
            return true;
        } else if ( c == '\n' && cc == '\r' && fCurrentEntity.isExternal() ) {
            if ( fCurrentEntity.position == fCurrentEntity.count ) {
                fCurrentEntity.ch[0] = ( char ) cc;
                load ( 1, false );
            }
            fCurrentEntity.position++;
            if ( fCurrentEntity.ch[fCurrentEntity.position] == '\n' ) {
                fCurrentEntity.position++;
            }
            return true;
        }
        return false;
    }
    public boolean skipSpaces() throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        int c = fCurrentEntity.ch[fCurrentEntity.position];
        if ( XMLChar.isSpace ( c ) ) {
            boolean external = fCurrentEntity.isExternal();
            do {
                boolean entityChanged = false;
                if ( c == '\n' || ( external && c == '\r' ) ) {
                    if ( fCurrentEntity.position == fCurrentEntity.count - 1 ) {
                        fCurrentEntity.ch[0] = ( char ) c;
                        entityChanged = load ( 1, true );
                        if ( !entityChanged ) {
                            fCurrentEntity.position = 0;
                        }
                    }
                    if ( c == '\r' && external ) {
                        if ( fCurrentEntity.ch[++fCurrentEntity.position] != '\n' ) {
                            fCurrentEntity.position--;
                        }
                    }
                }
                if ( !entityChanged ) {
                    fCurrentEntity.position++;
                }
                if ( fCurrentEntity.position == fCurrentEntity.count ) {
                    load ( 0, true );
                }
            } while ( XMLChar.isSpace ( c = fCurrentEntity.ch[fCurrentEntity.position] ) );
            return true;
        }
        return false;
    }
    public boolean skipString ( String s ) throws IOException {
        if ( fCurrentEntity.position == fCurrentEntity.count ) {
            load ( 0, true );
        }
        final int length = s.length();
        for ( int i = 0; i < length; i++ ) {
            char c = fCurrentEntity.ch[fCurrentEntity.position++];
            if ( c != s.charAt ( i ) ) {
                fCurrentEntity.position -= i + 1;
                return false;
            }
            if ( i < length - 1 && fCurrentEntity.position == fCurrentEntity.count ) {
                System.arraycopy ( fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1 );
                if ( load ( i + 1, false ) ) {
                    fCurrentEntity.position -= i + 1;
                    return false;
                }
            }
        }
        return true;
    }
    final boolean load ( int offset, boolean changeEntity )
    throws IOException {
        int count = fCurrentEntity.reader.read ( fCurrentEntity.ch, offset,
                    DEFAULT_XMLDECL_BUFFER_SIZE );
        boolean entityChanged = false;
        if ( count != -1 ) {
            if ( count != 0 ) {
                fCurrentEntity.count = count + offset;
                fCurrentEntity.position = offset;
            }
        } else {
            fCurrentEntity.count = offset;
            fCurrentEntity.position = offset;
            entityChanged = true;
            if ( changeEntity ) {
                endEntity();
                if ( fCurrentEntity == null ) {
                    throw new EOFException();
                }
                if ( fCurrentEntity.position == fCurrentEntity.count ) {
                    load ( 0, false );
                }
            }
        }
        return entityChanged;
    }
    private static final class RewindableInputStream extends InputStream {
        private InputStream fInputStream;
        private byte[] fData;
        private int fEndOffset;
        private int fOffset;
        private int fLength;
        private int fMark;
        public RewindableInputStream ( InputStream is ) {
            fData = new byte[DEFAULT_XMLDECL_BUFFER_SIZE];
            fInputStream = is;
            fEndOffset = -1;
            fOffset = 0;
            fLength = 0;
            fMark = 0;
        }
        @Override
        public int read() throws IOException {
            int b = 0;
            if ( fOffset < fLength ) {
                return fData[fOffset++] & 0xff;
            }
            if ( fOffset == fEndOffset ) {
                return -1;
            }
            if ( fOffset == fData.length ) {
                byte[] newData = new byte[fOffset << 1];
                System.arraycopy ( fData, 0, newData, 0, fOffset );
                fData = newData;
            }
            b = fInputStream.read();
            if ( b == -1 ) {
                fEndOffset = fOffset;
                return -1;
            }
            fData[fLength++] = ( byte ) b;
            fOffset++;
            return b & 0xff;
        }
        @Override
        public int read ( byte[] b, int off, int len ) throws IOException {
            int bytesLeft = fLength - fOffset;
            if ( bytesLeft == 0 ) {
                if ( fOffset == fEndOffset ) {
                    return -1;
                }
                int returnedVal = read();
                if ( returnedVal == -1 ) {
                    fEndOffset = fOffset;
                    return -1;
                }
                b[off] = ( byte ) returnedVal;
                return 1;
            }
            if ( len < bytesLeft ) {
                if ( len <= 0 ) {
                    return 0;
                }
            } else {
                len = bytesLeft;
            }
            if ( b != null ) {
                System.arraycopy ( fData, fOffset, b, off, len );
            }
            fOffset += len;
            return len;
        }
        @Override
        public long skip ( long n )
        throws IOException {
            int bytesLeft;
            if ( n <= 0 ) {
                return 0;
            }
            bytesLeft = fLength - fOffset;
            if ( bytesLeft == 0 ) {
                if ( fOffset == fEndOffset ) {
                    return 0;
                }
                return fInputStream.skip ( n );
            }
            if ( n <= bytesLeft ) {
                fOffset += n;
                return n;
            }
            fOffset += bytesLeft;
            if ( fOffset == fEndOffset ) {
                return bytesLeft;
            }
            n -= bytesLeft;
            return fInputStream.skip ( n ) + bytesLeft;
        }
        @Override
        public int available() throws IOException {
            int bytesLeft = fLength - fOffset;
            if ( bytesLeft == 0 ) {
                if ( fOffset == fEndOffset ) {
                    return -1;
                }
                return 0;
            }
            return bytesLeft;
        }
        @Override
        public synchronized void mark ( int howMuch ) {
            fMark = fOffset;
        }
        @Override
        public synchronized void reset() {
            fOffset = fMark;
        }
        @Override
        public boolean markSupported() {
            return true;
        }
        @Override
        public void close() throws IOException {
            if ( fInputStream != null ) {
                fInputStream.close();
                fInputStream = null;
            }
        }
    }
    private void scanXMLDecl() throws IOException, JasperException {
        if ( skipString ( "<?xml" ) ) {
            if ( XMLChar.isName ( peekChar() ) ) {
                fStringBuffer.clear();
                fStringBuffer.append ( "xml" );
                while ( XMLChar.isName ( peekChar() ) ) {
                    fStringBuffer.append ( ( char ) scanChar() );
                }
                String target = fSymbolTable.addSymbol ( fStringBuffer.ch,
                                fStringBuffer.offset,
                                fStringBuffer.length );
                scanPIData ( target, fString );
            } else {
                scanXMLDeclOrTextDecl ( false );
            }
        }
    }
    private void scanXMLDeclOrTextDecl ( boolean scanningTextDecl )
    throws IOException, JasperException {
        scanXMLDeclOrTextDecl ( scanningTextDecl, fStrings );
        String encodingPseudoAttr = fStrings[1];
        if ( encodingPseudoAttr != null ) {
            isEncodingSetInProlog = true;
            encoding = encodingPseudoAttr;
        }
    }
    private void scanXMLDeclOrTextDecl ( boolean scanningTextDecl,
                                         String[] pseudoAttributeValues )
    throws IOException, JasperException {
        String version = null;
        String encoding = null;
        String standalone = null;
        final int STATE_VERSION = 0;
        final int STATE_ENCODING = 1;
        final int STATE_STANDALONE = 2;
        final int STATE_DONE = 3;
        int state = STATE_VERSION;
        boolean dataFoundForTarget = false;
        boolean sawSpace = skipSpaces();
        while ( peekChar() != '?' ) {
            dataFoundForTarget = true;
            String name = scanPseudoAttribute ( scanningTextDecl, fString );
            switch ( state ) {
            case STATE_VERSION: {
                if ( name == fVersionSymbol ) {
                    if ( !sawSpace ) {
                        reportFatalError ( scanningTextDecl
                                           ? "jsp.error.xml.spaceRequiredBeforeVersionInTextDecl"
                                           : "jsp.error.xml.spaceRequiredBeforeVersionInXMLDecl",
                                           null );
                    }
                    version = fString.toString();
                    state = STATE_ENCODING;
                    if ( !version.equals ( "1.0" ) ) {
                        err.jspError ( "jsp.error.xml.versionNotSupported",
                                       version );
                    }
                } else if ( name == fEncodingSymbol ) {
                    if ( !scanningTextDecl ) {
                        err.jspError ( "jsp.error.xml.versionInfoRequired" );
                    }
                    if ( !sawSpace ) {
                        reportFatalError ( scanningTextDecl
                                           ? "jsp.error.xml.spaceRequiredBeforeEncodingInTextDecl"
                                           : "jsp.error.xml.spaceRequiredBeforeEncodingInXMLDecl",
                                           null );
                    }
                    encoding = fString.toString();
                    state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                } else {
                    if ( scanningTextDecl ) {
                        err.jspError ( "jsp.error.xml.encodingDeclRequired" );
                    } else {
                        err.jspError ( "jsp.error.xml.versionInfoRequired" );
                    }
                }
                break;
            }
            case STATE_ENCODING: {
                if ( name == fEncodingSymbol ) {
                    if ( !sawSpace ) {
                        reportFatalError ( scanningTextDecl
                                           ? "jsp.error.xml.spaceRequiredBeforeEncodingInTextDecl"
                                           : "jsp.error.xml.spaceRequiredBeforeEncodingInXMLDecl",
                                           null );
                    }
                    encoding = fString.toString();
                    state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                } else if ( !scanningTextDecl && name == fStandaloneSymbol ) {
                    if ( !sawSpace ) {
                        err.jspError ( "jsp.error.xml.spaceRequiredBeforeStandalone" );
                    }
                    standalone = fString.toString();
                    state = STATE_DONE;
                    if ( !standalone.equals ( "yes" ) && !standalone.equals ( "no" ) ) {
                        err.jspError ( "jsp.error.xml.sdDeclInvalid" );
                    }
                } else {
                    err.jspError ( "jsp.error.xml.encodingDeclRequired" );
                }
                break;
            }
            case STATE_STANDALONE: {
                if ( name == fStandaloneSymbol ) {
                    if ( !sawSpace ) {
                        err.jspError ( "jsp.error.xml.spaceRequiredBeforeStandalone" );
                    }
                    standalone = fString.toString();
                    state = STATE_DONE;
                    if ( !standalone.equals ( "yes" ) && !standalone.equals ( "no" ) ) {
                        err.jspError ( "jsp.error.xml.sdDeclInvalid" );
                    }
                } else {
                    err.jspError ( "jsp.error.xml.encodingDeclRequired" );
                }
                break;
            }
            default: {
                err.jspError ( "jsp.error.xml.noMorePseudoAttributes" );
            }
            }
            sawSpace = skipSpaces();
        }
        if ( scanningTextDecl && state != STATE_DONE ) {
            err.jspError ( "jsp.error.xml.morePseudoAttributes" );
        }
        if ( scanningTextDecl ) {
            if ( !dataFoundForTarget && encoding == null ) {
                err.jspError ( "jsp.error.xml.encodingDeclRequired" );
            }
        } else {
            if ( !dataFoundForTarget && version == null ) {
                err.jspError ( "jsp.error.xml.versionInfoRequired" );
            }
        }
        if ( !skipChar ( '?' ) ) {
            err.jspError ( "jsp.error.xml.xmlDeclUnterminated" );
        }
        if ( !skipChar ( '>' ) ) {
            err.jspError ( "jsp.error.xml.xmlDeclUnterminated" );
        }
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;
    }
    public String scanPseudoAttribute ( boolean scanningTextDecl,
                                        XMLString value )
    throws IOException, JasperException {
        String name = scanName();
        if ( name == null ) {
            err.jspError ( "jsp.error.xml.pseudoAttrNameExpected" );
        }
        skipSpaces();
        if ( !skipChar ( '=' ) ) {
            reportFatalError ( scanningTextDecl ?
                               "jsp.error.xml.eqRequiredInTextDecl"
                               : "jsp.error.xml.eqRequiredInXMLDecl",
                               name );
        }
        skipSpaces();
        int quote = peekChar();
        if ( quote != '\'' && quote != '"' ) {
            reportFatalError ( scanningTextDecl ?
                               "jsp.error.xml.quoteRequiredInTextDecl"
                               : "jsp.error.xml.quoteRequiredInXMLDecl" ,
                               name );
        }
        scanChar();
        int c = scanLiteral ( quote, value );
        if ( c != quote ) {
            fStringBuffer2.clear();
            do {
                fStringBuffer2.append ( value );
                if ( c != -1 ) {
                    if ( c == '&' || c == '%' || c == '<' || c == ']' ) {
                        fStringBuffer2.append ( ( char ) scanChar() );
                    } else if ( XMLChar.isHighSurrogate ( c ) ) {
                        scanSurrogates ( fStringBuffer2 );
                    } else if ( XMLChar.isInvalid ( c ) ) {
                        String key = scanningTextDecl
                                     ? "jsp.error.xml.invalidCharInTextDecl"
                                     : "jsp.error.xml.invalidCharInXMLDecl";
                        reportFatalError ( key, Integer.toString ( c, 16 ) );
                        scanChar();
                    }
                }
                c = scanLiteral ( quote, value );
            } while ( c != quote );
            fStringBuffer2.append ( value );
            value.setValues ( fStringBuffer2 );
        }
        if ( !skipChar ( quote ) ) {
            reportFatalError ( scanningTextDecl ?
                               "jsp.error.xml.closeQuoteMissingInTextDecl"
                               : "jsp.error.xml.closeQuoteMissingInXMLDecl",
                               name );
        }
        return name;
    }
    private void scanPIData ( String target, XMLString data )
    throws IOException, JasperException {
        if ( target.length() == 3 ) {
            char c0 = Character.toLowerCase ( target.charAt ( 0 ) );
            char c1 = Character.toLowerCase ( target.charAt ( 1 ) );
            char c2 = Character.toLowerCase ( target.charAt ( 2 ) );
            if ( c0 == 'x' && c1 == 'm' && c2 == 'l' ) {
                err.jspError ( "jsp.error.xml.reservedPITarget" );
            }
        }
        if ( !skipSpaces() ) {
            if ( skipString ( "?>" ) ) {
                data.clear();
                return;
            } else {
                err.jspError ( "jsp.error.xml.spaceRequiredInPI" );
            }
        }
        fStringBuffer.clear();
        if ( scanData ( "?>", fStringBuffer ) ) {
            do {
                int c = peekChar();
                if ( c != -1 ) {
                    if ( XMLChar.isHighSurrogate ( c ) ) {
                        scanSurrogates ( fStringBuffer );
                    } else if ( XMLChar.isInvalid ( c ) ) {
                        err.jspError ( "jsp.error.xml.invalidCharInPI",
                                       Integer.toHexString ( c ) );
                        scanChar();
                    }
                }
            } while ( scanData ( "?>", fStringBuffer ) );
        }
        data.setValues ( fStringBuffer );
    }
    private boolean scanSurrogates ( XMLStringBuffer buf )
    throws IOException, JasperException {
        int high = scanChar();
        int low = peekChar();
        if ( !XMLChar.isLowSurrogate ( low ) ) {
            err.jspError ( "jsp.error.xml.invalidCharInContent",
                           Integer.toString ( high, 16 ) );
            return false;
        }
        scanChar();
        int c = XMLChar.supplemental ( ( char ) high, ( char ) low );
        if ( !XMLChar.isValid ( c ) ) {
            err.jspError ( "jsp.error.xml.invalidCharInContent",
                           Integer.toString ( c, 16 ) );
            return false;
        }
        buf.append ( ( char ) high );
        buf.append ( ( char ) low );
        return true;
    }
    private void reportFatalError ( String msgId, String arg )
    throws JasperException {
        err.jspError ( msgId, arg );
    }
}
