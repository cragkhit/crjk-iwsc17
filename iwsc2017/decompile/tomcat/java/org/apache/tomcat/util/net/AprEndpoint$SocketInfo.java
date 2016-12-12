package org.apache.tomcat.util.net;
public static class SocketInfo {
    public long socket;
    public long timeout;
    public int flags;
    public boolean read() {
        return ( this.flags & 0x1 ) == 0x1;
    }
    public boolean write() {
        return ( this.flags & 0x4 ) == 0x4;
    }
    public static int merge ( final int flag1, final int flag2 ) {
        return ( flag1 & 0x1 ) | ( flag2 & 0x1 ) | ( ( flag1 & 0x4 ) | ( flag2 & 0x4 ) );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "Socket: [" );
        sb.append ( this.socket );
        sb.append ( "], timeout: [" );
        sb.append ( this.timeout );
        sb.append ( "], flags: [" );
        sb.append ( this.flags );
        return sb.toString();
    }
}
