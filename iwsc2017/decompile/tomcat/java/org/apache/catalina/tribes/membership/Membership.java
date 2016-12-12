package org.apache.catalina.tribes.membership;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.catalina.tribes.Member;
public class Membership implements Cloneable {
    protected static final Member[] EMPTY_MEMBERS;
    private final Object membersLock;
    protected final Member local;
    protected HashMap<Member, MbrEntry> map;
    protected volatile Member[] members;
    protected final Comparator<Member> memberComparator;
    public Object clone() {
        synchronized ( this.membersLock ) {
            final Membership clone = new Membership ( this.local, this.memberComparator );
            final HashMap<Member, MbrEntry> tmpclone = ( HashMap<Member, MbrEntry> ) this.map.clone();
            clone.map = tmpclone;
            clone.members = this.members.clone();
            return clone;
        }
    }
    public Membership ( final Member local, final boolean includeLocal ) {
        this ( local, new MemberComparator(), includeLocal );
    }
    public Membership ( final Member local ) {
        this ( local, false );
    }
    public Membership ( final Member local, final Comparator<Member> comp ) {
        this ( local, comp, false );
    }
    public Membership ( final Member local, final Comparator<Member> comp, final boolean includeLocal ) {
        this.membersLock = new Object();
        this.map = new HashMap<Member, MbrEntry>();
        this.members = Membership.EMPTY_MEMBERS;
        this.local = local;
        this.memberComparator = comp;
        if ( includeLocal ) {
            this.addMember ( local );
        }
    }
    public void reset() {
        synchronized ( this.membersLock ) {
            this.map.clear();
            this.members = Membership.EMPTY_MEMBERS;
        }
    }
    public boolean memberAlive ( final Member member ) {
        if ( member.equals ( this.local ) ) {
            return false;
        }
        boolean result = false;
        synchronized ( this.membersLock ) {
            MbrEntry entry = this.map.get ( member );
            if ( entry == null ) {
                entry = this.addMember ( member );
                result = true;
            } else {
                final Member updateMember = entry.getMember();
                if ( updateMember.getMemberAliveTime() != member.getMemberAliveTime() ) {
                    updateMember.setMemberAliveTime ( member.getMemberAliveTime() );
                    updateMember.setPayload ( member.getPayload() );
                    updateMember.setCommand ( member.getCommand() );
                    final Member[] newMembers = this.members.clone();
                    Arrays.sort ( newMembers, this.memberComparator );
                    this.members = newMembers;
                }
            }
            entry.accessed();
        }
        return result;
    }
    public MbrEntry addMember ( final Member member ) {
        final MbrEntry entry = new MbrEntry ( member );
        synchronized ( this.membersLock ) {
            if ( !this.map.containsKey ( member ) ) {
                this.map.put ( member, entry );
                final Member[] results = new Member[this.members.length + 1];
                System.arraycopy ( this.members, 0, results, 0, this.members.length );
                results[this.members.length] = member;
                Arrays.sort ( results, this.memberComparator );
                this.members = results;
            }
        }
        return entry;
    }
    public void removeMember ( final Member member ) {
        synchronized ( this.membersLock ) {
            this.map.remove ( member );
            int n = -1;
            for ( int i = 0; i < this.members.length; ++i ) {
                if ( this.members[i] == member || this.members[i].equals ( member ) ) {
                    n = i;
                    break;
                }
            }
            if ( n < 0 ) {
                return;
            }
            final Member[] results = new Member[this.members.length - 1];
            int j = 0;
            for ( int k = 0; k < this.members.length; ++k ) {
                if ( k != n ) {
                    results[j++] = this.members[k];
                }
            }
            this.members = results;
        }
    }
    public Member[] expire ( final long maxtime ) {
        synchronized ( this.membersLock ) {
            if ( !this.hasMembers() ) {
                return Membership.EMPTY_MEMBERS;
            }
            ArrayList<Member> list = null;
            for ( final MbrEntry entry : this.map.values() ) {
                if ( entry.hasExpired ( maxtime ) ) {
                    if ( list == null ) {
                        list = new ArrayList<Member>();
                    }
                    list.add ( entry.getMember() );
                }
            }
            if ( list != null ) {
                final Member[] result = new Member[list.size()];
                list.toArray ( result );
                for ( int j = 0; j < result.length; ++j ) {
                    this.removeMember ( result[j] );
                }
                return result;
            }
            return Membership.EMPTY_MEMBERS;
        }
    }
    public boolean hasMembers() {
        return this.members.length > 0;
    }
    public Member getMember ( final Member mbr ) {
        final Member[] members = this.members;
        if ( members.length > 0 ) {
            for ( int i = 0; i < members.length; ++i ) {
                if ( members[i].equals ( mbr ) ) {
                    return members[i];
                }
            }
        }
        return null;
    }
    public boolean contains ( final Member mbr ) {
        return this.getMember ( mbr ) != null;
    }
    public Member[] getMembers() {
        return this.members;
    }
    static {
        EMPTY_MEMBERS = new Member[0];
    }
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
    protected static class MbrEntry {
        protected final Member mbr;
        protected long lastHeardFrom;
        public MbrEntry ( final Member mbr ) {
            this.mbr = mbr;
        }
        public void accessed() {
            this.lastHeardFrom = System.currentTimeMillis();
        }
        public Member getMember() {
            return this.mbr;
        }
        public boolean hasExpired ( final long maxtime ) {
            final long delta = System.currentTimeMillis() - this.lastHeardFrom;
            return delta > maxtime;
        }
    }
}
