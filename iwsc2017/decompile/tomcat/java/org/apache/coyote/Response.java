package org.apache.coyote;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.tomcat.util.http.parser.MediaType;
import java.io.StringReader;
import org.apache.tomcat.util.buf.MessageBytes;
import java.nio.charset.Charset;
import javax.servlet.WriteListener;
import org.apache.tomcat.util.http.MimeHeaders;
import java.util.Locale;
import org.apache.tomcat.util.res.StringManager;
public final class Response {
    private static final StringManager sm;
    private static final Locale DEFAULT_LOCALE;
    int status;
    String message;
    final MimeHeaders headers;
    OutputBuffer outputBuffer;
    final Object[] notes;
    volatile boolean commited;
    volatile ActionHook hook;
    String contentType;
    String contentLanguage;
    String characterEncoding;
    long contentLength;
    private Locale locale;
    private long contentWritten;
    private long commitTime;
    Exception errorException;
    boolean charsetSet;
    Request req;
    volatile WriteListener listener;
    private boolean fireListener;
    private boolean registeredForWrite;
    private final Object nonBlockingStateLock;
    public Response() {
        this.status = 200;
        this.message = null;
        this.headers = new MimeHeaders();
        this.notes = new Object[32];
        this.commited = false;
        this.contentType = null;
        this.contentLanguage = null;
        this.characterEncoding = "ISO-8859-1";
        this.contentLength = -1L;
        this.locale = Response.DEFAULT_LOCALE;
        this.contentWritten = 0L;
        this.commitTime = -1L;
        this.errorException = null;
        this.charsetSet = false;
        this.fireListener = false;
        this.registeredForWrite = false;
        this.nonBlockingStateLock = new Object();
    }
    public Request getRequest() {
        return this.req;
    }
    public void setRequest ( final Request req ) {
        this.req = req;
    }
    public void setOutputBuffer ( final OutputBuffer outputBuffer ) {
        this.outputBuffer = outputBuffer;
    }
    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }
    protected void setHook ( final ActionHook hook ) {
        this.hook = hook;
    }
    public final void setNote ( final int pos, final Object value ) {
        this.notes[pos] = value;
    }
    public final Object getNote ( final int pos ) {
        return this.notes[pos];
    }
    public void action ( final ActionCode actionCode, final Object param ) {
        if ( this.hook != null ) {
            if ( param == null ) {
                this.hook.action ( actionCode, this );
            } else {
                this.hook.action ( actionCode, param );
            }
        }
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus ( final int status ) {
        this.status = status;
    }
    public String getMessage() {
        return this.message;
    }
    public void setMessage ( final String message ) {
        this.message = message;
    }
    public boolean isCommitted() {
        return this.commited;
    }
    public void setCommitted ( final boolean v ) {
        if ( v && !this.commited ) {
            this.commitTime = System.currentTimeMillis();
        }
        this.commited = v;
    }
    public long getCommitTime() {
        return this.commitTime;
    }
    public void setErrorException ( final Exception ex ) {
        this.errorException = ex;
    }
    public Exception getErrorException() {
        return this.errorException;
    }
    public boolean isExceptionPresent() {
        return this.errorException != null;
    }
    public void reset() throws IllegalStateException {
        if ( this.commited ) {
            throw new IllegalStateException();
        }
        this.recycle();
    }
    public boolean containsHeader ( final String name ) {
        return this.headers.getHeader ( name ) != null;
    }
    public void setHeader ( final String name, final String value ) {
        final char cc = name.charAt ( 0 );
        if ( ( cc == 'C' || cc == 'c' ) && this.checkSpecialHeader ( name, value ) ) {
            return;
        }
        this.headers.setValue ( name ).setString ( value );
    }
    public void addHeader ( final String name, final String value ) {
        this.addHeader ( name, value, null );
    }
    public void addHeader ( final String name, final String value, final Charset charset ) {
        final char cc = name.charAt ( 0 );
        if ( ( cc == 'C' || cc == 'c' ) && this.checkSpecialHeader ( name, value ) ) {
            return;
        }
        final MessageBytes mb = this.headers.addValue ( name );
        if ( charset != null ) {
            mb.setCharset ( charset );
        }
        mb.setString ( value );
    }
    private boolean checkSpecialHeader ( final String name, final String value ) {
        if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
            this.setContentType ( value );
            return true;
        }
        if ( name.equalsIgnoreCase ( "Content-Length" ) ) {
            try {
                final long cL = Long.parseLong ( value );
                this.setContentLength ( cL );
                return true;
            } catch ( NumberFormatException ex ) {
                return false;
            }
        }
        return false;
    }
    public void sendHeaders() {
        this.action ( ActionCode.COMMIT, this );
        this.setCommitted ( true );
    }
    public Locale getLocale() {
        return this.locale;
    }
    public void setLocale ( final Locale locale ) {
        if ( locale == null ) {
            return;
        }
        this.locale = locale;
        this.contentLanguage = locale.toLanguageTag();
    }
    public String getContentLanguage() {
        return this.contentLanguage;
    }
    public void setCharacterEncoding ( final String charset ) {
        if ( this.isCommitted() ) {
            return;
        }
        if ( charset == null ) {
            return;
        }
        this.characterEncoding = charset;
        this.charsetSet = true;
    }
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }
    public void setContentType ( final String type ) {
        if ( type == null ) {
            this.contentType = null;
            return;
        }
        MediaType m = null;
        try {
            m = MediaType.parseMediaType ( new StringReader ( type ) );
        } catch ( IOException ex ) {}
        if ( m == null ) {
            this.contentType = type;
            return;
        }
        this.contentType = m.toStringNoCharset();
        String charsetValue = m.getCharset();
        if ( charsetValue != null ) {
            charsetValue = charsetValue.trim();
            if ( charsetValue.length() > 0 ) {
                this.charsetSet = true;
                this.characterEncoding = charsetValue;
            }
        }
    }
    public void setContentTypeNoCharset ( final String type ) {
        this.contentType = type;
    }
    public String getContentType() {
        String ret = this.contentType;
        if ( ret != null && this.characterEncoding != null && this.charsetSet ) {
            ret = ret + ";charset=" + this.characterEncoding;
        }
        return ret;
    }
    public void setContentLength ( final long contentLength ) {
        this.contentLength = contentLength;
    }
    public int getContentLength() {
        final long length = this.getContentLengthLong();
        if ( length < 2147483647L ) {
            return ( int ) length;
        }
        return -1;
    }
    public long getContentLengthLong() {
        return this.contentLength;
    }
    public void doWrite ( final ByteBuffer chunk ) throws IOException {
        final int len = chunk.remaining();
        this.outputBuffer.doWrite ( chunk );
        this.contentWritten += len - chunk.remaining();
    }
    public void recycle() {
        this.contentType = null;
        this.contentLanguage = null;
        this.locale = Response.DEFAULT_LOCALE;
        this.characterEncoding = "ISO-8859-1";
        this.charsetSet = false;
        this.contentLength = -1L;
        this.status = 200;
        this.message = null;
        this.commited = false;
        this.commitTime = -1L;
        this.errorException = null;
        this.headers.clear();
        this.listener = null;
        this.fireListener = false;
        this.registeredForWrite = false;
        this.contentWritten = 0L;
    }
    public long getContentWritten() {
        return this.contentWritten;
    }
    public long getBytesWritten ( final boolean flush ) {
        if ( flush ) {
            this.action ( ActionCode.CLIENT_FLUSH, this );
        }
        return this.outputBuffer.getBytesWritten();
    }
    public WriteListener getWriteListener() {
        return this.listener;
    }
    public void setWriteListener ( final WriteListener listener ) {
        if ( listener == null ) {
            throw new NullPointerException ( Response.sm.getString ( "response.nullWriteListener" ) );
        }
        if ( this.getWriteListener() != null ) {
            throw new IllegalStateException ( Response.sm.getString ( "response.writeListenerSet" ) );
        }
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.action ( ActionCode.ASYNC_IS_ASYNC, result );
        if ( !result.get() ) {
            throw new IllegalStateException ( Response.sm.getString ( "response.notAsync" ) );
        }
        this.listener = listener;
        if ( this.isReady() ) {
            synchronized ( this.nonBlockingStateLock ) {
                this.registeredForWrite = true;
                this.fireListener = true;
            }
            this.action ( ActionCode.DISPATCH_WRITE, null );
            if ( !ContainerThreadMarker.isContainerThread() ) {
                this.action ( ActionCode.DISPATCH_EXECUTE, null );
            }
        }
    }
    public boolean isReady() {
        if ( this.listener == null ) {
            throw new IllegalStateException ( Response.sm.getString ( "response.notNonBlocking" ) );
        }
        boolean ready = false;
        synchronized ( this.nonBlockingStateLock ) {
            if ( this.registeredForWrite ) {
                this.fireListener = true;
                return false;
            }
            ready = this.checkRegisterForWrite();
            this.fireListener = !ready;
        }
        return ready;
    }
    public boolean checkRegisterForWrite() {
        final AtomicBoolean ready = new AtomicBoolean ( false );
        synchronized ( this.nonBlockingStateLock ) {
            if ( !this.registeredForWrite ) {
                this.action ( ActionCode.NB_WRITE_INTEREST, ready );
                this.registeredForWrite = !ready.get();
            }
        }
        return ready.get();
    }
    public void onWritePossible() throws IOException {
        boolean fire = false;
        synchronized ( this.nonBlockingStateLock ) {
            this.registeredForWrite = false;
            if ( this.fireListener ) {
                this.fireListener = false;
                fire = true;
            }
        }
        if ( fire ) {
            this.listener.onWritePossible();
        }
    }
    static {
        sm = StringManager.getManager ( Response.class );
        DEFAULT_LOCALE = Locale.getDefault();
    }
}
