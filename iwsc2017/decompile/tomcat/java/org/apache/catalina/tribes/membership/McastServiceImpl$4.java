package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.Member;
class McastServiceImpl$4 implements Runnable {
    final   Member val$member;
    @Override
    public void run() {
        final String name = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName ( "Membership-MemberExpired." );
            McastServiceImpl.this.service.memberDisappeared ( this.val$member );
        } finally {
            Thread.currentThread().setName ( name );
        }
    }
}
