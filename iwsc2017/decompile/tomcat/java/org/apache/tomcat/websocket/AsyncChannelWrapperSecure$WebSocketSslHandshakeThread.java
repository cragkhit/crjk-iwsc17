package org.apache.tomcat.websocket;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult;
private class WebSocketSslHandshakeThread extends Thread {
    private final WrapperFuture<Void, Void> hFuture;
    private SSLEngineResult.HandshakeStatus handshakeStatus;
    private SSLEngineResult.Status resultStatus;
    public WebSocketSslHandshakeThread ( final WrapperFuture<Void, Void> hFuture ) {
        this.hFuture = hFuture;
    }
    @Override
    public void run() {
        try {
            AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).beginHandshake();
            AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).position ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).limit() );
            this.handshakeStatus = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getHandshakeStatus();
            this.resultStatus = SSLEngineResult.Status.OK;
            boolean handshaking = true;
            while ( handshaking ) {
                switch ( this.handshakeStatus ) {
                case NEED_WRAP: {
                    AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ).clear();
                    final SSLEngineResult r = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).wrap ( AsyncChannelWrapperSecure.access$800(), AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ) );
                    this.checkResult ( r, true );
                    AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ).flip();
                    final Future<Integer> fWrite = AsyncChannelWrapperSecure.access$400 ( AsyncChannelWrapperSecure.this ).write ( AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ) );
                    fWrite.get();
                    continue;
                }
                case NEED_UNWRAP: {
                    AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).compact();
                    if ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).position() == 0 || this.resultStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                        final Future<Integer> fRead = AsyncChannelWrapperSecure.access$400 ( AsyncChannelWrapperSecure.this ).read ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ) );
                        fRead.get();
                    }
                    AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).flip();
                    final SSLEngineResult r = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).unwrap ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ), AsyncChannelWrapperSecure.access$800() );
                    this.checkResult ( r, false );
                    continue;
                }
                case NEED_TASK: {
                    Runnable r2 = null;
                    while ( ( r2 = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getDelegatedTask() ) != null ) {
                        r2.run();
                    }
                    this.handshakeStatus = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getHandshakeStatus();
                    continue;
                }
                case FINISHED: {
                    handshaking = false;
                    continue;
                }
                case NOT_HANDSHAKING: {
                    throw new SSLException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.notHandshaking" ) );
                }
                }
            }
        } catch ( SSLException | InterruptedException | ExecutionException e ) {
            this.hFuture.fail ( e );
        }
        this.hFuture.complete ( null );
    }
    private void checkResult ( final SSLEngineResult result, final boolean wrap ) throws SSLException {
        this.handshakeStatus = result.getHandshakeStatus();
        this.resultStatus = result.getStatus();
        if ( this.resultStatus != SSLEngineResult.Status.OK && ( wrap || this.resultStatus != SSLEngineResult.Status.BUFFER_UNDERFLOW ) ) {
            throw new SSLException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.check.notOk", this.resultStatus ) );
        }
        if ( wrap && result.bytesConsumed() != 0 ) {
            throw new SSLException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.check.wrap" ) );
        }
        if ( !wrap && result.bytesProduced() != 0 ) {
            throw new SSLException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.check.unwrap" ) );
        }
    }
}
