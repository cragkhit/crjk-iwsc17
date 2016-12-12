package org.apache.jasper.compiler;
private static class Id extends Token {
    String id;
    Id ( final String whiteSpace, final String id ) {
        super ( whiteSpace );
        this.id = id;
    }
    @Override
    public String toString() {
        return this.whiteSpace + this.id;
    }
    @Override
    String toTrimmedString() {
        return this.id;
    }
}
