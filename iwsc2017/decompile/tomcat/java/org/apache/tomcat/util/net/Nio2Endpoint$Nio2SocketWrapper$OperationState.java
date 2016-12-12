package org.apache.tomcat.util.net;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
private static class OperationState<A> {
    private final ByteBuffer[] buffers;
    private final int offset;
    private final int length;
    private final A attachment;
    private final long timeout;
    private final TimeUnit unit;
    private final CompletionCheck check;
    private final CompletionHandler<Long, ? super A> handler;
    private volatile long nBytes;
    private volatile CompletionState state;
    private OperationState ( final ByteBuffer[] buffers, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
        this.nBytes = 0L;
        this.state = CompletionState.PENDING;
        this.buffers = buffers;
        this.offset = offset;
        this.length = length;
        this.timeout = timeout;
        this.unit = unit;
        this.attachment = attachment;
        this.check = check;
        this.handler = handler;
    }
}
