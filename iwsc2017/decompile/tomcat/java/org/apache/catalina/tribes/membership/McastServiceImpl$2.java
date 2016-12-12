package org.apache.catalina.tribes.membership;
import org.apache.catalina.tribes.Member;
class McastServiceImpl$2 implements Runnable {
    final   Member val$m;
    @Override
    public void run() {
        final String name = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName ( "Membership-MemberAdded." );
            McastServiceImpl.this.service.memberAdded ( this.val$m );
        } finally {
            Thread.currentThread().setName ( name );
        }
    }
}
