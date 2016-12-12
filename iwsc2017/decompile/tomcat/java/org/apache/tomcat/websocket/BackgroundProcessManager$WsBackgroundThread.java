package org.apache.tomcat.websocket;
private static class WsBackgroundThread extends Thread {
    private final BackgroundProcessManager manager;
    private volatile boolean running;
    public WsBackgroundThread ( final BackgroundProcessManager manager ) {
        this.running = true;
        this.setName ( "WebSocket background processing" );
        this.manager = manager;
    }
    @Override
    public void run() {
        while ( this.running ) {
            try {
                Thread.sleep ( 1000L );
            } catch ( InterruptedException ex ) {}
            BackgroundProcessManager.access$000 ( this.manager );
        }
    }
    public void halt() {
        this.setName ( "WebSocket background processing - stopping" );
        this.running = false;
    }
}
