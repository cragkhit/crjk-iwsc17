package org.apache.juli;
private final class Cleaner extends Thread {
    @Override
    public void run() {
        if ( ClassLoaderLogManager.this.useShutdownHook ) {
            ClassLoaderLogManager.this.shutdown();
        }
    }
}
