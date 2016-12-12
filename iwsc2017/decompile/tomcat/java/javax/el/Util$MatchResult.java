package javax.el;
private static class MatchResult implements Comparable<MatchResult> {
    private final int exact;
    private final int assignable;
    private final int coercible;
    private final boolean bridge;
    public MatchResult ( final int exact, final int assignable, final int coercible, final boolean bridge ) {
        this.exact = exact;
        this.assignable = assignable;
        this.coercible = coercible;
        this.bridge = bridge;
    }
    public int getExact() {
        return this.exact;
    }
    public int getAssignable() {
        return this.assignable;
    }
    public int getCoercible() {
        return this.coercible;
    }
    public boolean isBridge() {
        return this.bridge;
    }
    @Override
    public int compareTo ( final MatchResult o ) {
        int cmp = Integer.compare ( this.getExact(), o.getExact() );
        if ( cmp == 0 ) {
            cmp = Integer.compare ( this.getAssignable(), o.getAssignable() );
            if ( cmp == 0 ) {
                cmp = Integer.compare ( this.getCoercible(), o.getCoercible() );
                if ( cmp == 0 ) {
                    cmp = Boolean.compare ( o.isBridge(), this.isBridge() );
                }
            }
        }
        return cmp;
    }
}
