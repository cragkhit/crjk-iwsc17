package org.apache.catalina.ha.backend;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.net.Socket;
import org.apache.juli.logging.Log;
public class TcpSender implements Sender {
    private static final Log log;
    HeartbeatListener config;
    protected Proxy[] proxies;
    protected Socket[] connections;
    protected BufferedReader[] connectionReaders;
    protected BufferedWriter[] connectionWriters;
    public TcpSender() {
        this.config = null;
        this.proxies = null;
        this.connections = null;
        this.connectionReaders = null;
        this.connectionWriters = null;
    }
    @Override
    public void init ( final HeartbeatListener config ) throws Exception {
        this.config = config;
        final StringTokenizer tok = new StringTokenizer ( config.getProxyList(), "," );
        this.proxies = new Proxy[tok.countTokens()];
        int i = 0;
        while ( tok.hasMoreTokens() ) {
            final String token = tok.nextToken().trim();
            final int pos = token.indexOf ( 58 );
            if ( pos <= 0 ) {
                throw new Exception ( "bad ProxyList" );
            }
            this.proxies[i] = new Proxy();
            this.proxies[i].port = Integer.parseInt ( token.substring ( pos + 1 ) );
            try {
                this.proxies[i].address = InetAddress.getByName ( token.substring ( 0, pos ) );
            } catch ( Exception e ) {
                throw new Exception ( "bad ProxyList" );
            }
            ++i;
        }
        this.connections = new Socket[this.proxies.length];
        this.connectionReaders = new BufferedReader[this.proxies.length];
        this.connectionWriters = new BufferedWriter[this.proxies.length];
    }
    @Override
    public int send ( final String mess ) throws Exception {
        if ( this.connections == null ) {
            TcpSender.log.error ( "Not initialized" );
            return -1;
        }
        final String requestLine = "POST " + this.config.getProxyURL() + " HTTP/1.0";
        for ( int i = 0; i < this.connections.length; ++i ) {
            if ( this.connections[i] == null ) {
                try {
                    if ( this.config.getHost() != null ) {
                        this.connections[i] = new Socket();
                        final InetAddress addr = InetAddress.getByName ( this.config.getHost() );
                        InetSocketAddress addrs = new InetSocketAddress ( addr, 0 );
                        this.connections[i].setReuseAddress ( true );
                        this.connections[i].bind ( addrs );
                        addrs = new InetSocketAddress ( this.proxies[i].address, this.proxies[i].port );
                        this.connections[i].connect ( addrs );
                    } else {
                        this.connections[i] = new Socket ( this.proxies[i].address, this.proxies[i].port );
                    }
                    this.connectionReaders[i] = new BufferedReader ( new InputStreamReader ( this.connections[i].getInputStream() ) );
                    this.connectionWriters[i] = new BufferedWriter ( new OutputStreamWriter ( this.connections[i].getOutputStream() ) );
                } catch ( Exception ex ) {
                    TcpSender.log.error ( "Unable to connect to proxy: " + ex );
                    this.close ( i );
                }
            }
            if ( this.connections[i] != null ) {
                final BufferedWriter writer = this.connectionWriters[i];
                try {
                    writer.write ( requestLine );
                    writer.write ( "\r\n" );
                    writer.write ( "Content-Length: " + mess.length() + "\r\n" );
                    writer.write ( "User-Agent: HeartbeatListener/1.0\r\n" );
                    writer.write ( "Connection: Keep-Alive\r\n" );
                    writer.write ( "\r\n" );
                    writer.write ( mess );
                    writer.write ( "\r\n" );
                    writer.flush();
                } catch ( Exception ex2 ) {
                    TcpSender.log.error ( "Unable to send collected load information to proxy: " + ex2 );
                    this.close ( i );
                }
                if ( this.connections[i] != null ) {
                    String responseStatus = this.connectionReaders[i].readLine();
                    if ( responseStatus == null ) {
                        TcpSender.log.error ( "Unable to read response from proxy" );
                        this.close ( i );
                    } else {
                        responseStatus = responseStatus.substring ( responseStatus.indexOf ( 32 ) + 1, responseStatus.indexOf ( 32, responseStatus.indexOf ( 32 ) + 1 ) );
                        final int status = Integer.parseInt ( responseStatus );
                        if ( status != 200 ) {
                            TcpSender.log.error ( "Status is " + status );
                            this.close ( i );
                        } else {
                            String header = this.connectionReaders[i].readLine();
                            int contentLength = 0;
                            while ( !"".equals ( header ) ) {
                                final int colon = header.indexOf ( 58 );
                                final String headerName = header.substring ( 0, colon ).trim();
                                final String headerValue = header.substring ( colon + 1 ).trim();
                                if ( "content-length".equalsIgnoreCase ( headerName ) ) {
                                    contentLength = Integer.parseInt ( headerValue );
                                }
                                header = this.connectionReaders[i].readLine();
                            }
                            if ( contentLength > 0 ) {
                                final char[] buf = new char[512];
                                while ( contentLength > 0 ) {
                                    final int thisTime = ( contentLength > buf.length ) ? buf.length : contentLength;
                                    final int n = this.connectionReaders[i].read ( buf, 0, thisTime );
                                    if ( n <= 0 ) {
                                        TcpSender.log.error ( "Read content failed" );
                                        this.close ( i );
                                        break;
                                    }
                                    contentLength -= n;
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
    protected void close ( final int i ) {
        try {
            if ( this.connectionReaders[i] != null ) {
                this.connectionReaders[i].close();
            }
        } catch ( IOException ex ) {}
        this.connectionReaders[i] = null;
        try {
            if ( this.connectionWriters[i] != null ) {
                this.connectionWriters[i].close();
            }
        } catch ( IOException ex2 ) {}
        this.connectionWriters[i] = null;
        try {
            if ( this.connections[i] != null ) {
                this.connections[i].close();
            }
        } catch ( IOException ex3 ) {}
        this.connections[i] = null;
    }
    static {
        log = LogFactory.getLog ( HeartbeatListener.class );
    }
}
