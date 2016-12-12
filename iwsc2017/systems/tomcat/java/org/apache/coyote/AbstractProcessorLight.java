package org.apache.coyote;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.DispatchType;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
public abstract class AbstractProcessorLight implements Processor {
    private Set<DispatchType> dispatches = new CopyOnWriteArraySet<>();
    @Override
    public SocketState process ( SocketWrapperBase<?> socketWrapper, SocketEvent status )
    throws IOException {
        SocketState state = SocketState.CLOSED;
        Iterator<DispatchType> dispatches = null;
        do {
            if ( dispatches != null ) {
                DispatchType nextDispatch = dispatches.next();
                state = dispatch ( nextDispatch.getSocketStatus() );
            } else if ( status == SocketEvent.DISCONNECT ) {
            } else if ( isAsync() || isUpgrade() || state == SocketState.ASYNC_END ) {
                state = dispatch ( status );
                if ( state == SocketState.OPEN ) {
                    state = service ( socketWrapper );
                }
            } else if ( status == SocketEvent.OPEN_WRITE ) {
                state = SocketState.LONG;
            } else {
                state = service ( socketWrapper );
            }
            if ( state != SocketState.CLOSED && isAsync() ) {
                state = asyncPostProcess();
            }
            if ( getLog().isDebugEnabled() ) {
                getLog().debug ( "Socket: [" + socketWrapper +
                                 "], Status in: [" + status +
                                 "], State out: [" + state + "]" );
            }
            if ( dispatches == null || !dispatches.hasNext() ) {
                dispatches = getIteratorAndClearDispatches();
            }
        } while ( state == SocketState.ASYNC_END ||
                  dispatches != null && state != SocketState.CLOSED );
        return state;
    }
    public void addDispatch ( DispatchType dispatchType ) {
        synchronized ( dispatches ) {
            dispatches.add ( dispatchType );
        }
    }
    public Iterator<DispatchType> getIteratorAndClearDispatches() {
        Iterator<DispatchType> result;
        synchronized ( dispatches ) {
            result = dispatches.iterator();
            if ( result.hasNext() ) {
                dispatches.clear();
            } else {
                result = null;
            }
        }
        return result;
    }
    protected void clearDispatches() {
        synchronized ( dispatches ) {
            dispatches.clear();
        }
    }
    protected abstract SocketState service ( SocketWrapperBase<?> socketWrapper ) throws IOException;
    protected abstract SocketState dispatch ( SocketEvent status );
    protected abstract SocketState asyncPostProcess();
    protected abstract Log getLog();
}
