package org.apache.tomcat.jni;
import java.nio.ByteBuffer;
public class Socket {
    public static final int SOCK_STREAM = 0;
    public static final int SOCK_DGRAM  = 1;
    public static final int APR_SO_LINGER       = 1;
    public static final int APR_SO_KEEPALIVE    = 2;
    public static final int APR_SO_DEBUG        = 4;
    public static final int APR_SO_NONBLOCK     = 8;
    public static final int APR_SO_REUSEADDR    = 16;
    public static final int APR_SO_SNDBUF       = 64;
    public static final int APR_SO_RCVBUF       = 128;
    public static final int APR_SO_DISCONNECTED = 256;
    public static final int APR_TCP_NODELAY     = 512;
    public static final int APR_TCP_NOPUSH      = 1024;
    public static final int APR_RESET_NODELAY   = 2048;
    public static final int APR_INCOMPLETE_READ = 4096;
    public static final int APR_INCOMPLETE_WRITE = 8192;
    public static final int APR_IPV6_V6ONLY      = 16384;
    public static final int APR_TCP_DEFER_ACCEPT = 32768;
    public static final int APR_SHUTDOWN_READ      = 0;
    public static final int APR_SHUTDOWN_WRITE     = 1;
    public static final int APR_SHUTDOWN_READWRITE = 2;
    public static final int APR_IPV4_ADDR_OK = 0x01;
    public static final int APR_IPV6_ADDR_OK = 0x02;
    public static final int APR_UNSPEC = 0;
    public static final int APR_INET   = 1;
    public static final int APR_INET6  = 2;
    public static final int APR_PROTO_TCP  =   6;
    public static final int APR_PROTO_UDP  =  17;
    public static final int APR_PROTO_SCTP = 132;
    public static final int APR_LOCAL  = 0;
    public static final int APR_REMOTE = 1;
    public static final int SOCKET_GET_POOL = 0;
    public static final int SOCKET_GET_IMPL = 1;
    public static final int SOCKET_GET_APRS = 2;
    public static final int SOCKET_GET_TYPE = 3;
    public static native long create ( int family, int type,
                                       int protocol, long cont )
    throws Exception;
    public static native int shutdown ( long thesocket, int how );
    public static native int close ( long thesocket );
    public static native void destroy ( long thesocket );
    public static native int bind ( long sock, long sa );
    public static native int listen ( long sock, int backlog );
    public static native long acceptx ( long sock, long pool )
    throws Exception;
    public static native long accept ( long sock )
    throws Exception;
    public static native int acceptfilter ( long sock, String name, String args );
    public static native boolean atmark ( long sock );
    public static native int connect ( long sock, long sa );
    public static native int send ( long sock, byte[] buf, int offset, int len );
    public static native int sendb ( long sock, ByteBuffer buf,
                                     int offset, int len );
    public static native int sendib ( long sock, ByteBuffer buf,
                                      int offset, int len );
    public static native int sendbb ( long sock,
                                      int offset, int len );
    public static native int sendibb ( long sock,
                                       int offset, int len );
    public static native int sendv ( long sock, byte[][] vec );
    public static native int sendto ( long sock, long where, int flags,
                                      byte[] buf, int offset, int len );
    public static native int recv ( long sock, byte[] buf, int offset, int nbytes );
    public static native int recvt ( long sock, byte[] buf, int offset,
                                     int nbytes, long timeout );
    public static native int recvb ( long sock, ByteBuffer buf,
                                     int offset, int nbytes );
    public static native int recvbb ( long sock,
                                      int offset, int nbytes );
    public static native int recvbt ( long sock, ByteBuffer buf,
                                      int offset, int nbytes, long timeout );
    public static native int recvbbt ( long sock,
                                       int offset, int nbytes, long timeout );
    public static native int recvfrom ( long from, long sock, int flags,
                                        byte[] buf, int offset, int nbytes );
    public static native int optSet ( long sock, int opt, int on );
    public static native int optGet ( long sock, int opt )
    throws Exception;
    public static native int timeoutSet ( long sock, long t );
    public static native long timeoutGet ( long sock )
    throws Exception;
    public static native long sendfile ( long sock, long file, byte [][] headers,
                                         byte[][] trailers, long offset,
                                         long len, int flags );
    public static native long sendfilen ( long sock, long file, long offset,
                                          long len, int flags );
    public static native long pool ( long thesocket )
    throws Exception;
    private static native long get ( long socket, int what );
    public static native void setsbb ( long sock, ByteBuffer buf );
    public static native void setrbb ( long sock, ByteBuffer buf );
    public static native int dataSet ( long sock, String key, Object data );
    public static native Object dataGet ( long sock, String key );
}
