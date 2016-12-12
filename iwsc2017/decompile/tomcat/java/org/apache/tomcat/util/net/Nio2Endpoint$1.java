package org.apache.tomcat.util.net;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
class Nio2Endpoint$1 implements Runnable {
    @Override
    public void run() {
        try {
            for ( final Nio2Channel channel : Nio2Endpoint.this.getHandler().getOpenSockets() ) {
                Nio2Endpoint.this.closeSocket ( channel.getSocket() );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        } finally {
            Nio2Endpoint.access$002 ( Nio2Endpoint.this, true );
        }
    }
}
