// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.OnMessage;
import java.util.concurrent.ExecutionException;
import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Future;

public class EchoAsyncAnnotation
{
    private static final Future<Void> COMPLETED;
    Future<Void> f;
    StringBuilder sb;
    ByteArrayOutputStream bytes;
    
    public EchoAsyncAnnotation() {
        this.f = EchoAsyncAnnotation.COMPLETED;
        this.sb = null;
        this.bytes = null;
    }
    
    @OnMessage
    public void echoTextMessage(final Session session, final String msg, final boolean last) {
        if (this.sb == null) {
            this.sb = new StringBuilder();
        }
        this.sb.append(msg);
        if (last) {
            try {
                this.f.get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            this.f = (Future<Void>)session.getAsyncRemote().sendText(this.sb.toString());
            this.sb = null;
        }
    }
    
    @OnMessage
    public void echoBinaryMessage(final byte[] msg, final Session session, final boolean last) throws IOException {
        if (this.bytes == null) {
            this.bytes = new ByteArrayOutputStream();
        }
        this.bytes.write(msg);
        if (last) {
            try {
                this.f.get();
            }
            catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            this.f = (Future<Void>)session.getAsyncRemote().sendBinary(ByteBuffer.wrap(this.bytes.toByteArray()));
            this.bytes = null;
        }
    }
    
    @OnMessage
    public void echoPongMessage(final PongMessage pm) {
    }
    
    static {
        COMPLETED = new CompletedFuture();
    }
    
    private static class CompletedFuture implements Future<Void>
    {
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }
        
        @Override
        public boolean isCancelled() {
            return false;
        }
        
        @Override
        public boolean isDone() {
            return true;
        }
        
        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }
        
        @Override
        public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }
}
