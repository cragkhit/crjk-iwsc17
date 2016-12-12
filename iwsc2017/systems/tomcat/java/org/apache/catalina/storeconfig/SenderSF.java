package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
public class SenderSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aSender,
                                StoreDescription parentDesc ) throws Exception {
        if ( aSender instanceof ReplicationTransmitter ) {
            ReplicationTransmitter transmitter = ( ReplicationTransmitter ) aSender;
            MultiPointSender transport = transmitter.getTransport();
            if ( transport != null ) {
                storeElement ( aWriter, indent, transport );
            }
        }
    }
}
