package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import java.util.Iterator;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.MembershipService;
public class ChannelSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aChannel,
                                StoreDescription parentDesc ) throws Exception {
        if ( aChannel instanceof Channel ) {
            Channel channel = ( Channel ) aChannel;
            if ( channel instanceof ManagedChannel ) {
                ManagedChannel managedChannel = ( ManagedChannel ) channel;
                MembershipService service = managedChannel.getMembershipService();
                if ( service != null ) {
                    storeElement ( aWriter, indent, service );
                }
                ChannelSender sender = managedChannel.getChannelSender();
                if ( sender != null ) {
                    storeElement ( aWriter, indent, sender );
                }
                ChannelReceiver receiver = managedChannel.getChannelReceiver();
                if ( receiver != null ) {
                    storeElement ( aWriter, indent, receiver );
                }
                Iterator<ChannelInterceptor> interceptors = managedChannel.getInterceptors();
                while ( interceptors.hasNext() ) {
                    ChannelInterceptor interceptor = interceptors.next();
                    storeElement ( aWriter, indent, interceptor );
                }
            }
        }
    }
}
