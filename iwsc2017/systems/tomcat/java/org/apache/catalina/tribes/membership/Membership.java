package org.apache.catalina.tribes.membership;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.catalina.tribes.Member;
public class Membership implements Cloneable {
    protected static final Member[] EMPTY_MEMBERS = new Member[0];
    private final Object membersLock = new Object();
    protected final Member local;
    protected HashMap<Member, MbrEntry> map = new HashMap<>();
    protected volatile Member[] members = EMPTY_MEMBERS;
    protected final Comparator<Member> memberComparator;
    @Override
    public Object clone() {
        synchronized ( membersLock ) {
            Membership clone = new Membership ( local, memberComparator );
            @SuppressWarnings ( "unchecked" )
            final HashMap<Member, MbrEntry> tmpclone = ( HashMap<Member, MbrEntry> ) map.clone();
            clone.map = tmpclone;
            clone.members = members.clone();
            return clone;
        }
    }
    public Membership ( Member local, boolean includeLocal ) {
        this ( local, new MemberComparator(), includeLocal );
    }
    public Membership ( Member local ) {
        this ( local, false );
    }
    public Membership ( Member local, Comparator<Member> comp ) {
        this ( local, comp, false );
    }
    public Membership ( Member local, Comparator<Member> comp, boolean includeLocal ) {
        this.local = local;
        this.memberComparator = comp;
        if ( includeLocal ) {
            addMember ( local );
        }
    }
    public void reset() {
        synchronized ( membersLock ) {
            map.clear();
            members = EMPTY_MEMBERS ;
        }
    }
    public boolean memberAlive ( Member member ) {
        if ( member.equals ( local ) ) {
            return false;
        }
        boolean result = false;
        synchronized ( membersLock ) {
            MbrEntry entry = map.get ( member );
            if ( entry == null ) {
                entry = addMember ( member );
                result = true;
            } else {
                Member updateMember = entry.getMember();
                if ( updateMember.getMemberAliveTime() != member.getMemberAliveTime() ) {
                    updateMember.setMemberAliveTime ( member.getMemberAliveTime() );
                    updateMember.setPayload ( member.getPayload() );
                    updateMember.setCommand ( member.getCommand() );
                    Member[] newMembers = members.clone();
                    Arrays.sort ( newMembers, memberComparator );
                    members = newMembers;
                }
            }
            entry.accessed();
        }
        return result;
    }
    public MbrEntry addMember ( Member member ) {
        MbrEntry entry = new MbrEntry ( member );
        synchronized ( membersLock ) {
            if ( !map.containsKey ( member ) ) {
                map.put ( member, entry );
                Member results[] = new Member[members.length + 1];
                System.arraycopy ( members, 0, results, 0, members.length );
                results[members.length] = member;
                Arrays.sort ( results, memberComparator );
                members = results;
            }
        }
        return entry;
    }
    public void removeMember ( Member member ) {
        synchronized ( membersLock ) {
            map.remove ( member );
            int n = -1;
            for ( int i = 0; i < members.length; i++ ) {
                if ( members[i] == member || members[i].equals ( member ) ) {
                    n = i;
                    break;
                }
            }
            if ( n < 0 ) {
                return;
            }
            Member results[] = new Member[members.length - 1];
            int j = 0;
            for ( int i = 0; i < members.length; i++ ) {
                if ( i != n ) {
                    results[j++] = members[i];
                }
            }
            members = results;
        }
    }
    public Member[] expire ( long maxtime ) {
        synchronized ( membersLock ) {
            if ( !hasMembers() ) {
                return EMPTY_MEMBERS;
            }
            ArrayList<Member> list = null;
            Iterator<MbrEntry> i = map.values().iterator();
            while ( i.hasNext() ) {
                MbrEntry entry = i.next();
                if ( entry.hasExpired ( maxtime ) ) {
                    if ( list == null ) {
                        list = new java.util.ArrayList<>();
                    }
                    list.add ( entry.getMember() );
                }
            }
            if ( list != null ) {
                Member[] result = new Member[list.size()];
                list.toArray ( result );
                for ( int j = 0; j < result.length; j++ ) {
                    removeMember ( result[j] );
                }
                return result;
            } else {
                return EMPTY_MEMBERS ;
            }
        }
    }
    public boolean hasMembers() {
        return members.length > 0;
    }
    public Member getMember ( Member mbr ) {
        Member[] members = this.members;
        if ( members.length > 0 ) {
            for ( int i = 0; i < members.length; i++ ) {
                if ( members[i].equals ( mbr ) ) {
                    return members[i];
                }
            }
        }
        return null;
    }
    public boolean contains ( Member mbr ) {
        return getMember ( mbr ) != null;
    }
    public Member[] getMembers() {
        return members;
    }
    private static class MemberComparator implements Comparator<Member>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare ( Member m1, Member m2 ) {
            long result = m2.getMemberAliveTime() - m1.getMemberAliveTime();
            if ( result < 0 ) {
                return -1;
            } else if ( result == 0 ) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    protected static class MbrEntry {
        protected final Member mbr;
        protected long lastHeardFrom;
        public MbrEntry ( Member mbr ) {
            this.mbr = mbr;
        }
        public void accessed() {
            lastHeardFrom = System.currentTimeMillis();
        }
        public Member getMember() {
            return mbr;
        }
        public boolean hasExpired ( long maxtime ) {
            long delta = System.currentTimeMillis() - lastHeardFrom;
            return delta > maxtime;
        }
    }
}
