package org.apache.catalina.mbeans;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.rmi.server.RMIServerSocketFactory;
public static class RmiServerBindSocketFactory implements RMIServerSocketFactory {
    private final InetAddress bindAddress;
    public RmiServerBindSocketFactory ( final String address ) {
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
        return new ServerSocket ( port, 0, this.bindAddress );
    }
}
