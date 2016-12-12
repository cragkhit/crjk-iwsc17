package org.apache.catalina.tribes.group;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
public static class HeartbeatThread extends Thread {
    private static final Log log;
    protected static int counter;
    protected volatile boolean doRun;
    protected final GroupChannel channel;
    protected final long sleepTime;
    protected static synchronized int inc() {
        return HeartbeatThread.counter++;
    }
    public HeartbeatThread ( final GroupChannel channel, final long sleepTime ) {
        this.doRun = true;
        this.setPriority ( 1 );
        String channelName = "";
        if ( channel.getName() != null ) {
            channelName = "[" + channel.getName() + "]";
        }
        this.setName ( "GroupChannel-Heartbeat" + channelName + "-" + inc() );
        this.setDaemon ( true );
        this.channel = channel;
        this.sleepTime = sleepTime;
    }
    public void stopHeartbeat() {
        this.doRun = false;
        this.interrupt();
    }
    @Override
    public void run() {
        while ( this.doRun ) {
            try {
                Thread.sleep ( this.sleepTime );
                this.channel.heartbeat();
            } catch ( InterruptedException ex ) {}
            catch ( Exception x ) {
                HeartbeatThread.log.error ( GroupChannel.sm.getString ( "groupChannel.unable.sendHeartbeat" ), x );
            }
        }
    }
    static {
        log = LogFactory.getLog ( HeartbeatThread.class );
        HeartbeatThread.counter = 1;
    }
}
