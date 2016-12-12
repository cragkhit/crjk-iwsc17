package org.apache.tomcat.websocket;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.nio.ByteBuffer;
public interface AsyncChannelWrapper {
    Future<Integer> read ( ByteBuffer p0 );
    <B, A extends B> void read ( ByteBuffer p0, A p1, CompletionHandler<Integer, B> p2 );
    Future<Integer> write ( ByteBuffer p0 );
    <B, A extends B> void write ( ByteBuffer[] p0, int p1, int p2, long p3, TimeUnit p4, A p5, CompletionHandler<Long, B> p6 );
    void close();
    Future<Void> handshake() throws SSLException;
}
