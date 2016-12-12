package org.apache.jasper.compiler;
private static class Token {
    protected final String whiteSpace;
    Token ( final String whiteSpace ) {
        this.whiteSpace = whiteSpace;
    }
    char toChar() {
        return '\0';
    }
    @Override
    public String toString() {
        return this.whiteSpace;
    }
    String toTrimmedString() {
        return "";
    }
    String getWhiteSpace() {
        return this.whiteSpace;
    }
}
