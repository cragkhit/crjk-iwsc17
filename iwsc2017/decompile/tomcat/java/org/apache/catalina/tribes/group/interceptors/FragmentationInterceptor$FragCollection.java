package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.ChannelMessage;
public static class FragCollection {
    private final long received;
    private final ChannelMessage msg;
    private final XByteBuffer[] frags;
    public FragCollection ( final ChannelMessage msg ) {
        this.received = System.currentTimeMillis();
        final int count = XByteBuffer.toInt ( msg.getMessage().getBytesDirect(), msg.getMessage().getLength() - 4 );
        this.frags = new XByteBuffer[count];
        this.msg = msg;
    }
    public void addMessage ( final ChannelMessage msg ) {
        msg.getMessage().trim ( 4 );
        final int nr = XByteBuffer.toInt ( msg.getMessage().getBytesDirect(), msg.getMessage().getLength() - 4 );
        msg.getMessage().trim ( 4 );
        this.frags[nr] = msg.getMessage();
    }
    public boolean complete() {
        boolean result = true;
        for ( int i = 0; i < this.frags.length && result; result = ( this.frags[i] != null ), ++i ) {}
        return result;
    }
    public ChannelMessage assemble() {
        if ( !this.complete() ) {
            throw new IllegalStateException ( FragmentationInterceptor.sm.getString ( "fragmentationInterceptor.fragments.missing" ) );
        }
        int buffersize = 0;
        for ( int i = 0; i < this.frags.length; ++i ) {
            buffersize += this.frags[i].getLength();
        }
        final XByteBuffer buf = new XByteBuffer ( buffersize, false );
        this.msg.setMessage ( buf );
        for ( int j = 0; j < this.frags.length; ++j ) {
            this.msg.getMessage().append ( this.frags[j].getBytesDirect(), 0, this.frags[j].getLength() );
        }
        return this.msg;
    }
    public boolean expired ( final long expire ) {
        return System.currentTimeMillis() - this.received > expire;
    }
}
