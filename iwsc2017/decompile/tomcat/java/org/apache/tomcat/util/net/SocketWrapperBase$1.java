package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
static final class SocketWrapperBase$1 implements CompletionCheck {
    @Override
    public CompletionHandlerCall callHandler ( final CompletionState state, final ByteBuffer[] buffers, final int offset, final int length ) {
        for ( int i = 0; i < offset; ++i ) {
            if ( buffers[i].remaining() > 0 ) {
                return CompletionHandlerCall.CONTINUE;
            }
        }
        return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE : CompletionHandlerCall.NONE;
    }
}
