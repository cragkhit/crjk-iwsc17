package org.apache.catalina.tribes.group.interceptors;
import java.util.Comparator;
import java.util.Arrays;
import org.apache.catalina.tribes.group.AbsoluteOrder;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.tribes.ChannelException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class SimpleCoordinator extends ChannelInterceptorBase {
    private Member[] view;
    private final AtomicBoolean membershipChanged;
    public SimpleCoordinator() {
        this.membershipChanged = new AtomicBoolean();
    }
    private void membershipChanged() {
        this.membershipChanged.set ( true );
    }
    @Override
    public void memberAdded ( final Member member ) {
        super.memberAdded ( member );
        this.membershipChanged();
        this.installViewWhenStable();
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        super.memberDisappeared ( member );
        this.membershipChanged();
        this.installViewWhenStable();
    }
    protected void viewChange ( final Member[] view ) {
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        super.start ( svc );
        this.installViewWhenStable();
    }
    private void installViewWhenStable() {
        int stableCount = 0;
        while ( stableCount < 10 ) {
            if ( this.membershipChanged.compareAndSet ( true, false ) ) {
                stableCount = 0;
            } else {
                ++stableCount;
            }
            try {
                TimeUnit.MILLISECONDS.sleep ( 250L );
            } catch ( InterruptedException e ) {
                Thread.currentThread().interrupt();
            }
        }
        final Member[] members = this.getMembers();
        final Member[] view = new Member[members.length + 1];
        System.arraycopy ( members, 0, view, 0, members.length );
        view[members.length] = this.getLocalMember ( false );
        Arrays.sort ( view, AbsoluteOrder.comp );
        if ( Arrays.equals ( view, this.view ) ) {
            return;
        }
        this.viewChange ( this.view = view );
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        super.stop ( svc );
    }
    public Member[] getView() {
        return this.view;
    }
    public Member getCoordinator() {
        return ( this.view == null ) ? null : this.view[0];
    }
    public boolean isCoordinator() {
        return this.view != null && this.getLocalMember ( false ).equals ( this.getCoordinator() );
    }
}
