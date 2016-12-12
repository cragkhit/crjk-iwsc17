package org.apache.tomcat.util.net;
public static class SocketList {
    protected volatile int size;
    protected int pos;
    protected long[] sockets;
    protected long[] timeouts;
    protected int[] flags;
    protected SocketInfo info;
    public SocketList ( final int size ) {
        this.info = new SocketInfo();
        this.size = 0;
        this.pos = 0;
        this.sockets = new long[size];
        this.timeouts = new long[size];
        this.flags = new int[size];
    }
    public int size() {
        return this.size;
    }
    public SocketInfo get() {
        if ( this.pos == this.size ) {
            return null;
        }
        this.info.socket = this.sockets[this.pos];
        this.info.timeout = this.timeouts[this.pos];
        this.info.flags = this.flags[this.pos];
        ++this.pos;
        return this.info;
    }
    public void clear() {
        this.size = 0;
        this.pos = 0;
    }
    public boolean add ( final long socket, final long timeout, final int flag ) {
        if ( this.size == this.sockets.length ) {
            return false;
        }
        for ( int i = 0; i < this.size; ++i ) {
            if ( this.sockets[i] == socket ) {
                this.flags[i] = SocketInfo.merge ( this.flags[i], flag );
                return true;
            }
        }
        this.sockets[this.size] = socket;
        this.timeouts[this.size] = timeout;
        this.flags[this.size] = flag;
        ++this.size;
        return true;
    }
    public boolean remove ( final long socket ) {
        for ( int i = 0; i < this.size; ++i ) {
            if ( this.sockets[i] == socket ) {
                this.sockets[i] = this.sockets[this.size - 1];
                this.timeouts[i] = this.timeouts[this.size - 1];
                this.flags[this.size] = this.flags[this.size - 1];
                --this.size;
                return true;
            }
        }
        return false;
    }
    public void duplicate ( final SocketList copy ) {
        copy.size = this.size;
        copy.pos = this.pos;
        System.arraycopy ( this.sockets, 0, copy.sockets, 0, this.size );
        System.arraycopy ( this.timeouts, 0, copy.timeouts, 0, this.size );
        System.arraycopy ( this.flags, 0, copy.flags, 0, this.size );
    }
}
