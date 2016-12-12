package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
static final class SocketWrapperBase$2 implements CompletionCheck {
    @Override
    public CompletionHandlerCall callHandler ( final CompletionState state, final ByteBuffer[] buffers, final int offset, final int length ) {
        return ( state == CompletionState.DONE ) ? CompletionHandlerCall.DONE : CompletionHandlerCall.NONE;
    }
}
