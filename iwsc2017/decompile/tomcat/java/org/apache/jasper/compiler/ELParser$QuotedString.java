package org.apache.jasper.compiler;
private static class QuotedString extends Token {
    private String value;
    QuotedString ( final String whiteSpace, final String v ) {
        super ( whiteSpace );
        this.value = v;
    }
    @Override
    public String toString() {
        return this.whiteSpace + this.value;
    }
    @Override
    String toTrimmedString() {
        return this.value;
    }
}
