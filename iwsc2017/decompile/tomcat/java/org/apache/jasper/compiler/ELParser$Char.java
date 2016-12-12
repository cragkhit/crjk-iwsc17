package org.apache.jasper.compiler;
private static class Char extends Token {
    private char ch;
    Char ( final String whiteSpace, final char ch ) {
        super ( whiteSpace );
        this.ch = ch;
    }
    @Override
    char toChar() {
        return this.ch;
    }
    @Override
    public String toString() {
        return this.whiteSpace + this.ch;
    }
    @Override
    String toTrimmedString() {
        return "" + this.ch;
    }
}
