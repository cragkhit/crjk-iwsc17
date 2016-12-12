package org.apache.catalina.tribes.group;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Arrays;
import org.apache.catalina.tribes.Member;
public class AbsoluteOrder {
    public static final AbsoluteComparator comp;
    public static void absoluteOrder ( final Member[] members ) {
        if ( members == null || members.length <= 1 ) {
            return;
        }
        Arrays.sort ( members, AbsoluteOrder.comp );
    }
    public static void absoluteOrder ( final List<Member> members ) {
        if ( members == null || members.size() <= 1 ) {
            return;
        }
        Collections.sort ( members, AbsoluteOrder.comp );
    }
    static {
        comp = new AbsoluteComparator();
    }
    public static class AbsoluteComparator implements Comparator<Member>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare ( final Member m1, final Member m2 ) {
            int result = this.compareIps ( m1, m2 );
            if ( result == 0 ) {
                result = this.comparePorts ( m1, m2 );
            }
            if ( result == 0 ) {
                result = this.compareIds ( m1, m2 );
            }
            return result;
        }
        public int compareIps ( final Member m1, final Member m2 ) {
            return this.compareBytes ( m1.getHost(), m2.getHost() );
        }
        public int comparePorts ( final Member m1, final Member m2 ) {
            return this.compareInts ( m1.getPort(), m2.getPort() );
        }
        public int compareIds ( final Member m1, final Member m2 ) {
            return this.compareBytes ( m1.getUniqueId(), m2.getUniqueId() );
        }
        protected int compareBytes ( final byte[] d1, final byte[] d2 ) {
            int result = 0;
            if ( d1.length == d2.length ) {
                for ( int i = 0; result == 0 && i < d1.length; result = this.compareBytes ( d1[i], d2[i] ), ++i ) {}
            } else if ( d1.length < d2.length ) {
                result = -1;
            } else {
                result = 1;
            }
            return result;
        }
        protected int compareBytes ( final byte b1, final byte b2 ) {
            return this.compareInts ( b1, b2 );
        }
        protected int compareInts ( final int b1, final int b2 ) {
            int result = 0;
            if ( b1 != b2 ) {
                if ( b1 < b2 ) {
                    result = -1;
                } else {
                    result = 1;
                }
            }
            return result;
        }
    }
}
