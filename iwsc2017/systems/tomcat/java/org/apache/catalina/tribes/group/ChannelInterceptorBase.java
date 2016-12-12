package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
public abstract class ChannelInterceptorBase implements ChannelInterceptor {
    private ChannelInterceptor next;
    private ChannelInterceptor previous;
    private Channel channel;
    protected int optionFlag = 0;
    public ChannelInterceptorBase() {
    }
    public boolean okToProcess ( int messageFlags ) {
        if ( this.optionFlag == 0 ) {
            return true;
        }
        return ( ( optionFlag & messageFlags ) == optionFlag );
    }
    @Override
    public final void setNext ( ChannelInterceptor next ) {
        this.next = next;
    }
    @Override
    public final ChannelInterceptor getNext() {
        return next;
    }
    @Override
    public final void setPrevious ( ChannelInterceptor previous ) {
        this.previous = previous;
    }
    @Override
    public void setOptionFlag ( int optionFlag ) {
        this.optionFlag = optionFlag;
    }
    @Override
    public final ChannelInterceptor getPrevious() {
        return previous;
    }
    @Override
    public int getOptionFlag() {
        return optionFlag;
    }
    @Override
    public void sendMessage ( Member[] destination, ChannelMessage msg, InterceptorPayload payload ) throws
        ChannelException {
        if ( getNext() != null ) {
            getNext().sendMessage ( destination, msg, payload );
        }
    }
    @Override
    public void messageReceived ( ChannelMessage msg ) {
        if ( getPrevious() != null ) {
            getPrevious().messageReceived ( msg );
        }
    }
    @Override
    public void memberAdded ( Member member ) {
        if ( getPrevious() != null ) {
            getPrevious().memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( Member member ) {
        if ( getPrevious() != null ) {
            getPrevious().memberDisappeared ( member );
        }
    }
    @Override
    public void heartbeat() {
        if ( getNext() != null ) {
            getNext().heartbeat();
        }
    }
    @Override
    public boolean hasMembers() {
        if ( getNext() != null ) {
            return getNext().hasMembers();
        } else {
            return false;
        }
    }
    @Override
    public Member[] getMembers() {
        if ( getNext() != null ) {
            return getNext().getMembers();
        } else {
            return null;
        }
    }
    @Override
    public Member getMember ( Member mbr ) {
        if ( getNext() != null ) {
            return getNext().getMember ( mbr );
        } else {
            return null;
        }
    }
    @Override
    public Member getLocalMember ( boolean incAlive ) {
        if ( getNext() != null ) {
            return getNext().getLocalMember ( incAlive );
        } else {
            return null;
        }
    }
    @Override
    public void start ( int svc ) throws ChannelException {
        if ( getNext() != null ) {
            getNext().start ( svc );
        }
    }
    @Override
    public void stop ( int svc ) throws ChannelException {
        if ( getNext() != null ) {
            getNext().stop ( svc );
        }
        channel = null;
    }
    @Override
    public void fireInterceptorEvent ( InterceptorEvent event ) {
    }
    @Override
    public Channel getChannel() {
        return channel;
    }
    @Override
    public void setChannel ( Channel channel ) {
        this.channel = channel;
    }
}
