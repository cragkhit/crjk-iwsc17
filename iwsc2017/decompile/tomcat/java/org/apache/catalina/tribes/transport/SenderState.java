package org.apache.catalina.tribes.transport;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.catalina.tribes.Member;
import java.util.concurrent.ConcurrentMap;
public class SenderState {
    public static final int READY = 0;
    public static final int SUSPECT = 1;
    public static final int FAILING = 2;
    protected static final ConcurrentMap<Member, SenderState> memberStates;
    private volatile int state;
    public static SenderState getSenderState ( final Member member ) {
        return getSenderState ( member, true );
    }
    public static SenderState getSenderState ( final Member member, final boolean create ) {
        SenderState state = SenderState.memberStates.get ( member );
        if ( state == null && create ) {
            state = new SenderState();
            final SenderState current = SenderState.memberStates.putIfAbsent ( member, state );
            if ( current != null ) {
                state = current;
            }
        }
        return state;
    }
    public static void removeSenderState ( final Member member ) {
        SenderState.memberStates.remove ( member );
    }
    private SenderState() {
        this ( 0 );
    }
    private SenderState ( final int state ) {
        this.state = 0;
        this.state = state;
    }
    public boolean isSuspect() {
        return this.state == 1 || this.state == 2;
    }
    public void setSuspect() {
        this.state = 1;
    }
    public boolean isReady() {
        return this.state == 0;
    }
    public void setReady() {
        this.state = 0;
    }
    public boolean isFailing() {
        return this.state == 2;
    }
    public void setFailing() {
        this.state = 2;
    }
    static {
        memberStates = new ConcurrentHashMap<Member, SenderState>();
    }
}
