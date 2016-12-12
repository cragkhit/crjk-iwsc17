package org.apache.catalina.tribes;
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
