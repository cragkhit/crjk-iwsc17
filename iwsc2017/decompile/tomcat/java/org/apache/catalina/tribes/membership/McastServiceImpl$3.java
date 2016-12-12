package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.io.ChannelData;
class McastServiceImpl$3 implements Runnable {
    final   ChannelData[] val$data;
    @Override
    public void run() {
        final String name = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName ( "Membership-MemberAdded." );
            for ( int i = 0; i < this.val$data.length; ++i ) {
                try {
                    if ( this.val$data[i] != null && !McastServiceImpl.this.member.equals ( this.val$data[i].getAddress() ) ) {
                        McastServiceImpl.this.msgservice.messageReceived ( this.val$data[i] );
                    }
                } catch ( Throwable t ) {
                    if ( t instanceof ThreadDeath ) {
                        throw ( ThreadDeath ) t;
                    }
                    if ( t instanceof VirtualMachineError ) {
                        throw ( VirtualMachineError ) t;
                    }
                    McastServiceImpl.access$000().error ( McastServiceImpl.sm.getString ( "mcastServiceImpl.unableReceive.broadcastMessage" ), t );
                }
            }
        } finally {
            Thread.currentThread().setName ( name );
        }
    }
}
