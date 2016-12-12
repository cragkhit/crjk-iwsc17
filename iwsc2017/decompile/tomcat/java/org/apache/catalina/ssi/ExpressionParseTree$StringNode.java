package org.apache.catalina.ssi;
private class StringNode extends Node {
    StringBuilder value;
    String resolved;
    public StringNode ( final String value ) {
        this.resolved = null;
        this.value = new StringBuilder ( value );
    }
    public String getValue() {
        if ( this.resolved == null ) {
            this.resolved = ExpressionParseTree.access$700 ( ExpressionParseTree.this ).substituteVariables ( this.value.toString() );
        }
        return this.resolved;
    }
    @Override
    public boolean evaluate() {
        return this.getValue().length() != 0;
    }
    @Override
    public String toString() {
        return this.value.toString();
    }
}
