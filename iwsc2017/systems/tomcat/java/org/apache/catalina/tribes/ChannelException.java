package org.apache.catalina.tribes;
import java.util.ArrayList;
public class ChannelException extends Exception {
    private static final long serialVersionUID = 1L;
    protected static final FaultyMember[] EMPTY_LIST = new FaultyMember[0];
    private ArrayList<FaultyMember> faultyMembers = null;
    public ChannelException() {
        super();
    }
    public ChannelException ( String message ) {
        super ( message );
    }
    public ChannelException ( String message, Throwable cause ) {
        super ( message, cause );
    }
    public ChannelException ( Throwable cause ) {
        super ( cause );
    }
    @Override
    public String getMessage() {
        StringBuilder buf = new StringBuilder ( super.getMessage() );
        if ( faultyMembers == null || faultyMembers.size() == 0 ) {
            buf.append ( "; No faulty members identified." );
        } else {
            buf.append ( "; Faulty members:" );
            for ( int i = 0; i < faultyMembers.size(); i++ ) {
                FaultyMember mbr = faultyMembers.get ( i );
                buf.append ( mbr.getMember().getName() );
                buf.append ( "; " );
            }
        }
        return buf.toString();
    }
    public boolean addFaultyMember ( Member mbr, Exception x ) {
        return addFaultyMember ( new FaultyMember ( mbr, x ) );
    }
    public int addFaultyMember ( FaultyMember[] mbrs ) {
        int result = 0;
        for ( int i = 0; mbrs != null && i < mbrs.length; i++ ) {
            if ( addFaultyMember ( mbrs[i] ) ) {
                result++;
            }
        }
        return result;
    }
    public boolean addFaultyMember ( FaultyMember mbr ) {
        if ( this.faultyMembers == null ) {
            this.faultyMembers = new ArrayList<>();
        }
        if ( !faultyMembers.contains ( mbr ) ) {
            return faultyMembers.add ( mbr );
        } else {
            return false;
        }
    }
    public FaultyMember[] getFaultyMembers() {
        if ( this.faultyMembers == null ) {
            return EMPTY_LIST;
        }
        return faultyMembers.toArray ( new FaultyMember[faultyMembers.size()] );
    }
    public static class FaultyMember {
        protected final Exception cause;
        protected final Member member;
        public FaultyMember ( Member mbr, Exception x ) {
            this.member = mbr;
            this.cause = x;
        }
        public Member getMember() {
            return member;
        }
        public Exception getCause() {
            return cause;
        }
        @Override
        public String toString() {
            return "FaultyMember:" + member.toString();
        }
        @Override
        public int hashCode() {
            return ( member != null ) ? member.hashCode() : 0;
        }
        @Override
        public boolean equals ( Object o ) {
            if ( member == null || ( ! ( o instanceof FaultyMember ) ) || ( ( ( FaultyMember ) o ).member == null ) ) {
                return false;
            }
            return member.equals ( ( ( FaultyMember ) o ).member );
        }
    }
}
