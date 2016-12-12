package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelInterceptor;
public abstract class ChannelInterceptorBase implements ChannelInterceptor {
    private ChannelInterceptor next;
    private ChannelInterceptor previous;
    private Channel channel;
    protected int optionFlag;
    public ChannelInterceptorBase() {
        this.optionFlag = 0;
    }
    public boolean okToProcess ( final int messageFlags ) {
        return this.optionFlag == 0 || ( this.optionFlag & messageFlags ) == this.optionFlag;
    }
    @Override
    public final void setNext ( final ChannelInterceptor next ) {
        this.next = next;
    }
    @Override
    public final ChannelInterceptor getNext() {
        return this.next;
    }
    @Override
    public final void setPrevious ( final ChannelInterceptor previous ) {
        this.previous = previous;
    }
    @Override
    public void setOptionFlag ( final int optionFlag ) {
        this.optionFlag = optionFlag;
    }
    @Override
    public final ChannelInterceptor getPrevious() {
        return this.previous;
    }
    @Override
    public int getOptionFlag() {
        return this.optionFlag;
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        if ( this.getNext() != null ) {
            this.getNext().sendMessage ( destination, msg, payload );
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( this.getPrevious() != null ) {
            this.getPrevious().messageReceived ( msg );
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        if ( this.getPrevious() != null ) {
            this.getPrevious().memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        if ( this.getPrevious() != null ) {
            this.getPrevious().memberDisappeared ( member );
        }
    }
    @Override
    public void heartbeat() {
        if ( this.getNext() != null ) {
            this.getNext().heartbeat();
        }
    }
    @Override
    public boolean hasMembers() {
        return this.getNext() != null && this.getNext().hasMembers();
    }
    @Override
    public Member[] getMembers() {
        if ( this.getNext() != null ) {
            return this.getNext().getMembers();
        }
        return null;
    }
    @Override
    public Member getMember ( final Member mbr ) {
        if ( this.getNext() != null ) {
            return this.getNext().getMember ( mbr );
        }
        return null;
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        if ( this.getNext() != null ) {
            return this.getNext().getLocalMember ( incAlive );
        }
        return null;
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        if ( this.getNext() != null ) {
            this.getNext().start ( svc );
        }
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        if ( this.getNext() != null ) {
            this.getNext().stop ( svc );
        }
        this.channel = null;
    }
    @Override
    public void fireInterceptorEvent ( final InterceptorEvent event ) {
    }
    @Override
    public Channel getChannel() {
        return this.channel;
    }
    @Override
    public void setChannel ( final Channel channel ) {
        this.channel = channel;
    }
}
