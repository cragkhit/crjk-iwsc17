package org.apache.coyote.http11;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class Http11InputBuffer implements InputBuffer, ApplicationBufferHandler {
    private static final Log log = LogFactory.getLog ( Http11InputBuffer.class );
    private static final StringManager sm = StringManager.getManager ( Http11InputBuffer.class );
    private static final byte[] CLIENT_PREFACE_START =
        "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes ( StandardCharsets.ISO_8859_1 );
    private final Request request;
    private final MimeHeaders headers;
    private boolean parsingHeader;
    private boolean swallowInput;
    private ByteBuffer byteBuffer;
    private int end;
    private SocketWrapperBase<?> wrapper;
    private InputBuffer inputStreamInputBuffer;
    private InputFilter[] filterLibrary;
    private InputFilter[] activeFilters;
    private int lastActiveFilter;
    private boolean parsingRequestLine;
    private int parsingRequestLinePhase = 0;
    private boolean parsingRequestLineEol = false;
    private int parsingRequestLineStart = 0;
    private int parsingRequestLineQPos = -1;
    private HeaderParsePosition headerParsePos;
    private final HeaderParseData headerData = new HeaderParseData();
    private final int headerBufferSize;
    private int socketReadBufferSize;
    public Http11InputBuffer ( Request request, int headerBufferSize ) {
        this.request = request;
        headers = request.getMimeHeaders();
        this.headerBufferSize = headerBufferSize;
        filterLibrary = new InputFilter[0];
        activeFilters = new InputFilter[0];
        lastActiveFilter = -1;
        parsingHeader = true;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerParsePos = HeaderParsePosition.HEADER_START;
        swallowInput = true;
        inputStreamInputBuffer = new SocketInputBuffer();
    }
    void addFilter ( InputFilter filter ) {
        if ( filter == null ) {
            throw new NullPointerException ( sm.getString ( "iib.filter.npe" ) );
        }
        InputFilter[] newFilterLibrary = new InputFilter[filterLibrary.length + 1];
        for ( int i = 0; i < filterLibrary.length; i++ ) {
            newFilterLibrary[i] = filterLibrary[i];
        }
        newFilterLibrary[filterLibrary.length] = filter;
        filterLibrary = newFilterLibrary;
        activeFilters = new InputFilter[filterLibrary.length];
    }
    InputFilter[] getFilters() {
        return filterLibrary;
    }
    void addActiveFilter ( InputFilter filter ) {
        if ( lastActiveFilter == -1 ) {
            filter.setBuffer ( inputStreamInputBuffer );
        } else {
            for ( int i = 0; i <= lastActiveFilter; i++ ) {
                if ( activeFilters[i] == filter ) {
                    return;
                }
            }
            filter.setBuffer ( activeFilters[lastActiveFilter] );
        }
        activeFilters[++lastActiveFilter] = filter;
        filter.setRequest ( request );
    }
    void setSwallowInput ( boolean swallowInput ) {
        this.swallowInput = swallowInput;
    }
    @Override
    public int doRead ( ApplicationBufferHandler handler ) throws IOException {
        if ( lastActiveFilter == -1 ) {
            return inputStreamInputBuffer.doRead ( handler );
        } else {
            return activeFilters[lastActiveFilter].doRead ( handler );
        }
    }
    void recycle() {
        wrapper = null;
        request.recycle();
        for ( int i = 0; i <= lastActiveFilter; i++ ) {
            activeFilters[i].recycle();
        }
        byteBuffer.limit ( 0 ).position ( 0 );
        lastActiveFilter = -1;
        parsingHeader = true;
        swallowInput = true;
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
    }
    void nextRequest() {
        request.recycle();
        if ( byteBuffer.remaining() > 0 && byteBuffer.position() > 0 ) {
            byteBuffer.compact();
        }
        byteBuffer.limit ( byteBuffer.limit() - byteBuffer.position() ).position ( 0 );
        for ( int i = 0; i <= lastActiveFilter; i++ ) {
            activeFilters[i].recycle();
        }
        lastActiveFilter = -1;
        parsingHeader = true;
        swallowInput = true;
        headerParsePos = HeaderParsePosition.HEADER_START;
        parsingRequestLine = true;
        parsingRequestLinePhase = 0;
        parsingRequestLineEol = false;
        parsingRequestLineStart = 0;
        parsingRequestLineQPos = -1;
        headerData.recycle();
    }
    boolean parseRequestLine ( boolean keptAlive ) throws IOException {
        if ( !parsingRequestLine ) {
            return true;
        }
        if ( parsingRequestLinePhase < 2 ) {
            byte chr = 0;
            do {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( keptAlive ) {
                        wrapper.setReadTimeout ( wrapper.getEndpoint().getKeepAliveTimeout() );
                    }
                    if ( !fill ( false ) ) {
                        parsingRequestLinePhase = 1;
                        return false;
                    }
                    wrapper.setReadTimeout ( wrapper.getEndpoint().getConnectionTimeout() );
                }
                if ( !keptAlive && byteBuffer.position() == 0 && byteBuffer.limit() >= CLIENT_PREFACE_START.length - 1 ) {
                    boolean prefaceMatch = true;
                    for ( int i = 0; i < CLIENT_PREFACE_START.length && prefaceMatch; i++ ) {
                        if ( CLIENT_PREFACE_START[i] != byteBuffer.get ( i ) ) {
                            prefaceMatch = false;
                        }
                    }
                    if ( prefaceMatch ) {
                        parsingRequestLinePhase = -1;
                        return false;
                    }
                }
                if ( request.getStartTime() < 0 ) {
                    request.setStartTime ( System.currentTimeMillis() );
                }
                chr = byteBuffer.get();
            } while ( ( chr == Constants.CR ) || ( chr == Constants.LF ) );
            byteBuffer.position ( byteBuffer.position() - 1 );
            parsingRequestLineStart = byteBuffer.position();
            parsingRequestLinePhase = 2;
            if ( log.isDebugEnabled() ) {
                log.debug ( "Received ["
                            + new String ( byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining(), StandardCharsets.ISO_8859_1 ) + "]" );
            }
        }
        if ( parsingRequestLinePhase == 2 ) {
            boolean space = false;
            while ( !space ) {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( !fill ( false ) ) {
                        return false;
                    }
                }
                int pos = byteBuffer.position();
                byte chr = byteBuffer.get();
                if ( chr == Constants.SP || chr == Constants.HT ) {
                    space = true;
                    request.method().setBytes ( byteBuffer.array(), parsingRequestLineStart,
                                                pos - parsingRequestLineStart );
                } else if ( !HttpParser.isToken ( chr ) ) {
                    byteBuffer.position ( byteBuffer.position() - 1 );
                    throw new IllegalArgumentException ( sm.getString ( "iib.invalidmethod" ) );
                }
            }
            parsingRequestLinePhase = 3;
        }
        if ( parsingRequestLinePhase == 3 ) {
            boolean space = true;
            while ( space ) {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( !fill ( false ) ) {
                        return false;
                    }
                }
                byte chr = byteBuffer.get();
                if ( ! ( chr == Constants.SP || chr == Constants.HT ) ) {
                    space = false;
                    byteBuffer.position ( byteBuffer.position() - 1 );
                }
            }
            parsingRequestLineStart = byteBuffer.position();
            parsingRequestLinePhase = 4;
        }
        if ( parsingRequestLinePhase == 4 ) {
            int end = 0;
            boolean space = false;
            while ( !space ) {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( !fill ( false ) ) {
                        return false;
                    }
                }
                int pos = byteBuffer.position();
                byte chr = byteBuffer.get();
                if ( chr == Constants.SP || chr == Constants.HT ) {
                    space = true;
                    end = pos;
                } else if ( chr == Constants.CR || chr == Constants.LF ) {
                    parsingRequestLineEol = true;
                    space = true;
                    end = pos;
                } else if ( chr == Constants.QUESTION && parsingRequestLineQPos == -1 ) {
                    parsingRequestLineQPos = pos;
                } else if ( HttpParser.isNotRequestTarget ( chr ) ) {
                    throw new IllegalArgumentException ( sm.getString ( "iib.invalidRequestTarget" ) );
                }
            }
            if ( parsingRequestLineQPos >= 0 ) {
                request.queryString().setBytes ( byteBuffer.array(), parsingRequestLineQPos + 1,
                                                 end - parsingRequestLineQPos - 1 );
                request.requestURI().setBytes ( byteBuffer.array(), parsingRequestLineStart,
                                                parsingRequestLineQPos - parsingRequestLineStart );
            } else {
                request.requestURI().setBytes ( byteBuffer.array(), parsingRequestLineStart,
                                                end - parsingRequestLineStart );
            }
            parsingRequestLinePhase = 5;
        }
        if ( parsingRequestLinePhase == 5 ) {
            boolean space = true;
            while ( space ) {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( !fill ( false ) ) {
                        return false;
                    }
                }
                byte chr = byteBuffer.get();
                if ( ! ( chr == Constants.SP || chr == Constants.HT ) ) {
                    space = false;
                    byteBuffer.position ( byteBuffer.position() - 1 );
                }
            }
            parsingRequestLineStart = byteBuffer.position();
            parsingRequestLinePhase = 6;
            end = 0;
        }
        if ( parsingRequestLinePhase == 6 ) {
            while ( !parsingRequestLineEol ) {
                if ( byteBuffer.position() >= byteBuffer.limit() ) {
                    if ( !fill ( false ) ) {
                        return false;
                    }
                }
                int pos = byteBuffer.position();
                byte chr = byteBuffer.get();
                if ( chr == Constants.CR ) {
                    end = pos;
                } else if ( chr == Constants.LF ) {
                    if ( end == 0 ) {
                        end = pos;
                    }
                    parsingRequestLineEol = true;
                } else if ( !HttpParser.isHttpProtocol ( chr ) ) {
                    throw new IllegalArgumentException ( sm.getString ( "iib.invalidHttpProtocol" ) );
                }
            }
            if ( ( end - parsingRequestLineStart ) > 0 ) {
                request.protocol().setBytes ( byteBuffer.array(), parsingRequestLineStart,
                                              end - parsingRequestLineStart );
            } else {
                request.protocol().setString ( "" );
            }
            parsingRequestLine = false;
            parsingRequestLinePhase = 0;
            parsingRequestLineEol = false;
            parsingRequestLineStart = 0;
            return true;
        }
        throw new IllegalStateException (
            "Invalid request line parse phase:" + parsingRequestLinePhase );
    }
    boolean parseHeaders() throws IOException {
        if ( !parsingHeader ) {
            throw new IllegalStateException ( sm.getString ( "iib.parseheaders.ise.error" ) );
        }
        HeaderParseStatus status = HeaderParseStatus.HAVE_MORE_HEADERS;
        do {
            status = parseHeader();
            if ( byteBuffer.position() > headerBufferSize || byteBuffer.capacity() - byteBuffer.position() < socketReadBufferSize ) {
                throw new IllegalArgumentException ( sm.getString ( "iib.requestheadertoolarge.error" ) );
            }
        } while ( status == HeaderParseStatus.HAVE_MORE_HEADERS );
        if ( status == HeaderParseStatus.DONE ) {
            parsingHeader = false;
            end = byteBuffer.position();
            return true;
        } else {
            return false;
        }
    }
    int getParsingRequestLinePhase() {
        return parsingRequestLinePhase;
    }
    void endRequest() throws IOException {
        if ( swallowInput && ( lastActiveFilter != -1 ) ) {
            int extraBytes = ( int ) activeFilters[lastActiveFilter].end();
            byteBuffer.position ( byteBuffer.position() - extraBytes );
        }
    }
    int available ( boolean read ) {
        int available = byteBuffer.remaining();
        if ( ( available == 0 ) && ( lastActiveFilter >= 0 ) ) {
            for ( int i = 0; ( available == 0 ) && ( i <= lastActiveFilter ); i++ ) {
                available = activeFilters[i].available();
            }
        }
        if ( available > 0 || !read ) {
            return available;
        }
        try {
            fill ( false );
            available = byteBuffer.remaining();
        } catch ( IOException ioe ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "iib.available.readFail" ), ioe );
            }
            available = 1;
        }
        return available;
    }
    boolean isFinished() {
        if ( byteBuffer.limit() > byteBuffer.position() ) {
            return false;
        }
        if ( lastActiveFilter >= 0 ) {
            return activeFilters[lastActiveFilter].isFinished();
        } else {
            return false;
        }
    }
    ByteBuffer getLeftover() {
        int available = byteBuffer.remaining();
        if ( available > 0 ) {
            return ByteBuffer.wrap ( byteBuffer.array(), byteBuffer.position(), available );
        } else {
            return null;
        }
    }
    void init ( SocketWrapperBase<?> socketWrapper ) {
        wrapper = socketWrapper;
        wrapper.setAppReadBufHandler ( this );
        int bufLength = headerBufferSize +
                        wrapper.getSocketBufferHandler().getReadBuffer().capacity();
        if ( byteBuffer == null || byteBuffer.capacity() < bufLength ) {
            byteBuffer = ByteBuffer.allocate ( bufLength );
            byteBuffer.position ( 0 ).limit ( 0 );
        }
    }
    private boolean fill ( boolean block ) throws IOException {
        if ( parsingHeader ) {
            if ( byteBuffer.limit() >= headerBufferSize ) {
                throw new IllegalArgumentException ( sm.getString ( "iib.requestheadertoolarge.error" ) );
            }
        } else {
            byteBuffer.limit ( end ).position ( end );
        }
        byteBuffer.mark();
        if ( byteBuffer.position() < byteBuffer.limit() ) {
            byteBuffer.position ( byteBuffer.limit() );
        }
        byteBuffer.limit ( byteBuffer.capacity() );
        int nRead = wrapper.read ( block, byteBuffer );
        byteBuffer.limit ( byteBuffer.position() ).reset();
        if ( nRead > 0 ) {
            return true;
        } else if ( nRead == -1 ) {
            throw new EOFException ( sm.getString ( "iib.eof.error" ) );
        } else {
            return false;
        }
    }
    private HeaderParseStatus parseHeader() throws IOException {
        byte chr = 0;
        while ( headerParsePos == HeaderParsePosition.HEADER_START ) {
            if ( byteBuffer.position() >= byteBuffer.limit() ) {
                if ( !fill ( false ) ) {
                    headerParsePos = HeaderParsePosition.HEADER_START;
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }
            chr = byteBuffer.get();
            if ( chr == Constants.CR ) {
            } else if ( chr == Constants.LF ) {
                return HeaderParseStatus.DONE;
            } else {
                byteBuffer.position ( byteBuffer.position() - 1 );
                break;
            }
        }
        if ( headerParsePos == HeaderParsePosition.HEADER_START ) {
            headerData.start = byteBuffer.position();
            headerParsePos = HeaderParsePosition.HEADER_NAME;
        }
        while ( headerParsePos == HeaderParsePosition.HEADER_NAME ) {
            if ( byteBuffer.position() >= byteBuffer.limit() ) {
                if ( !fill ( false ) ) {
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }
            int pos = byteBuffer.position();
            chr = byteBuffer.get();
            if ( chr == Constants.COLON ) {
                headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
                headerData.headerValue = headers.addValue ( byteBuffer.array(), headerData.start,
                                         pos - headerData.start );
                pos = byteBuffer.position();
                headerData.start = pos;
                headerData.realPos = pos;
                headerData.lastSignificantChar = pos;
                break;
            } else if ( !HttpParser.isToken ( chr ) ) {
                headerData.lastSignificantChar = pos;
                byteBuffer.position ( byteBuffer.position() - 1 );
                return skipLine();
            }
            if ( ( chr >= Constants.A ) && ( chr <= Constants.Z ) ) {
                byteBuffer.put ( pos, ( byte ) ( chr - Constants.LC_OFFSET ) );
            }
        }
        if ( headerParsePos == HeaderParsePosition.HEADER_SKIPLINE ) {
            return skipLine();
        }
        while ( headerParsePos == HeaderParsePosition.HEADER_VALUE_START ||
                headerParsePos == HeaderParsePosition.HEADER_VALUE ||
                headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE ) {
            if ( headerParsePos == HeaderParsePosition.HEADER_VALUE_START ) {
                while ( true ) {
                    if ( byteBuffer.position() >= byteBuffer.limit() ) {
                        if ( !fill ( false ) ) {
                            return HeaderParseStatus.NEED_MORE_DATA;
                        }
                    }
                    chr = byteBuffer.get();
                    if ( ! ( chr == Constants.SP || chr == Constants.HT ) ) {
                        headerParsePos = HeaderParsePosition.HEADER_VALUE;
                        byteBuffer.position ( byteBuffer.position() - 1 );
                        break;
                    }
                }
            }
            if ( headerParsePos == HeaderParsePosition.HEADER_VALUE ) {
                boolean eol = false;
                while ( !eol ) {
                    if ( byteBuffer.position() >= byteBuffer.limit() ) {
                        if ( !fill ( false ) ) {
                            return HeaderParseStatus.NEED_MORE_DATA;
                        }
                    }
                    chr = byteBuffer.get();
                    if ( chr == Constants.CR ) {
                    } else if ( chr == Constants.LF ) {
                        eol = true;
                    } else if ( chr == Constants.SP || chr == Constants.HT ) {
                        byteBuffer.put ( headerData.realPos, chr );
                        headerData.realPos++;
                    } else {
                        byteBuffer.put ( headerData.realPos, chr );
                        headerData.realPos++;
                        headerData.lastSignificantChar = headerData.realPos;
                    }
                }
                headerData.realPos = headerData.lastSignificantChar;
                headerParsePos = HeaderParsePosition.HEADER_MULTI_LINE;
            }
            if ( byteBuffer.position() >= byteBuffer.limit() ) {
                if ( !fill ( false ) ) {
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }
            chr = byteBuffer.get ( byteBuffer.position() );
            if ( headerParsePos == HeaderParsePosition.HEADER_MULTI_LINE ) {
                if ( ( chr != Constants.SP ) && ( chr != Constants.HT ) ) {
                    headerParsePos = HeaderParsePosition.HEADER_START;
                    break;
                } else {
                    byteBuffer.put ( headerData.realPos, chr );
                    headerData.realPos++;
                    headerParsePos = HeaderParsePosition.HEADER_VALUE_START;
                }
            }
        }
        headerData.headerValue.setBytes ( byteBuffer.array(), headerData.start,
                                          headerData.lastSignificantChar - headerData.start );
        headerData.recycle();
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }
    private HeaderParseStatus skipLine() throws IOException {
        headerParsePos = HeaderParsePosition.HEADER_SKIPLINE;
        boolean eol = false;
        while ( !eol ) {
            if ( byteBuffer.position() >= byteBuffer.limit() ) {
                if ( !fill ( false ) ) {
                    return HeaderParseStatus.NEED_MORE_DATA;
                }
            }
            int pos = byteBuffer.position();
            byte chr = byteBuffer.get();
            if ( chr == Constants.CR ) {
            } else if ( chr == Constants.LF ) {
                eol = true;
            } else {
                headerData.lastSignificantChar = pos;
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "iib.invalidheader",
                                       new String ( byteBuffer.array(), headerData.start,
                                                    headerData.lastSignificantChar - headerData.start + 1,
                                                    StandardCharsets.ISO_8859_1 ) ) );
        }
        headerParsePos = HeaderParsePosition.HEADER_START;
        return HeaderParseStatus.HAVE_MORE_HEADERS;
    }
    private static enum HeaderParseStatus {
        DONE, HAVE_MORE_HEADERS, NEED_MORE_DATA
    }
    private static enum HeaderParsePosition {
        HEADER_START,
        HEADER_NAME,
        HEADER_VALUE_START,
        HEADER_VALUE,
        HEADER_MULTI_LINE,
        HEADER_SKIPLINE
    }
    private static class HeaderParseData {
        int start = 0;
        int realPos = 0;
        int lastSignificantChar = 0;
        MessageBytes headerValue = null;
        public void recycle() {
            start = 0;
            realPos = 0;
            lastSignificantChar = 0;
            headerValue = null;
        }
    }
    private class SocketInputBuffer implements InputBuffer {
        @Override
        public int doRead ( ApplicationBufferHandler handler ) throws IOException {
            if ( byteBuffer.position() >= byteBuffer.limit() ) {
                if ( !fill ( true ) ) {
                    return -1;
                }
            }
            int length = byteBuffer.remaining();
            handler.setByteBuffer ( byteBuffer.duplicate() );
            byteBuffer.position ( byteBuffer.limit() );
            return length;
        }
    }
    @Override
    public void setByteBuffer ( ByteBuffer buffer ) {
        byteBuffer = buffer;
    }
    @Override
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
    @Override
    public void expand ( int size ) {
        if ( byteBuffer.capacity() >= size ) {
            byteBuffer.limit ( size );
        }
        ByteBuffer temp = ByteBuffer.allocate ( size );
        temp.put ( byteBuffer );
        byteBuffer = temp;
        byteBuffer.mark();
        temp = null;
    }
}
