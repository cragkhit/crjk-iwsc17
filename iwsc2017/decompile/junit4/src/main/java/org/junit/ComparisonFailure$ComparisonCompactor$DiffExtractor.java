package org.junit;
private class DiffExtractor {
    private final String sharedPrefix;
    private final String sharedSuffix;
    private DiffExtractor() {
        this.sharedPrefix = ComparisonCompactor.access$100 ( ComparisonCompactor.this );
        this.sharedSuffix = ComparisonCompactor.access$200 ( ComparisonCompactor.this, this.sharedPrefix );
    }
    public String expectedDiff() {
        return this.extractDiff ( ComparisonCompactor.access$300 ( ComparisonCompactor.this ) );
    }
    public String actualDiff() {
        return this.extractDiff ( ComparisonCompactor.access$400 ( ComparisonCompactor.this ) );
    }
    public String compactPrefix() {
        if ( this.sharedPrefix.length() <= ComparisonCompactor.access$500 ( ComparisonCompactor.this ) ) {
            return this.sharedPrefix;
        }
        return "..." + this.sharedPrefix.substring ( this.sharedPrefix.length() - ComparisonCompactor.access$500 ( ComparisonCompactor.this ) );
    }
    public String compactSuffix() {
        if ( this.sharedSuffix.length() <= ComparisonCompactor.access$500 ( ComparisonCompactor.this ) ) {
            return this.sharedSuffix;
        }
        return this.sharedSuffix.substring ( 0, ComparisonCompactor.access$500 ( ComparisonCompactor.this ) ) + "...";
    }
    private String extractDiff ( final String source ) {
        return "[" + source.substring ( this.sharedPrefix.length(), source.length() - this.sharedSuffix.length() ) + "]";
    }
}
