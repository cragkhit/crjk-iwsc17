package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
class StaticMembershipInterceptor$1 extends Thread {
    final   ChannelInterceptorBase val$base;
    final   Member val$member;
    @Override
    public void run() {
        this.val$base.memberAdded ( this.val$member );
        if ( StaticMembershipInterceptor.this.getfirstInterceptor().getMember ( this.val$member ) != null ) {
            StaticMembershipInterceptor.this.sendLocalMember ( new Member[] { this.val$member } );
        }
    }
}
