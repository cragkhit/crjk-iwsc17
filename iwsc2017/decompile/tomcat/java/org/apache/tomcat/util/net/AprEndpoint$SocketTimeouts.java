package org.apache.tomcat.util.net;
public static class SocketTimeouts {
    protected int size;
    protected long[] sockets;
    protected long[] timeouts;
    protected int pos;
    public SocketTimeouts ( final int size ) {
        this.pos = 0;
        this.size = 0;
        this.sockets = new long[size];
        this.timeouts = new long[size];
    }
    public void add ( final long socket, final long timeout ) {
        this.sockets[this.size] = socket;
        this.timeouts[this.size] = timeout;
        ++this.size;
    }
    public long remove ( final long socket ) {
        long result = 0L;
        for ( int i = 0; i < this.size; ++i ) {
            if ( this.sockets[i] == socket ) {
                result = this.timeouts[i];
                this.sockets[i] = this.sockets[this.size - 1];
                this.timeouts[i] = this.timeouts[this.size - 1];
                --this.size;
                break;
            }
        }
        return result;
    }
    public long check ( final long date ) {
        while ( this.pos < this.size ) {
            if ( date >= this.timeouts[this.pos] ) {
                final long result = this.sockets[this.pos];
                this.sockets[this.pos] = this.sockets[this.size - 1];
                this.timeouts[this.pos] = this.timeouts[this.size - 1];
                --this.size;
                return result;
            }
            ++this.pos;
        }
        this.pos = 0;
        return 0L;
    }
}
