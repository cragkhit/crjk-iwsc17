package org.apache.jasper.compiler;
public static class ValidAttribute {
    private final String name;
    private final boolean mandatory;
    public ValidAttribute ( final String name, final boolean mandatory ) {
        this.name = name;
        this.mandatory = mandatory;
    }
    public ValidAttribute ( final String name ) {
        this ( name, false );
    }
}
