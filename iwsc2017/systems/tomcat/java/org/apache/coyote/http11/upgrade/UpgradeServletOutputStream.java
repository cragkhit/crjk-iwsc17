package org.apache.coyote.http11.upgrade;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import org.apache.coyote.ContainerThreadMarker;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.net.DispatchType;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class UpgradeServletOutputStream extends ServletOutputStream {
    private static final Log log = LogFactory.getLog ( UpgradeServletOutputStream.class );
    private static final StringManager sm =
        StringManager.getManager ( UpgradeServletOutputStream.class );
    private final UpgradeProcessorBase processor;
    private final SocketWrapperBase<?> socketWrapper;
    private final Object registeredLock = new Object();
    private final Object writeLock = new Object();
    private volatile boolean flushing = false;
    private volatile boolean closed = false;
    private volatile WriteListener listener = null;
    private boolean registered = false;
    public UpgradeServletOutputStream ( UpgradeProcessorBase processor,
                                        SocketWrapperBase<?> socketWrapper ) {
        this.processor = processor;
        this.socketWrapper = socketWrapper;
    }
    @Override
    public final boolean isReady() {
        if ( listener == null ) {
            throw new IllegalStateException (
                sm.getString ( "upgrade.sos.canWrite.ise" ) );
        }
        if ( closed ) {
            return false;
        }
        synchronized ( registeredLock ) {
            if ( flushing ) {
                registered = true;
                return false;
            } else if ( registered ) {
                return false;
            } else {
                boolean result = socketWrapper.isReadyForWrite();
                registered = !result;
                return result;
            }
        }
    }
    @Override
    public final void setWriteListener ( WriteListener listener ) {
        if ( listener == null ) {
            throw new IllegalArgumentException (
                sm.getString ( "upgrade.sos.writeListener.null" ) );
        }
        if ( this.listener != null ) {
            throw new IllegalArgumentException (
                sm.getString ( "upgrade.sos.writeListener.set" ) );
        }
        if ( closed ) {
            throw new IllegalStateException ( sm.getString ( "upgrade.sos.write.closed" ) );
        }
        synchronized ( registeredLock ) {
            registered = true;
            if ( ContainerThreadMarker.isContainerThread() ) {
                processor.addDispatch ( DispatchType.NON_BLOCKING_WRITE );
            } else {
                socketWrapper.registerWriteInterest();
            }
        }
        this.listener = listener;
    }
    final boolean isClosed() {
        return closed;
    }
    @Override
    public void write ( int b ) throws IOException {
        synchronized ( writeLock ) {
            preWriteChecks();
            writeInternal ( new byte[] { ( byte ) b }, 0, 1 );
        }
    }
    @Override
    public void write ( byte[] b, int off, int len ) throws IOException {
        synchronized ( writeLock ) {
            preWriteChecks();
            writeInternal ( b, off, len );
        }
    }
    @Override
    public void flush() throws IOException {
        preWriteChecks();
        flushInternal ( listener == null, true );
    }
    private void flushInternal ( boolean block, boolean updateFlushing ) throws IOException {
        try {
            synchronized ( writeLock ) {
                if ( updateFlushing ) {
                    flushing = socketWrapper.flush ( block );
                    if ( flushing ) {
                        socketWrapper.registerWriteInterest();
                    }
                } else {
                    socketWrapper.flush ( block );
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            onError ( t );
            if ( t instanceof IOException ) {
                throw ( IOException ) t;
            } else {
                throw new IOException ( t );
            }
        }
    }
    @Override
    public void close() throws IOException {
        if ( closed ) {
            return;
        }
        closed = true;
        flushInternal ( ( listener == null ), false );
    }
    private void preWriteChecks() {
        if ( listener != null && !socketWrapper.canWrite() ) {
            throw new IllegalStateException ( sm.getString ( "upgrade.sos.write.ise" ) );
        }
        if ( closed ) {
            throw new IllegalStateException ( sm.getString ( "upgrade.sos.write.closed" ) );
        }
    }
    private void writeInternal ( byte[] b, int off, int len ) throws IOException {
        if ( listener == null ) {
            socketWrapper.write ( true, b, off, len );
        } else {
            socketWrapper.write ( false, b, off, len );
        }
    }
    final void onWritePossible() {
        try {
            if ( flushing ) {
                flushInternal ( false, true );
                if ( flushing ) {
                    return;
                }
            } else {
                flushInternal ( false, false );
            }
        } catch ( IOException ioe ) {
            onError ( ioe );
            return;
        }
        boolean fire = false;
        synchronized ( registeredLock ) {
            if ( socketWrapper.isReadyForWrite() ) {
                registered = false;
                fire = true;
            } else {
                registered = true;
            }
        }
        if ( fire ) {
            ClassLoader oldCL = processor.getUpgradeToken().getContextBind().bind ( false, null );
            try {
                listener.onWritePossible();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                onError ( t );
            } finally {
                processor.getUpgradeToken().getContextBind().unbind ( false, oldCL );
            }
        }
    }
    private final void onError ( Throwable t ) {
        if ( listener == null ) {
            return;
        }
        ClassLoader oldCL = processor.getUpgradeToken().getContextBind().bind ( false, null );
        try {
            listener.onError ( t );
        } catch ( Throwable t2 ) {
            ExceptionUtils.handleThrowable ( t2 );
            log.warn ( sm.getString ( "upgrade.sos.onErrorFail" ), t2 );
        } finally {
            processor.getUpgradeToken().getContextBind().unbind ( false, oldCL );
        }
        try {
            close();
        } catch ( IOException ioe ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "upgrade.sos.errorCloseFail" ), ioe );
            }
        }
    }
}
