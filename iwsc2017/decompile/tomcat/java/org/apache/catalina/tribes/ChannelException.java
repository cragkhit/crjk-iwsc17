package org.apache.catalina.tribes;
import java.util.ArrayList;
public class ChannelException extends Exception {
    private static final long serialVersionUID = 1L;
    protected static final FaultyMember[] EMPTY_LIST;
    private ArrayList<FaultyMember> faultyMembers;
    public ChannelException() {
        this.faultyMembers = null;
    }
    public ChannelException ( final String message ) {
        super ( message );
        this.faultyMembers = null;
    }
    public ChannelException ( final String message, final Throwable cause ) {
        super ( message, cause );
        this.faultyMembers = null;
    }
    public ChannelException ( final Throwable cause ) {
        super ( cause );
        this.faultyMembers = null;
    }
    @Override
    public String getMessage() {
        final StringBuilder buf = new StringBuilder ( super.getMessage() );
        if ( this.faultyMembers == null || this.faultyMembers.size() == 0 ) {
            buf.append ( "; No faulty members identified." );
        } else {
            buf.append ( "; Faulty members:" );
            for ( int i = 0; i < this.faultyMembers.size(); ++i ) {
                final FaultyMember mbr = this.faultyMembers.get ( i );
                buf.append ( mbr.getMember().getName() );
                buf.append ( "; " );
            }
        }
        return buf.toString();
    }
    public boolean addFaultyMember ( final Member mbr, final Exception x ) {
        return this.addFaultyMember ( new FaultyMember ( mbr, x ) );
    }
    public int addFaultyMember ( final FaultyMember[] mbrs ) {
        int result = 0;
        for ( int i = 0; mbrs != null && i < mbrs.length; ++i ) {
            if ( this.addFaultyMember ( mbrs[i] ) ) {
                ++result;
            }
        }
        return result;
    }
    public boolean addFaultyMember ( final FaultyMember mbr ) {
        if ( this.faultyMembers == null ) {
            this.faultyMembers = new ArrayList<FaultyMember>();
        }
        return !this.faultyMembers.contains ( mbr ) && this.faultyMembers.add ( mbr );
    }
    public FaultyMember[] getFaultyMembers() {
        if ( this.faultyMembers == null ) {
            return ChannelException.EMPTY_LIST;
        }
        return this.faultyMembers.toArray ( new FaultyMember[this.faultyMembers.size()] );
    }
    static {
        EMPTY_LIST = new FaultyMember[0];
    }
    public static class FaultyMember {
        protected final Exception cause;
        protected final Member member;
        public FaultyMember ( final Member mbr, final Exception x ) {
            this.member = mbr;
            this.cause = x;
        }
        public Member getMember() {
            return this.member;
        }
        public Exception getCause() {
            return this.cause;
        }
        @Override
        public String toString() {
            return "FaultyMember:" + this.member.toString();
        }
        @Override
        public int hashCode() {
            return ( this.member != null ) ? this.member.hashCode() : 0;
        }
        @Override
        public boolean equals ( final Object o ) {
            return this.member != null && o instanceof FaultyMember && ( ( FaultyMember ) o ).member != null && this.member.equals ( ( ( FaultyMember ) o ).member );
        }
    }
}
