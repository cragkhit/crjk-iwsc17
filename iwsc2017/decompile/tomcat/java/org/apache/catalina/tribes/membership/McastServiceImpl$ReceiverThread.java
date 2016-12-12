package org.apache.catalina.tribes.membership;
public class ReceiverThread extends Thread {
    int errorCounter;
    public ReceiverThread() {
        this.errorCounter = 0;
        String channelName = "";
        if ( McastServiceImpl.access$100 ( McastServiceImpl.this ).getName() != null ) {
            channelName = "[" + McastServiceImpl.access$100 ( McastServiceImpl.this ).getName() + "]";
        }
        this.setName ( "Tribes-MembershipReceiver" + channelName );
    }
    @Override
    public void run() {
        while ( McastServiceImpl.this.doRunReceiver ) {
            try {
                McastServiceImpl.this.receive();
                this.errorCounter = 0;
            } catch ( ArrayIndexOutOfBoundsException ax ) {
                if ( !McastServiceImpl.access$000().isDebugEnabled() ) {
                    continue;
                }
                McastServiceImpl.access$000().debug ( "Invalid member mcast package.", ax );
            } catch ( Exception x ) {
                if ( this.errorCounter == 0 && McastServiceImpl.this.doRunReceiver ) {
                    McastServiceImpl.access$000().warn ( McastServiceImpl.sm.getString ( "mcastServiceImpl.error.receiving" ), x );
                } else if ( McastServiceImpl.access$000().isDebugEnabled() ) {
                    McastServiceImpl.access$000().debug ( "Error receiving mcast package" + ( McastServiceImpl.this.doRunReceiver ? ". Sleeping 500ms" : "." ), x );
                }
                if ( !McastServiceImpl.this.doRunReceiver ) {
                    continue;
                }
                try {
                    Thread.sleep ( 500L );
                } catch ( Exception ex ) {}
                if ( ++this.errorCounter < McastServiceImpl.this.recoveryCounter ) {
                    continue;
                }
                this.errorCounter = 0;
                RecoveryThread.recover ( McastServiceImpl.this );
            }
        }
    }
}
