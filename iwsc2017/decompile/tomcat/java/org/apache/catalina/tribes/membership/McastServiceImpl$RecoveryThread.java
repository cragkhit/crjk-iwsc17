package org.apache.catalina.tribes.membership;
import java.util.concurrent.atomic.AtomicBoolean;
protected static class RecoveryThread extends Thread {
    private static final AtomicBoolean running;
    final McastServiceImpl parent;
    public static synchronized void recover ( final McastServiceImpl parent ) {
        if ( !parent.isRecoveryEnabled() ) {
            return;
        }
        if ( !RecoveryThread.running.compareAndSet ( false, true ) ) {
            return;
        }
        final Thread t = new RecoveryThread ( parent );
        String channelName = "";
        if ( McastServiceImpl.access$100 ( parent ).getName() != null ) {
            channelName = "[" + McastServiceImpl.access$100 ( parent ).getName() + "]";
        }
        t.setName ( "Tribes-MembershipRecovery" + channelName );
        t.setDaemon ( true );
        t.start();
    }
    public RecoveryThread ( final McastServiceImpl parent ) {
        this.parent = parent;
    }
    public boolean stopService() {
        try {
            this.parent.stop ( 12 );
            return true;
        } catch ( Exception x ) {
            McastServiceImpl.access$000().warn ( McastServiceImpl.sm.getString ( "mcastServiceImpl.recovery.stopFailed" ), x );
            return false;
        }
    }
    public boolean startService() {
        try {
            this.parent.init();
            this.parent.start ( 12 );
            return true;
        } catch ( Exception x ) {
            McastServiceImpl.access$000().warn ( McastServiceImpl.sm.getString ( "mcastServiceImpl.recovery.startFailed" ), x );
            return false;
        }
    }
    @Override
    public void run() {
        boolean success = false;
        int attempt = 0;
        try {
            while ( !success ) {
                if ( McastServiceImpl.access$000().isInfoEnabled() ) {
                    McastServiceImpl.access$000().info ( McastServiceImpl.sm.getString ( "mcastServiceImpl.recovery" ) );
                }
                if ( this.stopService() & this.startService() ) {
                    success = true;
                    if ( McastServiceImpl.access$000().isInfoEnabled() ) {
                        McastServiceImpl.access$000().info ( McastServiceImpl.sm.getString ( "mcastServiceImpl.recovery.successful" ) );
                    }
                }
                try {
                    if ( success ) {
                        continue;
                    }
                    if ( McastServiceImpl.access$000().isInfoEnabled() ) {
                        McastServiceImpl.access$000().info ( McastServiceImpl.sm.getString ( "mcastServiceImpl.recovery.failed", Integer.toString ( ++attempt ), Long.toString ( this.parent.recoverySleepTime ) ) );
                    }
                    Thread.sleep ( this.parent.recoverySleepTime );
                } catch ( InterruptedException ex ) {}
            }
        } finally {
            RecoveryThread.running.set ( false );
        }
    }
    static {
        running = new AtomicBoolean ( false );
    }
}
