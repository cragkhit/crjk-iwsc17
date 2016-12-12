package org.apache.catalina.connector;
import javax.servlet.WriteListener;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import org.apache.tomcat.util.buf.B2CConverter;
import java.security.PrivilegedExceptionAction;
import org.apache.catalina.Globals;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import java.nio.Buffer;
import java.util.HashMap;
import org.apache.coyote.Response;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.buf.C2BConverter;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import java.io.Writer;
public class OutputBuffer extends Writer {
    private static final StringManager sm;
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private final Map<Charset, C2BConverter> encoders;
    private ByteBuffer bb;
    private final CharBuffer cb;
    private boolean initial;
    private long bytesWritten;
    private long charsWritten;
    private volatile boolean closed;
    private boolean doFlush;
    private String enc;
    protected C2BConverter conv;
    private Response coyoteResponse;
    private volatile boolean suspended;
    public OutputBuffer() {
        this ( 8192 );
    }
    public OutputBuffer ( final int size ) {
        this.encoders = new HashMap<Charset, C2BConverter>();
        this.initial = true;
        this.bytesWritten = 0L;
        this.charsWritten = 0L;
        this.closed = false;
        this.doFlush = false;
        this.suspended = false;
        this.clear ( this.bb = ByteBuffer.allocate ( size ) );
        this.clear ( this.cb = CharBuffer.allocate ( size ) );
    }
    public void setResponse ( final Response coyoteResponse ) {
        this.coyoteResponse = coyoteResponse;
    }
    public boolean isSuspended() {
        return this.suspended;
    }
    public void setSuspended ( final boolean suspended ) {
        this.suspended = suspended;
    }
    public boolean isClosed() {
        return this.closed;
    }
    public void recycle() {
        this.initial = true;
        this.bytesWritten = 0L;
        this.charsWritten = 0L;
        this.clear ( this.bb );
        this.clear ( this.cb );
        this.closed = false;
        this.suspended = false;
        this.doFlush = false;
        if ( this.conv != null ) {
            this.conv.recycle();
            this.conv = null;
        }
        this.enc = null;
    }
    @Override
    public void close() throws IOException {
        if ( this.closed ) {
            return;
        }
        if ( this.suspended ) {
            return;
        }
        if ( this.cb.remaining() > 0 ) {
            this.flushCharBuffer();
        }
        if ( !this.coyoteResponse.isCommitted() && this.coyoteResponse.getContentLengthLong() == -1L && !this.coyoteResponse.getRequest().method().equals ( "HEAD" ) && !this.coyoteResponse.isCommitted() ) {
            this.coyoteResponse.setContentLength ( this.bb.remaining() );
        }
        if ( this.coyoteResponse.getStatus() == 101 ) {
            this.doFlush ( true );
        } else {
            this.doFlush ( false );
        }
        this.closed = true;
        final Request req = ( Request ) this.coyoteResponse.getRequest().getNote ( 1 );
        req.inputBuffer.close();
        this.coyoteResponse.action ( ActionCode.CLOSE, null );
    }
    @Override
    public void flush() throws IOException {
        this.doFlush ( true );
    }
    protected void doFlush ( final boolean realFlush ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        try {
            this.doFlush = true;
            if ( this.initial ) {
                this.coyoteResponse.sendHeaders();
                this.initial = false;
            }
            if ( this.cb.remaining() > 0 ) {
                this.flushCharBuffer();
            }
            if ( this.bb.remaining() > 0 ) {
                this.flushByteBuffer();
            }
        } finally {
            this.doFlush = false;
        }
        if ( realFlush ) {
            this.coyoteResponse.action ( ActionCode.CLIENT_FLUSH, null );
            if ( this.coyoteResponse.isExceptionPresent() ) {
                throw new ClientAbortException ( this.coyoteResponse.getErrorException() );
            }
        }
    }
    public void realWriteBytes ( final ByteBuffer buf ) throws IOException {
        if ( this.closed ) {
            return;
        }
        if ( this.coyoteResponse == null ) {
            return;
        }
        if ( buf.remaining() > 0 ) {
            try {
                this.coyoteResponse.doWrite ( buf );
            } catch ( IOException e ) {
                throw new ClientAbortException ( e );
            }
        }
    }
    public void write ( final byte[] b, final int off, final int len ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        this.writeBytes ( b, off, len );
    }
    public void write ( final ByteBuffer from ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        this.writeBytes ( from );
    }
    private void writeBytes ( final byte[] b, final int off, final int len ) throws IOException {
        if ( this.closed ) {
            return;
        }
        this.append ( b, off, len );
        this.bytesWritten += len;
        if ( this.doFlush ) {
            this.flushByteBuffer();
        }
    }
    private void writeBytes ( final ByteBuffer from ) throws IOException {
        if ( this.closed ) {
            return;
        }
        this.append ( from );
        this.bytesWritten += from.remaining();
        if ( this.doFlush ) {
            this.flushByteBuffer();
        }
    }
    public void writeByte ( final int b ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        if ( this.isFull ( this.bb ) ) {
            this.flushByteBuffer();
        }
        this.transfer ( ( byte ) b, this.bb );
        ++this.bytesWritten;
    }
    public void realWriteChars ( final CharBuffer from ) throws IOException {
        while ( from.remaining() > 0 ) {
            this.conv.convert ( from, this.bb );
            if ( this.bb.remaining() == 0 ) {
                break;
            }
            if ( from.remaining() <= 0 ) {
                continue;
            }
            this.flushByteBuffer();
        }
    }
    @Override
    public void write ( final int c ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        if ( this.isFull ( this.cb ) ) {
            this.flushCharBuffer();
        }
        this.transfer ( ( char ) c, this.cb );
        ++this.charsWritten;
    }
    @Override
    public void write ( final char[] c ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        this.write ( c, 0, c.length );
    }
    @Override
    public void write ( final char[] c, final int off, final int len ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        this.append ( c, off, len );
        this.charsWritten += len;
    }
    @Override
    public void write ( final String s, final int off, final int len ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        if ( s == null ) {
            throw new NullPointerException ( OutputBuffer.sm.getString ( "outputBuffer.writeNull" ) );
        }
        int sOff = off;
        final int sEnd = off + len;
        while ( sOff < sEnd ) {
            final int n = this.transfer ( s, sOff, sEnd - sOff, this.cb );
            sOff += n;
            if ( this.isFull ( this.cb ) ) {
                this.flushCharBuffer();
            }
        }
        this.charsWritten += len;
    }
    @Override
    public void write ( String s ) throws IOException {
        if ( this.suspended ) {
            return;
        }
        if ( s == null ) {
            s = "null";
        }
        this.write ( s, 0, s.length() );
    }
    public void setEncoding ( final String s ) {
        this.enc = s;
    }
    public void checkConverter() throws IOException {
        if ( this.conv == null ) {
            this.setConverter();
        }
    }
    private void setConverter() throws IOException {
        if ( this.coyoteResponse != null ) {
            this.enc = this.coyoteResponse.getCharacterEncoding();
        }
        if ( this.enc == null ) {
            this.enc = "ISO-8859-1";
        }
        final Charset charset = getCharset ( this.enc );
        this.conv = this.encoders.get ( charset );
        if ( this.conv == null ) {
            this.conv = createConverter ( charset );
            this.encoders.put ( charset, this.conv );
        }
    }
    private static Charset getCharset ( final String encoding ) throws IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                return AccessController.doPrivileged ( ( PrivilegedExceptionAction<Charset> ) new PrivilegedExceptionAction<Charset>() {
                    @Override
                    public Charset run() throws IOException {
                        return B2CConverter.getCharset ( encoding );
                    }
                } );
            } catch ( PrivilegedActionException ex ) {
                final Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new IOException ( ex );
            }
        }
        return B2CConverter.getCharset ( encoding );
    }
    private static C2BConverter createConverter ( final Charset charset ) throws IOException {
        if ( Globals.IS_SECURITY_ENABLED ) {
            try {
                return AccessController.doPrivileged ( ( PrivilegedExceptionAction<C2BConverter> ) new PrivilegedExceptionAction<C2BConverter>() {
                    @Override
                    public C2BConverter run() throws IOException {
                        return new C2BConverter ( charset );
                    }
                } );
            } catch ( PrivilegedActionException ex ) {
                final Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new IOException ( ex );
            }
        }
        return new C2BConverter ( charset );
    }
    public long getContentWritten() {
        return this.bytesWritten + this.charsWritten;
    }
    public boolean isNew() {
        return this.bytesWritten == 0L && this.charsWritten == 0L;
    }
    public void setBufferSize ( final int size ) {
        if ( size > this.bb.capacity() ) {
            this.clear ( this.bb = ByteBuffer.allocate ( size ) );
        }
    }
    public void reset() {
        this.reset ( false );
    }
    public void reset ( final boolean resetWriterStreamFlags ) {
        this.clear ( this.bb );
        this.clear ( this.cb );
        this.bytesWritten = 0L;
        this.charsWritten = 0L;
        if ( resetWriterStreamFlags ) {
            if ( this.conv != null ) {
                this.conv.recycle();
            }
            this.conv = null;
            this.enc = null;
        }
        this.initial = true;
    }
    public int getBufferSize() {
        return this.bb.capacity();
    }
    public boolean isReady() {
        return this.coyoteResponse.isReady();
    }
    public void setWriteListener ( final WriteListener listener ) {
        this.coyoteResponse.setWriteListener ( listener );
    }
    public boolean isBlocking() {
        return this.coyoteResponse.getWriteListener() == null;
    }
    public void checkRegisterForWrite() {
        this.coyoteResponse.checkRegisterForWrite();
    }
    public void append ( final byte[] src, int off, int len ) throws IOException {
        if ( this.bb.remaining() == 0 ) {
            this.appendByteArray ( src, off, len );
        } else {
            final int n = this.transfer ( src, off, len, this.bb );
            len -= n;
            off += n;
            if ( this.isFull ( this.bb ) ) {
                this.flushByteBuffer();
                this.appendByteArray ( src, off, len );
            }
        }
    }
    public void append ( final char[] src, final int off, final int len ) throws IOException {
        if ( len <= this.cb.capacity() - this.cb.limit() ) {
            this.transfer ( src, off, len, this.cb );
            return;
        }
        if ( len + this.cb.limit() < 2 * this.cb.capacity() ) {
            final int n = this.transfer ( src, off, len, this.cb );
            this.flushCharBuffer();
            this.transfer ( src, off + n, len - n, this.cb );
        } else {
            this.flushCharBuffer();
            this.realWriteChars ( CharBuffer.wrap ( src, off, len ) );
        }
    }
    public void append ( final ByteBuffer from ) throws IOException {
        if ( this.bb.remaining() == 0 ) {
            this.appendByteBuffer ( from );
        } else {
            this.transfer ( from, this.bb );
            if ( this.isFull ( this.bb ) ) {
                this.flushByteBuffer();
                this.appendByteBuffer ( from );
            }
        }
    }
    private void appendByteArray ( final byte[] src, int off, int len ) throws IOException {
        if ( len == 0 ) {
            return;
        }
        for ( int limit = this.bb.capacity(); len >= limit; len -= limit, off += limit ) {
            this.realWriteBytes ( ByteBuffer.wrap ( src, off, limit ) );
        }
        if ( len > 0 ) {
            this.transfer ( src, off, len, this.bb );
        }
    }
    private void appendByteBuffer ( final ByteBuffer from ) throws IOException {
        if ( from.remaining() == 0 ) {
            return;
        }
        final int limit = this.bb.capacity();
        final int fromLimit = from.limit();
        while ( from.remaining() >= limit ) {
            from.limit ( from.position() + limit );
            this.realWriteBytes ( from.slice() );
            from.position ( from.limit() );
            from.limit ( fromLimit );
        }
        if ( from.remaining() > 0 ) {
            this.transfer ( from, this.bb );
        }
    }
    private void flushByteBuffer() throws IOException {
        this.realWriteBytes ( this.bb.slice() );
        this.clear ( this.bb );
    }
    private void flushCharBuffer() throws IOException {
        this.realWriteChars ( this.cb.slice() );
        this.clear ( this.cb );
    }
    private void transfer ( final byte b, final ByteBuffer to ) {
        this.toWriteMode ( to );
        to.put ( b );
        this.toReadMode ( to );
    }
    private void transfer ( final char b, final CharBuffer to ) {
        this.toWriteMode ( to );
        to.put ( b );
        this.toReadMode ( to );
    }
    private int transfer ( final byte[] buf, final int off, final int len, final ByteBuffer to ) {
        this.toWriteMode ( to );
        final int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( buf, off, max );
        }
        this.toReadMode ( to );
        return max;
    }
    private int transfer ( final char[] buf, final int off, final int len, final CharBuffer to ) {
        this.toWriteMode ( to );
        final int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( buf, off, max );
        }
        this.toReadMode ( to );
        return max;
    }
    private int transfer ( final String s, final int off, final int len, final CharBuffer to ) {
        this.toWriteMode ( to );
        final int max = Math.min ( len, to.remaining() );
        if ( max > 0 ) {
            to.put ( s, off, off + max );
        }
        this.toReadMode ( to );
        return max;
    }
    private void transfer ( final ByteBuffer from, final ByteBuffer to ) {
        this.toWriteMode ( to );
        final int max = Math.min ( from.remaining(), to.remaining() );
        if ( max > 0 ) {
            final int fromLimit = from.limit();
            from.limit ( from.position() + max );
            to.put ( from );
            from.limit ( fromLimit );
        }
        this.toReadMode ( to );
    }
    private void clear ( final Buffer buffer ) {
        buffer.rewind().limit ( 0 );
    }
    private boolean isFull ( final Buffer buffer ) {
        return buffer.limit() == buffer.capacity();
    }
    private void toReadMode ( final Buffer buffer ) {
        buffer.limit ( buffer.position() ).reset();
    }
    private void toWriteMode ( final Buffer buffer ) {
        buffer.mark().position ( buffer.limit() ).limit ( buffer.capacity() );
    }
    static {
        sm = StringManager.getManager ( OutputBuffer.class );
    }
}
