package org.apache.catalina.tribes.transport.bio;
import java.util.Map;
import java.io.IOException;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.AbstractSender;
public class MultipointBioSender extends AbstractSender implements MultiPointSender {
    protected final HashMap<Member, BioSender> bioSenders;
    public MultipointBioSender() {
        this.bioSenders = new HashMap<Member, BioSender>();
    }
    @Override
    public synchronized void sendMessage ( final Member[] destination, final ChannelMessage msg ) throws ChannelException {
        final byte[] data = XByteBuffer.createDataPackage ( ( ChannelData ) msg );
        final BioSender[] senders = this.setupForSend ( destination );
        ChannelException cx = null;
        for ( int i = 0; i < senders.length; ++i ) {
            try {
                senders[i].sendMessage ( data, ( msg.getOptions() & 0x2 ) == 0x2 );
            } catch ( Exception x ) {
                if ( cx == null ) {
                    cx = new ChannelException ( x );
                }
                cx.addFaultyMember ( destination[i], x );
            }
        }
        if ( cx != null ) {
            throw cx;
        }
    }
    protected BioSender[] setupForSend ( final Member[] destination ) throws ChannelException {
        ChannelException cx = null;
        final BioSender[] result = new BioSender[destination.length];
        for ( int i = 0; i < destination.length; ++i ) {
            try {
                BioSender sender = this.bioSenders.get ( destination[i] );
                if ( sender == null ) {
                    sender = new BioSender();
                    AbstractSender.transferProperties ( this, sender );
                    sender.setDestination ( destination[i] );
                    this.bioSenders.put ( destination[i], sender );
                }
                result[i] = sender;
                if ( !result[i].isConnected() ) {
                    result[i].connect();
                }
                result[i].keepalive();
            } catch ( Exception x ) {
                if ( cx == null ) {
                    cx = new ChannelException ( x );
                }
                cx.addFaultyMember ( destination[i], x );
            }
        }
        if ( cx != null ) {
            throw cx;
        }
        return result;
    }
    @Override
    public void connect() throws IOException {
        this.setConnected ( true );
    }
    private synchronized void close() throws ChannelException {
        ChannelException x = null;
        final Object[] members = this.bioSenders.keySet().toArray();
        for ( int i = 0; i < members.length; ++i ) {
            final Member mbr = ( Member ) members[i];
            try {
                final BioSender sender = this.bioSenders.get ( mbr );
                sender.disconnect();
            } catch ( Exception e ) {
                if ( x == null ) {
                    x = new ChannelException ( e );
                }
                x.addFaultyMember ( mbr, e );
            }
            this.bioSenders.remove ( mbr );
        }
        if ( x != null ) {
            throw x;
        }
    }
    @Override
    public void add ( final Member member ) {
    }
    @Override
    public void remove ( final Member member ) {
        final BioSender sender = this.bioSenders.remove ( member );
        if ( sender != null ) {
            sender.disconnect();
        }
    }
    @Override
    public synchronized void disconnect() {
        try {
            this.close();
        } catch ( Exception ex ) {}
        this.setConnected ( false );
    }
    public void finalize() throws Throwable {
        try {
            this.disconnect();
        } catch ( Exception ex ) {}
        super.finalize();
    }
    @Override
    public boolean keepalive() {
        final boolean result = false;
        final Map.Entry<Member, BioSender>[] entries = this.bioSenders.entrySet().toArray ( new Map.Entry[this.bioSenders.size()] );
        for ( int i = 0; i < entries.length; ++i ) {
            final BioSender sender = entries[i].getValue();
            if ( sender.keepalive() ) {
                this.bioSenders.remove ( entries[i].getKey() );
            }
        }
        return result;
    }
}
