package org.apache.catalina.mbeans;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import javax.net.ssl.SSLServerSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
public static class SslRmiServerBindSocketFactory extends SslRMIServerSocketFactory {
    private static final SSLServerSocketFactory sslServerSocketFactory;
    private static final String[] defaultProtocols;
    private final InetAddress bindAddress;
    public SslRmiServerBindSocketFactory ( final String[] enabledCipherSuites, final String[] enabledProtocols, final boolean needClientAuth, final String address ) {
        super ( enabledCipherSuites, enabledProtocols, needClientAuth );
        InetAddress bindAddress = null;
        try {
            bindAddress = InetAddress.getByName ( address );
        } catch ( UnknownHostException e ) {
            JmxRemoteLifecycleListener.access$000().error ( JmxRemoteLifecycleListener.sm.getString ( "jmxRemoteLifecycleListener.invalidRmiBindAddress", address ), e );
        }
        this.bindAddress = bindAddress;
    }
    @Override
    public ServerSocket createServerSocket ( final int port ) throws IOException {
        final SSLServerSocket sslServerSocket = ( SSLServerSocket ) SslRmiServerBindSocketFactory.sslServerSocketFactory.createServerSocket ( port, 0, this.bindAddress );
        if ( this.getEnabledCipherSuites() != null ) {
            sslServerSocket.setEnabledCipherSuites ( this.getEnabledCipherSuites() );
        }
        if ( this.getEnabledProtocols() == null ) {
            sslServerSocket.setEnabledProtocols ( SslRmiServerBindSocketFactory.defaultProtocols );
        } else {
            sslServerSocket.setEnabledProtocols ( this.getEnabledProtocols() );
        }
        sslServerSocket.setNeedClientAuth ( this.getNeedClientAuth() );
        return sslServerSocket;
    }
    static {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getDefault();
        } catch ( NoSuchAlgorithmException e ) {
            throw new IllegalStateException ( e );
        }
        sslServerSocketFactory = sslContext.getServerSocketFactory();
        final String[] protocols = sslContext.getDefaultSSLParameters().getProtocols();
        final List<String> filteredProtocols = new ArrayList<String> ( protocols.length );
        for ( final String protocol : protocols ) {
            if ( !protocol.toUpperCase ( Locale.ENGLISH ).contains ( "SSL" ) ) {
                filteredProtocols.add ( protocol );
            }
        }
        defaultProtocols = filteredProtocols.toArray ( new String[filteredProtocols.size()] );
    }
}
