package org.apache.tomcat.util.net;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.CompletionHandler;
private class ScatterReadCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
    @Override
    public void completed ( final Long nBytes, final OperationState<A> state ) {
        if ( ( int ) ( Object ) nBytes < 0 ) {
            this.failed ( ( Throwable ) new EOFException(), state );
        } else {
            ( ( OperationState<Object> ) state ).nBytes += nBytes;
            final CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
            boolean complete = true;
            boolean completion = true;
            if ( ( ( OperationState<Object> ) state ).check != null ) {
                switch ( ( ( OperationState<Object> ) state ).check.callHandler ( currentState, ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length ) ) {
                case CONTINUE: {
                    complete = false;
                }
                case NONE: {
                    completion = false;
                    break;
                }
                }
            }
            if ( complete ) {
                Nio2SocketWrapper.access$900 ( Nio2SocketWrapper.this ).release();
                ( ( OperationState<Object> ) state ).state = currentState;
                if ( completion && ( ( OperationState<Object> ) state ).handler != null ) {
                    ( ( OperationState<Object> ) state ).handler.completed ( ( ( OperationState<Object> ) state ).nBytes, ( ( OperationState<Object> ) state ).attachment );
                }
            } else {
                Nio2SocketWrapper.this.getSocket().read ( ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length, ( ( OperationState<Object> ) state ).timeout, ( ( OperationState<Object> ) state ).unit, state, this );
            }
        }
    }
    @Override
    public void failed ( final Throwable exc, final OperationState<A> state ) {
        IOException ioe;
        if ( exc instanceof IOException ) {
            ioe = ( IOException ) exc;
        } else {
            ioe = new IOException ( exc );
        }
        Nio2SocketWrapper.this.setError ( ioe );
        Nio2SocketWrapper.access$900 ( Nio2SocketWrapper.this ).release();
        if ( exc instanceof AsynchronousCloseException ) {
            return;
        }
        ( ( OperationState<Object> ) state ).state = ( Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE );
        if ( ( ( OperationState<Object> ) state ).handler != null ) {
            ( ( OperationState<Object> ) state ).handler.failed ( ioe, ( ( OperationState<Object> ) state ).attachment );
        }
    }
}
