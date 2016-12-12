package org.apache.catalina.tribes.membership;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import java.util.Comparator;
private static class MemberComparator implements Comparator<Member>, Serializable {
    private static final long serialVersionUID = 1L;
    @Override
    public int compare ( final Member m1, final Member m2 ) {
        final long result = m2.getMemberAliveTime() - m1.getMemberAliveTime();
        if ( result < 0L ) {
            return -1;
        }
        if ( result == 0L ) {
            return 0;
        }
        return 1;
    }
}
