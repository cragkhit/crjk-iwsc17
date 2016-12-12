package org.apache.catalina.tribes.group.interceptors;
protected class PingThread extends Thread {
    @Override
    public void run() {
        while ( TcpPingInterceptor.this.running ) {
            try {
                Thread.sleep ( TcpPingInterceptor.this.interval );
                TcpPingInterceptor.this.sendPing();
            } catch ( InterruptedException ex ) {}
            catch ( Exception x ) {
                TcpPingInterceptor.access$000().warn ( TcpPingInterceptor.sm.getString ( "tcpPingInterceptor.pingFailed.pingThread" ), x );
            }
        }
    }
}
