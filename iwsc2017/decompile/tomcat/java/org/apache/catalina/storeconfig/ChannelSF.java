package org.apache.catalina.storeconfig;
import java.util.Iterator;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ManagedChannel;
import org.apache.catalina.tribes.Channel;
import java.io.PrintWriter;
public class ChannelSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aChannel, final StoreDescription parentDesc ) throws Exception {
        if ( aChannel instanceof Channel ) {
            final Channel channel = ( Channel ) aChannel;
            if ( channel instanceof ManagedChannel ) {
                final ManagedChannel managedChannel = ( ManagedChannel ) channel;
                final MembershipService service = managedChannel.getMembershipService();
                if ( service != null ) {
                    this.storeElement ( aWriter, indent, service );
                }
                final ChannelSender sender = managedChannel.getChannelSender();
                if ( sender != null ) {
                    this.storeElement ( aWriter, indent, sender );
                }
                final ChannelReceiver receiver = managedChannel.getChannelReceiver();
                if ( receiver != null ) {
                    this.storeElement ( aWriter, indent, receiver );
                }
                final Iterator<ChannelInterceptor> interceptors = managedChannel.getInterceptors();
                while ( interceptors.hasNext() ) {
                    final ChannelInterceptor interceptor = interceptors.next();
                    this.storeElement ( aWriter, indent, interceptor );
                }
            }
        }
    }
}
