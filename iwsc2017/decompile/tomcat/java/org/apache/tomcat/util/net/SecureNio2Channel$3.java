package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.nio.channels.CompletionHandler;
class SecureNio2Channel$3 implements CompletionHandler<Integer, A> {
    final   long val$timeout;
    final   TimeUnit val$unit;
    final   Object val$attachment;
    final   int val$written;
    final   ByteBuffer val$src;
    final   CompletionHandler val$handler;
    @Override
    public void completed ( final Integer nBytes, final A attach ) {
        if ( nBytes < 0 ) {
            this.failed ( new EOFException(), attach );
        } else if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
            SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer, this.val$timeout, this.val$unit, this.val$attachment, ( CompletionHandler<Integer, ? super Object> ) this );
        } else if ( this.val$written == 0 ) {
            SecureNio2Channel.this.write ( this.val$src, this.val$timeout, this.val$unit, this.val$attachment, this.val$handler );
        } else {
            this.val$handler.completed ( this.val$written, attach );
        }
    }
    @Override
    public void failed ( final Throwable exc, final A attach ) {
        this.val$handler.failed ( exc, attach );
    }
}
