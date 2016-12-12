package org.apache.catalina.tribes;
public interface MembershipListener {
    void memberAdded ( Member p0 );
    void memberDisappeared ( Member p0 );
}
