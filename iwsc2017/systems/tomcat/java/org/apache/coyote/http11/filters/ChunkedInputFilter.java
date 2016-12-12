package org.apache.coyote.http11.filters;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.Constants;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.res.StringManager;
public class ChunkedInputFilter implements InputFilter, ApplicationBufferHandler {
    private static final StringManager sm = StringManager.getManager (
            ChunkedInputFilter.class.getPackage().getName() );
    protected static final String ENCODING_NAME = "chunked";
    protected static final ByteChunk ENCODING = new ByteChunk();
    static {
        ENCODING.setBytes ( ENCODING_NAME.getBytes ( StandardCharsets.ISO_8859_1 ),
                            0, ENCODING_NAME.length() );
    }
    protected InputBuffer buffer;
    protected int remaining = 0;
    protected ByteBuffer readChunk;
    protected boolean endChunk = false;
    protected final ByteChunk trailingHeaders = new ByteChunk();
    protected boolean needCRLFParse = false;
    private Request request;
    private final long maxExtensionSize;
    private final int maxTrailerSize;
    private long extensionSize;
    private final int maxSwallowSize;
    private boolean error;
    private final Set<String> allowedTrailerHeaders;
    public ChunkedInputFilter ( int maxTrailerSize, Set<String> allowedTrailerHeaders,
                                int maxExtensionSize, int maxSwallowSize ) {
        this.trailingHeaders.setLimit ( maxTrailerSize );
        this.allowedTrailerHeaders = allowedTrailerHeaders;
        this.maxExtensionSize = maxExtensionSize;
        this.maxTrailerSize = maxTrailerSize;
        this.maxSwallowSize = maxSwallowSize;
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        if ( endChunk ) {
            return -1;
        }
        checkError();
        if ( needCRLFParse ) {
            needCRLFParse = false;
            parseCRLF ( false );
        }
        if ( remaining <= 0 ) {
            if ( !parseChunkHeader() ) {
                throwIOException ( sm.getString ( "chunkedInputFilter.invalidHeader" ) );
            }
            if ( endChunk ) {
                parseEndChunk();
                return -1;
            }
        }
        int result = 0;
        if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
            if ( readBytes() < 0 ) {
                throwIOException ( sm.getString ( "chunkedInputFilter.eos" ) );
            }
        }
        if ( remaining > readChunk.remaining() ) {
            result = readChunk.remaining();
            remaining = remaining - result;
            if ( readChunk != handler.getByteBuffer() ) {
                handler.setByteBuffer ( readChunk.duplicate() );
            }
            readChunk.position ( readChunk.limit() );
        } else {
            result = remaining;
            if ( readChunk != handler.getByteBuffer() ) {
                handler.setByteBuffer ( readChunk.duplicate() );
                handler.getByteBuffer().limit ( readChunk.position() + remaining );
            }
            readChunk.position ( readChunk.position() + remaining );
            remaining = 0;
            if ( ( readChunk.position() + 1 ) >= readChunk.limit() ) {
                needCRLFParse = true;
            } else {
                parseCRLF ( false );
            }
        }
        return result;
    }
    @Override
    public void setRequest ( Request request ) {
        this.request = request;
    }
    @Override
    public long end() throws IOException {
        long swallowed = 0;
        int read = 0;
        while ( ( read = doRead ( this ) ) >= 0 ) {
            swallowed += read;
            if ( maxSwallowSize > -1 && swallowed > maxSwallowSize ) {
                throwIOException ( sm.getString ( "inputFilter.maxSwallow" ) );
            }
        }
        return readChunk.remaining();
    }
    @Override
    public int available() {
        return readChunk != null ? readChunk.remaining() : 0;
    }
    @Override
    public void setBuffer ( InputBuffer buffer ) {
        this.buffer = buffer;
    }
    @Override
    public void recycle() {
        remaining = 0;
        if ( readChunk != null ) {
            readChunk.position ( 0 ).limit ( 0 );
        }
        endChunk = false;
        needCRLFParse = false;
        trailingHeaders.recycle();
        trailingHeaders.setLimit ( maxTrailerSize );
        extensionSize = 0;
        error = false;
    }
    @Override
    public ByteChunk getEncodingName() {
        return ENCODING;
    }
    @Override
    public boolean isFinished() {
        return endChunk;
    }
    protected int readBytes() throws IOException {
        return buffer.doRead ( this );
    }
    protected boolean parseChunkHeader() throws IOException {
        int result = 0;
        boolean eol = false;
        int readDigit = 0;
        boolean extension = false;
        while ( !eol ) {
            if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                if ( readBytes() <= 0 ) {
                    return false;
                }
            }
            byte chr = readChunk.get ( readChunk.position() );
            if ( chr == Constants.CR || chr == Constants.LF ) {
                parseCRLF ( false );
                eol = true;
            } else if ( chr == Constants.SEMI_COLON && !extension ) {
                extension = true;
                extensionSize++;
            } else if ( !extension ) {
                int charValue = HexUtils.getDec ( chr );
                if ( charValue != -1 && readDigit < 8 ) {
                    readDigit++;
                    result = ( result << 4 ) | charValue;
                } else {
                    return false;
                }
            } else {
                extensionSize++;
                if ( maxExtensionSize > -1 && extensionSize > maxExtensionSize ) {
                    throwIOException ( sm.getString ( "chunkedInputFilter.maxExtension" ) );
                }
            }
            if ( !eol ) {
                readChunk.position ( readChunk.position() + 1 );
            }
        }
        if ( readDigit == 0 || result < 0 ) {
            return false;
        }
        if ( result == 0 ) {
            endChunk = true;
        }
        remaining = result;
        return true;
    }
    protected void parseCRLF ( boolean tolerant ) throws IOException {
        boolean eol = false;
        boolean crfound = false;
        while ( !eol ) {
            if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                if ( readBytes() <= 0 ) {
                    throwIOException ( sm.getString ( "chunkedInputFilter.invalidCrlfNoData" ) );
                }
            }
            byte chr = readChunk.get ( readChunk.position() );
            if ( chr == Constants.CR ) {
                if ( crfound ) {
                    throwIOException ( sm.getString ( "chunkedInputFilter.invalidCrlfCRCR" ) );
                }
                crfound = true;
            } else if ( chr == Constants.LF ) {
                if ( !tolerant && !crfound ) {
                    throwIOException ( sm.getString ( "chunkedInputFilter.invalidCrlfNoCR" ) );
                }
                eol = true;
            } else {
                throwIOException ( sm.getString ( "chunkedInputFilter.invalidCrlf" ) );
            }
            readChunk.position ( readChunk.position() + 1 );
        }
    }
    protected void parseEndChunk() throws IOException {
        while ( parseHeader() ) {
        }
    }
    private boolean parseHeader() throws IOException {
        MimeHeaders headers = request.getMimeHeaders();
        byte chr = 0;
        if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
            if ( readBytes() < 0 ) {
                throwEOFException ( sm.getString ( "chunkedInputFilter.eosTrailer" ) );
            }
        }
        chr = readChunk.get ( readChunk.position() );
        if ( chr == Constants.CR || chr == Constants.LF ) {
            parseCRLF ( false );
            return false;
        }
        int startPos = trailingHeaders.getEnd();
        boolean colon = false;
        while ( !colon ) {
            if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                if ( readBytes() < 0 ) {
                    throwEOFException ( sm.getString ( "chunkedInputFilter.eosTrailer" ) );
                }
            }
            chr = readChunk.get ( readChunk.position() );
            if ( ( chr >= Constants.A ) && ( chr <= Constants.Z ) ) {
                chr = ( byte ) ( chr - Constants.LC_OFFSET );
            }
            if ( chr == Constants.COLON ) {
                colon = true;
            } else {
                trailingHeaders.append ( chr );
            }
            readChunk.position ( readChunk.position() + 1 );
        }
        int colonPos = trailingHeaders.getEnd();
        boolean eol = false;
        boolean validLine = true;
        int lastSignificantChar = 0;
        while ( validLine ) {
            boolean space = true;
            while ( space ) {
                if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                    if ( readBytes() < 0 ) {
                        throwEOFException ( sm.getString ( "chunkedInputFilter.eosTrailer" ) );
                    }
                }
                chr = readChunk.get ( readChunk.position() );
                if ( ( chr == Constants.SP ) || ( chr == Constants.HT ) ) {
                    readChunk.position ( readChunk.position() + 1 );
                    int newlimit = trailingHeaders.getLimit() - 1;
                    if ( trailingHeaders.getEnd() > newlimit ) {
                        throwIOException ( sm.getString ( "chunkedInputFilter.maxTrailer" ) );
                    }
                    trailingHeaders.setLimit ( newlimit );
                } else {
                    space = false;
                }
            }
            while ( !eol ) {
                if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                    if ( readBytes() < 0 ) {
                        throwEOFException ( sm.getString ( "chunkedInputFilter.eosTrailer" ) );
                    }
                }
                chr = readChunk.get ( readChunk.position() );
                if ( chr == Constants.CR || chr == Constants.LF ) {
                    parseCRLF ( true );
                    eol = true;
                } else if ( chr == Constants.SP ) {
                    trailingHeaders.append ( chr );
                } else {
                    trailingHeaders.append ( chr );
                    lastSignificantChar = trailingHeaders.getEnd();
                }
                if ( !eol ) {
                    readChunk.position ( readChunk.position() + 1 );
                }
            }
            if ( readChunk == null || readChunk.position() >= readChunk.limit() ) {
                if ( readBytes() < 0 ) {
                    throwEOFException ( sm.getString ( "chunkedInputFilter.eosTrailer" ) );
                }
            }
            chr = readChunk.get ( readChunk.position() );
            if ( ( chr != Constants.SP ) && ( chr != Constants.HT ) ) {
                validLine = false;
            } else {
                eol = false;
                trailingHeaders.append ( chr );
            }
        }
        String headerName = new String ( trailingHeaders.getBytes(), startPos,
                                         colonPos - startPos, StandardCharsets.ISO_8859_1 );
        if ( allowedTrailerHeaders.contains ( headerName.toLowerCase ( Locale.ENGLISH ) ) ) {
            MessageBytes headerValue = headers.addValue ( headerName );
            headerValue.setBytes ( trailingHeaders.getBytes(), colonPos,
                                   lastSignificantChar - colonPos );
        }
        return true;
    }
    private void throwIOException ( String msg ) throws IOException {
        error = true;
        throw new IOException ( msg );
    }
    private void throwEOFException ( String msg ) throws IOException {
        error = true;
        throw new EOFException ( msg );
    }
    private void checkError() throws IOException {
        if ( error ) {
            throw new IOException ( sm.getString ( "chunkedInputFilter.error" ) );
        }
    }
    @Override
    public void setByteBuffer ( ByteBuffer buffer ) {
        readChunk = buffer;
    }
    @Override
    public ByteBuffer getByteBuffer() {
        return readChunk;
    }
    @Override
    public void expand ( int size ) {
    }
}