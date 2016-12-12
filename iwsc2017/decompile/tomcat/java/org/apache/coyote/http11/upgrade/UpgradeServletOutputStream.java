package org.apache.coyote.http11.upgrade;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import org.apache.tomcat.util.net.DispatchType;
import org.apache.coyote.ContainerThreadMarker;
import javax.servlet.WriteListener;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.servlet.ServletOutputStream;
public class UpgradeServletOutputStream extends ServletOutputStream {
    private static final Log log;
    private static final StringManager sm;
    private final UpgradeProcessorBase processor;
    private final SocketWrapperBase<?> socketWrapper;
    private final Object registeredLock;
    private final Object writeLock;
    private volatile boolean flushing;
    private volatile boolean closed;
    private volatile WriteListener listener;
    private boolean registered;
    public UpgradeServletOutputStream ( final UpgradeProcessorBase processor, final SocketWrapperBase<?> socketWrapper ) {
        this.registeredLock = new Object();
        this.writeLock = new Object();
        this.flushing = false;
        this.closed = false;
        this.listener = null;
        this.registered = false;
        this.processor = processor;
        this.socketWrapper = socketWrapper;
    }
    public final boolean isReady() {
        if ( this.listener == null ) {
            throw new IllegalStateException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.canWrite.ise" ) );
        }
        if ( this.closed ) {
            return false;
        }
        synchronized ( this.registeredLock ) {
            if ( this.flushing ) {
                this.registered = true;
                return false;
            }
            if ( this.registered ) {
                return false;
            }
            final boolean result = this.socketWrapper.isReadyForWrite();
            this.registered = !result;
            return result;
        }
    }
    public final void setWriteListener ( final WriteListener listener ) {
        if ( listener == null ) {
            throw new IllegalArgumentException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.writeListener.null" ) );
        }
        if ( this.listener != null ) {
            throw new IllegalArgumentException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.writeListener.set" ) );
        }
        if ( this.closed ) {
            throw new IllegalStateException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.write.closed" ) );
        }
        synchronized ( this.registeredLock ) {
            this.registered = true;
            if ( ContainerThreadMarker.isContainerThread() ) {
                this.processor.addDispatch ( DispatchType.NON_BLOCKING_WRITE );
            } else {
                this.socketWrapper.registerWriteInterest();
            }
        }
        this.listener = listener;
    }
    final boolean isClosed() {
        return this.closed;
    }
    public void write ( final int b ) throws IOException {
        synchronized ( this.writeLock ) {
            this.preWriteChecks();
            this.writeInternal ( new byte[] { ( byte ) b }, 0, 1 );
        }
    }
    public void write ( final byte[] b, final int off, final int len ) throws IOException {
        synchronized ( this.writeLock ) {
            this.preWriteChecks();
            this.writeInternal ( b, off, len );
        }
    }
    public void flush() throws IOException {
        this.preWriteChecks();
        this.flushInternal ( this.listener == null, true );
    }
    private void flushInternal ( final boolean block, final boolean updateFlushing ) throws IOException {
        try {
            synchronized ( this.writeLock ) {
                if ( updateFlushing ) {
                    this.flushing = this.socketWrapper.flush ( block );
                    if ( this.flushing ) {
                        this.socketWrapper.registerWriteInterest();
                    }
                } else {
                    this.socketWrapper.flush ( block );
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.onError ( t );
            if ( t instanceof IOException ) {
                throw ( IOException ) t;
            }
            throw new IOException ( t );
        }
    }
    public void close() throws IOException {
        if ( this.closed ) {
            return;
        }
        this.closed = true;
        this.flushInternal ( this.listener == null, false );
    }
    private void preWriteChecks() {
        if ( this.listener != null && !this.socketWrapper.canWrite() ) {
            throw new IllegalStateException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.write.ise" ) );
        }
        if ( this.closed ) {
            throw new IllegalStateException ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.write.closed" ) );
        }
    }
    private void writeInternal ( final byte[] b, final int off, final int len ) throws IOException {
        if ( this.listener == null ) {
            this.socketWrapper.write ( true, b, off, len );
        } else {
            this.socketWrapper.write ( false, b, off, len );
        }
    }
    final void onWritePossible() {
        try {
            if ( this.flushing ) {
                this.flushInternal ( false, true );
                if ( this.flushing ) {
                    return;
                }
            } else {
                this.flushInternal ( false, false );
            }
        } catch ( IOException ioe ) {
            this.onError ( ioe );
            return;
        }
        boolean fire = false;
        synchronized ( this.registeredLock ) {
            if ( this.socketWrapper.isReadyForWrite() ) {
                this.registered = false;
                fire = true;
            } else {
                this.registered = true;
            }
        }
        if ( fire ) {
            final ClassLoader oldCL = this.processor.getUpgradeToken().getContextBind().bind ( false, null );
            try {
                this.listener.onWritePossible();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                this.onError ( t );
            } finally {
                this.processor.getUpgradeToken().getContextBind().unbind ( false, oldCL );
            }
        }
    }
    private final void onError ( final Throwable t ) {
        if ( this.listener == null ) {
            return;
        }
        final ClassLoader oldCL = this.processor.getUpgradeToken().getContextBind().bind ( false, null );
        try {
            this.listener.onError ( t );
        } catch ( Throwable t2 ) {
            ExceptionUtils.handleThrowable ( t2 );
            UpgradeServletOutputStream.log.warn ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.onErrorFail" ), t2 );
        } finally {
            this.processor.getUpgradeToken().getContextBind().unbind ( false, oldCL );
        }
        try {
            this.close();
        } catch ( IOException ioe ) {
            if ( UpgradeServletOutputStream.log.isDebugEnabled() ) {
                UpgradeServletOutputStream.log.debug ( UpgradeServletOutputStream.sm.getString ( "upgrade.sos.errorCloseFail" ), ioe );
            }
        }
    }
    static {
        log = LogFactory.getLog ( UpgradeServletOutputStream.class );
        sm = StringManager.getManager ( UpgradeServletOutputStream.class );
    }
}
