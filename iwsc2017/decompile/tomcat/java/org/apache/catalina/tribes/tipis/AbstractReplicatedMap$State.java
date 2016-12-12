package org.apache.catalina.tribes.tipis;
private enum State {
    NEW ( false ),
    INITIALIZED ( true ),
    DESTROYED ( false );
    private final boolean available;
    private State ( final boolean available ) {
        this.available = available;
    }
    public boolean isAvailable() {
        return this.available;
    }
}
