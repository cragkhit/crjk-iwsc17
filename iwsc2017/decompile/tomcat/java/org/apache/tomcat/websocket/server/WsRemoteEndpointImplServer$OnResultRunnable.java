package org.apache.tomcat.websocket.server;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
private static class OnResultRunnable implements Runnable {
    private final SendHandler sh;
    private final Throwable t;
    private OnResultRunnable ( final SendHandler sh, final Throwable t ) {
        this.sh = sh;
        this.t = t;
    }
    @Override
    public void run() {
        if ( this.t == null ) {
            this.sh.onResult ( new SendResult() );
        } else {
            this.sh.onResult ( new SendResult ( this.t ) );
        }
    }
}
