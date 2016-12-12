package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.ChannelMessage;
protected static class MessageOrder {
    private final long received;
    private MessageOrder next;
    private final int msgNr;
    private ChannelMessage msg;
    public MessageOrder ( final int msgNr, final ChannelMessage msg ) {
        this.received = System.currentTimeMillis();
        this.msg = null;
        this.msgNr = msgNr;
        this.msg = msg;
    }
    public boolean isExpired ( final long expireTime ) {
        return System.currentTimeMillis() - this.received > expireTime;
    }
    public ChannelMessage getMessage() {
        return this.msg;
    }
    public void setMessage ( final ChannelMessage msg ) {
        this.msg = msg;
    }
    public void setNext ( final MessageOrder order ) {
        this.next = order;
    }
    public MessageOrder getNext() {
        return this.next;
    }
    public int getCount() {
        int counter = 1;
        for ( MessageOrder tmp = this.next; tmp != null; tmp = tmp.next ) {
            ++counter;
        }
        return counter;
    }
    public static MessageOrder add ( final MessageOrder head, final MessageOrder add ) {
        if ( head == null ) {
            return add;
        }
        if ( add == null ) {
            return head;
        }
        if ( head == add ) {
            return add;
        }
        if ( head.getMsgNr() > add.getMsgNr() ) {
            add.next = head;
            return add;
        }
        MessageOrder iter = head;
        MessageOrder prev = null;
        while ( iter.getMsgNr() < add.getMsgNr() && iter.next != null ) {
            prev = iter;
            iter = iter.next;
        }
        if ( iter.getMsgNr() < add.getMsgNr() ) {
            add.next = iter.next;
            iter.next = add;
        } else {
            if ( iter.getMsgNr() <= add.getMsgNr() ) {
                throw new ArithmeticException ( OrderInterceptor.sm.getString ( "orderInterceptor.messageAdded.sameCounter" ) );
            }
            prev.next = add;
            add.next = iter;
        }
        return head;
    }
    public int getMsgNr() {
        return this.msgNr;
    }
}
