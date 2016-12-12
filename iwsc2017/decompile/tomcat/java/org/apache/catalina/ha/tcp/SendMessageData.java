package org.apache.catalina.ha.tcp;
import org.apache.catalina.tribes.Member;
public class SendMessageData {
    private Object message;
    private Member destination;
    private Exception exception;
    public SendMessageData ( final Object message, final Member destination, final Exception exception ) {
        this.message = message;
        this.destination = destination;
        this.exception = exception;
    }
    public Member getDestination() {
        return this.destination;
    }
    public Exception getException() {
        return this.exception;
    }
    public Object getMessage() {
        return this.message;
    }
}
