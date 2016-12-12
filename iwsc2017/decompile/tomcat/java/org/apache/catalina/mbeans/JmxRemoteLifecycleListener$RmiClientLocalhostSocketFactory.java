package org.apache.catalina.mbeans;
import java.io.IOException;
import java.net.Socket;
import java.io.Serializable;
import java.rmi.server.RMIClientSocketFactory;
public static class RmiClientLocalhostSocketFactory implements RMIClientSocketFactory, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FORCED_HOST = "localhost";
    private final RMIClientSocketFactory factory;
    public RmiClientLocalhostSocketFactory ( final RMIClientSocketFactory theFactory ) {
        this.factory = theFactory;
    }
    @Override
    public Socket createSocket ( final String host, final int port ) throws IOException {
        if ( this.factory == null ) {
            return new Socket ( "localhost", port );
        }
        return this.factory.createSocket ( "localhost", port );
    }
}
