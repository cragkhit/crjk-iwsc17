package org.apache.catalina.tribes;
public interface MembershipListener {
    public void memberAdded ( Member member );
    public void memberDisappeared ( Member member );
}
