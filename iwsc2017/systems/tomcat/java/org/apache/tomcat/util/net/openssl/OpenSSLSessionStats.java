package org.apache.tomcat.util.net.openssl;
import org.apache.tomcat.jni.SSLContext;
public final class OpenSSLSessionStats {
    private final long context;
    OpenSSLSessionStats ( long context ) {
        this.context = context;
    }
    public long number() {
        return SSLContext.sessionNumber ( context );
    }
    public long connect() {
        return SSLContext.sessionConnect ( context );
    }
    public long connectGood() {
        return SSLContext.sessionConnectGood ( context );
    }
    public long connectRenegotiate() {
        return SSLContext.sessionConnectRenegotiate ( context );
    }
    public long accept() {
        return SSLContext.sessionAccept ( context );
    }
    public long acceptGood() {
        return SSLContext.sessionAcceptGood ( context );
    }
    public long acceptRenegotiate() {
        return SSLContext.sessionAcceptRenegotiate ( context );
    }
    public long hits() {
        return SSLContext.sessionHits ( context );
    }
    public long cbHits() {
        return SSLContext.sessionCbHits ( context );
    }
    public long misses() {
        return SSLContext.sessionMisses ( context );
    }
    public long timeouts() {
        return SSLContext.sessionTimeouts ( context );
    }
    public long cacheFull() {
        return SSLContext.sessionCacheFull ( context );
    }
}
