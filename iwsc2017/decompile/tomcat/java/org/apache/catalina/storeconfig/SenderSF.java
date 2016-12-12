package org.apache.catalina.storeconfig;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import java.io.PrintWriter;
public class SenderSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aSender, final StoreDescription parentDesc ) throws Exception {
        if ( aSender instanceof ReplicationTransmitter ) {
            final ReplicationTransmitter transmitter = ( ReplicationTransmitter ) aSender;
            final MultiPointSender transport = transmitter.getTransport();
            if ( transport != null ) {
                this.storeElement ( aWriter, indent, transport );
            }
        }
    }
}
