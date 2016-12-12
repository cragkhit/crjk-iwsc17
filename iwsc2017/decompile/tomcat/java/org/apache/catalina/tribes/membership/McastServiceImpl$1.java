package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.Member;
class McastServiceImpl$1 implements Runnable {
    final   Member val$m;
    @Override
    public void run() {
        final String name = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName ( "Membership-MemberDisappeared." );
            McastServiceImpl.this.service.memberDisappeared ( this.val$m );
        } finally {
            Thread.currentThread().setName ( name );
        }
    }
}
