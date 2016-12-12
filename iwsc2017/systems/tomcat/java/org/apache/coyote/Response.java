package org.apache.coyote;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.WriteListener;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.parser.MediaType;
import org.apache.tomcat.util.res.StringManager;
public final class Response {
    private static final StringManager sm = StringManager.getManager ( Response.class );
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    int status = 200;
    String message = null;
    final MimeHeaders headers = new MimeHeaders();
    OutputBuffer outputBuffer;
    final Object notes[] = new Object[Constants.MAX_NOTES];
    volatile boolean commited = false;
    volatile ActionHook hook;
    String contentType = null;
    String contentLanguage = null;
    String characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
    long contentLength = -1;
    private Locale locale = DEFAULT_LOCALE;
    private long contentWritten = 0;
    private long commitTime = -1;
    Exception errorException = null;
    boolean charsetSet = false;
    Request req;
    public Request getRequest() {
        return req;
    }
    public void setRequest ( Request req ) {
        this.req = req;
    }
    public void setOutputBuffer ( OutputBuffer outputBuffer ) {
        this.outputBuffer = outputBuffer;
    }
    public MimeHeaders getMimeHeaders() {
        return headers;
    }
    protected void setHook ( ActionHook hook ) {
        this.hook = hook;
    }
    public final void setNote ( int pos, Object value ) {
        notes[pos] = value;
    }
    public final Object getNote ( int pos ) {
        return notes[pos];
    }
    public void action ( ActionCode actionCode, Object param ) {
        if ( hook != null ) {
            if ( param == null ) {
                hook.action ( actionCode, this );
            } else {
                hook.action ( actionCode, param );
            }
        }
    }
    public int getStatus() {
        return status;
    }
    public void setStatus ( int status ) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage ( String message ) {
        this.message = message;
    }
    public boolean isCommitted() {
        return commited;
    }
    public void setCommitted ( boolean v ) {
        if ( v && !this.commited ) {
            this.commitTime = System.currentTimeMillis();
        }
        this.commited = v;
    }
    public long getCommitTime() {
        return commitTime;
    }
    public void setErrorException ( Exception ex ) {
        errorException = ex;
    }
    public Exception getErrorException() {
        return errorException;
    }
    public boolean isExceptionPresent() {
        return ( errorException != null );
    }
    public void reset() throws IllegalStateException {
        if ( commited ) {
            throw new IllegalStateException();
        }
        recycle();
    }
    public boolean containsHeader ( String name ) {
        return headers.getHeader ( name ) != null;
    }
    public void setHeader ( String name, String value ) {
        char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( checkSpecialHeader ( name, value ) ) {
                return;
            }
        }
        headers.setValue ( name ).setString ( value );
    }
    public void addHeader ( String name, String value ) {
        addHeader ( name, value, null );
    }
    public void addHeader ( String name, String value, Charset charset ) {
        char cc = name.charAt ( 0 );
        if ( cc == 'C' || cc == 'c' ) {
            if ( checkSpecialHeader ( name, value ) ) {
                return;
            }
        }
        MessageBytes mb = headers.addValue ( name );
        if ( charset != null ) {
            mb.setCharset ( charset );
        }
        mb.setString ( value );
    }
    private boolean checkSpecialHeader ( String name, String value ) {
        if ( name.equalsIgnoreCase ( "Content-Type" ) ) {
            setContentType ( value );
            return true;
        }
        if ( name.equalsIgnoreCase ( "Content-Length" ) ) {
            try {
                long cL = Long.parseLong ( value );
                setContentLength ( cL );
                return true;
            } catch ( NumberFormatException ex ) {
                return false;
            }
        }
        return false;
    }
    public void sendHeaders() {
        action ( ActionCode.COMMIT, this );
        setCommitted ( true );
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale ( Locale locale ) {
        if ( locale == null ) {
            return;
        }
        this.locale = locale;
        contentLanguage = locale.toLanguageTag();
    }
    public String getContentLanguage() {
        return contentLanguage;
    }
    public void setCharacterEncoding ( String charset ) {
        if ( isCommitted() ) {
            return;
        }
        if ( charset == null ) {
            return;
        }
        characterEncoding = charset;
        charsetSet = true;
    }
    public String getCharacterEncoding() {
        return characterEncoding;
    }
    public void setContentType ( String type ) {
        if ( type == null ) {
            this.contentType = null;
            return;
        }
        MediaType m = null;
        try {
            m = MediaType.parseMediaType ( new StringReader ( type ) );
        } catch ( IOException e ) {
        }
        if ( m == null ) {
            this.contentType = type;
            return;
        }
        this.contentType = m.toStringNoCharset();
        String charsetValue = m.getCharset();
        if ( charsetValue != null ) {
            charsetValue = charsetValue.trim();
            if ( charsetValue.length() > 0 ) {
                charsetSet = true;
                this.characterEncoding = charsetValue;
            }
        }
    }
    public void setContentTypeNoCharset ( String type ) {
        this.contentType = type;
    }
    public String getContentType() {
        String ret = contentType;
        if ( ret != null
                && characterEncoding != null
                && charsetSet ) {
            ret = ret + ";charset=" + characterEncoding;
        }
        return ret;
    }
    public void setContentLength ( long contentLength ) {
        this.contentLength = contentLength;
    }
    public int getContentLength() {
        long length = getContentLengthLong();
        if ( length < Integer.MAX_VALUE ) {
            return ( int ) length;
        }
        return -1;
    }
    public long getContentLengthLong() {
        return contentLength;
    }
    public void doWrite ( ByteBuffer chunk ) throws IOException {
        int len = chunk.remaining();
        outputBuffer.doWrite ( chunk );
        contentWritten += len - chunk.remaining();
    }
    public void recycle() {
        contentType = null;
        contentLanguage = null;
        locale = DEFAULT_LOCALE;
        characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
        charsetSet = false;
        contentLength = -1;
        status = 200;
        message = null;
        commited = false;
        commitTime = -1;
        errorException = null;
        headers.clear();
        listener = null;
        fireListener = false;
        registeredForWrite = false;
        contentWritten = 0;
    }
    public long getContentWritten() {
        return contentWritten;
    }
    public long getBytesWritten ( boolean flush ) {
        if ( flush ) {
            action ( ActionCode.CLIENT_FLUSH, this );
        }
        return outputBuffer.getBytesWritten();
    }
    volatile WriteListener listener;
    private boolean fireListener = false;
    private boolean registeredForWrite = false;
    private final Object nonBlockingStateLock = new Object();
    public WriteListener getWriteListener() {
        return listener;
    }
    public void setWriteListener ( WriteListener listener ) {
        if ( listener == null ) {
            throw new NullPointerException (
                sm.getString ( "response.nullWriteListener" ) );
        }
        if ( getWriteListener() != null ) {
            throw new IllegalStateException (
                sm.getString ( "response.writeListenerSet" ) );
        }
        AtomicBoolean result = new AtomicBoolean ( false );
        action ( ActionCode.ASYNC_IS_ASYNC, result );
        if ( !result.get() ) {
            throw new IllegalStateException (
                sm.getString ( "response.notAsync" ) );
        }
        this.listener = listener;
        if ( isReady() ) {
            synchronized ( nonBlockingStateLock ) {
                registeredForWrite = true;
                fireListener = true;
            }
            action ( ActionCode.DISPATCH_WRITE, null );
            if ( !ContainerThreadMarker.isContainerThread() ) {
                action ( ActionCode.DISPATCH_EXECUTE, null );
            }
        }
    }
    public boolean isReady() {
        if ( listener == null ) {
            throw new IllegalStateException ( sm.getString ( "response.notNonBlocking" ) );
        }
        boolean ready = false;
        synchronized ( nonBlockingStateLock ) {
            if ( registeredForWrite ) {
                fireListener = true;
                return false;
            }
            ready = checkRegisterForWrite();
            fireListener = !ready;
        }
        return ready;
    }
    public boolean checkRegisterForWrite() {
        AtomicBoolean ready = new AtomicBoolean ( false );
        synchronized ( nonBlockingStateLock ) {
            if ( !registeredForWrite ) {
                action ( ActionCode.NB_WRITE_INTEREST, ready );
                registeredForWrite = !ready.get();
            }
        }
        return ready.get();
    }
    public void onWritePossible() throws IOException {
        boolean fire = false;
        synchronized ( nonBlockingStateLock ) {
            registeredForWrite = false;
            if ( fireListener ) {
                fireListener = false;
                fire = true;
            }
        }
        if ( fire ) {
            listener.onWritePossible();
        }
    }
}
