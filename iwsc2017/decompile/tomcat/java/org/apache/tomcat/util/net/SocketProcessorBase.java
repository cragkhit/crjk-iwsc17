package org.apache.tomcat.util.net;
import java.util.Objects;
public abstract class SocketProcessorBase<S> implements Runnable {
    protected SocketWrapperBase<S> socketWrapper;
    protected SocketEvent event;
    public SocketProcessorBase ( final SocketWrapperBase<S> socketWrapper, final SocketEvent event ) {
        this.reset ( socketWrapper, event );
    }
    public void reset ( final SocketWrapperBase<S> socketWrapper, final SocketEvent event ) {
        Objects.requireNonNull ( event );
        this.socketWrapper = socketWrapper;
        this.event = event;
    }
    @Override
    public final void run() {
        synchronized ( this.socketWrapper ) {
            if ( this.socketWrapper.isClosed() ) {
                return;
            }
            this.doRun();
        }
    }
    protected abstract void doRun();
}
