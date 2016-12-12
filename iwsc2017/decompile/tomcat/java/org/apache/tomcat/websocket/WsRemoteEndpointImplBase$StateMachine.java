package org.apache.tomcat.websocket;
private static class StateMachine {
    private State state;
    private StateMachine() {
        this.state = State.OPEN;
    }
    public synchronized void streamStart() {
        this.checkState ( State.OPEN );
        this.state = State.STREAM_WRITING;
    }
    public synchronized void writeStart() {
        this.checkState ( State.OPEN );
        this.state = State.WRITER_WRITING;
    }
    public synchronized void binaryPartialStart() {
        this.checkState ( State.OPEN, State.BINARY_PARTIAL_READY );
        this.state = State.BINARY_PARTIAL_WRITING;
    }
    public synchronized void binaryStart() {
        this.checkState ( State.OPEN );
        this.state = State.BINARY_FULL_WRITING;
    }
    public synchronized void textPartialStart() {
        this.checkState ( State.OPEN, State.TEXT_PARTIAL_READY );
        this.state = State.TEXT_PARTIAL_WRITING;
    }
    public synchronized void textStart() {
        this.checkState ( State.OPEN );
        this.state = State.TEXT_FULL_WRITING;
    }
    public synchronized void complete ( final boolean last ) {
        if ( last ) {
            this.checkState ( State.TEXT_PARTIAL_WRITING, State.TEXT_FULL_WRITING, State.BINARY_PARTIAL_WRITING, State.BINARY_FULL_WRITING, State.STREAM_WRITING, State.WRITER_WRITING );
            this.state = State.OPEN;
        } else {
            this.checkState ( State.TEXT_PARTIAL_WRITING, State.BINARY_PARTIAL_WRITING, State.STREAM_WRITING, State.WRITER_WRITING );
            if ( this.state == State.TEXT_PARTIAL_WRITING ) {
                this.state = State.TEXT_PARTIAL_READY;
            } else if ( this.state == State.BINARY_PARTIAL_WRITING ) {
                this.state = State.BINARY_PARTIAL_READY;
            } else if ( this.state != State.WRITER_WRITING ) {
                if ( this.state != State.STREAM_WRITING ) {
                    throw new IllegalStateException ( "BUG: This code should never be called" );
                }
            }
        }
    }
    private void checkState ( final State... required ) {
        for ( final State state : required ) {
            if ( this.state == state ) {
                return;
            }
        }
        throw new IllegalStateException ( WsRemoteEndpointImplBase.access$400().getString ( "wsRemoteEndpoint.wrongState", this.state ) );
    }
}
