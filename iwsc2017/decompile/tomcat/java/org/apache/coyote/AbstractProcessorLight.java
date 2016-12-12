package org.apache.coyote;
import org.apache.juli.logging.Log;
import java.io.IOException;
import java.util.Iterator;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.tomcat.util.net.DispatchType;
import java.util.Set;
public abstract class AbstractProcessorLight implements Processor {
    private Set<DispatchType> dispatches;
    public AbstractProcessorLight() {
        this.dispatches = new CopyOnWriteArraySet<DispatchType>();
    }
    @Override
    public AbstractEndpoint.Handler.SocketState process ( final SocketWrapperBase<?> socketWrapper, final SocketEvent status ) throws IOException {
        AbstractEndpoint.Handler.SocketState state = AbstractEndpoint.Handler.SocketState.CLOSED;
        Iterator<DispatchType> dispatches = null;
        do {
            if ( dispatches != null ) {
                final DispatchType nextDispatch = dispatches.next();
                state = this.dispatch ( nextDispatch.getSocketStatus() );
            } else if ( status != SocketEvent.DISCONNECT ) {
                if ( this.isAsync() || this.isUpgrade() || state == AbstractEndpoint.Handler.SocketState.ASYNC_END ) {
                    state = this.dispatch ( status );
                    if ( state == AbstractEndpoint.Handler.SocketState.OPEN ) {
                        state = this.service ( socketWrapper );
                    }
                } else if ( status == SocketEvent.OPEN_WRITE ) {
                    state = AbstractEndpoint.Handler.SocketState.LONG;
                } else {
                    state = this.service ( socketWrapper );
                }
            }
            if ( state != AbstractEndpoint.Handler.SocketState.CLOSED && this.isAsync() ) {
                state = this.asyncPostProcess();
            }
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( "Socket: [" + socketWrapper + "], Status in: [" + status + "], State out: [" + state + "]" );
            }
            if ( dispatches == null || !dispatches.hasNext() ) {
                dispatches = this.getIteratorAndClearDispatches();
            }
        } while ( state == AbstractEndpoint.Handler.SocketState.ASYNC_END || ( dispatches != null && state != AbstractEndpoint.Handler.SocketState.CLOSED ) );
        return state;
    }
    public void addDispatch ( final DispatchType dispatchType ) {
        synchronized ( this.dispatches ) {
            this.dispatches.add ( dispatchType );
        }
    }
    public Iterator<DispatchType> getIteratorAndClearDispatches() {
        Iterator<DispatchType> result;
        synchronized ( this.dispatches ) {
            result = this.dispatches.iterator();
            if ( result.hasNext() ) {
                this.dispatches.clear();
            } else {
                result = null;
            }
        }
        return result;
    }
    protected void clearDispatches() {
        synchronized ( this.dispatches ) {
            this.dispatches.clear();
        }
    }
    protected abstract AbstractEndpoint.Handler.SocketState service ( final SocketWrapperBase<?> p0 ) throws IOException;
    protected abstract AbstractEndpoint.Handler.SocketState dispatch ( final SocketEvent p0 );
    protected abstract AbstractEndpoint.Handler.SocketState asyncPostProcess();
    protected abstract Log getLog();
}
