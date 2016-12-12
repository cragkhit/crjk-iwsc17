package org.apache.catalina.tribes.membership;
public class SenderThread extends Thread {
    final long time;
    int errorCounter;
    public SenderThread ( final long time ) {
        this.errorCounter = 0;
        this.time = time;
        String channelName = "";
        if ( McastServiceImpl.access$100 ( McastServiceImpl.this ).getName() != null ) {
            channelName = "[" + McastServiceImpl.access$100 ( McastServiceImpl.this ).getName() + "]";
        }
        this.setName ( "Tribes-MembershipSender" + channelName );
    }
    @Override
    public void run() {
        while ( McastServiceImpl.this.doRunSender ) {
            try {
                McastServiceImpl.this.send ( true );
                this.errorCounter = 0;
            } catch ( Exception x ) {
                if ( this.errorCounter == 0 ) {
                    McastServiceImpl.access$000().warn ( McastServiceImpl.sm.getString ( "mcastServiceImpl.send.failed" ), x );
                } else {
                    McastServiceImpl.access$000().debug ( "Unable to send mcast message.", x );
                }
                if ( ++this.errorCounter >= McastServiceImpl.this.recoveryCounter ) {
                    this.errorCounter = 0;
                    RecoveryThread.recover ( McastServiceImpl.this );
                }
            }
            try {
                Thread.sleep ( this.time );
            } catch ( Exception ex ) {}
        }
    }
}
