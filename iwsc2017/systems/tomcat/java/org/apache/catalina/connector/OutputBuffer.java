package org.apache.catalina.connector;
import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.C2BConverter;
import org.apache.tomcat.util.res.StringManager;
public class OutputBuffer extends Writer {
    private static final StringManager sm = StringManager.getManager ( OutputBuffer.class );
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    private final Map<Charset, C2BConverter> encoders = new HashMap<>();
    private ByteBuffer bb;
    private final CharBuffer cb;
    private boolean initial = true;
    private long bytesWritten = 0;
    private long charsWritten = 0;
    private volatile boolean closed = false;
    private boolean doFlush = false;
    private String enc;
    protected C2BConverter conv;
    private Response coyoteResponse;
    private volatile boolean suspended = false;
    public OutputBuffer() {
        this ( DEFAULT_BUFFER_SIZE );
    }
    public OutputBuffer ( int size ) {
        bb = ByteBuffer.allocate ( size );
        clear ( bb );
        cb = CharBuffer.allocate ( size );
        clear ( cb );
    }
    public void setResponse ( Response coyoteResponse ) {
        this.coyoteResponse = coyoteResponse;
    }
    public boolean isSuspended() {
        return this.suspended;
    }
    public void setSuspended ( boolean suspended ) {
        this.suspended = suspended;
    }
    public boolean isClosed() {
        return this.closed;
    }
    public void recycle() {
        initial = true;
        bytesWritten = 0;
        charsWritten = 0;
        clear ( bb );
        clear ( cb );
        closed = false;
        suspended = false;
        doFlush = false;
        if ( conv != null ) {
            conv.recycle();
            conv = null;
        }
        enc = null;
    }
    @Override
    public void close() throws IOException {
        if ( closed ) {
            return;
        }
        if ( suspended ) {
            return;
        }
        if ( cb.remaining() > 0 ) {
            flushCharBuffer();
        }
        if ( ( !coyoteResponse.isCommitted() ) && ( coyoteResponse.getContentLengthLong() == -1 )
                && !coyoteResponse.getRequest().method().equals ( "HEAD" ) ) {
            if ( !coyoteResponse.isCommitted() ) {
                coyoteResponse.setContentLength ( bb.remaining() );
            }
        }
        if ( coyoteResponse.getStatus() == HttpServletResponse.SC_SWITCHING_PROTOCOLS ) {
            doFlush ( true );
        } else {
            doFlush ( false );
        }
        closed = true;
        Request req = ( Request ) coyoteResponse.getRequest().getNote ( CoyoteAdapter.ADAPTER_NOTES );
        req.inputBuffer.close();
        coyoteResponse.action ( ActionCode.CLOSE, null );
    }
    @Override
    public void flush() throws IOException {
        doFlush ( true );
    }
    protected void doFlush ( boolean realFlush ) throws IOException {
        if ( suspended ) {
            return;
        }
        try {
            doFlush = true;
            if ( initial ) {
                coyoteResponse.sendHeaders();
                initial = false;
            }
            if ( cb.remaining() > 0 ) {
                flushCharBuffer();
            }
            if ( bb.remaining() > 0 ) {
                flushByteBuffer();
            }
        } finally {
            doFlush = false;
        }
        if ( realFlush ) {
            coyoteResponse.action ( ActionCode.CLIENT_FLUSH, null );
            if ( coyoteResponse.isExceptionPresent() ) {
                throw new ClientAbortException ( coyoteResponse.getErrorException() );
            }
        }
    }
    public void realWriteBytes ( ByteBuffer buf ) throws IOException {
        if ( closed ) {
            return;
        }
        if ( coyoteResponse == null ) {
            return;
        }
        if ( buf.remaining() > 0 ) {
            try {
                coyoteResponse.doWrite ( buf );
            } catch ( IOException e ) {
                throw new ClientAbortException ( e );
            }
        }
    }
    public void write ( byte b[], int off, int len ) throws IOException {
        if ( suspended ) {
            return;
        }
        writeBytes ( b, off, len );
    }
    public void write ( ByteBuffer from ) throws IOException {
        if ( suspended ) {
            return;
        }
        writeBytes ( from );
    }
    private void writeBytes ( byte b[], int off, int len ) throws IOException {
        if ( closed ) {
            return;
        }
        append ( b, off, len );
        bytesWritten += len;
        if ( doFlush ) {
            flushByteBuffer();
        }
    }
    private void writeBytes ( ByteBuffer from ) throws IOException {
        if ( closed ) {
            return;
        }
        append ( from );
        bytesWritten += from.remaining();
        if ( doFlush ) {
            flushByteBuffer();
        }
    }
    public void writeByte ( int b ) throws IOException {
        if ( suspended ) {
            return;
        }
        if ( isFull ( bb ) ) {
            flushByteBuffer();
        }
        transfer ( ( byte ) b, bb );
        bytesWritten++;
    }
    public void realWriteChars ( CharBuffer from ) throws IOException {
        while ( from.remaining() > 0 ) {
            conv.convert ( from, bb );
            if ( bb.remaining() == 0 ) {
                break;
            }
            if ( from.remaining() > 0 ) {
                flushByteBuffer();
            }
        }
    }
    @Override
    public void write ( int c ) throws IOException {
        if ( suspended ) {
            return;
        }
        if ( isFull ( cb ) ) {
            flushCharBuffer();
        }
        transfer ( ( char ) c, cb );
        charsWritten++;
    }
    @Override
    public void write ( char c[] ) throws IOException {
        if ( suspended ) {
            return;
        }
        write ( c, 0, c.length );
    }
    @Override
    public void write ( char c[], int off, int len ) throws IOException {
        if ( suspended ) {
            return;
        }
        append ( c, off, len );
        charsWritten += len;
    }
    @Override
    public void write ( String s, int off, int len ) throws IOException {
        if ( suspended ) {
            return;
        }
        if ( s == null ) {
            throw new NullPointerException ( sm.getString ( "outputBuffer.writeNull" ) );
        }
        int sOff = off;
        int sEnd = off + len;
        while ( sOff < sEnd ) {
            int n = transfer ( s, sOff, sEnd - sOff, cb );
            sOff += n;
            if ( isFull ( cb ) ) {
                flushCharBuffer();
            }
        }
        charsWritten += len;
    }
    @Override
    public void write ( String s ) throws IOException {
        if ( suspended ) {
            return;
        }
        if ( s == null ) {
            s = "null";
        }
        write ( s, 0, s.length() );
    }
    public void setEncoding ( String s ) {
        enc = s;
    }
    public void checkConverter() throws IOException {
        if ( conv == null ) {
            setConverter();
        }
    }
    private void setConverter() throws IOException {
        if ( coyoteResponse != null ) {
            enc = coyoteResponse.getCharacterEncoding();
        }
        if ( enc == null ) {
            enc = org.apache.coyote.Constants.DEFAULT_CHARACTER_ENCODING;
        }
        final Charset charset = getCharset ( enc );
        conv = encoders.get ( charset );
        if ( conv == null ) {
            conv = createConverter ( charset );
            encoders.put ( charset, conv );
        }
    }
    private static Charset getCharset ( final String encoding ) throws IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                return AccessController.doPrivileged ( new PrivilegedExceptionAction<Charset>() {
                    @Override
                    public Charset run() throws IOException {
                        return B2CConverter.getCharset ( encoding );
                    }
                } );
            } catch ( PrivilegedActionException ex ) {
                Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new IOException ( ex );
                }
            }
        } else {
            return B2CConverter.getCharset ( encoding );
        }
    }
    private static C2BConverter createConverter ( final Charset charset ) throws IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                return AccessController.doPrivileged ( new PrivilegedExceptionAction<C2BConverter>() {
                    @Override
                    public C2BConverter run() throws IOException {
                        return new C2BConverter ( charset );
                    }
                } );
            } catch ( PrivilegedActionException ex ) {
                Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new IOException ( ex );
                }
            }
        } else {
            return new C2BConverter ( charset );
        }
    }
    public long getContentWritten() {
        return bytesWritten + charsWritten;
    }
    public boolean isNew() {
        return ( bytesWritten == 0 ) && ( charsWritten == 0 );
    }
    public void setBufferSize ( int size ) {
        if ( size > bb.capacity() ) {
            bb = ByteBuffer.allocate ( size );
            clear ( bb );
        }
    }
    public void reset() {
        reset ( false );
    }
    public void reset ( boolean resetWriterStreamFlags ) {
        clear ( bb );
        clear ( cb );
        bytesWritten = 0;
        charsWritten = 0;
        if ( resetWriterStreamFlags ) {
            if ( conv != null ) {
                conv.recycle();
            }
            conv = null;
            enc = null;
        }
        initial = true;
    }
    public int getBufferSize() {
        return bb.capacity();
    }
    public boolean isReady() {
        return coyoteResponse.isReady();
    }
    public void setWriteListener ( WriteListener listener ) {
        coyoteResponse.setWriteListener ( listener );
    }
    public boolean isBlocking() {
        return coyoteResponse.getWriteListener() == null;
    }
    public void checkRegisterForWrite() {
        coyoteResponse.checkRegisterForWrite();
    }
    public void append ( byte src[], int off, int len ) throws IOException {
        if ( bb.remaining() == 0 ) {
            appendByteArray ( src, off, len );
        } else {
            int n = transfer ( src, off, len, bb );
            len = len - n;
            off = off + n;
            if ( isFull ( bb ) ) {
                flushByteBuffer();
                appendByteArray ( src, off, len );
            }
        }
    }
    public void append ( char src[], int off, int len ) throws IOException {
        if ( len <= cb.capacity() - cb.limit() ) {
            transfer ( src, off, len, cb );
            return;
        }
        if ( len + cb.limit() < 2 * cb.capacity() ) {
            int n = transfer ( src, off, len, cb );
            flushCharBuffer();
            transfer ( src, off + n, len - n, cb );
        } else {
            flushCharBuffer();
            realWriteChars ( CharBuffer.wrap ( src, off, len ) );
        }
    }
    public void append ( ByteBuffer from ) throws IOException {
        if ( bb.remaining() == 0 ) {
            appendByteBuffer ( from );
        } else {
            transfer ( from, bb );
            if ( isFull ( bb ) ) {
                flushByteBuffer();
                appendByteBuffer ( from );
            }
        }
    }
    private void appendByteArray ( byte src[], int off, int len ) throws IOException {
        if ( len == 0 ) {
            return;
        }
        int limit = bb.capacity();
        while ( len >= limit ) {
            realWriteBytes ( ByteBuffer.wrap ( src, off, limit ) );
            len = len - limit;
            off = off + limit;
        }
        if ( len > 0 ) {
            transfer ( src, off, len, bb );
        }
    }
    private void appendByteBuffer ( ByteBuffer from ) throws IOException {
        if ( from.remaining() == 0 ) {
            return;
        }
        int limit = bb.capacity();
        int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            from.limit ( from.position() + limit );
            realWriteBytes ( from.slice() );
            from.position ( from.limit() );
            from.limit ( fromLimit );
        }
        if ( from.remaining() > 0 ) {
            transfer ( from, bb );
        }
    }
    private void flushByteBuffer() throws IOException {
        realWriteBytes ( bb.slice() );
        clear ( bb );
    }
    private void flushCharBuffer() throws IOException {
        realWriteChars ( cb.slice() );
        clear ( cb );
    }
    private void transfer ( byte b, ByteBuffer to ) {
        toWriteMode ( to );
        to.put ( b );
        toReadMode ( to );
    }
    private void transfer ( char b, CharBuffer to ) {
        toWriteMode ( to );
        to.put ( b );
        toReadMode ( to );
    }
    private int transfer ( byte[] buf, int off, int len, ByteBuffer to ) {
        toWriteMode ( to );
        int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( buf, off, max );
        }
        toReadMode ( to );
        return max;
    }
    private int transfer ( char[] buf, int off, int len, CharBuffer to ) {
        toWriteMode ( to );
        int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( buf, off, max );
        }
        toReadMode ( to );
        return max;
    }
    private int transfer ( String s, int off, int len, CharBuffer to ) {
        toWriteMode ( to );
        int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( s, off, off + max );
        }
        toReadMode ( to );
        return max;
    }
    private void transfer ( ByteBuffer from, ByteBuffer to ) {
        toWriteMode ( to );
        int max = Math.min ( from.remaining(), to.remaining() );
        if ( max > 0 ) {
            int fromLimit = from.limit();
            from.limit ( from.position() + max );
            to.put ( from );
            from.limit ( fromLimit );
        }
        toReadMode ( to );
    }
    private void clear ( Buffer buffer ) {
        buffer.rewind().limit ( 0 );
    }
    private boolean isFull ( Buffer buffer ) {
        return buffer.limit() == buffer.capacity();
    }
    private void toReadMode ( Buffer buffer ) {
        buffer.limit ( buffer.position() )
        .reset();
    }
    private void toWriteMode ( Buffer buffer ) {
        buffer.mark()
        .position ( buffer.limit() )
        .limit ( buffer.capacity() );
    }
}
